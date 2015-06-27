package iZomateCore.TestCore;

import iZomateCore.AppCore.PluginInfo;
import iZomateCore.LogCore.Logs;
import iZomateCore.ServerCore.RPCServer.RPCServer;
import iZomateCore.UtilityCore.Emailer;
import iZomateCore.UtilityCore.SSHCore;
import iZomateCore.UtilityCore.TimeUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Extend this class to create a new Test. The Test will be run in three phases:
 * Setup, Execute, and Verify. The Execute phase will be run multiple times -
 * once for each TestCase( aka datarow in the dataparam). If Setup fails,
 * Execute will not be run. Cleanup is always run.
 */
public abstract class Test implements Runnable {
	private enum TestPhase {
		STARTUP, TESTCASE, SHUTDOWN
	};

	private Logs m_pLogs= null;
	protected boolean m_bVerboseLogs= true;
	private String m_strBuildNumber= null;
	private TestParameters m_TestParams;
	private TestCaseParameters m_ActiveTCParams= null;
	private Testbed m_pActiveTestbed= null;
	private SSHCore m_pActiveSSHCore= null;
	private Map<String, SSHCore> m_mSSHCores= new HashMap<String, SSHCore>();
	private Map<String, Testbed> m_mTestbeds= new HashMap<String, Testbed>();
    private String m_strEmailErrorMessages= "";

    /**
     * Constructor
     * 
     * @param args
     * @throws Exception
     */
	protected Test( String[] args ) throws Exception {		
		this.m_TestParams= new TestParameters( args );
	}

	/**
	 * Runnable's run override
	 */
	@Override
	public final void run() {
		int nExitCode= 0;
		try {
			// _Setup, StartUp, TestCase, ShutDown, _Cleanup
			this._setup();
			if( this._doPhase( TestPhase.STARTUP ) )
				this._doPhase( TestPhase.TESTCASE );

			this._doPhase( TestPhase.SHUTDOWN );
			this._cleanup();
			
			this._Logs()._ResultLog()._printSummary();
			nExitCode= this._Logs()._ResultLog()._GetErrorCount();

		} catch( Throwable t ) {
			nExitCode= -1;
			System.out.println( "Exception encountered while running test...\n" + t.getMessage() );
            this.m_strEmailErrorMessages += "<br>Exception in run() - " + t.getMessage();
			t.printStackTrace();
		}
		
		if( nExitCode != 0 )
			System.exit( nExitCode );
	}

	/**
	 * The Main Logs Object
	 * 
	 * @return Logs object
	 * @throws Exception
	 */
	public Logs _Logs() throws Exception {
		if( this.m_pLogs == null )
			this.m_pLogs= new Logs( this._GetCommonParams()._GetLogDir(),
					this._GetCommonParams()._GetTestScriptName(), 0,
					this._GetCommonParams()._GetEcho() );
		return this.m_pLogs;
	}

	/**
	 * Returns the Testbed object associated with strTestbed.  Creates the Testbed object if one does not already exist for strTestbed and adds it to the Test's list of Testbeds.  
	 * Sets the Testbed returned to the current active testbed whci can be accessed via _Testbed()
	 * 
	 * @param strTestbed
	 * @return Testbed object for the specified testbed
	 * @throws Exception
	 */
	public Testbed _Testbed( String strTestbed ) throws Exception {
		this.m_pActiveTestbed= this.m_mTestbeds.get( strTestbed );
		if( this.m_pActiveTestbed == null ) {
			this.m_pActiveTestbed= new Testbed( strTestbed, this );
			this.m_pLogs._ResultLog()._setRemoteServer( this.m_pActiveTestbed._RemoteServer() );
			this.m_mTestbeds.put( strTestbed, this.m_pActiveTestbed );
		}
		return this.m_pActiveTestbed;
	}

	/**
	 * Returns the Testbed object for the Testbed at the nTestbedNum item in the Testbeds list
	 * Sets the Testbed returned to the current active testbed whci can be accessed via _Testbed()
	 * 
	 * @param nTestbedNum
	 * @return Testbed object for the specified testbed
	 * @throws Exception
	 */
	public Testbed _Testbed( int nTestbedNum ) throws Exception {
		if( this.m_pActiveTestbed == null || nTestbedNum > this.m_mTestbeds.size() )
			return null;

		return this._Testbed( (String)this.m_mTestbeds.keySet().toArray()[nTestbedNum - 1] ); // Convert to 0 based
	}

