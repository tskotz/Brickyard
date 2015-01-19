package iZomateCore.iZTests.RX;

import iZomateCore.ServerCore.RPCServer.RemoteServer.RemoteFile;
import iZomateCore.TestCore.TestCaseParameters;

public class RXFileLoadingTest extends RXAppTest {

	protected RXFileLoadingTest( String[] args ) throws Exception {
		super( args );
	}
	
	//! Run the test
	public static void main(String[] args) throws Exception	{
		new RXFileLoadingTest( args ).run();
	}
	
	@Override
	protected void _onStartUp(TestCaseParameters pCommonParameters)
			throws Exception {
		// Comment to clean up eclipse warning
	}

	@Override
	protected void _TestCase(TestCaseParameters pTestcaseParameters)
			throws Exception {
		
		// Close any file that's open
		this._Testbed()._HostApp()._Actions()._UnloadAudioFile();
		
		// Get the list of files in this directory
		String strDir= pTestcaseParameters._GetString( "audioFileDir" );
		this._Logs()._ResultLog()._logMessage( "Loading files in directory " + strDir + "." );
		RemoteFile dir= this._Testbed()._CreateRemoteFile( strDir );
		if( !dir._exists() ) {
			this._Logs()._ResultLog()._logError("Directory " + strDir + " does not exist.", false);
			return;
		}
		String[] files= dir._list( false );
		
		boolean bSaveAs= pTestcaseParameters._GetBool("saveAs", false);
		int nFileTypes= 8; // Currently RX can save 8 file types, change this if we add more
		String[] fileTypes= { "WAVE", "AIFF", "FLAC", "MP3", "MP4", "M4A", "Ogg Vorbis", "RX Document" };
		int nFileType= 0;

		// Iterate through files, loading, saving, and closing them.
		int nCount= 0;
		for( int i = 0; i < files.length; i++ ) {
			String strName= files[i];
			// Ignore files that were saved by this test
			if( !strName.contains("_RXFileLoadingTest") && !strName.startsWith(".") ) {
				// Load the file
				this._Logs()._ResultLog()._logMessage( "Loading audio file " + strName + "..." );
				this._Testbed()._HostApp()._Actions()._LoadAudioFile( strDir + "/" + strName, true, false );
				this._Logs()._ResultLog()._logMessage( "Loaded audio file " + strName + "." );
				
				// Save the file if the flag is set
				String strSaveAs= null;
				if( bSaveAs ) {
					strName= strName.replace( '.', '_' );
					strSaveAs= strName + "_RXFileLoadingTest";
					this._Logs()._ResultLog()._logMessage( "Saving audio file " + strName + " as " + strSaveAs + " (" + fileTypes[nFileType] + ")..." );
					_SaveAsType( strSaveAs, nFileType );
					this._Logs()._ResultLog()._logMessage( "Saved " + strSaveAs + "." );
					// Cycle through available file types
					nFileType++;
					if( nFileType == nFileTypes ) {
						nFileType= 0;
					}
				}
		  	
				// Close each file
				this._Testbed()._HostApp()._Actions()._UnloadAudioFile();
				
				nCount++;
			} else {
				this._Logs()._ResultLog()._logMessage( "Skipping file " + strName + "." );
			}
		}
		this._Logs()._ResultLog()._logMessage( "Found a total of " + nCount + " audio files." );
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
