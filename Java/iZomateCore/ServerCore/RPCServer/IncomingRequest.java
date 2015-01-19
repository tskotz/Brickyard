package iZomateCore.ServerCore.RPCServer;

import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.RPCServer.Chunks.BaseChunk;
import iZomateCore.ServerCore.RPCServer.Chunks.CompoundChunk;

/**
 * An incoming request. This class is for use within an RPC server.
 */
public class IncomingRequest extends CompoundChunk
{
    private 	long    uid = 0;
    protected 	String 	mFunctionName;

    /**
     * Constructs a request from an RQST byte[] array.
     *
     * @param b the byte[] array containing a reply data.
     * @param offset the starting byte in the array
     * @param swapBytes true if the endianess of data types does not match this platform
     * @throws Exception
     */
    public IncomingRequest(byte[] b, int offset, boolean swapBytes) throws Exception
    {
    	super("IZIncomingRequest");

    	String transferType = new String(b, offset, 4);
    	this.mChunkSize = BaseChunk.convertByte4ToInt(b, offset + 4, swapBytes);
            
    	if (!transferType.equals(NotificationType.RQST.name()))
    		throw new Exception("Expected Transfer type: " + NotificationType.RQST + ", but detected: " + transferType);

    	if (this.mChunkSize < 12)
    		throw new Exception("Buffer to small to be an IZIncomingRequest");

    	this.setChunkType(NotificationType.RQST);
    	this.uid = BaseChunk.convertByte4ToLong(b, offset + 8, swapBytes); //bytes 8-11 are the transfer UID
    	this.setDatums(b, offset+12, this.mChunkSize-12, swapBytes); //offset by 12 to skip get to the data
        
        BaseChunk d = this._getDatum(0);
        if ( d != null )
            this.mFunctionName = d._getFunction();
    }

    /**
     * Gets the function as a string.
     *
     * @return the function string
     */
    @Override
	public String _getFunction ()
    {
        return this.mFunctionName;
    }

    /**
     * Returns the UID of the outgoing request UID which this reply belongs to.
     *
     * @return uid.
     */
    public long getTransactionUID()
    {
        return this.uid;
    }

    /**
     * Converts the value of the data element to a string.
     *
     * @return the value of the data element as a string.
     */
    @Override
	public String toString()
    {
    	String str = " <B>" + this.getChunkType() + "</B> (" + this.getChunkSize() + ") transfer ID: <B>" + this.uid + "</B>\n" + super.toString();
        for ( int i = 0; i < this._getCount(); i++ )
            str += this._getNth(i).toString();
    	str += "\t)";
    	return str;
    }
}
