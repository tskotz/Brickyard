package iZomateCore.ServerCore.RPCServer.RemoteServer;

import iZomateCore.LogCore.Logs;
import iZomateCore.ServerCore.RPCServer.IncomingReply;
import iZomateCore.ServerCore.RPCServer.OutgoingRequest;
import iZomateCore.ServerCore.RPCServer.RPCServer;
import iZomateCore.UtilityCore.TimeUtils;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * The RemoteServer class provides the ability to communicate with the iZomateRemoteServer
 * on a remote system. The iZomateRemoteServer provides remote control abilities such as launching
 * applications, manipulating files, executing command-line commands, etc.
 */
public final class RemoteServer
{
    //Testbed information
    private int 	mTestbedVersion = -1;
    private String 	mTestbedSeparator = "";

    //Other
    private Logs 				mLogs = null;
    private MIDIrs 				mMIDIrs = null;
    private RemoteRobot 		mRR = null;
    private RemoteSystemInfo 	mRemSysInfo = null;
    private RPCServer 			mServer = null;

	/**
     * Constructor.  Establishes communication with the iZomateRemoteServer
     * on the specified system.
     *
     * @param ip the ip of the system running the iZomateRemoteServer
     * @param logs the Logs object from the current test
	 * @throws Exception
     */
	public RemoteServer(String ip, Logs logs) throws Exception
	{
		this.mServer = new RPCServer(ip, "iZomateRemoteServer", logs);
		this.mLogs = logs;

		//Verify that the iZomateRemoteServer version on testbed is up-to-date
		this.mLogs._TransactionLog()._log("Connection successful.  iZomateRemoteServer version: " + this.getVersion());
	}
	
	/**
     * Sets the minimum time interval between process request calls allowing user to "slow down" RPCServer

	 * @param throttleTimeMS
	 */
	public void _SetProcessRequestThrottle( int throttleTimeMS ) {
		this.mServer._SetProcessRequestThrottle( throttleTimeMS );
	}
	
	/**
     * Returns the current minimum time interval between process request calls
	 * 
	 * @return mProcessRequestThrottleMS
	 */
    public int _GetProcessRequestThrottle() {
    	return this.mServer._GetProcessRequestThrottle();
    }
    
	/**
	 * The MIDI methods that can be executed on the remote system
	 * 
	 * @return MIDIrs
	 * @throws Exception
	 */
	public MIDIrs _MIDI() throws Exception
	{
		if (this.mMIDIrs == null)
			this.mMIDIrs = new MIDIrs(this.mServer);
		
		return this.mMIDIrs;
	}

	/**
	 * The Remote Robot methods for controlling the remote system
	 * 
	 * @return RemoteRobot
	 * @throws Exception
	 */
	public RemoteRobot _Robot() throws Exception
	{
		if (this.mRR == null)
			this.mRR = new RemoteRobot(this.mServer);

		return this.mRR;
	}

	/**
	 * The system info methods for the remote system
	 * 
	 * @return RemoteSystemInfo
	 * @throws Exception
	 */
	public RemoteSystemInfo _SysInfo() throws Exception
	{
		if (this.mRemSysInfo == null)
			this.mRemSysInfo = new RemoteSystemInfo(this, this.mServer);
		
		return this.mRemSysInfo;
	}

    //-----------------------------------
	//          Exec Methods
	//-----------------------------------


    public int _commandLine(String command, boolean waitFor) throws Exception {
        return this._commandLine(new String[]{command}, null, null, waitFor);
    }

    public int _commandLine(String[] commands, boolean waitFor) throws Exception {
        return this._commandLine(commands, null, null, waitFor);
    }

