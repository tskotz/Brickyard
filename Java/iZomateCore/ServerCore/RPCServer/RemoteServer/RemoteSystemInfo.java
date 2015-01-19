package iZomateCore.ServerCore.RPCServer.RemoteServer;

import org.omg.CORBA.BooleanHolder;

import iZomateCore.ServerCore.RPCServer.IncomingReply;
import iZomateCore.ServerCore.RPCServer.OutgoingRequest;
import iZomateCore.ServerCore.RPCServer.RPCServer;

public final class RemoteSystemInfo
{
    private String mTestbedOSarch= "";
    private String mTestbedOSname= "";
    private String mTestbedOSnameFull= "";
    private String mTestbedOSversion= "";
    private String mTestbedJVMarch= "";
    private String mTestbedJVMversion= "";
    private String mTestbedUser= "";
    private String mTestbedUserDir= "";
    private BooleanHolder mIsWin= null;	

	/**
     * The iZomate RPC server from which this object originated.
     */
    protected final RPCServer 		mServer;
    protected final RemoteServer 	mRemoteServer;
    private 		int 			mTestbedMonitors= -1;
    
    /**
     * Constructor.
     * @param remSrvr the Parent RemoteServer object
     * @param srvr the Remote RPCServer.
     * @throws Exception
     */
    public RemoteSystemInfo( RemoteServer remSrvr, RPCServer srvr ) throws Exception {
    	this.mRemoteServer= remSrvr; //We need this to call _runCommand
        this.mServer= srvr;
    }

    //-----------------------------------
	//          Public Methods
	//-----------------------------------
    
    /**
     * Gets the process id for the process with the specified name
     * 
     * @param procName
     * @return The pid
     * @throws Exception
     */
    public long _getProcPid(String procName) throws Exception {
       	OutgoingRequest req= this.mServer._createRequest( "getProcPID" );
        req._addString( procName, "procName" );
        IncomingReply reply= this.mServer._processRequest( req );
        @SuppressWarnings("unused")
		String theName= reply._exists( "Name" ) ? reply._getString( "Name" ): null;
        return reply._getInt64( "pid" );
    }
    
    /**
     * Gets the process info for the specified pid
     * 
     * @param pid
     * @return ProcessInfo
     * @throws Exception
     */
    public ProcessInfo _getProcInfo( long pid ) throws Exception {
    	if( pid == 0 )
    		return null;
    	
       	OutgoingRequest req= this.mServer._createRequest( "getProcInfo" );
        req._addInt64( pid, "pid" );
        IncomingReply reply= this.mServer._processRequest( req );        
        return new ProcessInfo(reply._getString("Name"), 	pid,
        					   reply._getInt64("MemSize"), 	reply._getInt64("MemRes"), 
        					   reply._getInt64("MemShare"), reply._getInt64("MemPageFaults"), 
        					   reply._getInt64("Threads"), 	reply._getInt64("CPUTime"), reply._getDouble("CPUPerc"));
    }
    
    /**
     * Gets the system info.
     * 
     * @return SystemInfo
     * @throws Exception
     */
    public SystemInfo _getSystemInfo() throws Exception
    {
    	IncomingReply reply= this.mServer._createAndProcessRequest("getSystemInfo");
        return new SystemInfo(reply._getDouble("UserTime"), reply._getDouble("SysTime"),
        					  reply._getDouble("NiceTime"), reply._getDouble("WaitTime"), 
        					  reply._getDouble("IdleTime"), reply._getDouble("StolenTime"), 
        					  reply._getInt64("MemFree"), reply._getInt64("MemUsed"), 
        					  reply._getInt64("MemTotal"), reply._getInt64("MemRAM"));
    }
    
	//-----------------------------------
	//          OS Methods
	//-----------------------------------

    /**
     * Returns the number of monitors on the remote system.
     *
     * @return The number of monitors on the remote system
     * @throws Exception
     */
	public int _GetNumOfMonitors() throws Exception {
		if( this.mTestbedMonitors == -1 )
            this.mTestbedMonitors= this.mServer._createAndProcessRequest( "getNumberOfMonitors" )._getInt32(0);

        return this.mTestbedMonitors;
    }

	/**
     * Returns the testbed system's Operating System Architecture Property.
     *
     * @return The string representing the operating system architecture (32-bit, 64-bit)
     * @throws Exception
     */
    public String _GetOSArch() throws Exception {
    	if( this.mTestbedOSarch.equals("") ) {
    		if( this._isMac() )
    			this.mTestbedOSarch= "64-bit";
    		else
    			this.mTestbedOSarch= this.mRemoteServer._runCommand( "wmic", "os", "get", "OSArchitecture" ).split( "\n" )[1];
    	}
    	return this.mTestbedOSarch;
    }

