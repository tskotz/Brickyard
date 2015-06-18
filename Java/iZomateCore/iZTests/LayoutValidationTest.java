package iZomateCore.iZTests;

import iZomateCore.TestCore.Test;
import iZomateCore.TestCore.TestCaseParameters;
import iZomateCore.UtilityCore.FileUtils;

import java.io.File;

public class LayoutValidationTest extends Test {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception	{
		new LayoutValidationTest( args ).run();
	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public LayoutValidationTest( String[] args ) throws Exception {
		super( args );
	}

	@Override
	protected void _StartUp( TestCaseParameters pCommonParameters ) throws Exception {
		this._Testbed( pCommonParameters._GetTestbed() )._HostApp( pCommonParameters._GetApp() )._Actions().
				_Launch( 5, null, pCommonParameters._GetForceNewInstanceOnStart(), pCommonParameters._GetHideAllWinAtStart() );
		this._Testbed()._HostApp()._Plugin( pCommonParameters._GetPlugin() );
	}

    @Override
    protected void _SetupTestCase( TestCaseParameters pTestcaseParameters )
            throws Exception {
        // TODO Auto-generated method stub

    }

	@Override
	protected void _TestCase( TestCaseParameters pTestcaseParameters ) throws Exception {
		String strLayoutFile= 		pTestcaseParameters._GetString( "layoutFile" );
		String strObjName= 	 	   	pTestcaseParameters._GetString( "objectName" );
		String strObjectGoldImage= 	pTestcaseParameters._GetString( "objectGoldImage" );
		
		this._Logs()._ResultLog()._logMessage( "Loading Layout File: " + strLayoutFile );
		//this._Testbed()._HostApp()._Plugin()._Console()._ExecuteCommand( "Load LayoutFile" );
		this._Logs()._ResultLog()._logMessage( "Grabbing screen image for object name: " + strObjName );
		String strImage= this._Testbed()._HostApp()._Plugin()._Controls().
				_Button( strObjName ).
				_GrabScreenImage( this._Logs()._GetLogsDir() );
		
		this._Logs()._ResultLog()._logMessage( "Comparing object image file: \"" + strImage + "\" with Gold file: \"" + strObjectGoldImage + "\"" );
		String strDiffImageFile= this._Logs()._GetLogsDir() + "/" + FileUtils.fileName(strImage).replace( ".", "_dif.");
		if( new File( strObjectGoldImage ).exists() )
			FileUtils._CompareImages( strObjectGoldImage, strImage, strDiffImageFile, this._Logs() );
		else {
			this._Logs()._ResultLog()._logWarning( "Gold file not found.  Setting screen image grab to gold file: \"" + strObjectGoldImage + "\"\n" );
			this._Logs()._ResultLog()._logImage( FileUtils.fileName( strImage ) );
			FileUtils.copyFile( strImage,  strObjectGoldImage );
		}
	}
	
	@Override
	protected void _OnTestCaseException( TestCaseParameters pTestcaseParameters, Exception e ) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	protected void _ShutDown( TestCaseParameters pCommonParameters ) throws Exception {
		// TODO Auto-generated method stub
	}

}
