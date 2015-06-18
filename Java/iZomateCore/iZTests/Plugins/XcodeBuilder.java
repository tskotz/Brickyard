package iZomateCore.iZTests.Plugins;

import java.util.Arrays;

import iZomateCore.TestCore.Test;
import iZomateCore.TestCore.TestCaseParameters;

public class XcodeBuilder extends Test {

	public static void main(String[] args) throws Exception {
		new XcodeBuilder( args ).run();
	}

	protected XcodeBuilder(String[] args) throws Exception {
		super(args);
	}

	@Override
	protected void _StartUp(TestCaseParameters pCommonParameters) throws Exception {
	}

	@Override
	protected void _SetupTestCase(TestCaseParameters pTestcaseParameters) throws Exception {
		// TODO Auto-generated method stub	
	}

	@Override
	protected void _TestCase(TestCaseParameters pTestcaseParameters) throws Exception {		
		this._Testbed( pTestcaseParameters._GetTestbed() );		

		String strXcodeBuild= pTestcaseParameters._GetString("xcodebuild");
		String strProject= pTestcaseParameters._GetString("xcodeProject");
		String strScheme= pTestcaseParameters._GetString("xcodeScheme");
		
		this._Logs()._ResultLog()._logLine("<b>Building: "+strProject+"</b><br>");
					
		this._RunCommand( "Building: ", new String[]{strXcodeBuild, "-project", strProject, "-scheme", strScheme}, null );
	}
	
	/**
	 * 
	 * @param strDescription
	 * @param sCmds
	 * @param strWorkingDir
	 * @throws Exception
	 */
	private void _RunCommand( String strDescription, String[] sCmds, String strWorkingDir ) throws Exception {
		StringBuffer stdOut= new StringBuffer();
		StringBuffer stdErr= new StringBuffer();
		Boolean bWaitFor= true;
		int iTimeout= 120;

		this._Logs()._ResultLog()._logLine("<b>"+strDescription+"</b>");
		this._Logs()._ResultLog()._logData("Command Args: " + Arrays.toString(sCmds));			
		this._Testbed()._RemoteServer()._commandLine(sCmds, null, strWorkingDir, stdOut, stdErr, bWaitFor, iTimeout);
		this._Logs()._ResultLog()._logData(stdOut.toString());
		if( stdErr.length() > 0 )
			this._Logs()._ResultLog()._logError(stdErr.toString(), true);		
	}

	@Override
	protected void _OnTestCaseException(TestCaseParameters pTestcaseParameters, Exception e) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	protected void _ShutDown(TestCaseParameters pCommonParameters) throws Exception {
		// TODO Auto-generated method stub
	}

}
