package iZomateCore.ServerCore.RPCServer.Chunks;

import iZomateCore.ServerCore.CoreEnums.NotificationType;

import java.nio.charset.Charset;

/**
 * A Double data element.
 */
public class DoubleChunk extends BaseChunk
{
    private double value;

    /**
     * Creates a double data element as an old style Real datum.
     *
     * @param name The name of the data element, or null.
     * @param value The value of the data element.
     * @param datumID the datum id to assign to this datum.  Used for Murrow backwards compatibility
     */
    public DoubleChunk(String name, double value, NotificationType datumID)
    {
        super(name, datumID);
        this.value = value;
    }

   /**
     * Creates a double data element.
     *
     * @param name The name of the data element, or null.
     * @param value The value of the data element.
     */
    public DoubleChunk(String name, double value)
    {
        super(name, NotificationType.Dble);
        this.value = value;
    }

    /**
     * Creates a double data element from a string.
     *
     * @param name The name of the data element, or null.
     * @param digits A string containing the value of the data element, formatting per the RPC protocol.
     */

    public DoubleChunk(String name, String digits)
    {
        this(name, Double.valueOf(digits).doubleValue());
    }

    /**
     * Creates a double datum from a byte[] based on the RPC Stream Transfer Specification.
     *
     * @param b buffer containing notification data
     * @param offset starting byte in buffer
     * @param swapBytes true if the endianess of data types does not match this platform
     * @throws Exception
     */
    public DoubleChunk(byte[] b, int offset, boolean swapBytes) throws Exception
    {
        super("", NotificationType.Dble);

        String datumType = new String(b, offset, 4);
        this.mChunkSize = BaseChunk.convertByte4ToInt(b, offset + 4, swapBytes);

        if (!datumType.equals(this.getChunkType().name()))
    		throw new Exception("Expected Datum type: " + this.getChunkType() + ", but detected: " + datumType);

    	if (this.mChunkSize < 16)
    		throw new Exception("Buffer to small to be an DoubleDatum");

        this.value = Double.longBitsToDouble(BaseChunk.convertByte8ToLong(b, offset+8, swapBytes));
        this.mName = new String(b, offset+16, this.mChunkSize-16, Charset.forName("UTF-8"));
    }

    /**
     * Gets the double value of the datum.
     *
     * @return the double value
     */
    @Override
	public double getDouble()
    {
        return this.value;
    }

    /**
     * Converts the datum into a properly formed byte array that is ready
     * to be added to a RQST or RPLY transfer.<br>
     *
     * See RPC Stream Transfer Specification doc for complete protocol details:
     * <A HREF="doc-files/RPCStreamTransferSpecification.docx">RPCStreamTransferSpecification.docx</A>
     *
     * @param swapBytes specifies whether data needs to be byte swapped.<br>
     *               Set to true if sending to RPC Server on Windows.<br>
     *               Set to false if sending to Remote Launcher or RPC Server on OSX
     */
    @Override
	public byte[] toByteArray(boolean swapBytes)
    {
        byte[] nameData = this.mName.getBytes(Charset.forName("UTF-8"));
        this.mChunkSize = 16 + nameData.length;
        byte[] byteArray = new byte [this.mChunkSize];
        byte[] mDatumSize = BaseChunk.convertIntToByte4(this.mChunkSize, swapBytes);
        byte[] data = BaseChunk.convertDbleToByte8(this.value, swapBytes);

        System.arraycopy(this.getChunkType().toString().getBytes(), 	0, byteArray, 0,  4);
        System.arraycopy(mDatumSize,             						0, byteArray, 4,  4);
        System.arraycopy(data,                  						0, byteArray, 8,  8);
        System.arraycopy(nameData,      								0, byteArray, 16, nameData.length);

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
    	return "\t<B>" + this.getChunkType() + "</B> (" + this.getChunkSize() + ") \tName: " + this.mName + "\tValue: " + this.value;
    }

    /**
     * Converts the value of the data element to a string.
     *
     * @return the value of the data element as a string.
     */
	public String valueToString()
	{
		return Double.toString(this.value);
	}
}