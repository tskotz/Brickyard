package iZomateCore.ServerCore.RPCServer.Chunks;

import iZomateCore.ServerCore.CoreEnums.NotificationType;

import java.nio.charset.Charset;

/**
 * A float data element.
 */
public class FloatChunk extends BaseChunk
{
    private float value;

    /**
     * Creates a float data element.
     *
     * @param name The name of the data element, or null.
     * @param value The value of the data element.
     */
    public FloatChunk(String name, float value)
    {
        super(name, NotificationType.Flot);
        this.value = value;
    }

    /**
     * Creates a float data element from a string.
     *
     * @param name The name of the data element, or null.
     * @param digits A string containing the value of the data element, formatting per the RPC specification.
     */

    public FloatChunk(String name, String digits)
    {
        this(name, Float.valueOf(digits).longValue());
    }

    /**
     * Creates a float datum from a byte[] based on the RPC Stream Transfer Specification.
     *
     * @param b buffer containing notification data
     * @param offset starting byte in buffer
     * @param swapBytes true if the endianess of data types does not match this platform
     * @throws Exception
     */
    public FloatChunk(byte[] b, int offset, boolean swapBytes) throws Exception
    {
        super("", NotificationType.Flot);

        String datumType = new String(b, offset, 4);
        this.mChunkSize = BaseChunk.convertByte4ToInt(b, offset + 4, swapBytes);

        if (!datumType.equals(this.getChunkType().name()))
    		throw new Exception("Expected Datum type: " + this.getChunkType() + ", but detected: " + datumType);

    	if (this.mChunkSize < 12)
    		throw new Exception("Buffer to small to be an FloatDatum");

        this.value = Float.intBitsToFloat(BaseChunk.convertByte4ToInt(b, offset+8, swapBytes));
        this.mName = new String(b, offset+12, this.mChunkSize-12, Charset.forName("UTF-8"));
    }

    /**
     * Gets the float value of the datum.
     *
     * @return the float value
     */
    @Override
	public float getFloat()
    {
        return this.value;
    }

    /**
     * Converts the datum into a properly formed byte array that is ready
     * to be added to a RQST or RPLY transfer.<br>
     *
     * See RPC Stream Transfer Specification doc for complete protocol details:
     * <A HREF="doc-files/RPCStreamTransferSpecification.doc">RPCStreamTransferSpecification.doc</A>
     *
     * @param swapBytes specifies whether data needs to be byte swapped.<br>
     *               Set to true if sending to RPC Server on Windows.<br>
     *               Set to false if sending to Remote Launcher or RPC Server on OSX
     */
    @Override
	public byte[] toByteArray(boolean swapBytes)
    {
        byte[] nameData = this.mName.getBytes(Charset.forName("UTF-8"));
        this.mChunkSize = 12 + nameData.length;
        byte[] byteArray = new byte [this.mChunkSize];
        byte[] mDatumSize = convertIntToByte4(this.mChunkSize, swapBytes);
        byte[] data = convertFloatToByte4(this.value, swapBytes);

        System.arraycopy(this.getChunkType().toString().getBytes(), 	0, byteArray, 0,  4);
        System.arraycopy(mDatumSize,             						0, byteArray, 4,  4);
        System.arraycopy(data,                  						0, byteArray, 8,  4);
        System.arraycopy(nameData,      								0, byteArray, 12, nameData.length);

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
	@Override
	public String valueToString()
	{
		return Float.toString(this.value);
	}
}