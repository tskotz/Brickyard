package iZomateCore.iZTests;

import java.awt.Point;
import java.awt.event.KeyEvent;

import iZomateCore.UtilityCore.TimeUtils;

import iZomateCore.AppCore.AppEnums.Images;
import iZomateCore.AppCore.AppEnums.Insert;
import iZomateCore.AppCore.AppEnums.PluginType;
import iZomateCore.AppCore.AppEnums.TrackFormat;
import iZomateCore.AppCore.AppEnums.TrackType;
import iZomateCore.AppCore.AppEnums.HostType;
import iZomateCore.AppCore.HostApps.ProTools11;

import iZomateCore.ServerCore.RPCServer.RemoteServer.RemoteFile;
import iZomateCore.TestCore.Test;
import iZomateCore.TestCore.TestCaseParameters;

/**
 * ProTools 11 Host API
 * This is a set of methods to allow us to automate a host.
 * @author tskotz/nlapenn
 *
 */
public class ProTools11Test extends Test {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public String m_pluginCategory;
	public String m_pluginName;
	public String m_audioFile;
	
	public static void main(String[] args) throws Exception	{
		new ProTools11Test( args ).run();
	}

	/**
	 * Constructor
	 * @param args command line args
	 * @throws Exception
	 */
	protected ProTools11Test( String[] args ) throws Exception {
		super( args );
	}

	/**
	 * Gets called only once before the test cases are run
	 */
	@Override
	protected void _StartUp( TestCaseParameters pCommonParameters ) throws Exception {
		System.out.println( "StartUp!" );
	}
	
	/**
	 * Gets called for each test case defined in test input file
	 */
	protected void _TestCase( TestCaseParameters pParams ) throws Exception {
		this._LaunchPT( pParams );
		ProTools11 pProTools= (ProTools11)this._Testbed()._HostApp();

		//while (true) {
		//this._Testbed()._Robot()._imageGetMouseImage("TestAutoImages", "untitled");
		//}
		
		
		pProTools._createNewTrack( TrackFormat.Stereo, TrackType.AudioTrack );
		pProTools._removeTrack();
		pProTools._createNewTrack( TrackFormat.Stereo, TrackType.InstrumentTrack );
		pProTools._removeTrack();
		pProTools._createNewTrack( TrackFormat.DEFAULT, TrackType.MIDITrack );
		pProTools._removeTrack();
		
		pProTools._importAudioFile( this.m_audioFile );

		pProTools._instantiatePlugin(this.m_pluginName, this.m_pluginCategory);
				
		pProTools._hidePluginUI();
		
		pProTools._showPluginUI();
		
		//pProTools._hidePluginUI();
		
		if ( pProTools._isPlaying() == false )
			pProTools._togglePlay();
		
		TimeUtils.sleep(5);
		
		pProTools._togglePlay();
		
		pProTools._uninstantiatePlugin();
		
		pProTools._removeTrack();
		
		pProTools._saveSession();
		pProTools._Quit( 10, true );
	}
	
	/**
	 * Gets called if an exception is caught by the Test base class while processing _TestCase
	 */
	@Override
	protected void _OnTestCaseException( TestCaseParameters pTestcaseParameters, Exception e ) throws Exception {
		this._Testbed()._DismissCrashReporter( pTestcaseParameters._GetSubmitCrashReport() );
	}

	/**
	 * Gets called only once after all test cases have been run.
	 */
	@Override
	protected void _ShutDown( TestCaseParameters pCommonParameters ) throws Exception {				
		this._Testbed()._DismissCrashReporter( pCommonParameters._GetSubmitCrashReport() );
	}

	@Override
	protected void _SetupTestCase(TestCaseParameters pParams)
			throws Exception {
		
		//String strTestbed= pParams._GetTestbed();
		this._Testbed( pParams._GetTestbed() );
		this._Testbed( pParams._GetTestbed() )._HostApp( pParams._GetApp() );

		//this._Testbed()._Robot()._imageGetMouseImage("TestAutoImages/Hosts/ProTools11/Win7", "pt11");

		this._Testbed()._RemoteServer()._SetProcessRequestThrottle(250);
		this.m_pluginCategory = pParams._GetString("pluginCategory");
		this.m_pluginName = pParams._GetString("pluginName");
		this.m_audioFile = pParams._GetString("audioFile");
	}
	
	/**
	 * Makes sure the app is running and in proper state
	 * 
	 * @param pParams
	 * @throws Exception
	 */
	private void _LaunchPT( TestCaseParameters pParams ) throws Exception {
		if( pParams._GetBool( "forceNewAppInstance" ) || !this._Testbed()._HostApp()._amIRunning() ) {
			if( pParams._GetBool( "forceNewAppInstance" ) )
				this._Testbed()._HostApp()._ForceQuit();
			
			int nMaxSplashScreenWait= 60;
			// Grab a copy of the session template and launch PT with the local session copy
			RemoteFile sessionFile= this._Testbed()._RemoteServer()._createRemoteFile( pParams._GetString( "session" ) );
			RemoteFile tmpSessionDir= this._Testbed()._RemoteServer()._createRemoteFile( pParams._GetString( "tempSessionDir" ) );
			RemoteFile sessionFileCopy= this._Testbed()._RemoteServer()._createRemoteFile( tmpSessionDir._getAbsolutePath() + "/" + sessionFile._getName() );

			if( !sessionFile._exists() )
				throw new Exception( "Session File not found: " + pParams._GetString( "session" ) );

			tmpSessionDir._deleteContents( true );
			tmpSessionDir._mkdirs();
			sessionFile._copyTo( sessionFileCopy, 30 );
			
			this._Testbed()._HostApp()._Launch( 10, sessionFileCopy._getAbsolutePath(), false, false );
			TimeUtils.sleep( 10 );
			
			while( this._Testbed()._RemoteServer()._Robot()._imageFind( Images.Host_ProTools11_AvidSplashScreenLogo ) != null ) {
				System.out.println( "Waiting for splash screen to go away" );
				TimeUtils.sleep( 1 );
				if( nMaxSplashScreenWait-- < 0 )
					throw new Exception( "The app appears to be stuck on the splash screen!" );
			}
			TimeUtils.sleep( 3 );
		}
	}
	
}
