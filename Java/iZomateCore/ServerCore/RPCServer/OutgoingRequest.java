package iZomateCore.ServerCore.RPCServer;

import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.NotificationCallback;
import iZomateCore.ServerCore.RPCServer.Chunks.BaseChunk;
import iZomateCore.ServerCore.RPCServer.Chunks.CompoundChunk;
import iZomateCore.UtilityCore.TimeUtils;
import org.omg.CORBA.BooleanHolder;

import java.util.HashMap;

/**
 * An outgoing request.
 */
public class OutgoingRequest extends CompoundChunk
{
    private long 								reqestUID = -1;
    protected byte[]  							messageArray;
    private int									timeoutVal = TimeUtils._GetTimeoutMS( 15 * 1000 ); //convert to ms
    private boolean								waitRequest = false;
    private long								waitDurationMS = 0;
    private long								waitStopTime = 0;
    private String 								eventType = null; //make into enum
    private int 								eventValue;
    private boolean								eventValueCheck;
    private String 								eventMessage = null;
    private boolean								eventMessageCheck;
    private NotificationCallback 				eventCallback = null;
    private boolean								eventIsSet = false;
    private boolean								eventReceived = false;
    private String[]							eventMessageDatumTags = null;
    private String 								eventMessageSubstBeginParam = null;
    private String								eventMessageSubstEndParam = null;
    private String								eventValueDatumName = null;
    private HashMap<String, BooleanHolder>		eventOverrideList = new HashMap<String, BooleanHolder>();
    private boolean								waitForNotificationOnly = false;
    private boolean								noReply= false;

    /**
     * Constructs an iZomate method call request. Call "add" methods to add input parameters to the request.
     *
     * @param method the name of the method to be called.
     * @param uid the requests unique identifier
     */
    protected OutgoingRequest(String method, long uid)
    {
    	super("IZOutgoingRequest");
    	this.reqestUID = uid;
        this.setChunkType(NotificationType.RQST);

        if (method != null)
        	this.addFunction(method);
    }

    /**
     * This sets the maximum time to wait between incoming notifications without throwing an error.
     *
     * @param timeout timeout value in seconds
     */
    public void _setTimeoutVal (int timeout)
    {
    	if( timeout != -1 )
    		this.timeoutVal= TimeUtils._GetTimeoutMS( timeout * 1000 );
    }

    /**
     * Returns the timeout value set for this request.
     *
     * @return timeout value in milliseconds.
     */
    protected int getTimeoutVal ()
    {
    	if (this.isWaitRequest() && this.waitStopTime != -1)
    	{
    		int newTimeout = (int)(this.waitStopTime - System.currentTimeMillis());
    		return (newTimeout < 0) ? 0 : newTimeout;
    	}
    	else
    		return this.timeoutVal;
    }

    /**
     * Sets a wait duration for this request.  This causes the server to wait for the specified time
     * when processing this request.  The server continues to check for and process incoming notifications while waiting.
     * If an event notification or callback is set but not handled within the wait duration an error is thrown.
     *
     * @param waitDurationMS the number of milliseconds to wait before returning from request
     */
    public void _setWaitDuration (int waitDurationMS)
    {
    	this.waitRequest = true;
    	this.waitDurationMS = waitDurationMS;
    	this.waitStopTime = -1;
    }

    /**
     * Returns the wait duration for the request
     * @return wait duration
     */
    protected long getWaitDuration()
    {
    	return this.waitDurationMS;
    }

    /**
     * Returns whether the request has the wait flag set.
     * @return true if wait flag is set.
     */
    protected boolean isWaitRequest()
    {
    	return this.waitRequest;
    }

    /**
     * Initializes the request's wait stop time to the current time + the wait duration which activates the wait process.
     */
    protected void activateWaitRequest()
    {
    	if (this.isWaitRequest())
    		this.waitStopTime = System.currentTimeMillis() + this.waitDurationMS;
    }