	/**
	 * Returns the active Testbed object.  i.e. the last testbed explicitly called by name or number
	 * 
	 * @return the active Testbed object
	 * @throws Exception
	 */
	public Testbed _Testbed() throws Exception {
		if( this.m_pActiveTestbed == null )
			throw new Exception( "A Testbed has not been set.  You must call _Testbed( String strTestbed ) in _SetupTestCase() before calling _Testbed()" );
		return this.m_pActiveTestbed;
	}

	/**
	 * Return a String array of the current registered Testbeds
	 * 
	 * @return String[]
	 */
	public String[] _GetTestbeds() {
		return this.m_mTestbeds.keySet().toArray(new String[0]);
	}
	
	/**
	 * Initializes an SSH Client and adds it to the SSH list.  Sets it to the active SSH client as well.
	 * 
	 * @param strUsername
	 * @param strMachine
	 * @param strPrivateKey
	 * @return
	 * @throws Exception
	 */
	public SSHCore _SSH( String strUsername, String strMachine, String strPrivateKey ) throws Exception {
		this.m_pActiveSSHCore= this.m_mSSHCores.get( strUsername + "@" + strMachine );
		if( this.m_pActiveSSHCore == null ) {
			this.m_pActiveSSHCore= new SSHCore( strUsername, strMachine, strPrivateKey );
			this.m_mSSHCores.put( strUsername + "@" + strMachine, this.m_pActiveSSHCore );
		}
		return this.m_pActiveSSHCore;
	}

	/**
	 * Returns the active SSH client
	 * 
	 * @return
	 * @throws Exception
	 */
	public SSHCore _SSH() throws Exception {
		if( this.m_pActiveSSHCore == null )
			throw new Exception( "An SSHCore has not been set.  You must call _SSH( String strUsername, String strMachine ) before calling _SSH()" );
		return this.m_pActiveSSHCore;
	}

	/**
	 * Gets the plugin info for the first Plugin in the first HostApp on the first Testbed
	 * 
	 * @return
	 * @throws Exception
	 */
    public PluginInfo _PluginInfo() throws Exception{
        return this._Testbed()._HostApp()._Plugin()._GetPluginInfo();
    }

    /**
     * Pre Run Initialization Steps
     *  
     * @throws Exception
     */
    private void _setup() throws Exception {
		this.m_ActiveTCParams= this._GetCommonParams();
		this._Logs()._ResultLog()._logLine( "<H1>"
											+ this.m_ActiveTCParams._GetTestScriptName()
											+ "</H1>" 
											+ " <i>" + TimeUtils.getTimestamp() + "</i><br>"
											+ "Scripts Version: " + RPCServer.sRPCCoreVersion
											+ "\n\n" );
	}

	/**
	 * Post Run Cleanup Steps
	 * 
	 * @throws Exception
	 */
	private void _cleanup() throws Exception {
		this._postResults();
		this._SendEmail();
	}

	/**
	 * Performs the specified test phase, catching and reporting any exceptions.
	 * Test phases are "StartUp", "TestCase", and "ShutDown". The execute phase
	 * is called for every datarow( aka testcase) in the dataparam.
	 * 
	 * @return true if the phase passed, false if it failed
	 * @throws Exception
	 */
	private boolean _doPhase(TestPhase phase) throws Exception {
		boolean bPassed= true;

		// Run the TestPhase
		try {
			if( phase.equals(TestPhase.STARTUP) )  {
				this._StartUp( this._GetCommonParams() );
            } else if( phase.equals(TestPhase.TESTCASE) ) {
				this._RunTestCases();
            } else if( phase.equals(TestPhase.SHUTDOWN) ){
				this._Logs()._ResultLog()._logTestbedSystemMetrics( this._Testbed() );
				this._ShutDown( this._GetCommonParams() );
				this._Logs()._ResultLog()._logTestbedSystemMetrics( this._Testbed() );
            }
		} catch( Exception e ) {
			bPassed= false;
			this._Logs()._ResultLog()._LogException( e, true );
		}

		return bPassed;
	}

