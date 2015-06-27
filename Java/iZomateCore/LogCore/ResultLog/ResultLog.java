package iZomateCore.LogCore.ResultLog;

import iZomateCore.AppCore.PluginInfo;
import iZomateCore.LogCore.Log;
import iZomateCore.LogCore.TransactionLog;
import iZomateCore.ServerCore.RPCServer.RemoteServer.RemoteServer;
import iZomateCore.ServerCore.RPCServer.RemoteServer.SystemInfo;
import iZomateCore.TestCore.TestCaseParameters;
import iZomateCore.TestCore.Testbed;
import iZomateCore.UtilityCore.TimeUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	private Set<Integer> 	mTCFailureSet = new HashSet<Integer>();
	private Map<String, ArrayList<Metric>> mMetrics= new HashMap<String, ArrayList<Metric>>();

	
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
		this.log( "<html>\n<HEAD>\n\t<meta charset=\"UTF-8\">\n\t<TITLE>"+logName+"</TITLE>\n" +
					"<style type=\"text/css\">\n\tBODY { font-family:Courier;font-size:13; }\n\tH1,H2,H3,H4,H5 { font-family:Ariel; }\n\tFOOTER { font-family:Ariel;font-size:16; }\n</style>\n"+
				"</HEAD>\n<body>\n");
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
	public void _logTestCaseStartupInfo( TestCaseParameters params ) throws Exception {
		String strPlugin= (params._GetPlugin()==null || params._GetPlugin().isEmpty()) ? "" : (" Plugin: <i>" + params._GetPlugin() + "</i><br>\n");
		this.log( "<HR><H2>" + ++this.mTCCount + ". " + params._GetTestCaseName() + "</H2>\n" + 
				  "   Start: <i>" + TimeUtils.getDateTime() + "</i><br>\n" + 
					strPlugin + 
				  "Host App: <i>" + params._GetApp() + "</i><br>\n" +
				  " Testbed: <i>" + params._GetTestbed() + "</i><br>\n<br>\n" );
		
		this._logMetric("testcase", String.valueOf(this.mTCCount));
	}
	
	public void _logTestCaseFinishInfo() throws Exception {
		this._logMetric("testcase", String.valueOf(this.mTCCount));
	}
	
	/**
	 * 
	 * @param params
	 * @throws Exception
	 */
	public void _LogPluginInfo( PluginInfo info ) throws Exception {
		this.log( "<H4>Plugin Info</H4>\n" +
				  " Full Name: <i>" + info.m_strFullName + "</i><br>\n" + 
				  "Short Name: <i>" + info.m_strShortName + "</i><br>\n" + 
				  "     Build: <i>" + info.m_nBuildNumber + "</i><br>\n" + 
				  "   Version: <i>" + info.m_nVersionNumber + "</i><br>\n<br>\n" );
	}
	
	/**
	 * @throws Exception 
	 */
	public void _printSummary() throws Exception {		
		String format = String.format("%%0%dd", 2);  
	    long elapsedTime = System.currentTimeMillis() - this.mStartTime;  
	    String ms = String.format(format, elapsedTime % 1000);  
	    elapsedTime = elapsedTime / 1000;  
	    String seconds = String.format(format, elapsedTime % 60);  
	    String minutes = String.format(format, (elapsedTime % 3600) / 60);  
	    String hours = String.format(format, elapsedTime / 360);  
	    String time =  hours + ":" + minutes + ":" + seconds + "." + ms;
	    
	    String strImg= "AutomationToolbox/Preferences/Templates/Images/GreenDot.png";
		String summary= "<b>" + this.mTCCount + "</b> Testcase" + (this.mTCCount>1?"s":"")+ " Run<br>";
		if( this.mErrors > 0 ) {
			summary+= "<b>" + this.mTCFailureSet.size() + "</b> Testcase" + (this.mTCFailureSet.size()>1?"s":"")+ " failed.  <b>" + this.mErrors + "</b> <FONT color=\"RED\">Error" + (this.mErrors>1?"s":"")+ "</FONT> detected<br>";
			strImg= "AutomationToolbox/Preferences/Templates/Images/RedDot.png";
		}
		if( this.mWarnings > 0 ) {
			summary+= "<b>" + this.mWarnings + "</b> <FONT color=\"ORANGE\">Warning(s)</FONT> were detected<br>";
			if( this.mErrors == 0 )
				strImg= "AutomationToolbox/Preferences/Templates/Images/YellowDot.png";
		}
			
		// Important:  Make sure <H2>Test Summary</H2> and summary are not on the same line so we don't break status page Pass/Fail test
		this._logString("\n</body>\n<footer><br>\n<HR>\n\t<H2><img src=\"/AutoManager/GetImage?"+strImg+"\" height=\"15\" width=\"15\"> Test Summary</H2>\n" + summary + "<br>Elapsed Run Time: " + time + "\n");
		this._logString( this._insertMetricsPerfGraph() );
		this._logString("</footer>\n</html>");
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
	 * @throws Exception 
	 */
	public void _incrErrorCount() throws Exception {
		this.mErrors++;
		this.mTCFailureSet.add(this.mTCCount);
		this._logMetric("ERROR", "1");
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
	 * @throws Exception 
	 */
	public void _incrWarningCount() throws Exception {
		this.mWarnings++;
		this._logMetric("WARNING", "1");
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
    		this.log(TimeUtils.getTime() + "  " + str+"<br>\n");
    	else
    		this.log(str+"<br>\n");
    		
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
            strAnchor= "<A href=\"GetResultData?" +this.getLogDir() + "/" + this.mTransactionLog.getFileName() + "#" + strXLinkID + "\" name=\"" + strXLinkID + "\"><FONT color=\"" + strColor + "\">" + strAnchor + "</FONT></A>\n";        	
        }
        return strAnchor;
    }
    
    /**
     * Writes a Metric element to the log file with a <Metric> tag.
     *
     * @param str the string to be written
     * @throws Exception
     */
    public void _logMetric(String strType, String strData) throws Exception
    {
    	if( !this.mMetrics.containsKey(strType) )
    		this.mMetrics.put(strType, new ArrayList<Metric>());

    	this.mMetrics.get(strType).add( new Metric(strData) );
    	this._logString("<Metric timestamp=\"" + TimeUtils.getDateTime() + "\" type=\"" + strType + "\" data=\"" + strData + "\"></Metric>\n");
    }

    /**
     * 
     * @param pTestbed
     * @throws Exception
     */
    public void _logTestbedSystemMetrics( Testbed pTestbed ) throws Exception {
    	SystemInfo sysInfo= pTestbed._SysInfo()._getSystemInfo();
    	this._logMetric("MemUsed", String.valueOf(sysInfo.mMemUsed));
    	this._logMetric("SysTime", String.valueOf(sysInfo.mSysTime));
    }
    
	/**
	 * 
	 * @return
	 */
	private String _insertMetricsPerfGraph() {
		Map<String, Integer> yAxis= new HashMap<String, Integer>();
		yAxis.put("testcase", 0);
		yAxis.put("MemUsed", 1);
		yAxis.put("SysTime", 2);
		yAxis.put("ERROR", 3);
		yAxis.put("WARNING", 3);
		
		String strSeriesData= "";
		for( String strMetric: this.mMetrics.keySet() ) {
			strSeriesData+= "{\n  name: '"+strMetric+"',\n" + 
							"  yAxis: "+yAxis.get(strMetric)+",\n";
			if( strMetric.equals("ERROR"))
				strSeriesData+="  type: 'column',\n  color: 'red',\n";
			else if( strMetric.equals("WARNING"))
				strSeriesData+="  type: 'column',\n  color: 'orange',\n";

			strSeriesData+=	"  data: [ ";
			for( Metric m : this.mMetrics.get(strMetric) )
				strSeriesData+= String.format("[Date.UTC(%s), %s],", m._JSUTC(), m.m_strData ); // [Date.UTC(2015, 5, 17, 8, 55, 42, 559), 1]
			strSeriesData+= " ]\n},";
		}
					
		return (
		"<hr>\n" +
		"<script type=\"text/javascript\" src=\"GetResource?AutomationToolbox/Preferences/Templates/Resources/jquery.js\"></script>\n" +
		"<script type=\"text/javascript\" src=\"GetResource?AutomationToolbox/Preferences/Templates/Resources/highcharts/highcharts.js\"></script>\n" +
		"<script type=\"text/javascript\" src=\"GetResource?AutomationToolbox/Preferences/Templates/Resources/highcharts/exporting.js\"></script>\n" +
		"<script>\n" +
		"    $(function () {\n" +
		"      $('#metricsgraph').highcharts({\n" +
		"           chart: { type: 'spline', zoomType: 'xy' },\n" +
		"           title: { text: 'Performance Metrics' },\n" +
		"           subtitle: { text: 'Source: Testcases' },\n" +
		"           xAxis: { title: { enabled:true,\n" +
		"                    		  text: 'TOD' },\n" +
		"           		 type: 'datetime' },\n" +
		"           yAxis: [ { title: { text: 'Testcase' },\n" +
		"                      labels: { formatter: function () { return this.value; } } },\n" +
		"                    { title: { text: 'Memory' },\n" +
		"                      labels: { formatter: function () { \n" +
		"                                   var maxElement = this.axis.max;\n" +
        "                                   if (maxElement > 1000000000) {\n" +
        "                                       return (this.value / 1000000000).toFixed(1) + \" GB\";\n" +
        "                                   } else if (maxElement > 1000000) {\n" +
        "                                       return (this.value / 1000000).toFixed(1) + \" MB\";\n" +
        "                                   } else if (maxElement > 1000) {\n" +
        "                                       return (this.value / 1000).toFixed(1) + \" KB\";\n" +
        "                                   } else {\n" +
        "                                       return (this.value) + \" B\";\n" +
        "                                };\n" +
		"                              } } },\n" +
		"                    { title: { text: 'CPU' },\n" +
		"                      labels: { formatter: function () { return this.value + '%'; } } },\n" +
		"                    { title: { text: '' },\n" +
		"                      min:0, max:1,\n" +
		"                      labels: { enabled: false } } ],\n" +
		"           tooltip: { crosshairs: true,\n" +
		"                      formatter: function () { var d= new Date(this.x);\n" +
		"                                 				var strSuffix= Highcharts.numberFormat(this.y, 0, \".\", \",\");\n" +
		"                                 				if( this.series.name.indexOf(\"testcase\") > -1 )\n" +
		"                                 					strSuffix= \"Testcase \" + Highcharts.numberFormat(this.y, 0, \".\", \",\");\n" +
		"                                 				else if( this.series.name.indexOf(\"SysTime\") > -1 )\n" +
		"                                 					strSuffix= Highcharts.numberFormat(this.y, 2, \".\", \",\") + \"% CPU\";\n" +
		"                                 				else if( this.series.name.indexOf(\"MemUsed\") > -1 )\n" +
		"                                 					strSuffix= Highcharts.numberFormat(this.y, 0, \".\", \",\") + \" bytes\";\n" +
		"\n" +
		"                                 				return  '<b>' + this.series.name + '</b><br><b>' + strSuffix + '</b> on ' + d.toUTCString().replace(' GMT', \".\" + d.getMilliseconds()); } },\n" +
		"            plotOptions: { spline: { marker: { radius: 3, lineColor: '#666666', lineWidth: 1 } } },\n" +
		"            series: ["+strSeriesData+"]\n" +
		"        });\n" +
		"    });\n" +
		"</script>\n" +
		"<div id=\"metricsgraph\" style=\"min-width: 310px; height: 400px; margin: 0 auto\"></div>\n"
		);
	}
	
    /**
     * Metric helper class
     * @author terryskotz
     *
     */
    public class Metric {
    	public final Date 	m_Date;
    	public final String m_strData;
    	
    	public Metric(String strData) {
    		Calendar c = Calendar.getInstance(); 
    		c.setTime(new Date()); 
    		c.add(Calendar.MONTH, -1); //JS UTC is 0 based for month so subtract a month
    		this.m_Date= c.getTime();
    		this.m_strData= strData;
    	}

    	public String _JSUTC() {
    		return new SimpleDateFormat("yyyy, MM, dd, hh, mm, ss, SSS").format( this.m_Date );
    	}
    }

}
