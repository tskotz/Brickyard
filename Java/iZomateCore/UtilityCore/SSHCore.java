package iZomateCore.UtilityCore;

import ch.ethz.ssh2.*;

import java.io.*;

/**
 * 
 * @author tskotz
 *
 */
public class SSHCore {
	//Example:  iztestauto@test-auto-macpro01.izotope.int
	//			ssh -l iztestauto buildarchive.izotope.int -i iztestauto_id_rsa 
	private	String		m_strUsername= null;
	private	String		m_strMachineName= null;
	private String		m_strPrivateKey= null;
	private Connection 	m_pConnection= null;
	private Session		m_pSession= null;
	private String		m_strResults= null;
	private SCPClient	m_pSCPClient= null;
	
	/**
	 * 
	 * @param strUsername
	 * @param strMachine
	 */
	public SSHCore( final String strUsername, final String strMachine, final String strPrivateKey ) {
		this.m_strUsername= strUsername;
		this.m_strMachineName= strMachine;
		this.m_strPrivateKey= strPrivateKey;
		this.m_pConnection= new Connection( strMachine );
	}

	/**
	 * 
	 * @param strCommand
	 * @throws Exception
	 */
	public SSHCore _ExecCommand( String strCommand ) throws Exception {
		System.out.println( "ssh " + this.m_strUsername + "@" + this.m_strMachineName + " " + strCommand );
		this._session( true ).execCommand( strCommand );
		
		// Grab the results
		InputStream stdout= new StreamGobbler( this._session( false ).getStdout() );
		BufferedReader br= new BufferedReader( new InputStreamReader( stdout ) );

		this.m_strResults= "";
		while( true ) {
	        String line = br.readLine();
	        if( line == null )
                break;
	        this.m_strResults += (this.m_strResults.isEmpty() ? "" : "\n") + line;
            System.out.println(line);
        }
		br.close();
		return this;
	}
	
	/**
	 * 
	 * @param fSourceFile - File or directory to scp
	 * @param strDestinationPath
	 * @return
	 * @throws Exception
	 */
	public SSHCore _SCP_Put( File fSourceFile, String strRemoteDirPath, String strCHMOD ) throws Exception {
		if( fSourceFile.isDirectory() ) {
			strRemoteDirPath+= "/" + fSourceFile.getName();
			this._ExecCommand( "mkdir -p \"" + strRemoteDirPath + "\";chmod " + strCHMOD + " \"" + strRemoteDirPath + "\"" );
			for ( File f : fSourceFile.listFiles() ) {
				this._SCP_Put( f, strRemoteDirPath, strCHMOD );
			}
		}
		else
			this._scpClient().put( fSourceFile.getAbsolutePath(), strRemoteDirPath, strCHMOD );
		
		return this;
	}

	/**
	 * 
	 * @param fSourceFile
	 * @param strDestinationPath
	 * @return
	 * @throws IOException
	 */
	public SSHCore _SCP_Get( String strRemoteFile, File fLocalTargetDir ) throws IOException {
		this._scpClient().get( "\"" + strRemoteFile + "\"", fLocalTargetDir.getAbsolutePath());
		return this;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public String _GetResult() throws IOException {
		return this.m_strResults;
	}
	
	/**
	 * 
	 */
	public void _Close() {
		if( this.m_pSession != null )
			this.m_pSession.close();
		if( this.m_pConnection != null )
			this.m_pConnection.close();
		if( this.m_pSCPClient != null )
			this.m_pSCPClient= null;
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private Session _session( boolean bNewSession ) throws IOException {
		this._authentcateConnection();
		
		if( this.m_pSession == null || bNewSession )
			this.m_pSession= this.m_pConnection.openSession();
		
		return this.m_pSession;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean _authentcateConnection() throws IOException {
		boolean bAuthed= this.m_pConnection.isAuthenticationComplete();
		if( !bAuthed ) {
			System.out.println( "Attempting SSH Authentication..." );
			this.m_pConnection.connect();
		
			File keyFile = new File( this.m_strPrivateKey );
			System.out.println( "Checking for key: " + keyFile.getAbsolutePath() );
			
			if( !keyFile.exists() ) {
				keyFile = new File( System.getProperty("user.home") + "/.ssh/" + this.m_strPrivateKey );
				System.out.println( "Checking for key: " + keyFile.getAbsolutePath() );
			}
			
			if( keyFile.exists() ) {
				System.out.print( "Attempting authentication with username: " + this.m_strUsername + ", key: " + keyFile.getAbsolutePath() + " ..." );
				bAuthed= this.m_pConnection.authenticateWithPublicKey( this.m_strUsername, keyFile, null );
			}
			
			if( !bAuthed ) {
				// Try using the private key as a password
				System.out.print( "Attempting to connect using username: " + this.m_strUsername + ", password: " + this.m_strPrivateKey );
				bAuthed= this.m_pConnection.authenticateWithKeyboardInteractive( this.m_strUsername, new InteractiveCallback() {          
		            public String[] replyToChallenge(String name, String instruction, int numPrompts, String[] prompt, boolean[] echo) throws Exception {
		                String[] responses = new String[numPrompts];
		                for (int x=0; x < numPrompts; x++)
		                    responses[x] = SSHCore.this.m_strPrivateKey;
		                return responses;
		            }
		        });
			}
		}
		
		System.out.println( "Authentication: " + bAuthed );
		return bAuthed;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private SCPClient _scpClient() throws IOException {
		this._authentcateConnection();
		if( this.m_pSCPClient == null )
			this.m_pSCPClient= this.m_pConnection.createSCPClient();
		return this.m_pSCPClient;
	}


}
