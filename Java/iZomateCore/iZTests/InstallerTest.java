package iZomateCore.iZTests;

import java.io.BufferedReader;
import java.io.FileReader;

import iZomateCore.TestCore.Test;
import iZomateCore.TestCore.TestCaseParameters;

/**
 * Plugin Installer Test
 * Downloads and installs a plug-in.  After installation it checks the files installed against a gold blessed install log file.
 * @author tskotz/nlapenn
 *
 */
public class InstallerTest extends Test {

	private String strAppName;
	private String strFullAppName;
	private String strVst32Folder;
	private String strVst64Folder;
	private int nTotalFiles;
	private int nMissingFiles;
	private int nPrefsChecked;
	private int nPrefsWrong;
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main( String[] args ) throws Exception {
		new InstallerTest( args ).run();
	}

	/**
	 * Constructor
	 * 
	 * @param args command line args
	 * @throws Exception
	 */
	protected InstallerTest( String[] args ) throws Exception {
		super( args );
	}

	/**
	 * Gets called only once before the test cases are run
	 */
	@Override
	protected void _StartUp( TestCaseParameters pCommonParameters ) throws Exception {
		System.out.println( "StartUp!" );
	}
	
	@Override
	protected void _SetupTestCase( TestCaseParameters pParams ) throws Exception {
		if( this._Testbed( pParams._GetTestbed() )._SysInfo()._isWin() )
			this._Testbed()._SetWinThemeToClassic();
		//this._Testbed( pParams._GetTestbed() );
	}
	
	/**
	 * Gets called for each test case defined in test input file
	 */
	@Override
	protected void _TestCase( TestCaseParameters pParams ) throws Exception {		
		// Pulls in the parameters from the XML file.
		String strProductFolder= pParams._GetString( "installerFolder" );
		String strInstaller= pParams._GetString( this._Testbed()._SysInfo()._isMac() ? "installerMac" : "installerWin" );
		String strGoldLogFile= pParams._GetString( this._Testbed()._SysInfo()._isMac() ? "installerLogMac" : "installerLogWin" );
		String strMacMountedInstallerPkg= pParams._GetString( "macMountedInstaller" );
		String strPassword= pParams._GetString("password");
		String strSerialNumber= pParams._GetString("serialNumber", null);
		String strName= pParams._GetString("authName", null);
		String strEmail= pParams._GetString("authEmail", null);
		boolean bVerifyInventory= pParams._GetBool("verifyInventory");
		boolean bcheckExtraFiles= pParams._GetBool("checkExtraFiles");
		boolean bRunUninstaller= pParams._GetBool("runUninstaller");
		boolean bGoldLog= pParams._GetBool("goldLog");
		boolean bContentInstaller= pParams._GetBool("isContentInstaller");
		boolean bRequiresAuth= pParams._GetBool("requiresAuth", false);
		
		this.strAppName= pParams._GetString( "appName" );
		this.strFullAppName= "iZotope " + this.strAppName;
		this.strVst32Folder= pParams._GetString( "vst32Folder" );
		this.strVst64Folder= pParams._GetString( "vst64Folder" );
		this.nTotalFiles= 0;
		this.nMissingFiles= 0;
		this.nPrefsChecked= 0;
		this.nPrefsWrong= 0;
		
		String strUninstaller= pParams._GetString("uninstallerWin", this.strAppName);
		if( this._Testbed()._SysInfo()._isMac())
			strUninstaller= pParams._GetString("uninstallerMac", this.strAppName);
		// Check if it is a windows UNC path and change it to Mac network path if we are running on Mac
		if( System.getProperty("os.name").equals( "Mac OS X" ) && strGoldLogFile.startsWith( "\\\\" ))
			strGoldLogFile= "/Volumes" + strGoldLogFile.substring( strGoldLogFile.indexOf( "/" ) );
		
		// Runs the correct automated install based on installer type
		if ( bRequiresAuth )
			this._Testbed()._DownloadAndRunInstallerAuth( strProductFolder, strInstaller, strMacMountedInstallerPkg, strPassword, strSerialNumber, strName, strEmail );
		else 
			this._Testbed()._DownloadAndRunInstaller( strProductFolder, strInstaller, strMacMountedInstallerPkg, strPassword );
		
		//Takes an inventory of the file system after the install
		this._Logs()._ResultLog()._logMessage("\nCreating log of installed files\n");
		this._inventoryInstall( bGoldLog );
		
		
		// The Tests themselves
		if( this._Testbed()._SysInfo()._isWin() ) {			
			if( bVerifyInventory ) this._verifyInstallWin( strGoldLogFile, bContentInstaller );
			if( bcheckExtraFiles && !bContentInstaller ) this._checkForExtraFiles();
			this._Logs()._ResultLog()._logMessage("\nStarting Phase: Uninstall\n");
			this._Testbed()._uninstallProduct( strUninstaller );
		}
		else {
			if( bVerifyInventory ) this._verifyInstallMac( strGoldLogFile );
			if( bcheckExtraFiles && !bContentInstaller ) this._checkForExtraFiles();
			this._Logs()._ResultLog()._logMessage("\nStarting Phase: Uninstall\n");
			if( bRunUninstaller ) this._Testbed()._uninstallProduct( strUninstaller, strPassword );
		}
					
//		this._postTestCaseResult( "\nFilesChecked", this.nTotalFiles );
//		this._postTestCaseResult( "FilesMissing", this.nMissingFiles );
	}
		
