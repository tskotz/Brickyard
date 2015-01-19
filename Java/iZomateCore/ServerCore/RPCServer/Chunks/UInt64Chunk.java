package iZomateCore.ServerCore.RPCServer.Chunks;

import iZomateCore.ServerCore.CoreEnums.NotificationType;

import java.math.BigInteger;
import java.nio.charset.Charset;

/**
 * A 64 bit unsigned integer data element.
 */
public class UInt64Chunk extends BaseChunk
{
    private BigInteger value;

    /**
     * Creates a 64 bit unsigned integer data element.
     *
     *      C++ ->  An uint64 is 0 to 18,446,744,073,709,551,615 [ 8 bytes (64 bits) ]
     *      Java -> Does not support unsigned data types. Will store as a Java BigInteger.
     *
     * @param name The name of the data element, or null.
     * @param value The value of the data element.
     * @throws Exception Thrown if the value does not fall in the short range of 0 to 18,446,744,073,709,551,615
     */
    public UInt64Chunk(String name, BigInteger value) throws Exception
    {
        super(name, NotificationType.UI64);
        // Throw if we accidentally attempt to create a uint 64 datum with a value C++ can't handle
        if (value.compareTo(new BigInteger("0")) == -1 || value.compareTo(new BigInteger("18446744073709551615")) == 1)
            throw new Exception(value + " out of range (0 to 18,446,744,073,709,551,615)");
        this.value = value;
    }

    /**
     * Creates a 64 bit unsigned integer data element from a string.
     *
     * @param name The name of the data element, or null.
     * @param digits A string containing the value of the data element, formatting per the RPC specification.
     * @throws Exception 
     */

    public UInt64Chunk(String name, String digits) throws Exception
    {
        this(name, new BigInteger(digits));
    }

    /**
     * Creates a 64 bit unsigned integer datum from a byte[] based on the RPC Stream Transfer Specification.
     *
     * @param b buffer containing notification data
     * @param offset starting byte in buffer
     * @param swapBytes true if the endianess of data types does not match this platform
     * @throws Exception
     */
    public UInt64Chunk(byte[] b, int offset, boolean swapBytes) throws Exception
    {
        super("", NotificationType.UI64);

        String datumType = new String(b, offset, 4);
        this.mChunkSize = BaseChunk.convertByte4ToInt(b, offset + 4, swapBytes);

        if (!datumType.equals(this.getChunkType().name()))
    		throw new Exception("Expected Datum type: " + this.getChunkType() + ", but detected: " + datumType);

    	if (this.mChunkSize < 16)
    		throw new Exception("Buffer to small to be an UInt64Datum");

    	byte[] tmp = new byte[8];
        System.arraycopy(b, offset+8, tmp, 0,  8);
        this.value = new BigInteger(1, tmp);
        this.mName = new String(b, offset+16, this.mChunkSize-16, Charset.forName("UTF-8"));
    }

    /**
     * Gets the 64 bit unsigned integer value of the datum.
     *
     * @return the 64 bit unsigned integer value
     */
    @Override
	public BigInteger getUInt64()
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
        this.mChunkSize = 16 + nameData.length;
        byte[] byteArray = new byte [this.mChunkSize];
        byte[] datumSize = BaseChunk.convertIntToByte4(this.mChunkSize, swapBytes);
        byte[] data = BaseChunk.convertLongToByte8(this.value.longValue(), swapBytes);

        System.arraycopy(this.getChunkType().toString().getBytes(), 	0, byteArray, 0,  4);
        System.arraycopy(datumSize,             						0, byteArray, 4,  4);
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
	@Override
	public String valueToString()
	{
		return this.value.toString();
	}
}