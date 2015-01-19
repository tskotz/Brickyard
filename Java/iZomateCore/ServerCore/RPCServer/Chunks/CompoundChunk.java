package iZomateCore.ServerCore.RPCServer.Chunks;

import iZomateCore.ServerCore.CoreEnums.NotificationType;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Vector;

/**
 * This class defines an element of incoming data which contains other data elements. It is used to transmit
 * arrays and structures, and also forms the base class for an entire incoming response to an RPC Server request. The
 * elements contained within a <code>CompoundDatum</code> may be named or unnamed.  By convention, the
 * order of the data elements is not considered significant if the data elements are named.
 */
public class CompoundChunk extends BaseChunk
{
    protected String            mTypeName;
    private Vector<BaseChunk>    	mContents;

    /**
     * Constructs an empty compound datum.
     *
     * @param type the type of the data element, or null.
     */
    public CompoundChunk(String type)
    {
        super("", NotificationType.Cmpd);
        this.mTypeName = type;
        this.mContents = new Vector<BaseChunk>();
        this.setChunkType(NotificationType.Cmpd);
    }

    /**
     * Constructs a compound data element from a byte array.
     *
     * @param name the name of the data element, or null.
     * @param type the type of the data element, or null.
     * @param b the byte array containing the data
     * @param offset the start position of the data in b
     * @param length the length of the data
     * @param swapBytes whether or not byte swap is necessary
     * @throws Exception
     */
    public CompoundChunk(String name, String type, byte [] b, int offset, long length, boolean swapBytes) throws Exception
    {
        super(name, NotificationType.Cmpd);
        this.mTypeName = type;
        this.mContents = new Vector <BaseChunk>();
        this.setDatums(b, offset, length, swapBytes);
    }

    /**
     * Creates a compound datum from a byte[] based on the RPC Stream Transfer Specification.
     *
     * @param b buffer containing notification data
     * @param offset starting byte in buffer
     * @param swapBytes true if the endianess of data types does not match this platform
     * @throws Exception
     */
    public CompoundChunk(byte[] b, int offset, boolean swapBytes) throws Exception
    {
        super("", NotificationType.Cmpd);
        this.mContents = new Vector <BaseChunk>();

        String IZDatumType = new String(b, offset, 4);
        this.mChunkSize = BaseChunk.convertByte4ToInt(b, offset + 4, swapBytes);

        if (!IZDatumType.equals(this.getChunkType().name()))
    		throw new Exception("Expected Datum type: " + this.getChunkType() + ", but detected: " + IZDatumType);

    	if (this.mChunkSize < 9)
    		throw new Exception("Buffer to small to be a CompoundDatum");

        byte nameSize = b[offset+8];
        this.mName = new String(b, offset+9, nameSize, Charset.forName("UTF-8"));
        byte typeSize = b[offset+9+nameSize];
        this.mTypeName = new String(b, offset+10+nameSize, typeSize);
        this.setDatums(b, offset+10+nameSize+typeSize, this.mChunkSize-(10+nameSize+typeSize), swapBytes);
    }

    /**
     * Sets the compound datum's type to the given type name
     * @param typeName the name to set for the type of compound datum
     */
    protected void setTypeName(String typeName)
    {
    	this.mTypeName = typeName;
    }

    /**
     * Creates the individual datums from the data in byte [] b
     *
     * @param b the byte array containing the data
     * @param offset the start position of the data in b (int because the new String complains when it is a long!!!)
     * @param length the length of the data
     * @param swapBytes whether or not byte swap is necessary
     * @throws Exception
     */
    protected void setDatums (byte [] b, int offset, long length, boolean swapBytes) throws Exception
    {
        BaseChunk d = null;
        String datumID;
        long end = offset + length;

        while (offset < end)
        {
            datumID = new String(b, offset, 4);

            if (datumID.equals("Stri"))
	            d = new StringChunk(b, offset, swapBytes);
            else if (datumID.equals("Bool"))
	            d = new BooleanChunk(b, offset, swapBytes);
            else if (datumID.equals("In16"))
	            d = new Int16Chunk(b, offset, swapBytes);
            else if (datumID.equals("UI16"))
	            d = new UInt16Chunk(b, offset, swapBytes);
            else if (datumID.equals("In32"))
	            d = new Int32Chunk(b, offset, swapBytes);
            else if (datumID.equals("UI32"))
	            d = new UInt32Chunk(b, offset, swapBytes);
            else if (datumID.equals("In64"))
	            d = new Int64Chunk(b, offset, swapBytes);
            else if (datumID.equals("UI64"))
	            d = new UInt64Chunk(b, offset, swapBytes);
            else if (datumID.equals("Flot"))
	            d = new FloatChunk(b, offset, swapBytes);
            else if (datumID.equals("Dble"))
	            d = new DoubleChunk(b, offset, swapBytes);
             else if (datumID.equals("Buff"))
	            d = new BufferChunk(b, offset, swapBytes);
            else if (datumID.equals("Cmpd"))
	            d = new CompoundChunk(b, offset, swapBytes);
            else if (datumID.equals("Func"))
	            d = new FunctionChunk(b, offset, swapBytes);
            else
                throw new Exception("Unknown datum type: '" + datumID + "'.");

            offset += d.getChunkSize();

            this.addDatum(d);
        }
    }

