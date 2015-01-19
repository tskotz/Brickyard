package iZomateCore.ServerCore.RPCServer.RemoteServer;

import iZomateCore.ServerCore.RPCServer.IncomingReply;
import iZomateCore.ServerCore.RPCServer.OutgoingRequest;
import iZomateCore.ServerCore.RPCServer.RPCServer;
import iZomateCore.UtilityCore.TimeUtils;

import java.io.RandomAccessFile;
import java.nio.charset.Charset;

/**
 * Represents a file on a remote system, accessed through the RemoteServer. This class implements most of
 * the methods of the standard Java {@link java.io.File File} class. Each object represents a file that is
 * accessed from the system on which the iZomateRemoteServer is running.
 */
public final class RemoteFile
{
	private RPCServer mServer; //the RemoteServer's RPCServer object
    private String mPathName; //the path to and name of the remote file

    /**
     * Constructor.
     *
     * @param server the RemoteServer's RPCServer
     * @param pathName the path name of the file, relative to the system on which the RemoteServer is running
     * @throws Exception
     */
    protected RemoteFile(RPCServer server, String pathName) throws Exception
    {
        this.mServer = server;

        // Get the appropriate separator chars for the testbeds Operating System
        this.mPathName = pathName != null ? pathName.replace("\\", "/") : "";

        if (this.mPathName.endsWith(":"))
        	this.mPathName += "/";
    }

	//-----------------------------------
	//          Public Methods
	//-----------------------------------

    /**
     * Returns the path name that was set in the constructor
     * @return
     */
    public String _getPathAndName()
    {
    	return this.mPathName;
    }
   
    /**
     * Returns the path name that was set in the constructor
     * @return
     * @throws Exception 
     */
    public String _getUNIXExePathAndName() throws Exception
    {
    	String strUNIXexe= this._getNameNoExt();
    	
    	if( this.mPathName.endsWith( "SAHook.app" ) )
    		strUNIXexe= "PluginHooksSA";
    	else if( this.mPathName.endsWith( "SAHookd.app" ) )
    		strUNIXexe= "PluginHooksSAd";

    	return this.mPathName + "/Contents/MacOS/" + strUNIXexe;
    }

    /**
     * Opens/launches the remote file
     * @throws Exception
     */
    public void _open() throws Exception {
    	OutgoingRequest req = this.mServer._createRequest("openFile");
        req._addString(this.mPathName, "file");
        IncomingReply reply= this.mServer._processRequest(req);
        if( reply._exists("error"))
        	throw new Exception( "RemoteFile, " + this.mPathName + " failed to open: " + reply._getString("error"));
    }
    
    /**
     * Copies the local file to the remote file.  Overwrites existing file.
     *
     * @param localSourceFile the path and name of the file to copy
     * @throws Exception
     */
    public void _copyFrom(String localSourceFile) throws Exception
    {
    	RandomAccessFile ras = null;
        byte[] bytes = null;
        int maxBufSize = 500000;
        int bufSize = 0;
        int offset = 0;

        try
        {
        	ras = new RandomAccessFile(localSourceFile, "r");

        	do
        	{
   	        	if (ras.length() - offset > maxBufSize)
   	        		bufSize = maxBufSize;
   	        	else
   	        		bufSize = (int)(ras.length() - offset);

   	        	if (bytes == null || bufSize != bytes.length)
   	        		bytes = new byte[bufSize];

   	        	ras.read(bytes, 0, bytes.length);

   	            OutgoingRequest req = this.mServer._createRequest("writeFileData");
   	    		req._addString(this.mPathName, "File");
   	        	req._addBoolean(offset > 0, "append");
   	        	req._addBuffer(bytes, "data");
   	            this.mServer._processRequest(req);
   	            offset += bytes.length;
          	 }
          	 while (ras.length() > offset);
        }
        catch (Exception e)
        {
        	throw new Exception("Failed to write " + localSourceFile + " to remote file: " + e.getMessage());
        }
        finally
        {
        	if (ras != null)
        		ras.close();
        }
    }

