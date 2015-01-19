package iZomateCore.iZTests.RX;

import iZomateCore.AppCore.AppEnums.RXModule;
import iZomateCore.AppCore.AppEnums.SAHookMenuBar;
import iZomateCore.AppCore.AppEnums.WindowControls.Buttons;
import iZomateCore.AppCore.AppEnums.WindowControls.TextEdits;
import iZomateCore.AppCore.Callbacks.CustomDialogCallback;
import iZomateCore.AppCore.Callbacks.EditBoxCallback;
import iZomateCore.AppCore.Callbacks.EventIntHolderCallback;
import iZomateCore.AppCore.Callbacks.RXMarkersAndRegionsWindowCallback;
import iZomateCore.AppCore.Plugin;
import iZomateCore.AppCore.WindowControls;
import iZomateCore.AppCore.WindowControls.ButtonState;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.RPCServer.RemoteServer.RemoteFile;
import iZomateCore.TestCore.Test;
import iZomateCore.TestCore.TestCaseParameters;
import iZomateCore.UtilityCore.TimeUtils;

import java.awt.event.KeyEvent;

public abstract class RXAppTest extends Test {

	//Hack for RX3 because edit boxes can activate themselves right after Process/Learn which blocks our Mouse event
	EditBoxCallback m_EditBoxHandlerCB= null;
	EventIntHolderCallback 	m_finishedTrainingCallback= null;
	EventIntHolderCallback 	m_finishedProcessingCallback= null;
	EventIntHolderCallback 	m_sessionLoadedCallback= null;
	EventIntHolderCallback 	m_initializedModuleCallback= null;
	
	private float	m_lastModuleInitializedTime= 0.0f;
	
	/******************************************************************
	 * Constructor 
	 */
	protected RXAppTest(String[] args) throws Exception {
		super(args);		
	}
	
	/******************************************************************
	 *  
	 */
	protected void _StartUp( TestCaseParameters pCommonParameters ) throws Exception {
		System.out.println( "StartUp!" );
		this._Testbed( pCommonParameters._GetTestbed() )._HostApp( pCommonParameters._GetApp() )._Actions()._Launch( 5, null, pCommonParameters._GetForceNewInstanceOnStart(), pCommonParameters._GetHideAllWinAtStart() );
		this._Testbed()._HostApp()._Plugin( pCommonParameters._GetPlugin() );
		this._Testbed()._HostApp()._GetAppServer()._RegisterNotificationCallback( new CustomDialogCallback( "Save Changes", null, this._Testbed()._SysInfo()._isMac() ? KeyEvent.VK_SPACE : KeyEvent.VK_N, this._Testbed()._HostApp() ) );
	
		//Hack for RX3 because edit boxes can activate themselves right after Process/Learn which blocks our Mouse event
		this.m_EditBoxHandlerCB= 			new EditBoxCallback( "\n", this._Testbed()._HostApp() );
		this.m_finishedTrainingCallback= 	new EventIntHolderCallback( EventSubType.FinishedTraining );
		this.m_finishedProcessingCallback= 	new EventIntHolderCallback( EventSubType.FinishedProcessing );
		this.m_sessionLoadedCallback= 		new EventIntHolderCallback( EventSubType.OpenedFile );
		this.m_initializedModuleCallback= 	new EventIntHolderCallback( EventSubType.InitializedModule );			
		
		this._Testbed()._HostApp()._GetAppServer()._RegisterNotificationCallback( this.m_EditBoxHandlerCB );
		this._Testbed()._HostApp()._GetAppServer()._RegisterNotificationCallback( this.m_sessionLoadedCallback );
		TimeUtils.sleep( 0.5 );
		
		// Turn everything off
		for( RXModule rxModule : RXModule.values() )
			_Controls()._Button( rxModule._getButtonID() )._setState( ButtonState.OFF );
		
		// Select proper tools
		// FIXME: Selection stuff is in flux and currently not used.
		//_Controls()._Button( "Editor Cursor Type 7 Button" )._setState( ButtonState.OFF ); //Grab and drag Tool off
		//_Controls()._Button( "Editor Cursor Type 0 Button" )._setState( ButtonState.ON ); //Time selection tool on

        if( pCommonParameters._GetPlugin() != null ) {
		    this._Testbed()._HostApp()._Plugin( pCommonParameters._GetPlugin() )._Console()._Perf_EnableLogging( true );
		    _Plugin()._Console()._Perf_ClearLog();
        }
		if( this.m_sessionLoadedCallback._GetMessage() != null && this.m_sessionLoadedCallback._GetMessage().contains( "Session reopened successfully" ))
			this._Testbed()._HostApp()._Actions()._UnloadAudioFile();
		//this._Logs()._ResultLog()._logLine( "This test will loop through presets and gather " + pParams._GetPresetPlayTime() + " seconds of play profiling data per preset with a max run time of " + pParams._GetTestDuration() + " seconds." );
				
		
		_onStartUp( pCommonParameters );
	}