    /**
     * Gets this compound datum.
     *
     * @return this CompoundDatum.
     */
    @Override
	public CompoundChunk getCompound()
    {
        return this;
    }

    /**
     * Returns the type name of the compound data element.
     *
     * @return the type name, or null.
     */
    public String _getTypeName()
    {
        return this.mTypeName;
    }

    /**
     * Returns the number of elements contained in this data element. This is not a recursive count; for
     * example, if data element X contains elements A, B and C, and element B is a compound data element
     * containing 4 elements, <code>X.getCount</code> will return 3, not 6.
     *
     * @return the number of contained elements.
     */
    public int _getCount()
    {
        return this.mContents.size();
    }

    /**
     * Returns the nth contained data element.
     *
     * @param position identifies which contained element to return.
     * @return the contained data element at the given position or null.
     */
    public BaseChunk _getNth(int position)
    {
       if ( position >= 0 && position < this.mContents.size() )
            return this.mContents.get(position);
        return null;
    }

    /**
     * Returns the contained data element with the given name.
     *
     * @param datumName identifies which contained element to return.
     * @return the contained data element with the given name or null.
     */
    public BaseChunk _findByName(String datumName)
    {
        if ( datumName != null && !datumName.equals("") )
        {
            for(int n = 0; n < this.mContents.size(); n++)
            {
                BaseChunk d = this.mContents.get(n);
                if(d.getChunkName().equals(datumName))
                    return d;
            }
        }
        return null;
    }

    /**
     * Returns the name of the nth contained data element.
     * If the element does not exist, an exception is thrown.
     *
     * @param position identifies which contained element to return the name of.
     * @return the name of the contained data element at the given position, or null if it has no name.
     */
    public String _getName(int position)
    {
        return this._getNth(position).getChunkName();
    }

    /**
     * Returns a contained element by name or position.
     * If the element does not exist, an exception is thrown.
     *
     * @param paramName the name of the data element, or null.
     * @return the contained data element.
     */
    public BaseChunk _getDatum(String paramName)
    {
        BaseChunk d = this._findByName(paramName);

        if ( d == null)
            throw new Error("element \"" + paramName + "\" not found in message: " + this.toString());

        return d;
    }

    /**
     * Returns a contained element by position.
     * If the element does not exist, an exception is thrown.
     *
     * @param position identified which contained element to return the value of.
     * @return the contained data element.
     */
    public BaseChunk _getDatum(int position)
    {
        return this._getNth(position);
    }

    /**
     * Returns whether a contained element, defined by name, exists.
     *
     * @param paramName the name of the data element, or null.
     * @return the contained data element.
     */
    public boolean _exists(String paramName)
    {
        return this._findByName(paramName) != null ? true : false;
    }

    /**
     * Returns the function name of the request.
     *
     * @return the string value of the request's function.
     * @throws Exception
     */
	@Override
	public String _getFunction() throws Exception
    {
        return this._getNth(0)._getFunction(); //function is always the 0 datum
    }

    /**
     * Returns the boolean value of the nth contained element.
     * Fails an assertion if the contained element is not a boolean.
     *
     * @param position identified which contained element to return the value of.
     * @return the boolean value of the contained element.
     * @throws Exception
     */
    public boolean _getBoolean(int position) throws Exception
    {
        return this._getNth(position).getBoolean();
    }