    /**
     * Returns whether the request has the wait flag set and initWaitTime() has been called
     * @return true if wait flag is set and initWaitTime() has been called.
     */
    protected boolean isWaitActive()
    {
    	return (this.isWaitRequest() && this.waitStopTime != -1);
    }

    /**
     * Returns whether the wait stop time has expired.
     * @return true if the current time is >= wait stop time.
     */
    protected boolean isWaitComplete()
    {
    	if (this.isWaitActive())
    		return System.currentTimeMillis() >= this.waitStopTime;
       	else
    		return false; //We have not started it yet
    }

    /**
     * Tells ProcessRequest() to not wait for a reply.
     * If an event notification or a callback has been set on this request then it will keep processing notifications until the event has been received and/or the callback handled.
     */
    protected void _setNoReply()
    {
    	this.noReply = true;
    }

    /**
     * Returns whether this request does not have an associated reply
     * @return
     */
    protected boolean _noReply()
    {
    	return this.noReply;
    }

    /**
     * Turns off the Request/Reply part of processing this event.  It will just read the socket and process notifications.
     * If an event notification or a callback has been set on this request then it will keep processing notifications until the event has been received and/or the callback handled.
     */
    protected void _markAsWaitForNotificationOnly()
    {
    	this.waitForNotificationOnly = true;
    }

    /**
     * Returns whether this request has been marked as "Wait for Event Notification Only".
     *
     * @return true if waitForNotificationOnly has been set
     */
    protected boolean markedAsWaitForNotificationOnly()
    {
    	return this.waitForNotificationOnly;
    }

    /**
     * Sets up the callback info for this request.  Process request won't return until the callback is handled.
     *
     * @param callback the callback to execute when the matching event is received.
     */
    public void _setCallback(NotificationCallback callback)
    {
    	if (callback != null)
    		this._setEventNotification(EventSubType.NULL, callback);
    }

    /**
     * Sets up the callback info for this request.  Process request won't return until the specified dialog is handled.
     *
     * @param title Wait for a dialog with this title to be handled
     * @param message Wait for a dialog with this message to be handled
     */
    public void _setDialogNotification(String title, String message)
    {
    	this._setEventNotification(title, message);
    }

    /**
     * Sets up the callback info for this request.  Process request won't return until the specified dialog
     * notification is received and the callback is handled. (Note that this method will only check the event type,
     * ignoring the event value and message).
     *
     * @param title Wait for a dialog with this title to be handled
     * @param message Wait for a dialog with this message to be handled
     * @param callback the callback to execute when the matching event is received.  Null to use default callback (if available).
     */
    public void _setDialogNotification(String title, String message, NotificationCallback callback)
    {
    	this._setEventNotification(title, message, callback);
    }

    /**
     * Sets up the callback info for this request.  Process request won't return until this event notification type is received.
     *
     * @param type The event type to wait for
     */
    public void _setEventNotification(EventSubType type)
    {
    	this.setEventNotification(type.getValue(), -1, false, null, null, null);
    }

    /**
     * Sets up the callback info for this request.  Process request won't return until this event notification type is received.
     *
     * @param type The event type to wait for
     */
    public void _setEventNotification(String type)
    {
    	this.setEventNotification(type, -1, false, null, null, null);
    }

    /**
     * Sets up the callback info for this request.  Process request won't return until this callback is executed
     * and the event notification type is received from the application. (Note that this method will only check the event type,
     * ignoring the event value and message).
     *
     * @param type The event type to wait for
     * @param callback the callback to execute when the matching event is received.  Null to use default callback (if available).
     */
    public void _setEventNotification(EventSubType type, NotificationCallback callback)
    {
    	this.setEventNotification(type.getValue(), -1, false, null, null, callback);
    }

    /**
     * Sets up the callback info for this request.  Process request won't return until this callback is executed
     * and the event notification type is received from the application. (Note that this method will only check the event type,
     * ignoring the event value and message).
     *
     * @param type The event type to wait for
     * @param callback the callback to execute when the matching event is received.  Null to use default callback (if available).
     */
    public void _setEventNotification(String type, NotificationCallback callback)
    {
    	this.setEventNotification(type, -1, false, null, null, callback);
    }

