package iZomateCore.ServerCore.RPCServer.Chunks;

import iZomateCore.ServerCore.CoreEnums.NotificationType;

import java.nio.charset.Charset;

/**
 * A boolean data element.
 */
public class BooleanChunk extends BaseChunk
{
    private boolean mValue;

    /**
     * Creates a boolean data element.
     *
     * @param name The name of the data element, or null.
     * @param value The value of the data element.
     */
    public BooleanChunk(String name, boolean value)
    {
        super(name, NotificationType.Bool);
        this.mValue = value;
    }

    /**
     * Creates a boolean datum from a byte[] based on the RPC Stream Transfer Specification.
     *
     * @param b buffer containing notification data
     * @param offset starting byte in buffer
     * @param swapBytes true if the endianess of data types does not match this platform
     * @throws Exception
     */
    public BooleanChunk(byte[] b, int offset, boolean swapBytes) throws Exception
    {
        super("", NotificationType.Bool);

        String datumType = new String(b, offset, 4);
        this.mChunkSize = BaseChunk.convertByte4ToInt(b, offset + 4, swapBytes);

        if (!datumType.equals(this.getChunkType().name()))
    		throw new Exception("Expected Datum type: " + this.getChunkType() + ", but detected: " + datumType);

    	if (this.mChunkSize < 9)
    		throw new Exception("Buffer to small to be an Boolean Datum");

        this.mValue = (b[offset+8] == 1);
        this.mName = new String(b, offset+9, this.mChunkSize-9, Charset.forName("UTF-8"));
    }

    /**
     * Returns the boolean value of the datum element.
     *
     * @return the boolean value of the datum element.
     */
    @Override
	public boolean getBoolean()
    {
        return this.mValue;
    }

    /**
     * Converts the datum into a properly formed byte array that is ready to be added to a RQST or RPLY transfer.<br>
     *
     * See RPC Stream Transfer Specification doc for complete protocol details:
     * <A HREF="doc-files/RPCStreamTransferSpecification.docx">RPCStreamTransferSpecification.docx</A>
     *
     * @param swapBytes specifies whether data needs to be byte swapped.<br>
     *               Set to true if sending to RPC Server on Windows.<br>
     *               Set to false if sending to Remote Launcher or RPC Server on OSX
     *
     * @return byte array containing the datums data
     */
    @Override
	public byte[] toByteArray(boolean swapBytes)
    {
        byte[] nameData = this.mName.getBytes(Charset.forName("UTF-8"));
        this.mChunkSize = 9 + nameData.length;
        byte[] byteArray = new byte [this.mChunkSize];
        byte[] datumSize = BaseChunk.convertIntToByte4(this.mChunkSize, swapBytes);
        byte[] data = BaseChunk.convertIntToByte1(this.mValue ? 1 : 0);

        System.arraycopy(this.getChunkType().toString().getBytes(), 	0, byteArray, 0, 4);
        System.arraycopy(datumSize,    									0, byteArray, 4, 4);
        System.arraycopy(data,         									0, byteArray, 8, 1);
        System.arraycopy(nameData,										0, byteArray, 9, nameData.length);

        return byteArray;
    }

    /**
     * Converts the value of the data element to a string.
     *
     * @return the value of the data element as a string.
     */
    @Override
	public String toString()
    {
    	return "\t<B>" + this.getChunkType() + "</B> (" + this.getChunkSize() + ") \tName: " + this.mName + "\tValue: " + this.mValue;
    }

    /**
     * Converts the value of the data element to a string.
     *
     * @return the value of the data element as a string.
     */
	@Override
	public String valueToString()
	{
		return Boolean.toString(this.mValue);
	}

}
