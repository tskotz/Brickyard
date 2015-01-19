package iZomateCore.ServerCore.CoreEnums;

public enum CBStatus 
{
    /**	Default state indicating the callback has not been called. */
    NOTHANDLED 		(0),
    /** Indicates that the callback has been called and is in progress. */
    INPROGRESS 		(1),
    /** Indicates that the callback has NOT been executed BUT marks current Notification for removal. */
    REMOVE 			(2),
    /** Indicates that the callback has been executed and marks the requests's event as processed. */
    HANDLED			(3);

    private final int mValue;

    /**
     * This constructor is private.
     */
    private CBStatus(int val)
    {
    	this.mValue = val;
    }

    /**
     * Returns enum representing the callback status.
     *
     * @param val the int val to convert into a CBStatus enum
     * @return the CBStatus enum, or throws Exception
     * @throws Exception if an invalid CBStatus is specified
     */
    public static CBStatus getEnum(int val) throws Exception
    {
    	for (CBStatus t: CBStatus.values())
    	{
    	    if (t.mValue == val)
    	        return t;
    	}

    	throw new Exception("Invalid CBStatus: " + val);
    }

    /**
     * Returns the int value of the CBStatus
     * @return the int value of the CBStatus
     */
    public final int getValue(int x)
    {
    	return this.mValue;
    }

}
