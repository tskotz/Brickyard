package iZomateCore.ServerCore.Notifications;

import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.RPCServer.Chunks.BaseChunk;

import java.nio.charset.Charset;

/**
 * An 'XCPT' transfer datum.
 */
public class ExceptionNotification extends BaseChunk
{
    private String	m_Value;

    /**
     * Creates an transfer 'XCPT' notification datum.
     * @param message the exception message
     */
    public ExceptionNotification(String message)
    {
        super("", NotificationType.XCPT);
        this.m_Value = message;
    }

    /**
     * Creates an transfer 'XCPT' notification datum from a byte[] based on the RPC Stream Transfer Specification.
     * @param b buffer containing notification data
     * @param offset starting byte in buffer
     * @param swapBytes true if the endianess of data types does not match this platform
     * @throws Exception
     */
    public ExceptionNotification(byte[] b, int offset, boolean swapBytes) throws Exception
    {
        super("", NotificationType.XCPT);

        String transferType = new String(b, offset, 4);
        this.mChunkSize = BaseChunk.convertByte4ToInt(b, offset + 4, swapBytes);

        if (transferType.equals(this.getChunkType()))
    		throw new Exception("Expected Transfer type: " + this.getChunkType() + ", but detected: " + transferType);

    	if (this.mChunkSize < 8)
    		throw new Exception("Buffer to small to be an Exception Notification");

        this.m_Value = new String(b, offset+8, this.mChunkSize-8, Charset.forName("UTF-8"));
    }

    /**
     * Returns the string value of the datum element.
     * @return the string value of the datum element.
     */
    @Override
	public String getString()
    {
        return this.m_Value;
    }

    /**
     * Converts the datum into a properly formed byte array that is ready
     * to be added to a XCPT transfer.<br>
     *
     * See RPC Stream Transfer Specification doc for complete protocol details:
     * <A HREF="doc-files/RPCStreamTransferSpecification.doc">RPCStreamTransferSpecification.doc</A>
     *
     * @param swapBytes specifies whether data needs to be byte swapped.<br>
     *               Set to true if sending to RPC server on Windows.<br>
     *               Set to false if sending to Remote Launcher or RPC server on OSX
     */
    @Override
	public byte[] toByteArray(boolean swapBytes)
    {
    	 if( this.m_Value == null )
         	this.m_Value= "Unknown Exception";
    	 
        this.setChunkType(NotificationType.XCPT);
        this.mChunkSize = 8 + this.m_Value.length();
        byte[] byteArray = new byte [this.mChunkSize];
        byte[] datumSize = BaseChunk.convertIntToByte4(this.mChunkSize, swapBytes);

        System.arraycopy(this.getChunkType().toString().getBytes(),    0, byteArray, 0,             4);
        System.arraycopy(datumSize,                 		0, byteArray, 4,                        4);
        System.arraycopy(this.m_Value.getBytes(),     		0, byteArray, 8+this.mName.length(),     this.m_Value.length());

        return byteArray;
    }

    /**
     * Converts the Notification to a string.
     * @return the Notification as a string.
     */
    @Override
	public String toString()
    {
    	return "<FONT COLOR=\"FF0000\"><B>" + this.getChunkType() + "</B> (" + this.getChunkSize() + ") \n\tMessage: " + this.m_Value + "</FONT>";
    }
    
}