	/**
	 * 
	 * @throws Exception
	 */
	private boolean _RunTestCases() throws Exception {
		while( this.m_TestParams._HasNextTestCase() ) {
            System.out.println( "============ Running Test Case ==============" );

			try {
				this.m_ActiveTCParams= this.m_TestParams._GetNextTestCase();
				if( this.m_bVerboseLogs ) 
					this._Logs()._ResultLog()._logTestCaseStartupInfo( this.m_ActiveTCParams );

                this._SetupTestCase( this.m_ActiveTCParams );
				this._Logs()._ResultLog()._logTestbedSystemMetrics( this._Testbed() );
                
                // Run test case
				this._TestCase( this.m_ActiveTCParams );
			} catch( Exception e ) {
				this._Logs()._ResultLog()._LogException( e, true );
				this.m_strEmailErrorMessages += "<br>" + this.m_ActiveTCParams._GetTestCaseName() + " - " + e.getMessage();
				this._OnTestCaseException( this.m_ActiveTCParams, e );
			}
			this._Logs()._ResultLog()._logTestbedSystemMetrics( this._Testbed() );
			this._Logs()._ResultLog()._logTestCaseFinishInfo();
		}

		this._Logs()._ResultLog()._logMetric("testcase", "0"); // No more test cases
		return( this._Logs()._ResultLog()._GetErrorCount() == 0 );
	}

	/**
	 * Helper class
	 * 
	 * @return common params
	 */
	public TestCaseParameters _GetCommonParams() {
		return this.m_TestParams._GetCommonParams();
	}

	/**
	 * Helper class
	 * 
	 * @return testcase params for current test case
	 */
	public TestCaseParameters _GetCurrentTestCaseParams() {
		return this.m_ActiveTCParams;
	}