	/**
	 * Gets called if an exception is caught by the Test base class while processing _TestCase
	 */
	@Override
	protected void _OnTestCaseException( TestCaseParameters pTestcaseParameters, Exception e ) throws Exception {
		this._Testbed()._DismissCrashReporter( pTestcaseParameters._GetSubmitCrashReport() );
	}

	/**
	 * Gets called only once after all test cases have been run.
	 */
	@Override
	protected void _ShutDown( TestCaseParameters pCommonParameters ) throws Exception {				
		if( this._Testbed()._SysInfo()._isWin() )
			this._Testbed()._SetWinThemeToOriginal();
		
		this._Testbed()._DismissCrashReporter( pCommonParameters._GetSubmitCrashReport() );
	}

	
	//***********************************\\
	// Private Methods
	//***********************************//

	/**
	 * Verifies that files were installed against a gold log
	 * 
	 * @param strLogFile Gold qualified logfile
	 * @throws Exception
	 */
	private void _verifyInstallWin( String strLogFile, boolean bContentInstaller ) throws Exception {
		
		this._Logs()._ResultLog()._logMessage("\nStarting Phase: Verify against inventory\n");
		String strSysDrive= this._Testbed()._RemoteServer()._runCommand( "echo", "%SYSTEMDRIVE%" );
		String strUserName= this._Testbed()._RemoteServer()._runCommand( "echo", "%USERNAME%" );
		
		this._checkInventoryFile( strLogFile, strSysDrive, strUserName, bContentInstaller );
		
		if ( !bContentInstaller ) this._checkUpdateVersion( strSysDrive );
		
		this._Logs()._ResultLog()._logLine( "\nFiles Checked: " + this.nTotalFiles );
		this._Logs()._ResultLog()._logLine( "Files not found: " + this.nMissingFiles );
		this._Logs()._ResultLog()._logLine( "Preferences Checked: " + this.nPrefsChecked );
		this._Logs()._ResultLog()._logLine( "Preferences that were wrong: " + this.nPrefsWrong );
	}
	
	/**
	 * Checks each line of the gold qualified installer manifest for OSX against what is on disk
	 * 
	 * @param strLogFile Gold qualified logfile
	 * @throws Exception
	 */
	private void _verifyInstallMac( String strLogFile ) throws Exception {
		
		this._Logs()._ResultLog()._logMessage("\nStarting Phase: Verify against inventory\n");
		
		String strFileName= strLogFile;

			FileReader logFile= new FileReader( strFileName );
			BufferedReader logFileReader= new BufferedReader( logFile );

			String strCurrentLine= logFileReader.readLine();

			while( strCurrentLine!= null ) {				
				String strFileLocation= strCurrentLine;
				
				if( strCurrentLine.contains("File:") ) {

					this.nTotalFiles++;
					
					if( strCurrentLine.contains("~/") )
						strFileLocation= strCurrentLine.substring( strCurrentLine.indexOf("~/")
																   ,strCurrentLine.indexOf(";") );
					else
						strFileLocation= strCurrentLine.substring( strCurrentLine.indexOf("/")
																   ,strCurrentLine.indexOf(";") );
					
					if( strFileLocation.contains("Icon")) strFileLocation += "\r";
					
					if( !this._Testbed()._CreateRemoteFile( strFileLocation)._exists()) { 
						this._Logs()._ResultLog()._logWarning( strFileLocation.replace("\r", "") + " is missing!" );
						this.nMissingFiles++;
					}
				}
				
				strCurrentLine= logFileReader.readLine();
			}
			
			this._Logs()._ResultLog()._logLine( "\nFiles Checked: " + this.nTotalFiles );
			this._Logs()._ResultLog()._logLine( "Files not found: " + this.nMissingFiles );
			
			logFileReader.close();
	}
	