    /**
     * Returns the 16 bit integer value of the nth contained element.
     * Fails an assertion if the contained element is not a 16 bit integer.
     *
     * @param position identified which contained element to return the value of.
     * @return the 16 bit integer value of the contained element.
     * @throws Exception
     */
    public short _getInt16(int position) throws Exception
    {
        return this._getNth(position).getInt16();
    }

    /**
     * Returns the C++ unsigned 16 bit integer value (as int) of a contained element.
     * Since java does not support unsigned types, the C++ unsigned 16 bit integer
     * value must be placed into a java type that can hold the max
     * C++ unsigned 16 bit integer value which in this case is an unsigned 16 bit integer.
     *
     * @param position identified which contained element to return the value of.
     * @return the unsigned 16 bit integer value of the contained element.
     * @throws Exception
     */
    public int _getUInt16(int position) throws Exception
    {
        return this._getNth(position).getUInt16();
    }

    /**
     * Returns the 32 bit integer value of the nth contained element.
     * Fails an assertion if the contained element is not an integer.
     *
     * @param position identified which contained element to return the value of.
     * @return the 32 bit integer value of the contained element.
     * @throws Exception
     */
    public int _getInt32(int position) throws Exception
    {
        return this._getNth(position).getInt32();
    }

    /**
     * Returns the 32 bit unsigned integer value of the nth contained element.
     * Fails an assertion if the contained element is not an integer.
     *
     * @param position identified which contained element to return the value of.
     * @return the 32 bit unsigned integer value of the contained element.
     * @throws Exception
     */
    public long _getUInt32(int position) throws Exception
    {
        return this._getNth(position).getUInt32();
    }

    /**
     * Returns the 64 bit integer value of the nth contained element.
     * Fails an assertion if the contained element is not an integer.
     *
     * @param position identified which contained element to return the value of.
     * @return the 64 bit integer value of the contained element.
     * @throws Exception
     */
    public long _getInt64(int position) throws Exception
    {
        return this._getNth(position).getInt64();
    }

    /**
     * Returns the 64 bit unsigned integer value of the nth contained element.
     * Fails an assertion if the contained element is not an integer.
     *
     * @param position identified which contained element to return the value of.
     * @return the 64 bit unsigned integer value of the contained element.
     * @throws Exception
     */
    public BigInteger _getUInt64(int position) throws Exception
    {
        return this._getNth(position).getUInt64();
    }

    /**
     * Returns the float value of the nth contained element.
     * Fails an assertion if the contained element is not a float.
     *
     * @param position identified which contained element to return the value of.
     * @return the float value of the contained element.
     * @throws Exception
     */
    public float _getFloat(int position) throws Exception
    {
        return this._getNth(position).getFloat();
    }

    /**
     * Returns the double value of the nth contained element.
     * Fails an assertion if the contained element is not a double.
     *
     * @param position identified which contained element to return the value of.
     * @return the double value of the contained element.
     * @throws Exception
     */
    public double _getDouble(int position) throws Exception
    {
        return this._getNth(position).getDouble();
    }

    /**
     * Returns the buffer data (byte[]) of a contained element.
     * Fails an assertion if the contained element is not a Buffer.
     *
     * @param position identified which contained element to return the value of.
     * @return the buffer data (byte[]) value of the contained element.
     * @throws Exception
     */
     public byte[] _getBuffer(int position) throws Exception
    {
        return this._getNth(position).getBuffer();
    }

    /**
     * Returns the string value of the nth contained element.
     * Fails an assertion if the contained element is not a string.
     *
     * @param position identified which contained element to return the value of.
     * @return the string value of the contained element.
     * @throws Exception
     */
    public String _getString(int position) throws Exception
    {
        return this._getNth(position).getString();
    }

    /**
     * Returns the boolean value of a contained element.
     * Fails an assertion if the contained element is not a boolean.
     *
     * @param paramName the name of the desired element.
     * @return the boolean value of the contained element.
     * @throws Exception
     */
    public boolean _getBoolean(String paramName) throws Exception
    {
        return this._getDatum(paramName).getBoolean();
    }

    /**
     * Returns the 32 bit integer value of a contained element.
     * Fails an assertion if the contained element is not an integer.
     *
     * @param paramName the name of the desired element.
     * @return the 32 bit integer value of the contained element.
     * @throws Exception
     */
    public int _getInt32(String paramName) throws Exception
    {
        return this._getDatum(paramName).getInt32();
    }