    /**
     * Sets up the callback info for this request.  Process request won't return until the event is received from the application.
     * Note that this method will only check the event type and message, ignoring the event value).
     *
     * @param type The event type to wait for
     * @param message the event message to wait for
     */
    public void _setEventNotification(EventSubType type, String message)
    {
    	this.setEventNotification(type.getValue(), -1, false, null, message, null);
    }

    /**
     * Sets up the callback info for this request.  Process request won't return until the event is received from the application.
     * Note that this method will only check the event type and message, ignoring the event value).
     *
     * @param type The event type to wait for
     * @param message the event message to wait for
     */
    public void _setEventNotification(String type, String message)
    {
    	this.setEventNotification(type, -1, false, null, message, null);
    }

    /**
     * Sets up the callback info for this request.  Process request won't return until this callback is executed
     * and the event is received from the application. (Note that this method will only check the event type and message,
     * ignoring the event value).
     *
     * @param type The event type to wait for
     * @param message the event message to wait for
     * @param callback the callback to execute when the matching event is received.  Null to use default callback (if available).
     */
    public void _setEventNotification(EventSubType type, String message, NotificationCallback callback)
    {
    	this.setEventNotification(type.getValue(), -1, false, null, message, callback);
    }

    /**
     * Sets up the callback info for this request.  Process request won't return until this callback is executed
     * and the event is received from the application. (Note that this method will only check the event type and message,
     * ignoring the event value).
     *
     * @param type The event type to wait for
     * @param message the event message to wait for
     * @param callback the callback to execute when the matching event is received.  Null to use default callback (if available).
     */
    public void _setEventNotification(String type, String message, NotificationCallback callback)
    {
    	this.setEventNotification(type, -1, false, null, message, callback);
    }

    /**
     * Sets up the callback info for this request.  Process request won't return until this
     * event notification is received from the application.
     *
     * @param type The event type to wait for
     * @param value The event value to wait for
     * @param message the event message to wait for
     */
    public void _setEventNotification(String type, int value, String message)
    {
    	this.setEventNotification(type, value, true, null, message, null);
    }

    /**
     * Sets up the callback info for this request.  Process request won't return until this
     * event notification is received from the application.
     *
     * @param type The event type to wait for
     * @param value The name of a datum in the reply to dynamically set the event integer value from
     * @param message the event message to wait for
     * @param callback the callback to execute when the matching event is received.  Null to use default callback (if available).
     */
    public void _setEventNotification(String type, String value, String message, NotificationCallback callback)
    {
    	this.setEventNotification(type, -1, true, value, message, callback);
    }

    /**
     * Sets up the callback info for this request.  Process request won't return until this callback is executed
     * and the event notification is received from the application.
     *
     * @param type The event type to wait for
     * @param value The event value to wait for
     * @param callback the callback to execute when the matching event is received.  Null to use default callback (if available).
     */
    public void _setEventNotification(String type, int value, NotificationCallback callback)
    {
    	this.setEventNotification(type, value, true, null, null, callback);
    }

    /**
     * Sets up the callback info for this request.  Process request won't return until this callback is executed
     * and the event notification is received from the application.
     *
     * @param type The event type to wait for
     * @param value The event value to wait for
     * @param message the event message to wait for
     * @param callback the callback to execute when the matching event is received.  Null to use default callback (if available).
     */
    public void _setEventNotification(String type, int value, String message, NotificationCallback callback)
    {
    	this.setEventNotification(type, value, true, null, message, callback);
    }