    /**
     * Executes the given command on the remote system and waits for it to complete. This method is
     * implemented in the iZomateRemoteServer by SystemUtils.exec. Note that the command is not
     * automatically executed within a shell, so that features like i/o redirection (using angle brackets)
     * will not work unless you issue a command that explicitly invokes a shell.
     * <b>Java's Exec method separates the command String on whitespace, even if you wrap a String with
     * whitespace within quotes.  So, for example, if you need to execute an application with whitespace
     * in the path, you must use the version of exec that takes in a String array, not this version.</b>
     *
     * @param command the command to execute on the remote system.
     * @param stdOut if this parameter is not null, the standard output of the command will be appended to
     *            this string buffer.
     * @param stdErr if this parameter is not null, the standard error output of the command will be appended
     *            to this string buffer.
     * @return the exit code from the process (normally 0 on success).
     * @throws Exception
     */
    public int _commandLine(String[] commands, StringBuffer stdOut, StringBuffer stdErr, boolean waitFor) throws Exception {   
        return this._commandLine(commands, null, null, stdOut, stdErr, waitFor, TimeUtils._GetTimeout( 60 ));
    }
    
    /**
     * Executes the given command on the remote system and waits for it to complete. This method is
     * implemented in the iZomateRemoteServer SystemUtils.exec. Note that the command is not
     * automatically executed within a shell, so that features like i/o redirection (using angle brackets)
     * will not work unless you issue a command that explicitly invokes a shell.
     * <b>Java's Exec method separates the command String on whitespace, even if you wrap a String with
     * whitespace within quotes.  So, for example, if you need to execute an application with whitespace
     * in the path, you must use the version of exec that takes in a String array, not this version.</b>
     *
     * @param command the command to execute on the remote system.
     * @param workingDir the name of the working directory for the command. If this parameter is null, the
     *            subprocess (command) inherits its working directory from the caller.
     * @param stdOut if this parameter is not null, the standard output of the command will be appended to
     *            this string buffer.
     * @param stdErr if this parameter is not null, the standard error output of the command will be appended
     *            to this string buffer.
     * @return the exit code from the process (normally 0 on success).
     * @throws Exception
     */
    public int _commandLine(String command, String workingDir, StringBuffer stdOut, StringBuffer stdErr) throws Exception {
        return this._commandLine(new String[] {command}, null, workingDir, stdOut, stdErr, false, TimeUtils._GetTimeout( 60 ));
    }

    /**
     * Executes the given command on the remote system and waits for it to complete. This method is
     * implemented in the iZomateRemoteServer by SystemUtils.exec. Note that the command is not
     * automatically executed within a shell, so that features like i/o redirection (using angle brackets)
     * will not work unless you issue a command that explicitly invokes a shell.
     * <b>Java's Exec method separates the command String on whitespace, even if you wrap a String with
     * whitespace within quotes.  So, for example, if you need to execute an application with whitespace
     * in the path, you must use the version of exec that takes in a String array, not this version.</b>
     *
     * @param command the command to execute on the remote system.
     * @param envVars an array of strings, each of which contains an environment variable setting of the form
     *            <code><i>name=value</i></code>. If this parameter is null, the subprocess (command)
     *            inherits its environment from the caller.
     * @param workingDir the name of the working directory for the command. If this parameter is null, the
     *            subprocess (command) inherits its working directory from the caller.
     * @param stdOut if this parameter is not null, the standard output of the command will be appended to
     *            this string buffer.
     * @param stdErr if this parameter is not null, the standard error output of the command will be appended
     *            to this string buffer.
     * @param timeout The time this process should wait before raising an error.
     * @return the exit code from the process (normally 0 on success).
     * @throws Exception
     */
    public int _commandLine(String[] commands, String[] envVars, String workingDir, StringBuffer stdOut, StringBuffer stdErr, boolean waitFor, int timeout) throws Exception
    {
        OutgoingRequest req = this.mServer._createRequest("exec");
        for (int i= 0; i < commands.length; i++)
            req._addString(commands[i], "cmd");

        if (stdOut != null)
        	req._addBoolean(true, "stdOut");
        if (stdErr != null)
        	req._addBoolean(true, "stdErr");
        if (workingDir != null)
            req._addString(workingDir, "dir");
        if (waitFor)
        	req._addBoolean(true, "waitFor");

        for (int i = 0; envVars != null && i < envVars.length; i++)
           	req._addString(envVars[i], "envVar");

        req._setTimeoutVal(timeout);

        IncomingReply reply = this.mServer._processRequest(req);

        if (stdOut != null)
            stdOut.append(reply._getString("stdOut"));
        if (stdErr != null)
            stdErr.append(reply._getString("stdErr"));
        
        return reply._getInt32("retCode");
    }

