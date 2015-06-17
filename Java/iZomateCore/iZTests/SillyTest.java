package iZomateCore.iZTests;

import iZomateCore.TestCore.Test;
import iZomateCore.TestCore.TestCaseParameters;

import javax.swing.*;

public class SillyTest extends Test {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main( String[] args ) throws Exception {
		SillyTest x= new SillyTest( args );
		x.run();
		
		//if( x._Logs()._ResultLog()._GetErrorCount() > 0)
		//	System.exit( x._Logs()._ResultLog()._GetErrorCount() );
	}

	public SillyTest( String[] args ) throws Exception {
		super( args );
	}

	@Override
	protected void _StartUp( TestCaseParameters pCommonParameters ) throws Exception {
		// Nothing to do
	}

    @Override
    protected void _SetupTestCase( TestCaseParameters pTestcaseParameters )  throws Exception {
        // TODO Auto-generated method stub
    }

	@Override
	protected void _TestCase( TestCaseParameters pParams ) throws Exception {
		Object[] options = {"Pass", "Fail"};
		int n = JOptionPane.showOptionDialog(null,
			"Should "+ pParams._GetTestCaseName() +" testcase pass or fail?",
			pParams._GetString("SillyTestMessage"),
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE,
			null,     //do not use a custom Icon
			options,  //the titles of buttons
			options[0]//default button title
		); 
		
		if( n == 0 )
			this._Logs()._ResultLog()._logData(pParams._GetTestCaseName() + " passed!");
		else
			this._Logs()._ResultLog()._logError(pParams._GetTestCaseName() + " failed!", true);
		
		//this._Testbed( pParams._GetTestbed() );
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
