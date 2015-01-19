package iZomateCore.ServerCore.RPCServer.Chunks;

import iZomateCore.ServerCore.CoreEnums.NotificationType;

import java.nio.charset.Charset;

/**
 * A data element representing the remote function to execute
 */
public class FunctionChunk extends BaseChunk
{
    private String mFunction;

    /**
     * Creates a function data element.
     *
     * @param functionName The name of the RPC function to call.
     */
    public FunctionChunk(String functionName)
    {
    	super("", NotificationType.Func);
        this.mFunction = functionName;
    }

    /**
     * Creates a function datum from a byte[] based on the RPC Stream Transfer Specification.
     *
     * @param b buffer containing notification data
     * @param offset starting byte in buffer
     * @param swapBytes true if the endianess of data types does not match this platform
     * @throws Exception
     */
    public FunctionChunk(byte[] b, int offset, boolean swapBytes) throws Exception
    {
        super("", NotificationType.Func);

        String datumType = new String(b, offset, 4);
        this.mChunkSize = BaseChunk.convertByte4ToInt(b, offset + 4, swapBytes);

        if (!datumType.equals(this.getChunkType().name()))
    		throw new Exception("Expected Datum type: " + this.getChunkType() + ", but detected: " + datumType);

    	if (this.mChunkSize < 8)
    		throw new Exception("Buffer to small to be an FunctionDatum");

        this.mFunction = new String(b, offset+8, (int)this.mChunkSize-8, Charset.forName("UTF-8"));
    }

    /**
     * Gets the function as a string.
     *
     * @return the function string
     */
    @Override
	public String _getFunction()
    {
        return this.mFunction;
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
        byte[] data = this.mFunction.getBytes(Charset.forName("UTF-8"));
        this.mChunkSize = 8 + data.length;
        byte[] byteArray = new byte [this.mChunkSize];
        byte[] datumSize = BaseChunk.convertIntToByte4(this.mChunkSize, swapBytes);

        System.arraycopy(this.getChunkType().toString().getBytes(), 	0, byteArray, 0,  4);
        System.arraycopy(datumSize,             						0, byteArray, 4,  4);
        System.arraycopy(data,  										0, byteArray, 8, data.length);

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
    	return "\t<B>" + this.getChunkType() + "</B> (" + this.getChunkSize() + ") \tValue: " + this.mFunction;
    }

    /**
     * Converts the value of the data element to a string.
     *
     * @return the value of the data element as a string.
     */
	@Override
	public String valueToString()
	{
		return this.mFunction;
	}
}
