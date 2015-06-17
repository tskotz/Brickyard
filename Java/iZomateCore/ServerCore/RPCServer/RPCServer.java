package iZomateCore.ServerCore.RPCServer;

import iZomateCore.LogCore.Logs;
import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.*;
import iZomateCore.ServerCore.RPCServer.Chunks.BaseChunk;
import iZomateCore.UtilityCore.TimeUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Calendar;


public class RPCServer
{
	static public double		sRPCCoreVersion= 1.0041;

	private String 				mHostName;
	private String 				mHostIP;
	private String 				mRPCServerID;
	private Socket				mSock= null;
	private int					mPort= 0;
	private InputStream			mIStream= null;
	private OutputStream		mOStream= null;
	private	int					mClientVersion;
	private boolean				mSwapBytes;		//The endianess of the transfer
	private int 				reqestUID = 0; //unique id generated for Request transfers only
	private int 				reqRefCount = 0; //use the accessor methods to get, increment and decrement value
	private long 				mNotificationIdleTime = 0; 	//Used to calculate desired idle time waiting for a RPLY from the application
    private	WaitForEventsList   mWaitForList = new WaitForEventsList();
	private TransactionList		mTransactions = new TransactionList();
    private NotificationCallbackRegistry	mNtfcnCBRegistry = new NotificationCallbackRegistry();
    private Logs 				logs = null;
    private int					mProcessRequestThrottleMS= 0;
    private long				mProcessRequestTimestamp= 0;
    private boolean				mVerboseLogging= false;
    
	/**
     * The current required version of the RemoteServer and RPCServer on the testbed system
     */
    private	int					mMiniZomateRemoteServerVers= 9;
    private	int					mMinRPCServerVers= 7;

	public RPCServer(String host, String appName, Logs logs) throws Exception {
		// Remove the webserver port if in host address
		if( host.contains(":") )
			host= host.substring(0, host.indexOf(":"));
		
		this.mHostIP = java.net.InetAddress.getByName(host).getHostAddress();
		this.mHostName = java.net.InetAddress.getByName(host).getHostName();
		this.mRPCServerID = appName;
        this.logs = logs;
        this._setVerboseLogging( this.mVerboseLogging );
	}

	/**
	 * Caution!!! This is used for connecting to RPCServers that we don't care what their server id is.
	 * This is used for force shutting them down
	 * @param host
	 * @param logs
	 * @throws Exception
	 */
	public RPCServer(String host, int port, Logs logs) throws Exception {
		this.mHostIP = java.net.InetAddress.getByName(host).getHostAddress();
		this.mHostName = java.net.InetAddress.getByName(host).getHostName();
		this.mPort= port;
		this.mRPCServerID = null;
        this.logs = logs;
        this._setVerboseLogging( this.mVerboseLogging );
	}

	/**
	 * Turns on/off transaction list logging
	 * @param bState
	 */
	public void _setVerboseLogging( boolean bState ) {
		if( bState )
	        this.mTransactions.setVerboseLog( this.logs._TransactionLog() );
		else
	        this.mTransactions.setVerboseLog( null );
	}
	
	/**
	 * Returns True if there is a remote RPC connection
	 * @return true/false
	 */
	public boolean _isConnected() {
		return (this.mSock != null);
	}
	
    /**
     * @return the remote DLL version on the testbed system
     */
    public int _getClientVersion() {
        return this.mClientVersion;
    }

    /**
     * @return the port number
     */
    public int _getPort() {
        return this.mPort;
    }

    /**
    * Returns the host system IP.
    *
    * @return the host system ip
    */
   public String _GetMachineIP() {
       return this.mHostIP;
   }

    /**
     * Returns the host system name.
     *
     * @return the host system name
     */
    public String _GetMachineName() {
        return this.mHostName;
    }
    
    /**
     * Sets the minimum time interval between process request calls allowing user to "slow down" RPCServer
     * 
     * @param throttleTimeMS
     */
    public void _SetProcessRequestThrottle( int throttleTimeMS ) {
    	this.mProcessRequestThrottleMS= throttleTimeMS;
    }

    /**
     * Returns the current minimum time interval between process request calls
     * 
	 * @return mProcessRequestThrottleMS
     */
    public int _GetProcessRequestThrottle() {
    	return this.mProcessRequestThrottleMS;
    }

    /**
     * Closes and nulls out the socket
     * 
     * @throws Exception
     */
    public void _Shutdown() throws Exception {
    	if( this.mSock != null ) {
			this.mSock.close();
			this.mSock= null;
    	}
    }

    /**
     * Attempts to connect to the remote C++ RPCServer running on the specified port ignoring the server id and tell it to shutdown its connection and free up the port.
     * @throws Exception
     */
    public void _ShutdownRemoteSocket() throws Exception
    {
		if (this.connectToSocket( this.mPort )) {	
			System.out.println("Shutting down server: " + this.mRPCServerID + " on port: " + this.mPort);	
			this.logs._TransactionLog()._logBold( "Shutting down server: " + this.mRPCServerID + " on port: " + this.mPort );
	    	OutgoingRequest req= this._createRequest( "RPC_ForceSocketShutdown" );
	    	req._setEventNotification( EventSubType.RPCServerEvent, "Stopped" );
	    	req._setNoReply();
	        this._processRequest( req );
		}
		this._Shutdown();
    }

	/**
	 * Attempts to connect to the remote RPC in the client application
	 * @return true if successful else false
	 * @throws Exception
	 */
	public boolean _connectToClient() throws Exception {
		if (this._isConnected())
			return true;
		
		if (this.mRPCServerID.equals("iZomateRemoteServer"))
			return this.connectToRemoteServerRPCServer();
		else
			return this.connectToDLLRPCServer();
	}
	
	/**
	 * Attempts to connect to the RemoteServer RPC on its designated port
	 * 
	 * @throws Exception
	 */
	private boolean connectToRemoteServerRPCServer() throws Exception {
		if (this.connectToSocket(54320))
			return true;
		
		System.out.println("Connection FAILED! " + this.mRPCServerID + "  " + this.mHostName);
		return false;
	}

