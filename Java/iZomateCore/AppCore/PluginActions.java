package iZomateCore.AppCore;

import iZomateCore.AppCore.AppEnums.WindowControls.Buttons;
import iZomateCore.AppCore.AppEnums.WindowControls.Sliders;
import iZomateCore.AppCore.Callbacks.PresetManagerCallback;
import iZomateCore.AppCore.Callbacks.RXStatusPanelEventCallback;
import iZomateCore.AppCore.WindowControls.ButtonState;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.RPCServer.RemoteServer.RemoteFile;
import iZomateCore.TestCore.TestCaseParameters;
import iZomateCore.UtilityCore.TimeUtils;

import java.util.List;

public class PluginActions {
	private Plugin		m_pPlugin;
	
	public PluginActions( Plugin pPlugin ) {
		this.m_pPlugin= pPlugin;
	}
	
	/**
	 * 
	 * @param bRefresh
	 * @return
	 * @throws Exception
	 */
	public List<String> _GetPresets( boolean bRefresh ) throws Exception {
		if( this.m_pPlugin.m_strPresets == null || bRefresh ) {
			//TODO: I would like to get this directly from the Preset Manager
			RemoteFile fPresetFolder= null;
			String strPluginName= this.m_pPlugin.m_pHostApp._HostType()._GetPluginName();
			//TODO: Get GetConfigFileProperty|PresetFolder working on mac
			if( this.m_pPlugin.m_pHostApp._Testbed()._SysInfo()._isMac() ) {
				if( strPluginName.startsWith( "RX3" ) )
					fPresetFolder= this.m_pPlugin.m_pHostApp._Testbed()._CreateRemoteFile( "~/Documents/iZotope/RX 3/Presets/" + strPluginName.substring( 3 ) );
				else if( strPluginName.startsWith( "RX4" ) )
					fPresetFolder= this.m_pPlugin.m_pHostApp._Testbed()._CreateRemoteFile( "~/Documents/iZotope/RX 4/Presets/" + strPluginName.substring( 3 ) );
				else
					fPresetFolder= this.m_pPlugin.m_pHostApp._Testbed()._CreateRemoteFile( "~/Documents/iZotope/" + this.m_pPlugin.m_pHostApp._HostType()._GetPluginName() + "/Global Presets" );
			}
			else
				fPresetFolder= this.m_pPlugin.m_pHostApp._Testbed()._CreateRemoteFile( this.m_pPlugin.m_pHostApp._AppAutomationRequest( "GetConfigFileProperty|PresetFolder|" )._getString( "value" ) );
				
			// Get the file list and only add xml files
		    for( String item : fPresetFolder._list( true /*recursive*/ ) )
		        if( item.endsWith( ".xml" ) ) {
					if( strPluginName.startsWith( "RX" ) )
						this.m_pPlugin.m_strPresets.add( item.replace( ".xml", "" ) ); // RX 3 requires the xml to be removed
					else
						this.m_pPlugin.m_strPresets.add( item );
		        }
		    
			if( this.m_pPlugin.m_strPresets.isEmpty() )
				this.m_pPlugin._Logs()._ResultLog()._logWarning( "No presets were found in preset folder: '" + fPresetFolder._getPathAndName() + "'" );
			// Make sure the Preset Manager is closed
			// Nectar is special case.  It is a text label instead of a button
			else if( strPluginName.equals( "Nectar2" ) && this.m_pPlugin._Controls()._Button( "Dialog Preset" )._info().mVisible ) 
				this.m_pPlugin._Controls()._Button( Buttons.GlobalPreset._for( this.m_pPlugin ) )._SetEventNotification( EventSubType.PresetManager._for( this.m_pPlugin ), "FullyHidden" )._setState( ButtonState.OFF );
			else // Make sure the Preset Manager is closed
				this.m_pPlugin._Controls()._Button( Buttons.GlobalPreset._for( this.m_pPlugin ) )._SetEventNotification( EventSubType.PresetManager._for( this.m_pPlugin ), "FullyHidden" )._setState( ButtonState.OFF );
		}
		
		return this.m_pPlugin.m_strPresets;
	}
	
