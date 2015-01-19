package iZomateRemoteServer;

import iZomateCore.ServerCore.RPCServer.Chunks.BaseChunk;
import iZomateCore.ServerCore.RPCServer.IncomingRequest;
import iZomateCore.ServerCore.RPCServer.OutgoingReply;
import iZomateRemoteServer.Methods.*;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;

/**
 * The RemoteServerMain thread.
 */
public class ServerThread extends Thread
{
	private final Class<?>				mStandardArgs[] = new Class[3];
    private final Socket                mSocket;
	private final InputStream           mIStream;
	private final OutputStream          mOStream;
    private final String                mClientName;
    public static boolean               mDebug          = false;
    private ArrayList<Object>			mHookClasses = new ArrayList<Object>();

    /**
     * Constructs a server (but does not start it).
     *
     * @param socket the master socket to listen on for connection requests.
     * @throws Exception
     */
    public ServerThread(Socket socket) throws Exception
    {
        RemoteServerMain.logDebug("new ServerThread");
        this.mSocket = socket;
        this.mClientName = socket.getInetAddress().getHostName();      
        this.mIStream = this.mSocket.getInputStream();
        this.mOStream = this.mSocket.getOutputStream();
        
        //Add the Hook classes here
        this.mHookClasses.add(new GlobalMethods());
        this.mHookClasses.add(new FileMethods());
        this.mHookClasses.add(new RobotMethods());
        this.mHookClasses.add(new SystemInfoMethods());
        this.mHookClasses.add(new MIDIMethods());

        //For use with getDeclaredMethod()
        if(this.mStandardArgs[0] == null)
        {
            this.mStandardArgs[0] = Class.forName("iZomateCore.ServerCore.RPCServer.IncomingRequest");
            this.mStandardArgs[1] = Class.forName("iZomateCore.ServerCore.RPCServer.OutgoingReply");
            this.mStandardArgs[2] = Class.forName("iZomateRemoteServer.ServerThread");
        }
    }

    /**
     * The main loop of the server thread.
     */
    @Override
	public void run()
    {
    	RemoteServerMain.logDebug("ServerThread.run()\n");

        try
        {
            while (true)
            {
            	RemoteServerMain.logDebug("=============== waiting for next message =============== \n");
                byte [] msg = this.readNextMessage();

                if (msg == null)
                    break; // EOF on socket

                OutgoingReply reply;

                try
                {
                    IncomingRequest req = new IncomingRequest(msg, 0, false);

                    String log = "RQST: " + req._getFunction() + "\n";
                    for (int i = 0; i < req._getCount(); i++)
                    	log += req._getNth(i).toString() + (i == req._getCount() ? "" : "\n");
                    log = log.replaceAll("\t\t", "\t").replaceAll("<B>", "").replaceAll("</B>", "").replaceAll("\n\n", "\n");
                    RemoteServerMain.log(log.substring(0, log.length()-1));

                    reply = this.executeRequest(req);
                }
                catch (Exception x)
                {
                    StringWriter stack = new StringWriter(128);
                    x.printStackTrace();
                    x.printStackTrace(new PrintWriter(stack));
                    String stackStr = x.toString();
                    RemoteServerMain.log(stackStr);
                    reply = new OutgoingReply(-1);
                    if( x.getMessage() != null )
                    	reply.setException(x.getMessage());
                    else if( x.getCause() != null && x.getCause().getMessage() != null)
                    	reply.setException(x.getCause().getMessage());
                    else
                    	reply.setException( stackStr );
                }

                this.sendReply(reply);
            }
        }
        catch (Throwable x)
        {
            System.err.println("fatal exception in server thread for client \"" + this.mClientName + "\":\n");
            x.printStackTrace();
        }

        try
        {
            System.err.println("closing connection with client \"" + this.mClientName + "\"");
            this.mSocket.close();
        }
        catch(Exception foo)
        {
        	/* empty block */
        }

        RemoteServerMain.log("################ connection terminated ################");
    }

