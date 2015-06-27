package iZomateCore.iZTests.Performance;

import iZomateCore.AppCore.Callbacks.CustomDialogCallback;
import iZomateCore.AppCore.WindowControls.ButtonState;
import iZomateCore.AppCore.WindowControls.WidgetInfo;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.TestCore.Test;
import iZomateCore.TestCore.TestCaseParameters;
import iZomateCore.UtilityCore.TimeUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Vector;

/**
 * Plugin Performance Test
 * Opens a plugin in a specified host, loads an audio file (if specified), presses play, cycles thru presets (if found), stops play, captures and reports plugin profile performance data
 * @author tskotz
 *
 */

public class RXPerformanceTest extends Test {
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception	{
		new RXPerformanceTest( args ).run();
	}

	/**
	 * Constructor
	 * @param args command line args
	 * @throws Exception
	 */
	protected RXPerformanceTest( String[] args ) throws Exception {
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
    }

	/**
	 * Gets called for each test case defined in test input file
	 */
	@Override
	protected void _TestCase( TestCaseParameters pParams ) throws Exception {		

		TimeUtils.sleep( .5 ); // Let it catch it's breath	
		this._Testbed()._HostApp()._Plugin( pParams._GetPlugin() )._Console()._Perf_EnableLogging( true );
		this._Testbed()._HostApp()._Plugin()._Console()._Perf_ClearLog();
		this._Logs()._ResultLog()._logLine( "This test will loop through presets and gather " + pParams._GetPresetPlayTime() + " seconds of play profiling data per preset with a max run time of " + pParams._GetTestDuration() + " seconds." );
		
		this._Testbed()._HostApp()._Actions()._LoadAudioFile( pParams._GetAudioFile(), false, true );
	
		// Turn everything off
		for( int i=0; i < 9; ++i )
			this._Testbed()._HostApp()._Plugin()._Controls()._Button( "Element Panel Button " + i )._setState( ButtonState.OFF );
		
		// Select audio samples
		this._Testbed()._HostApp()._Plugin()._Controls()._Button( "Editor Cursor Type 7 Button" )._setState( ButtonState.OFF ); //Grab and drag Tool off
		this._Testbed()._HostApp()._Plugin()._Controls()._Button( "Editor Cursor Type 0 Button" )._setState( ButtonState.ON ); //Time selection tool on
		this._Testbed()._Robot()._keyType( this._Testbed()._SysInfo()._isMac() ? KeyEvent.VK_META : KeyEvent.VK_CONTROL, KeyEvent.VK_D ); //deselect all
		WidgetInfo info= this._Testbed()._HostApp()._Plugin()._Controls()._Button( "EditorView EffectPanel Overlay" )._info();
		Point p= new Point( info.mX + info.mWidth/2, info.mY + info.mHeight/4 );
		this._Testbed()._Robot()._mouseDragAndDrop( p, new Point( p.x+5, p.y ) );
		
		// Start Play
		//this._Testbed()._HostApp()._Actions()._StartPlay();

		// Turn Spectrum analyzer On
		this._Testbed()._HostApp()._Plugin()._Controls()._Button( "Element Panel Button 8" )._setState( ButtonState.ON );

		// Start Cycling through presets
		String[] strModule= new String[]{"Declip", "Declick & Decrackle", "Remove Hum", "Denoise", "Spectral Repair", "Gain", "EQ", "Channel Operations", "Spectrum Analyzer"};
		for( int i=0; i < 8; ++i ) {
			this._Logs()._ResultLog()._logLine( "<H4>Testing Submodule: " + strModule[i] + ":</H4>");
			this._Testbed()._HostApp()._Plugin()._Controls()._Button( "Element Panel Button " + i )._setState( ButtonState.ON );
			Vector<String> presets= this._Testbed()._HostApp()._Plugin()._Controls()._ComboBox( strModule[i] + " Preset Manager|Preset ComboBox" )._info().mCBoxItems;
			for( String preset : presets )
				this._Testbed()._HostApp()._Plugin()._Actions()._ChangeRXPreset( preset, strModule[i] );
		}
		
		for( int i=8; i >= 0; --i )
			this._Testbed()._HostApp()._Plugin()._Controls()._Button( "Element Panel Button " + i )._setState( ButtonState.OFF );
		
		// Stop play
		//this._Testbed()._HostApp()._Actions()._StopPlay();

		if( pParams._GetQuitWhenComplete() ) {
			this._Testbed()._Robot()._keyType( this._Testbed()._SysInfo()._isMac() ? KeyEvent.VK_META : KeyEvent.VK_CONTROL, KeyEvent.VK_W );
			this._Testbed()._HostApp()._GetAppServer()._waitForEvent( EventSubType.DLOGDismissed, null, new CustomDialogCallback( "Save Changes", null, this._Testbed()._SysInfo()._isMac() ? KeyEvent.VK_SPACE : KeyEvent.VK_N, this._Testbed()._HostApp() ), 5 );
			this._Testbed()._HostApp()._Quit( 20, true );
		}
		
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