	/**
	 * Checks that the installed VST version matches the updater version in the registry.
	 * 
	 * @param strSysDrive Root drive for Windows machines.  Derived from the test params.
	 * @throws Exception
	 */
	private void _checkUpdateVersion( String strSysDrive ) throws Exception {
		
		try {
			String installedVersion= this._Testbed()._RemoteServer()._runCommand( "wmic datafile where Name=\""
																				 + this.strVst32Folder.replace("/", "\\\\") + "\\\\"
																				 + this.strFullAppName + ".dll\" get Version" );
			installedVersion= installedVersion.substring( installedVersion.indexOf("\n")+1 );
			installedVersion= installedVersion.substring(0,3) + installedVersion.substring(4,5);
			String registryVersion= this._Testbed()._RemoteServer()._runCommand( "REG QUERY \"HKCU\\Software\\iZotope\\Updater\\InstalledProducts\\"
																				+ this.strFullAppName + "\" /v CurrentVersion" );
			registryVersion= registryVersion.substring( registryVersion.indexOf("REG_SZ")+10 );
			
			if( !registryVersion.equals(installedVersion) )
				this._Logs()._ResultLog()._logWarning( "Installed Product does not match version in Registry: " + installedVersion + " vs " + registryVersion );
			else
				this._Logs()._ResultLog()._logLine( "Product version matches registry." );
		}
		catch(Exception e) {
			this._Logs()._ResultLog()._logWarning("Update string check failed: It's possible VST isn't installing.");
		}
	}
	
	/**
	 * Parses and checks an individual log entry Windows installs
	 * 
	 * @param strInventoryFile
	 * @param strSysDrive Root drive
	 * @param strUserName User name for documents directory
	 * @throws Exception
	 */
	private void _checkInventoryFile( String strInventoryFile, String strSysDrive, String strUserName, boolean bContentInstaller ) throws Exception {			
		boolean b64= this._Testbed()._RemoteServer()._SysInfo()._GetOSArch().equals( "64-bit" );

		FileReader logFile= null;
		BufferedReader logFileReader= null;
		
		try {
			logFile= new FileReader( strInventoryFile );
			logFileReader= new BufferedReader( logFile );
			String strCurrentLine= logFileReader.readLine();
			String strFileLocation= null;
			
			while( strCurrentLine!= null ) {		
				if( !strCurrentLine.contains( b64 ? "Win32Only" : "x64Only" ) 
					&& strCurrentLine.contains("File:") 
					&& !strCurrentLine.contains("deleteafterinstall") ) {
					
					this.nTotalFiles++;
						
					if( b64 )
						strFileLocation= this._formatFileLocation64( this._getRegValueString(strCurrentLine, "File:", 0), strSysDrive, strUserName, bContentInstaller );
					else
						strFileLocation= this._formatFileLocation32( this._getRegValueString(strCurrentLine, "File:", 0), strSysDrive, strUserName, bContentInstaller );
							
					if( !this._Testbed()._CreateRemoteFile(strFileLocation)._exists() ) {
						this._Logs()._ResultLog()._logWarning( strFileLocation.replace("/", "\\") + " is missing!" );
						this.nMissingFiles++;
					}
								
				}
				else if( strCurrentLine.contains("Registry Root:") 
						 && !strCurrentLine.contains("not checkforupdates") 
					     && strCurrentLine.contains("Value") ) {	
					this._checkRegistry( strCurrentLine, strFileLocation, strSysDrive, strUserName, bContentInstaller );
				}
	
				strCurrentLine= logFileReader.readLine();
			}
		}
		finally {
			if( logFileReader != null )
				logFileReader.close();
			if( logFile != null )
				logFile.close();
		}
	}
		