	/**
	 * Attempts to connect to the remote RPC on one of the open ports
	 * 
	 * @throws Exception
	 */
	private boolean connectToDLLRPCServer() throws Exception {
		int ports[] = {54321, 54322, 54323, 54324, 54325};
		
		for (int p: ports)
			if (this.connectToSocket(p))
				return true;
		
		System.out.println("Failed to open a connection to " + this.mRPCServerID + " on " + this.mHostName + "(" + this.mHostIP + ")");
		return false;
	}
	
	/**
	 * Connects to the socket, checks version for minimum support requirements.
	 *
	 * @throws Exception
	 */
	private boolean connectToSocket(int port) throws Exception {
		boolean connected = false;
		this.mPort = port;

		if (this.mHostName == null || this.mHostName.equals(""))
			throw new Exception("No host system was specified.");

		System.out.println("Attempting to connect to '" + this.mRPCServerID + "' on " + this.mHostName + "(" + this.mHostIP + ") on port " + this.mPort);

		try {
			SocketAddress sockaddr = new InetSocketAddress(this.mHostIP, this.mPort);
			this.mSock = new Socket();
			this.mSock.connect(sockaddr, 2000);
			connected = this.performHandshake();
		}
		catch (Exception e) {
			this._Shutdown();
			System.out.println("Could not open socket on port " + this.mPort + ": " + e.getMessage());
			// Don't swallow the version mismatch exception
			if( e.getMessage().contains( "The required version is " ) )
				throw e;
		}
		
		return connected;
	}
	
	/**
	 * Performs the handshake on the new socket connection
	 * @return true if successful else false
	 * @throws Exception
	 */
	private boolean performHandshake() throws Exception {
		boolean bAccepted = false;
		
		this.mIStream = this.mSock.getInputStream();
		this.mOStream = this.mSock.getOutputStream();

		// check socket for incoming handshake.  Server will return a short value of 1 so that we can determine
		// if multi byte data types need to be byte swapped (big vs. little endian)
		byte[] buffer = this.simpleReadSocket(15000);
		
		if (buffer == null)
			System.out.println("We could not connect with the RPC server inside the application.  It does not appear to be running.");
		else {	
			// Check for the handshake:  short(endianess check), int32(version), byte(size of app name), string(app name)			
			//	  bytes 0 thru 1 	= the short 1 value for the byte order test
			//	  bytes 2 thru 5 	= the version number as int
			//		byte  6			= size of app name we are connecting to
			//		bytes 7 thru end= the app name
			
			if ( BaseChunk.convertByte2ToShort(buffer, 0, false) == 1 )
				this.mSwapBytes = false;
			else if ( BaseChunk.convertByte2ToShort(buffer, 0, true) == 1 )
				this.mSwapBytes = true;
			else {
				String strMessage= new String(buffer, 0, buffer.length);
				System.out.println("Rejected: " + strMessage );
				return false;
			}

			//Now that we know the byte order we can get the version number
			if (buffer.length < 8)
				System.out.println("Invalid handshake buffer length: " + buffer.length);
			
			this.mClientVersion = BaseChunk.convertByte4ToInt(buffer, 2, this.mSwapBytes);
			
			String serverID= new String(buffer, 7, buffer[6]);
			this.logs._TransactionLog()._echo()._logBold( "Connected to server ID:  '" + serverID + "' on " + this.mHostName + "(" + this.mHostIP + ") on port " + this.mPort + " running version: " + this.mClientVersion );

			// If no server id was specified then we want to connect to what ever server is running on the port
			if( this.mRPCServerID == null )
				 this.mRPCServerID= serverID;
			
			if( serverID.equals( this.mRPCServerID ) ) {
				if( serverID.equals( "iZomateRemoteServer" ) && this.mClientVersion < this.mMiniZomateRemoteServerVers ) {
					this.mOStream.write("rejected".getBytes());
					throw new Exception("The iZomateRemoteServer.jar (version " + this.mClientVersion + ") installed on the testbed '" + this.mHostName + "' is no longer supported.\n" + 
							"The required version is " + this.mMiniZomateRemoteServerVers + " or higher.");
				}
				if( this.mClientVersion < this.mMinRPCServerVers ) {
					this.mOStream.write("rejected".getBytes());
					throw new Exception("The iZTestAutomation dll/bundle (version " + this.mClientVersion + ") installed on the testbed '" + this.mHostName + "' is no longer supported.\n" + 
							"The required version is " + this.mMinRPCServerVers + " or higher.");
				}

				this.logs._TransactionLog()._echo()._logBold( "Connection Accepted" );
				//tell RPC Server we are ready to start receiving transactions.  Prevents transactions being sent before handshake has completed.
				this.mOStream.write("accepted".getBytes());
				bAccepted = true;
			}
			else {
				this.logs._TransactionLog()._echo()._log( "Connection Rejected: Server ID Mismatch: Expected '" + this.mRPCServerID + "' but found '" + serverID + "'" );
				this.mOStream.write("rejected".getBytes());
			}
		}
   
		return bAccepted;
	}   

	/**
	 * Reads the bytes, if any, from the socket.
	 * This is tailored for small reads which are not in the Transfer form such as the initial handshake.
	 *
	 * @param timeout how long to attempt reading the socket, in milliseconds, before giving up. If < 0, uses <code>defaultTimeout</code> instead.
	 * @return the bytes from the socket
	 * @throws Exception
	 */
	private byte[] simpleReadSocket(int timeout) throws Exception {
		int messageSize = 0;
		byte [] buff = null;
		byte byte1 = 0;

		if(timeout < 0)
			timeout = TimeUtils._GetDefaultTimeoutMS();

		this.mSock.setSoTimeout(timeout); //timeout is in millisecods

		try	{
			byte1 = (byte)this.mIStream.read(); //wait for the first byte
		}
		catch (SocketTimeoutException e) {
			throw new Exception ("The call to 'Simple' readSocket timed out waiting for reply from " + this.mRPCServerID + ".");
		}

		//Make sure there was actually something read from the socket
		if (byte1 != -1) {
			messageSize = this.mIStream.available();
			buff = new byte[messageSize+1];
			buff[0] = byte1;

			if(messageSize > 0)
				this.mIStream.read(buff, 1, messageSize);
		}

		return buff;
	}
	 
