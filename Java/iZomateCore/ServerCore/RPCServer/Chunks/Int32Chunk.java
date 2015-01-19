package iZomateCore.ServerCore.RPCServer.Chunks;

import iZomateCore.ServerCore.CoreEnums.NotificationType;

import java.nio.charset.Charset;

/**
 * A 32 bit integer data element.
 */
public class Int32Chunk extends BaseChunk
{
    private int mValue;

    /**
     * Creates a 32 bit integer data element.
     *	
     *	Java and C++ types are identical: 32 bit, -2,147,483,648 to 2,147,483,647
     *
     * @param name The name of the data element, or null.
     * @param value The value of the data element.
     */
    public Int32Chunk(String name, int value)
    {
        super(name, NotificationType.In32);
        this.mValue = value;
    }

    /**
     * Creates a 32 bit integer datum from a byte[] based on the RPC Stream Transfer Specification.
     *
     * @param b buffer containing notification data
     * @param offset starting byte in buffer
     * @param swapBytes true if the endianess of data types does not match this platform
     * @throws Exception
     */
    public Int32Chunk(byte[] b, int offset, boolean swapBytes) throws Exception
    {
        super("", NotificationType.In32);

        String datumType = new String(b, offset, 4);
        this.mChunkSize = BaseChunk.convertByte4ToInt(b, offset + 4, swapBytes);

        if (!datumType.equals(this.getChunkType().name()))
    		throw new Exception("Expected Datum type: " + this.getChunkType() + ", but detected: " + datumType);

    	if (this.mChunkSize < 12)
    		throw new Exception("Buffer to small to be an Int32Datum");

        this.mValue = BaseChunk.convertByte4ToInt(b, offset+8, swapBytes);
        this.mName = new String(b, offset+12, this.mChunkSize-12, Charset.forName("UTF-8"));
    }

    /**
     * Creates a 32 bit integer data element from a string.
     *
     * @param name The name of the data element, or null.
     * @param digits A string containing the value of the data element.
     * @throws Exception 
     * @throws NumberFormatException 
     */
    public Int32Chunk(String name, String digits)
    {
        this(name, Integer.valueOf(digits).intValue());
    }

    /**
     * Returns the value of a 32 bit integer data element.
     *
     * @return The 32 bit integer value of the data element.
     */
    @Override
	public int getInt32()
    {
        return this.mValue;
    }

    /**
     * Converts the datum into a properly formed byte array that is ready
     * to be added to a RQST or RPLY transfer.<br>
     *
     * See RPC Stream Transfer Specification doc for complete protocol details:
     * <A HREF="doc-files/RPCStreamTransferSpecification.docx">RPCStreamTransferSpecification.docx</A>
     *
     * @param swapBytes specifies whether data needs to be byte swapped.<br>
     *               Set to true if sending to ARRPC ServerO on Windows.<br>
     *               Set to false if sending to Remote Launcher or RPC Server on OSX
     */
    @Override
	public byte[] toByteArray(boolean swapBytes)
    {
        byte[] nameData = this.mName.getBytes(Charset.forName("UTF-8"));
        this.mChunkSize = 12 + nameData.length;
        byte[] byteArray = new byte [this.mChunkSize];
        byte[] datumSize = BaseChunk.convertIntToByte4(this.mChunkSize, swapBytes);
        byte[] data = BaseChunk.convertIntToByte4(this.mValue, swapBytes);

        System.arraycopy(this.getChunkType().toString().getBytes(), 	0, byteArray, 0,  	4);
        System.arraycopy(datumSize,             						0, byteArray, 4,    4);
        System.arraycopy(data,                  						0, byteArray, 8,    4);
        System.arraycopy(nameData,      								0, byteArray, 12,	nameData.length);

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
		return Integer.toString(this.mValue);
	}

}
