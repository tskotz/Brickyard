package iZomateCore.ServerCore.Notifications;

import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.RPCServer.Chunks.BaseChunk;

import java.nio.charset.Charset;

/**
 * An 'EVNT' RPC transfer notification.
 */
public class EventNotification extends BaseChunk
{
    private String              m_EventType;
    private String              m_Message;
    private int                 m_EventInt;

    /**
     * Creates an RPC transfer 'EVNT' notification.
     *
     * @param type the type of application event
     * @param message the event message
     * @param eventInt (optional) int value.
     */
    public EventNotification(String type, String message, int eventInt)
    {
        super("", NotificationType.EVNT);
        this.m_EventInt = eventInt;
        this.m_EventType = type;
        this.m_Message = message;
    }

    /**
     * Creates an RPC transfer 'EVNT' notification from a byte[] based on the RPC Stream Transfer Specification.
     *
     * @param b buffer containing notification data
     * @param offset starting byte in buffer
     * @param swapBytes true if the endianess of data types does not match this platform
     * @throws Exception
     */
    public EventNotification(byte[] b, int offset, boolean swapBytes) throws Exception
    {
        super("", NotificationType.EVNT);

        String transferType = new String(b, offset, 4);
        this.mChunkSize = BaseChunk.convertByte4ToInt(b, offset + 4, swapBytes);
        
        if (!transferType.equals(this.getChunkType().name()))
    		throw new Exception("Expected Transfer type: " + this.getChunkType().name() + ", but detected: " + transferType);

    	if (this.mChunkSize < 13)
    		throw new Exception("Buffer to small to be an Event Notification");

	    this.m_EventInt = BaseChunk.convertByte4ToInt(b, offset+8, swapBytes);
	    byte eventTypeSize = b[offset+12];
	    this.m_EventType = new String(b, offset+13, eventTypeSize, Charset.forName("UTF-8"));
	    this.m_Message = new String(b, offset+13+eventTypeSize, this.mChunkSize-(13+eventTypeSize), Charset.forName("UTF-8"));
    }

    /**
     * @return the (optional) int value
     */
    public int getEventIntValue()
    {
        return this.m_EventInt;
    }

    /**
     * @return the event type
     */
    public String getEventType()
    {
        return this.m_EventType;
    }

    /**
     * @return the event message
     */
    public String getEventMessage()
    {
        return this.m_Message;
    }

    /**
     * Not needed for Notifications
     */
    @Override
	public byte[] toByteArray(boolean swapBytes)
    {
    	return null;
    }

    /**
     * Converts the Notification to a string.
     * @return the Notification as a string.
     */
    @Override
	public String toString()
    {
    	String charVal = (this.m_EventType.equals(EventSubType.KeyboardEvent.getValue())) ? " [" + (char)this.m_EventInt + "]" : "";
    	
    	return "<FONT COLOR=\"0000CC\"><B>" + this.getChunkType() + "</B> (" + this.getChunkSize() + ") \n\tType: " + this.m_EventType + "\n\tValue: " + this.m_EventInt + charVal + "\n\tMessage: " + this.m_Message + "</FONT>";
    }
}