	/**
	 * Increments the request reference count by one.  This keeps track of how many active requests are being processed.
	 */
	private void incrReqRefCount() {
		this.reqRefCount++;
	}

	/**
	 * Decrements the request reference count by one.  This keeps track of how many active requests are being processed.
	 *
	 * @throws Exception
	 */
	private void decrReqRefCount() throws Exception {
		if (this.reqRefCount == 0)
			throw new Exception("Request Reference Count can't be decrememted when it is already at 0!");
		this.reqRefCount--;

		if (this.reqRefCount == 0) {
			//this.logs._TransactionLog()._log("<B>Emptying Translist and WaitforEventsList</B>");
			this.mTransactions.emptyTransactionList();
			this.mWaitForList._emptyWaitForEventsList();
		}
	}

	/**
	 * Creates a request with the given function name.
	 *
	 * @param functionName The name of the registered hook to call in the application under test
	 * @return IZOutgoingRequest for the given function name.
	 */
	public OutgoingRequest _createRequest(String functionName) {
		return new OutgoingRequest(functionName, ++this.reqestUID);
	}

	/**
	 * Creates a request with the given function name.
	 *
	 * @param functionName The name of the registered hook to call in the application under test
	 * @return IZOutgoingRequest for the given function name.
	 * @throws Exception 
	 */
	public IncomingReply _createAndProcessRequest(String functionName) throws Exception {
		return this._processRequest(new OutgoingRequest(functionName, ++this.reqestUID));
	}

	/**
	 * Writes a request to the open socket connection.  If this request has an event associated with it then it won't return
	 * the reply until that event has been received.  If the event has a callback, the callback will be called passing in the
	 * data for each incoming event. The callback determines which events respond too.
	 *
	 * @param req The request to write to the open socket connection.
	 * @return The IZIncompingReply read in from the socket for this request
	 * @throws Exception
	 *
	 */
	public IncomingReply _processRequest(OutgoingRequest req) throws Exception {
		IncomingReply reply = null;
 		this.incrReqRefCount();
 		
		try {
			this.checkRequestThrottle();
			reply = this.makeRequest(req);
			if (!req.isRequestComplete() && req.checkNotificationOverride(reply) == false) //Make sure we need to wait for the notification
				this.waitForNotification(req);
 		}
 		finally {
			this.decrReqRefCount();
		}
		return reply;
	}
	
	/**
	 * Keeps reading form socket until the specified event and message arrives
	 *
     * @param type The event type to wait for
     * @param message the event message to wait for
	 * @throws Exception 
	 */
	public void _waitForEvent( EventSubType type, String message, NotificationCallback callback, int timeout ) throws Exception
	{
		OutgoingRequest req = this._createRequest(null);
		req._setEventNotification(type, message, callback);
		req._markAsWaitForNotificationOnly();
		req._setTimeoutVal(timeout);
		this.logs._TransactionLog()._log("<B>Processing WaitForEvent: " + this.mPort + ":" + req.getEventType() + ":" + req.getEventMessage() + ":" + req.getEventValue() + ": " + (req.isWaitRequest() ? "wait" : "timeout") + ":" + (req.isWaitRequest() ? req.getWaitDuration() : req.getTimeoutVal()) + "</B>");
		this._processRequest(req);
		this.logs._TransactionLog()._log("<B>Completed WaitForEvent: " + this.mPort + ":" + req.getEventType() + ":" + req.getEventMessage() + ":" + req.getEventValue() + ": " + (req.isWaitRequest() ? "wait" : "timeout") + ":" + (req.isWaitRequest() ? req.getWaitDuration() : req.getTimeoutVal()) + "</B>");
	}

	/**
	 * Checks if an Event is still active in the WaitForEvent queue
	 * 
	 * @param type
	 * @param message
	 */
	public boolean _isWaitForEventActive( String type, String message ) {
		return this.mWaitForList._isStillWaitingFor( type, 0, message );
	}

	/**
	 * Removes an Event from WaitForEvent queue
	 * 
	 * @param type
	 * @param message
	 */
	public void _removeWaitForEvent( String type, String message ) {
    	this.mWaitForList._setHandled( type, 0, message );
	}
	 
	/**
	 * Sends a request and returns the response.
	 *
	 * @param request the request to send to the RPC server
	 * @return the response from the server, which may be an exception
	 * @throws Exception
	 */
	private IncomingReply makeRequest(OutgoingRequest req) throws Exception
	{
		byte[] reqMsg = req.toByteArray(this.mSwapBytes);

		this.logs._TransactionLog()._log(this.transactionHeader() + "\n" + req.toString());

		if (req.getEventIsSet())
			this.mWaitForList._add(req.getEventType(), req.getEventValue(), req.checkEventValue(), req.getEventMessage(), req.checkEventMessage());

		// make sure stream is empty before writing to it
		this.readTransferFromSocket( req, 0, false );
		this.processNotifications( req );

		if (req.markedAsWaitForNotificationOnly()) {
			this.mNotificationIdleTime = System.currentTimeMillis();
			return null; //return null because there is no function associated with this req
		}

		// send the request
		this.write(reqMsg, 0, req.getChunkSize());
		
		if (req._noReply()) {
			this.mNotificationIdleTime = System.currentTimeMillis();
			return null;
		}
		
		return this.waitForReply(req);
	}

	/**
	 *
	 * @param b
	 * @param offset
	 * @param length
	 * @throws Exception
	 */
	private void write(byte[] b, int offset, int length) throws Exception
	{
		try
		{	// send the request
			this.mOStream.write(b, offset, length);
		}
		catch (SocketException se)
		{
			this.logs._TransactionLog()._logBold("Socket Error trying to write to stream: " + se.getMessage() + "  Retrying...");

			//try to reset the socket and try again
			this._connectToClient();
			this.mOStream.write(b, offset, length);
		}
	}

