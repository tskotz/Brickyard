package iZomateCore.ServerCore.Notifications;

import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.RPCServer.Chunks.BaseChunk;

import java.nio.charset.Charset;

/**
 *
 */
public class StatusNotification extends BaseChunk
{
    private String		m_Message;
    private int         m_Percentage;

    /**
     * Creates an RPC transfer 'STAT' notification datum.
     * @param percentage
     * @param message
     */
    public  StatusNotification(int percentage, String message)
    {
        super("", NotificationType.STAT);
    	this.m_Percentage = percentage;
    	this.m_Message = message;
    }

    /**
     * Creates an RPC transfer 'STAT' notification datum from a byte[] based on the RPC Stream Transfer Specification.
     * @param b buffer containing notification data
     * @param offset starting byte in buffer
     * @param swapBytes true if the endianess of data types does not match this platform
     * @throws Exception
     *
     */
    public StatusNotification(byte[] b, int offset, boolean swapBytes) throws Exception
    {
        super("", NotificationType.STAT);

        String transferType = new String(b, offset, 4);
        this.mChunkSize = BaseChunk.convertByte4ToInt(b, offset + 4, swapBytes);

        if (!transferType.equals(this.getChunkType().name()))
    		throw new Exception("Expected Transfer type: " + this.getChunkType().name() + ", but detected: " + transferType);

    	if (this.mChunkSize < 9)
    		throw new Exception("Buffer to small to be an Status Notification");

        this.m_Percentage = b[offset+8]; //Get the progress (%) byte, increment offset by one
        this.m_Message = new String(b, offset+9, this.mChunkSize-9, Charset.forName("UTF-8")); //Get the message text minus 4(type), 4(size), 1(progess)
    }

    /**
     * @return the (optional) int value
     */
    public int getPercentage()
    {
        return this.m_Percentage;
    }

    /**
     * @return the event message
     */
    public String getMessage()
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
    	return "<FONT COLOR=\"880000\"><B>" + this.getChunkType() + "</B> (" + this.getChunkSize() + ") \n\tProgress: " + this.m_Percentage + "%\n\tMessage: " + this.m_Message + "</FONT>";
	}
}
