package iZomateCore.ServerCore.RPCServer.Chunks;

import iZomateCore.ServerCore.CoreEnums.NotificationType;

import java.nio.charset.Charset;

/**
 * A String data element.
 */
public class StringChunk extends BaseChunk
{
    private String mValue;

    /**
     * Creates a string data element from non UTF-8 characters.
     *
     * @param name The name of the data element, or null.
     * @param value The value of the data element.
     */
    public StringChunk(String name, String value)
    {
    	super(name, NotificationType.Stri);
    	this.mValue = value;
    }

    /**
     * Creates a String datum from a byte[] based on the RPC Stream Transfer Specification.
     *
     * @param b buffer containing notification data
     * @param offset starting byte in buffer
     * @param swapBytes true if the endianess of data types does not match this platform
     * @throws Exception
     */
    public StringChunk(byte[] b, int offset, boolean swapBytes) throws Exception
    {
        super("", NotificationType.Stri);

        String datumType = new String(b, offset, 4);
        this.mChunkSize = BaseChunk.convertByte4ToInt(b, offset + 4, swapBytes);

        if (!datumType.equals(this.getChunkType().name()))
    		throw new Exception("Expected Datum type: " + this.getChunkType() + ", but detected: " + datumType);

    	if (this.mChunkSize < 9)
    		throw new Exception("Buffer to small to be a StringDatum");

        byte nameSize = b[offset+8];
        this.mName = new String(b, offset+9, nameSize, Charset.forName("UTF-8"));
        this.mValue = new String(b, offset+9+nameSize, this.mChunkSize-(9+nameSize), Charset.forName("UTF-8"));
    }

    /**
     * Returns the value of a string data element.
     *
     * @return The value of the data element.
     */
    @Override
	public String getString()
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
     *               Set to true if sending to RPC Server on Windows.<br>
     *               Set to false if sending to Remote Launcher or RPC Server on OSX
     */
    @Override
	public byte[] toByteArray(boolean swapBytes)
    {
        byte[] nameData = this.mName.getBytes(Charset.forName("UTF-8"));
		byte[] nameSize = BaseChunk.convertIntToByte1(nameData.length);
        byte[] data = this.mValue.getBytes(Charset.forName("UTF-8"));
        this.mChunkSize = 9 + nameData.length + data.length;
		byte[] byteArray = new byte[this.mChunkSize];
		byte[] datumSize = BaseChunk.convertIntToByte4(this.mChunkSize, swapBytes);

        System.arraycopy(this.getChunkType().toString().getBytes(), 0, byteArray, 0,  					4);
		System.arraycopy(datumSize, 								0, byteArray, 4, 					4);
		System.arraycopy(nameSize, 									0, byteArray, 8, 					1);
		System.arraycopy(nameData, 									0, byteArray, 9, 					nameData.length);
		System.arraycopy(data, 										0, byteArray, 9 + nameData.length, 	data.length);

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
    	String tabs = this.mName.length() > 4?"\t\t":"\t";
    	return "\t<B>" + this.getChunkType() + "</B> (" + this.getChunkSize() + ") \tName: " + this.mName + tabs + "Value: " + this.mValue;
    }

    /**
     * Converts the value of the data element to a string.
     *
     * @return the value of the data element as a string.
     */
	@Override
	public String valueToString()
	{
		return this.mValue;
	}

}
