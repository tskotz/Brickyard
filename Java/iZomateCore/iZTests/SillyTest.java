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
		new SillyTest( args ).run();
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
		JOptionPane.showMessageDialog( null, "Hello world:" + pParams._GetTestbed() );
		this._Testbed( pParams._GetTestbed() );
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
