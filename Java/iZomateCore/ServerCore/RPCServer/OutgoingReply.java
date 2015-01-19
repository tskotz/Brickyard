package iZomateCore.ServerCore.RPCServer;

import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.ExceptionNotification;

/**
 * An outgoing response. This class is for use within an RPC server.
 */
public class OutgoingReply extends OutgoingRequest
{
    protected ExceptionNotification 	excptn = null; //is the outgoing data an exception

    /**
     * Creates an empty IZOutgoingReply
     * @param uid the request transfer uid this reply is associated with.
     */
	public OutgoingReply(long uid)
    {
        super(null, uid);
        this.setChunkType(NotificationType.RPLY);
    }

    /**
     * Turns the response into an exception and sets the exception code.
     * @param message the exception name
     */
    public void setException(String message)
    {
        this.excptn = new ExceptionNotification(message);
    }

    /**
     * Creates a Reply Transfer byte []
     *
     * @param swapBytes specifies whether data needs to be byte swapped.<br>
     * @return byte []
     */
    public byte[] toReplyTransfer(boolean swapBytes)
    {
        if ( this.excptn != null )
        	return this.excptn.toByteArray(swapBytes);
        else
            return this.toByteArray(swapBytes);
    }

    /**
     * @return the size of this datum in bytes
     */
    @Override
	public int getChunkSize()
    {
        if ( this.excptn != null )
        	return this.excptn.getChunkSize();
        else
            return this.mChunkSize;
    }

}