package iZomateCore.AppCore;

import iZomateCore.AppCore.AppEnums.*;
import iZomateCore.AppCore.AppEnums.WindowControls.Buttons;
import iZomateCore.AppCore.AppEnums.WindowControls.DualListBoxes;
import iZomateCore.AppCore.NotificationCallbacks.DLOGNotificationCallback;
import iZomateCore.AppCore.WindowControls.Side;
import iZomateCore.AppCore.WindowControls.WidgetInfo;
import iZomateCore.LogCore.Logs;
import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.NotificationCallback;
import iZomateCore.ServerCore.RPCServer.IncomingReply;
import iZomateCore.ServerCore.RPCServer.OutgoingRequest;
import iZomateCore.ServerCore.RPCServer.RPCServer;
import iZomateCore.ServerCore.RPCServer.RemoteServer.ProcessInfo;
import iZomateCore.ServerCore.RPCServer.RemoteServer.RemoteFile;
import iZomateCore.ServerCore.RPCServer.RemoteServer.SystemInfo;
import iZomateCore.TestCore.TestCaseParameters;
import iZomateCore.TestCore.Testbed;
import iZomateCore.UtilityCore.TimeUtils;

import javax.sound.midi.ShortMessage;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class HostApp implements HostInterface
{

	public RemoteFile			m_fAppFile;
	public DecimalFormat 		mDecFrmtr;
	public RPCServer 			m_pAppServer;
	public long					mPID;	

	private WindowControls		mWindowControls;
	private String 				mMIDIDevice = null;
	private Iterator<String>	mCategoryIter= null;
	private Iterator<String>	mPatchIter= null;
	private WidgetInfo 			mDLBInfo = null;
	private String 				mCategory= null;
	private String				mPrevPreset= null;
	private int					mPresetChangeCount= 0;
	private long				mStartTime;
	private ProcessInfo			mStartMemory= null;
	private SystemInfo			mStartSystemInfo;
	private LinkedHashMap<String,ProcessInfo> m_pPIMap = new LinkedHashMap<String,ProcessInfo>();
	private	ProcessInfo			m_pPrevPI = new ProcessInfo();
	private HostActions			m_pHostActions= null;
	private Testbed				m_pTestbed= null;
	private Map<String, Plugin>	m_mPlugins= new HashMap<String, Plugin>();
	private Plugin				m_pActivePlugin= null;
	private HostType			m_HostType = HostType.UNKNOWN;
	private long				m_lAudioMemoryOverhead= 0;
	public DLOGNotificationCallback m_DefaultDLOGCallback= null;

	protected ArrayList<Double> mInstantiationTimes= new ArrayList<Double>();
	protected ArrayList<Double> m_pMemoryArray= new ArrayList<Double>();
	
	public static int			s_bytesPerMB= 1048576;


	public HostApp( String strHostApp, Testbed pTestbed ) throws Exception {
		this.m_pTestbed= pTestbed;
		this.m_HostType= HostType.getEnum( strHostApp );
				
		// Instantiate App RPC Server
		if( this.m_HostType._HasAppRPCServer() ){
			this._GetAppServer();
		
			if( this._HostType().equals( HostType.ProToolsMac ) )
				this.mWindowControls._setYOffset( 85 );
			// Register Custom Notification Callback objects
			this.m_DefaultDLOGCallback= new DLOGNotificationCallback( this );
			this._GetAppServer()._RegisterNotificationCallback( this.m_DefaultDLOGCallback );
		
		}
		
		this.m_fAppFile = this._Testbed()._CreateRemoteFile( strHostApp );		
		
		
		
		this.mDecFrmtr = new DecimalFormat();
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setGroupingSeparator(',');
		this.mDecFrmtr.setDecimalFormatSymbols(dfs);
	}

	
	public Logs _Logs() throws Exception {
		return this.m_pTestbed._Logs();
	}
	
	public Testbed _Testbed() {
		return this.m_pTestbed;
	}
	
	public HostType _HostType() {
		return this.m_HostType;
	}
	
	public RemoteFile _HostAppFile() {
		return this.m_fAppFile;
	}
	
	public RPCServer _GetAppServer() throws Exception {
		if( this.m_pAppServer == null ) {
			// Instantiate Host App RPC Server
			this.m_pAppServer = new RPCServer( this._Testbed()._GetMachineName(), "APP:"+this._HostType()._GetInternalName(), this._Logs() );
			this.mWindowControls = new WindowControls( this.m_pAppServer, this._Testbed() );
			this.m_pAppServer._connectToClient(); //TODO:  Figure why we block to socket if we don't do this.  we should not have to do this.  Only seems to happen on plugins
		}
		return this.m_pAppServer;
	}
	
	public ProcessInfo _GetProcessInfo() throws Exception {
		return this._Testbed()._SysInfo()._getProcInfo( this.mPID );
	}

	public ProcessInfo _GetProcessInfo( String strMemo, boolean bLogResults ) throws Exception {
		ProcessInfo pi = this._GetProcessInfo();
//		SystemInfo si = this.mRmtServer._remSysInfo._getSystemInfo();
		if( strMemo.equals( "\tMemory Check" ))
			this.m_pMemoryArray.add( (double) pi.mMemRes/s_bytesPerMB );

		this.m_pPIMap.put( strMemo, pi );
		if( bLogResults ) { 
			this._Logs()._ResultLog()._logGeneric( String.format("%1$-30s", strMemo ) + 
				"	Mem: " 			+ String.format("%1$-12s", this.mDecFrmtr.format(pi.mMemRes)) + 
				" (" 				+ String.format("%1$-12s", this.mDecFrmtr.format(pi.mMemRes - this.m_pPrevPI.mMemRes)) + ")" +
				" 	CPU %: " 		+ String.format("%1$-3s", this.mDecFrmtr.format(pi.mCPUPercentage*100)) +
				" 	CPU Time: " 	+ String.format("%1$-3s", this.mDecFrmtr.format(pi.mCPUTime)) +
				" 	Threads: " 		+ String.format("%1$-3s", this.mDecFrmtr.format(pi.mThreads)) +
				" 	Pages: " 		+ String.format("%1$-3s", this.mDecFrmtr.format(pi.mMemPageFaults)), "MemoryCheck");
		}
		this.m_pPrevPI = pi;
//		System.out.println(pi.toString());
		return pi;
	}
	
	public void _SetAudioMemoryOverhead( long lBytes ) {
		this.m_lAudioMemoryOverhead= lBytes;
	}

	public long _GetAudioMemoryOverhead() {
		return this.m_lAudioMemoryOverhead;
	}
			
	public void _initStartTime() {
		this.mStartTime= System.currentTimeMillis();
	}
	
	public TestCaseParameters _GetParams() {
		return this._Testbed()._Test()._GetCurrentTestCaseParams();
	}
	
	public HostActions _Actions() {
		if( this.m_pHostActions == null )
			this.m_pHostActions= new HostActions( this );
		return this.m_pHostActions;
	}
	
	public String _LogRunTime() throws Exception {
		String format = String.format("%%0%dd", 2);  
	    long elapsedTime = (System.currentTimeMillis() - this.mStartTime)/1000;  
	    String seconds = String.format(format, elapsedTime % 60);  
	    String minutes = String.format(format, (elapsedTime % 3600) / 60);  
	    String hours = String.format(format, elapsedTime / 3600);  
	    String time =  hours + ":" + minutes + ":" + seconds; 
		this._Logs()._ResultLog()._logMessage("Total Testcases: " + this.mPresetChangeCount );
		this._Logs()._ResultLog()._logMessage("Elapsed Run Time: " + time);

	    return time;  
	}
	
	@Override
	public void _Launch(int sleep, String optionalFile, boolean bNewInstance, boolean bHideWindows ) throws Exception
	{		
		// Check for and handle crash reporter
		this._Testbed()._DismissCrashReporter( this._GetParams()._GetSubmitCrashReport() );
		this._initStartTime();
		
		//isAppRunning will return the PID if it is running
		this.mPID= this._Testbed()._IsAppRunning( this.m_fAppFile._getName() );
		
		if( this.mPID != 0 ) {
			this._Logs()._ResultLog()._logData(this.m_fAppFile._getName() + " is already running");
			// Try to connect
//TODO: Test his against iris test to see how it should be refactored
//			if( this.mPluginServer != null && this.mPluginServer._connectToClient() ) {
//				if( bHideWindows ) {//Force it to be active window
//					this._Testbed()._Robot()._keyTypePause(.25, KeyEvent.VK_WINDOWS, KeyEvent.VK_M); // Hide all
//					//TODO: Fix IRIS specific code
//					this._Testbed()._Robot()._imageClick(TestAutomationImages.IrisTaskBarIcon, 1);
//				}
//				if( this.m_pAppServer != null )
//					this.m_pAppServer._connectToClient();
//			}
				
			if( bNewInstance ) {
				//TODO: Fix regular Quit to make sure our app is front most so we don't accidentally quit the wrong app
				if( !this._Testbed()._ForceQuit( this.m_fAppFile._getName(), 5 ) )
					throw new Exception( "An instance of " + this.m_fAppFile._getName() + " was detected but we could not connect to it or forcibly terminate its process");
				
				if( this.m_pAppServer != null )
					this.m_pAppServer._Shutdown();

				this.mPID= 0;
				this.mStartMemory= null;
			}
		}
		
		this.mStartSystemInfo= this._Testbed()._SysInfo()._getSystemInfo();
		this._Logs()._ResultLog()._logData( "Initial System Memory: " + this.mDecFrmtr.format(this.mStartSystemInfo.mMemFree) );

		if( this.mPID == 0 ) {
			if (!this.m_fAppFile._exists())
				throw new Exception(this.m_fAppFile._getPathAndName() + " not found!");

			if( bHideWindows )
				this._Testbed()._Robot()._keyTypePause(.25, KeyEvent.VK_WINDOWS, KeyEvent.VK_M); // Hide all

			if( optionalFile != null ) {
				if (!this._Testbed()._CreateRemoteFile(optionalFile)._exists())
					throw new Exception(optionalFile + " not found!");				
			}

			this._Logs()._ResultLog()._logData("Launching '" + this.m_fAppFile._getPathAndName() + "'");
			this._Testbed()._LaunchApp( this.m_fAppFile, optionalFile );
			
			long timeout = System.currentTimeMillis() + (sleep*1000);
			
			// We are connecting to a 3rd party app so we have to AppServer to connect to
			if( this.m_pAppServer == null ) {
				do {
					TimeUtils.sleep(2);
					this.mPID = this._Testbed()._IsAppRunning(this.m_fAppFile._getName());
				} while (this.mPID == 0 && System.currentTimeMillis() < timeout);
			}
			else {
				//Wait for the app server connection
				while( !this.m_pAppServer._connectToClient() ) {
					if( System.currentTimeMillis() > timeout )
						throw new Exception( "Could not connect to App RPC Server inside of " + this.m_fAppFile._getName() );
					TimeUtils.sleep(.1);
				};
				
				//Even though we connected to the server, make sure the system is fully aware of the new proc
				while( this.mPID == 0 ) {
					this.mPID= this._Testbed()._SysInfo()._getProcPid( this._HostAppFile()._getName() );
					if( this.mPID == 0 ) {
						if( System.currentTimeMillis() > timeout )
							throw new Exception( "Could not find pid for " + this.m_fAppFile._getName() );
						TimeUtils.sleep(.1);
					}
				}
			}

			//this.mRemoteServer._clickImage("/IzoImages/IrisAuthWinContinueBtn.bmp", 1);
		}
		
		if( this.mStartMemory == null )
			this.mStartMemory= this._GetProcessInfo("Initial Memory", true);
	}
	
	/**
	 * Force Quit the Host App if it is running
	 * 
	 * @throws Exception 
	 */
	public void _ForceQuit() throws Exception {
		if( !this._Testbed()._ForceQuit( this.m_fAppFile._getName(), 5 ) )
			throw new Exception( "An instance of " + this.m_fAppFile._getName() + " was detected but we could not connect to it or forcibly terminate its process");
		
		if( this.m_pAppServer != null )
			this.m_pAppServer._Shutdown();

		this.mPID= 0;
		this.mStartMemory= null;
	}
	
	/**
	 * 
	 * @return
	 */
	public double _GetStartMemory() {
		return this.mStartMemory.mMemRes/s_bytesPerMB;
	}

    /**
     *   Press the hot keys that will quit the app
     */
    private void _pressQuitHotkeys() throws Exception {
        // Quit the app
        if( this._Testbed()._SysInfo()._isWin() ) {
            if( /*this.m_HostType == HostType.RX3 || this.m_HostType == HostType.RX3ii ||*/ this.m_HostType == HostType.RX4 ) {
                // Note: Alt-F4 may not quit the whole app if a dialog box is open.
                // We mapped a special key for this in the options to handle this.
                this._Testbed()._Robot()._keyType(KeyEvent.VK_CONTROL, KeyEvent.VK_Q);
            }
            else
                this._Testbed()._Robot()._keyType(KeyEvent.VK_ALT, KeyEvent.VK_F4);
        } 
        else // Mac
            this._Testbed()._Robot()._keyType(KeyEvent.VK_META, KeyEvent.VK_Q);
    }

	@Override
	public void _Quit( int maxQuitDuration, boolean bGuarantee ) throws Exception {
		this._Logs()._ResultLog()._logLine( "<HR>" );
		this._Logs()._ResultLog()._logData("Quitting '" + this.m_fAppFile._getPathAndName() + "'");
		boolean bHasPlugin= this._Testbed()._HostApp()._GetPlugins().length > 0;
		
		this._Actions().m_strLastLoadedAudioFile= null;

		if( this._amIRunning() )
            this._pressQuitHotkeys();
			
		if( bHasPlugin && this._Testbed()._HostApp()._Plugin()._GetPluginInfo().m_strCodeBranch.equals( "master" ))
			this._Testbed()._HostApp()._GetAppServer()._waitForEvent( EventSubType.RPCServerEvent, "Destroyed", null, 30 );
		else
			TimeUtils.sleep(3);
		
		this._GetAppServer()._Shutdown();
		if( bHasPlugin ) {
			for( String strPlugin : this._GetPlugins() )
				this._Plugin( strPlugin )._GetPluginServer()._Shutdown();
		}

		this._Testbed()._WaitForAppToQuit( this.m_fAppFile._getName(), maxQuitDuration, true );
				
		this._Logs()._ResultLog()._logData("Quit complete");
		this.mPID= 0; //reset
		SystemInfo si= this._Testbed()._SysInfo()._getSystemInfo();
		this._Logs()._ResultLog()._logData( "Available System Memory: " + this.mDecFrmtr.format(si.mMemFree) + " (" + this.mDecFrmtr.format(si.mMemFree - this.mStartSystemInfo.mMemFree) + ")");
	}

	public boolean _amIRunning() throws Exception {
		return this._Testbed()._RemoteServer()._IsAppRunning( this.m_fAppFile._getName() ) != 0;
	}

	@Override
	public void _createNewTrack(TrackFormat trackFormat, TrackType trackType) throws Exception {
		throw new Exception("Must override base class _createNewTrack()");
	}

	@Override
	public void _importAudioFile(String audioFile) throws Exception {
		throw new Exception("Must override base class _importAudioFile(String audioFile)");
	}

	@Override
	public void _togglePlay() throws Exception {
		throw new Exception("Must override base class _togglePlay()");
	}

	@Override
	public void _instantiatePlugin(String strPluginName, int insert) throws Exception {
		throw new Exception("Must override base class _instantiatePlugin(Plugin plugin, Insert insert)");
	}
	
	@Override
	public void _instantiatePlugin(PluginType plugin, int insert) throws Exception {
		throw new Exception("Must override base class _instantiatePlugin(Plugin plugin, Insert insert)");
	}

	@Override
	public void _uninstantiatePlugin(int insertNumber) throws Exception {
		throw new Exception("Must override base class _uninstantiatePlugin(int insertNumber)");
	}

	public IncomingReply _AppAutomationRequest( String strRequest ) throws Exception {
		OutgoingRequest req= this.m_pAppServer._createRequest( "appAutomationRequest" );
		req._addString( strRequest, "req" );
		return this.m_pAppServer._processRequest( req );
	}
	
	
	public void _sendMidiNotes(int fundamental, int voices, int cmd) throws Exception {	
		if (this.mMIDIDevice == null)
			this.mMIDIDevice = this._Testbed()._SysInfo()._isWin()?"Out To MIDI Yoke:  1":"RPCServer MIDI Input";
		
		List<ShortMessage> midiMsgs = new ArrayList<ShortMessage>();	
		for (int i = 0; i < voices; ++i)
		{
			ShortMessage shortMsg = new ShortMessage();
			shortMsg.setMessage(cmd, 0, (fundamental + 3*i), 100);
			midiMsgs.add(shortMsg);
		}
		
		this._Testbed()._MIDI()._sendMIDICommand(this.mMIDIDevice, midiMsgs.toArray(new ShortMessage[midiMsgs.size()]));
	}
	
	//TODO: Test this against Iris.  I think this needs to move to plugin class
	public void _cycleThruPresetsInMenu(int loopCount) throws Exception {
		WidgetInfo categoryInfo = this.mWindowControls._DualListBox(DualListBoxes.Preset)._info();
		for( int i = 0; i < loopCount; i++ ) {
			for( String category: categoryInfo.mDLBLeftItems.keySet() ) {
				this.mWindowControls._Button( Buttons.Presets )._SetCallback( new PresetsCallback( category, null, 0 ) )._click();
				WidgetInfo PatchInfo = this.mWindowControls._DualListBox( DualListBoxes.Preset )._info(); //Refreshes the patches content for the right box
				for( String patch: PatchInfo.mDLBRightItems.keySet() )
					this.mWindowControls._Button( Buttons.Presets )._SetCallback( new PresetsCallback( null, patch, 0 ) )._click();
			}
		}			
	}
	
	/**
	 * 
	 * @param bListBox
	 * @throws Exception
	 */
	//TODO: Test this against Iris.  I think this needs to move to plugin class
	public void _nextPreset( boolean bListBox, boolean bFromTop ) throws Exception {
		String patch= null;
		long startTime= System.currentTimeMillis();
		
		if( bListBox ) {
			// Check to see if we are starting from the top
			if( this.mDLBInfo == null ) {
				//Refreshes the category and patches content
				this.mDLBInfo= this.mWindowControls._DualListBox(DualListBoxes.Preset)._info();
				this.mCategory= this.mDLBInfo.mDLBLeftCurSel;
				// Set the Categroy Iter and Patch Iter to the current selection
				this.mCategoryIter= this.mDLBInfo.mDLBLeftItems.keySet().iterator();
				if( !bFromTop && this.mDLBInfo.mDLBLeftCurSel != null )
					while( !this.mCategoryIter.next().equals( this.mDLBInfo.mDLBLeftCurSel ) );						
				this.mPatchIter= this.mDLBInfo.mDLBRightItems.keySet().iterator();
				if( !bFromTop && this.mDLBInfo.mDLBRightCurSel != null )
					while( !this.mPatchIter.next().equals( this.mDLBInfo.mDLBRightCurSel ) );
			}
									
			if ( this.mPatchIter.hasNext() ) {
				patch= this.mPatchIter.next();
				this.mWindowControls._Button( Buttons.Presets )._SetCallback( new PresetsCallback(null, patch, 0 ) )._click();
			}
			// If the next patch is null then we need to switch to the next category
			else if( !this.mPatchIter.hasNext() && this.mCategoryIter.hasNext() ) {
				// Switch categories and refresh the patches info
				this.mCategory= this.mCategoryIter.next();
				this.mWindowControls._Button( Buttons.Presets )._SetCallback( new PresetsCallback( this.mCategory, null, 0 ) )._click();
				this.mDLBInfo= this.mWindowControls._DualListBox(DualListBoxes.Preset)._info();
				this.mPatchIter= this.mDLBInfo.mDLBRightItems.keySet().iterator();
				patch= this.mPatchIter.hasNext()?this.mPatchIter.next():null; // advance to next patch
			}
			else {
				// We have cycled thru everything.  Let's start back at the top
				this.mDLBInfo= null;
				this._nextPreset( true/*List Box*/, true/*From Top*/ ); // start again from top
			}
		}
		else // just click the next preset down arrow button
			this.mWindowControls._Button(Buttons.PresetNext)._click();
		
		this.mPresetChangeCount++;
		String caption= this.mWindowControls._Button(Buttons.Presets)._info().mCaption;		
			
		if( patch == null )
			this._Logs()._ResultLog()._TextFormat( "<B>" )._logGeneric(this.mPresetChangeCount + "  The category '" + this.mCategory + "' is empty    (" + (System.currentTimeMillis()-startTime) + "ms)", "PresetChange");
		else if( this.mPatchIter != null && !caption.equals( this._createPresetDisplayString(caption, patch) ) )
			this._Logs()._ResultLog()._logError(this.mPresetChangeCount + "  Patch was not selected: Attempted to select '" + patch + "' but '" + caption + "' was loaded instead", true );
		else if( this.mPrevPreset != null && caption.equals(this.mPrevPreset) )
			this._Logs()._ResultLog()._logError(this.mPresetChangeCount + "  Patch did NOT change! It remained on '" + caption + "'", true );		
		else
			this._Logs()._ResultLog()._TextFormat( "<B>" )._logGeneric(this.mPresetChangeCount + "  Changed preset to '" + caption + "'    (" + (System.currentTimeMillis()-startTime) + "ms)", "PresetChange");
		
		this.mPrevPreset= caption;
	}
	
	/**
	 * 
	 * @param caption
	 * @param patch
	 * @return
	 */
	private String _createPresetDisplayString(String caption, String patch) {
		String strPresetDisplay= null;
		String prefix= caption.startsWith("* ")?"* ":"";
				
		if( caption.startsWith( prefix + this.mCategory + " - " ) )
			strPresetDisplay= prefix + this.mCategory + " - " + patch; //category - patch
		else
			strPresetDisplay= prefix + patch; //just patch
		int nDots= caption.indexOf("...");
		
		if( nDots != -1 ) {
			if( strPresetDisplay.length() >= nDots )
				strPresetDisplay= strPresetDisplay.subSequence(0, nDots) + "...";				
		}
		
		return strPresetDisplay;
	}
		
	public Plugin _Plugin( String strPlugin ) throws Exception {
		if( strPlugin == null || strPlugin.isEmpty() )
			strPlugin= this._HostType()._GetInternalName();
		
		this.m_pActivePlugin= this.m_mPlugins.get( strPlugin );
		if( this.m_pActivePlugin == null) {
			this.m_pActivePlugin= new Plugin( strPlugin, this );	
			this.m_mPlugins.put( strPlugin, this.m_pActivePlugin );
		}

		return this.m_pActivePlugin;
	}
	
	public Plugin _Plugin( int nPluginNum ) throws Exception {
		if( this.m_pActivePlugin == null || nPluginNum > this.m_mPlugins.size() )
			return null;
		
		return this._Plugin( (String) this.m_mPlugins.keySet().toArray()[nPluginNum-1] ); // Convert to 0 based
	}

	public Plugin _Plugin() throws Exception {
		if( this.m_pActivePlugin == null )
			throw new Exception( "A Plugin has not been set.  You must call _Plugin( String strPlugin ) before calling _Plugin()" );
		return this.m_pActivePlugin;
	}
	
	/**
	 * 
	 * @return
	 */
	public String[] _GetPlugins() {
		return this.m_mPlugins.keySet().toArray( new String[0] );
	}

	/**
	 * 
	 * @author tskotz
	 *
	 */
	public class PresetsCallback extends NotificationCallback {
		private String mCategory;
		private String mPatch;
		private double mSleep;
		
		public PresetsCallback(String category, String patch, double sleep) {
			this.mCategory= category;
			this.mPatch= patch;
			this.mSleep= sleep;

			// Override only the values that we care about
			this.m_pNotificationType= 	NotificationType.EVNT;
			this.m_strID= 		"MouseEvent";
			this.m_strMessage= 	"Invoking";
		}
		
		@Override
		public CBStatus _Callback( String title, String id, String message, String[] buttons, int value ) throws Exception {
			if (this.mCategory != null) {
				HostApp.this.mWindowControls._DualListBox(DualListBoxes.Preset)._select(this.mCategory, Side.Left);
				
				// We need to grab a patch in the new category if none was selected or else the new category won't stick
				if( this.mPatch == null ) {
					WidgetInfo info= HostApp.this.mWindowControls._DualListBox(DualListBoxes.Preset)._info();
					if( info.mDLBRightItems.keySet().iterator().hasNext() )
						this.mPatch= info.mDLBRightItems.keySet().iterator().next();
				}
			}
				
			if (this.mPatch != null)
				HostApp.this.mWindowControls._DualListBox(DualListBoxes.Preset)._select(this.mPatch, Side.Right);

			TimeUtils.sleep(this.mSleep);
			try {
				HostApp.this.mWindowControls._Button(Buttons.PresetClose)._setTO(3)._click();
			} catch (Exception e) {
				//Try it one more time
				HostApp.this.mWindowControls._Button(Buttons.PresetClose)._setTO(3)._click();				
			}
			return CBStatus.HANDLED;
		}
	}

	@Override
	public void _instantiatePlugin(PluginType plugin, Insert insert)
			throws Exception {
		throw new Exception("Must override base class _instantiatePlugin(PluginType plugin, Insert insert)");
	}


	@Override
	public void _createNewTrack(TrackFormat trackFormat) throws Exception {
		throw new Exception("Must override base class _createNewTrack(TrackFormat trackFormat)");
	}


	@Override
	public void _removeTrack() throws Exception {
		throw new Exception("Must override base class _removeTrack()");
	}


	@Override
	public void _instantiatePlugin(String strPlugin, String strCategory)
			throws Exception {
		throw new Exception("Must override base class _instantiatePlugin(String strPlugin, String strCategory)");
	}


	@Override
	public void _uninstantiatePlugin() throws Exception {
		throw new Exception("Must override base class _uninstantiatePlugin()");
	}


	@Override
	public void _hidePluginUI() throws Exception {
		throw new Exception("Must override base class _hidePluginUI()");
	}


	@Override
	public void _showPluginUI() throws Exception {
		throw new Exception("Must override base class _showPluginUI()");
	}


	@Override
	public boolean _isPlaying() throws Exception {
		throw new Exception("Must override base class _isPlaying()");
	}

}
