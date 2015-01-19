package iZomateCore.LogCore.ResultLog;

import iZomateCore.AppCore.PluginInfo;
import iZomateCore.LogCore.Log;
import iZomateCore.LogCore.TransactionLog;
import iZomateCore.ServerCore.RPCServer.RemoteServer.RemoteServer;
import iZomateCore.TestCore.TestCaseParameters;
import iZomateCore.UtilityCore.TimeUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ResultLog extends Log
{
    private int				mRelCol = 1; 		//The current column number, relative to the current indentation level.
	private int				mErrors = 0;
	private int				mWarnings = 0;
	private String			mHTMLStartFontTag= "";
	private String			mHTMLEndFontTag= "";
	private boolean			mBToggleDiv= false;
	private int				mDivID= 0;
	private int				mXLinkID= 0;
	private boolean			mFilterSpecialCharsOneShot= true;
	private long			mStartTime= System.currentTimeMillis();
	private RemoteServer	mRemoteServer= null;
	private TransactionLog	mTransactionLog= null;
	private int				mTCCount= 0;
	
    /**
     * Creates a new test results log file.
     * If the file does not exist, it is created.
     * If it already exists, it is overwritten.
     *
     * @param LogName the name of the log file
     * @param LogDir the parent directory of the log file
     * @param testCases the total number of testcases in the dataparams
     * @param echo true to echo the result log output to the console
     * @throws Exception
     */
	public ResultLog( String logName, String logDir, int testCases, boolean echo ) throws Exception {
		super( logName, logDir, ".html", false, echo);
		this.log( "<pre>\n");
	}
	
	public void _setRemoteServer( RemoteServer rs ) {
		this.mRemoteServer= rs;
	}
	
	public void _SetTransactionLog( TransactionLog fTransactionLog ) {
		this.mTransactionLog= fTransactionLog;
	}
	
	/**
	 * 
	 * @param params
	 * @throws Exception
	 */
	public void _logTestInfo( TestCaseParameters params ) throws Exception {
		String strPlugin= (params._GetPlugin()==null || params._GetPlugin().isEmpty()) ? "" : (" Plugin: <i>" + params._GetPlugin() + "</i>\n");
		this.log( "<HR></pre><H2>" + ++this.mTCCount + ". " + params._GetTestCaseName() + "</H2><pre>\n" + strPlugin + 
				  "Host App: <i>" + params._GetApp() + "</i>\n" +
				  " Testbed: <i>" + params._GetTestbed() + "</i>\n\n" );
	}
	
	/**
	 * 
	 * @param params
	 * @throws Exception
	 */
	public void _LogPluginInfo( PluginInfo info ) throws Exception {
		this.log( "</pre><H4>Plugin Info</H4><pre>\n" +
				  " Full Name: <i>" + info.m_strFullName + "</i>\n" + 
				  "Short Name: <i>" + info.m_strShortName + "</i>\n" + 
				  "     Build: <i>" + info.m_nBuildNumber + "</i>\n" + 
				  "   Version: <i>" + info.m_nVersionNumber + "</i>\n\n" );
	}

	/**
	 * @throws Exception 
	 */
	public void _printSummary() throws Exception {
		String summary= "<b>" + this.mTCCount + "</b> Testcases Run<br>";
		
		String format = String.format("%%0%dd", 2);  
	    long elapsedTime = (System.currentTimeMillis() - this.mStartTime)/1000;  
	    String seconds = String.format(format, elapsedTime % 60);  
	    String minutes = String.format(format, (elapsedTime % 3600) / 60);  
	    String hours = String.format(format, elapsedTime / 3600);  
	    String time =  hours + ":" + minutes + ":" + seconds;
	    
		if( this.mErrors > 0 )
			summary+= "<b>" + this.mErrors + "</b> <FONT color=\"RED\">Error(s)</FONT> were detected<br>";
		if( this.mWarnings > 0 )
			summary+= "<b>" + this.mWarnings + "</b> <FONT color=\"ORANGE\">Warning(s)</FONT> were detected<br>";
			
		this._logString("\n<HR></pre><H2>Test Summary</H2>" + summary + "<br>Elapsed Run Time: " + time);
	} 
	
	/**
	 * 
	 * @return
	 */
	public int _GetErrorCount() {
		return this.mErrors;
	}
	
	/**
	 * 
	 * @return
	 */
	public void _incrErrorCount() {
		this.mErrors++;
	}

	/**
	 * 
	 * @return
	 */
	public int _GetWarningCount() {
		return this.mWarnings;
	}
	
	/**
	 * 
	 * @return
	 */
	public void _incrWarningCount() {
		this.mWarnings++;
	}

	/**
	 * 
	 * @param htmlTag
	 */
	public ResultLog _TextFormat( String htmlTag ) {
		this.mHTMLStartFontTag= htmlTag;
		this.mHTMLEndFontTag= htmlTag.replace( "<", "</" );
		return this;
	}
	
	/**
	 * 
	 * @param state
	 */
	public ResultLog _skipSpecialCharsFilter() {
		this.mFilterSpecialCharsOneShot= false;
		return this;
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	private void addToggleScript() throws Exception {
		if( !this.mBToggleDiv ) {
			this.log( "<script language=\"javascript\">\n" +
					  "	function toggleDiv(divid){\n" +
					  "		if(document.getElementById(divid).style.display == 'none'){\n" +
					  "			document.getElementById(divid).style.display = 'block';\n" +
					  "		}else{\n" +
					  "			document.getElementById(divid).style.display = 'none';\n" +
					  "		}\n" + 
					  "	}\n" +
					"</script>" );
			this.mBToggleDiv= true;
		}
	}
	
	/**
	 * 
	 * @param data
	 * @param linkName
	 * @param visible
	 * @return
	 * @throws Exception
	 */
	public ResultLog _logDivData( String data, String linkName, boolean visible ) throws Exception {
		this.addToggleScript();
		
		String strDivID= "div" + ++this.mDivID;
		this._logString( "<a href=\"javascript:;\" onmousedown=\"toggleDiv('" + strDivID + "');\">" + linkName + "</a>\n" +
					  "<div id=\"div" + this.mDivID + "\" style=\"display:" + (visible?"block":"none") + "\">" + data + "</div>" );
		return this;
	}
	
	/**
	 * 
	 * @param linkName
	 * @param visible
	 * @return
	 * @throws Exception
	 */
	public String _logDivStart( String linkName, boolean visible ) throws Exception {
		this.addToggleScript();	
		String strDivID= "div" + ++this.mDivID;

		this._logLine( "	<a href=\"javascript:;\" onmousedown=\"toggleDiv('" + strDivID + "');\">" + linkName + "</a>\n" +
					  "<div id=\"div" + this.mDivID + "\" style=\"display:" + (visible?"block":"none") + "\">" );
		return strDivID;
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public ResultLog _logDivEnd( String strDivID ) throws Exception {
		this.addToggleScript();	
		this._logLine( "</div>" );
		return this;
	}

    /**
     * Writes a data message string to the log file with a <Log> tag and a Data type.
     *
     * @param str the string to be written
     * @throws Exception
     */
    public void _logData(String str) throws Exception {
    	this._logGeneric(str, "data");
    }
    
    public void _logImage( String imageURL ) throws Exception {
    	this._logImage( imageURL, -1, -1 );
    }

    public void _logImage( String imageURL, int width, int height ) throws Exception {
    	this.log( "<img src=\"" + imageURL + "\" " + (width==-1?"":"width=\""+width+"\" ") + (height==-1?"":"height=\""+height+"\" ") + "/>" );
    }

    public void _logLink( String link, String text ) throws Exception {
    	this._logLine( "<a href=\"" + link + "\">" + text + "</a>" );
    }
    
    public void _logScreenShot() throws Exception {
    	this._logScreenShot( null );
    }
    
    public void _logScreenShot( String linkName ) throws Exception {
    	if( this.mRemoteServer != null ) {
    		String fname= "Screenshots/" + new SimpleDateFormat("yyyy.MM.dd_hh.mm.ss.SSS'.jpg'").format( new Date() );
    		File f= new File( this.getLogDir() + "/" + fname );
    		f.getParentFile().mkdirs();
			try {
	    		RandomAccessFile fileScreeenShot;
				fileScreeenShot= new RandomAccessFile( f, "rw" );
	    		fileScreeenShot.write( this.mRemoteServer._Robot()._screenDataGet( null ) );
	    		fileScreeenShot.close();
			} catch( Exception e ) {
				this._logMessage( e.getMessage() );
			}
    		this._logLink( fname, linkName!=null?linkName:"screenshot" );
    	}
    }

    /**
     * Writes a debug message string to the log file with a <Log> tag and a Debug type.
     *
     * @param str the string to be written
     * @throws Exception
     */
    public void _logDebug(String str) throws Exception {
    	this._logGeneric(str, "debug");
	}

    /**
     * Writes an error message string to the log file with an <Error> tag
     * and increments the error count for the current log block.
     *
     * @param str the string to be written
     * @throws Exception
     */
    public void _logError( String str , boolean bLogScreenShot ) throws Exception {
    	this._incrErrorCount();
    	String strAnchor= this._createTransactionLogAnchor( str, "RED" );
	    this._logLine("<Error timestamp=\"" + TimeUtils.getDateTime() + "\"><FONT color=\"RED\">" + strAnchor + "</FONT></Error>");
	    
        if( bLogScreenShot )
        	this._logScreenShot( "Screenshot" );
	}

    /**
     * Writes an error message string to the log file with an <Error> tag
     * and increments the error count for the current log block.
     *
     * @param str the string to be written
     * @throws Exception
     */
    public void _logWarning(String str) throws Exception {
    	this._incrWarningCount();
    	String strAnchor= this._createTransactionLogAnchor( str, "ORANGE" );
	    this._logLine("<Warning timestamp=\"" + TimeUtils.getDateTime() + "\"><FONT color=\"ORANGE\">" + strAnchor + "</FONT></Error>");
	}
    
    /**
     * Writes an exception to the log file and increments the error count.
     *
     * @param x the exception
     * @throws Exception
     */
    public void _LogException( Throwable x, boolean bLogScreenShot ) throws Exception {
    	this._incrErrorCount();
		System.out.println( x.getMessage() );
    	this.addToggleScript();
        StringWriter sw = new StringWriter();
        x.printStackTrace(new PrintWriter(sw));
    	String strAnchor= this._createTransactionLogAnchor( "TRANS LOG: " + x.getMessage(), "BLUE" );
    	this._logLine( "<a href=\"javascript:;\" onmousedown=\"toggleDiv('div" + (++this.mDivID) + "');\"><FONT color=\"RED\">EXCEPTION: " + x.getMessage() + "</FONT></a><br/>\n" +
				  "<div id=\"div" + this.mDivID + "\" style=\"display:none\">" + strAnchor );
    	
        if( bLogScreenShot )
        	this._logScreenShot( "Screenshot" );

        this._logLine( sw.toString() + "</div>" ); 
        sw.close();
	}

    /**
     * Writes a string of the user-specified type to the log file with a <Log> tag and a User-specified type
     *
     * @param str the string to be written
     * @param type the type of data to log
     * @throws Exception
     */
    public void _logGeneric(String str, String type) throws Exception	{
	    this._logLine("<Log type=\"" + type + "\" timestamp=\"" + TimeUtils.getDateTime() + "\">" + this.mHTMLStartFontTag + this.escapeSpecialChars(str) + this.mHTMLEndFontTag + "</Log>");
	    this.mHTMLStartFontTag="";
	    this.mHTMLEndFontTag="";
    }

    /**
     * Writes a string to the log file as a separate line. A leading end-of-line character is written if
     * necessary, and a final end-of-line character is written always. NOTE: lines written to the log file
     * using this method will not automatically be viewed. Use one of the more specific logging options, or
     * wrap your logLine call in an XML tag when you want the logged line to be visible in an XML browser.
     * <b>This method is only intended for use by the iZomateCore architecture.</b>
     *
     * @param str the string to be written
     * @throws Exception
     */
    @Override
    public void _logLine(String str) throws Exception {
    	this._logLine( str, false );
    }

    	/**
     * Writes a string to the log file as a separate line. A leading end-of-line character is written if
     * necessary, and a final end-of-line character is written always. NOTE: lines written to the log file
     * using this method will not automatically be viewed. Use one of the more specific logging options, or
     * wrap your logLine call in an XML tag when you want the logged line to be visible in an XML browser.
     * <b>This method is only intended for use by the iZomateCore architecture.</b>
     *
     * @param str the string to be written
     * @throws Exception
     */
    public void _logLine(String str, boolean bTimeStamp) throws Exception {
    	//this.startLine();
    	if( bTimeStamp )
    		this.log(TimeUtils.getTime() + "  " + str+"\n");
    	else
    		this.log(str+"\n");
    		
        //this.startLine();
    }
    
    /**
     * 
     * @param str
     * @throws Exception
     */
    public void _logString(String str) throws Exception {
    	this.log( str );
    }

    /**
     * Writes a message string to the log file with a <Log> tag and a Message type.
     *
     * @param str the string to be written
     * @throws Exception
     */
    public void _logMessage(String str) throws Exception
    {
    	this._logGeneric(str, "message");
    }

    /**
     * Writes a message string to the log file with a <Log> tag and a Performance type.
     *
     * @param str the string to be written
     * @throws Exception
     */
    public void _logPerformance(String str) throws Exception
    {
    	this._logGeneric(str, "performance");
    }
    /**
     * Outputs a newline if we're not already at the beginning of a line.
     *
     * @throws Exception
     */
    protected void startLine() throws Exception
    {
        if (this.mRelCol != 0)
            this.log("\n");
    }

    /**
     *     Replaces browser unfriendly characters with friendly ones.
     *
     *      @param      stringToClean  the string you want to clean up
     *      @return		Returns the updated string
     */
    private final String escapeSpecialChars(String stringToClean)
    {
    	if( stringToClean != null && this.mFilterSpecialCharsOneShot ) {
        	this.mFilterSpecialCharsOneShot= true; //make sure this is turned back on
    		return stringToClean.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    	}
        return stringToClean;
    }
    
    /**
     * 
     * @param strAnchorText
     * @return
     * @throws Exception
     */
    private String _createTransactionLogAnchor( String strAnchorText, String strColor ) throws Exception {
    	String strAnchor= this.escapeSpecialChars( strAnchorText );
        if( this.mTransactionLog != null ) {
            String strXLinkID= "xlink" + (++this.mXLinkID);
            this.mTransactionLog._log( "<A href=\"" + this.getFileName() + "#" + strXLinkID + "\" name=\"" + strXLinkID + "\"><FONT color=\"" + strColor + "\">RESULT LOG: " + strAnchor + "</FONT></A>" );
            strAnchor= "<A href=\"" + this.mTransactionLog.getFileName() + "#" + strXLinkID + "\" name=\"" + strXLinkID + "\"><FONT color=\"" + strColor + "\">" + strAnchor + "</FONT></A>\n";        	
        }
        return strAnchor;
    }

}
