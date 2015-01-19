package iZomateCore.ServerCore.RPCServer;

import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.RPCServer.Chunks.BaseChunk;
import iZomateCore.ServerCore.RPCServer.Chunks.CompoundChunk;

/**
 * An incoming reply (RPLY) to an iZomate request (RQST).
 */
public class IncomingReply extends CompoundChunk
{
    private long    uid = 0;

    /**
     * Constructs an IZIncomingReply from a byte[] array.
     *
     * @param b the byte[] array containing a reply data.
     * @param offset the starting byte in the array
     * @param swapBytes true if the endianess of data types does not match this platform
     * @throws Exception
     */
    protected IncomingReply(byte[] b, int offset, boolean swapBytes) throws Exception
    {
        super("IZIncomingReply");

        String transferType = new String(b, offset, 4);
        this.mChunkSize = BaseChunk.convertByte4ToInt(b, offset + 4, swapBytes);
        
        if (!transferType.equals(NotificationType.RPLY.name()))
    		throw new Exception("Expected Transfer type: " + NotificationType.RPLY + ", but detected: " + transferType);

    	if (this.mChunkSize < 12)
    		throw new Exception("Buffer to small to be an IZIncomingReply");

        this.setChunkType(NotificationType.RPLY);
        this.uid = BaseChunk.convertByte4ToLong(b, offset + 8, swapBytes); //bytes 8-11 are the transfer UID
        this.setDatums(b, offset+12, this.mChunkSize-12, swapBytes); //offset by 12 to skip get to the data
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
    	return "<B>" + this.getChunkType() + "</B> (" + this.getChunkSize() + ") transfer ID: <B>" + this.uid + "</B>\n" + super.toString();
    }
}