    /**
     * Copies the remote file to a specified file on the local system.  Overwrites existing file.
     *
     * @param localDestFile the path and name of the file copy to create
     * @throws Exception
     */
    public void _copyTo(String localDestFile) throws Exception
    {
        long offset = 0;
        long remaining = 0;
        RandomAccessFile outfile = null;

        try
        {
        	outfile = new RandomAccessFile(localDestFile, "rw");
    		outfile.setLength(0);

        	do
        	{
        		OutgoingRequest req = this.mServer._createRequest("readFileData");
        		req._addString(this.mPathName);
        		req._addUInt32(offset, "offset");

        		IncomingReply reply = this.mServer._processRequest(req);
        		offset = reply._getUInt32("offset");
        		remaining = reply._getUInt32("remaining");
        		outfile.write(reply._getBuffer("data"));
         	}
        	while (remaining != 0);
        }
        catch (Exception e)
        {
           	throw new Exception("Failed to copy to " + localDestFile + " to remote file: " + e.getMessage());
        }
        finally
        {
        	if (outfile != null)
        		outfile.close();
        }
    }

    /**
     * Makes a copy of the remote file on the remote system at the specified remote destination
     *
     * @param remoteDestFile The new remote file's pathname for the copied file
     * @param timeout how long to wait to copy the file
     * @return true if and only if the copied file exists in the new location, false otherwise.
     * @throws Exception
     */
    public boolean _copyTo(RemoteFile remoteDestFile, int timeout) throws Exception
    {
        OutgoingRequest req = this.mServer._createRequest("copyTo");
        req._addString(this.mPathName, "File");
        req._addString(remoteDestFile._getPathAndName(), "Dest");
        req._setTimeoutVal(timeout);
        IncomingReply reply = this.mServer._processRequest(req);
        return reply._getBoolean(0);
    }

    /**
     * Creates a new file. See {@link java.io.File#createNewFile File.createNewFile}.
     *
     * @return true if the file was created (I think)
     * @throws Exception
     */
    public boolean _createNewFile() throws Exception
    {
    	return this.makeRequest("createNewFile")._getBoolean(0);
    }

    /**
     * Deletes the file. See {@link java.io.File#delete File.delete}.
     *
     * @return true if the file was deleted
     * @throws Exception
     */
    public boolean _delete() throws Exception
    {
        return this.makeRequest("delete")._getBoolean(0);
    }

    /**
     * <b>USE WITH CAUTION!!!</b><br><br>
     * This deletes the all of the contents including subfolders within the specified directory.
     *
     * @param deleteDirectory If true, then it deletes the folder after it has been emptied
     * @return true if the delete was successful
     * @throws Exception
     */
    public boolean _deleteContents(boolean deleteDirectory) throws Exception
    {
    	OutgoingRequest req = this.mServer._createRequest("deleteDirectoryContents");
        req._addString(this.mPathName, "directory");
        req._addBoolean(deleteDirectory, "deleteWhenEmpty");
        this.mServer._processRequest(req);
        return true;
   }

    /**
     * Tests whether the file exists. See {@link java.io.File#exists File.exists}.
     * @return true if the file exists
     *
     * @throws Exception
     */
    public boolean _exists() throws Exception
    {
    	return this._exists( TimeUtils._GetDefaultTimeout() );
	}
    
    /**
     * Tests whether the file exists. See {@link java.io.File#exists File.exists}.
     * @return true if the file exists
     *
     * @throws Exception
     */
    public boolean _exists( int timeout ) throws Exception
    {
        return this.makeRequest("exists", timeout)._getBoolean(0);
    }

    /**
     * Returns the absolute path for the file. See {@link java.io.File#getAbsolutePath File.getAbsolutePath}.
     *
     * @return the absolute path for the file
     * @throws Exception
     */
    public String _getAbsolutePath() throws Exception
    {
        return this.makeRequest("getAbsolutePath")._getString(0);
    }

    /**
     * Returns the canonical path for the file. See {@link java.io.File#getCanonicalPath File.getCanonicalPath}.
     *
     * @return the canonical path for the file
     * @throws Exception
     */
    public String _getCanonicalPath() throws Exception
    {
        return this.makeRequest("getCanonicalPath")._getString(0);
    }

    /**
     * Returns the name of the file. See {@link java.io.File#getName File.getName}.
     *
     * @return the name of the file
     * @throws Exception
     */
    public String _getName() throws Exception
    {
        return this.mPathName.substring(this.mPathName.lastIndexOf("/")+1);
    }
    
