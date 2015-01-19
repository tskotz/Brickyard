package iZomateCore.ServerCore.CoreEnums;

/**
 *	RPCImageTypes - the supported screen data image formats
 */
public enum RPCImageFormat
{
	/** JPEG */		JPG		("jpg"),
    /** Bitmap */	BMP		("bmp"),
    /** Bitmap */	WBMP	("wbmp"),
    /** Gif */	  	GIF		("gif"),
    /** PNG */		PNG		("png");

	private String value;

    /**
     * Private Constructor.
     *
     * @param value the supported java data image format name
     */
	private RPCImageFormat (String value)
	{
		this.value = value;
	}

	/**
     * Returns the enum corresponding to the passed in string
     *
     * @param key the string to convert to an enum
     * @return the enum
	 * @throws Exception
	 */
	public static RPCImageFormat getEnum(String key) throws Exception
	{
		for (RPCImageFormat val : RPCImageFormat.values())
		{
			if (val.value().equalsIgnoreCase(key))
				return val;
		}
		throw new Exception("Invalid ImageFormat: " + key);
	}

	/**
	 * Returns the value of the enum.
	 *
	 * @return the value of enum
	 */
	public String value()
	{
		return this.value;
	}
}