    /**
     * Returns the 32 bit unsigned integer value of a contained element.
     * Fails an assertion if the contained element is not an integer.
     *
     * @param paramName the name of the desired element.
     * @return the 32 bit unsigned integer value of the contained element.
     * @throws Exception
     */
    public long _getUInt32(String paramName) throws Exception
    {
        return this._getDatum(paramName).getUInt32();
    }

    /**
     * Returns the 64 bit integer value of a contained element.
     * Fails an assertion if the contained element is not an integer.
     *
     * @param paramName the name of the desired element.
     * @return the 64 bit integer value of the contained element.
     * @throws Exception
     */
    public long _getInt64(String paramName) throws Exception
    {
        return this._getDatum(paramName).getInt64();
    }

    /**
     * Returns the 64 bit unsigned integer value of a contained element.
     * Fails an assertion if the contained element is not an integer.
     *
     * @param paramName the name of the desired element.
     * @return the 64 bit unsigned integer value of the contained element.
     * @throws Exception
     */
    public BigInteger _getUInt64(String paramName) throws Exception
    {
        return this._getDatum(paramName).getUInt64();
    }

    /**
     * Returns the 16 bit integer value of a contained element.
     * Fails an assertion if the contained element is not a 16 bit integer.
     *
     * @param paramName the name of the desired element.
     * @return the 16 bit integer value of the contained element.
     * @throws Exception
     */
    public short _getInt16(String paramName) throws Exception
    {
        return this._getDatum(paramName).getInt16();
    }

    /**
     * Returns the C++ unsigned 16 bit integer value as int of a contained element.
     * Since java does not support unsigned types, the C++ unsigned 16 bit integer
     * value must be placed into a java type that can hold the max
     * C++ unsigned 16 bit integer value which in this case is an int.
     * Fails an assertion if the contained element is not a UI16.
     *
     * @param paramName the name of the desired element.
     * @return the unsigned 16 bit integer value as int of the contained element.
     * @throws Exception
     */
   public int _getUInt16(String paramName) throws Exception
    {
        return this._getDatum(paramName).getUInt16();
    }

    /**
     * Returns the float value of a contained element.
     * Fails an assertion if the contained element is not a float.
     *
     * @param paramName the name of the desired element.
     * @return the float value of the contained element.
     * @throws Exception
     */
    public float _getFloat(String paramName) throws Exception
    {
        return this._getDatum(paramName).getFloat();
    }

    /**
     * Returns the double value of a contained element.
     * Fails an assertion if the contained element is not a double.
     *
     * @param paramName the name of the desired element.
     * @return the double value of the contained element.
     * @throws Exception
     */
    public double _getDouble(String paramName) throws Exception
    {
        return this._getDatum(paramName).getDouble();
    }

    /**
     * Returns the RawValue (byte[]) value of a contained element.
     * Fails an assertion if the contained element is not a rawValue.
     *
     * @param paramName the name of the desired element.
     * @return the RawValue (byte[]) value of the contained element.
     * @throws Exception
     */
    public byte[] _getBuffer(String paramName) throws Exception
    {
        return this._getDatum(paramName).getBuffer();
    }

    /**
     * Returns the string value of a contained element.
     * Fails an assertion if the contained element is not a string.
     *
     * @param paramName the name of the desired element.
     * @return the string value of the contained element.
     * @throws Exception
     */
    public String _getString(String paramName) throws Exception
    {
        return this._getDatum(paramName).getString();
    }

    /**
     * Appends a function data element.
     *
     * @param function name of the function to lookup
     */
    public void addFunction(String function)
    {
        BaseChunk d = new FunctionChunk(function);
        this.addDatum(d);
    }

    /**
     * Appends a boolean data element.
     *
     * @param value the value of the data element.
     * @param paramName the name of the data element, or null.
     */
    public void _addBoolean(boolean value, String paramName)
    {
        BaseChunk d = new BooleanChunk(paramName, value);
        this.addDatum(d);
    }

    /**
     * Appends a 16 bit integer data element.
     *
     * @param value the value of the data element.
     * @param paramName the name of the data element, or null.
  * @throws Exception 
     */
     public void _addInt16(short value, String paramName)
     {
         BaseChunk d = new Int16Chunk(paramName, value);
         this.addDatum(d);
     }