	/**
	 * Keeps reading data from the socket until the reply to the specified request is received or request's timeout is exceeded.
	 *
	 * @param req
	 * @return The IZIncompingReply read in from the socket for this request
	 * @throws Exception
	 */
	private IncomingReply waitForReply(OutgoingRequest req) throws Exception
	{
		IncomingReply reply = null;

		try
		{
			this.logs._TransactionLog()._log("Waiting for "  + req._getFunction() + "'s RPLY ; default timeout = " + req.getTimeoutVal());
			this.mNotificationIdleTime = System.currentTimeMillis();

			// Keep reading until we get a reply or we are idle for the specified timeout
			while (reply == null && !this.reqTimedOut(req) )
			{
				this.readTransferFromSocket(req, req.getTimeoutVal(), true);
				reply = (IncomingReply)this.mTransactions.getReplyTransaction(req.getTransferUID());
				if (reply != null && !req.isRequestComplete() && req.checkNotificationOverride(reply) == false)
					this.mWaitForList._update(req.getEventType(), req.getEventValue(), req.getEventMessage(), req.getEventType(), req._getEventValueFromReply(reply), this.filterEventMessageTags(reply, req));
				
				this.processNotifications(req);

				//If the reply didn't come in during the read then check for it again because it may have come in during processNotifications
				if (reply == null)
					reply = (IncomingReply)this.mTransactions.getReplyTransaction(req.getTransferUID());
				if (reply != null && !req.isRequestComplete() && req.checkNotificationOverride(reply) == false) //in case we didn't have a reply the first time we tried to update the WaitForList
					this.mWaitForList._update(req.getEventType(), req.getEventValue(), req.getEventMessage(), req.getEventType(), req._getEventValueFromReply(reply), this.filterEventMessageTags(reply, req));
			}

			if (reply == null && this.reqTimedOut(req))
			{
				this.logs._TransactionLog()._logBold("E1: " + this.mRPCServerID + " has not sent a reply to transfer ID:" + req.getTransferUID() + " (" + req._getFunction() + ") request within the specified " + (req.isWaitRequest() ? "wait" : "time") + " duration: " + (req.isWaitRequest() ? req.getWaitDuration() : req.getTimeoutVal()) + " ms");
				throw new Exception(this.mRPCServerID + " has not sent a reply to the " + req._getFunction() + " request within the specified " + (req.isWaitRequest() ? "wait" : "time") + " duration: " + (req.isWaitRequest() ? req.getWaitDuration() : req.getTimeoutVal()) + " ms");
			}
		}
		catch (Throwable t)
		{
			if (!t.getMessage().equals("Expected Nested Event Loop") && !t.getMessage().contains("Transfer"))
			{
				if (t.getMessage().contains("The call to readSocket timed out"))
				{
					this.logs._TransactionLog()._logBold("E2: " + this.mRPCServerID + " has not sent a reply to transfer ID:" + req.getTransferUID() + " (" + req._getFunction() + ") request within the specified " + (req.isWaitRequest() ? "wait" : "time") + " duration: " + (req.isWaitRequest() ? req.getWaitDuration() : req.getTimeoutVal()) + " ms");
					throw new Exception(this.mRPCServerID + " has not sent a reply to the " + req._getFunction() + " request within the specified " + (req.isWaitRequest() ? "wait" : "time") + " duration: " + (req.isWaitRequest() ? req.getWaitDuration() : req.getTimeoutVal()) + " ms");
				}
				else
					throw new Exception(t); //pass it on
			}
		}

		return reply;
	}

	/**
	 * @param req
	 * @throws Exception
	 */
	private void waitForNotification(OutgoingRequest req) throws Exception
	{
		this.logs._TransactionLog()._log("<B>Waiting for Notification: " + this.mPort + " TYPE:" + req.getEventType() + ", MSG:" + req.getEventMessage() + ", VAL:" + req.getEventValue() + ", " + (req.isWaitRequest() ? "wait" : "timeout") + ":" + (req.isWaitRequest() ? req.getWaitDuration() : req.getTimeoutVal()) + "</B>");
		if( req.getEventCallback() != null ) {
			if( req.isCallbackHandled() )
				this.logs._TransactionLog()._log("<B>...Notification callback already handled: " + this.mPort + ":" + req.getEventCallback()._ToString() + "</B>");
			else
				this.logs._TransactionLog()._log("<B>...Waiting for the Notification callback: " + this.mPort + ":" + req.getEventCallback()._ToString() + "</B>");
		}

		int origTransListVersion;
		req.activateWaitRequest(); //starts the wait clock if this is a wait request
		this.processNotifications(req);

		// Keep reading until our request is complete or we are idle for the specified timeout
		try
		{
			while (!req.isRequestComplete() && !this.reqTimedOut(req) )
			{
				this.readTransferFromSocket(req, req.getTimeoutVal(), !req.isWaitRequest());

				do
				{
					origTransListVersion = this.mTransactions.getListVersion();
					this.processNotifications(req);
				}
				while (!req.isRequestComplete() && !this.mTransactions.transactions.isEmpty() && this.mTransactions.getListVersion() != origTransListVersion);
			}
		}
		catch (Exception x)
		{
			if (!x.getMessage().contains("readSocket timed out"))
				throw x;
		}

		// Throw exception if request timed out
		if (!req.isRequestComplete())
		{
			String message;

			if (!req.isEventComplete())
				message =  this.mRPCServerID + " has not sent the expected notification: " + req.getEventType() + ":"  + req.getEventMessage() + ":" + req.getEventValue() + ", within the specified " + (req.isWaitRequest() ? "wait" : "time") + " duration: " + (req.isWaitRequest() ? req.getWaitDuration() : req.getTimeoutVal()) + " ms";
			else
				message = this.mRPCServerID + " has not completed the registered callback within the specified " + (req.isWaitRequest() ? "wait" : "time") + " duration: " + (req.isWaitRequest() ? req.getWaitDuration() : req.getTimeoutVal()) + " ms";

			this.logs._TransactionLog()._logBold("ERROR: " + message);
			throw new Exception(message);
		}

		this.logs._TransactionLog()._log("<B>Received Notification: " + req.getEventType() + ":" + req.getEventMessage() + ":" + req.getEventValue() + "</B>");
	}

