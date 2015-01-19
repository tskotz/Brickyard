package iZomateCore.AppCore;


import iZomateCore.AppCore.AppEnums.SAHookMenuBar;
import iZomateCore.AppCore.Callbacks.CustomDialogCallback;
import iZomateCore.AppCore.Callbacks.OpenAudioFileCallback;
import iZomateCore.AppCore.Callbacks.OpenAudioFileCallbackOld;
import iZomateCore.AppCore.Callbacks.RXAddFilesBatchCallback;
import iZomateCore.AppCore.Callbacks.RXBatchJobCompleteCallback;
import iZomateCore.AppCore.Callbacks.RXBatchOutputOptionsCallback;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.Notifications.NotificationCallback;
import iZomateCore.ServerCore.RPCServer.RemoteServer.RemoteFile;
import iZomateCore.UtilityCore.TimeUtils;

import java.awt.event.KeyEvent;

public class HostActions {
	private HostApp		m_pHostApp;
	public  String		m_strLastLoadedAudioFile= null;
	
	public HostActions( HostApp pHostApp ) {
		this.m_pHostApp= pHostApp;
	}
	
	public void _Launch( int nSleep, String strOptionalFile, boolean bRequireNewInstance, boolean bHideWindows ) throws Exception {
		this.m_pHostApp._Launch( nSleep, strOptionalFile, bRequireNewInstance, bHideWindows );
	}
	
	public void _SAHookMenuBar( SAHookMenuBar mbItem ) throws Exception {
		mbItem._doHotKey( this.m_pHostApp._Testbed() );
	}
	
	public void _SAHookMenuBar( SAHookMenuBar mbItem, NotificationCallback pCallback ) throws Exception {
		mbItem._doHotKey( this.m_pHostApp._Testbed(), this.m_pHostApp.m_pAppServer, EventSubType.MenuInvoke, "Handled", pCallback );
	}
	
	/**
	 * 
	 * @param strAudioFile
	 * @throws Exception
	 */
	public double _ImportFile( String strFile ) throws Exception {
		double dImportTime= 0;

		if( strFile != null && strFile != "" ) {
			RemoteFile rfFile= this.m_pHostApp._Testbed()._CreateRemoteFile( strFile );
			
			if( !rfFile._exists() )
				throw new Exception( "The file specified for import was not found: " + rfFile._getPathAndName() );
			
			this.m_pHostApp._Logs()._ResultLog()._logString( "Importing file: " + rfFile._getPathAndName() );
			
			if( this.m_pHostApp._Testbed()._SysInfo()._isWin() )
				strFile= strFile.replace( "/", "\\" );

			CustomDialogCallback cb= new CustomDialogCallback( "Import params", null, strFile, KeyEvent.VK_ENTER, this.m_pHostApp );
			this._SAHookMenuBar( SAHookMenuBar.File_Import_Params, cb._IsGoToFolder() );
			dImportTime= System.currentTimeMillis() - cb._GetDismissalTime();
			
			this.m_pHostApp._Logs()._ResultLog()._logLine( "  (" + dImportTime + " ms)" );
		}
		// Return import time
		return dImportTime;
	}

	/**
	 * 
	 * @param strAudioFile
	 * @throws Exception
	 */
	public void _LoadAudioFile( String strAudioFile, boolean bForce, boolean bLogResults ) throws Exception {
		if( strAudioFile == null || strAudioFile == "" )
			this.m_pHostApp._Logs()._ResultLog()._logMessage( "No audio file supplied; using existing file" );
		else if( bForce || !strAudioFile.equals( this.m_strLastLoadedAudioFile ) ){
			RemoteFile rfAudioFile= this.m_pHostApp._Testbed()._CreateRemoteFile( strAudioFile );
			
			if( !rfAudioFile._exists() )
				throw new Exception( "Audio file not found: " + rfAudioFile._getPathAndName() );
					
			this._LoadTheFile( rfAudioFile, bLogResults );
			// This check doesn't quite work yet
			if( this.m_pHostApp.m_DefaultDLOGCallback.m_strLastDLOG.contains( "Title: Can't open file" ) )
				this._LoadTheFile( rfAudioFile, bLogResults ); //try it one more time
			
			this.m_pHostApp._SetAudioMemoryOverhead( (long) ( this.m_pHostApp._GetProcessInfo("Memory - audio file loaded", bLogResults).mMemRes/HostApp.s_bytesPerMB - this.m_pHostApp._GetStartMemory() ) );
		}

		this.m_strLastLoadedAudioFile= strAudioFile;
		this.m_pHostApp._Logs()._ResultLog()._logLine( "" );
	}
	
