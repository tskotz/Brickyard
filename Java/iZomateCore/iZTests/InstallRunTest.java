package iZomateCore.iZTests;

import iZomateCore.AppCore.AppEnums.TrackType;
import iZomateCore.TestCore.Test;
import iZomateCore.TestCore.TestCaseParameters;
import iZomateCore.UtilityCore.TimeUtils;

public class InstallRunTest extends Test {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception	{
		new InstallRunTest( args ).run();
	}
	
	/**
	 * Constructor
	 * @param args command line args
	 * @throws Exception
	 */
	protected InstallRunTest( String[] args ) throws Exception {
		super( args );
	}
	
	@Override
    protected void _SetupTestCase( TestCaseParameters pParams ) throws Exception {
        this._Testbed( pParams._GetTestbed() );
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
		String strProductFolder= pParams._GetString( "buildArchiveProductDir" );
		String strInstaller= pParams._GetString( "installer" );
		String strMacMountedInstallerPkg= pParams._GetString( "macMountedInstallerPkg" );
		String strPassword= pParams._GetString( "password" );
		
		//this._Testbed()._DownloadAndRunInstaller( strProductFolder, strInstaller, strMacMountedInstallerPkg, strPassword );
		this._Testbed()._HostApp( pParams._GetApp() )._Actions()._Launch( 5, null, pParams._GetForceNewInstanceOnStart(), pParams._GetHideAllWinAtStart() );
		//this._Testbed()._HostApp()._createNewTrack( null, TrackType.AudioTrack );
		TimeUtils.sleep( 5 );
		this._Testbed()._HostApp()._importAudioFile( "Clifton's Gold.wav" );
		TimeUtils.sleep( 1 );
		this._Testbed()._HostApp()._instantiatePlugin( pParams._GetPlugin(), 0 );
		TimeUtils.sleep( 1 );
		this._Testbed()._HostApp()._togglePlay();
		TimeUtils.sleep( 3 );
		this._Testbed()._HostApp()._togglePlay();
		TimeUtils.sleep( 1 );
		this._Testbed()._HostApp()._uninstantiatePlugin(0);
		
		//this._Testbed()._HostApp()._Plugin( pParams._GetPlugin() );
	}
	
	protected void _RunInstalledProduct( TestCaseParameters pParams ) throws Exception {
		this._Testbed( pParams._GetTestbed() )._HostApp( pParams._GetApp() )._Actions()._Launch( 5, null, pParams._GetForceNewInstanceOnStart(), pParams._GetHideAllWinAtStart() );
		
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
		
}
