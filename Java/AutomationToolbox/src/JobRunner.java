package AutomationToolbox.src;

import iZomateCore.TestCore.TestParameters;
import org.hyperic.sigar.Sigar;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class JobRunner {
	static public String	NO_CLASSPATH_SPECIFIED= "<No Classpath Specified!>";
	static public String	NO_CMD_LINE_ARGS_SPECIFIED= "<No Command Line Args Specified!>";
	
	private File			m_fJobRequest= null;
	private File 			m_fJobXML= null;
	private File			m_fJobDir= null;
	private File			m_fCompletedDir= null;
	private File			m_fRunningDir= null;
	private File			m_fQueuedDir= null;
	public 	String			m_strJobID= null;
	public	String			m_strTimestamp= "";
	public	String 			m_strUser= "";
	public	String			m_strOptCmdLineArgs= "";
	public	String			m_strElapsedTime= "--";
	public	String			m_strClassPathSeparator;
	public	Process		 	m_pProcess= null;
	public	List<String> 	m_strTestbeds= new ArrayList<String>();
	public	List<String>	m_strClasspath= new ArrayList<String>();
	public	List<TestInfo>	m_fTests= new ArrayList<TestInfo>();
	public	boolean			m_bErrors= false;
		
	/**
	 * 
	 * @param fJob
	 */
	public JobRunner( File fJobRequest, File fRunningDir, File fCompletedDir, File fQueuedDir ) {
		this.m_fJobRequest= fJobRequest;
		this.m_fCompletedDir= fCompletedDir;
		this.m_fQueuedDir= fQueuedDir;
		this.m_fRunningDir= fRunningDir;
		this._readJobFile( fJobRequest );	
		this.m_strClassPathSeparator= System.getProperty("os.name").equals( "Mac OS X" ) ? ":" : ";";
 	}
	
	/**
	 * 
	 * @return
	 */
	public File _GetJobFile() {
		return this.m_fJobXML;
	}
	
	/**
	 * 
	 * @return
	 */
	public File _GetOutputFile() {
		return new File( this.m_fJobXML.getParentFile(), this.m_fJobXML.getName().replace( ".job.xml", ".job.txt" ) );
	}
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<File> _GetResultsFile() {
		return this._findFiles( this._getJobDir(), "_ResultLog.html" );
	}
	
	/**
	 * 
	 * @return
	 */
	public String _GetStatusString() {
		return this._getJobDir().getParentFile().getName();
	}
	
	/**
	 * 
	 * @return
	 */
	public String _GetJobID() {
		if( this.m_strJobID == null )
			this.m_strJobID= this.m_fJobXML.getParentFile().getName();
		return this.m_strJobID;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean _IsValid() {
		return this.m_fJobXML != null;
	}
	
	/**
	 * 
	 * @param fParentDir
	 */
	private File _CreateJobDir( File fParentDir ) {
		File fJobDir= null;
		// Create a unique folder for staging the job
		do {
		    this.m_strJobID= this.m_strUser.replace( " ", "-") + "_" + new SimpleDateFormat("yyyy.MM.dd_hh.mm.ss.SSS").format( new Date() );
		    fJobDir= new File(fParentDir.getAbsolutePath() + "/" + this.m_strJobID);
		} while( fJobDir.exists() );
					
		return fJobDir;
	}
	
	/**
	 * 
	 * @param fRunningDir
	 * @param fCompletedDir
	 */
	public String _Run() {		
		try {			
			if( this.m_fJobXML.getAbsolutePath().contains( this.m_fQueuedDir.getAbsolutePath() ) ) {
				// Move from Queued to Running
				this.m_strJobID= this.m_fJobXML.getParentFile().getName();
				this.m_fJobDir= new File( this.m_fRunningDir, this.m_strJobID );
				this.m_fJobXML.getParentFile().renameTo( this.m_fJobDir );
				this.m_fJobXML= new File( this.m_fJobDir, this.m_fJobXML.getName() );
			}
			else {
				this.m_fJobDir= this._CreateJobDir( this.m_fRunningDir );
				// Create the job dir
				this.m_fJobDir.mkdirs();
				// Move it to the new Running Dir
				this.m_fJobXML.renameTo( new File( this.m_fJobDir, this.m_fJobXML.getName() ) );
				// Point it to new file in new location
				this.m_fJobXML= new File( this.m_fJobDir, this.m_fJobXML.getName() );
			}
    		
    		// Classpath
    		String strFullClassPath="";
    		if( this.m_strClasspath.isEmpty() && !DatabaseMgr._Preferences()._GetPref( Preferences.DefaultJars ).isEmpty() )
    			this.m_strClasspath.add( DatabaseMgr._Preferences()._GetPref( Preferences.DefaultJars ) );
    		
    		for( String strClassPath : this.m_strClasspath )
    			strFullClassPath+= (strFullClassPath.isEmpty() ? "" : this.m_strClassPathSeparator ) + this._getFullClassPath( strClassPath );
    		
    		// Run the tests
    		boolean bOK= true;
			for( TestInfo fTestFile :  this.m_fTests ) {
				if( !fTestFile._Dataparam().exists() ) {
					System.out.println( "Could not start job " + this.m_fJobXML.getName() +" because folder does not exist: " + fTestFile._Dataparam().getAbsolutePath().replace("\\", "/") );
					bOK= false;
				}
			}
			
			if( bOK )
				this._runTest( this.m_fTests, strFullClassPath );
		}
		catch( Exception e ) {
			System.out.println( e.getMessage() );
			this._log( e.getMessage() );
			this._Kill();
			this.m_strJobID= null;
		}
		
		return this.m_strJobID;
	}
	
	/**
	 * 
	 * @return
	 */
	public String _GetClassPathString() {
		String strClassPath= "";
		for( String s : this.m_strClasspath )
			strClassPath+= (strClassPath.isEmpty() ? "" : this.m_strClassPathSeparator ) + s;
		if( strClassPath.isEmpty() )
			strClassPath= JobRunner.NO_CLASSPATH_SPECIFIED;
		return strClassPath;
	}
	
	/**
	 * 
	 * @return
	 */
	public String _GetCommandLineArgs() {
		if( this.m_strOptCmdLineArgs.isEmpty() )
			return JobRunner.NO_CMD_LINE_ARGS_SPECIFIED;
		return this.m_strOptCmdLineArgs;
	}

	/**
	 * 
	 * @return
	 */
	public boolean _IsRunning() {
		if( this.m_strJobID == null )
			this.m_strJobID= this._getJobDir().getName();
		
		String strPID= this._GetAllRunningJobs().get( this.m_strJobID );
		
		if( strPID == null /*this.m_pProcess == null || this.m_pProcess.exitValue() == 0*/ )
			return false;
		else 
			return true;
	}
	
	/**
	 * 
	 * @param fQueuedDir
	 */
	public void _QueueJob() {
		// Make sure it is not already queued
		if( !this.m_fJobXML.getAbsolutePath().contains( this.m_fQueuedDir.getAbsolutePath() )) {
			File fQueuedJobDir= this._CreateJobDir( this.m_fQueuedDir );
			fQueuedJobDir.mkdirs();
			File fNewFile= new File(fQueuedJobDir, this.m_fJobXML.getName());
			this.m_fJobXML.renameTo( fNewFile );
			this.m_fJobXML= fNewFile;
		}		
	}
	
	/**
	 * 
	 */
	public void _Retire( File fRetiredDir ) {
		if( fRetiredDir != null && fRetiredDir.exists() ) {
			System.out.println( "Moving " + this._getJobDir().getName() + " to " + fRetiredDir.getAbsolutePath() );
			this._getJobDir().renameTo( new File( fRetiredDir, this._getJobDir().getName() ) );
		}
	}
		
	/**
	 * 
	 */
	public void _CleanUp() {
		File fJobDirToMove= null;
		
		//Always move to completed
		if( this.m_fCompletedDir != null )
			fJobDirToMove= this._getJobDir();
		
		if( fJobDirToMove != null )
			fJobDirToMove.renameTo(new File(this.m_fCompletedDir, fJobDirToMove.getName()));
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean _Kill() {
		System.out.println( "Attempting to kill " + this._GetJobID() );
		String strPIDs= this._GetAllRunningJobs().get( this._GetJobID() );
		if( strPIDs != null ) {
			for( String strPID : strPIDs.split(","))
				try {
					if( System.getProperty("os.name").contains( "Mac OS X" ) ) 
						new ProcessBuilder( "/bin/bash", "-c", "kill -9 " + strPID ).start();
					else
						new Sigar().kill( Long.valueOf(strPID), -9 );
				} catch( Exception e ) {
					e.printStackTrace();
				}
		}
		this._CleanUp();
		
		return( strPIDs != null );
	}
	
	/**
	 * 
	 * @return
	 */
	private File _getJobDir() {
		if( this.m_fJobDir != null )
			return this.m_fJobDir;
		else if( this.m_fJobXML != null ) {
			if( this.m_fJobXML.isDirectory() )
				return this.m_fJobXML;
			else if( !this.m_fJobXML.getParentFile().equals( this.m_fRunningDir ))
				return this.m_fJobXML.getParentFile();
		}
		return this.m_fJobRequest;
	}
	
	/**
	 * Helper function for determining results dir for a dataparam file
	 * @param fDataparamFile
	 * @param strTestbed
	 * @return
	 */
	public static String GetResultsDirFromTest( File fDataparamFile, String strTestbed ) {
		return "AutomationResults" + fDataparamFile.getParent().substring(fDataparamFile.getParent().indexOf("/DataParams/")) + "/" + strTestbed;
	}
	
	/**
	 * 
	 * @param fTestFile
	 * @param strClassPath
	 * @throws Exception 
	 */
	private void _runTest( List<TestInfo> fTestFiles, String strClassPath ) throws Exception {	
		File f= new File( this.m_fJobDir, "run" + (System.getProperty("os.name").equals( "Mac OS X" )?".command":".bat") );
		RandomAccessFile fRunFile = new RandomAccessFile( f, "rw");

		try {
			for( TestInfo pTestInfo :  this.m_fTests ) {
		        TestParameters pTestParams= new TestParameters( new String[]{"-paramsFile", pTestInfo._Dataparam().getAbsolutePath().replace("\\", "/")} );
		        		        		        
		        //  comp/testpath
		        String strResultsDirName= GetResultsDirFromTest( pTestInfo._Dataparam(), pTestInfo._Testbed() );

				String test= "java -debug" + 
							 " -classpath " + strClassPath + " " + 
							 pTestParams._GetCommonParams()._GetMainClass() +
							 " jobid=" + this.m_strJobID + 
							 " -paramsFile \"" + pTestInfo._Dataparam().getAbsolutePath().replace("\\", "/") + "\"" +
							 " -logDir \"" + this.m_fJobDir.getAbsolutePath().replace("\\", "/") + "/" + strResultsDirName + "\"" +
							 " -testbed \"" + pTestInfo._Testbed() + "\" " +
							 this.m_strOptCmdLineArgs + "\n";
				fRunFile.writeBytes( test );
			}
		} finally {
			fRunFile.close();
		}
		
		try {
			String runCmd= f.getAbsolutePath() + " >> \"" + this._GetOutputFile().getAbsolutePath().replace("\\", "/") + "\" 2>&1";
			if( System.getProperty("os.name").equals( "Mac OS X" ) ) {
				this._log( "/bin/bash -c " + runCmd + "\n\n" );
				this.m_pProcess= Runtime.getRuntime().exec( new String[]{"chmod", "777", f.getAbsolutePath()} );
				this.m_pProcess= Runtime.getRuntime().exec( new String[]{"/bin/bash", "-c", runCmd} );
			}
			else {
				this._log( "cmd /C " + runCmd + "\n\n" );
				this.m_pProcess= Runtime.getRuntime().exec(new String[]{"cmd", "/C", runCmd});
			}
		} catch( Exception e ) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * 
	 */
	private void _readJobFile( File fJobRequest ) {
		// If we are pointing to a Job folder then find the .job.xml file
		if( fJobRequest.isDirectory() ) {
			for( File f : fJobRequest.listFiles() )
				if( f.getName().endsWith( ".job.xml" )) {
					this.m_fJobXML= f;
					break;
				}
		}
		else if( fJobRequest.getName().endsWith( ".job.xml"  ) ) 
			this.m_fJobXML= fJobRequest;
		
		// Make sure we found the .job.xml file
		if( this.m_fJobXML == null ) {
			System.out.println( "No .job.xml file found in Job folder: " + fJobRequest.getAbsolutePath().replace("\\", "/") );
			this.m_bErrors= true;
			return;
		}
				
        SAXBuilder builder = new SAXBuilder(false);
        Document doc;
		try {
			doc= builder.build( this.m_fJobXML );
		    //Get the root element
	        Element root= doc.getRootElement();
	        	      
	        this.m_strTimestamp= 	this._getChildText( JobTags.Timestamp.toString(), root, null );
	        this.m_strUser= 	 	this._getChildText( JobTags.User.toString(), root, null );
	        
	        // <CommandLineArgs>...</CommandLineArgs>
			List<?> strItems= root.getChildren( JobTags.CommandLineArgs.toString() );
	        for( Object objItem : strItems )
	        	if( !((Element)objItem).getText().isEmpty() )
	        		this.m_strOptCmdLineArgs+= (this.m_strOptCmdLineArgs.isEmpty() ? "" : " ") + ((Element)objItem).getText();
	        // <Classpath>...</Classpath>
	        strItems= root.getChildren( JobTags.Classpath.toString() );
	        for( Object objItem : strItems )
	        	if( !((Element)objItem).getText().isEmpty() )
	        		this.m_strClasspath.add( ((Element)objItem).getText() );
	        // <DataParamFile testbed="machine1" group="Bank1">...</CommandLineArgs>
	        strItems= root.getChildren( JobTags.DataParamFile.toString() );
	        for( Object objItem : strItems )
	        	if( !((Element)objItem).getText().isEmpty() ) {
	        		this.m_fTests.add( new TestInfo( ((Element)objItem).getText(), ((Element)objItem).getAttributeValue("testbed")) );
	        		this.m_strTestbeds.add( ((Element)objItem).getAttributeValue("testbed") );
	        	}
	        
		} catch( Exception e ) {
			e.printStackTrace();
			this._log( e.getMessage() );
			this.m_bErrors= true;
		}
	}
	
	/**
	 * 
	 * @param strChild
	 * @param eRoot
	 * @return
	 */
	private String _getChildText( String strChild, Element eRoot, String strDefault ) {
		try {
			return eRoot.getChild( strChild ).getText();
		} catch ( Exception e ) {
			return strDefault;
		}
	}
	
	/**
	 * 
	 * @param strClassPath
	 * @return
	 * @throws Exception 
	 */
	private String _getFullClassPath( String strClassPath ) throws Exception {
		String strFullClassPath= strClassPath;
		
		// Check to see if we are pointing to a directory.  If so then include all subdirs in classpath
		File fClassPathDir= new File( strClassPath );
		if( ! fClassPathDir.exists() )
			throw new Exception( "ERROR: Classpath item not found: " + strClassPath );			
		
		if( fClassPathDir.isDirectory() ) {
			strFullClassPath= "\"" + fClassPathDir.getAbsolutePath().replace("\\", "/") + "/*\"";
			for( File f : fClassPathDir.listFiles() ) {
				if( f.isDirectory() )
					strFullClassPath+= this.m_strClassPathSeparator + this._getFullClassPath( f.getAbsolutePath().replace("\\", "/") );
			}
		}
		
		return strFullClassPath;
	}
	
	/**
	 * 
	 * @return
	 */
	public Map<String, String> _GetAllRunningJobs() {
		if( System.getProperty("os.name").equals( "Mac OS X" ) )
			return this._GetAllRunningJobsMac();
		else
			return this._GetAllRunningJobsWin();
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, String> _GetAllRunningJobsMac() {
		Map<String, String> list= new HashMap<String, String>();
		
		try {			
		    String command= "ps -x -o pid,time,command";
		    Process child= Runtime.getRuntime().exec( command );
            StringBuffer stdOut= new StringBuffer();
            BufferedInputStream outBuf = new BufferedInputStream(child.getInputStream());
		    int c;
		    
		    while( (c = outBuf.read()) != -1 ) {
		    	if( c == 10 ) {
	    			String strPid= "";
	    			String strTime= "";
	    			String strCommand= "";
	    			String strJobID= "";
	    			
			    	if( stdOut.toString().contains( "/run.command" ) && stdOut.toString().contains( "/AutomationToolbox/ManagerStagingDirs/Running/" ) ) {
		    			for( int i= 0; i < stdOut.length(); ++i ) {
		    				if( strPid.isEmpty() )
		    					while( stdOut.charAt( i ) != ' ' )
		    						strPid+= stdOut.charAt( i++ );
		    				else if( strTime.isEmpty() )
		    					while( stdOut.charAt( i ) != ' ' )
		    						strTime+= stdOut.charAt( i++ );
		    				else
		    					while( i < stdOut.length()  )
		    						strCommand+= stdOut.charAt( i++ );
		    			}
		    					    			
		    			int x= strCommand.indexOf( "/Running/" );
		    			if( x != -1 )
		    				while( strCommand.charAt( 9 + x ) != '/' )
		    					strJobID+= strCommand.charAt( 9 + x++ );		    			
		    				
		    			list.put( strJobID, (list.get(strJobID)!=null?list.get(strJobID)+",":"") + strPid );
		    		}
			    	else if( stdOut.toString().contains( "java -debug -classpath" ) ) {
		    			for( int i= 0; i < stdOut.length(); ++i ) {
		    				if( strPid.isEmpty() )
		    					while( stdOut.charAt( i ) != ' ' )
		    						strPid+= stdOut.charAt( i++ );
		    				else if( strTime.isEmpty() )
		    					while( stdOut.charAt( i ) != ' ' )
		    						strTime+= stdOut.charAt( i++ );
		    				else
		    					while( i < stdOut.length()  )
		    						strCommand+= stdOut.charAt( i++ );
		    			}
		    			
		    			int x= strCommand.indexOf( "jobid=" );
		    			if( x != -1 )
		    				while( strCommand.charAt( 6 + x ) != ' ' )
		    					strJobID+= strCommand.charAt( 6 + x++ );
		    					    				
		    			list.put( strJobID, (list.get(strJobID)!=null?list.get(strJobID)+",":"") + strPid );
		    		}
		    		//Reset the string buff
		    		stdOut.setLength( 0 );
		    	}
		    	else
		    		stdOut.append( (char)c);
		    }
		    outBuf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> _GetAllRunningJobsWin() {
		Map<String, String> list= new HashMap<String, String>();
		Sigar mSigar= new Sigar();
		
		try {
			for( long p : mSigar.getProcList() ) {
				try {
					if( mSigar.getProcExe(p).getName().contains( "cmd.exe" ) ) {
						for( String arg : mSigar.getProcArgs(p) ) {
							if( arg.contains( "\\run.bat" ) && arg.contains( "\\AutomationToolbox\\ManagerStagingDirs\\Running\\" ) ) {
								int x= arg.indexOf( "\\Running\\" );
			    				if( x != -1 ) {
			    					String strJobID= "";
			    					while( arg.charAt( 9 + x ) != '\\' )
			    						strJobID+= arg.charAt( 9 + x++ );
			    					list.put( strJobID, (list.get(strJobID)!=null?list.get(strJobID)+",":"") + String.valueOf( p ) );
			    				}
							}
						}
					}
					else if( mSigar.getProcExe(p).getName().endsWith( "java.exe" ) ) {
						for( String arg : mSigar.getProcArgs(p) ) {
							if( arg.startsWith("jobid=") ) {
								String strJobID= arg.substring(6);
								list.put( strJobID, (list.get(strJobID)!=null?list.get(strJobID)+",":"") + String.valueOf( p ) );
							}
						}
					}
				}
				catch (Exception e) {
					//Keep calm and carry on 
				}
			}
		} catch( Exception e ) {
			e.printStackTrace();
		}
			
		return list;	
	}
	
	/**
	 * 
	 * @param strData
	 */
	private void _log( String strData ) {
		try {
		    BufferedWriter out= new BufferedWriter(new FileWriter( this._GetOutputFile() ));
		    out.write( strData );
		    out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @return
	 */
	private ArrayList<File> _findFiles( File fDir, String strPartialMatch ) {
		ArrayList<File> lFiles= new ArrayList<File>();
		for( File fItem : fDir.listFiles() ) {
			if( fItem.isDirectory() ) {
				ArrayList<File> fResultsFiles= this._findFiles( fItem, strPartialMatch );
				if( !fResultsFiles.isEmpty() )
					lFiles.addAll( fResultsFiles );
			}
			else if( fItem.getName().endsWith( strPartialMatch ) )
				lFiles.add( fItem );
		}
		
		return lFiles;
	}


	/**
	 * 
	 * @author tskotz
	 *
	 */
	public class Parameter {
		private Element m_Element;
		
		public Parameter( Element parameterElement ) {
			this.m_Element= parameterElement;
		}
		
		public String _Name() {
			return this.m_Element.getAttribute( "name" ).getValue();			
		}

		public String _Type() {
			return this.m_Element.getAttribute( "type" ).getValue();			
		}

		public String _Value() {
			return this.m_Element.getAttribute( "value" ).getValue();			
		}

		public String _Description() {
			return this.m_Element.getAttribute( "description" ).getValue();			
		}
	}
	
	/**
	 * 
	 * @author tskotz
	 *
	 */
	public class TestInfo {
		private File m_strDataparamFile;
		private String m_strTestbed;
		
		public TestInfo( String strDataparamFile, String strTestbed ) {
			this.m_strDataparamFile= new File( strDataparamFile );
			this.m_strTestbed= strTestbed;
		}
		
		public File _Dataparam() {
			return this.m_strDataparamFile;			
		}

		public String _Testbed() {
			return this.m_strTestbed;			
		}
	}

}