    /**
     * Returns the name of the file. See {@link java.io.File#getName File.getName}.
     *
     * @return the name of the file
     * @throws Exception
     */
    public String _getNameNoExt() throws Exception
    {
        return this.mPathName.substring(this.mPathName.lastIndexOf("/")+1, this.mPathName.length()-4);
    }
    
    /**
     * Returns the name of the file. See {@link java.io.File#getName File.getName}.
     *
     * @return the name of the file
     * @throws Exception
     */
    public String _getParentPath() throws Exception
    {
        return this.mPathName.substring(0, this.mPathName.lastIndexOf("/"));
    }


    /**
     * Returns the pathname string of this abstract pathname's parent, or null if this pathname does not name
     * a parent directory. See {@link java.io.File#getParent File.getParent}.
     *
     * @return the pathname string of this abstract pathname's parent
     * @throws Exception
     */
    public RemoteFile _getParent() throws Exception
    {
        return new RemoteFile(this.mServer, this._getParentPath() );
    }

    /**
     * Returns whether the abstract path refers to a directory. See
     * {@link java.io.File#isDirectory File.isDirectory}.
     *
     * @return whether the abstract path refers to a directory
     * @throws Exception
     */
    public boolean _isDirectory() throws Exception
    {
        return this.makeRequest("isDirectory")._getBoolean(0);
    }

    /**
     * Returns whether the abstract path refers to a file. See {@link java.io.File#isFile File.isFile}.
     *
     * @return whether the abstract path refers to a file
     * @throws Exception
     */
    public boolean _isFile() throws Exception
    {
        return this.makeRequest("isFile")._getBoolean(0);
    }

    /**
     * Returns an array of strings naming the files and directories in the directory denoted by this abstract
     * pathname. See File.list.
     *
     * @param bRecursive list all children and their children
     * @return an array of strings naming the files and directories in the directory
     * @throws Exception
     */
    public String[] _list( boolean bRecursive ) throws Exception
    {
    	OutgoingRequest req= this.mServer._createRequest( "list" );
        req._addString(this.mPathName, "File");
    	req._addBoolean( bRecursive, "recursive" );
    	IncomingReply reply = this.mServer._processRequest( req );
        int n = reply._getCount();
        String[] ret = new String[n];
        for(int i = 0; i < n; i++)
            ret[i] = reply._getString(i);
        return ret;
    }

    /**
     * Creates the directory named by this abstract pathname. See {@link java.io.File#mkdir File.mkdir}.
     *
     * @return true if the directory is created
     * @throws Exception
     */
    public boolean _mkdir() throws Exception
    {
        return this.makeRequest("mkdir")._getBoolean(0);
    }

    /**
     * Creates the directory named by this abstract pathname, including any necessary but nonexistent parent
     * directories. See {@link java.io.File#mkdirs File.mkdirs}.
     *
     * @return true if the directories are created
     * @throws Exception
     */
    public boolean _mkdirs() throws Exception
    {
        return this.makeRequest("mkdirs")._getBoolean(0);
    }

    /**
     * Reads the file data as UTF-8 and returns it as string.
     *
     * @return String
     * @throws Exception
     */
    public String _readFileData() throws Exception
    {
    	return this._readFileData(Charset.forName("UTF-8"));
    }

    /**
     * Reads the file data as specified Charset and returns it as string.
     *
     * @param charset the character encoding type
     * @return String
     * @throws Exception
     */
    public String _readFileData(Charset charset) throws Exception
    {
        return new String(this._readFileRawData(), charset);
    }

    /**
     * Reads the file data and returns it as byte[].
     *
     * @return byte[]
     * @throws Exception
     */
    public byte[] _readFileRawData() throws Exception
    {
        long offset = 0;
        long remaining = 0;
        byte[] data = {};

    	do
    	{
    		OutgoingRequest req = this.mServer._createRequest("readFileData");
    		req._addString(this.mPathName, "File");
    		req._addUInt32(offset, "offset");
    		IncomingReply reply = this.mServer._processRequest(req);
    		offset = reply._getUInt32("offset");
    		remaining = reply._getUInt32("remaining");

    		byte[] tmp = new byte[data.length + reply._getBuffer("data").length];
            System.arraycopy(data, 0, tmp, 0, data.length);
            System.arraycopy(reply._getBuffer("data"), 0, tmp, data.length, reply._getBuffer("data").length);
            data = tmp;
    	}
    	while (remaining != 0);

    	return data;
    }

