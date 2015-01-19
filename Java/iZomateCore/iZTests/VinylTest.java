package iZomateCore.iZTests;

import iZomateCore.TestCore.Test;
import iZomateCore.TestCore.TestCaseParameters;
import iZomateCore.UtilityCore.TimeUtils;

public class VinylTest extends Test{
	
	protected VinylTest(String[] args) throws Exception {
		super(args);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception	{
		new VinylTest( args ).run();
	}

	/**
	 * Gets called only once before the test cases are run
	 */
	@Override
	protected void _StartUp( TestCaseParameters pCommonParameters ) throws Exception {
		System.out.println( "StartUp!" );
		this._Testbed( pCommonParameters._GetTestbed() )._HostApp( pCommonParameters._GetApp() )._Actions()._Launch( 3, null, pCommonParameters._GetForceNewInstanceOnStart(), pCommonParameters._GetHideAllWinAtStart() );
		this._Testbed()._HostApp()._Plugin( pCommonParameters._GetPlugin() )._Console()._Perf_EnableLogging( true );
	}

    @Override
    protected void _SetupTestCase( TestCaseParameters pTestcaseParameters )
            throws Exception {
        // TODO Auto-generated method stub

    }

	/**
	 * Gets called for each test case defined in test input file
	 */
	@Override
	protected void _TestCase( TestCaseParameters pParams ) throws Exception {
		this._Testbed()._HostApp()._Plugin()._Console()._Perf_ClearLog();
		this._Testbed()._HostApp()._Actions()._LoadAudioFile( pParams._GetAudioFile(), false, true );
		
		// Start Play
		this._Testbed()._HostApp()._Actions()._StartPlay();

		// Set up our UI
		this._Testbed()._HostApp()._Plugin()._Actions()._VinylConfigureUI( pParams );
		this._Logs()._ResultLog()._logScreenShot( "Screenshot: UI Config" );
		
		TimeUtils.sleep( pParams._GetPresetPlayTime() );

		// Stop play
		this._Testbed()._HostApp()._Actions()._StopPlay();

		this._Testbed()._HostApp()._GetProcessInfo("\tMemory Check", true);
		this._Testbed()._HostApp()._Plugin()._Profilers()._LogToResultsFile();
	}

	/**
	 * Gets called if an exception is caught by the Test base class while processing _TestCase
	 */
	@Override
	protected void _OnTestCaseException( TestCaseParameters pTestcaseParameters, Exception e ) throws Exception {
		if( pTestcaseParameters._GetQuitWhenComplete() )
			this._Testbed()._HostApp()._Quit( 20, true );
	}

	/**
	 * Gets called only once after all test cases have been run.
	 */
	@Override
	protected void _ShutDown( TestCaseParameters pCommonParameters ) throws Exception {				
		this._Testbed()._HostApp()._LogRunTime();
		this._Testbed()._HostApp()._Plugin()._CreateGraphs( pCommonParameters );
		// Make sure the app has quit if QuitWhenComplete is true.
		// TODO: If I can figure out how to resolve the funky interaction with jfreechart I can put this back to performing the regular quit here.
		if( pCommonParameters._GetQuitWhenComplete() )
			this._Testbed()._ForceQuit( this._Testbed()._HostApp()._HostAppFile()._getName(), 10 );
	}

}
