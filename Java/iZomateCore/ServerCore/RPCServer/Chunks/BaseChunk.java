package iZomateCore.ServerCore.RPCServer.Chunks;

import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.NotificationType;

import java.math.BigInteger;

/**
 * This is an abstract base class for data elements.
 */
public abstract class BaseChunk 
{
    protected 	String  		mName;						//The name of the data element, or null if the element has no name.
    private 	NotificationType 		mChunkID = null;
    private 	CBStatus 		mCBStatus = CBStatus.NOTHANDLED;
    protected	int				mChunkSize = -1;

    /**
     * Constructs a named data element.
     *
     * @param name The name of the element, or null.
     */
    protected BaseChunk(String name, NotificationType chunkType)
    {
        if ( name != null)
            this.mName = name;
        else
            this.mName = "";

        this.mChunkID = chunkType;
    }

    /**
     * @return the size of this chunk in bytes
     */
    public int getChunkSize()
    {
    	return this.mChunkSize;
    }

    /**
     * Returns the name of the data element. Returns empty string if name was not set.
     *
     * @return The name of the data element.
     */
    public String getChunkName()
    {
        return this.mName;
    }

    /**
     * Sets the name of the data element.
     *
     * @param name
     */
    public void setChunkName(String name)
    {
        this.mName = name;
    }

    /**
     * Sets the chunkType of this chunk.
     *
     * @param chunkType
     */
    public void setChunkType(NotificationType chunkType)
    {
		this.mChunkID = chunkType;
	}

	/**
	 * Returns the chunkType set for this chunk.
	 * @return the chunk's chunkType
	 */
	public NotificationType getChunkType()
	{
		return this.mChunkID;
	}

	/**
	 * Sets the CBStatus of this chunk.  Used for Notification chunks.
	 *
	 * @param stat
	 */
	public void setCBStatus(CBStatus stat)
	{
		this.mCBStatus = stat;
	}

	/**
	 * Returns true if the callback status is set to HANDLED.
	 *
	 * @return true if the callback status is HANDLED.
	 */
	public boolean isCallbackHandled()
	{
		return (this.mCBStatus == CBStatus.HANDLED);
	}

	/**
	 * Returns true if the callback status is set to INPROGRESS.
	 *
	 * @return true if the callback status is INPROGRESS.
	 */
	public boolean isCallbackInProgress()
	{
		return (this.mCBStatus == CBStatus.INPROGRESS);
	}

    /**
     * Returns the function name of the data element.
     * Throws an assertion if the data element is not a function.
     *
     * @return The function name of the data element.
     * @throws Exception
     */
    public String _getFunction() throws Exception
    {
    	throw new Exception("Unreachable code: " + "Not a function chunk.");
    }

    /**
     * Returns the value of a boolean data element. Throws an assertion if the data element is not a boolean.
     *
     * @return The value of the data element.
     * @throws Exception
     */
    public boolean getBoolean() throws Exception
    {
    	throw new Exception("Unreachable code: " + "Not a boolean chunk.");
    }

    /**
     * Returns the value of a 16 bit integer data element. Throws an assertion if the data element is not a 16 bit integer.
     *
     * @return The value of the data element.
     * @throws Exception
     */
    public short getInt16() throws Exception
    {
    	throw new Exception("Unreachable code: " + "Not a 16 bit integer chunk.");
    }

    /**
     * Returns the value of a "unsigned" 16 bit integer data element. Since Java does not support unsigned 16 bit integers, we
     * will be using a Java int to store the "unsigned 16 bit integer" value. This is necessary for communicating with
     * the C++ RPC server. Throws an assertion if the data element is not an int.
     *
     * @return The value of the data element.
     * @throws Exception
     */
    public int getUInt16() throws Exception
    {
    	throw new Exception("Unreachable code: " + "Not an unsigned 16 bit integer chunk.");
    }

    /**
     * Returns the value of a 32 bit integer data element. Throws an assertion if the data element is not an int32.
     *
     * @return The value of the data element.
     * @throws Exception
     */
    public int getInt32() throws Exception
    {
    	throw new Exception("Unreachable code: " + "Not an int32 chunk.");
    }

    /**
     * Returns the value of a 32 bit unsigned integer data element. Throws an assertion if the data element is not a Uint32.
     *
     * @return The value of the data element.
     * @throws Exception
     */
    public long getUInt32() throws Exception
    {
    	throw new Exception("Unreachable code: " + "Not an uint32 chunk.");
    }

    /**
     * Returns the value of a 64 bit integer data element. Throws an assertion if the data element is not an int64.
     *
     * @return The value of the data element.
     * @throws Exception
     */
    public long getInt64() throws Exception
    {
    	throw new Exception("Unreachable code: " + "Not an int64 chunk.");
    }