    /**
     * Renames the file denoted by this abstract pathname See {@link java.io.File#renameTo File.renameTo}.
     *
     * @param dest The new abstract pathname for the named file
     * @return true if and only if the renaming succeeded, false otherwise.
     * @throws Exception
     */
    public boolean _renameTo(RemoteFile dest) throws Exception
    {
        OutgoingRequest req = this.mServer._createRequest("renameTo");
        req._addString(this.mPathName);
        req._addString(dest._getAbsolutePath());
        IncomingReply reply = this.mServer._processRequest(req);
        return reply._getBoolean(0);
    }

    /**
     * Unzips a zip file into a specified destination directory.  Returns the destination directory.
     *
     * @param destinationDir the path to the destination directory where we want to unzip.  Will be created if it does not exist.
     * @param mergeSubDirs determines how to handle extracting sub directories if they already exist in the destination directory.
     * 					TRUE will extract into the existing sub dir, FALSE will remove the existing sub dir before extracting
     * @return RemoteFile to the destination directory
     * @throws Exception
     */
    public RemoteFile _unzip(String destinationDir, boolean mergeSubDirs) throws Exception
    {
        OutgoingRequest req = this.mServer._createRequest("unzip");
        req._addString(this.mPathName, "zippedFile");
        req._addString(destinationDir, "destinationFolder");
        req._addBoolean(mergeSubDirs, "mergeSubDirs");
        this.mServer._processRequest(req);
        return new RemoteFile(this.mServer, destinationDir);
    }

    /**
     * Writes data to the remote file.  Overwrites existing file.
     *
     * @param data the data to write to remote file
     * @throws Exception
     */
    public void _writeFileData(String data) throws Exception
    {
        byte[] bytes = null;
        int maxBufSize = 500000;
        int bufSize = 0;
        int offset = 0;

        try
        {
            do
            {
	        	if (data.length() - offset > maxBufSize)
	        		bufSize = maxBufSize;
	        	else
	        		bufSize = data.length() - offset;

	        	if (bytes == null || bufSize != bytes.length)
	        		bytes = new byte[bufSize];

	            System.arraycopy(data.getBytes(), offset, bytes, 0, bytes.length);

	            OutgoingRequest req = this.mServer._createRequest("writeFileData");
	    		req._addString(this.mPathName);
	        	req._addBoolean(offset > 0, "append");
	        	req._addBuffer(bytes, "data");
	            this.mServer._processRequest(req);
	            offset += bytes.length;
            }
            while (data.length() > offset);
        }
        catch (Exception e)
        {
        	throw new Exception("Failed to write data to remote file: " + e.getMessage());
        }
    }

    /**
     * Zips the directory and returns the new zip file.
     *
     * @param newZipFile the full path to the zip file to create
     * @return RemoteFile to the newly created zip file
     * @throws Exception
     */
    public RemoteFile _zip(String newZipFile) throws Exception
    {
        OutgoingRequest req = this.mServer._createRequest("zip");
        req._addString(this.mPathName, "folderToZip");
        req._addString(newZipFile, "ZipFileToCreate");
        this.mServer._processRequest(req);
        return new RemoteFile(this.mServer, newZipFile);
    }

    //-----------------------------------
	//          Private Methods
	//-----------------------------------

    /**
     * Back-end to save duplicate code when making requests in this class.
     *
     * @param the name of the remote method to call
     * @throws Exception
     */
    private IncomingReply makeRequest(String methodName) throws Exception
    {
        return this.makeRequest( methodName, TimeUtils._GetDefaultTimeout() );
    }

    private IncomingReply makeRequest(String methodName, int timeout) throws Exception
    {
        OutgoingRequest req = this.mServer._createRequest(methodName);
        req._addString(this.mPathName, "File");
        req._setTimeoutVal( timeout );
        return this.mServer._processRequest(req);
    }
}