    /**
     * Sets up the callback info for this request.  Process request won't return until this callback is executed
     * and the event notification is received from the application.
     *
     * @param type The event type associated with this callback
     * @param value The event value associated with this callback
     * @param valueCheck False to ignore the event value in callback Notification check
     * @param valueDatumName The name of a datum in the reply to dynamically set the event integer value from (or null to ignore)
     * @param message the event message associated with his callback
     * @param messageCheck False to ignore the message in callback Notification check
     * @param callback the callback to execute when the matching event is received.  Null to use default callback (if available).
     */
    private void setEventNotification(String type, int value, boolean valueCheck, String valueDatumName, String message, NotificationCallback callback)
    {
    	this.eventType = type;  				//EVNT type, 	DLOG title   or WNDW winTitle
    	this.eventValue = value;				//EVNT value
    	this.eventValueCheck = valueCheck;		//Should we check the EVNT value
    	this.eventValueDatumName = valueDatumName; //Reply Datum to get the EVNT value from
    	this.eventMessage = message;			//EVNT message,	DLOG message or WNDW winID
    	this.eventMessageCheck = (message != null);	//Should we check the message
    	this.eventCallback = callback;

    	if (!this.eventIsSet && this.eventType != null) //only do this once
    	{
    		this.eventIsSet = true;
    		this.eventReceived = false;
    	}
    }

    /**
     * Specifies the begin and end parameters to be used in enclosing datum names for message substitution.
     * example:
     * If the reply contains the datums named x and y which contain the x and y mouse event coordinates needed to
     * match the Event Notification message: "down(100, 200)", then specify "<" and ">" as the substitution params.
     * Your event message would look like this:
     *     message: down(<x>, <y>)  ==> (after substitution)  down(100, 200)
     *
     * <b>NOTE: Special characters such as [ cause string manipulation errors.  Avoid using special characters.</b>
     * @param begin the begin datum name substitution value i.e. "<"
     * @param end  the end datum name substitution value i.e. ">"
     * @throws Exception
     */
    //TODO: Figure out how to handle special characters
    public void _setEventNotificationMessageSubstParams(String begin, String end) throws Exception
    {
    	if (begin == null || end == null || begin.isEmpty() || end.isEmpty())
    		throw new Exception("Both begin and end params must be specified.");

    	if (this.eventMessage == null || this.eventMessage.isEmpty())
    		throw new Exception("An event message must be set prior to calling _setEventNotificationMessageSubstParams.");

    	this.eventMessageSubstBeginParam = begin;
    	this.eventMessageSubstEndParam = end;

    	String[] tmp = this.eventMessage.split(begin);
		this.eventMessageDatumTags = new String[tmp.length -1];
		for ( int i = 1; i < tmp.length; ++i)
			this.eventMessageDatumTags[i-1] = tmp[i].split(end)[0];
    }

    /**
     * Gets the eventMessageSubstBeginParam value
     * @return The eventMessageSubstBeginParam value
     */
    protected String _getEventMessageSubstBeginParam()
    {
    	return this.eventMessageSubstBeginParam;
    }

    /**
     * Gets the eventMessageSubstEndParam value
     * @return The eventMessageSubstEndParam value
     */
    protected String _getEventMessageSubstEndParam()
    {
    	return this.eventMessageSubstEndParam;
    }

    /**
     * Gets the event value from a datum value in the reply based on the eventValueDatumName.
     * If eventValueDatumName is not set, just calls getEventValue() instead.
     * @return The eventValueDatumName value
     * @throws Exception
     */
    protected int _getEventValueFromReply(IncomingReply reply) throws Exception
    {
    	if (this.eventValueDatumName != null && !this.eventValueDatumName.equals(""))
    	{
    		NotificationType datumType = reply._getDatum(this.eventValueDatumName).getChunkType();

    		if (datumType == NotificationType.In32)
    			this.eventValue = reply._getInt32(this.eventValueDatumName);
    		else
    			throw new Exception("Unsupported event value datum type: " + datumType.getID());
    	}

    	return this.getEventValue();
    }

    /**
     * Updates the Event Notifiaction's event message
     * @param newMessage The new event message.
     */
    protected void updateEventMessage(String newMessage)
    {
    	this.eventMessage = newMessage;
    }