    /**
     * Returns the value of a 64 bit unsigned integer data element. Throws an assertion if the data element is not an uint64.
     *
     * @return The value of the data element.
     * @throws Exception
     */
    public BigInteger getUInt64() throws Exception
    {
    	throw new Exception("Unreachable code: " + "Not an uint64 chunk.");
    }

    /**
     * Returns the value of a float data element. Throws an assertion if the data element is not a float.
     *
     * @return The value of the data element.
     * @throws Exception
     */
    public float getFloat() throws Exception
    {
    	throw new Exception("Unreachable code: " + "Not a float chunk.");
    }

    /**
     * Returns the value of a double data element. Throws an assertion if the data element is not a double.
     *
     * @return The value of the data element.
     * @throws Exception
     */
    public double getDouble() throws Exception
    {
    	throw new Exception("Unreachable code: " + "Not a double chunk.");
    }

    /**
     * Returns the byte[] buffer of a Buffer data element.
     *
     * @return The value of the data element.
     * @throws Exception
     */
    public byte[] getBuffer() throws Exception
    {
    	throw new Exception("Unreachable code: " + "Not a rawValue chunk.");
    }

    /**
     * Returns the value of a string data element. Throws an assertion if the data element is not a string.
     *
     * @return The value of the data element.
     * @throws Exception
     */
    public String getString() throws Exception
    {
    	throw new Exception("Unreachable code: " + "Not a string chunk.");
    }

    /**
     * Returns the value of a compound data element. Throws an assertion if the data element is not compound.
     *
     * @return The value of the data element.
     * @throws Exception
     */
    public CompoundChunk getCompound() throws Exception
    {
    	throw new Exception("Unreachable code: " + "Not a compound chunk.");
    }

    /**
     * Converts the value of the data element to a string.
     *
     * @return the value of the data element as a string.
     * @throws Exception
     */
	public String valueToString() throws Exception
    {
    	throw new Exception("Unreachable code: " + "Need to provide override.");
    }

    //-----------------------------------------------------
    //  Abstract Functions
    //-----------------------------------------------------

    /**
     * Converts the chunk into a properly formed byte array that is ready
     * to be added to a RQST or RPLY transaction.<br>
     *
     * See RPC Stream Transfer Specification doc for complete protocol details:
     * <A HREF="iZomote/doc/ServerCore/RPCStreamTransferSpecification.docx">RPCStreamTransferSpecification.docx</A>
     *
     * @param swapBytes specifies whether data needs to be byte swapped.<br>
     *               Set to true if sending to RPC on Windows.<br>
     *               Set to false if sending to Remote Launcher or RPC on OSX
     * @return byte []
     */
    public abstract byte[] toByteArray(boolean swapBytes);

    /**
     * Converts the chunk to a string representation.
     *
     * @return the chunk as a string.
     */
    @Override
	public abstract String toString();

    //-----------------------------------------------------
    //  Helper conversion routines
    //-----------------------------------------------------

	/**
     * Copies the 8 individual bytes of a double into a byte[8] array.
     * @param d the double value
     * @param swapBytes true will swap the order of the bytes, false will not swap them.
     * @return the byte[8] representation of d
     */
    static public byte[] convertDbleToByte8 (double d, boolean swapBytes)
    {
        return convertLongToByte8(Double.doubleToRawLongBits(d), swapBytes);
    }

    /**
     * Copies the 8 individual bytes of a long into a byte[8] array.
     * Performs bitwise & to isolate the byte we want and then bitshift it down to first byte
     * @param l
     * @param swapBytes true will swap the order of the bytes, false will not swap them.(big v. little endian)
     * @return the byte[8] array
     */
    static public byte[] convertLongToByte8 (long l, boolean swapBytes)
    {
        byte[] buf = new byte[8];

        if ( swapBytes )
        {
            buf[0] = (byte)((l & 0x000000FF) >> 0);
            buf[1] = (byte)((l & 0x0000FF00) >> 8);
            buf[2] = (byte)((l & 0x00FF0000) >> 16);
            buf[3] = (byte)((l & 0xFF000000) >> 24);
            l >>>= 32;  //because & only works with ints, we need to shift the last 4 bytes down
            buf[4] = (byte)((l & 0x000000FF) >> 0);
            buf[5] = (byte)((l & 0x0000FF00) >> 8);
            buf[6] = (byte)((l & 0x00FF0000) >> 16);
            buf[7] = (byte)((l & 0xFF000000) >> 24);
        }
        else
        {
            buf[7] = (byte)((l & 0x000000FF) >> 0);
            buf[6] = (byte)((l & 0x0000FF00) >> 8);
            buf[5] = (byte)((l & 0x00FF0000) >> 16);
            buf[4] = (byte)((l & 0xFF000000) >> 24);
            l >>>= 32;  //because & only works with ints, we need to shift the last 4 bytes down
            buf[3] = (byte)((l & 0x000000FF) >> 0);
            buf[2] = (byte)((l & 0x0000FF00) >> 8);
            buf[1] = (byte)((l & 0x00FF0000) >> 16);
            buf[0] = (byte)((l & 0xFF000000) >> 24);
        }

       return buf;
    }

