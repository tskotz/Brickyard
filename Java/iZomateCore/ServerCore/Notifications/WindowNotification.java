package iZomateCore.ServerCore.Notifications;

import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.RPCServer.Chunks.BaseChunk;

import java.nio.charset.Charset;

/**
 * A 'WNDW' RPC transfer notification.
 */
public class WindowNotification extends BaseChunk
{
    private String              m_Status;
    private String              m_ID;
    private String              m_Title;

    /**
     * Creates an RPC transfer 'WNDW' notification.
     *
     * @param status the window status.
     * @param id the window identified.
     * @param title the window title text.
     */
    public WindowNotification(String status, String id, String title)
    {
        super("", NotificationType.WNDW);
        this.m_Status = status;
        this.m_ID = id;
        this.m_Title = title;
    }

    /**
     * Creates an RPC transfer 'WNDW' notification chunk from a byte[] based on the RPC Stream Transfer Specification.
     *
     * @param b buffer containing notification data
     * @param offset starting byte in buffer
     * @param swapBytes true if the endianess of data types does not match this platform
     * @throws Exception
     */
    public WindowNotification(byte[] b, int offset, boolean swapBytes) throws Exception
    {
        super("", NotificationType.WNDW);

        String transferType = new String(b, offset, 4);
        this.mChunkSize = BaseChunk.convertByte4ToInt(b, offset + 4, swapBytes);

        if (!transferType.equals(this.getChunkType().name()))
    		throw new Exception("Expected Transfer type: " + this.getChunkType() + ", but detected: " + transferType);

    	if (this.mChunkSize < 10)
    		throw new Exception("Buffer to small to be an Window Notification");

        byte titleSize = b[offset+8];
        this.m_Title = new String(b, offset+9, titleSize, Charset.forName("UTF-8"));
        byte uidSize = b[offset+9+titleSize];
        this.m_ID = new String(b, offset+10+titleSize, uidSize, Charset.forName("UTF-8"));
        this.m_Status = new String(b, offset+10+titleSize+uidSize, this.mChunkSize-(10+titleSize+uidSize), Charset.forName("UTF-8"));
    }

    /**
     * Returns the status message of the window event
     * @return the status message of the window event
     */
    public String getWindowStatus()
    {
        return this.m_Status;
    }

    /**
     * Returns the window pane id of the window event
     * @return the window pane id of the window event
     */
    public String getWindowID()
    {
        return this.m_ID;
    }

    /**
     * Returns the window title of the window event
     * @return the window title of the window event
     */
    public String getWindowTitle()
    {
        return this.m_Title;
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
        return "<FONT COLOR=\"006600\"><B>" + this.getChunkType() + "</B> (" + this.getChunkSize() + ") \n\tTitle: " + this.m_Title + "\n\tID: " + this.m_ID + "\n\tMessage: " + this.m_Status + "</FONT>";
    }
}