	/**
	 * 
	 * @param rfAudioFile
	 * @throws Exception
	 */
	private void _LoadTheFile( RemoteFile rfAudioFile, boolean bLogResults ) throws Exception {
		if( bLogResults ) this.m_pHostApp._Logs()._ResultLog()._logMessage( "Loading audio file: " + rfAudioFile._getPathAndName() );
		
		this.m_pHostApp._AppAutomationRequest( "SetConfigFileProperty|LastFolder|" + rfAudioFile._getParentPath() );
		if( this.m_pHostApp._Plugin()._GetPluginInfo().m_strCodeBranch.equals( "master" ) || this.m_pHostApp._Plugin()._GetPluginInfo().m_nCodeModDate >= 20130823 )
			this._SAHookMenuBar( SAHookMenuBar.File_Open, new OpenAudioFileCallback( rfAudioFile._getName(), this.m_pHostApp ) );
		else //old
			this._SAHookMenuBar( SAHookMenuBar.File_Open, new OpenAudioFileCallbackOld( rfAudioFile._getName(), this.m_pHostApp ) );
		
		if( !this.m_pHostApp._Plugin()._GetPluginInfo().m_strCodeBranch.equals( "master" ) ) {
			// TODO:  Add an event notification so we don't have to do this
			for( int i= 0; i<10; i++) {
				TimeUtils.sleep( .5 );
				if( this.m_pHostApp._GetProcessInfo().mMemRes/HostApp.s_bytesPerMB > this.m_pHostApp._GetStartMemory() )
					break; // The file has been fully loaded
			}
		}
		TimeUtils.sleep( 1.0 );
	}