    protected void _SetupTestCase( TestCaseParameters pCommonParameters ) throws Exception {
        // do nothing...yet
    }


	/******************************************************************
	 *  
	 */
	protected final void _ShutDown( TestCaseParameters pCommonParameters ) throws Exception {
        System.out.println( "Shutdown!" );
		_onShutDown( pCommonParameters );

        if( pCommonParameters._GetQuitWhenComplete() )
            this._Testbed()._HostApp()._Quit( 5, true );
		
		try {// Delete Session directory			
			String strSessionDir= this._Testbed()._RemoteServer()._SysInfo()._GetUserDir();
			if( this._Testbed()._SysInfo()._isWin() )
				strSessionDir+= "/AppData/Roaming/iZotope/";
			else
				strSessionDir+= "/Library/Application Support/iZotope/";
			
			if( this._Testbed()._HostApp()._HostType()._GetPluginName() == "RX3" ) {
				strSessionDir+= "iZotope RX 3 Session Data/";
			} else if( this._Testbed()._HostApp()._HostType()._GetPluginName() == "RX4" ) {
				strSessionDir+= "iZotope RX 4 Session Data/";
			}
			
			RemoteFile fDir= this._Testbed()._CreateRemoteFile(  strSessionDir );
			fDir._deleteContents(false);			
		} catch( Exception e) {
			// do nothing
		}
	}
	
	/******************************************************************
	 * Overrides needed for the derived classes
	 */
	protected abstract void _onStartUp( TestCaseParameters pCommonParameters ) throws Exception;  
	protected abstract void _onShutDown( TestCaseParameters pCommonParameters ) throws Exception;
		
	/******************************************************************
	 *  
	 */
	float _GetProcessInitMillis() {
		return this.m_lastModuleInitializedTime;
	}
	
	/******************************************************************
	 * Helpers 
	 */
	public WindowControls _Controls() throws Exception { return _Plugin()._Controls();}	
	public Plugin _Plugin() throws Exception { return _Testbed()._HostApp()._Plugin(); }
			
	
	/******************************************************************
	 * Set A Selection
	 */
	public void _SetSelection( TestCaseParameters pParams ) throws Exception {
		try {
		if( this.m_EditBoxHandlerCB == null) throw new Exception("Need to call _SetCallbacks()");
		
		// Dont select anything if we dont have a start
		if( pParams._GetString( "selectionStart hms", null ) == null || pParams._GetString( "selectionStart hz", null ) == null ) return;
		
		// Set Selection 
		this._Testbed()._HostApp()._GetAppServer()._UnregisterNotificationCallback( this.m_EditBoxHandlerCB );
		_Controls()._TextEdit( TextEdits.SelectionStartTime )._SetText( pParams._GetString( "selectionStart hms" ) );
		_Controls()._TextEdit( TextEdits.SelectionEndTime )  ._SetText( pParams._GetString( "selectionEnd hms" ) );
		_Controls()._TextEdit( TextEdits.SelectionLength )   ._SetText( pParams._GetString( "selectionLength hms" ) );
		_Controls()._TextEdit( TextEdits.SelectionStartFreq )._SetText( pParams._GetString( "selectionStart hz" ) );
		_Controls()._TextEdit( TextEdits.SelectionEndFreq )  ._SetText( pParams._GetString( "selectionEnd hz" ) );
		_Controls()._TextEdit( TextEdits.SelectionRangeFreq )._SetText( pParams._GetString( "selectionLength hz" ) );
		this._Testbed()._HostApp()._GetAppServer()._RegisterNotificationCallback( this.m_EditBoxHandlerCB );
		
		} catch( Exception e) {
			this._Logs()._ResultLog()._LogException(e, true);
			throw new Exception( e.getMessage() );
		}
	}

