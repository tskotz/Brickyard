package iZomateRemoteServer;

import iZomateCore.ServerCore.RPCServer.Chunks.BaseChunk;
import iZomateCore.ServerCore.RPCServer.RPCServer;

import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;

public class RemoteServerMain implements Runnable
{
	private static Boolean		   	sDebug         = true;
    private static PrintStream		sLogStream     = null; //optional stream used to print to log file
    public static int				sVersion	   = 12;
    private static String			sServerName    = "iZomateRemoteServer";
    private int						mPort    	   = 0;
    
    public RemoteServerMain( int port ) {
    	this.mPort= port;
    }

    public RemoteServerMain() {
    	this( 54320 ); //default port
    }

    /**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception
	{	
		int port= Integer.valueOf( args[0] );
		
		//Log java version to console
	    Properties p = System.getProperties();
	    String version = p.getProperty("java.version");
	    String home = p.getProperty("java.home");
	    System.out.println("/**********************************************\\");
	    System.out.println("|*             "+ sServerName +"              *|");
	    System.out.println("\\**********************************************/\n");
	    System.out.println("         Java Location: " + home);
	    System.out.println("          Java Version: " + version);
	    System.out.println("                  Port: " + String.valueOf(port));
	    System.out.println("       RServer Version: " + sVersion);
	    System.out.println("   iZomateCore Version: " + RPCServer.sRPCCoreVersion);
	    System.out.println("\n");

	    //Go!
	    mainLoop( port );
	}

	/**
	 * The main execution loop for the server.
	 * <br>
	 * This doesn't actually handle any RPC requests. It just waits for connection
	 * requests and passes them off to a ServerThread to handle subsequent RPC traffic.
	 *
	 * @throws Exception
	 */
	private static void mainLoop( int port ) throws Exception
	{
	    ServerSocket master= new ServerSocket(port);
	    
	    while( true ) {
	    	//Listen for a connection
	        Socket sock= master.accept();
	        System.out.println( "################ initializing connection ################" );
	        System.out.print( "from client \"" + sock.getInetAddress().getHostName() + "\" at " + sock.getInetAddress().getHostAddress() );

			byte[] buff= new byte[7 + sServerName.length()];
	        
			System.arraycopy(BaseChunk.convertShortToByte2( 1, false ), 	 		0, buff, 0, 2);	//endianess flag
			System.arraycopy(BaseChunk.convertIntToByte4( sVersion, false ), 		0, buff, 2, 4); //version
			System.arraycopy(BaseChunk.convertIntToByte1( sServerName.length() ),	0, buff, 6, 1); //name length
			System.arraycopy(sServerName.getBytes(), 								0, buff, 7, sServerName.length()); //name
	        	       
			//Start the handshake process
	        sock.getOutputStream().write( buff );

	        //Wait for the response
	        buff= new byte[8];
	        int len = sock.getInputStream().read( buff, 0, buff.length );
            String response = new String( buff, 0, len<0?0:len );
	        
            if( response.equals( "accepted" ) ) {
            	System.out.println(" : Accepted.  Starting server thread.\n");
            	ServerThread srvr= new ServerThread(sock);
	    	    ServerThread.mDebug= sDebug;
	    	    srvr.start();
	        }
            else if( response.equals("stop") ) {
            	System.out.println( "\nProcessing stop request" );
            	master.close();
            	sock.close();
            	break;
            }
	        else
	        	System.out.println( " : Rejected\n" );
	    }
    	System.out.println( "\nRemoteServer has been shutdown\n" );
	}
	
	//-----------------------------------
	//            Logging
	//-----------------------------------

	/**
	 * Logs activity to standard out and/or the log file.
     *
     * @param s The string to write to the log/sys out
     */
    protected static void log( String s )
    {
    	if (s != null && !s.equals(""))
    	{
	        String dateTime = getDateTime();

	        String output = ("[" + dateTime + "] " + s);

	        if (sLogStream != null)
	        	sLogStream.println(output);
			        
	        System.out.println(output);
    	}
    }

    /**
     * Logs activity to standard out and/or the log file,
     * if the debug command line switch was specified.
     *
     * @param s The string to write to the log/sys out
     */
    protected static void logDebug( String s )
    {
        if (sDebug)
        	log(s);
    }

    /**
     * Returns the current date and time as a string.
     *
     * @return the current date and time
     */
    private static String getDateTime()
    {
    	GregorianCalendar cal = new GregorianCalendar();
        String dateTime = "";

        dateTime += Integer.toString(cal.get(Calendar.YEAR));
        dateTime += "-";
        dateTime += Integer.toString(cal.get(Calendar.MONTH) + 1); //the Calendar.MONTH appears to be off by one
        dateTime += "-";
        dateTime += Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
        dateTime += " ";

        String temp;
        temp = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
        dateTime += (temp.length() == 1) ? ("0" + temp + ":") : (temp + ":");
        temp = Integer.toString(cal.get(Calendar.MINUTE));
        dateTime += (temp.length() == 1) ? ("0" + temp + ":") : (temp + ":");
        temp = Integer.toString(cal.get(Calendar.SECOND));
        dateTime += (temp.length() == 1) ? ("0" + temp) : temp;

        return dateTime;
    }

	@Override
	public void run() {
		try {
			main( new String[] {String.valueOf( this.mPort )} );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	

}

