package iZomateCore.iZTests.Plugins;

import java.util.ArrayList;
import java.util.Arrays;

import iZomateCore.ServerCore.RPCServer.RemoteServer.RemoteServer;
import iZomateCore.TestCore.Test;
import iZomateCore.TestCore.TestCaseParameters;

public class GitRepoManager extends Test {

	public static void main(String[] args) throws Exception {
		new GitRepoManager( args ).run();
	}

	protected GitRepoManager(String[] args) throws Exception {
		super(args);
	}

	@Override
	protected void _StartUp(TestCaseParameters pCommonParameters) throws Exception {
		this._Testbed( pCommonParameters._GetTestbed() );		
	}

	@Override
	protected void _SetupTestCase(TestCaseParameters pTestcaseParameters) throws Exception {
		// TODO Auto-generated method stub	
	}

	@Override
	protected void _TestCase(TestCaseParameters pTestcaseParameters) throws Exception {		
		String strGitURL= pTestcaseParameters._GetString("GitURL");
		String strGitRepo= pTestcaseParameters._GetString(this._Testbed()._RemoteServer()._SysInfo()._isMac()?"GitRepoDirMac":"GitRepoDirWin");
		String strGitBranch= pTestcaseParameters._GetString("GitBranch");
		//String strGitTag= pTestcaseParameters._GetString("GitTag");
		Boolean bGitClean= pTestcaseParameters._GetBool("GitClean");
		Boolean bGitClone= pTestcaseParameters._GetBool("GitCloneIfNecessary");
		ArrayList<String> arrCmds= new ArrayList<String>();
		
		RemoteServer pRemoteServer= this._Testbed()._RemoteServer();

		this._Logs()._ResultLog()._logLine("<b>Local GIT Repo: "+strGitRepo+"</b><br>");

		if( bGitClone && !pRemoteServer._createRemoteFile(strGitRepo)._exists() ) {
			arrCmds.addAll(Arrays.asList("git", "clone", strGitURL, strGitRepo));
					
			if( strGitBranch != null && !strGitBranch.isEmpty() )
				arrCmds.addAll(Arrays.asList("--branch", strGitBranch));
			
			this._RunCommand( "Cloning Repo: " + strGitURL, arrCmds.toArray(new String[arrCmds.size()]), null );
		}
		
		if( strGitBranch != null && !strGitBranch.isEmpty() ) {
			this._RunCommand( "Checking out branch: " + strGitBranch, new String[]{"git","checkout","-f", strGitBranch}, strGitRepo );
			this._Logs()._ResultLog()._logLine("");
		}
		
		if( bGitClean )
			this._RunCommand( "Cleaning repo: " + strGitRepo, new String[]{"git","clean","-fdx"}, strGitRepo );
				
		this._RunCommand( "Fetching: ", new String[]{"git","fetch"}, strGitRepo );

		this._RunCommand( "Commits: " + strGitRepo, new String[]{"git","log", "HEAD..origin/"+strGitBranch, "--pretty=format:\"%h   %aD   %an   %s\""}, strGitRepo );

		this._RunCommand( "Pulling: ", new String[]{"git","pull"}, strGitRepo );
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
		this._Logs()._ResultLog()._logLine("Command Args: " + Arrays.toString(sCmds));			
		this._Testbed()._RemoteServer()._commandLine(sCmds, null, strWorkingDir, stdOut, stdErr, bWaitFor, iTimeout);
		this._Logs()._ResultLog()._logLine(stdOut.toString());
		this._Logs()._ResultLog()._logLine(stdErr.toString());
		//TODO: Figure out how to tell real errors from general status
		//if( stdErr.length() > 0 )
		//	this._Logs()._ResultLog()._logError(stdErr.toString(), true);
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
