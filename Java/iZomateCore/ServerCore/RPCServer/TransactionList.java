package iZomateCore.ServerCore.RPCServer;

import iZomateCore.LogCore.TransactionLog;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.DialogNotification;
import iZomateCore.ServerCore.Notifications.EventNotification;
import iZomateCore.ServerCore.Notifications.ExceptionNotification;
import iZomateCore.ServerCore.Notifications.WindowNotification;
import iZomateCore.ServerCore.RPCServer.Chunks.BaseChunk;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Manages the active transactions which have been pulled of the input stream.
 */
public class TransactionList
{
	TransactionLog tlog = null;  //for debugging
	int listVersion = 0;
    Vector<BaseChunk> transactions;

	/**
	 * Manages the active transactions which have been pulled of the input stream.
	 */
	public TransactionList()
	{
		this.transactions = new Vector<BaseChunk>();
	}

    /**
     * Adds the transaction to the list
     * @param transaction the IZDatum to add to the transactions list
     */
    public void addTransaction(BaseChunk transaction)
    {
    	this.transactions.add(transaction);
    	this.listVersion++;
    }

    /**
     * Returns the version number of the transaction list.  The version number is incremented every time a new transaction is added.
     *
     * @return the list version number
     */
    protected int getListVersion()
    {
    	return this.listVersion;
    }

    /**
     * Removes the transaction from the transactions list at the given position
     * @param transPosition
     */
    private void removeTransaction(int transPosition)
    {
        this.dumpTransaction(transPosition, "Removing Transaction:\n");
	    this.transactions.remove(transPosition);
    }

    /**
     * Finds the most recent transaction of the same type as the transaction passed in and replaces it.
     * If none found then it will be added.
     * @param transaction
     */
    public void updateTransaction(BaseChunk transaction)
    {
    	boolean updated = false;

        for (int i = (this.transactions.size() - 1); i >= 0; i--) // start at the last transfer added to vector and work backward
        {
        	if (this.transactions.get(i).isCallbackHandled())
    			this.removeTransaction(i); //remove it if it has already been processed
        	else if (this.transactions.get(i).getChunkType().equals(transaction.getChunkType()))
    	    {
    	    	this.transactions.set(i, transaction); //overwrite existing element
    	    	updated = true;
    	    	break;
    	    }
        }

        if (!updated)
        	this.transactions.add(transaction);
    }

    /**
     * Returns the specified transfer datum and removes it from the list, or returns null if not found.
     * This method iterates the list backwards. Last in, first out.
     *
     * @param transUID the transaction UID of the desired RPLY transaction.
     * @return IZDatum The RPLY transaction if the transfer was found, null if it was not
     */
    public BaseChunk getReplyTransaction(long transUID)
    {
        for (int i = (this.transactions.size() - 1); i >= 0; i--) // start at the last transfer added to vector and work backward
        {
    	    if (this.transactions.get(i).getChunkType().equals(NotificationType.RPLY))
    	    {
    	        IncomingReply ret = (IncomingReply) this.transactions.get(i);
    			if (ret.getTransactionUID() == transUID)
    			{
    			    this.removeTransaction(i);
    			    return ret;
    			}
    	    }
        }
        return null;
    }

    /**
     * Returns a reference to the first(most recent) transaction in the list
     * @param type
     * @return Returns a reference to the first transaction in the list
     */
    public BaseChunk getFirstTransaction(NotificationType type)
    {
    	return this.getNextTransaction(type, null);
    }