	/**
	 * Returns the value field of an installer log entry
	 * 
	 * @param strCurrentLine
	 * @param strValueName
	 * @param intOffset
	 * @return registry value
	 * @throws Exception
	 */
	private String _getRegValueString( String strCurrentLine, String strValueName, int intOffset ) throws Exception {
		String regValue= strCurrentLine.substring( strCurrentLine.indexOf(strValueName) + intOffset, strCurrentLine.indexOf(";", strCurrentLine.indexOf(strValueName)) );
		return regValue;
	}
		
	/**
	 * Checks to see if a value exists in the Windows registry
	 * 
	 * @param strCurrentLine Registry entry in the log file
	 * @param strFileLocation Unformatted log entry
	 * @param strSysDrive Root drive
	 * @param strUserName User name for documents directory
	 * @throws Exception
	 */
	private void _checkRegistry( String strCurrentLine, String strFileLocation, String strSysDrive, String strUserName, boolean bContentInstaller ) throws Exception {
		boolean b64= this._Testbed()._CreateRemoteFile( strSysDrive + "/Program Files (x86)" )._exists();
		this.nPrefsChecked++;
		String regRoot= this._getRegValueString( strCurrentLine, "Root:", 6 );
		String regSubKey= this._getRegValueString( strCurrentLine, "Subkey:", 8 ).replace("\"", "");
		String regValueName= null;
		String regEntry= null;
		String regValueData= null;
		String invValueData= null;
		
		if(strCurrentLine.contains("ValueName:")) {
			regValueName= this._getRegValueString( strCurrentLine, "ValueName:", 11);
			regEntry= this._Testbed()._RemoteServer()._runCommand( "REG QUERY "+"\""
																   +regRoot+"\\"
																   +regSubKey+"\""+" /v "
																   +regValueName);
		}			
		else {
			regEntry= this._Testbed()._RemoteServer()._runCommand( "REG QUERY "+"\""
																   +regRoot+"\\"
																   +regSubKey+"\"");
		}
		
		if( strCurrentLine.contains("dword") ) {
			regValueData= regEntry.substring( regEntry.indexOf("DWORD")+9 );
			invValueData= this._getRegValueString( strCurrentLine, "ValueData", 11 );
			invValueData= "0x" + invValueData;
		}
		if( strCurrentLine.contains("expandsz") ) {
			regValueData= regEntry.substring( regEntry.indexOf("REG_EXPAND_SZ")+17 );
			invValueData= this._getRegValueString( strCurrentLine, "ValueData", 11 );
			invValueData= invValueData.replace("{code:GetNewSystemPath}", "C:\\Program Files\\Common Files\\iZotope\\Runtimes");
		}
		else {
			regValueData= regEntry.substring( regEntry.indexOf("REG_SZ")+10 );
			invValueData= this._getRegValueString( strCurrentLine, "ValueData", 11 );
			if (b64)
				invValueData= this._formatFileLocation64( invValueData, strSysDrive, strUserName, bContentInstaller );
			else
				invValueData= this._formatFileLocation32( invValueData, strSysDrive, strUserName, bContentInstaller );
			invValueData= invValueData.replace( "\"", "" ).replace( "/", "\\" );
		}
		
		if( invValueData.equals(regValueData) )
			this._Logs()._ResultLog()._logLine( strCurrentLine.replace("/", "\\") + " matches." );
		else if (regValueData.contains(invValueData))
			this._Logs()._ResultLog()._logLine( strCurrentLine.replace("/", "\\") + " matches." );
		else {
			this._Logs()._ResultLog()._logWarning( strCurrentLine.replace("/", "\\") + " doesn't match!" );
			this.nPrefsWrong++;
		}
	}
	
