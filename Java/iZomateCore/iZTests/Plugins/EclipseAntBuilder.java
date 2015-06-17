package iZomateCore.iZTests.Plugins;

import java.util.Arrays;

import iZomateCore.TestCore.Test;
import iZomateCore.TestCore.TestCaseParameters;

public class EclipseAntBuilder extends Test {

	public static void main(String[] args) throws Exception {
		new EclipseAntBuilder( args ).run();
	}

	protected EclipseAntBuilder(String[] args) throws Exception {
		super(args);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void _StartUp(TestCaseParameters pCommonParameters) throws Exception {
	}

	@Override
	protected void _SetupTestCase(TestCaseParameters pTestcaseParameters)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void _TestCase(TestCaseParameters pTestcaseParameters) throws Exception {		
		StringBuffer stdOut= new StringBuffer();
		StringBuffer stdErr= new StringBuffer();
		Boolean bWaitFor= true;

		this._Testbed( pTestcaseParameters._GetTestbed() );				
		String strAntFile= pTestcaseParameters._GetString( "AntBuildFile" );
		
		String[] sCmds= new String[]{"java", "-jar", "/Applications/eclipse/plugins/org.eclipse.equinox.launcher_1.3.0.v20140415-2008.jar", "-application", "org.eclipse.ant.core.antRunner", "-buildfile", strAntFile };
		this._Logs()._ResultLog()._logLine("<b>Running Ant File</b>");
		this._Logs()._ResultLog()._logData("Command Args: " + Arrays.toString(sCmds));			
		this._Testbed()._RemoteServer()._commandLine(sCmds, stdOut, stdErr, bWaitFor);
		this._Logs()._ResultLog()._logData(stdOut.toString());
		this._Logs()._ResultLog()._logData(stdErr.toString());
	}

	@Override
	protected void _OnTestCaseException(TestCaseParameters pTestcaseParameters,
			Exception e) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void _ShutDown(TestCaseParameters pCommonParameters)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}
