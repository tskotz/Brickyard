package iZomateCore.ServerCore.Notifications;

import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.RPCServer.Chunks.BaseChunk;

import java.nio.charset.Charset;

/**
 * A 'DLOG' RPC transfer Notification.
 */
public class DialogNotification extends BaseChunk
{
    private String m_Title;
    private String m_Buttons;
    private String m_Message;

    /**
     * Creates an RPC transfer 'DLOG' notification.
     * @param title
     * @param buttons the text from the dialog's button(s)
     * @param message the text from the dialog message
     */
    public DialogNotification(String title, String buttons, String message)
    {
        super("", NotificationType.DLOG);

        this.m_Title = title;
        this.m_Buttons = buttons;
        this.m_Message = message;
    }

    /**
     * Creates an RPC transfer 'DLOG' notification Chunk from a byte[] based on the RPC Stream Transfer Specification.
     * @param b buffer containing notification data
     * @param offset starting byte in buffer
     * @param swapBytes true if the endianess of data types does not match this platform
     * @throws Exception
     */
    public DialogNotification(byte[] b, int offset, boolean swapBytes) throws Exception
    {
        super("", NotificationType.DLOG);

        String transferType = new String(b, offset, 4);
        this.mChunkSize = BaseChunk.convertByte4ToInt(b, offset + 4, swapBytes);

        if (!transferType.equals(this.getChunkType().name()))
    		throw new Exception("Expected Transfer type: " + this.getChunkType() + ", but detected: " + transferType);

        if (this.mChunkSize < 10)
    		throw new Exception("Buffer to small to be an Dialog Notification");

        byte titleSize = b[offset+8];
        this.m_Title = new String(b, offset+9, titleSize, Charset.forName("UTF-8"));
        byte buttonSize = b[offset+9+titleSize];
        this.m_Buttons = new String(b, offset+10+titleSize, buttonSize, Charset.forName("UTF-8"));
        this.m_Message = new String(b, offset+10+titleSize+buttonSize, this.mChunkSize-(10+titleSize+buttonSize), Charset.forName("UTF-8"));
    }

    /**
     * Returns the button(s) from the dialog
     * @return the text from the dialog's button(s)
     */
    public String[] getDialogButtons()
    {
    	return this.m_Buttons.split("|");
    }

    /**
     * Returns the title from the dialog
     * @return the title text from the dialog
     */
    public String getDialogTitle()
    {
        return this.m_Title;
    }

    /**
     * Returns the message from the dialog
     * @return the message text from the dialog
     */
    public String getDialogMessage()
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
    	return "<FONT COLOR=\"990099\"><B>" + this.getChunkType() + "</B> (" + this.getChunkSize() + ") \n\tTitle: " + this.m_Title + "\n\tButtons: " + this.m_Buttons + "\n\tMessage: " + this.m_Message + "</FONT>";
    }
}
