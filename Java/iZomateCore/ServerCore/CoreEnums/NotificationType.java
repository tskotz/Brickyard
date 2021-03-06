package iZomateCore.ServerCore.CoreEnums;


/**
 * This class specifies the types of RPC stream datums.  For an overview of the stream notification
 * protocol, please see the specification at: iZomote.doc.ServerCore.RPCStreamTransferSpecification.docx
 */
public enum NotificationType
{
	// Datum types as defined in iZomote.doc.ServerCore.RPCStreamTransferSpecification.docx
	/** Request notification. */
	RQST	("RQST"),
	/** Reply notification.  The answer to a RQST. */
	RPLY	("RPLY"),
	/** Window notification.  Generated by app when windows change state. */
	WNDW	("WNDW"),
	/** Dialog notification.  Generated by app when dialogs are created. */
	DLOG	("DLOG"),
	/** Event notification.  Generated by app when specific events. */
	EVNT	("EVNT"),
	/** Status notification.  Generated by app when Progress Bars are displayed and updated. */
	STAT	("STAT"),
	/** Exception notifications.  Generated by RPC dll/dylib if errors occur in automation hooks. */
	XCPT	("XCPT"),
	/** String value datum. */
	Stri	("Stri"),
	/** Boolean value datum. */
	Bool	("Bool"),
	/** 16 bit Integer value datum. */
	In16	("In16"),
	/** Unsigned 16 Integer bit value datum. */
	UI16	("UI16"),
	/** 32 bit Integer value datum. */
	In32	("In32"),
	/** 32 bit Unsigned Integer value datum. */
	UI32	("UI32"),
	/** 64 bit Integer value datum. */
	In64	("In64"),
	/** 64 bit Unsigned Integer value datum. */
	UI64	("UI64"),
	/** Float value datum. */
	Flot	("Flot"),
	/** Double value datum. */
	Dble	("Dble"),
	/** Buffer Data datum. */
	Buff	("Buff"),
	/** Compound datum.  A datum which contains other datums. */
	Cmpd	("Cmpd"),
	/** Function datum.  Contains the name of the function to call. */
	Func	("Func");

	private final String     mID;

    /**
     * Constructor
     * 
     * @param id the notification type ID
     */
	NotificationType(String id)
    {
        this.mID = id;
    }

    /**
     * Returns the enum corresponding to the passed in string
     *
     * @param key the string to convert to an enum
     * @return the enum
     * @throws Exception
     */
    public static NotificationType getEnum(String key)
        throws Exception
    {
    	for (NotificationType val : NotificationType.values())
		{
			if (val.mID.equalsIgnoreCase(key))
				return val;
		}
		throw new Exception("Invalid NotificationType: " + key);
    }

    /**
     * Returns the ID for the notification type
     *
     * @return the ID for the notification type
     */
    public String getID()
    {
        return this.mID;
    }
}