	/**
	 * Formats log entries for 64-bit Windows
	 * 
	 * @param strFileLocation Unformatted log entry.
	 * @param strSysDrive Root drive
	 * @param strUserName User name for documents directory
	 * @return Formatted log entry
	 * @throws Exception
	 */
	private String _formatFileLocation64( String strFileLocation, String strSysDrive, String strUserName, boolean bContentInstaller ) throws Exception {		
		if (bContentInstaller == true) {
			strFileLocation= strFileLocation.replace("{app}", strSysDrive + "/Users/" + strUserName + "/Documents/iZotope/" + this.strAppName);
		}
		strFileLocation= strFileLocation.replace("File: ", "")
				.replace("{code:GetUserVSTPluginsFolder32|{pf32}/Steinberg/VstPlugins}", this.strVst32Folder)
				.replace("{code:GetUserVSTPluginsFolder64|{pf64}/Steinberg/VstPlugins}", this.strVst64Folder)
				.replace("{pf32}", strSysDrive + "/Program Files (x86)")
				.replace("{pf64}", strSysDrive + "/Program Files")
				.replace("{cf32}", strSysDrive + "/Program Files (x86)/Common Files")
				.replace("{cf64}", strSysDrive + "/Program Files/Common Files")
				.replace("{userdocs}", strSysDrive + "/Users/" + strUserName + "/Documents")
				.replace("{syswow64}", strSysDrive + "/Windows/sysWOW64")
				.replace("{userappdata}", strSysDrive + "/Users/" + strUserName + "/AppData/Roaming")
				.replace("{app}", strSysDrive + "/Program Files (x86)/iZotope/"+ this.strAppName)
				.replace("{code:GetUserCustomLibraryFolder}", strSysDrive + "/Users/" + strUserName + "/Documents/iZotope/" + this.strAppName);
		
		return strFileLocation;
	}
	
	/**
	 * Formats log entries for 32-bit Windows
	 * 
	 * @param strFileLocation Unformatted log entry
	 * @param strSysDrive Root drive
	 * @param strUserName User name for documents directory
	 * @return Formatted log entry
	 * @throws Exception
	 */
	private String _formatFileLocation32( String strFileLocation, String strSysDrive, String strUserName, boolean bContentInstaller) throws Exception {		
		if (bContentInstaller == true) {
			strFileLocation= strFileLocation.replace("{app}", strSysDrive + "/Users/" + strUserName + "/Documents/iZotope/" + this.strAppName);
		}
		strFileLocation= strFileLocation.replace("File: ", "")
				 .replace("{code:GetUserVSTPluginsFolder32|{pf32}/Steinberg/VstPlugins}", this.strVst32Folder)
				 .replace("{pf32}", strSysDrive + "/Program Files")
				 .replace("{cf32}", strSysDrive + "/Program Files/Common Files")
				 .replace("{userdocs}", strSysDrive + "/Users/" + strUserName + "/Documents")
				 .replace("{userappdata}", strSysDrive + "/Users/" + strUserName + "/AppData/Roaming")
				 .replace("{app}", strSysDrive + "/Program Files/iZotope/"+ this.strAppName)
				 .replace("{code:GetUserCustomLibraryFolder}", strSysDrive + "/Users/" + strUserName + "/Documents/iZotope/" + this.strAppName);
		
		return strFileLocation;
	}
	
