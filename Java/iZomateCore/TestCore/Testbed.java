package iZomateCore.TestCore;

import iZomateCore.AppCore.AppEnums.HostType;
import iZomateCore.AppCore.AppEnums.Images;
import iZomateCore.AppCore.AppEnums.WindowControls.Buttons;
import iZomateCore.AppCore.AppEnums.WindowControls.MouseButtons;
import iZomateCore.AppCore.HostApp;
import iZomateCore.AppCore.WindowControls;
import iZomateCore.AppCore.WindowControls.ClickSpot;
import iZomateCore.LogCore.Logs;
import iZomateCore.ServerCore.RPCServer.RPCServer;
import iZomateCore.ServerCore.RPCServer.RemoteServer.*;
import iZomateCore.UtilityCore.TimeUtils;

import iZomateCore.AppCore.HostApps.*;

import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Testbed {
	private RemoteServer			m_pRemoteServer= null;
	private HostApp					m_pActiveHostApp= null;
	private Test					m_pTest= null;
	private Logs					m_pEmergencyLogs= null;
	private Map<String, HostApp>	m_mHostApps= new HashMap<String, HostApp>();
	private String 					m_strCrashReportMessage= null;
	private String 					m_strOrigTheme= null;

	/**
	 * 
	 * @param strTestbed
	 * @param pTest
	 * @throws Exception
	 */
	public Testbed( String strTestbed, Test pTest ) throws Exception {
		this.m_pTest= pTest;
		this.m_pRemoteServer= new RemoteServer( strTestbed, this._Logs() );
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public Logs _Logs() throws Exception {
		if( this.m_pTest != null )
			return this.m_pTest._Logs();
		else {
			if( this.m_pEmergencyLogs == null )
				this.m_pEmergencyLogs= new Logs( null, "DefaultTestbedLog", 0 , true );
			return this.m_pEmergencyLogs;
		}		
	}
	
	/**
	 * 
	 * @return
	 */
	public Test _Test() {
		return this.m_pTest;
	}
		
	/**
	 * 
	 * @param strHostApp
	 * @return
	 * @throws Exception
	 */
	public HostApp _HostApp( String strHostApp ) throws Exception {
		this.m_pActiveHostApp= this.m_mHostApps.get( strHostApp );
		if( this.m_pActiveHostApp == null) {
			HostType ht= HostType.getEnum( strHostApp );

			if( ht == HostType.AbletonLiveMac )
				this.m_pActiveHostApp= new AbletonLive9( strHostApp, this );
			else if( ht == HostType.ProToolsMac || ht == HostType.ProToolsWin )
				this.m_pActiveHostApp= new ProTools11( strHostApp, this );
			else
				this.m_pActiveHostApp= new HostApp( strHostApp, this );
			
			this.m_mHostApps.put( strHostApp, this.m_pActiveHostApp );
		}
		
		return this.m_pActiveHostApp;
	}
	
	/**
	 * 
	 * @param nIndex
	 * @return
	 * @throws Exception
	 */
	public HostApp _HostApp( int nAppNum ) throws Exception {
		if( this.m_pActiveHostApp == null || nAppNum > this.m_mHostApps.size() )
			return null;
		
		return this._HostApp( (String) this.m_mHostApps.keySet().toArray()[nAppNum-1] ); // Convert to 0 based
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public HostApp _HostApp() throws Exception {
		if( this.m_pActiveHostApp == null )
			throw new Exception( "A HostApp has not been set.  You must call _SetHostApp() before calling _HostApp()" );
		return this.m_pActiveHostApp;
	}
	
	/**
	 * 
	 * @return
	 */
	public String[] _GetHostApps() {
		return this.m_mHostApps.keySet().toArray( new String[0] );
	}

	/**
	 * 
	 * @param strAudioFile
	 * @return
	 * @throws Exception
	 */
	public RemoteFile _CreateRemoteFile( String strAudioFile ) throws Exception {
		return this.m_pRemoteServer._createRemoteFile( strAudioFile );
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public RemoteRobot _Robot() throws Exception {
		return this.m_pRemoteServer._Robot();
	}
	
	/**
	 * 
	 * @return
	 */
	public String _GetMachineName() {
		return this.m_pRemoteServer._GetMachineName();
	}
	
	/**
	 * The MIDI methods that can be executed on the remote system
	 * 
	 * @return MIDIrs
	 * @throws Exception
	 */
	public MIDIrs _MIDI() throws Exception {
		return this.m_pRemoteServer._MIDI();
	}

    /**
     * 
     * @param appFile
     * @param args
     * @throws Exception
     */
    public void _LaunchApp( RemoteFile appFile, String args ) throws Exception {
    	this.m_pRemoteServer._LaunchApp( appFile, args );
    }

    /**
     * Checks if the specified application is running.
     *
     * @param processName the process name of the application to check
     * @return true if the application is still running
     * @throws Exception
     */
    public int _IsAppRunning( String strProcessName ) throws Exception {
	   return this._RemoteServer()._IsAppRunning( strProcessName );
	}
    
    /**
     * Waits up to the specified number seconds for app to shutdown.  If app has not shutdown in that time then it will call force quit.
     * @param strAppName
     * @param timeout
     * @throws Exception
     */
    public void _WaitForAppToQuit( String strAppName, int timeout, boolean bLogErrorOnForceQuit ) throws Exception {
		long stopTime = System.currentTimeMillis() + (timeout*1000);
		
		TimeUtils.sleep( .5 );

		// Wait for process to go away
		while( this._IsAppRunning( strAppName ) > 0 ) {
			if( System.currentTimeMillis() > stopTime ) {
				if( bLogErrorOnForceQuit )
					this._Logs()._ResultLog()._logError( strAppName + " failed to shutdown.  Killing process.", true );
				else
					this._Logs()._ResultLog()._logWarning( strAppName + " failed to shutdown.  Killing process." );

				this._ForceQuit( strAppName, 10 );
				break;
			}
			TimeUtils.sleep( timeout - System.currentTimeMillis() > stopTime ? 1 : timeout - System.currentTimeMillis() );
		}
    }
    
    /**
     * Force quits the specified application.
     *
     * @param processName the process name of the application to force quit
     * @param attempts how many times to attempt to force quit the process (with a 5 second sleep between each attempt)
     * @return true if the process is no longer running, false if the process could not be stopped
     * @throws Exception
     */
    public boolean _ForceQuit( String strProcessName, int nAttempts ) throws Exception {
    	if( this._IsAppRunning( strProcessName ) != 0 )
    		return this._RemoteServer()._ForceQuit( strProcessName, nAttempts );
    	return true;
    }
	
	/**
     * Attempts to connect to the remote C++ RPCServer running on the specified port ignoring the server id and tell it to shutdown its connection and free up the port.
	 * @param port
	 * @throws Exception
	 */
    public void _ForceSocketShutdown( int port ) throws Exception
    {
    	RPCServer pServer= new RPCServer( this._GetMachineName(), port, this._Logs() );  	
    	pServer._ShutdownRemoteSocket();
    }
    
	/**
	 * The system info methods for the remote system
	 * 
	 * @return RemoteSystemInfo
	 * @throws Exception
	 */
	public RemoteSystemInfo _SysInfo() throws Exception {
		return this.m_pRemoteServer._SysInfo();
	}
	
	/**
	 * 
	 * @return
	 */
	public RemoteServer _RemoteServer() {
		return this.m_pRemoteServer;
	}

	/**
	 * 
	 * @param fSourceFile
	 * @param strDestinationFile
	 * @param strUserMachine
	 * @param bRecursiveCopy
	 * @throws Exception
	 */
	public void _SCP( File fSourceFile, String strDestinationFile, String strUserMachine, boolean bRecursiveCopy ) throws Exception {				
		String strFlag= bRecursiveCopy?"-r":"";
		String strDest= strUserMachine + ":/" + strDestinationFile;
		System.out.println( "\n\nWorking dir: " + fSourceFile.getParent() );
		System.out.println( "scp " + strFlag + " \"" + fSourceFile.getName() + "\" \"" + strDest + "\"" );
		
		Process p= Runtime.getRuntime().exec( new String[]{ "scp",  strFlag, fSourceFile.getName(), strDest}, null, fSourceFile.getParentFile() );
		
        BufferedInputStream inputStream = new BufferedInputStream(p.getInputStream());
        byte stdOut[]= new byte[ inputStream.available() ];
        inputStream.read( stdOut );

        BufferedInputStream errorStream = new BufferedInputStream(p.getErrorStream());
        byte stdErr[]= new byte[ errorStream.available() ];
        errorStream.read( stdErr );

       p.waitFor();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void _DismissCrashReporter( boolean bSubmitReport ) throws Exception {
		//TODO: Pull crash reporter out and put into its own class
		String strIZCrashReporter= "iZotope Crash Reporter" + (this._SysInfo()._isMac() ? ".app" : ".exe");

		if( this._IsAppRunning( strIZCrashReporter ) > 0 ) {
			this._Logs()._ResultLog()._logMessage( "Crash Reporter has been detected!" );
			
			RPCServer crashReporter= new RPCServer(this._GetMachineName(), "APP:CrashReporter", this._Logs());
			if( crashReporter._connectToClient() ) {
				Buttons btn= Buttons.CR_IgnoreError; //default
				WindowControls wc= new WindowControls( crashReporter, this );
				
				if( bSubmitReport ) {
					//TODO: figure out why we don't get the click notification for the comments box
					//wc._Button(Buttons.CR_CommentsBox)._click();
					this._Robot()._mouseClick(wc._Button(Buttons.CR_CommentsBox)._info()._point(ClickSpot.Center), 2, MouseButtons.Left);
					this._Robot()._keyType("Submitted by Test Automation script:" + 
											"\nTest: " + this.m_pTest._GetCurrentTestCaseParams()._GetTestScriptName() + 
											"\nTestbed: " + this.m_pRemoteServer._GetMachineName() +
											"\nApp: " + this.m_pTest._GetCurrentTestCaseParams()._GetApp() );
					TimeUtils.sleep( .5 );

					btn= Buttons.CR_SendToiZotope;
					this.m_strCrashReportMessage= "A crash report was submitted at " + TimeUtils.getTimestamp() + (this.m_strCrashReportMessage!=null ? "<br>" + this.m_strCrashReportMessage : "" );
				}
				
				this._Robot()._mouseClick( wc._Button(btn)._info()._point(ClickSpot.Center), 1, MouseButtons.Left );
				this._Logs()._ResultLog()._logMessage( "Clicked Crash Reporter button: " + btn._getValue() );
				
				this._WaitForAppToQuit( strIZCrashReporter, 5, true );
			}
			else if( !this._ForceQuit( strIZCrashReporter , 5 ) )
				throw new Exception( "An instance of " + strIZCrashReporter + " was detected but we could not connect to it or forcibly terminate its process");
		}		
	}

	/**
	 * @return The crash report message or null
	 */
	public String _GetCrashReportMessage() {
		return this.m_strCrashReportMessage;
	}
	
	/**
	 * Forces the Windows theme to Classic and returns orig theme
	 * @throws Exception
	 */
	public void _SetWinThemeToClassic() throws Exception {
		String strClassicTheme= "C:\\Windows\\Resources\\Ease of Access Themes\\classic.theme";
		String strCurrentTheme= this._Test()._Testbed()._RemoteServer()._runCommand( "REG QUERY \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\" /v CurrentTheme" ).split("    ")[3];

		// Force to classic theme
		if( !strCurrentTheme.equalsIgnoreCase( strClassicTheme ) ) {
			this.m_strOrigTheme= strCurrentTheme;
			this._Test()._Testbed()._RemoteServer()._runCommand( "\"" + strClassicTheme + "\"" );
			TimeUtils.sleep( 1 );
			this._Test()._Testbed()._RemoteServer()._runCommand( "\"" + strClassicTheme + "\"" ); // The second time forces the Personalization window to the front!
			this._Test()._Testbed()._Robot()._keyType( java.awt.event.KeyEvent.VK_CONTROL, java.awt.event.KeyEvent.VK_W ); // Close the Personalization window
		}
	}

	/**
	 * Restores the original Windows theme if it had been changed
	 * @throws Exception
	 */
	public void _SetWinThemeToOriginal() throws Exception {
		if( this.m_strOrigTheme != null ) {
			this._Test()._Testbed()._RemoteServer()._runCommand( "\"" + this.m_strOrigTheme + "\"" );
			TimeUtils.sleep( 1 );
			this._Test()._Testbed()._RemoteServer()._runCommand( "\"" + this.m_strOrigTheme + "\"" ); // The second time forces the Personalization window to the front!
			this._Test()._Testbed()._Robot()._keyType( java.awt.event.KeyEvent.VK_CONTROL, java.awt.event.KeyEvent.VK_W ); // Close the Personalization window
		}
	}

	/**
	 * 
	 * @param strBuildArchiveProductDir
	 * @param strInstallerName
	 * @param strMacMountedVolumeName
	 * @param strPassword
	 * @throws Exception
	 */
	public void _DownloadAndRunInstaller( String strBuildArchiveProductDir, String strInstallerName, String strMacMountedVolumeName, String strPassword ) throws Exception
	{
		if( this._SysInfo()._isMac() )
			this._DownloadAndRunInstallerMac( strBuildArchiveProductDir, strInstallerName, strMacMountedVolumeName, strPassword, null, null, null );
		else
			this._DownloadAndRunInstallerWin( strBuildArchiveProductDir, strInstallerName, null, null, null );
	}
	
	/**
	 * 
	 * @param strBuildArchiveProductDir
	 * @param strInstallerName
	 * @param strMacMountedVolumeName
	 * @param strPassword
	 * @param strSerial
	 * @param strName
	 * @param strEmail
	 * @throws Exception
	 */
	public void _DownloadAndRunInstallerAuth( String strBuildArchiveProductDir, String strInstallerName, String strMacMountedVolumeName, String strPassword, String strSerial, String strName, String strEmail ) throws Exception
	{
		if( this._SysInfo()._isMac() )
			this._DownloadAndRunInstallerMac( strBuildArchiveProductDir, strInstallerName, strMacMountedVolumeName, strPassword, strSerial, strName, strEmail );
		else
			this._DownloadAndRunInstallerWin( strBuildArchiveProductDir, strInstallerName, strSerial, strName, strEmail );
	}
	
	/**
	 * 
	 * @param strBuildArchiveProductDir
	 * @param strInstallerName
	 * @throws Exception
	 */
	private void _DownloadAndRunInstallerWin( String strBuildArchiveProductDir, String strInstallerName, String strSerial, String strName, String strEmail ) throws Exception
	{	
		RemoteFile rfInstaller= this._DownloadInstaller( strBuildArchiveProductDir, strInstallerName );
		this._Test()._Logs()._ResultLog()._logLine( "Launching installer: " + rfInstaller._getAbsolutePath(), true );
		this._Test()._Testbed()._LaunchApp( rfInstaller, null );
		TimeUtils.sleep( 2 );
		
		try {
			this._Test()._Logs()._ResultLog()._logLine( "Handling Installer GUI", true );
			String os= this._Test()._Testbed()._RemoteServer()._SysInfo()._GetOSName();
			this._Test()._Logs()._ResultLog()._logLine( "Waiting for image: " + Images.Inst_RunButton._For( os ).name(), true );
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_RunButton._For( os ), 1, 20, true, 60 ); //Open File Security Warning Dialog
			
			TimeUtils.sleep( 2 );
			this._Test()._Logs()._ResultLog()._logLine( "Checking for Auth Wizard", true );
			if ( this._Test()._Testbed()._Robot()._imageFind(Images.Auth_AuthWizard) != null ) { // If the Installer pops up Auth check
				if ( strSerial == null || strName == null || strEmail == null ) 
					throw new Exception("Installer requires Authorization Serial, Name and Email" );
				this._AuthorizeProduct(strSerial, strName, strEmail);
			}
			
			this._Test()._Logs()._ResultLog()._logLine( "Selecting language", true );
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_OKButton._For( os ), 1, 2 ); // Select Setup Language Dialog
			TimeUtils.sleep( 2 );
			this._Test()._Logs()._ResultLog()._logLine( "Checking for Auth Wizard again", true );
			if ( this._Test()._Testbed()._Robot()._imageFind(Images.Auth_AuthWizard) != null ) { // If the Installer pops up Auth check
				if ( strSerial == null || strName == null || strEmail == null ) 
					throw new Exception("Installer requires Authorization Serial, Name and Email" );
				this._AuthorizeProduct(strSerial, strName, strEmail);
			}
			TimeUtils.sleep( 2 ); // Splash Screen
			this._Test()._Logs()._ResultLog()._logLine( "Handling Welcome page", true );
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_NextButton, 1, 4 ); // Welcome Page
			this._Test()._Logs()._ResultLog()._logLine( "Handling License Agreement", true );
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_AcceptButton, 1, 2 ); // License Agreement Page
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_NextButton, 1, 2 ); // License Agreement Page
			this._Test()._Logs()._ResultLog()._logLine( "Handling Select Destination Location Page", true );
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_NextButton, 1, 2 ); // Select Destination Location
			this._Test()._Logs()._ResultLog()._logLine( "Handling Components Page", true );
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_NextButton, 1, 2 ); // Select Components
			this._Test()._Logs()._ResultLog()._logLine( "Checking for Sound Library Page", true );
			if( this._Test()._Testbed()._Robot()._imageFind( Images.Inst_SelectSoundLibContFolderText, 2, false ) != null ) {
				this._Test()._Logs()._ResultLog()._logLine( "Handling Sound Library Page", true );
				this._Test()._Testbed()._Robot()._imageClick( Images.Inst_NextButton, 1, 2 ); // Sound Library Content Folder
			}
			this._Test()._Logs()._ResultLog()._logLine( "Handling 64 bit VST Plugins Page", true );
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_NextButton, 1, 2 ); // Select VST Plugins Folder (64bit)
			this._Test()._Logs()._ResultLog()._logLine( "Handling 32 bit VST Plugins Page", true );
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_NextButton, 1, 2 ); // Select VST Plugins Folder (32bit)
			this._Test()._Logs()._ResultLog()._logLine( "Handling Start Menu Folder Page", true );
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_NextButton, 1, 2 ); // Select Start Menu Folder
			this._Test()._Logs()._ResultLog()._logLine( "Handling Additional Tasks Page: Check for iZotope Updates", true );
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_CheckForUpdatesButton, 1, 2 ); // Select Additional Tasks
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_NextButton, 1, 2 ); // Select Additional Tasks
			this._Test()._Logs()._ResultLog()._logLine( "Starting Install...", true );
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_InstallButton, 1, 2, true ); // Ready to Install
			TimeUtils.sleep( 5 );
			for( int i= 0; i <= 360; ++i ) {
				if( this._Test()._Testbed()._Robot()._imageFind( Images.Inst_CancelButton ) == null 
					&& this._Test()._Testbed()._Robot()._imageFind( Images.Inst_CancelButtonDisabled ) == null )
					break;
				TimeUtils.sleep( 1 );
			}
			TimeUtils.sleep( 2 );
			this._Test()._Logs()._ResultLog()._logLine( "Install Complete", true );
			this._Test()._Logs()._ResultLog()._logLine( "Handling Information Page", true );
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_NextButton, 1, 5 ); // Information Page
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_NextButton, 1, 2 ); 
			this._Test()._Logs()._ResultLog()._logLine( "Attempting to uncheck Launch Application", true );
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_LaunchApplication, 1, 2 ); // Uncheck Launch Application
			this._Test()._Logs()._ResultLog()._logLine( "Clicking Finish button", true );
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_FinishButton, 1, 2, true );
		}
		catch( Exception e ) {
			this._Test()._Logs()._ResultLog()._logError( "Force Quitting: " + rfInstaller._getName(), true );
			this._Test()._Testbed()._ForceQuit( rfInstaller._getName(), 1 );
			throw e;
		}
	}
	
	/**
	 * 
	 * @param strBuildArchiveProductDir
	 * @param strInstallerName
	 * @param strMacMountedInstallerPkg
	 * @throws Exception
	 */
	private void _DownloadAndRunInstallerMac( String strBuildArchiveProductDir, String strInstallerName, String strMacMountedInstallerPkg, String strPassword, String strSerial, String strName, String strEmail ) throws Exception
	{
		StringBuffer stdOut= new StringBuffer();
		StringBuffer stdErr= new StringBuffer();
		String strVolumeName= strMacMountedInstallerPkg.split( "/" )[2];
		
		this._Test()._Testbed()._RemoteServer()._commandLine( new String[]{ "diskutil", "umount", strVolumeName }, stdOut, stdErr, true );
		RemoteFile rfInstallerDMG= this._DownloadInstaller( strBuildArchiveProductDir, strInstallerName );
		RemoteFile rfInstallerPkg= this._Test()._Testbed()._CreateRemoteFile( strMacMountedInstallerPkg );
				
		// Mount the DMG
		this._Test()._Logs()._ResultLog()._logLine( "Launching DMG: " + rfInstallerDMG._getAbsolutePath() );
		this._Test()._Testbed()._LaunchApp( rfInstallerDMG, null );
		TimeUtils.sleep( 2 );
		
		// Wait for volume to mount
		for( int i= 0; i <= 20; ++i ) {
			if( rfInstallerPkg._exists() )
				break;
			else if( i == 20 )
				throw new Exception("Installer not found: " + rfInstallerPkg._getPathAndName() );
			
			TimeUtils.sleep( 1 );
		}

		this._Test()._Logs()._ResultLog()._logLine( "Launching installer: " + rfInstallerPkg._getAbsolutePath() );
		this._Test()._Testbed()._LaunchApp( rfInstallerPkg, null );
		TimeUtils.sleep( 3 );

		try {
			this._Test()._Logs()._ResultLog()._logLine( "Handling Installer GUI" );
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_ContinueBtn, 1, 20, true ); // Welcome page 
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_ContinueBtn, 1, 4, true ); // Software License page 
			TimeUtils.sleep( .7 ); // wait because the button slides down and we can click in the wrong location
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_AgreeBtn, 1, 4, true ); // Software License Agree popup
			TimeUtils.sleep( 1 );
			if( this._Test()._Testbed()._Robot()._imageFind( Images.Inst_DestinationSelectLabel ) != null ) { // Destination Select page 
				this._Test()._Testbed()._Robot()._imageClick( Images.Inst_InstallForLabel, 1, 4, true ); // Sometimes we need to click on it in order to make the continue button enabled
				this._Test()._Testbed()._Robot()._imageClick( Images.Inst_ContinueBtn, 1, 10, true ); 
				TimeUtils.sleep( 1 );
			}
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_ContinueBtn, 1, 4, false ); // Sample Content Destination Select page
			TimeUtils.sleep( 1 );
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_InstallBtn, 1, 4, true ); // Standard Install page
			TimeUtils.sleep( 1 );
			// Handle Password
			if( this._Test()._Testbed()._Robot()._imageFind( Images.Inst_PasswordLabel, 10, true ) != null) {
				this._Test()._Testbed()._Robot()._keyType( strPassword );
				this._Test()._Testbed()._Robot()._keyType( "\n" );
			}
			for( int i= 0; i <= 60; ++i ) {
				if( this._Test()._Testbed()._Robot()._imageFind(Images.Auth_AuthWizard._For(this._Test()._Testbed()._RemoteServer()._SysInfo()._GetOSName())) != null) {
					if ( strSerial == null || strName == null || strEmail == null ) throw new Exception("Installer requires Authorization Serial, Name and Email" );
					this._AuthorizeProduct(strSerial, strName, strEmail);
				}
				if( this._Test()._Testbed()._Robot()._imageFind( Images.Inst_Success ) != null ) 
					break;
				TimeUtils.sleep( 1 );
			}
			this._Test()._Testbed()._Robot()._imageClick(Images.Inst_Success, 1, 20, true);
			this._Test()._Testbed()._Robot()._keyPress(KeyEvent.VK_ENTER);
			//this._Test()._Testbed()._Robot()._imageClick( Images.Inst_CloseBtn, 1, 60, true ); // Close Installer
			//if( this._Testbed()._Robot()._imageFind( this.m_strLocalImageDir + "/OSX_WYLTCFUpdatesText2.bmp" ) != null ) 
		}
		catch( Exception e ) {
			this._Test()._Logs()._ResultLog()._logError( "Force Quitting: " + rfInstallerPkg._getName(), true );
			this._Test()._Testbed()._ForceQuit( "Installer", 1 );
			throw e;
		}
		finally {		
			TimeUtils.sleep( .25 );
			this._Test()._Testbed()._Robot()._imageClick( Images.Inst_NoBtn, 1, 4, false ); // Click No on the Check for Updates dialog
			TimeUtils.sleep( 1 );
			this._Test()._Testbed()._RemoteServer()._commandLine( new String[]{ "diskutil", "umount", strVolumeName }, stdOut, stdErr, true );
		}
	}
	
	/**
	 *  Uninstalls the app/plugin specified
	 * @param strProductName
	 * @throws Exception
	 */
	public void _uninstallProduct(String strProductName) throws Exception {
		this._uninstallProduct( strProductName, "");
	}
	
	/**
	 * Uninstalls the app or plugin specified.
	 * @param strProductName
	 * @param strPassword This is the UAC password for the computer
	 * @throws Exception
	 */
	public void _uninstallProduct(String strProductName, String strPassword ) throws Exception {
		if( this._SysInfo()._isMac() )
			this._uninstallMac( strProductName, strPassword );
		else
			this._uninstallWin( strProductName );
	}
	
	/**
	 * 
	 * @param strProductName
	 * @throws Exception
	 */
	private void _uninstallWin( String strProductName ) throws Exception {
		String strSysDrive= this._Test()._Testbed()._RemoteServer()._runCommand("echo", "%SYSTEMDRIVE%");
		boolean b64= this._Test()._Testbed()._SysInfo()._GetOSArch().equals( "64-bit" );
		
		RemoteFile rfUninstaller= this._Test()._Testbed()._CreateRemoteFile(strSysDrive + "/Program Files" + (b64?" (x86)":"") + "/iZotope/" + strProductName + "/unins000.exe");
		if (strProductName.contains("/")){
			rfUninstaller= this._Test()._Testbed()._CreateRemoteFile(strProductName.replace("{userdocs}", strSysDrive 
																										  + "/Users/" 
																										  + this._Test()._Testbed()._RemoteServer()._runCommand( "echo", "%USERNAME%" )
																										  + "/Documents"));
		}
		
		if (!rfUninstaller._exists())
			throw new Exception("Uninstaller not found: " + rfUninstaller._getAbsolutePath());
		
		this._Test()._Testbed()._LaunchApp( rfUninstaller, null);
		TimeUtils.sleep( 1 );
		
		String os= this._Test()._Testbed()._RemoteServer()._SysInfo()._GetOSName();
		this._Test()._Testbed()._Robot()._imageClick( Images.Inst_YesButton._For( os ), 1, 10 );
		
		for( int i= 0; i <= 60; ++i ) {
			if( this._Test()._Testbed()._Robot()._imageFind( Images.Inst_YesToAll ) != null )
				this._Test()._Testbed()._Robot()._imageClick( Images.Inst_YesToAll, 1 );
			if( this._Test()._Testbed()._Robot()._imageFind( Images.Inst_CancelButton ) == null 
				&& this._Test()._Testbed()._Robot()._imageFind( Images.Inst_CancelButtonDisabled ) == null )
				break;
			TimeUtils.sleep( 1 );
		}
		
		TimeUtils.sleep( .5 );
		
		this._Test()._Testbed()._Robot()._imageClick( Images.Inst_OKButton._For( os ), 1 );
	}
	
	/**
	 * 
	 * @param strProductName
	 * @throws Exception
	 */
	private void _uninstallMac( String strProductName, String strPassword ) throws Exception {
		RemoteFile rfUninstaller= this._Test()._Testbed()._CreateRemoteFile("/Library/Application Support/iZotope/" + strProductName + "/Uninstall iZotope " + strProductName + ".app");
		if (strProductName.contains("/")){
			rfUninstaller= this._Test()._Testbed()._CreateRemoteFile(strProductName);
		}
		
		if( !rfUninstaller._exists() )
			throw new Exception( "Uninstaller not found: " + rfUninstaller._getAbsolutePath() );
		
		this._Test()._Testbed()._LaunchApp( rfUninstaller, null );
		TimeUtils.sleep( .5 );		
		this._Test()._Testbed()._Robot()._imageClick( Images.Inst_YesButton, 1, 10, true );
		TimeUtils.sleep( 1 );
		this._Test()._Testbed()._Robot()._imageClick( Images.Inst_PasswordLabel, 1, 10, false );
		this._Test()._Testbed()._Robot()._keyType( strPassword );
		this._Test()._Testbed()._Robot()._keyType( "\n" );
		TimeUtils.sleep( 1 );		
		for( int i= 0; i <= 60; ++i ) {
			if ( this._Test()._Testbed()._Robot()._imageFind( Images.Inst_UninstallSuccess) != null) {
				this._Test()._Testbed()._Robot()._imageClick( Images.Inst_UninstallSuccess, 1, 10, true );
				break;
			}
			if ( this._Test()._Testbed()._Robot()._imageFind( Images.Inst_UnRemovedFiles) != null) {
				this._Test()._Testbed()._Robot()._imageClick( Images.Inst_UnRemovedFiles, 1, 10, true );
				break;
			}
			TimeUtils.sleep(1);
		}
		this._Test()._Testbed()._Robot()._keyType( "\n" );
	}
	
	
	/**
	 * 
	 * @param strBuildArchiveProductDir
	 * @param strInstallerName
	 * @return
	 * @throws Exception
	 */
	private RemoteFile _DownloadInstaller( String strBuildArchiveProductDir, String strInstallerName ) throws Exception
	{
		String idFile= System.getProperty("user.dir") + "/Jars/SSH/iztestauto_id_rsa";
		String strAccount= "iztestauto";
		String strServer= "monster";
		String strLatest= null;
		String strInstallerFile= null;
		String strEscInstallerName= strInstallerName.replace( " ", "\\ " ).replace( "(", "\\(" ).replace( ")", "\\)" );
		String fullInstaller= null;
				
		//Find the latest installer up on build archives
		this._Test()._SSH( strAccount, strServer, idFile );

		if( strInstallerName.contains("*") ) { // Find Latest.  Handles non wildcard cases in strBuildArchiveProductDir as well.  i.e. "/build_archive/BreakTweaker/Build-full-*-BreakTweaker" vs "/build_archive/BreakTweaker"
			strLatest= this._Test()._SSH()._ExecCommand( "ls -t " + strBuildArchiveProductDir + " | head -n1" )._GetResult().replace( ":", "" );
			if( !strLatest.isEmpty() && !strLatest.startsWith( strBuildArchiveProductDir.substring( 0, strBuildArchiveProductDir.indexOf( "*" )!=-1?strBuildArchiveProductDir.indexOf( "*" ):strBuildArchiveProductDir.length() ) ))
				strLatest= strBuildArchiveProductDir + "/" + strLatest;
			fullInstaller= this._Test()._SSH()._ExecCommand( "ls " + strLatest + "/" + strEscInstallerName )._GetResult();
			strInstallerFile= fullInstaller.replace( strLatest + "/", "" );
		}
		else {
			//Make sure it exists
			strInstallerFile= this._Test()._SSH()._ExecCommand( "cd " + strBuildArchiveProductDir + "/" + ";ls " + strEscInstallerName )._GetResult();
			fullInstaller= strBuildArchiveProductDir + "/" + strInstallerFile;
		}

		if( strInstallerFile == null || strInstallerFile.isEmpty() )
			throw new Exception( "No installer file matching \"" + strInstallerName + "\" was found in \"" + strBuildArchiveProductDir + "/" + strLatest + "\"" );

		// Find the build number in the strInstallerFile i.e.: iZotope BreakTweaker Web Image 1.00_(Build_490).dmg
		String[] strTokens= strInstallerFile.replace( " ", "_" ).replace( "-", "_" ).replace( "(", "_" ).replace( ")", "_" ).split( "_" );
		// Assume build number is the int near the end of the string so work back to front
		for( int i= strTokens.length-1; i > 0; --i ) {
			try {
				this._Test()._setBuildNumber( String.valueOf( Integer.parseInt( strTokens[i] ) ) );
				break;
			} catch( Exception e ) {
				// Keep looping until the above call succeeds in converting the string to an int.
			}
		}

		if( this._Test()._getBuildNumber() == null )
			throw new Exception( "Failed to extract buildnumber from installer file: \"" + strInstallerFile + "\"" );

		String strUser= this._RemoteServer()._SysInfo()._GetUser();
		String strDownloadedInstaller= strUser + "/Downloads/" + strInstallerFile;
		
		StringBuffer stdOut= new StringBuffer();
		StringBuffer stdErr= new StringBuffer();
		String strBrowser= this._SysInfo()._isMac() ? "/Applications/Safari.app" : "C:/Program Files (x86)/Google/Chrome/Application/chrome.exe";
		String strURL= "http://buildarchive.izotope.int" + fullInstaller.replace( "/build_archive/", "/archive/" );	
		this._Test()._Logs()._ResultLog()._logLine( "Downloading installer: " + strURL, true );
		
		if( this._SysInfo()._isWin() ) {
			// If this folder doesn't exists we're 32-bit
			if ( this._Test()._Testbed()._SysInfo()._GetOSArch().equals( "32-bit" )) 
				strBrowser= strBrowser.replace(" (x86)", "");
		}
		
		if( !this._Test()._Testbed()._CreateRemoteFile( strBrowser )._exists() ) 
			throw new Exception( "The browser, " + strBrowser + ", is required to download installers" );

		this._Test()._Logs()._ResultLog()._logLine( "Deleting existing installer: " + strDownloadedInstaller, true );
		this._Test()._Testbed()._RemoteServer()._commandLine( new String[]{ "rm", "-f", strDownloadedInstaller }, false );
		this._Test()._Logs()._ResultLog()._logLine( "Starting download", true );
		if( this._SysInfo()._isMac() )
			this._Test()._Testbed()._RemoteServer()._commandLine( new String[]{ "open", "-a", strBrowser, strURL }, stdOut, stdErr, true );
		else {
			this._Test()._Testbed()._RemoteServer()._commandLine( new String[]{ strBrowser }, false );
			TimeUtils.sleep( 1 );
			this._Test()._Testbed()._RemoteServer()._commandLine( new String[]{ strBrowser, strURL }, stdOut, stdErr, true );
		}
		
		TimeUtils.sleep( 5 );

		RemoteFile rfInstaller= this._Test()._Testbed()._CreateRemoteFile( strDownloadedInstaller );
		
		// Wait for Installer to download and then close the browser window
		for( int i= 0; i <= 300; ++i ) {
			if( rfInstaller._exists( 60 ) ) {
				this._Test()._Logs()._ResultLog()._logLine( "Download complete", true );
				TimeUtils.sleep( 1 );
				if( this._SysInfo()._isMac() ) {
					this._Test()._Logs()._ResultLog()._logLine( "Closing browser window", true );
					this._Test()._Testbed()._Robot()._keyType( java.awt.event.KeyEvent.VK_META, java.awt.event.KeyEvent.VK_W ); // Close the Safari window
				}
				else {
					TimeUtils.sleep( 5 ); // sometimes the file is there but the download is not complete so wait a bit more				
					this._Test()._Logs()._ResultLog()._logLine( "Closing browser window", true );
					this._Test()._Testbed()._Robot()._keyType( java.awt.event.KeyEvent.VK_CONTROL, java.awt.event.KeyEvent.VK_W ); // Close the Chrome window
					TimeUtils.sleep( 1 );
				}
				break;
			}
			else if( i == 300 )
				throw new Exception("DMG not found: " + rfInstaller._getPathAndName() );
			
			TimeUtils.sleep( 1.5 );
		}
		
		return rfInstaller;
	}
	
	/**
	 * 
	 * @param strSerialNumber
	 * @param strName
	 * @param strEmail
	 */
	public void _AuthorizeProduct(String strSerialNumber, String strName, String strEmail ) throws Exception {
		this._Test()._Logs()._ResultLog()._logLine( "Handling Authorization" );
		String os= this._Test()._Testbed()._RemoteServer()._SysInfo()._GetOSName();
		int iControlKey = KeyEvent.VK_CONTROL;
		if (os == "OSX") iControlKey = KeyEvent.VK_META;
		this._Test()._Testbed()._Robot()._imageClick(Images.Auth_AuthWizard._For(os), 1, 5, false);
		
		if ( os == "OSX")
			this._Test()._Testbed()._Robot()._keyPress(KeyEvent.VK_ENTER); // First Auth Screen
		else
			this._Test()._Testbed()._Robot()._imageClick( Images.Auth_AuthorizeButton._For(os), 1, 10, false ); // First Auth Screen
		TimeUtils.sleep(.5);
		// Enter Serial Number
		this._Test()._Testbed()._Robot()._imageClick(Images.Auth_SerialNumberBox._For(os), 1, new java.awt.Point(120,3));
		this._Test()._Testbed()._Robot()._keyType(iControlKey, KeyEvent.VK_A);
		this._Test()._Testbed()._Robot()._keyType( strSerialNumber );
		// Enter Name
		this._Test()._Testbed()._Robot()._imageClick(Images.Auth_NameBox._For(os), 1, new java.awt.Point(120,4));
		this._Test()._Testbed()._Robot()._keyType(iControlKey, KeyEvent.VK_A);
		this._Test()._Testbed()._Robot()._keyType( strName );
		// Enter Email
		this._Test()._Testbed()._Robot()._imageClick(Images.Auth_EmailBox._For(os), 1, new java.awt.Point(120,4));
		this._Test()._Testbed()._Robot()._keyType(iControlKey, KeyEvent.VK_A);
		this._Test()._Testbed()._Robot()._keyType( strEmail );
		if ( os == "OSX")
			this._Test()._Testbed()._Robot()._keyPress(KeyEvent.VK_ENTER);
		else
			this._Test()._Testbed()._Robot()._imageClick(Images.Auth_AuthorizeButton._For(os), 1, 10, false);
		TimeUtils.sleep(1);
		if ( os == "OSX")
			this._Test()._Testbed()._Robot()._keyPress(KeyEvent.VK_ENTER); // Submit Auth Screen
		else
			this._Test()._Testbed()._Robot()._imageClick(Images.Auth_SubmitButton._For(os), 1, 10, false);
		
		for( int i= 0; i <= 60; ++i ) {
			if ( this._Test()._Testbed()._Robot()._imageFind(Images.Auth_Successful._For(os)) != null)
				break;
			TimeUtils.sleep(.5);
		}
		
		if ( os == "OSX")
			this._Test()._Testbed()._Robot()._keyPress(KeyEvent.VK_ENTER); // Continue after Auth
		else
			this._Test()._Testbed()._Robot()._imageClick(Images.Auth_ContinueButton._For(os), 1, 10, true);
		
	}
}