	/**
	* Reads the bytes, if any, from the socket guaranteeing complete transactions.
	*
	* @param timeout maximum length of time, in milliseconds, to continue to read the incoming socket
	* @param throwXcptOnTimeOut true will cause readSocket to throw and Exception if no bytes are on
	*		  the stream, false will catch the socket timeout and contine without throwing an exception.
	*
	* @return the bytes from the socket
	* @throws Exception
	*/
	private byte[] readSocket(int timeout, boolean throwXcptOnTimeOut) throws Exception {
		int transferSizeTally = 0;
		int totalBytesRead = 0;
		int transferSize = 0;

		if( this.mSock == null ) {
			if( !this._connectToClient() )
				throw new Exception( "Could not connect to " + this.mRPCServerID + "'s socket on machine:" + this._GetMachineName() );
		}

		// zero timeout value means infinite blocking so use 1 ms if timeout is 0
		this.mSock.setSoTimeout( timeout>0?timeout:1 );//milliseconds

		// Just wait for the first byte
		byte[] buff = new byte[1];
		try {
			totalBytesRead = this.mIStream.read(buff, 0, buff.length);
			if (totalBytesRead == -1)
				buff = null;
		}
		catch (SocketTimeoutException e) {
			if( throwXcptOnTimeOut )
				throw new Exception("The call to readSocket timed out waiting for the iZomate notification from the application.");
			else
				buff = null;
		}

		//If we successfully read incoming data off the stream then get the rest of it
		if( buff != null ) {
			do {
				int readSize = this.mIStream.available();

				if( readSize > 0 ) {
					//This prevents infinite read loop and eventual buff overflow if events never stop coming in.
					//Just read enough from the stream to complete the last transfer in the buffer.
					if( totalBytesRead >= 65535 && (readSize > transferSizeTally - totalBytesRead) )
						readSize = transferSizeTally - totalBytesRead;

					//If we try to read more than 65535 bytes in a single read then the bytes after 65535 are NULL
					if( readSize > 65535 )
						readSize = 65535;

					totalBytesRead += readSize; // Keep track of entire stream capacity

					if( buff == null )
						buff = new byte[readSize];
					else {
					    // create new array to hold current buff bytes and new bytes
						byte[] temp = new byte[totalBytesRead];
						System.arraycopy(buff, 0, temp, 0, buff.length);
						buff = temp;
					}

					this.mIStream.read(buff, buff.length-readSize, readSize);

					// Make sure that there are at least 8 bytes before you attempt to read the 'size' of the transfer
					if(transferSizeTally == 0 && totalBytesRead >= 8 )
						transferSizeTally += BaseChunk.convertByte4ToLong( buff, 4, this.mSwapBytes );

					/*----------------------------------------------------------------------------------------
					 * if transferSizeTally == totalBytesRead then we have the exact number of bytes we need
					 * and inner while loop will exit.  If transferSizeTally > totalBytesRead then keep looping
					 * because we have more bytes to read.  If transferSizeTally < totalBytesRead then we have
					 * more than one transfer in the buffer
					 */
					// Add 8 to account for the first 8 bytes of the next transfer
					while( transferSizeTally + 8 < totalBytesRead ) {
						transferSize = BaseChunk.convertByte4ToInt( buff, transferSizeTally + 4, this.mSwapBytes );

						//Transfer sizes MUST be 8 or more bytes in size
						if( transferSize < 8 ) {
							String emsg = "Error reading stream data:  Detected transfer with size less than 8 at byte " + transferSizeTally;
							this.logs._TransactionLog()._logColor( emsg, "FF8040" );
							throw new Exception( emsg );
						}
						else
							transferSizeTally += transferSize;
					}
				}
			} while( transferSizeTally != totalBytesRead );
		}

		return buff;
	}

	/**
	 * Reads the next Transfer(s), if any, from the socket and parses them.
	 *
	 * @param req
	 * @param timeout maximum length of time, in milliseconds, to continue to read the incoming socket
	 * @param throwXcptOnTimeOut true will cause readSocket to throw an Exception if no bytes are on the
	 *			stream, false will catch the socket timeout and continue without throwing an exception.
	 * @throws Exception
	 */
	private void readTransferFromSocket(OutgoingRequest req, int timeout, boolean throwXcptOnTimeOut) throws Exception {
		byte[] buff = this.readSocket(timeout, throwXcptOnTimeOut);
		
		if (buff != null ) {
			this.parse( buff );
	
			if( this.processStatusNotifications() == CBStatus.NOTHANDLED ) {
				int statTimeout = timeout > TimeUtils._GetDefaultTimeout() ? timeout : TimeUtils._GetDefaultTimeout();
				do {
					try {
						this.processNotifications( req ); //process WNDW and DLOG notifications while we wait
						if( this.processStatusNotifications() != CBStatus.HANDLED ) { //just in case the STAT came in while we were processing notifications
							buff = this.readSocket( statTimeout, true );
							if( buff == null ) //prevent infinite loops in situations where readSocket() returns null
								throw new Exception( "readSocket timed out" );
							this.parse( buff );
						}
					}
					catch( Exception e ) {
						if( e.getMessage().contains("readSocket timed out") ) {
							this.logs._TransactionLog()._logColor( "readTransferFromSocket 2 Timed Out!", "FF8040" );
							throw new Exception( this.mRPCServerID + " has not sent iZomate a STAT status in '" + statTimeout + "' milliseconds, nor has it "
													+ "sent the expected 'done/complete' status to iZomate. It appears that the application is frozen." );
						}
						throw e;
					}
				}
				while( this.processStatusNotifications() != CBStatus.HANDLED );
			}
		}
	}