	/******************************************************************
	 * Train. Returns learning time
	 * Not tested yet. Taken from ProcessFile test
	 */
	 public float _Train( TestCaseParameters pParams ) throws Exception {
		try {
		if( this.m_EditBoxHandlerCB == null || this.m_finishedTrainingCallback == null ) throw new Exception("Need to call _SetCallbacks()" );
		
		if( pParams._GetString("trainingType", null ) == null || pParams._GetString("trainingType", null ).equals( "none" ) ) return 0;
		
		if( !(pParams._GetString("trainingType" ).equals( "region" ) || pParams._GetString( "trainingType" ).equals( "selection" )) ) 
			return 0;
		
		// Set Training Region
		if( pParams._GetString( "trainingType" ).equals( "region" ) )
			this._Testbed()._HostApp()._Actions()._SAHookMenuBar( SAHookMenuBar.RX_View_Markers_And_Regions, 
																  new RXMarkersAndRegionsWindowCallback( pParams._GetString( "trainingRegion" ), this._Testbed()._HostApp() ) );
		else if( pParams._GetString( "trainingType" ).equals( "selection" ) ) {
			// Set Training Selection 
			this._Testbed()._HostApp()._GetAppServer()._UnregisterNotificationCallback( this.m_EditBoxHandlerCB );
			_Controls()._TextEdit( TextEdits.SelectionStartTime )._SetText( pParams._GetString( "trainingStart hms" ) );
			_Controls()._TextEdit( TextEdits.SelectionEndTime )  ._SetText( pParams._GetString( "trainingEnd hms" ) );
			_Controls()._TextEdit( TextEdits.SelectionLength )   ._SetText( pParams._GetString( "trainingLength hms" ) );
			_Controls()._TextEdit( TextEdits.SelectionStartFreq )._SetText( pParams._GetString( "trainingStart hz" ) );
			_Controls()._TextEdit( TextEdits.SelectionEndFreq )  ._SetText( pParams._GetString( "trainingEnd hz" ) );
			_Controls()._TextEdit( TextEdits.SelectionRangeFreq )._SetText( pParams._GetString( "trainingLength hz" ) );
			this._Testbed()._HostApp()._GetAppServer()._RegisterNotificationCallback( this.m_EditBoxHandlerCB );
		}
	
		// Perform Training
		_Controls()._Button( RXModule._getEnum( pParams._GetString( "module" ) )._getButtonID() )._setState( ButtonState.ON ); // Open Module
		_Controls()._ComboBox( pParams._GetString( "module" ) + " Preset Manager|Preset ComboBox" )._Select( pParams._GetString( "modulePreset" ) ); // Select preset
		_Controls()._Button( "EffectPanel " + pParams._GetString( "module" ) + "|" + Buttons.Module_LearnButton._getValue() )._SetCallback( this.m_finishedTrainingCallback )._click(); // Learn
		_Controls()._Button( RXModule._getEnum( pParams._GetString( "module" ) )._getButtonID() )._setState( ButtonState.OFF ); // Close Module
		this._Testbed()._Robot()._keyType( this._Testbed()._SysInfo()._isMac() ? KeyEvent.VK_META : KeyEvent.VK_CONTROL, KeyEvent.VK_D ); // Deselect all
	
		// Return time
		return this.m_finishedTrainingCallback._GetIntValue();
		
		} catch( Exception e) {
			this._Logs()._ResultLog()._LogException(e, true);
			throw new Exception( e.getMessage() );
		}
	}
	
	/********************************************************************
	 * Load an audio file specified by strings 'audiofilesDir' and 'audioFile' in \a pParams,
	 * and process it using a module specified by string 'module'.
	 */
	 public float _LoadFileAndProcess( TestCaseParameters pParams ) throws Exception {
		 
		try {
			// Load an audio file, process, and close the file.
			String audioFile= pParams._GetString( "audiofilesDir", null ) + "/" + pParams._GetString( "audioFile", null );
			this._Testbed()._HostApp()._Actions()._LoadAudioFile( audioFile, true, false );
	
			float fResult= _Process( pParams, null, true );
			
			this._Testbed()._HostApp()._Actions()._UnloadAudioFile();			
			return fResult;
			
		} catch( Exception e ) {
			// Try to close the file if an exception is thrown.
			this._Testbed()._HostApp()._Actions()._UnloadAudioFile();
			
			this._Logs()._ResultLog()._LogException(e, true);
			throw new Exception( e.getMessage() );
		}
	}
	 