    /**
     * Executes a command.  Returns stdOut.  Throws exception if stdErr detected.
     * 
     * @param commands
     * @return stdOut
     * @throws Exception stdErr
     */
    public String _runCommand( String... args ) throws Exception {
    	return this._runCommand( null, args );
    }

    /**
     * Executes a command running in the specified working dir.  Returns stdOut.  Throws exception if stdErr detected.
     * 
     * @param workingDir
     * @param commands
     * @return stdOut
     * @throws Exception stdErr
     */
    public String _runCommand( RemoteFile workingDir, String... args ) throws Exception {
		StringBuffer stdOut= new StringBuffer();
		StringBuffer stdErr= new StringBuffer();
    	String[] cmds= new String[args.length + 2];

    	for( int i=0; i < args.length; ++i )
    		cmds[i+2]= args[i];

		// If Win, run command inside a cmd.exe shell
    	if( this._SysInfo()._isWin()) {
	    	cmds[0]= "cmd.exe";
	    	cmds[1]= "/C";
    	}
    	else {
	    	cmds[0]= "sh";
	    	cmds[1]= "-c";
    	}

		this._commandLine( cmds, null, workingDir==null?null:workingDir._getAbsolutePath(), stdOut, stdErr, true, TimeUtils._GetTimeout( 60 ) );

		if( stdErr.length() > 0)
			throw new Exception( stdErr.toString() );
		
		return stdOut.toString().trim();
    }
    
    /**
     * Returns a RemoteFile, representing a file on the remote testbed system.
     * A RemoteFile allows you to remotely call most of the methods of the
     * standard Java {@link java.io.File File} class.
     *
     * @param pathName the path name of the file, relative to the system on which the RemoteServer is running
     * @return the RemoteFile object
     * @throws Exception
     */
    public RemoteFile _createRemoteFile(String pathName) throws Exception {
    	return new RemoteFile(this.mServer, pathName);
    }

    /**
     * Force quits the specified application.
     *
     * @param processName the process name of the application to force quit
     * @return true if the process is no longer running, false if the process could not be stopped
     * @throws Exception
     */
    public boolean _forceQuit(String processName) throws Exception
    {
    	return this._ForceQuit(processName, 1);
    }

    /**
     * Force quits the specified application.
     *
     * @param processName the process name of the application to force quit
     * @param attempts how many times to attempt to force quit the process (with a 5 second sleep between each attempt)
     * @return true if the process is no longer running, false if the process could not be stopped
     * @throws Exception
     */
    public boolean _ForceQuit(String processName, int attempts) throws Exception
    {
    	int loop = 0;
    	boolean success = false;

    	//Try to quit up to a max number of 'attempts' times
    	while (loop < attempts && !success)
    	{
    		//Attempt to force quit the app
    		this.forceQuit(processName);

    		//Verify if app quit succesfully
			success = this._IsAppRunning(processName) == 0;

			//If app is still running, sleep for 5 seconds and try again
			if (!success && ++loop != attempts)
    			TimeUtils.sleep( TimeUtils._GetTimeout( 5 ) );
    	}

    	return success;
    }

