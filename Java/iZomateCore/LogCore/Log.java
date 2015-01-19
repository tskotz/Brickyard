package iZomateCore.LogCore;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * The base Log class.  All log files should extend this class.
 */
public abstract class Log
{
	private   boolean append;
	protected boolean echo = false; 	//Echo to console?
	private   RandomAccessFile file; 	//The log file
	private   String filePath; 			//The full path to the log file
	private   String logDir; 			//The log file's parent directory
	private   String logExtension; 		//The log file's extension
	private   String logName; 			//The log file's name

    /**
     * Creates a new test log file.
     * If the file does not exist, it is created.
     *
     * @param LogName the name of the log file
     * @param LogDir the parent directory of the log file
     * @param LogExtension the file extension of the log
     * @param Append what to do if the file already exists: true = append, false = replace.
     * @param EchoToConsole true to echo the log to the console
     * @throws IOException
     */
	public Log(String LogName, String LogDir, String LogExtension, boolean Append, boolean EchoToConsole) throws IOException
	{
		this.echo = EchoToConsole;
		this.logDir = LogDir;
		this.logExtension = LogExtension;
		this.append = Append;
		this.logName = LogName;

		this.formatLogPath();
	}

	//-----------------------------------
	//         Accessor Methods
	//-----------------------------------

	/**
     * Returns the log file's extension.
     *
     * @return the file extension of this log
     */
	public final String getFileExtension()
	{
		return this.logExtension;
	}

	/**
     * Returns the log file's name.
     *
     * @return the file name of this log
     */
    public final String getFileName()
	{
    	return this.logName + "." + this.logExtension;
	}

    /**
     * Returns the full path to the log path.
     *
     * @return the full file path of this log
     */
	public final String getFilePath()
	{
		return this.filePath;
	}

    /**
     * Returns the full path to the log path.
     *
     * @return the full file path of this log
     */
	public final String getLogDir()
	{
		return this.logDir;
	}

	
    //-----------------------------------
	//    Protected & Private Methods
	//-----------------------------------

	/**
     * Returns the random access file object for this log.
	 * @throws Exception
     *
     * @returnt the random access file
     * @throws Exception
     */
	protected final RandomAccessFile _file() throws Exception
	{
		if( this.file == null ) {
			File f= new File( this.filePath );
			f.getParentFile().mkdirs();
			
			this.file = new RandomAccessFile( f, "rw");

			//Are we appending to an existing file?
			if(this.append)
				this.file.seek(this.file.length());
			else
				this.file.setLength(0);
		}

		return this.file;
	}

	/**
     * Writes a string to the log file. Each carriage return, linefeed, or adjacent pair of carriage return
     * plus linefeed is translated to a standard end-of-line marker, to ensure that line endings are always
     * treated consistently. Tabs are expanded to spaces so that tab characters are reserved for initial
     * indentation. All log output goes through this method.
     *
     * @param str the string to be written.
     * @throws Exception
     */
    protected void log(String str) throws Exception
    {
        str = normalizeEOLs(str);
        String newString = "";

        for(int i = 0; i < str.length(); i++)
        {
            char c = str.charAt(i);
            if(c == '\n') // physical end-of-line
            	newString += "\r\n";
            else if(c == '\t') // tab character
            	newString += ("    ");
            else
            	newString += c;
        }

        this.write(newString);
    }

    /**
     * Writes a string to the log file as a separate line by adding a leading end-of-line character.
     * NOTE: lines written to the log file using this method may not automatically be viewed.
     * For example, for an XML log, you need to wrap your logLine call in an XML tag when
     * you want the logged line to be visible in an XML browser.
     *
     * @param str the string to be written
     * @throws Exception
     */
    protected void _logLine(String str) throws Exception
    {
        this.log("\n");
        this.log(str);
    }

    /**
     * Normalizes all end-of-line sequences ("\n\n", "\n\r", "\r\n", "\r\r", "\r") to "\n".
     *
     * @param in the string to reformat
     * @return the reformatted string
     */
    protected static String normalizeEOLs(String in)
    {
    	in = in.replace("\\\\n\\\\n", "\\\\n");
    	in = in.replace("\\\\n\\\\r", "\\\\n");
    	in = in.replace("\\\\r\\\\n", "\\\\n");
    	in = in.replace("\\\\r\\\\r", "\\\\n");
    	in = in.replace("\\\\r", "\\\\n");

        return in;
    }

	/**
    * All raw output goes through here, except for tabs, newlines, and patches.
    *
    * @param s the string to write
    * @throws Exception
    */
	protected final void write(String s)throws Exception
	{
	    this._file().write(s.getBytes("UTF-8"));
	    if(this.echo)
	        System.out.print(s+"\n");
	}

	/**
     * Formats the full path to the log file.
     */
	private final void formatLogPath()
	{
		//Make sure log dir isn't null
		if (this.logDir == null)
			this.logDir = "";

		//Use the correct separator character
		else if (this.logDir.contains("\\\\") && ! File.separator.equals("\\"))
			this.logDir = this.logDir.replaceAll("\\\\", "/");
		else if (this.logDir.contains("/") && ! File.separator.equals("/"))
			this.logDir = this.logDir.replaceAll("/", "\\\\");

		//Make sure the log directory doesn't end with a file separator character
		if (this.logDir.endsWith(File.separator))
			this.logDir = this.logDir.substring(0, this.logDir.length()-1);

		//Make sure the log extension doesn't contain a period
		this.logExtension = this.logExtension.replaceAll("\\.", "");

		//Finally, construct the full path to the log file
		this.filePath = this.logDir + File.separator + this.logName + "." + this.logExtension;
	}

}