    /**
     * Copies the 4 LSB bytes of an 8 byte long into a byte[4] array.
     * Perform bitwise & to isolate the byte we want and then bitshift it down to first byte
     * @param l
     * @param swapBytes true will swap the order of the bytes, false will not swap them.
     * @return the byte[4] array
     */
    static public byte[] convertLongToByte4 (long l, boolean swapBytes)
    {
        byte[] buf = new byte[4];

        if ( swapBytes )
        {
            buf[0] = (byte)((l & 0x000000FF) >> 0);
            buf[1] = (byte)((l & 0x0000FF00) >> 8);
            buf[2] = (byte)((l & 0x00FF0000) >> 16);
            buf[3] = (byte)((l & 0xFF000000) >> 24);
        }
        else
        {
            buf[3] = (byte)((l & 0x000000FF) >> 0);
            buf[2] = (byte)((l & 0x0000FF00) >> 8);
            buf[1] = (byte)((l & 0x00FF0000) >> 16);
            buf[0] = (byte)((l & 0xFF000000) >> 24);
        }

       return buf;
    }

    /**
     * Copies the 4 individual bytes of an int into a byte[4] array.
     * Performs bitwise & to isolate the byte we want and then bitshift it down to first byte
     * @param d the int value to convert to byte [4]
     * @param swapBytes true will swap the order of the bytes, false will not swap them.
     * @return byte [4] representation of d
     */
    static public byte[] convertIntToByte4 (int d, boolean swapBytes)
    {
        byte[] buf = new byte[4];

        if ( swapBytes )
        {
            buf[0] = (byte)((d & 0x000000FF) >> 0);
            buf[1] = (byte)((d & 0x0000FF00) >> 8);
            buf[2] = (byte)((d & 0x00FF0000) >> 16);
            buf[3] = (byte)((d & 0xFF000000) >> 24);
        }
        else
        {
            buf[3] = (byte)((d & 0x000000FF) >> 0);
            buf[2] = (byte)((d & 0x0000FF00) >> 8);
            buf[1] = (byte)((d & 0x00FF0000) >> 16);
            buf[0] = (byte)((d & 0xFF000000) >> 24);
        }

       return buf;
    }

    /**
     * Copies the 4 individual bytes of a float into a byte[4] array.
     * Performs bitwise & to isolate the byte we want and then bitshift it down to first byte
     * @param f the float value to convert to byte [4]
     * @param swapBytes true will swap the order of the bytes, false will not swap them.
     * @return byte [4] representation of f
     */
    static public byte[] convertFloatToByte4 (float f, boolean swapBytes)
    {
       return convertIntToByte4(Float.floatToRawIntBits(f), swapBytes);
    }

    /**
     * Copies the 2 individual bytes of a short into a byte[2] array.
     * Performs bitwise & to isolate the byte we want and then bitshift it down to first byte
     * @param d the short value as int to convert to byte [2]
     * @param swapBytes true will swap the order of the bytes, false will not swap them.
     * @return byte [2] representation of d
     */
    static public byte[] convertShortToByte2 (int d, boolean swapBytes)
    {
        byte[] buf = new byte[2];
        if ( swapBytes )
        {
            buf[0] = (byte)((d & 0x00FF) >> 0);
            buf[1] = (byte)((d & 0xFF00) >> 8);
        }
        else
        {
            buf[1] = (byte)((d & 0x00FF) >> 0);
            buf[0] = (byte)((d & 0xFF00) >> 8);
        }
        return buf;
    }

    /**
     * Copies the LSB byte (byte[0] of an int into a byte[1] array.
     * Performs bitwise & to isolate the byte we want and then bitshift it down to first byte
     * @param d
     * @return the byte[] array
     */
    static public byte[] convertIntToByte1 (int d)
    {
        byte[] buf = new byte[1];
        buf[0] = (byte)((d & 0x000000FF) >> 0);

       return buf;
    }