	/**
	 * Parses the bytes and passes them to their appropriate handler classes.
	 *
	 * @param b the byte[] array to parse
	 * @throws Exception
	 */
	private void parse( byte[] b ) throws Exception {
		int offset = 0;
		int length = (b == null ? 0 : b.length);
		BaseChunk transaction = null;

		if( length > 0 )
			this.logs._TransactionLog()._log( this.transactionHeader() );

		while( offset < length && length > 0 ) {
			if( b.length < (offset + 4) )
				throw new Exception("iZomate is attempting to parse an invalid transfer.");

			String transferType = new String(b, offset, 4); //bytes 0 - 3

			if (transferType.equals(NotificationType.RPLY.getID()))
				transaction = new IncomingReply(b, offset, this.mSwapBytes);
			else if (transferType.equals(NotificationType.RQST.getID())) //used by the Remote Launcher
				transaction = new IncomingRequest(b, offset, this.mSwapBytes);
			else if (transferType.equals(NotificationType.EVNT.getID()))
				transaction = new EventNotification(b, offset, this.mSwapBytes);
			else if (transferType.equals(NotificationType.WNDW.getID()))
				transaction = new WindowNotification(b, offset, this.mSwapBytes);
			else if (transferType.equals(NotificationType.DLOG.getID()))
				transaction = new DialogNotification(b, offset, this.mSwapBytes);
			else if (transferType.equals(NotificationType.XCPT.getID()))
				transaction = new ExceptionNotification(b, offset, this.mSwapBytes);
			else if (transferType.equals(NotificationType.STAT.getID()))
				transaction = new StatusNotification(b, offset, this.mSwapBytes);
			else
				throw new Exception("Parse detected unknown transfer type: '" + transferType + "'.");

			this.logs._TransactionLog()._log(transaction.toString());

			if (transaction.getChunkType() == NotificationType.STAT)
				this.mTransactions.updateTransaction(transaction);  //this will only keep one transaction of this kind in the list
			else
				this.mTransactions.addTransaction(transaction);

			offset += transaction.getChunkSize();
		}

		this.processExceptions();  // will throw Exception if an XCPT transfer is found.

		if( b != null )
			this.mNotificationIdleTime = System.currentTimeMillis(); //reset the NotifyHandler's base idle time
	}

	/**
	 *
	 * @param req
	 * @return
	 */
	private boolean reqTimedOut( OutgoingRequest req ) {
		if( req.isWaitActive() )
			return req.isWaitComplete();
		else
			return System.currentTimeMillis() >= this.mNotificationIdleTime + req.getTimeoutVal();
	}
	
	/**
	 * Call the window and dialog handler methods to process any window or dialogs contained in the
	 * NotifyHandler class.  This method does NOT read the socket for transactions.
	 *
	 * @param req
	 * @throws Exception
	 */
	private void processNotifications( OutgoingRequest req ) throws Exception {
		this.mTransactions.dumpTransactions();
		this.processWindowNotifications(req);
		this.processDialogNotifications(req);
		this.processEventNoticifations(req);
		this.processWaitForEventsList(req);
		this.mTransactions.dumpTransactions();
	}

    /**
     * Handles EVNT transactions by calling their respective handler methods.
     *
     * @param req
     * @throws Exception
     */
    private void processEventNoticifations( OutgoingRequest req ) throws Exception
    {
    	NotificationCallback cb = null;

        EventNotification eventNotif = (EventNotification)this.mTransactions.getFirstTransaction( NotificationType.EVNT );
        while( eventNotif != null ) {
        	if( !eventNotif.isCallbackInProgress() ) {
	    		CBStatus cbStat = CBStatus.NOTHANDLED;
	    		eventNotif.setCBStatus(CBStatus.INPROGRESS);
	    		
	    	    //Try the req's callback first
	    	    if( !req.isCallbackComplete() && req.getEventCallback() != null
	    	    	&& req.getEventCallback()._CompareNotificationType( eventNotif.getChunkType() )
	    	    	&& req.getEventCallback()._CompareID( eventNotif.getEventType() )
	    	    	&& req.getEventCallback()._CompareMessage( eventNotif.getEventMessage() ) ) 
	    	    {
	    	    	cbStat = req.getEventCallback()._Callback( null, eventNotif.getEventType(), eventNotif.getEventMessage(), null, eventNotif.getEventIntValue() );
	    	    	if( cbStat == CBStatus.HANDLED )
	    	    		this.logs._TransactionLog()._log("<B>Handled Callback Notification: " + req.getEventCallback()._ToString() + "</B>");
	    	    }
	    	    
	    	    //If the req's callback did not handle it then try the registered callback
	    	    if (cbStat == CBStatus.NOTHANDLED) {
			    	//First check to see if there is a specific callback registered for this window message
	    	    	cb = this.mNtfcnCBRegistry._getCallback(NotificationType.EVNT, eventNotif.getEventType(), eventNotif.getEventMessage());

		    		if (cb != null)
		    			cbStat = cb._Callback( eventNotif.getEventType(), null, eventNotif.getEventMessage(), null, eventNotif.getEventIntValue() );

		    		//If no callback found then check for a default
		    		if (cb == null || cbStat == CBStatus.NOTHANDLED) {
		    			cb = this.mNtfcnCBRegistry._getCallback(NotificationType.EVNT, null, null);

		    			if (cb != null)
		    				cbStat = cb._Callback( eventNotif.getEventType(), null, eventNotif.getEventMessage(), null, eventNotif.getEventIntValue() );
		    		}
	    	    }
	    	    else
	    	    	req.setCBStatus(cbStat);

	    	    // Attempt to mark it as handled if it is in WaitForList
		    	this.mWaitForList._setHandled( eventNotif.getEventType(), eventNotif.getEventIntValue(), eventNotif.getEventMessage() );

	    	    eventNotif.setCBStatus(cbStat);
        	}

        	eventNotif = (EventNotification)this.mTransactions.getNextTransaction( NotificationType.EVNT, eventNotif );
    	}
    }