	/**
	 * Send Email
	 * @throws Exception
	 */
	public void _SendEmail() throws Exception {
		if( this._GetCommonParams()._GetEmailRecipients() != null && !this._GetCommonParams()._GetEmailRecipients().isEmpty() ) {
			int errCount= this._Logs()._ResultLog()._GetErrorCount();

            // Do not set emails if our tests pass and we have 'no
            if( errCount == 0 && !this._GetCommonParams()._GetSendPassedEmails() ){
                return;
            }

			String subject= this._GetCommonParams()._GetTestScriptName() + " " + this._Testbed()._SysInfo()._Platform() + " : " + ( errCount == 0 ? "PASSED" : "FAILED");

			String message= "<H3>This is a automated email informing you that the "
					+ this._GetCommonParams()._GetTestScriptName() + " "
					+ ( errCount == 0 ? "<FONT color=\"GREEN\">PASSED</FONT>" : "<FONT color=\"RED\">FAILED</FONT>")
					+ ( errCount > 0 ? " with " + errCount + " error(s)" : "")
					+ ".</H3>\n";					

			String strResultsLink= this._GetCommonParams()._GetResultsLink( this._Testbed()._SysInfo()._Platform() );
			if( strResultsLink != null) {
				if( this._getBuildNumber() != null ) {
					message += "<br><h4><a href='" + strResultsLink.replace("%BuildNumber%", this._getBuildNumber()) + "'>";
                    message += "Results Log</a></h4>";
                } 
				else
					message += "<br>No results link: Unable to retrieve build number from plugin";
			}

            if( !this.m_strEmailErrorMessages.isEmpty() )
                message += "<br><b><font color='RED'>Errors:" + this.m_strEmailErrorMessages + "</b></font>";

			for( String strTestbed : this._GetTestbeds())
				if( this._Testbed(strTestbed)._GetCrashReportMessage() != null)
					message += "<br><br>Crash Reports:<br>" + this._Testbed()._GetCrashReportMessage();

			new Emailer( this._GetCommonParams()._GetEmailSender() )
					._SetRecipients( this._GetCommonParams()._GetEmailRecipients() )
					._SendEmail(subject, message);
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	private void _postResults() throws Exception {
		if( this._GetCommonParams()._GetSSHRemoteArchiveDir() != null
			&& this._GetCommonParams()._GetSSHUserAtHost() != null && this._getBuildNumber() != null ) {
			try {
				String strPrivateKey= null;
				String[] strUserAndHost= this._GetCommonParams()._GetSSHUserAtHost().split("@");
				String strRemotePath= this._GetCommonParams()._GetSSHRemoteArchiveDir();

				// Check if a private key was specified
				if( strUserAndHost[1].contains( " -i " ) ) {
					String[] strHostAndPrvateKey= strUserAndHost[1].split( " -i " );
					strUserAndHost[1]= strHostAndPrvateKey[0];
					strPrivateKey= strHostAndPrvateKey[1];
				}

				strRemotePath= this._GetCommonParams()._GetSSHRemoteArchiveDir().replace( "%BuildNumber%", this._getBuildNumber() );

				this._SSH( strUserAndHost[0], strUserAndHost[1], strPrivateKey );
				this._SSH()._SCP_Put( new File(this._Logs()._GetLogsDir()), strRemotePath, "0774" );
			} catch( Exception e) {
				this._Logs()._ResultLog()._LogException( e, false );
			}
		}		
	}

	/**
	 * This assumes that all the plugins are from the same build.  It will return the build number of the first plugin it finds
	 * 
	 * @return
	 * @throws Exception
	 */
	protected String _getBuildNumber() throws Exception {
		for( int i= 1; this._Testbed( i ) != null && this.m_strBuildNumber == null; i++ )
			for( int j=1; this._Testbed( i )._HostApp( j ) != null && this.m_strBuildNumber == null; j++ )
				for( int k=1; this._Testbed( i )._HostApp( j )._Plugin( k ) != null && this.m_strBuildNumber == null; k++ ) {
					try {
						this.m_strBuildNumber= String.valueOf(this._Testbed( i )._HostApp( j )._Plugin( k )._GetPluginInfo().m_nBuildNumber);
					} catch( Exception e) {
						// Keep Calm and Carry On
					}
				}
		
		return this.m_strBuildNumber;
	}
	
	/**
	 * Sets the build number.  Overrides the Plugin buildnumber.  See _getBuildNumber
	 * 
	 * @return The build number as string
	 * @throws Exception
	 */
	protected void _setBuildNumber( String strBuildNumber ) throws Exception {
		this.m_strBuildNumber= strBuildNumber;
	}


	// -----------------------------------
	// Abstract Phase Methods
	// -----------------------------------

	/**
	 * Override this method to perform test-specific setup. Any exceptions
	 * thrown by this method will be caught and logged, and will prevent the
	 * <code>execute</code> method from being called. You can perform the setup
	 * for your test in your <code>execute</code> method instead, but putting it
	 * here can provides clearer error reporting.
	 * 
	 * @param pCommonParameters
	 *            commonParameters
	 * @throws Exception
	 */
	protected abstract void _StartUp(TestCaseParameters pCommonParameters)
			throws Exception;

    /**
     * Override this method to perform the setup the test case execution.
     * HostApp, PluginInfo should be set up here.
     *
     * Example:
     * this._Testbed( pCommonParameters._GetTestbed() )._HostApp( pCommonParameters._GetApp() )._Actions()._Launch( 5, null, pCommonParameters._GetForceNewInstanceOnStart(), pCommonParameters._GetHideAllWinAtStart() );
     *
     * @param pTestcaseParameters
     *            TestCaseParameters for this testcase
     * @throws Exception
     */
    protected abstract void _SetupTestCase(TestCaseParameters pTestcaseParameters)
            throws Exception;
	/**
	 * Override this method to perform the test execution. Any exceptions thrown
	 * by this method will be caught and logged. This method will be called
	 * multiple times (once for each datarow in the dataparam, aka each
	 * testcase).
	 * 
	 * @param pTestcaseParameters
	 *            TestCaseParameters for this testcase
	 * @throws Exception
	 */
	protected abstract void _TestCase(TestCaseParameters pTestcaseParameters)
			throws Exception;

	/**
	 * Override this method to to be informed of any exception caught by the
	 * internal test case processing. The test will continue with the next test
	 * case when exiting this method Any exceptions thrown by this method will
	 * be caught and logged.
	 * 
	 * @param pTestcaseParameters
	 *            TestCaseParameters for this testcase
	 * @param e
	 *            exception to throw
	 * @throws Exception
	 */
	protected abstract void _OnTestCaseException(
			TestCaseParameters pTestcaseParameters, Exception e)
			throws Exception;

	/**
	 * Override this method to perform test-specific cleanup. It is called
	 * regardless of whether <code>setup</code> or <code>execute</code> threw an
	 * exception. Any exceptions thrown by this method will be caught and
	 * logged.
	 * 
	 * @param pCommonParameters
	 *            commonParameters
	 * @throws Exception
	 */
	protected abstract void _ShutDown(TestCaseParameters pCommonParameters)
			throws Exception;

}
