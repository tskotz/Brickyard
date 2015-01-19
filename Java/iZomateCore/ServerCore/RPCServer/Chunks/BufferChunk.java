package iZomateCore.ServerCore.RPCServer.Chunks;

import iZomateCore.ServerCore.CoreEnums.NotificationType;

import java.nio.charset.Charset;

/**
 * A Buffer data element.
 */
public class BufferChunk extends BaseChunk
{
    private byte[]              value;

    public BufferChunk(String name, byte[] value)
    {
    	super(name, NotificationType.Buff);
    	this.value = value;
    }

    public BufferChunk(byte[] b, int offset, boolean swapBytes) throws Exception
    {
        super("", NotificationType.Buff);

        String datumType = new String(b, offset, 4);
        this.mChunkSize = BaseChunk.convertByte4ToInt(b, offset + 4, swapBytes);

        if (!datumType.equals(this.getChunkType().name()))
    		throw new Exception("Expected Datum type: " + this.getChunkType() + ", but detected: " + datumType);

    	if (this.mChunkSize < 9)
    		throw new Exception("Buffer to small to be an BufferDatum");

    	byte nameSize = b[offset+8];
        this.mName = new String(b, offset+9, nameSize);
        this.value = new byte[this.mChunkSize-(9+nameSize)];
        System.arraycopy(b, offset+9+nameSize, this.value, 0, this.value.length);
    }
    
    /**
     * Returns the buffer byte[].
     *
     * @return The value of the data element.
     */
    @Override
	public byte[] getBuffer()
    {
        return this.value;
    }

    //=====================================================================
    /**
     * Converts the datum into a properly formed byte array that is ready
     * to be added to a RQST or RPLY transfer.<br>
     *
     * See RPC Stream Transfer Specification doc for complete protocol details:
     * <A HREF="doc-files/RPCStreamTransferSpecification.docx">RPCStreamTransferSpecification.docx</A>
     *
     * @param swapBytes specifies whether data needs to be byte swapped.<br>
     *               Set to true if sending to RPC on Windows.<br>
     *               Set to false if sending to Remote Launcher or RPC Server on OSX
     */
	public byte[] toByteArray(boolean swapBytes)
	{
        byte[] nameData = this.mName.getBytes(Charset.forName("UTF-8"));
        byte[] nameSize = BaseChunk.convertIntToByte1(nameData.length);
        this.mChunkSize = 9 + nameData.length + this.value.length;
        byte[] byteArray = new byte [this.mChunkSize];
        byte[] datumSize = BaseChunk.convertIntToByte4(this.mChunkSize, swapBytes);

        System.arraycopy(this.getChunkType().toString().getBytes(), 0, byteArray, 0,  					4);
        System.arraycopy(datumSize,                 				0, byteArray, 4,                    4);
        System.arraycopy(nameSize,                  				0, byteArray, 8,                    1);
        System.arraycopy(nameData,      							0, byteArray, 9,                    nameData.length);
        System.arraycopy(this.value,     							0, byteArray, 9 + nameData.length,	this.value.length);

        return byteArray;
	}

    /**
     * Converts the value of the data element to a string.
     *
     * @return the value of the data element as a string.
     */
	public String toString()
	{
    	String val = "*Raw value datum logging disabled for values larger than 500*";

    	if (this.value.length <= 500)
    		val = this.valueToString();

    	return "\t<B>" + this.getChunkType() + "</B> (" + this.getChunkSize() + ") \tName: " + this.mName + "\tValue: " + val;
    }

    /**
     * Converts the value of the data element to a string.
     *
     * @return the value of the data element as a string.
     */
	public String valueToString()
	{
		return new String(this.value);
	}

}