	/**
	 * Takes an inventory of installed files in locations installs can possibly touch.  
	 * Piped to a file to be used for comparison against different installer versions and builds.
	 * 
	 * @param bGoldInstaller
	 * @throws Exception
	 */
	private void _inventoryInstall( boolean bGoldInstaller ) throws Exception {
		if (this._Testbed()._SysInfo()._isMac()) {
			String remoteLog= "./DirectoryLog/" + this.strAppName.replace(" ", "_") + (bGoldInstaller?"_Gold":"") + "_Log.txt";
			this._Testbed()._CreateRemoteFile( remoteLog )._getParent()._mkdirs();
			this._Testbed()._RemoteServer()._runCommand("ls -R ~/Documents/iZotope > " + remoteLog );
			this._Testbed()._RemoteServer()._runCommand("ls -R /Library/Audio/Plug-Ins >> " + remoteLog );
			this._Testbed()._RemoteServer()._runCommand("ls -R /Library/Application\\ Support/Digidesign/Plug-Ins >> " + remoteLog );
			this._Testbed()._RemoteServer()._runCommand("ls -R /Library/Application\\ Support/iZotope >> " + remoteLog );
			this._Testbed()._RemoteServer()._runCommand("ls -R /Library/Application\\ Support/Avid/Audio/Plug-Ins >> " + remoteLog );
			this._Testbed()._RemoteServer()._runCommand("ls -R ~/Library/Preferences >> " + remoteLog );
			this._Testbed()._RemoteServer()._runCommand("ls /Library/Preferences >> " + remoteLog );
		}
		else {
			String currentDirectory= this._Testbed()._RemoteServer()._runCommand("cd");
			String remoteLog= currentDirectory.replace("\\", "/") + "/DirectoryLog/" 
							  + this.strAppName.replace(" ", "_") 
							  + (bGoldInstaller?"_Gold":"") 
							  + "_Log.txt";
			this._Testbed()._CreateRemoteFile( remoteLog )._getParent()._mkdirs();
			String strSysDrive= this._Testbed()._RemoteServer()._runCommand( "echo", "%SYSTEMDRIVE%" );
			
			if( this._Testbed()._SysInfo()._GetOSArch().equals( "64-bit" ) ) {
				System.out.println(this._Testbed()._RemoteServer()._runCommand("dir", "/s", "/b", "\"" + strSysDrive + "/Program Files (x86)/iZotope\"", ">", remoteLog));
				System.out.println(this._Testbed()._RemoteServer()._runCommand("dir", "/s", "/b", "\"" + strSysDrive + "/Program Files (x86)/Common Files\"", ">>", remoteLog));
				System.out.println(this._Testbed()._RemoteServer()._runCommand("dir /s /b \"%WINDIR%/sysWow64\" >> " + remoteLog));
			}
			else {
				System.out.println(this._Testbed()._RemoteServer()._runCommand("dir", "/s", "/b", "\"" + strSysDrive + "/Program Files/iZotope\"", ">", remoteLog));
				System.out.println(this._Testbed()._RemoteServer()._runCommand("dir", "/s", "/b", "\"" + strSysDrive + "/Program Files/Common Files\"", ">>", remoteLog));
			}
			System.out.println(this._Testbed()._RemoteServer()._runCommand("dir", "/s", "/b", "\"%HOMEDRIVE%/Users/%USERNAME%/Documents\"", ">>", remoteLog));
			System.out.println(this._Testbed()._RemoteServer()._runCommand("dir", "/s", "/b", "\"%APPDATA%\"", ">>", remoteLog));
			System.out.println(this._Testbed()._RemoteServer()._runCommand("dir", "/s", "/b", "\"%WINDIR%/system32\"", ">>", remoteLog));
		}
	}
	
	/**
	 * Diffs the file logs of the old vs the new builds and reports and differences
	 * 
	 * @param strSysDrive
	 * @param strUserName
	 * @throws Exception
	 */
	private void _checkForExtraFiles() throws Exception {
		
		this._Logs()._ResultLog()._logMessage("\nStarting Phase: Check for Extra Files\n");
		String remoteLog= "./DirectoryLog/" + this.strAppName.replace(" ", "_") + "_Log.txt";
		String remoteGoldLog= "./DirectoryLog/" + this.strAppName.replace(" ", "_") + "_Gold_Log.txt";
		String diff= null;
		
		if (this._Testbed()._RemoteServer()._SysInfo()._isMac())
			diff= this._Testbed()._RemoteServer()._runCommand("diff -a " + remoteGoldLog + " " + remoteLog);
		else  
			diff= this._Testbed()._RemoteServer()._runCommand("\\\\munchkin\\Development\\TestAutomation\\Utils\\diff.exe", "-a", remoteGoldLog, remoteLog);
		
		String[] diffLines= diff.split("\n");
		
		int numMissing= 0;
		int numExtra= 0;
		int itDiff= 0;
		
		while( itDiff < diffLines.length) {
			if (diffLines[itDiff].contains("<")) { 
				this._Logs()._ResultLog()._logWarning("File no longer exists: " + diffLines[itDiff]);
				numMissing++;
			}
			else if (diffLines[itDiff].contains(">")) { 
				this._Logs()._ResultLog()._logWarning("New file found: " + diffLines[itDiff]);
				numExtra++;
			}
			itDiff++;
		}
		
		this._Logs()._ResultLog()._logMessage(numMissing + " files are either missing or were removed.");
		this._Logs()._ResultLog()._logMessage(numExtra + " new files were found.");
	}

}