    /**
     * Force quits the specified application.
     *
     * @param processName the process name of the application to force quit
     * @throws Exception
     */
    private void forceQuit(String processName) throws Exception
    {
    	StringBuffer stdOut = new StringBuffer();
        StringBuffer stdErr = new StringBuffer();

        //Mac: Get PID and call 'kill'
    	if (this._SysInfo()._isMac()) {
    		//Get process list
    	    String pname= "[" + processName.charAt(0) + "]" + processName.substring(1);
    	    //Get process list on remote testbed system
    	    this._commandLine(new String[] {"/bin/sh", "-c", "ps -x | grep '" + pname + "' | grep -vi Java"}, stdOut, stdErr, true);
            ArrayList<String> pids = new ArrayList<String>();
            String[] temp = (stdOut.toString()).split("\n");

            //Get PID of matching process name
            for (int i=0; i<temp.length; i++) {
                if (temp[i].toLowerCase().contains(processName.toLowerCase())) {
                    String[] str = temp[i].split("[^0-9]");
                    if (str[2].length() > 0)
                        pids.add(str[2]);
                    else if (str[1].length() > 0)
                    	pids.add(str[1]);
                    else
                    	pids.add(str[0]);
                }
            }

            //Call 'kill' on PID
            for (String pid: pids) {
            	this.mLogs._ResultLog()._logDebug("kill " + pid);
            	this._commandLine(new String[]{"kill", pid}, stdOut, stdErr, true);
            }	
        }
        else //PC: call 'taskkill'
        {
        	this.mLogs._ResultLog()._logDebug("taskkill /F /T /IM " + processName);
        	this._commandLine(new String[]{"taskkill", "/F", "/T", "/IM", "\"" + processName + "\""}, stdOut, stdErr, true);
        }
    }

    /**
     * Returns the default path of the AppLauncher utility on the remote Testbed system.
     *
     * @return the expected path of the AppLauncher on the remote Testbed system
     * @throws Exception
     */
    public String xgetAppLauncher() throws Exception
    {
    	if (this._SysInfo()._isMac())
    		return "/Applications/iZomate/AppLauncher.pl";
    	else
    		return "C:\\iZomate\\AppLauncher.exe";
    }

	 /**
     * Returns the testbed system's separator character for file system paths.
     *
     * @return the testbed system's separator character for file system paths
     * @throws Exception
     */
    public String _getSeparatorChar() throws Exception
    {
        if (this.mTestbedSeparator.equals(""))
            this.mTestbedSeparator = this.mServer._createAndProcessRequest("getSeparatorChar")._getString(0);
        return this.mTestbedSeparator;
    }

    /**
     * Returns the current version of the iZomateRemoteServer on the testbed system.
     *
     * @return the current version of the iZomateCore.jar java sources on the testbed system
     * @throws Exception
     */
    public int getVersion() throws Exception
    {
        if (this.mTestbedVersion == -1)
            this.mTestbedVersion = this.mServer._createAndProcessRequest("getIzomateVersion")._getInt32(0);

        return this.mTestbedVersion;
    }
    
    /**
     * 
     * @param appFile
     * @param args
     * @throws Exception
     */
    public void _LaunchApp( RemoteFile appFile, String args ) throws Exception {      	
		if( this._SysInfo()._isMac() )
			this._commandLine( args==null ? new String[]{"open", appFile._getAbsolutePath()}
										  :	new String[]{"open", appFile._getAbsolutePath(), args}, false);
		else {
			//TODO: Figure out why we can't load audio file from menu when we launch this way?!?!?!
			//this._commandLine( args==null ? new String[]{"cmd.exe", "/c", "start \"\" \"" + appFile._getPathAndName() + "\""}
			//							  : new String[]{"cmd.exe", "/c", "start \"\" \"" + appFile._getPathAndName() + "\" " + args}, false);
			// Open the run menu so we can type in the Open Box
			this._Robot()._keyType( KeyEvent.VK_WINDOWS, KeyEvent.VK_R );
			// Open the start menu so we can type in the Search Box - Replaced with above but thought this might be useful to keep around
			//this._Robot()._keyType( KeyEvent.VK_CONTROL, KeyEvent.VK_ESCAPE );
			TimeUtils.sleep( .25 );
			this._Robot()._keyType( "\"" + appFile._getPathAndName() + "\" " + (args==null?"":args) + "\n" );
		}		
    }

    /**
     * Checks if the specified application is running.
     *
     * @param processName the process name of the application to check
     * @return the pid of the application if it is still running or 0 if not running
     * @throws Exception
     */
    public int _IsAppRunning( String processName ) throws Exception {
	    //Get process list on remote testbed system
	    if( this._SysInfo()._isMac() )
	    	return this._IsAppRunningMac( processName );
	    else
	    	return this._IsAppRunningWin( processName );
	}