    /**
     * Returns the OS Architecture of the remote RPCServer's JVM
     * 
     * @return The string representing the remote RCPServer's JVM architecture
     * @throws Exception
     */
    public String _GetJVMArch() throws Exception {
    	if( this.mTestbedJVMarch.equals("") )
    		this.mTestbedJVMarch= this.mServer._createAndProcessRequest( "getOSarch" )._getString(0);
    	return this.mTestbedJVMarch;
    }

    /**
     * Returns the testbed system's Operating System full name.
     *
     * @return The string representing the operating system name
     * @throws Exception
     */
    public String _GetOSNameFull() throws Exception {
    	if( this.mTestbedOSnameFull.equals("") ) {
    		if( this._isMac() )
    			this.mTestbedOSnameFull= this.mRemoteServer._runCommand( "sw_vers", "-productName" ) + " " + this.mRemoteServer._runCommand( "sw_vers", "-productVersion" );
    		else
    			this.mTestbedOSnameFull= this.mRemoteServer._runCommand( "wmic", "os", "get", "Caption" ).split( "\n" )[1] + " " + this._GetOSVersion();
    	}
    	return this.mTestbedOSnameFull;
    }

    /**
     * Returns the testbed system's Operating System Short name.
     * 
     * @return
     * @throws Exception
     */
    public String _GetOSName() throws Exception {
    	if( this.mTestbedOSname.equals("") ) {
    		if( this._GetOSNameFull().contains( "Mac OS X" ) )
    			this.mTestbedOSname= "OSX";
    		else if( this._GetOSNameFull().contains( "Microsoft Windows" ) ) {
    			String[] strList= this._GetOSNameFull().split( " " );
    			this.mTestbedOSname= strList[1].substring( 0, 3 ) + strList[2];
    		}
    	}
    	return this.mTestbedOSname;
    }

    /**
     * Returns the testbed system's Operating System Version Property.
     *
     * @return The string representing the operating system version
     * @throws Exception
     */
    public String _GetOSVersion() throws Exception {
    	if( this.mTestbedOSversion.equals("") ) {
    		if( this._isMac() )
    			this.mTestbedOSversion= this.mRemoteServer._runCommand( "sw_vers", "-productVersion" );
    		else
    			this.mTestbedOSversion= this.mRemoteServer._runCommand( "wmic", "os", "get", "Version" ).split( "\n" )[1];
    	}
    	return this.mTestbedOSversion;
    }

    /**
     * Returns the testbed system's Remote RPCServer's JVM Version Property.
     *
     * @return The string representing the operating system version
     * @throws Exception
     */
    public String _GetJVMVersion() throws Exception {
    	if( this.mTestbedJVMversion.equals("") )
    		this.mTestbedJVMversion= this.mServer._createAndProcessRequest( "getOSversion" )._getString(0);
    	return this.mTestbedJVMversion;
    }

    /**
     * Returns the testbed system's current user Property.
     *
     * @return The string representing the operating system version
     * @throws Exception
     */
    public String _GetUser() throws Exception {
    	if( this.mTestbedUser.equals("") )
    		this.mTestbedUser= this.mServer._createAndProcessRequest( "getCurrentUser" )._getString(0);
    	return this.mTestbedUser;
    }

    /**
     * Returns the testbed system's active user directory
     * 
     * @return The testbed system's active user directory 
     * @throws Exception
     */
    public String _GetUserDir() throws Exception {
    	if( this.mTestbedUserDir.equals("") )
			if( this._isWin() )
				this.mTestbedUserDir= this.mRemoteServer._runCommand("echo", "%USERPROFILE%");
			else
				this.mTestbedUserDir= this.mRemoteServer._runCommand("echo $HOME");

    	return this.mTestbedUserDir;
    }

	/**
     * Returns true if the system running the iZomateRemoteServer is a Mac.
     *
     * @return true if the remote system is a Mac
	 * @throws Exception
     */
	public boolean _isMac() throws Exception {
		return !this._isWin();
	}

	/**
     * Returns true if the system running the iZomateRemoteServer is running Windows.
     *
     * @return true if the remote system is a running Windows
	 * @throws Exception
     */
	public boolean _isWin() throws Exception {
		if( this.mIsWin == null ) {
			this.mIsWin= new BooleanHolder();
			String strOS= this.mServer._createAndProcessRequest( "getOSinfo" )._getString(0);

			if( strOS.indexOf("Windows") != -1 )
				this.mIsWin.value= true;
	        else if( strOS.indexOf("Mac") != -1 )
	        	this.mIsWin.value= false;
	        else
	            throw new Error( "Unsupported Platform: " + strOS + ".  Can't get Operating System info." );
		}
		
		return this.mIsWin.value;
	}
	
	/**
     * Returns the platform as string "Mac" or "Win".
     *
     * @return string Mac or Win
	 * @throws Exception
     */
	public String _Platform() throws Exception {
		if( this._isWin() )
			return "Win";
		else
			return "Mac";
	}

}


