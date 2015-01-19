package iZomateCore.ServerCore.RPCServer.Chunks;

import iZomateCore.ServerCore.CoreEnums.NotificationType;

import java.nio.charset.Charset;

/**
 * A 32 bit unsigned integer data element.
 */
public class UInt32Chunk extends BaseChunk
{
    private long mValue;

    /**
     * Creates a 32 bit unsigned integer data element.
     *	
     *      C++ ->  A uint32 is 0 to 4,294,967,295 [ 4 bytes (32 bits) ]
     *      Java -> Does not support unsigned data types. Will store as a Java long (64 bit)
     *
     * @param name The name of the data element, or null.
     * @param value The value of the data element.
     * @throws Exception Thrown if the value does not fall in the range of 0 to 4,294,967,295
     */
    public UInt32Chunk(String name, long value) throws Exception
    {
    	super(name, NotificationType.UI32);
        // Throw if we accidentally attempt to create a uint 32 datum with a value C++ can't handle
        if (value < 0 || value > 4294967295L)
            throw new Exception(value + " out of range (0 to 4,294,967,295)");
        this.mValue = value;
    }

    public UInt32Chunk(byte[] b, int offset, boolean swapBytes) throws Exception
    {
        super("", NotificationType.UI32);
        
        String datumType = new String(b, offset, 4);
        this.mChunkSize = BaseChunk.convertByte4ToInt(b, offset + 4, swapBytes);

        if (!datumType.equals(this.getChunkType().name()))
    		throw new Exception("Expected Datum type: " + this.getChunkType() + ", but detected: " + datumType);

    	if (this.mChunkSize < 12)
    		throw new Exception("Buffer to small to be an RPCuint32Datum");

        this.mValue = BaseChunk.convertByte4ToLong(b, offset+8, swapBytes);
        this.mName = new String(b, offset+12, this.mChunkSize-12, Charset.forName("UTF-8"));
    }

    /**
     * Returns the value of a unsigned 32 bit integer data element.
     *
     * @return The unsigned 32 bit integer value of the data element.
     */
	public long getUInt32()
    {
        return this.mValue;
    }

	public byte[] toByteArray(boolean swapBytes)
	{
        byte[] nameData = this.mName.getBytes(Charset.forName("UTF-8"));
        this.mChunkSize = 12 + nameData.length;
        byte[] byteArray = new byte [this.mChunkSize];
        byte[] datumSize = BaseChunk.convertIntToByte4(this.mChunkSize, swapBytes);
        byte[] data = BaseChunk.convertLongToByte4(this.mValue, swapBytes);

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
	public String toString()
    {
    	return "\t<B>" + this.getChunkType() + "</B> (" + this.getChunkSize() + ") \tName: " + this.mName + "\tValue: " + this.mValue;
    }

    /**
     * Converts the value of the data element to a string.
     *
     * @return the value of the data element as a string.
     */
	public String valueToString()
	{
		return Long.toString(this.mValue);
	}

}