    /**
     * Handles WNDW transactions by calling their respective handler methods.
     *
     * @param req
     * @throws Exception
     */
    private void processWindowNotifications( OutgoingRequest req ) throws Exception {
    	NotificationCallback cb = null;

	    WindowNotification windowNotif = (WindowNotification) this.mTransactions.getFirstTransaction( NotificationType.WNDW );
    	while (windowNotif != null) {
    		if (!windowNotif.isCallbackInProgress()) {
	    	    CBStatus cbStat = CBStatus.NOTHANDLED;
	    	    windowNotif.setCBStatus(CBStatus.INPROGRESS); //prevents this datum from being processed from another callback's request until we are done with it

	    	    //Try req's callback first
	    	    if( !req.isCallbackComplete() && req.getEventCallback() != null
	    	    	&& req.getEventCallback()._CompareNotificationType( windowNotif.getChunkType() )
		    	    && req.getEventCallback()._CompareID( windowNotif.getWindowID() )
		    	    && req.getEventCallback()._CompareTitle( windowNotif.getWindowTitle() )
		    	    && req.getEventCallback()._CompareMessage( windowNotif.getWindowStatus() ) ) 
	    	    {
	    	    	cbStat = req.getEventCallback()._Callback( windowNotif.getWindowTitle(), windowNotif.getWindowID(), windowNotif.getWindowStatus(), null, 0 );
	    	    	if( cbStat == CBStatus.HANDLED )
	    	    		this.logs._TransactionLog()._log("<B>Handled Callback Notification: " + req.getEventCallback()._ToString() + "</B>");
	    	    }

	    	    //If the req's callback did not handle it then try the registered callback
	    	    if (cbStat == CBStatus.NOTHANDLED) {
	    	    	//First check to see if there is a specific callback registered for this window message
	        		cb = this.mNtfcnCBRegistry._getCallback(NotificationType.WNDW, windowNotif.getWindowTitle(), windowNotif.getWindowStatus());

	        		if (cb != null)
	           			cbStat = cb._Callback( windowNotif.getWindowTitle(), windowNotif.getWindowID(), windowNotif.getWindowStatus(), null, 0 );

	        		//If no callback found then check for a default
	        		if (cb == null || cbStat == CBStatus.NOTHANDLED) {
	            		cb = this.mNtfcnCBRegistry._getCallback( NotificationType.WNDW, null, null );

	            		if (cb != null)
	            			cbStat = cb._Callback( windowNotif.getWindowTitle(), windowNotif.getWindowID(), windowNotif.getWindowStatus(), null, 0 );
	        		}
	    	    }
	    	    else
	    	    	req.setCBStatus( cbStat );

		    	if(cbStat == CBStatus.HANDLED)
		    		this.mWaitForList._setHandled( windowNotif.getWindowTitle(), 0, windowNotif.getWindowID() );

		    	windowNotif.setCBStatus(cbStat);
    		}
    		windowNotif = (WindowNotification) this.mTransactions.getNextTransaction( NotificationType.WNDW, windowNotif );
    	}
    }

    /**
     * Handles DLOG transactions by calling their respective handler methods.
     *
     * @param req
     * @throws Exception
     */
    private void processDialogNotifications( OutgoingRequest req ) throws Exception {
    	NotificationCallback cb = null;

    	DialogNotification dialogNotif = (DialogNotification) this.mTransactions.getFirstTransaction( NotificationType.DLOG );
    	if (dialogNotif != null) {
    		if(!dialogNotif.isCallbackInProgress()) {
	    		CBStatus cbStat = CBStatus.NOTHANDLED;
		    	dialogNotif.setCBStatus(CBStatus.INPROGRESS);
		    	
	    	    //Try the req's callback first
	    	    if( !req.isRequestComplete() && req.getEventCallback() != null
	    	    	&& req.getEventCallback()._CompareNotificationType( dialogNotif.getChunkType() )
	    	    	&& req.getEventCallback()._CompareTitle( dialogNotif.getDialogTitle() )
	    	    	&& req.getEventCallback()._CompareMessage( dialogNotif.getDialogMessage() ) ) 
	    	    {
	    	    	cbStat = req.getEventCallback()._Callback(dialogNotif.getDialogTitle(), null, dialogNotif.getDialogMessage(), dialogNotif.getDialogButtons(), 0);
	    	    	if( cbStat == CBStatus.HANDLED )
	    	    		this.logs._TransactionLog()._log("<B>Handled Callback Notification: " + req.getEventCallback()._ToString() + "</B>");
	    	    }

	    	    //If the req's callback did not handle it then try the registered callback
		    	if( cbStat == CBStatus.NOTHANDLED) {
	    	    	//First check to see if there is a specific callback registered for this window message
		    		cb = this.mNtfcnCBRegistry._getCallback(NotificationType.DLOG, dialogNotif.getDialogTitle(), dialogNotif.getDialogMessage());

		    		if(cb != null)
		    			cbStat = cb._Callback(dialogNotif.getDialogTitle(), null, dialogNotif.getDialogMessage(), dialogNotif.getDialogButtons(), 0);

		    		//If no callback found or the previous callback did not handle it then check for a default
		    		if(cb == null || cbStat == CBStatus.NOTHANDLED) {
		    			cb = this.mNtfcnCBRegistry._getCallback(NotificationType.DLOG, null, null);

		    			if(cb != null)
		    				cbStat = cb._Callback(dialogNotif.getDialogTitle(), null, dialogNotif.getDialogMessage(), dialogNotif.getDialogButtons(), 0);
		    		}
		    	}
		    	else
		    		req.setCBStatus(cbStat);

	    	    if( cbStat == CBStatus.HANDLED )
	    	    	this.mWaitForList._setHandled( dialogNotif.getDialogTitle(), 0, dialogNotif.getDialogMessage() );

	       	    dialogNotif.setCBStatus(cbStat);
    		}
	       	dialogNotif = (DialogNotification) this.mTransactions.getNextTransaction( NotificationType.DLOG, dialogNotif );
    	}
    }