	 /**
     * Should the RPCServer verify the event value or ignore it?
     * @return false to ignore, true to verify
     */
    protected boolean checkEventValue()
    {
    	return this.eventValueCheck;
    }

    /**
     * Should the RPCServer verify the event message or ignore it?
     * @return false to ignore, true to verify
     */
    protected boolean checkEventMessage()
    {
    	return this.eventMessageCheck;
    }

    /**
     * Sets up the callback info for this request.  Process request won't return until the specified window is handled.
     *
     * @param title Wait for the window with this title to be handled
     * @param paneID Wait for the window with this pane ID to be handled
     * @param callback the callback to execute when the matching window event is received.  Null to use default callback (if available).
     */
    public void _setWindowNotification(String title, String paneID, NotificationCallback callback)
    {
    	this._setEventNotification(title, paneID, callback);
    }

    /**
     * Checks the reply for a datum with the given name and compares its value with the value supplied.
     * if the datums value equals the value supplied then the _processRequest will not wait for
     * the notificationCallback to be executed.  You may call this method more than once to
     * set multiple overrides.
     *
     * @param datumName name of the datum to look for
     * @param value the value of the datum to check against
     * @throws Exception
     */
    public void _setNotificationOverride(String datumName, boolean value) throws Exception
    {
    	if (this.eventOverrideList.containsKey(datumName))
    		throw new Exception("This event notification override has already been set: " + datumName);

    	this.eventOverrideList.put(datumName, new BooleanHolder(value));
    }

    /**
     * Returns if an override value was detected in the reply from this request,
     * indicating that the server should skip waiting for the set event notification
     *
     * @param reply the reply returned by processing this request
     * @return true to override
     * @throws Exception
     */
    protected boolean checkNotificationOverride(IncomingReply reply) throws Exception
    {
    	for (String datumName: this.eventOverrideList.keySet())
    	{
    		if (reply._exists(datumName))
    		{
    			if (reply._getBoolean(datumName) == this.eventOverrideList.get(datumName).value)
    				return true;
    		}
    	}

    	return false;
    }

    /**
     * Gets the event type as set from setEventNotification().
     * @return string - the event type.
     */
    protected String getEventType()
    {
    	return this.eventType;
    }

    /**
     * Gets the event value as set from setEventNotification().
     * @return int - the event value.
     */
    protected int getEventValue()
    {
    	return this.eventValue;
    }

    /**
     * Gets the event message as set from setEventNotification().
     * @return string - the event message.
     */
    protected String getEventMessage()
    {
    	return this.eventMessage;
    }

    /**
     * Gets the event message datum tags found in the event's message as set from setEventNotification().
     * Tags are in the form of <myTag> and are part of the event message.  i.e.  moved(<x>, <y>)
     * where <x> and <y> are tags.
     * @return string[] - the event message tags.
     */
    protected String[] getEventMessageDatumTags()
    {
    	return this.eventMessageDatumTags;
    }

    /**
     * Gets the event callback as set from setEventNotification().
     * @return NotificationCallback - the event callback.
     */
    protected NotificationCallback getEventCallback()
    {
    	return this.eventCallback;
    }

    /**
     * Returns true if setEventNotification() has been called.
     * @return true if setEventNotification(). has been called.
     */
    protected boolean getEventIsSet()
    {
    	return this.eventIsSet;
    }

    /**
     * Returns true if:  <br>the event notification is set and has been received (ignored if not set) <br>
     * 					 and the callback has been set and handled (ignored if not set) <br>
     * 					 and the wait duration has been exceeded. (ignored if not set)
     *
     * @return true if the request is complete
     * @throws Exception
     */
    protected boolean isRequestComplete() throws Exception
    {
    	boolean complete = true; //assume true until one of these conditions proves false

    	if (complete && this.eventCallback != null)
    		complete = this.isCallbackHandled();

    	if (complete && this.eventIsSet)
    		complete = this.eventReceived;

    	if (complete && this.isWaitRequest())
    		complete = this.isWaitComplete();

    	return complete;
    }

