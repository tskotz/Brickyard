package iZomateRemoteServer.Methods;

import iZomateCore.ServerCore.RPCServer.IncomingRequest;
import iZomateCore.ServerCore.RPCServer.OutgoingReply;
import iZomateRemoteServer.ServerThread;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Wrapper class for exported File objects. Every public method in this class
 * implements an RPC method. The corresponding client-side class is RemoteFile.
 */
public class FileMethods
{
	/**
     * Boilerplate constructor.
     *
     * @param map the export map
	 * @throws Exception
     */
	public FileMethods() throws Exception { }

    /**
     * This method is a wrapper for copying the file named by this abstract pathname, to the given name.
     *
     * @param req The IZIncomingRequest object.
     * @param reply The IZOutgoingReply object.
     * @param server The ServerThread object
     * @throws Exception
     */
    public void copyTo(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception {
        File in = this.getFile( req );
        File out = new File(req._getString("Dest"));
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(in).getChannel();
            destination = new FileOutputStream(out).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null)
                source.close();

            if(destination != null)
                destination.close();
        }
        reply._addBoolean(out.exists());
    }

    /**
     * This method is a wrapper for creating a new file.
     *
     * @param req The IZIncomingRequest object.
     * @param reply The IZOutgoingReply object.
     * @param server The ServerThread object
     * @throws Exception
     */
    public void createNewFile(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception {
        File file = this.getFile( req );
        reply._addBoolean(file.createNewFile());
    }

    /**
     * This method is a wrapper for deleting the file.
     *
     * @param req The IZIncomingRequest object.
     * @param reply The IZOutgoingReply object.
     * @param server The ServerThread object
     * @throws Exception
     */
    public void delete(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception {
        File file = this.getFile( req );
        reply._addBoolean(file.delete());
    }

    /**
     * This method deletes the contents of a directory and optionally the directory itself
     *
     * @param req The IZIncomingRequest object.
     * @param reply The IZOutgoingReply object.
     * @param server The ServerThread object
     * @throws Exception
     */
    public void deleteDirectoryContents(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception {
        File file = new File( this.getFileStr( req, "directory") );

        if (file.isDirectory()) {
        	if (!this.deleteDirContents(file, req._getBoolean("deleteWhenEmpty")))
        		throw new Exception("Some content failed to be deleted: " + file.getAbsolutePath());
        }
        else
        	throw new Exception("Item specified is not a directory: " + file.getAbsolutePath());
    }

	/**
	 * Deletes the contents of a directory and optionally the directory itself
	 *
	 * @param d the directory
	 * @param deleteWhenEmpty If true, the directory will be deleted when empty
	 * @return true if no problems were encountered
	 */
	private boolean deleteDirContents(File d, boolean deleteWhenEmpty) {
		boolean ok = true;

		for (File f : d.listFiles()) {
			//A directory must be empty before we can delete it
			if (f.isDirectory())
				ok = this.deleteDirContents(f, true);
			else
				ok = f.delete();

			if (!ok)
				break;
		}

		if (deleteWhenEmpty && ok)
			ok = d.delete();

		return ok;
	}

    /**
     * This method is a wrapper for seeing if file exists.
     *
     * @param req The IZIncomingRequest object.
     * @param reply The IZOutgoingReply object.
     * @param server The ServerThread object
     * @throws Exception
     */
    public void exists(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception {
        File file = this.getFile( req );
        reply._addBoolean(file.exists());
    }

    /**
     * This method is a wrapper for returning the absolute path for the file.
     *
     * @param req The IZIncomingRequest object.
     * @param reply The IZOutgoingReply object.
     * @param server The ServerThread object
     * @throws Exception
     */
    public void getAbsolutePath(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception {
        File file = this.getFile( req ); 
        reply._addString(file.getAbsolutePath());
    }

    /**
     * This method is a wrapper for returning the canonical path for the file.
     *
     * @param req The IZIncomingRequest object.
     * @param reply The IZOutgoingReply object.
     * @param server The ServerThread object
     * @throws Exception
     */
    public void getCanonicalPath(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception {
        File file = this.getFile( req );
        reply._addString(file.getCanonicalPath());
    }

    /**
     * This method is a wrapper for returning the name of the file.
     *
     * @param req The IZIncomingRequest object.
     * @param reply The IZOutgoingReply object.
     * @param server The ServerThread object
     * @throws Exception
     */
    public void getName(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
        File file = this.getFile( req );
        reply._addString(file.getName());
    }

    /**
     * This method is a wrapper for returning the pathname string of this abstract pathname's parent.
     *
     * @param req The IZIncomingRequest object.
     * @param reply The IZOutgoingReply object.
     * @param server The ServerThread object
     * @throws Exception
     */
    public void getParent(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception {
        File file = this.getFile( req );
        reply._addString(file.getParent());
    }

    /**
     * This method is a wrapper for returning whether the abstract path refers to a directory.
     *
     * @param req The IZIncomingRequest object.
     * @param reply The IZOutgoingReply object.
     * @param server The ServerThread object
     * @throws Exception
     */
    public void isDirectory(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception {
        File file = this.getFile( req );
        reply._addBoolean(file.isDirectory());
    }

    /**
     * This method is a wrapper for returning whether the abstract path refers to a file.
     *
     * @param req The IZIncomingRequest object.
     * @param reply The IZOutgoingReply object.
     * @param server The ServerThread object
     * @throws Exception
     */
    public void isFile(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception {
        File file = this.getFile( req );
        reply._addBoolean(file.isFile());
    }

    /**
     * This method is a wrapper for returning the length of the file
     *
     * @param req The IZIncomingRequest object.
     * @param reply The IZOutgoingReply object.
     * @param server The ServerThread object
     * @throws Exception
     */
    public void length(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception {
        File file = this.getFile( req );
        reply._addInt32((int)file.length());
    }

    /**
     * This method is a wrapper for returning the names of the files and directories in the directory.
     *
     * @param req The IZIncomingRequest object.
     * @param reply The IZOutgoingReply object.
     * @param server The ServerThread object
     * @throws Exception
     */
    public void list(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception {
    	String strRoot= this.getFileStr( req, "File" );
    	for( String s : this.listDirContents( new File( strRoot ), req._getBoolean( "recursive" ) ) )
            reply._addString( s.substring( strRoot.length() + (s.length() > strRoot.length()?1:0) ) );
    }
    
	/**
	 * Deletes the contents of a directory and optionally the directory itself
	 *
	 * @param d the directory
	 * @param deleteWhenEmpty If true, the directory will be deleted when empty
	 * @return true if no problems were encountered
	 */
	private ArrayList<String> listDirContents( File d, boolean bRecursive ) {
		ArrayList<String> contents= new ArrayList<String>();
		if( d.exists() ) {
			for (File f : d.listFiles()) {
				if ( bRecursive && f.isDirectory() )
					contents.addAll( this.listDirContents(f, true) );
				else
					contents.add( f.getPath() );
			}
		}	
		return contents;
	}

    /**
     * This method is a wrapper for creating the directory named by this abstract pathname.
     *
     * @param req The IZIncomingRequest object.
     * @param reply The IZOutgoingReply object.
     * @param server The ServerThread object
     * @throws Exception
     */
    public void mkdir(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception {
        File file = this.getFile( req );
        reply._addBoolean(file.mkdir());
    }

    /**
     * This method is a wrapper for creating the directory named by this abstract pathname, including any
     * necessary but nonexistent parent directories.
     *
     * @param req The IZIncomingRequest object.
     * @param reply The IZOutgoingReply object.
     * @param server The ServerThread object
     * @throws Exception
     */
    public void mkdirs(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception {
        File file = this.getFile( req );
        reply._addBoolean(file.mkdirs());
    }

    /**
     * This method reads data from a local file and sends it back to caller
     *
     * @param req The IZIncomingRequest object.
     * @param reply The IZOutgoingReply object.
     * @param server The ServerThread object
     * @throws Exception
     */
    public void readFileData(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception {
        File theFile = this.getFile( req );
        long offset = req._getUInt32("offset");

        RandomAccessFile ras = null;
        byte[] bytes = null;
        int maxBufSize = 500000;
        int bufSize = 0;

        try {
       	 	ras = new RandomAccessFile(theFile, "r");

        	if (ras.length() - offset > maxBufSize)
        		bufSize = maxBufSize;
        	else
        		bufSize = (int)(ras.length() - offset);

        	if (bytes == null || bufSize != bytes.length)
        		bytes = new byte[bufSize];

        	ras.seek(offset);
        	ras.read(bytes, 0, bytes.length);

            offset += bytes.length;

        	reply._addUInt32(offset, "offset");
        	reply._addUInt32(ras.length() - offset, "remaining");
        	reply._addBuffer(bytes, "data");
        }
        finally {
        	if (ras != null)
        		ras.close();
        }
    }

	/**
     * This method is a wrapper for copying the file named by this abstract pathname, to the given name.
     *
     * @param req The IZIncomingRequest object.
     * @param reply The IZOutgoingReply object.
     * @param server The ServerThread object
     * @throws Exception
     */
    public void renameTo(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception {
        File file = this.getFile( req );
        File file2 = new File( this.getFileStr( req, "NewName" ) );
        reply._addBoolean(file.renameTo(file2));
    }

	/**
     * This method unzips a zip file into a specified destination directory
     *
     * @param req The IZIncomingRequest object.
     * @param reply The IZOutgoingReply object.
     * @param server The ServerThread object
     * @throws Exception
     */
	public void unzip(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception {
		new Zip()._extract(req._getString("zippedFile"), req._getString("destinationFolder"), req._getBoolean("mergeSubDirs"));
    }

    /**
     * This method writes incoming raw data to a local file
     *
     * @param req The IZIncomingRequest object.
     * @param reply The IZOutgoingReply object.
     * @param server The ServerThread object
     * @throws Exception
     */
    public void writeFileData(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception {
        File theFile = this.getFile( req );
        boolean append = req._getBoolean("append");
        byte[] data = req._getBuffer("data");
        RandomAccessFile outfile = null;

        try {
        	outfile = new RandomAccessFile(theFile, "rw");

    		if (append)
    			outfile.seek(outfile.length());
    		else
    			outfile.setLength(0);

    		outfile.write(data);
        }
        finally {
        	if (outfile != null)
        		outfile.close();
        }
    }

    /**
     * This creates a zip file of the specified folder
     *
     * @param req The IZIncomingRequest object.
     * @param reply The IZOutgoingReply object.
     * @param server The ServerThread object
     * @throws Exception
     */
	public void zip(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception {
    	new Zip()._archive( this.getFileStr( req, "folderToZip"), this.getFileStr( req, "ZipFileToCreate") );
	}

	/**
	 * Opens a file or launches an application
	 * 
	 * @param req
	 * @param reply
	 * @param server
	 * @throws Exception
	 */
	public void openFile(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception {
		try {
			Desktop.getDesktop().open( this.getFile( req ) );
		} catch ( Exception e ) {
			req._addString(e.getMessage(), "error");
		}
	}
	
	/**
	 * Helper function that converts '~' to a path java can handle
	 * 
	 * @param req
	 * @return String
	 * @throws Exception
	 */
	private File getFile( IncomingRequest req ) throws Exception {
		return new File( this.replaceTilda( req._getString("File") ) );
	}

	/**
	 * Helper function that converts '~' to a path java can handle
	 * 
	 * @param req
	 * @return String
	 * @throws Exception
	 */
	private String getFileStr( IncomingRequest req, String strParam ) throws Exception {
		String str= this.replaceTilda( req._getString( strParam ) );
		if( str.endsWith( "/" ) || str.endsWith( "\\" ))
			str= str.substring( 0, str.length()-1 );
		return str;
	}

	/**
	 * Helper function that converts '~' to a path java can handle
	 * 
	 * @param req
	 * @return String
	 * @throws Exception
	 */
	private String replaceTilda( String strFile ) throws Exception {
		if( strFile.contains( "~" ))
			return strFile.replace( "~", System.getProperty("user.home") );
        return strFile;
	}
	
}