    /**
     * Returns a reference to the next transaction in the transaction list of the specified NotificationType.
     * This method iterates the list backwards. Last in, first out.
     *
     * @param type the desired TransferType
     * @param currentDatum
     * @return the transfer datum, or null if specified datum type does not exist
     */
    public BaseChunk getNextTransaction(NotificationType type, BaseChunk currentDatum)
    {
    	BaseChunk nextTrans = null;
    	ArrayList<Integer> trash = new ArrayList<Integer>();
    	boolean foundCurrentDatum = (currentDatum==null)?true:false; //null means find the first transaction

        for (int i = (this.transactions.size() - 1); i >= 0; i--) // start at the last transfer added to vector and work backward
        {
    		BaseChunk d = this.transactions.get(i);

           	if (d.getChunkType().equals(type))
        	{
        		if (foundCurrentDatum && !d.isCallbackHandled())
        		{
        			nextTrans = d;
        			break;
        		}
        		else if (d == currentDatum) //compares memory addresses rather than contents
        			foundCurrentDatum = true; //The next datum we find is the one we want
    	    }

        	if (d.isCallbackHandled()) //house cleaning
        		trash.add(i); //we want to remove it if it has already been processed
    	}

        //Take out the trash!
        for (int i = 0; i < trash.size(); i++)
        	this.removeTransaction(trash.get(i));

		return nextTrans;
    }

    /**
     * Clears out the transaction list
     */
    public void emptyTransactionList()
    {
    	this.transactions.removeAllElements();
    }

    //-----------------------------------
	//       Debugging Methods
	//-----------------------------------

    /**
     * Turns on transaction logging using the supplied translog
     * @param translog The translog to log data to
     */
    public void setVerboseLog(TransactionLog translog)
    {
    	this.tlog = translog;
    }

    /**
     * For Debugging.  Dumps the contents of the transaction list
     */
    public void dumpTransactions()
    {
		if (this.tlog != null)
		    for (int i = (this.transactions.size() - 1); i >= 0; i--)
		        this.dumpTransaction(i, (i == (this.transactions.size() - 1)?"Transaction List Dump:\n":null));
    }

    /**
     * Dumps the contents of the transaction for the given index to the Transaction Log. For debugging.
     * @param i index of the transaction to dump
     */
	private void dumpTransaction(int i, String message)
    {
     	if (this.tlog == null)
    		return;

     	try
		{
	     	BaseChunk d = this.transactions.get(i);

	     	if (d != null && !d.getChunkType().equals(NotificationType.RPLY))
	     	{
		     	if (message != null)
		     		this.tlog._log(message);

		     	String status = (d.isCallbackInProgress() ? "INPROGRESS" : (d.isCallbackHandled() ? "HANDLED" : "NOTHANDLED"));

		     	if (d.getChunkType().equals(NotificationType.EVNT))
		    		this.tlog._log("   " + i + ". " + d.getChunkType().getID() + ", status:" + status + ", " + ((EventNotification)d).getEventType() + ", " + ((EventNotification)d).getEventMessage() + ", " + ((EventNotification)d).getEventIntValue() + "\n");
		    	else if (d.getChunkType().equals(NotificationType.WNDW))
		    		this.tlog._log("   " + i + ". " + d.getChunkType().getID() + ", status:" + status + ", " + ((WindowNotification)d).getWindowTitle() + ", " + ((WindowNotification)d).getWindowID() + ", " + ((WindowNotification)d).getWindowStatus() + "\n");
		    	else if (d.getChunkType().equals(NotificationType.DLOG))
		    		this.tlog._log("   " + i + ". " + d.getChunkType().getID() + ", status:" + status + ", " + ((DialogNotification)d).getDialogTitle() + ", " + ((DialogNotification)d).getDialogMessage() + ", " + ((DialogNotification)d).getDialogButtons().toString() + "\n");
		    	else if (d.getChunkType().equals(NotificationType.XCPT))
		    		this.tlog._log("   " + i + ". " + d.getChunkType().getID() + ", status:" + status + ", " + ((ExceptionNotification)d).getString() + "\n");
		    	else if (d.getChunkType().equals(NotificationType.STAT))
		    		this.tlog._log("   " + i + ". " + d.getChunkType().getID() + ", status:" + status + "\n");
	     	}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
    }

}

