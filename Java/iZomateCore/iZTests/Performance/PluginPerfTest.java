package iZomateCore.iZTests.Performance;

import iZomateCore.TestCore.Test;
import iZomateCore.TestCore.TestCaseParameters;

/**
 * Plugin Performance Test
 * Opens a plugin in a specified host, loads an audio file (if specified), presses play, cycles thru presets (if found), stops play, captures and reports plugin profile performance data
 * @author tskotz
 *
 */
public class PluginPerfTest extends Test {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception	{
		new PluginPerfTest( args ).run();
	}

	/**
	 * Constructor
	 * @param args command line args
	 * @throws Exception
	 */
	protected PluginPerfTest( String[] args ) throws Exception {
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
    protected void _SetupTestCase( TestCaseParameters pParams ) throws Exception {
        this._Testbed( pParams._GetTestbed() )._HostApp( pParams._GetApp() )._Actions()._Launch( 5, null, pParams._GetForceNewInstanceOnStart(), pParams._GetHideAllWinAtStart() );
        this._Testbed()._HostApp()._Plugin( pParams._GetPlugin() )._Console()._Perf_EnableLogging( true );
    }

	/**
	 * Gets called for each test case defined in test input file
	 */
	@Override
	protected void _TestCase( TestCaseParameters pParams ) throws Exception {		


		this._Testbed()._HostApp()._Plugin()._Console()._Perf_ClearLog();

		if( pParams._GetString( "izparamsDir", null) != null )
			this._Logs()._ResultLog()._logLine( "<I>This test will loop through izparams and gather " + pParams._GetPresetPlayTime() + " seconds of play profiling data per preset with a max run time of " + pParams._GetTestDuration() + " seconds.</I>\n" );
		else
			this._Logs()._ResultLog()._logLine( "<I>This test will loop through presets and gather " + pParams._GetPresetPlayTime() + " seconds of play profiling data per preset with a max run time of " + pParams._GetTestDuration() + " seconds.</I>\n" );

		this._Testbed()._HostApp()._Actions()._LoadAudioFile( pParams._GetAudioFile(), false, true );

		// Start Play
		if( this._Testbed()._HostApp()._Plugin()._GetPluginInfo().m_nCodeModDate >= 20130823 )
			this._Testbed()._HostApp()._Actions()._StartPlay(); //Loop play not working right yet --> this._Testbed()._HostApp()._Actions()._StartLoopPlay();
		else
			this._Testbed()._HostApp()._Actions()._StartPlay();

		// Start Cycling through izparams or presets
		if( pParams._GetString( "izparamsDir", null) != null )
			this._Testbed()._HostApp()._Plugin()._Actions()._CycleThruiZParams( pParams._GetString( "izparamsDir"), pParams._GetInt( "testDuration", -1 ), pParams._GetPresetPlayTime() );
		else // use presets
			this._Testbed()._HostApp()._Plugin()._Actions()._CycleThruPresets( pParams._GetInt( "testDuration", -1 ), pParams._GetPresetPlayTime() );

		// Stop play
		this._Testbed()._HostApp()._Actions()._StopPlay();

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
		this._Testbed()._DismissCrashReporter( pTestcaseParameters._GetSubmitCrashReport() );

		if( pTestcaseParameters._GetQuitWhenComplete() ) {
			try {
				this._Testbed()._HostApp()._Quit( 20, true );
			} catch( Exception e2 ) {
				this._Testbed()._ForceQuit( this._Testbed()._HostApp()._HostAppFile()._getName(), 10 );	
			}
		}
	}

	/**
	 * Gets called only once after all test cases have been run.
	 */
	@Override
	protected void _ShutDown( TestCaseParameters pCommonParameters ) throws Exception {				
		this._Testbed()._DismissCrashReporter( pCommonParameters._GetSubmitCrashReport() );

		// Make sure the app has quit if QuitWhenComplete is true.
		// TODO: If I can figure out how to resolve the funky interaction with jfreechart I can put this back to performing the regular quit here.
		if( pCommonParameters._GetQuitWhenComplete() )
			this._Testbed()._ForceQuit( this._Testbed()._HostApp()._HostAppFile()._getName(), 10 );		
	}

}
