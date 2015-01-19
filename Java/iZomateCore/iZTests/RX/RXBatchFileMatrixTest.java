package iZomateCore.iZTests.RX;

import iZomateCore.ServerCore.RPCServer.RemoteServer.RemoteFile;
import iZomateCore.TestCore.TestCaseParameters;
public class RXBatchFileMatrixTest extends RXAppTest {

	
	protected RXBatchFileMatrixTest( String[] args ) throws Exception {
		super( args );
		
		;
	}
	
	//! Run the test
	public static void main(String[] args) throws Exception	{
		new RXBatchFileMatrixTest( args ).run();
	}
	
	@Override
	protected void _onStartUp(TestCaseParameters pCommonParameters)
			throws Exception {
		
		//Find the output directory and clear it.
		String strDir= pCommonParameters._GetString( "outputFileDir" );
		this._Logs()._ResultLog()._logMessage( "Locating output directory " + strDir + "." );
		RemoteFile dir= this._Testbed()._CreateRemoteFile( strDir );
		if (dir._exists()){
			this._Logs()._ResultLog()._logMessage("Clearing current files in " + strDir + " for new output");
			dir._deleteContents( false ); //clear the output directory
			this._Logs()._ResultLog()._logMessage("Clear complete!");
		}
		
		//Set up the input files
		this._Testbed()._HostApp()._Actions()._RXBatchAddFiles(pCommonParameters._GetString( "audioFileDir")); //Add Files to the Batch Processor based on Testcase xml file.
		this._Testbed()._HostApp()._Actions()._RXBatchClearAllProcessingSteps();// clearing the processing steps
		//this._Testbed()._HostApp()._Actions()._RXBatchAddProcessingStepWPreset( pTestcaseParameters._GetString( "moduleName"), pTestcaseParameters._GetString( "presetName")); we need a method for adding processing as well.
		// Comment to clean up eclipse warning
	}

	@Override
	protected void _TestCase(TestCaseParameters pTestcaseParameters)throws Exception {
		
		this._Testbed()._HostApp()._Actions()._RXBatchSetOutputFileFormat( pTestcaseParameters._GetString("audioFileType") );//Select the file type if specified
		this._Testbed()._HostApp()._Actions()._RXBatchSetOutputOptions( pTestcaseParameters._GetString("audioFileType"), pTestcaseParameters._GetString("outputOptions")); //Set the output options for the selected file type
		this._Testbed()._HostApp()._Actions()._RXBatchAddToFilename( pTestcaseParameters._GetString("addKind"), pTestcaseParameters._GetString("addText"), pTestcaseParameters._GetString("addLocation")); //Set the appended/prepended data
		this._Testbed()._HostApp()._Actions()._RXBatchRun( pTestcaseParameters._GetBool( "allowFailures"), pTestcaseParameters._GetInt( "batchTimeout" ) ); //Run the processor
		}
		
	//! Gets called if an exception is caught by the Test base class while processing _TestCase
	@Override
	protected void _OnTestCaseException( TestCaseParameters pTestcaseParameters, Exception e ) throws Exception {		
		// Dismiss the crash reporter, submitting a crash report if we wanted to
		this._Testbed()._DismissCrashReporter( pTestcaseParameters._GetSubmitCrashReport() );
		this._Logs()._ResultLog()._logLine( "\n" );
	}

	@Override
	protected void _onShutDown(TestCaseParameters pCommonParameters) throws Exception {

	}

}
