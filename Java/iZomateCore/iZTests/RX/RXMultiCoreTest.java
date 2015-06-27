package iZomateCore.iZTests.RX;

import iZomateCore.TestCore.TestCaseParameters;
import iZomateCore.UtilityCore.TimeUtils;

/**
 * MultiCore Performance Test * 
 * @author jhobbs
 *
 */  
public class RXMultiCoreTest extends RXAppTest {
	public enum TestResult { TEST_PASSED, TEST_WARNING, TEST_FAILED }
	
	// Time Table with our Re
	String htmlTimingTable= "";	// A full HTML table of our results
	String htmlTimingRow=""; 	// Equals last row. Used when bLogTimingRows == true
	boolean bLogTimingRows= true; // Log time table row by row
	
	
	
	/**
	 * 
	 * @param str
	 * @param strClass
	 * @param colspan
	 */
	private void timingTableAddHeaderCell(String str, String strClass, int colspan)	{ 
		this.htmlTimingRow+= "<th class=\"" + strClass + "\" colspan=\"" + colspan + "\">" + str +"</th> ";
		this.htmlTimingTable+= this.htmlTimingRow;
	}
	
	/**
	 * 
	 * @param str
	 * @param strClass
	 */
	private void timingTableAddCell(String str, String strClass ) { 
		this.htmlTimingRow+= "<td class=\"" + strClass +"\">" + str +"</td> ";
		this.htmlTimingTable+= this.htmlTimingRow; 
	}
	
	/**
	 * 
	 * @param i
	 * @param strClass
	 */
	@SuppressWarnings("unused")
	private void timingTableAddCell(int i, String strClass) { 
		this.htmlTimingRow+= "<td class=\"" + strClass +"\">" + String.valueOf(i) +"</td> ";
		this.htmlTimingTable+= this.htmlTimingRow;
	}
	