	/**
	 * 
	 * @param bRefresh
	 * @return
	 * @throws Exception
	 */
	public List<String> _GetiZParams( String strIZParamsDir, boolean bRefresh ) throws Exception {
		if( this.m_pPlugin.m_strPresets == null || bRefresh ) {
			RemoteFile fPresetFolder= this.m_pPlugin.m_pHostApp._Testbed()._CreateRemoteFile( strIZParamsDir );
				
			// Get the file list and only add xml files
		    for( String item : fPresetFolder._list( true /*recursive*/ ) )
		        if( item.endsWith( ".izparams" ) )
		        	this.m_pPlugin.m_strPresets.add( strIZParamsDir + "/" + item );

			if( this.m_pPlugin.m_strPresets.isEmpty() )
				this.m_pPlugin._Logs()._ResultLog()._logWarning( "No izparams were found in folder: '" + fPresetFolder._getPathAndName() + "'" );
		}
		
		return this.m_pPlugin.m_strPresets;
	}

	/**
	 * 
	 * @param strPreset
	 * @throws Exception
	 */
	public boolean _ChangePreset( String strPreset ) throws Exception {
		PresetManagerCallback cbPresetMgr= new PresetManagerCallback( strPreset, this.m_pPlugin );
		try {
			this.m_pPlugin._Controls()._Button( Buttons.GlobalPreset._for( this.m_pPlugin) )._SetCallback( cbPresetMgr )._setState( ButtonState.ON );
			this.m_pPlugin._Logs()._ResultLog()._TextFormat( "<B>" )._logGeneric( ++this.m_pPlugin.mPresetChangeCount + " Changed preset to '" + strPreset + "'    (" + cbPresetMgr.m_lTime + "ms)", "PresetChange" );
		} catch( Exception e ) {
			this.m_pPlugin._Logs()._ResultLog()._TextFormat( "<B>" )._logGeneric( ++this.m_pPlugin.mPresetChangeCount + " Failed to change preset to '" + strPreset + "'", "PresetChange" );	
			this.m_pPlugin._Logs()._ResultLog()._LogException( e, true );
		}
		this.m_pPlugin.mPresetLoadTimes.add( (double) cbPresetMgr.m_lTime );
		if( cbPresetMgr._GetErrorMsg() != null )
			this.m_pPlugin._Logs()._ResultLog()._logError( cbPresetMgr._GetErrorMsg(), true );
		return (cbPresetMgr._GetErrorMsg() == null);
	}
	
	/**
	 * 
	 * @param strPreset
	 * @param strModule
	 * @return
	 * @throws Exception
	 */
	public boolean _ChangeRXPreset( String strPreset, String strModule ) throws Exception {
		long lStartTime= System.currentTimeMillis();
		long lPresetloadTime= 0;
		RXStatusPanelEventCallback statusPanelCallback= new RXStatusPanelEventCallback( "Applied " );
		Exception excpt= null;
		try {
			this.m_pPlugin._Controls()._ComboBox( strModule + " Preset Manager" )._Select( strPreset );
			lPresetloadTime= System.currentTimeMillis() - lStartTime;
			this.m_pPlugin._Controls()._Button( "EffectPanel " + strModule + "|Apply Button" )._SetCallback( statusPanelCallback )._click();
		} catch( Exception e) {
			excpt= e;
			if( lPresetloadTime == 0 )
				lPresetloadTime= System.currentTimeMillis() - lStartTime;
		}
		//this.m_pPlugin.mProcessApplyTimes.add( (double) statusPanelCallback._GetProcessTime() );
		this.m_pPlugin.mPresetLoadTimes.add( (double) lPresetloadTime );
		this.m_pPlugin._Logs()._ResultLog()._TextFormat( "<B>" )._logGeneric( ++this.m_pPlugin.mPresetChangeCount + " Changed preset to '" + strPreset + "'    (" + lPresetloadTime + "ms)", "PresetChange" );
		if( excpt != null )
			this.m_pPlugin._Logs()._ResultLog()._logError( excpt.getMessage(), false );

		this.m_pPlugin._Logs()._ResultLog()._logGeneric( "\t" + statusPanelCallback._GetMessage(), "ProcessTime" );
		this.m_pPlugin._Console()._Perf_PauseLogging( false );
		this.m_pPlugin.m_pHostApp._GetProcessInfo("\tMemory Check", true);
		this.m_pPlugin._Profilers()._LogToResultsFile();

		return true;
	}