     /**
      * Note:  Java does not support unsigned shorts, so we are using int to store the value
      * @param value the value of the data element.
      * @param paramName the name of the data element, null.
      * @throws Exception
      */
     public void _addUInt16(int value, String paramName) throws Exception
     {
         BaseChunk d = new UInt16Chunk(paramName, value);
         this.addDatum(d);
     }

    /**
     * Appends a 32 bit integer data element.
     *
     * @param value the value of the data element.
     * @param paramName the name of the data element, or null.
     * @throws Exception 
     */
    public void _addInt32(int value, String paramName)
    {
        BaseChunk d = new Int32Chunk(paramName, value);
        this.addDatum(d);
    }

    /**
     * Appends a 32 bit unsigned integer data element.
     *
     * @param value the value of the data element.
     * @param paramName the name of the data element, or null.
     * @throws Exception 
     */
    public void _addUInt32(long value, String paramName) throws Exception
    {
        BaseChunk d = new UInt32Chunk(paramName, value);
        this.addDatum(d);
    }

    /**
     * Appends a 64 bit integer data element.
     *
     * @param value the value of the data element.
     * @param paramName the name of the data element, or null.
     */
    public void _addInt64(long value, String paramName)
    {
        BaseChunk d = new Int64Chunk(paramName, value);
        this.addDatum(d);
    }

    /**
     * Appends a 64 bit unsigned integer data element.
     *
     * @param value the value of the data element.
     * @param paramName the name of the data element, or null.
     * @throws Exception 
     */
    public void _addUInt64(BigInteger value, String paramName) throws Exception
    {
        BaseChunk d = new UInt64Chunk(paramName, value);
        this.addDatum(d);
    }

    /**
     * Appends a float data element.
     *
     * @param value the value of the data element.
     * @param paramName the name of the data element, null.
     */
    public void _addFloat(float value, String paramName)
    {
        BaseChunk d = new FloatChunk(paramName, value);
        this.addDatum(d);
    }

    /**
     * Appends a double data element.
     *
     * @param value the value of the data element.
     * @param paramName the name of the data element, null.
     */
    public void _addDouble(double value, String paramName)
    {
        BaseChunk d = new DoubleChunk(paramName, value);
        this.addDatum(d);
    }

    /**
     * Appends a RawValue data element.
     *
     * @param value the value of the data element.
     * @param paramName the name of the data element, null.
     */
    public void _addBuffer(byte[] value, String paramName)
    {
        BaseChunk d = new BufferChunk(paramName, value);
        this.addDatum(d);
    }

    /**
     * Appends a string data element.
     *
     * @param value the value of the data element.
     * @param paramName the name of the data element, null.
     */
    public void _addString(String value, String paramName)
    {
        BaseChunk d = new StringChunk(paramName, value);
        this.addDatum(d);
    }

    /**
     * Appends a boolean data element.
     *
     * @param value the value of the data element.
     */
    public void _addBoolean(boolean value)
    {
        this._addBoolean(value, "");
    }

    /**
     * Appends a 32 bit integer data element.
     *
     * @param value the value of the data element.
     * @throws Exception 
     */
    public void _addInt32(int value)
    {
        this._addInt32(value, "");
    }

    /**
     * Appends a 32 bit unsigned integer data element.
     *
     * @param value the value of the data element.
     * @throws Exception 
     */
    public void _addUInt32(int value) throws Exception
    {
        this._addUInt32(value, "");
    }

    /**
     * Appends a 64 bit integer data element.
     *
     * @param value the value of the data element.
     */
    public void _addInt64(int value)
    {
        this._addInt64(value, "");
    }

    /**
     * Appends a 64 bit unsigned integer data element.
     *
     * @param value the value of the data element.
     * @throws Exception 
     */
    public void _addUInt64(BigInteger value) throws Exception
    {
        this._addUInt64(value, "");
    }

    /**
     * Appends a 16 bit integer data element.
     *
     * @param value the value of the data element.
     * @throws Exception 
     */
    public void _addInt16(short value)
    {
        this._addInt16(value, "");
    }

    /**
     * Appends an unsigned 16 bit integer data element.
     *
     * @param value the value of the data element (as int because Java does not support unsigned).
     * @throws Exception
     */
    public void _addUInt16(int value) throws Exception
    {
        this._addUInt16(value, "");
    }

    /**
     * Appends a float data element.
     *
     * @param value the value of the data element.
     */
    public void _addFloat(float value)
    {
        this._addFloat(value, "");
    }