    /**
     * Returns the next line of input from the socket, or null on EOF
     *
     * @return the next line of input from the socket, or null on EOF
     */
    private byte [] readNextMessage() throws IOException
    {
        byte[] buff = null;
        int byte0 = -1;
        int readSize = 0;
        int totalBytesRead = 0;
        int transferSize = 0;
        int transferSizeTally = 0;

        // Wait until something is written to the stream
        try
        {
            byte0 = this.mIStream.read();
        }
        catch (java.net.SocketException se)
        {
        	RemoteServerMain.logDebug("....Connection from '" + this.mClientName + "' terminated...\n");
        }

        // Connection Terminated
        if ( byte0 == -1 )
            return null;

        // Continue reading the stream, now that we know there is something available to read
        do
        {
            readSize = this.mIStream.available();  //Assume there is more available to read, since we got the first byte on the stream

            //This prevents infinite read loop and eventual buff overflow if events never stop coming in.
            //Just read enough from the stream to complete the last transfer in the buffer.
            if ( totalBytesRead >= 65535 && (readSize > transferSizeTally - totalBytesRead) )
                readSize = transferSizeTally - totalBytesRead;

            //If we try to read more than 65535 bytes in a single read then the bytes after 65535 are NULL
             if ( readSize > 65535 )
                readSize = 65535;

            // 1st loop: Initialize buff array to readSize, add byte0, then read stream into buff (starting at buff[1])
            if (buff == null)
            {
                readSize += 1; //Add 1 to compensate for byte0 that was already read
                totalBytesRead += readSize; // Keep track of entire stream capacity
                buff = new byte[readSize];
                buff[0] = (byte)byte0;
                this.mIStream.read(buff, 1, readSize - 1);
            }
            else // All subsequent loops: Create new buff array to hold current buff bytes + new bytes, then read stream into buff
            {
                totalBytesRead += readSize; // Keep track of entire stream capacity
                byte[] temp = new byte[totalBytesRead];
                System.arraycopy(buff, 0, temp, 0, buff.length);
                buff = temp;
                this.mIStream.read(buff, buff.length - readSize, readSize);
            }

            // Make sure that there are at least 8 bytes before you attempt to read the 'size' of the transfer
            if(transferSizeTally == 0 && totalBytesRead >= 8)
                transferSizeTally += BaseChunk.convertByte4ToLong(buff, 4, false);

            /*----------------------------------------------------------------------------------------
             * if transferSizeTally == totalBytesRead then we have the exact number of bytes we need
             * and inner while loop will exit if transferSizeTally > totalBytesRead then keep looping
             * because we have more bytes to read.  If transferSizeTally < totalBytesRead then we have
             * more than one transfer in the buffer
             */

            // Add 8 to account for the first 8 bytes of the next transfer
            while (transferSizeTally + 8 < totalBytesRead)
            {
                transferSize = BaseChunk.convertByte4ToInt(buff, transferSizeTally + 4, false);

                //Transfer sizes MUST be 8 or more bytes in size
                if (transferSize < 8) {
                    String emsg = "Error reading stream data:  Detected transfer with size less than 8 at byte " + transferSizeTally;
                    throw new IOException(emsg);
                }
                else
                    transferSizeTally += transferSize;
            }
        } while((transferSizeTally != totalBytesRead));

        // Get just the data by removing the transfer header (first 8 bytes = ID and size) from the stream.
        byte[] data = null;
        data = new byte[transferSizeTally];
        System.arraycopy(buff, 0, data, 0, data.length);
        return data;
   }

    /**
     * SendReply.
     *
     * @param reply
     * @throws Exception
     */
    private void sendReply(OutgoingReply reply) throws Exception
	{
    	String log = "RPLY\n";
        for (int i = 0; i < reply._getCount(); i++)
        	log += reply._getNth(i).toString() + "\n";
        
        RemoteServerMain.log(log.replaceAll("\t\t", "\t").replaceAll("<B>", "").replaceAll("</B>", "").replaceAll("\n\n", "\n"));

	    this.mOStream.write(reply.toReplyTransfer(false), 0, reply.getChunkSize());
	}

    /**
     * ExecuteRequest.
     *
     * @param req
     * @throws Throwable
     */
    private OutgoingReply executeRequest(IncomingRequest req) throws Throwable
	{
	    Method 	method 		= null;
	    Object 	methodClass = null;
	    String 	funcName 	= req._getFunction();
	    RemoteServerMain.logDebug("function = " + funcName);

	    OutgoingReply reply = new OutgoingReply(req.getTransactionUID());

	    if (funcName.isEmpty())
        {
            reply.setException("Request did not have a function specified!");
            return reply;
        }
	    
	    //Find the hook
	    for (Object o : this.mHookClasses)
	    {
		    try
		    {
		    	method = o.getClass().getMethod(funcName, this.mStandardArgs);
		    	methodClass = o;
		    	break; //We must have found it
		    }
		    catch (Exception e) {/**Nothing to do*/}	
	    }
	         	
        if (method != null)
        {
    	    RemoteServerMain.logDebug("invoking method");
    	    method.invoke(methodClass, req, reply, this);	    
    	    RemoteServerMain.logDebug("method returned");
        }
        else
	        reply.setException("Request's function not found : " + funcName);

	    return reply;
	}

    /**
     * Close the socket from remote server side.
     *
     * @throws IOException
     */
    public void closeSocket() throws IOException
    {
        if (this.mSocket!=null)
        {
            this.mSocket.setSoLinger(true,0); // ensures we end the connection via sending RST to peer
            this.mSocket.close();
        }
    }
}