    /**
	 * Handles STAT transactions by calling their respective handler methods.
	 *
	 * @param req
	 * @throws Exception
	 */
	private CBStatus processStatusNotifications() throws Exception
	{
		CBStatus cbStat = CBStatus.HANDLED; //assume HANDLED unless a callback indicates otherwise
	    StatusNotification statNotif = (StatusNotification) this.mTransactions.getFirstTransaction(NotificationType.STAT);

	    if (statNotif != null) {
    	    statNotif.setCBStatus(CBStatus.INPROGRESS); //prevents this notification from being processed from another callback's request until we are done with it

    	    //First check to see if there is a specific callback registered for this window message
            NotificationCallback cb = this.mNtfcnCBRegistry._getCallback(NotificationType.STAT, null, statNotif.getMessage());

        	//Check for default callback
            if (cb == null)
            	cb = this.mNtfcnCBRegistry._getCallback(NotificationType.STAT, null, null);

        	if (cb != null)
           		cbStat = cb._Callback(null, null, statNotif.getMessage(), null, statNotif.getPercentage());

        	//We are done with this stat so mark it as handled
    	    statNotif.setCBStatus(CBStatus.HANDLED);
        }

    	return cbStat;
	}

	/**
	 * Checks NotifyHandler list for any exceptions. This method does NOT read the socket for transactions,
	 * that is handled by <code>checkForExceptions()</code>.
	 *
	 * @throws Exception thrown if an ExceptionNotification is found
	 */
	private void processExceptions() throws Exception
	{
		// Warning...Do NOT call readTransferFromSocket() here. I will loop infinitely. The parse portion automatically calls processException().
		ExceptionNotification x = (ExceptionNotification)this.mTransactions.getFirstTransaction(NotificationType.XCPT);
		if (x != null) {
			x.setCBStatus(CBStatus.HANDLED);
			throw new Exception(x.getString());
		}
	}
	
	 /**
     * Checks the Wait For Events List to see if the
     *
     * @param req
     * @throws Exception
     */
    private void processWaitForEventsList(OutgoingRequest req)
    {
    	if (this.mWaitForList._isHandled(req.getEventType(), req.getEventValue(), req.getEventMessage()))
    		req.setEventReceived();
    }

    /**
     * Looks for any event tags that were detected in the message and
     * substitutes the tag with datum value in the reply with the same name.
     *
     *  message: down(<x>, <y>)  ==>  down(100, 200)
     *
     * @throws Exception
     */
	private String filterEventMessageTags (IncomingReply reply, OutgoingRequest req) throws Exception
	{
		if (req.getEventMessageDatumTags() != null)
    	{
			String message = req.getEventMessage();
			for (String tag : req.getEventMessageDatumTags())
			{
				if (!reply._exists(tag))
					throw new Exception("The required datum '" + tag + "' was not found in reply");

				String val = reply._getDatum(tag).valueToString();
		    	message = message.replace(req._getEventMessageSubstBeginParam() + tag + req._getEventMessageSubstEndParam() , val);
			}

			//update click event with the specific coordinates
	    	req.updateEventMessage(message);
    	}

		return req.getEventMessage();
	}

    /**
     * Registers a callback to be called every time a notification is process of a specified NotificationType and EventType.
     *
     * @param type The notificationType associated with this callback
     * @param strTitleOrSubType The EVNT sub type or WND/DLOG title.
     * @param strMessage The notification message.
     * @param callback The callback to be called
     * @return true if successfully registered.
     * @throws Exception
     */
    public boolean _RegisterNotificationCallback( NotificationType type, String strTitleOrSubType, String strMessage, NotificationCallback callback ) throws Exception {
    	return this.mNtfcnCBRegistry._registerCallback( type, strTitleOrSubType, strMessage, callback );
    }

    /**
     * Registers a callback to be called every time a notification is process of a specified NotificationType and EventType.
     *
     * @param callback The callback to be register.  Pulls registration info out of callback itself
     * @return true if successfully registered.
     * @throws Exception
     */
    public boolean _RegisterNotificationCallback( NotificationCallback callback ) throws Exception {
    	return this.mNtfcnCBRegistry._registerCallback( callback );
    }

    /**
     * Unregisters a callback in the registry with a specific notification type, notification title or subtype and notification message.
     *
     * @param type the datum type associated with the callback.
     * @param strTitleOrSubType The EVNT sub type or WND/DLOG title.
     * @param strMessage The notification message.
     * @return true if unregistered successfully
     * @throws Exception
     */
    public boolean _UnregisterNotificationCallback( NotificationType type, String strTitleOrSubType, String strMessage ) throws Exception {
    	return this.mNtfcnCBRegistry._unregisterCallback( type, strTitleOrSubType, strMessage );
    }

    /**
     * Unregisters a callback in the registry with a specific notification type, notification title or subtype and notification message.
     *
     * @param callback The callback to be unregister.  Pulls registration info out of callback itself
     * @return true if unregistered successfully
     * @throws Exception
     */
    public boolean _UnregisterNotificationCallback( NotificationCallback callback  ) throws Exception {
    	return this.mNtfcnCBRegistry._unregisterCallback( callback );
    }
    
    /**
    *
    * @throws Exception
    */
   private String transactionHeader() throws Exception
   {
       Calendar theCal = Calendar.getInstance();
       String theTime = String.format("<I>%02d:%02d:%02d:%03d</I>", theCal.get(Calendar.HOUR_OF_DAY), theCal.get(Calendar.MINUTE), theCal.get(Calendar.SECOND), theCal.get(Calendar.MILLISECOND));
       return (theTime + " Port: " + this.mPort);
   }
  
   /**
    * 
    * @throws InterruptedException
    */
   private void checkRequestThrottle() throws InterruptedException {
		if( this.mProcessRequestThrottleMS > 0 ) {
			if( System.currentTimeMillis() - this.mProcessRequestTimestamp < this.mProcessRequestThrottleMS )
 	 			Thread.sleep(this.mProcessRequestThrottleMS -(int)(System.currentTimeMillis() - this.mProcessRequestTimestamp));
 			this.mProcessRequestTimestamp= System.currentTimeMillis();
 		}
   }

}

