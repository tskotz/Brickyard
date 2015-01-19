package iZomateRemoteServer.Methods;

import iZomateCore.ServerCore.RPCServer.IncomingRequest;
import iZomateCore.ServerCore.RPCServer.OutgoingReply;
import iZomateRemoteServer.RemoteServerMain;
import iZomateRemoteServer.ServerThread;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.ArrayList;

/**
 * Every method in this class implements a global RPC subroutine of the same name.
 */
public class GlobalMethods
{
	/**
     * Constructor.
     *
     * @param map the export map
	 * @throws Exception
     */
    public GlobalMethods() throws Exception
    {
    }

    /**
     * Used for file system commands.
     *
     * @param req
     * @param reply
     * @param server
     * @throws Exception
     */
    public void exec(IncomingRequest req, OutgoingReply reply, ServerThread server)
        throws Exception
    {
        File dir = null;
        ArrayList<String> cmds = new ArrayList<String>();
        ArrayList<String> env  = new ArrayList<String>();

        for (int i = 0; i < req._getCount(); ++i) {
        	if( req._getName(i).equals("cmd") )
                cmds.add( req._getString(i) );
        	else if( req._getName(i).equals("envVar") )
        		env.add( req._getString(i) );
        }

        if (req._exists("dir")) {
            dir = new File(req._getString("dir"));
            if (!dir.exists())
                reply.setException("The directory does not exist: " + dir.getPath());
        }

        if (dir == null || dir.exists()) {
            StringBuffer stdOut = new StringBuffer();
            StringBuffer stdErr = new StringBuffer();

            int retCode = 0; //default to error exit code
            try {
                int c;
                ProcessBuilder pb= new ProcessBuilder( cmds.toArray(new String[cmds.size()]) );
                if( dir != null )
                	pb.directory(dir);
                
                Process p= pb.start();
                
                //Process p = Runtime.getRuntime().exec(cmds.toArray(new String[cmds.size()]), (env.size()>0?env.toArray(new String[env.size()]):null), dir);                

                if (req._exists("stdOut") && req._getBoolean("stdOut") && stdOut != null) { //Note: the BufferedInputStream objects are necessary.
                    BufferedInputStream outBuf = new BufferedInputStream(p.getInputStream());
                    while(-1 != (c = outBuf.read()))
                        stdOut.append((char)c);
                }

                if (req._exists("stdErr") && req._getBoolean("stdErr") && stdErr != null) {
                    BufferedInputStream errBuf = new BufferedInputStream(p.getErrorStream());
                    while(-1 != (c = errBuf.read()))
                        stdErr.append((char)c);
                }

                if (req._exists("waitFor") && req._getBoolean("waitFor"))
                	retCode = p.waitFor();
            }
            catch (Exception x) {
            	stdErr = new StringBuffer(x.getMessage());
            }

            reply._addInt32(retCode, "retCode");
            reply._addString(stdOut.toString(), "stdOut");
            reply._addString(stdErr.toString(), "stdErr");
        }
    }

    /**
     * Returns the local system's Operating System Name Property.
     *
     * @param req The IncomingRequest object.
     * @param reply The OutgoingReply object.
     * @param server The ServerThread object.
     */
    public void getOSinfo(IncomingRequest req, OutgoingReply reply, ServerThread server)
    {
        reply._addString(java.lang.System.getProperties().getProperty("os.name"));
    }

    /**
     * Returns the local system's Operating System Architecture Property.
     *
     * @param req The IncomingRequest object.
     * @param reply The OutgoingReply object.
     * @param server The ServerThread object.
     */
    public void getOSarch(IncomingRequest req, OutgoingReply reply, ServerThread server)
    {
        reply._addString(java.lang.System.getProperties().getProperty("os.arch"));
    }

    /**
     * Returns the current user Property.
     *
     * @param req The IncomingRequest object.
     * @param reply The OutgoingReply object.
     * @param server The ServerThread object.
     */
    public void getCurrentUser(IncomingRequest req, OutgoingReply reply, ServerThread server)
    {
        reply._addString(java.lang.System.getProperties().getProperty("user.home"));
    }

    /**
     * Returns the local system's Operating System Version Property.
     *
     * @param req The IncomingRequest object.
     * @param reply The OutgoingReply object.
     * @param server The ServerThread object.
     */
    public void getOSversion(IncomingRequest req, OutgoingReply reply, ServerThread server)
    {
        reply._addString(java.lang.System.getProperties().getProperty("os.version"));
    }

    /**
     * Returns the testbed system's current version of the omate Java sources (omateCore.jar),
     * as defined in the RemoteServerMain Core class.
     *
     * @param req The IncomingRequest object.
     * @param reply The OutgoingReply object.
     * @param server The ServerThread object.
     */
    public void getIzomateVersion(IncomingRequest req, OutgoingReply reply, ServerThread server)
    {
        reply._addInt32(RemoteServerMain.sVersion);
    }

    /**
     * Returns the local system's separator character for filesystem paths.
     *
     * @param req The IncomingRequest object.
     * @param reply The OutgoingReply object.
     * @param server The ServerThread object.
     */
    public void getSeparatorChar(IncomingRequest req, OutgoingReply reply, ServerThread server)
    {
        reply._addString(java.io.File.separator);
    }

}