    /**
     * Checks if the specified application is running.
     *
     * @param processName the process name of the application to check
     * @return the pid of the application if it is still running or 0 if not running
     * @throws Exception
     */
    public int _IsAppRunningMac( String processName ) throws Exception {
	    StringBuffer stdOut = new StringBuffer();
	    StringBuffer stdErr = new StringBuffer();
	    
	    String pname= "[" + processName.charAt(0) + "]" + processName.substring(1);
	    //Get process list on remote testbed system
	    this._commandLine(new String[] {"/bin/sh", "-c", "ps -x | grep '" + pname + "' | grep -vi Java"}, stdOut, stdErr, true);

	    //Check if the application process is in the list
	    if( stdOut.length() > 0 ) {
	    	for( int start= 0; start < stdOut.length(); ++start ) {
	    		if( Character.isDigit( stdOut.charAt( start ) ) ) {
	    			for( int end= start+1; end < stdOut.length(); ++end ) {
	    	    		if( !Character.isDigit( stdOut.charAt( end ) ) )
	    	    			return Integer.valueOf( stdOut.substring( start, end ) );
	    			}
	    		}
	    	}
	    }

	    return 0; // not found
	}

    /**
     * Checks if the specified application is running.
     *
     * @param processName the process name of the application to check
     * @return true if the application is still running
     * @throws Exception
     */
    public int _IsAppRunningWin( String processName ) throws Exception {
    	int pid = 0;
	    StringBuffer stdOut = new StringBuffer();
	    StringBuffer stdErr = new StringBuffer();

	    //Get process list on remote testbed system
	    this._commandLine(new String[]{"tasklist", "/FI", "\"IMAGENAME eq " + processName + "\""}, stdOut, stdErr, true);

	    //Check if the application process is in the list
	    if( !stdOut.toString().contains( "INFO: No tasks are running which match the specified criteria." ) && !stdOut.toString().isEmpty() ) {
	    	//Lets grab the pid
	    	int offset = stdOut.indexOf( processName.length()>25?processName.substring(0, 25):processName ) + 25;
	    	while (stdOut.charAt(++offset) == ' ' );
	    	int start = offset; 	
	    	while (stdOut.charAt(++offset) != ' ' );
	    	pid = Integer.valueOf(stdOut.substring(start, offset));	    	
	    }

	    return pid;
	}
	
	/**
     * Minimizes/hides all windows on the remote testbed system.
     *
     * @throws Exception
     */
	public void _minimizeAllWindows() throws Exception
	{
		if (this._SysInfo()._isWin())
		{
			try
			{
				this._Robot()._keyPress(KeyEvent.VK_WINDOWS);
				this._Robot()._keyPress(KeyEvent.VK_M);
			}
			finally
			{
				this._Robot()._keyRelease(KeyEvent.VK_M);
				this._Robot()._keyRelease(KeyEvent.VK_WINDOWS);
			}
		}
		else
			this._commandLine( new String[] {"osascript", "-e", "tell application \"System Events\" to key code 103"}, null, null, true );
	}
	
	/**
	 * 
	 * @param strAppName
	 * @throws Exception
	 */
	public void _ActivateApp( String strAppName ) throws Exception {
		if( this._SysInfo()._isMac() )
			this._commandLine( new String[] {"osascript", "-e", "tell application \"" + strAppName + "\" to activate"}, null, null, true );
		// don't know how to do this on win yet ;-(
	}

	/**
	 * Returns the server's Host system's IP.
	 *
	 * @return the server's host system IP
	 */
	public final String _GetMachineIP()
	{
		return this.mServer._GetMachineIP();
	}

	/**
	 * Returns the server's Host system's Name.
	 *
	 * @return the server's host system name
	 */
	public final String _GetMachineName()
	{
        return this.mServer._GetMachineName();
	}

	/**
	 * Returns the server's port.
	 *
     * @return the port number
     */
    public int getPort()
    {
    	return this.mServer._getPort();
    }

}