	/**
	 * Developed for RX3.  Not sure if it works for other apps
	 * @throws Exception
	 */
	public void _UnloadAudioFile() throws Exception {
		this.m_pHostApp._Testbed()._Robot()._keyType( this.m_pHostApp._Testbed()._SysInfo()._isMac() ? KeyEvent.VK_META : KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_W );
		if( this.m_pHostApp._Testbed()._SysInfo()._isMac() ) {
            TimeUtils.sleep( 1.0 );
			this.m_pHostApp._Testbed()._Robot()._keyType( KeyEvent.VK_SPACE );			
			this.m_pHostApp._GetAppServer()._waitForEvent( EventSubType.MenuInvoke, "Handled", null, 15 );
		}
		else
			this.m_pHostApp._GetAppServer()._waitForEvent( EventSubType.KeyboardEvent, "KeyPress", null, 15 );
		
		TimeUtils.sleep( 1.0 );
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	
	public void _RXOpenBatchProcessor() throws Exception {
		this.m_pHostApp._Logs()._ResultLog()._logLine( "Opening Batch Processor.. \n" );
		this._SAHookMenuBar( SAHookMenuBar.RX_File_Batch_Processing); //open the batch processor window
		if( !this.m_pHostApp._Plugin()._GetPluginInfo().m_strCodeBranch.equals( "master" ) ) {
			// TODO:  Add an event notification so we don't have to do this
			for( int i= 0; i<10; i++) {
				TimeUtils.sleep( .5 );
				if( this.m_pHostApp._GetProcessInfo().mMemRes/HostApp.s_bytesPerMB > this.m_pHostApp._GetStartMemory() )
					break; // The Batch processor is open
				}
			}
		TimeUtils.sleep( 1.0 );
		}// _RXOpenBatchProcessor()

	public void _RXBatchAddFiles(String strDirectory) throws Exception {
		RemoteFile dir= this.m_pHostApp._Testbed()._CreateRemoteFile( strDirectory );
		this.m_pHostApp._Logs()._ResultLog()._logLine( "Searching for directory... \n" );
		if( !dir._exists() ) {
			this.m_pHostApp._Logs()._ResultLog()._LogException( new Exception("Directory " + strDirectory + " does not exist.") , false); //Error catching for if the directory is bogus.
			return;
		}
		this.m_pHostApp._Logs()._ResultLog()._logLine( "Directory found! \n" );		
		this.m_pHostApp._Testbed()._HostApp()._Actions()._RXOpenBatchProcessor(); //Open Batch Processing window
		this.m_pHostApp._Logs()._ResultLog()._logMessage( "Opened Batch processor.");

		iZomateCore.AppCore.WindowControls.Button buttonAddFiles= this.m_pHostApp._Plugin()._Controls()._Button( "Batch Process Window|Batch Process Detail|Add Input Files" ); //create a reference to the "Add Files..." button
		this.m_pHostApp._Logs()._ResultLog()._logMessage( "Loading files in directory " + strDirectory + "." );
		buttonAddFiles._SetCallback( new RXAddFilesBatchCallback( strDirectory , this.m_pHostApp))._click();//Add the files from the passed directory.
		TimeUtils.sleep(0.1);
	} //End _RXBatchAddFiles

	public void _RXBatchClearAllProcessingSteps() throws Exception {
		//while the "remove processing steps" button exists, keep trying to click the button.  If it doesn't, an exception will be thrown.  catch the exception and return to the rest of the program.
		Boolean exists = true;
		iZomateCore.AppCore.WindowControls.Button bRemoveProcess = this.m_pHostApp._Plugin()._Controls()._Button( "Batch Process Window|Batch Process Detail|Process Chain|Process Chain Row 0|Row Minus Button");
		while ( exists == true){ 
			try {
				bRemoveProcess._info();
				bRemoveProcess._click();
				TimeUtils.sleep(0.1);
			} catch (Exception e){
				this.m_pHostApp._Logs()._ResultLog()._logMessage( "removal of all steps complete!");
				exists = false;
				}
			}
		}//End _RXBatchClearAllProcessingSteps
	
	/*
	 * the following method is a work in progress.  It is not used by any test at this point.
	 */
	public void _RXBatchAddProcessingStepWPreset( String moduleName, String presetName) throws Exception{
		//Adds the specified process to the end of the processing chain.
		Boolean exists = true;
		int i = 0;
		iZomateCore.AppCore.WindowControls.Button bAddProcess = this.m_pHostApp._Plugin()._Controls()._Button( "Batch Process Window|Batch Process Detail|Add First Chain Row Button");
		try{//Try to get info on the "Add First Chain Row" button.  If it doesn't exist and throws and exception then it means we have processes already.
			bAddProcess._info(); 
			}catch (Exception e){  //If we catch that "button doesn't exist" try to find last row by finding all the rows that do exist.
				while (exists == true){
					try{//Test the existence of each row.
						bAddProcess = this.m_pHostApp._Plugin()._Controls()._Button( "Batch Process Window|Batch Process Detail|Process Chain|Process Chain Row " + i +"|Row Plus Button" );
						bAddProcess._info();
						i++;
						}catch (Exception b){ //For the first row that doesn't exist, roll back the index by one and click the add button.
							i--;
							bAddProcess = this.m_pHostApp._Plugin()._Controls()._Button( "Batch Process Window|Batch Process Detail|Process Chain|Process Chain Row " + i +"|Row Plus Button" );
							bAddProcess._click();
							}//catch
					}//try
				}//while
		
		}//End _RXBatchAddProcessingStepWPreset
	
	public void _RXBatchAddToFilename(String strKind, String strText, String strLocation ) throws Exception{
		WindowControls.ComboBox cbKind = this.m_pHostApp._Plugin()._Controls()._ComboBox("Batch Process Window|Batch Process Detail|Output Filename Modification Kind");//Create a reference to the add "blank" combo box
		
		/*
		 * There are two controls that need to be created within the if statement to avoid exceptions: 
		 * The location of the location combo box is dynamic and changes based on the kind of data that is selected.
		 * The existence of the Text Edit box for custom text depends on that kind of data being selected.
		 */
		
		if( strKind.equals( "text" )){
			cbKind._Select( "text" );
			WindowControls.TextEdit teCustomText = this.m_pHostApp._Plugin()._Controls()._TextEdit("Batch Process Window|Batch Process Detail|Output name custom text");
			WindowControls.ComboBox cbLocation = this.m_pHostApp._Plugin()._Controls()._ComboBox("Batch Process Window|Batch Process Detail|Output Filename Modification Location");//Create a reference to the append/prepend combo box
			teCustomText._SetText( strText );
			cbLocation._Select( strLocation );
		}else{
			cbKind._Select( "date and time");
			WindowControls.ComboBox cbLocation = this.m_pHostApp._Plugin()._Controls()._ComboBox("Batch Process Window|Batch Process Detail|Output Filename Modification Location");//Create a reference to the append/prepend combo box
			cbLocation._Select( strLocation );
		}
	}// end _RXBatchAddToFilename
	
	public void _RXBatchSetResultsFolder() throws Exception{//This method us used to set the output to the original file's folder
		WindowControls.ComboBox cbResultsFolder = this.m_pHostApp._Plugin()._Controls()._ComboBox("Batch Process Window|Batch Process Detail|Output Folder");
		cbResultsFolder._Select("original file's folder");
	}
	
	
	/*
	 * the following method is a work in progress.
	 */
	public void _RXBatchSetResultsFolder( String strFileFolder) throws Exception{//This method us used to set the output to the original file's folder
		WindowControls.ComboBox cbResultsFolder = this.m_pHostApp._Plugin()._Controls()._ComboBox("Batch Process Window|Batch Process Detail|Output Folder");
		cbResultsFolder._Select("choose folder...");
	}//End _RXBatchSetResultsFolder "choose folder..."
	
	public void _RXBatchSetOutputFileFormat( String strFileFormat ) throws Exception {
		// Select a file format
		this.m_pHostApp._Logs()._ResultLog()._logLine( "Selecting output file format type: "+ strFileFormat + "\n" );
		WindowControls.ComboBox cbFileType= this.m_pHostApp._Plugin()._Controls()._ComboBox( "Batch Process Window|Batch Process Detail|Output File Format" ); //Create a reference to the File Format Combo Box
		cbFileType._Select( strFileFormat );
		TimeUtils.sleep( 0.5 );
	}//End_RXBatchSetOutputFileFormat0
	
	public void _RXBatchSetOutputOptions(String strFormat, String strOutputOptionsParams) throws Exception {
		this.m_pHostApp._Logs()._ResultLog()._logLine( "Opening output options... \n" );
		WindowControls.Button bOutputOptions= this.m_pHostApp._Plugin()._Controls()._Button( "Batch Process Window|Batch Process Detail|Output Options Button" ); //Create a reference to the output options button
		bOutputOptions._SetCallback(new RXBatchOutputOptionsCallback( strFormat, strOutputOptionsParams, this.m_pHostApp ));
		TimeUtils.sleep(0.5);
		bOutputOptions._click();
		TimeUtils.sleep( 0.1 );
	}//End opening output options

	
	public void _RXBatchRun( Boolean bAllowFail, int intTimeout ) throws Exception {
		RXBatchJobCompleteCallback batchRunCb = new RXBatchJobCompleteCallback( bAllowFail, this.m_pHostApp ); //callback to wait until the process finishes.		
		this.m_pHostApp._Logs()._ResultLog()._logLine("Processing");
		iZomateCore.AppCore.WindowControls.Button buttonBatchRun = this.m_pHostApp._Plugin()._Controls()._Button("Batch Process Window|Batch Process Detail|Run button"); //create a reference to the Batch Processor "Process" button
		
		this.m_pHostApp._Logs()._ResultLog()._logLine( "Processing has started... \n" );
		buttonBatchRun._click();
		this.m_pHostApp._Testbed()._HostApp()._GetAppServer()._waitForEvent(EventSubType.BatchJobFinished, null, batchRunCb, intTimeout);
		TimeUtils.sleep(0.1);
	}//End _RXBatchProcessorProcess

	public void _StartPlay() throws Exception {
		this.m_pHostApp._Logs()._ResultLog()._logLine( "Starting Play...\n" );
		this._SAHookMenuBar( SAHookMenuBar.Audio_Play );
		if( this.m_pHostApp._HostType()._IsSAHook() )
			this.m_pHostApp._GetAppServer()._waitForEvent( EventSubType.KeyboardEvent, "KeyPress", null, 5 );

		//else
			//throw new Exception( "No Play Action defined for host: " + this.m_pHostApp._HostType()._GetAppName() );
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void _StartLoopPlay() throws Exception {
		this.m_pHostApp._Logs()._ResultLog()._logLine( "Starting Loop Play...\n" );
		this._SAHookMenuBar( SAHookMenuBar.Audio_Loop );
		if( this.m_pHostApp._HostType()._IsSAHook() )
			this.m_pHostApp._GetAppServer()._waitForEvent( EventSubType.KeyboardEvent, "KeyPress", null, 5 );

		//else
			//throw new Exception( "No Play Action defined for host: " + this.m_pHostApp._HostType()._GetAppName() );
	}

	public void _StopPlay() throws Exception {
		this.m_pHostApp._Logs()._ResultLog()._logLine( "Play Stopped" );
		this._SAHookMenuBar( SAHookMenuBar.Audio_Stop );
		if( this.m_pHostApp._HostType()._IsSAHook() )
			this.m_pHostApp._GetAppServer()._waitForEvent( EventSubType.KeyboardEvent, "KeyPress", null, 5 );
		//else
			//throw new Exception( "No Stop Action defined for host: " + this.m_pHostApp._HostType()._GetAppName() );
	}

}
