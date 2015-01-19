package iZomateCore.iZTests;

import iZomateCore.TestCore.Test;
import iZomateCore.TestCore.TestCaseParameters;
import iZomateCore.UtilityCore.TimeUtils;

import javax.sound.midi.ShortMessage;
import java.util.List;

/**
 * Plugin Performance Test
 * Opens BreakTweaker in a specified host, presses play, cycles thru presets (if found), stops play, captures and reports plugin profile performance data
 * @author tskotz, aford
 *
 */
public class BreakTweakerTest extends Test {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception	{
		new BreakTweakerTest( args ).run();
	}

	/**
	 * Constructor
	 * @param args command line args
	 * @throws Exception
	 */
	protected BreakTweakerTest( String[] args ) throws Exception {
		super( args );
	}

	/**
	 * Gets called only once before the test cases are run
	 */
	@Override
	protected void _StartUp( TestCaseParameters pCommonParameters ) throws Exception {
		System.out.println( "StartUp!" );
	}

    @Override
    protected void _SetupTestCase( TestCaseParameters pParams )
            throws Exception {
        this._Testbed( pParams._GetTestbed() )._HostApp( pParams._GetApp() )._Actions()._Launch( 5, null, pParams._GetForceNewInstanceOnStart(), pParams._GetHideAllWinAtStart() );
    }

	/**
	 * Gets called for each test case defined in test input file
	 */
	@Override
	protected void _TestCase( TestCaseParameters pParams ) throws Exception {

		this._Testbed()._HostApp()._Plugin( pParams._GetPlugin() )._Console()._Perf_EnableLogging( true );
		this._Testbed()._HostApp()._Plugin()._Console()._Perf_ClearLog();

		if( pParams._GetString( "izparamsDir", null) != null )
			this._Logs()._ResultLog()._logLine( "<I>This test will loop through izparams and gather " + pParams._GetPresetPlayTime() + " seconds of play profiling data per preset with a max run time of " + pParams._GetTestDuration() + " seconds.</I>\n" );
		else
			this._Logs()._ResultLog()._logLine( "<I>This test will loop through presets and gather " + pParams._GetPresetPlayTime() + " seconds of play profiling data per preset with a max run time of " + pParams._GetTestDuration() + " seconds.</I>\n" );

		boolean bStartPlay= true;

		// Start Cycling through izparams or presets
		if( pParams._GetString( "izparamsDir", null) != null ) {
			List<String> striZParams= this._Testbed()._HostApp()._Plugin()._Actions()._GetiZParams( pParams._GetString( "izparamsDir"), true );
			int nDuration= pParams._GetInt( "testDuration", -1 );
			long lStopTime_ms= nDuration*1000 + System.currentTimeMillis();
			int nPatterns= 8;
			double nSleepTime= (double)pParams._GetPresetPlayTime() / nPatterns;
				
			// Start cycling through izparams
			do {
				for( String s : striZParams ) {
					this._Testbed()._HostApp()._Plugin()._Actions()._LoadiZParamsAndLog( s );
					
					// Switch through patterns via MIDI (C4+)
					// Gate is default so hold the note for the length of the test
					for( int iPattern= 0; iPattern < nPatterns; iPattern++ ) {
						int nNote= 60 + iPattern;
						this._Testbed()._HostApp()._sendMidiNotes( nNote, 1, ShortMessage.NOTE_ON );
						TimeUtils.sleep( nSleepTime );
						this._Testbed()._HostApp()._sendMidiNotes( nNote, 1, ShortMessage.NOTE_OFF );
					}
					
					long lTimePreLog= System.currentTimeMillis();
					this._Testbed()._HostApp()._GetProcessInfo("\tMemory Check", true);
					this._Testbed()._HostApp()._Plugin()._Profilers()._LogToResultsFile();
					// Add some extra time to our stop time - don't include the time to log since that might take a while depending on buffer size!
					lStopTime_ms= lStopTime_ms + (System.currentTimeMillis() - lTimePreLog);
					
					if( nDuration >= 0 && System.currentTimeMillis() >= lStopTime_ms )
						break;
				}
			} while ( System.currentTimeMillis() < lStopTime_ms );
		}
		else // use presets
			this._Testbed()._HostApp()._Plugin()._Actions()._CycleThruPresets( pParams._GetInt( "testDuration", -1 ), pParams._GetPresetPlayTime() );

		// Stop play
		this._Testbed()._HostApp()._sendMidiNotes( 48, 1, ShortMessage.NOTE_OFF );

		if( pParams._GetQuitWhenComplete() )
			this._Testbed()._HostApp()._Quit( 20, true );

		this._Testbed()._HostApp()._LogRunTime();
		this._Testbed()._HostApp()._Plugin()._CreateGraphs( pParams );
	}

	/**
	 * Gets called if an exception is caught by the Test base class while processing _TestCase
	 */
	@Override
	protected void _OnTestCaseException( TestCaseParameters pTestcaseParameters, Exception e ) throws Exception {
		if( pTestcaseParameters._GetQuitWhenComplete() )
			this._Testbed()._HostApp()._Quit( 20, true );
		
		this._Testbed()._DismissCrashReporter( pTestcaseParameters._GetSubmitCrashReport() );
	}

	/**
	 * Gets called only once after all test cases have been run.
	 */
	@Override
	protected void _ShutDown( TestCaseParameters pCommonParameters ) throws Exception {				
		// Make sure the app has quit if QuitWhenComplete is true.
		// TODO: If I can figure out how to resolve the funky interaction with jfreechart I can put this back to performing the regular quit here.
		if( pCommonParameters._GetQuitWhenComplete() )
			this._Testbed()._ForceQuit( this._Testbed()._HostApp()._HostAppFile()._getName(), 10 );
		
		this._Testbed()._DismissCrashReporter( pCommonParameters._GetSubmitCrashReport() );
	}

}