	/**
	 * 
	 */
	private void timingTableStartRow() { 
		this.htmlTimingTable+= "<tr> "; 
		this.htmlTimingRow= "<tr>"; 
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void timingTableEndRow() throws Exception  { 
		this.htmlTimingRow+= "</tr>\n"; this.htmlTimingTable+= this.htmlTimingRow;
		if( this.bLogTimingRows ) _Plugin()._Logs()._ResultLog()._logString(this.htmlTimingRow);
	}
	
	/**
	 * 
	 * @param f
	 * @param places
	 * @param strClass
	 */
	private void timingTableAddCell(float f, int places, String strClass)	{
		if(places != 0)	this.htmlTimingRow+= "<td class=\"" + strClass +"\">" + String.format("%."+String.valueOf(places)+"f", f) +"</td> ";
		else 			this.htmlTimingRow+= "<td class=\"" + strClass +"\">" + String.format("%f", f) +"</td> ";
		this.htmlTimingTable+= this.htmlTimingRow; 
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception	{
		new RXMultiCoreTest( args ).run();
	}

	/**
	 * Constructor
	 * @param args command line args
	 * @throws Exception
	 */
	protected RXMultiCoreTest( String[] args ) throws Exception {
		super( args );
		this.m_bVerboseLogs= false;
	}

	/**
	 * Gets called only once before the test cases are run
	 */
	@Override
	protected void _onStartUp( TestCaseParameters pCommonParameters ) throws Exception {
		// Start our table
		// Style
		this._Testbed()._HostApp()._Plugin()._Logs()._ResultLog()._logString(
				"<style> " +										
					// Cell Types
					".tttest{ 		text-align:center; background-color:CCCCFF; border-color:DDDDFF;}" +
					".ttprocess{ 	text-align:right;  background-color:CCFFCC; border-color:CCFFCC;}" +
					".ttperfgain{ 	text-align:right;  background-color:FFFFCC; border-color:FFFFCC;}" +
					".ttperfcore{ 	text-align:right;  background-color:CCFFFF; border-color:CCFFFF;}" +
					
					// Overrides					
					//"table{ border-collapse:collapse; } " +					
					".ttred{ background-color:#8B0000; color:white; font-weight:bold; } " +
					".ttgreen{ background-color:006400; color:white; } " +
					".ttyellow{ background-color:FFFF00; color:black; } " +
					
					".ttbottomline{ border-bottom-width:5px; border-bottom-style:ridge; }" +
					"th{ text-align:center; font-weight:bold; }" +
				"</style>\n"); 
		
		this.htmlTimingTable= "<table border=1>\n";
		if( this.bLogTimingRows )
			this._Testbed()._HostApp()._Plugin()._Logs()._ResultLog()._logString("<table border=1>\n");
		
		// First Row
		timingTableStartRow();
			timingTableAddHeaderCell("Test (* Processed was primed)", "tttest", 99);			
		timingTableEndRow();
		
						
		// Print header columns
		timingTableStartRow();
			timingTableAddHeaderCell("DSP", "tttest ttbottomline", 1);
			timingTableAddHeaderCell("Preset", "tttest ttbottomline", 1);
			timingTableAddHeaderCell("AudioFile", "tttest ttbottomline", 1);
			timingTableAddHeaderCell("RX 2", "tttest ttbottomline", 1);
			
			timingTableAddHeaderCell("Init", "ttprocess ", 1);			
			timingTableAddHeaderCell("Process<br/>Time", "ttprocess", 1);
			timingTableAddHeaderCell("Perf<br/>Gain", "ttperfgain", 1);				
		timingTableEndRow();
			
	}
	
	/**
	 * Gets called for each test case defined in test input file
	 */
	@Override
	protected void _TestCase( TestCaseParameters pParams ) throws Exception {
	try {
		timingTableStartRow();
		String strModule= pParams._GetString("module");
		boolean bPrimeProcess= pParams._GetBool("run_priming_process", false);
					
		timingTableAddCell(strModule + (bPrimeProcess ? "*" : ""),"tttest");
		timingTableAddCell(pParams._GetString("modulePreset"),"tttest");
		timingTableAddCell(pParams._GetString("audioFile"),"tttest");
				
		// Add our RX2 Base Line.
		float baselineSeconds= 0.0f;				
		if( pParams._GetString("rx2_baseline_hms", null) != null ) {
			baselineSeconds= TimeUtils._HmsToSeconds(pParams._GetString("rx2_baseline_hms"));			
			timingTableAddCell(baselineSeconds, 2, "tttest");			
		} else {
			timingTableAddCell("---", "tttest");
		}	
		
		// Make sure there aren't any files already loaded because reloading the same file changes the expected notifications during load file causing test to fail
		this._Testbed()._HostApp()._Actions()._UnloadAudioFile();			

		// Process
		float initMillis= 0.0f;
		if( bPrimeProcess ) {
			_LoadFileAndProcess( pParams );
			initMillis= _GetProcessInitMillis();
		}
			
		float processTime= _LoadFileAndProcess( pParams );
		if( initMillis == 0.0f ) initMillis= _GetProcessInitMillis();
		timingTableAddCell(initMillis, 2, "tttest" );
		
		float fPercentBetter= (processTime != 0.0f) ? baselineSeconds / processTime : 99.99f;
		TestResult result= TestResult.TEST_PASSED;
		if( baselineSeconds == 0.0f ) 								result= TestResult.TEST_PASSED;
		else if( baselineSeconds > 2.0f && processTime == 0.00f ) 	result= TestResult.TEST_FAILED; // 0ms process may be due to the low resolution of the windows time. Fail if we report 0 for something that should definitely not be (aka from a bug in TA or RX)
		else if( processTime > 1.0f && fPercentBetter < 0.80f ) 	result= TestResult.TEST_FAILED;	 	// Quick processing are ignored
		else if( fPercentBetter < 1.0f ) 							result= TestResult.TEST_WARNING;	// Warn if we are between 80% and 100%
				
		String resultColor= result == TestResult.TEST_PASSED ? "ttgreen" :  result == TestResult.TEST_WARNING ? "ttyellow" : "ttred";
		timingTableAddCell(processTime, 2, "ttprocess " + resultColor );
		
		// PercentBetter 
		if( baselineSeconds == 0.0f)
			timingTableAddCell("---", "ttperfgain");
		else  {
			timingTableAddCell( fPercentBetter, 2, "ttperfgain " + resultColor );		
		}

		// End
		timingTableEndRow();
		String strResult= "Test '" + pParams._GetString("testcaseName", "") + "' "; 
		if( result == TestResult.TEST_FAILED )
			this._Logs()._ResultLog()._logError(strResult + "failed.", false);
		else if( result == TestResult.TEST_FAILED )
			this._Logs()._ResultLog()._logWarning(strResult + "has a warning.");
				
		} catch( Exception e) {
			this._Logs()._ResultLog()._LogException(e, false);
			this._Logs()._ResultLog()._incrErrorCount();
		}
		
	}


    /**
	 * Gets called if an exception is caught by the Test base class while processing _TestCase
	 */
	@Override
	protected void _OnTestCaseException( TestCaseParameters pTestcaseParameters, Exception e ) throws Exception {		
		this._Testbed()._DismissCrashReporter( pTestcaseParameters._GetSubmitCrashReport() );
		this._Logs()._ResultLog()._logLine( "\n" );
	}

	/**
	 * Gets called only once after all test cases have been run.
	 */
	@Override
	protected void _onShutDown( TestCaseParameters pCommonParameters ) throws Exception {
		this._Testbed()._HostApp()._LogRunTime();
		
		this._Logs()._ResultLog()._logLine( "<HR></pre>" );

		
		// End our table
		this.htmlTimingTable= "</table>\n";
		if( this.bLogTimingRows )
			_Logs()._ResultLog()._logString("</table>\n");
		else {		
			_Plugin()._Logs()._ResultLog()._logString(this.htmlTimingTable);
		}
		this._Testbed()._DismissCrashReporter( pCommonParameters._GetSubmitCrashReport() );
	}

}