	/**
	 * 
	 * @param pParams
	 * @throws Exception
	 */
	public void _VinylConfigureUI( TestCaseParameters pParams) throws Exception {
		for( Sliders s : Sliders.values() )
			this.m_pPlugin._Controls()._Slider( s )._MoveToValue( pParams._GetFloat(s._getValue()), 5 );
		
		//for( Buttons b : new Buttons[]{Buttons.VinylStereoSwitch, Buttons.VinylBypassSwitch} )
		//	this.m_pPlugin._Controls()._Button( b )._setState( pParams._GetBool(b._getValue()) ? ButtonState.ON : ButtonState.OFF );
	}

	/**
	 * 
	 * @param nDuration
	 * @param nPresetPlayTime
	 * @throws Exception
	 */
	public void _CycleThruPresets( int nDuration, int nPresetPlayTime ) throws Exception {
		long lStopTime_ms = nDuration*1000 + System.currentTimeMillis();
		this._GetPresets( true );
			
		// Start Cycling through presets
		do {
			if( this.m_pPlugin.m_strPresets.isEmpty() ) {
				this.m_pPlugin._Logs()._ResultLog()._TextFormat( "<B>" )._logGeneric( ++this.m_pPlugin.mPresetChangeCount + " Changed preset to 'NO PRESETS DETECTED'    (0ms)", "PresetChange" );
				this.m_pPlugin.mPresetLoadTimes.add( (double)0 );
				TimeUtils.sleep( nPresetPlayTime ); // Just play once
				lStopTime_ms= System.currentTimeMillis();
				this.m_pPlugin.m_pHostApp._GetProcessInfo("\tMemory Check", true);
				this.m_pPlugin._Profilers()._LogToResultsFile();
			}
			else {
				for( String s : this.m_pPlugin.m_strPresets ) {					
					boolean bOK= this._ChangePreset( s );
					this.m_pPlugin._Console()._Perf_PauseLogging( false );
					if( bOK )
						TimeUtils.sleep( nPresetPlayTime );
					this.m_pPlugin.m_pHostApp._GetProcessInfo("\tMemory Check", true);
					this.m_pPlugin._Profilers()._LogToResultsFile();
					if( nDuration >= 0 && System.currentTimeMillis() >= lStopTime_ms )
						break;
				}
			}
		} while ( System.currentTimeMillis() < lStopTime_ms );
	}

	/**
	 * 
	 * @param strIZParamsDir
	 * @param nDuration
	 * @param nPresetPlayTime
	 * @throws Exception
	 */
	public void _CycleThruiZParams( String strIZParamsDir, int nDuration, int nPresetPlayTime ) throws Exception {
		this._GetiZParams( strIZParamsDir, true );
		long lStopTime_ms = nDuration*1000 + System.currentTimeMillis();
			
		// Start Cycling through izparams
		do {
			for( String s : this.m_pPlugin.m_strPresets ) {
				this._LoadiZParamsAndLog( s );
				TimeUtils.sleep( nPresetPlayTime );
				this.m_pPlugin.m_pHostApp._GetProcessInfo("\tMemory Check", true);
				this.m_pPlugin._Profilers()._LogToResultsFile();
				if( nDuration >= 0 && System.currentTimeMillis() >= lStopTime_ms )
					break;
			}
		} while ( System.currentTimeMillis() < lStopTime_ms );
	}

	/**
	 * 
	 * @param strParams
	 * @throws Exception
	 */
	public void _LoadiZParamsAndLog( String strParams ) throws Exception {
		this.m_pPlugin._Logs()._ResultLog()._TextFormat( "<B>" )._logGeneric( ++this.m_pPlugin.mPresetChangeCount + " Testing izparams file '" + strParams + "'", "PresetChange" );
		this.m_pPlugin.mPresetLoadTimes.add( this.m_pPlugin.m_pHostApp._Actions()._ImportFile( strParams ) );
		this.m_pPlugin._Console()._Perf_PauseLogging( false );
	}

}