    /**
     * Appends a double data element.
     *
     * @param value the value of the data element.
     */
    public void _addDouble(double value)
    {
        this._addDouble(value, "");
    }

    /**
     * Appends a RawValue data element.
     *
     * @param value the value of the data element.
     */
    public void _addBuffer(byte[] value)
    {
        this._addBuffer(value, "");
    }

    /**
     * Appends a string data element.
     *
     * @param value the value of the data element.
     */
    public void _addString(String value)
    {
        this._addString(value, "");
    }

    /**
     * Adds a datum.
     *
     * @param d The IZDatum object.
     * @return true on success.
     */
    protected boolean addDatum(BaseChunk d)
    {
        if(d == null)
            throw new Error("datum is null.  We can't add a null datum");

        return this.mContents.add(d);
    }

    /**
     * Removes a datum.
     *
     * @param d The IZDatum object.
     * @return true on success.
     */
    protected boolean removeDatum(BaseChunk d)
    {
        if(d == null)
            throw new Error("datum is null.  We can't remove a null datum");

        return this.mContents.remove(d);
    }

    /**
     * Removes all contained elements.
     */
    protected void removeAll()
    {
        this.mContents.clear();
    }

    /**
     * Converts the datum into a properly formed byte array that is ready
     * to be added to a RQST or RPLY transaction.<br>
     *
     * See RPC Stream Transfer Specification doc for complete protocol details:
     * <A HREF="doc-files/RPCStreamTransferSpecification.docx">RPCStreamTransferSpecification.docx</A>
     *
     * @param swapBytes specifies whether data needs to be byte swapped.<br>
     *               Set to true if sending to RPC Server on Windows.<br>
     *               Set to false if sending to Remote Launcher or RPC Server on OSX
     *
     * @return byte array containing the datums data
     */
    @Override
	public byte[] toByteArray(boolean swapBytes)
    {
        BaseChunk d;
        byte [] data = new byte[0];

        for ( int i = 0; i < this._getCount(); i++ )
        {
            d = this._getNth(i);
            if ( i == 0 )
                data = d.toByteArray(swapBytes);
            else
            {
                byte [] tempData = d.toByteArray(swapBytes);
                byte [] tempBuff = new byte [data.length + tempData.length];
                System.arraycopy(data,      0,   tempBuff,    0,             data.length);
                System.arraycopy(tempData,  0,   tempBuff,   data.length,    tempData.length);
                data = tempBuff;
            }
        }

        if (this.mName == null)      this.mName = "";
        if (this.mTypeName == null)  this.mTypeName = "";

        byte[] nameData = this.mName.getBytes(Charset.forName("UTF-8"));
        byte[] nameSize = BaseChunk.convertIntToByte1(nameData.length);
        byte[] typeData = this.mTypeName.getBytes(Charset.forName("UTF-8"));
        byte[] typeSize = BaseChunk.convertIntToByte1(typeData.length);
        this.mChunkSize = 10 + nameData.length + typeData.length + data.length;
        byte[] byteArray = new byte [this.mChunkSize];
        byte[] datumSize = BaseChunk.convertIntToByte4(this.mChunkSize, swapBytes);

        System.arraycopy(this.getChunkType().toString().getBytes(), 0, byteArray, 0,  						  4);
        System.arraycopy(datumSize,						0, byteArray, 4,									  4);
        System.arraycopy(nameSize, 						0, byteArray, 8,									  1);
        System.arraycopy(nameData,						0, byteArray, 9,									  nameData.length);
        System.arraycopy(typeSize, 						0, byteArray, 9 +  nameData.length,					  1);
        System.arraycopy(typeData,						0, byteArray, 10 + nameData.length,					  typeData.length);
        System.arraycopy(data,							0, byteArray, 10 + nameData.length + typeData.length, data.length);

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
    	String str = "";
    	String indent = "";

    	if (this.getChunkType() == NotificationType.Cmpd)  //Don't print this if we are a RQST or RPLY
    	{
    		str = "\t<B>" + this.getChunkType() + "</B> (" + this.getChunkSize() + ") \tName: " + this.mName + "\tType: " + this.mTypeName + "\n";
    		indent = "\t";
    	}

        for ( int i = 0; i < this._getCount(); i++ )
            str = str + indent + this._getNth(i).toString() + (i < this._getCount()-1?"\n":"");

    	return str;
    }
}