	/********************************************************************
	 * Process the currently loaded file, using a module name from \a pParams or specified as \a strModule.
	 * Opens and closes the module before and after processing if \a bOpenAndClose is set.
	 * Returns the process time in seconds.
	 */
	public float _Process( TestCaseParameters pParams, String strModule, boolean bOpenAndClose ) throws Exception {
		try {
			if( this.m_EditBoxHandlerCB == null || this.m_finishedProcessingCallback == null ) throw new Exception("Need to call _SetCallbacks()" );
		
			// Clear any selection
			this._Testbed()._Robot()._keyType( this._Testbed()._SysInfo()._isMac() ? KeyEvent.VK_META : KeyEvent.VK_CONTROL, KeyEvent.VK_D );

			//this._Testbed()._HostApp()._Plugin()._Logs()._ResultLog()._logGeneric( "\tPreset: " + pParams._GetString( "modulePreset"), "PresetLoad" );

			_Train( pParams );	
		
			_SetSelection( pParams );

			// Open the module
			strModule= pParams._GetString( "module", strModule );
			TimeUtils.sleep( 0.3 );
			if( bOpenAndClose ) {
				_Controls()._Button( RXModule._getEnum( strModule )._getButtonID() )._setState( ButtonState.ON );
			}
			//btnModule._SetCallback( this.m_StatusInitializedCallback )._setState( ButtonState.ON ); // Open Module		
			TimeUtils.sleep( 1.0 );
			//m_lastModuleInitializedTime= this.m_StatusInitializedCallback._GetProcessTime();
		
			// Select a preset
			WindowControls.ComboBox cbPreset= _Controls()._ComboBox( strModule + " Preset Manager" );
			if( !pParams._GetString("modulePreset", "").equals("") ) {
				cbPreset._Select( pParams._GetString( "modulePreset" ) ); // Select Preset
				TimeUtils.sleep( 0.3 );
			}

			// Process			
			_Controls()._Button( "EffectPanel " + strModule + "|" + Buttons.Module_ProcessButton._getValue() )._SetCallback( this.m_finishedProcessingCallback )._setTimeOut(60*20)._click(); // Process
			
			//TimeUtils.sleep( 1.0 ); // shouldnt need this anymore
			if( bOpenAndClose ) {
				// Close the module
				_Controls()._Button( RXModule._getEnum( strModule )._getButtonID() )._setState( ButtonState.OFF );
				TimeUtils.sleep( 1.0 );
			}
		
			// Get processing time		
			float processMilliSec= this.m_finishedProcessingCallback._GetIntValue();
				
			return processMilliSec / 1000.0f;
		
		} catch( Exception e) {
			this._Logs()._ResultLog()._LogException(e, true);
			throw new Exception( e.getMessage() );
		}
	}
	
	/********************************************************************
	 * Save the current file as \a strName
	 */
	public void _SaveAs( String strName ) throws Exception {
		
		// Press the shortcut for Save As
		this._Testbed()._Robot()._keyType( this._Testbed()._SysInfo()._isMac() ? KeyEvent.VK_META : KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_S );
		TimeUtils.sleep( 2.0 );
		// Type the name
		this._Testbed()._Robot()._keyType( strName );
		TimeUtils.sleep( 0.5 );
		// Save
		this._Testbed()._Robot()._keyType( KeyEvent.VK_ENTER );
		
		EventIntHolderCallback callback= new EventIntHolderCallback( EventSubType.SavedFile );
		this._Testbed()._HostApp()._GetAppServer()._waitForEvent(EventSubType.SavedFile, null, callback, 360);
	}
	
	
	/********************************************************************
	 * Save the current file as \a strName, using the file type at index \n nIndex if possible
	 */
	public void _SaveAsType( String strName, int nIndex ) throws Exception {
		
		// Press the shortcut for Save As
		this._Testbed()._Robot()._keyType( this._Testbed()._SysInfo()._isMac() ? KeyEvent.VK_META : KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_S );
		TimeUtils.sleep( 2.0 );
		// Type the name
		this._Testbed()._Robot()._keyType( strName );
		TimeUtils.sleep( 0.5 );
		
		// Tab to the output format combo box and choose the desired format
		if( this._Testbed()._SysInfo()._isMac() ) {
			// Get to the widget
			for( int i= 0; i < 3; ++i ) {
				this._Testbed()._Robot()._keyType( KeyEvent.VK_TAB );
			}
			// Go down to the format
			for( int i= 0; i <= nIndex; ++i ) {
				this._Testbed()._Robot()._keyType( KeyEvent.VK_DOWN );
			}
			this._Testbed()._Robot()._keyType( KeyEvent.VK_ENTER );
		} else {
			// Get to the widget
			this._Testbed()._Robot()._keyType( KeyEvent.VK_TAB );
			this._Testbed()._Robot()._keyType( KeyEvent.VK_DOWN );
			// Go up to the top first
			for( int i= 0; i < 8; ++i ) {
				this._Testbed()._Robot()._keyType( KeyEvent.VK_UP );
			}
			// Go down to the format
			for( int i= 0; i < nIndex; ++i ) {
				this._Testbed()._Robot()._keyType( KeyEvent.VK_DOWN );
			}
			this._Testbed()._Robot()._keyType( KeyEvent.VK_ENTER );
		}
		
		// Save
		this._Testbed()._Robot()._keyType( KeyEvent.VK_ENTER );

		// TODO: Figure out why Mac does not get the event notification for the "Do you want to replace it?" dialog
		if( this._Testbed()._SysInfo()._isMac() )
			this._Testbed()._Robot()._keyType( KeyEvent.VK_SPACE );

		EventIntHolderCallback callback= new EventIntHolderCallback( EventSubType.SavedFile );
		this._Testbed()._HostApp()._GetAppServer()._waitForEvent(EventSubType.SavedFile, null, callback, 360);
	}
	
	
	

} // end class