    /**
     * Converts a byte [8] array into a Long.
     * Converts a byte to a long, bit shifts it to correct location and then or's it with l
     * @param b the byte [] holding the data to convert
     * @param offset the starting byte in the array
     * @param swapBytes true will swap the order of the bytes, false will not swap them.
     * @return long value
     */
    static public long convertByte8ToLong(byte [] b, int offset, boolean swapBytes)
    {
        long l = 0;

        if ( swapBytes )
        {
            l |= ( (long)( b[offset++] & 0xff ) ) << 0;
            l |= ( (long)( b[offset++] & 0xff ) ) << 8;
            l |= ( (long)( b[offset++] & 0xff ) ) << 16;
            l |= ( (long)( b[offset++] & 0xff ) ) << 24;
            l |= ( (long)( b[offset++] & 0xff ) ) << 32;
            l |= ( (long)( b[offset++] & 0xff ) ) << 40;
            l |= ( (long)( b[offset++] & 0xff ) ) << 48;
            l |= ( (long)( b[offset]   & 0xff ) ) << 56;
        }
        else
        {
            l |= ( (long)( b[offset++] & 0xff ) ) << 56;
            l |= ( (long)( b[offset++] & 0xff ) ) << 48;
            l |= ( (long)( b[offset++] & 0xff ) ) << 40;
            l |= ( (long)( b[offset++] & 0xff ) ) << 32;
            l |= ( (long)( b[offset++] & 0xff ) ) << 24;
            l |= ( (long)( b[offset++] & 0xff ) ) << 16;
            l |= ( (long)( b[offset++] & 0xff ) ) << 8;
            l |= ( (long)( b[offset]   & 0xff ) ) << 0;
        }

        return l;
    }

    /**
     * Converts a byte [4] array into a Long.
     * Converts a byte to a long, bit shifts it to correct location and then or's it with l
     * @param b the byte [] holding the data to convert
     * @param offset the starting byte in the array
     * @param swapBytes true will swap the order of the bytes, false will not swap them.
     * @return long value
     */
    static public long convertByte4ToLong(byte [] b, int offset, boolean swapBytes)
    {
        long l = 0;

        if ( swapBytes )
        {
            l |= ( (long)( b[offset++] & 0xff ) ) << 0;
            l |= ( (long)( b[offset++] & 0xff ) ) << 8;
            l |= ( (long)( b[offset++] & 0xff ) ) << 16;
            l |= ( (long)( b[offset]   & 0xff ) ) << 24;
        }
        else
        {
            l |= ( (long)( b[offset++] & 0xff ) ) << 24;
            l |= ( (long)( b[offset++] & 0xff ) ) << 16;
            l |= ( (long)( b[offset++] & 0xff ) ) << 8;
            l |= ( (long)( b[offset]   & 0xff ) ) << 0;
        }

        return l;
    }


    /**
     * Converts a byte [4] array into an int.
     * Converts a byte to a int, bit shifts it to correct location and then or's it with i
     * @param b the byte [] holding the data to convert
     * @param offset the starting byte in the array
     * @param swapBytes true will swap the order of the bytes, false will not swap them.
     * @return int value
     */
    static public int convertByte4ToInt(byte [] b, int offset, boolean swapBytes)
    {
        int i = 0;

        if ( swapBytes )
        {
            i |= ( b[offset++] & 0xff ) << 0;
            i |= ( b[offset++] & 0xff ) << 8;
            i |= ( b[offset++] & 0xff ) << 16;
            i |= ( b[offset]   & 0xff ) << 24;
        }
        else
        {
            i |= ( b[offset++] & 0xff ) << 24;
            i |= ( b[offset++] & 0xff ) << 16;
            i |= ( b[offset++] & 0xff ) << 8;
            i |= ( b[offset]   & 0xff ) << 0;
       }

        return i;
    }

    /**
     * Converts a byte [2] array into a short.
     * Converts a byte to a short, bit shifts it to correct location and then or's it with s
     * @param b the byte [] holding the data to convert
     * @param offset the starting byte in the array
     * @param swapBytes true will swap the order of the bytes, false will not swap them.
     * @return short value
     */
    static public short convertByte2ToShort(byte [] b, int offset, boolean swapBytes)
    {
        short s = 0;

        if ( swapBytes )
        {
            s |= ( (short)( b[offset++] & 0xff ) ) << 0;
            s |= ( (short)( b[offset]   & 0xff ) ) << 8;
        }
        else
        {
            s |= ( (short)( b[offset++] & 0xff ) ) << 8;
            s |= ( (short)( b[offset]   & 0xff ) ) << 0;
        }

        return s;
    }

    /**
     * Converts a byte [2] array into a char.
     * Converts a byte to a char, bit shifts it to correct location and then or's it with s
     * @param b the byte [] holding the data to convert
     * @param offset the starting byte in the array
     * @param swapBytes true will swap the order of the bytes, false will not swap them.
     * @return char value
     */
    static public char convertByte2ToChar(byte [] b, int offset, boolean swapBytes)
    {
        char s = 0;

        if ( swapBytes )
        {
            s |= ( (char)( b[offset++] & 0xff ) ) << 0;
            s |= ( (char)( b[offset]   & 0xff ) ) << 8;
        }
        else
        {
            s |= ( (char)( b[offset++] & 0xff ) ) << 8;
            s |= ( (char)( b[offset]   & 0xff ) ) << 0;
        }

        return s;
    }

}