    /**
     * Returns true if the event notification is set and has been received (or if no event was set).
     *
     * @return true if the event has been received (or was not set)
     * @throws Exception
     */
    protected boolean isEventComplete() throws Exception
    {
    	if (this.eventIsSet && !this.eventReceived)
    		return false;
    	else
    		return true;
    }

    /**
     * Returns true if the callback has been set and handled (or if no callback was set).
     *
     * @return true if the callback has been handled (or was not set)
     * @throws Exception
     */
    protected boolean isCallbackComplete() throws Exception
    {
    	if (this.eventCallback != null && !this.isCallbackHandled())
    		return false;
    	else
    		return true;
    }

    /**
	 * Sets the callbackStatus of this datum.  Used for Notification datums.
	 *
	 * @param stat
	 */
	@Override
	public void setCBStatus(CBStatus stat)
	{
		if (this.getEventCallback() != null)
			super.setCBStatus(stat);
	}

    /**
     * Marks the request's event notification as having been received.
     */
    protected void setEventReceived()
    {
    	this.eventReceived = true;
    }

    /**
     * For retrieving the transfer ID of a RQST to it can be placed in its RPLY
     * @return int
     */
    protected long getTransferUID()
    {
        return this.reqestUID;
    }

    /**
     * Creates a new compound datum
     *
     * @param typeName the type of the data element
     * @return new compound datum
     */
    public CompoundChunk _createCompoundDatum(String typeName)
    {
    	CompoundChunk datum = new CompoundChunk(typeName);

    	return datum;
    }

    /**
     * Creates the Transfer data
     *
     * @param swapBytes target system platform used in figuring out data endianness
     * @return byte []
     */
    @Override
	public byte[] toByteArray(boolean swapBytes)
    {
        BaseChunk d;

        this.mChunkSize = 12;
        this.checkMessageArraySize(this.mChunkSize);
        System.arraycopy(this.getChunkType().toString().getBytes(), 0, this.messageArray, 0, 4);

        for ( int i = 0; i < this._getCount(); i++ )
        {
            d = this._getNth(i);
            byte [] data = d.toByteArray(swapBytes);

            this.checkMessageArraySize(data.length);
            System.arraycopy(data, 0, this.messageArray, this.mChunkSize, data.length);
            this.mChunkSize += data.length;
        }

        byte[] transferSize = BaseChunk.convertIntToByte4(this.mChunkSize, swapBytes);
        byte[] transUID = BaseChunk.convertLongToByte4(this.reqestUID, swapBytes);
        System.arraycopy(transferSize, 0, this.messageArray, 4, 4);
        System.arraycopy(transUID, 0, this.messageArray, 8, 4);

        return this.messageArray;
    }

    /**
     * Makes sure there is enough room in the messageArray for checkSize.  If not then makes room.
     * @param checkSize size of the additional data
     */
    private void checkMessageArraySize(int checkSize)
    {
    	if (this.messageArray == null)
        	this.messageArray = new byte[1024];

        if (this.messageArray.length < this.mChunkSize + checkSize)
        {
            int newSize = this.messageArray.length + 1024; //bump it another 1k

            while (newSize < this.mChunkSize + checkSize)
                newSize += 1024; //keep bumping by 1k.  max message size is 2,147,483,647

           	byte[] tmp = new byte[newSize];
           	System.arraycopy(this.messageArray, 0, tmp, 0, this.messageArray.length);
           	this.messageArray = tmp;
        }
    }

    /**
     * Converts the value of the data element to a string.
     *
     * @return the value of the data element as a string.
     */
    @Override
	public String toString()
    {
    	return "<B>" + this.getChunkType() + "</B> (" + this.getChunkSize() + ") transfer ID: <B>" + this.reqestUID + "</B> <I>TimeOut: " + this.timeoutVal + "</I>" + "<I> Wait: " + this.waitDurationMS + "</I>\n" + super.toString();
    }
}
