package AutomationToolbox.src;

import iZomateCore.UtilityCore.TimeUtils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ToolboxHTTPServer implements HttpHandler {
	
	static final String  VERSION= "0.0001";

	private String  mstrWorkingDir= new File("").getAbsolutePath();
	private String 	mstrTemplateDir= this.mstrWorkingDir + "/AutomationToolbox/Preferences/Templates/";
	private String  mstrPort= null;
	private String  mstrWebServerURL= null;
	private LoadBalancer mpLoadBalancer= new LoadBalancer();
	
	static  int		sRequestCounter= 0;
	static final String STATUS_SUCCESS= "success";
	static final String STATUS_FAILED= "fail";
	
	/**
	 * 
	 * @param strPort
	 */
	public ToolboxHTTPServer( String strPort ) {
		this.mstrPort= strPort;
	}
	
	/**
	 * 
	 */
	public void _SetPort( String strPort ) {
		this.mstrPort= strPort;
	}
	
	/**
	 * 
	 */
	@Override
	public void handle( HttpExchange exchange ) throws IOException {
		String requestMethod = exchange.getRequestMethod();
    	Headers responseHeaders = exchange.getResponseHeaders();
    	responseHeaders.set("Access-Control-Allow-Origin", "*"); //Allow javascript XMLHttpRequest calls

    	System.out.println( "\n**** Processing REST Request " + (++sRequestCounter) + " ****" );
		System.out.println( "path: " + exchange.getRequestURI().getPath() );
		System.out.println( "Query: " + exchange.getRequestURI().getQuery() );
		System.out.println( "Protocol: " + exchange.getProtocol() );
		System.out.println( "Method: " + exchange.getRequestMethod() );
		System.out.println( "Local: " + exchange.getRequestHeaders().get( "Host" ).get( 0 ) );
		System.out.println( "Local: " + exchange.getLocalAddress().getHostName() + ":" + exchange.getLocalAddress().getPort() );
		System.out.println( "referer: " + (exchange.getRequestHeaders().containsKey( "Referer" ) ? exchange.getRequestHeaders().get( "Referer" ).get( 0 ) : "undefined" ) );
		System.out.println( "Remote Svr: " + exchange.getRemoteAddress().getHostName() + ":" +  exchange.getRemoteAddress().getPort() );
		System.out.println( "Remote Adr: " + (exchange.getRequestHeaders().containsKey( "Origin" ) ? exchange.getRequestHeaders().get( "Origin" ).get( 0 ).replace( "http://", "" ) : "undefined" ) );
		
		this.mstrWebServerURL= "http://" + exchange.getRequestHeaders().get( "Host" ).get( 0 );

	    if (requestMethod.equalsIgnoreCase("GET")) {
	    	String strStatus= "Ooops!";
	    	byte[] bufJPEG= null;
	    	
	    	if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/RunJob" )) {
	    		strStatus= this._runJob( exchange );
		    	responseHeaders.set("Content-Type", "text/plain");
	    	}
	    	else if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/JobEditor" )) {
	    		strStatus= this._showJobEditorPage( exchange.getRequestURI().getQuery() );
		    	responseHeaders.set("Content-Type", "text/html");
	    	}
	    	else if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/Status" )) {
	    		strStatus= this._showStatusPage( exchange.getRequestURI().getQuery() );
		    	responseHeaders.set("Content-Type", "text/html");
	    	}
	    	else if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/Scheduler" )) {
	    		strStatus= this._showSchedulerEditor( exchange.getRequestURI().getQuery() );
		    	responseHeaders.set("Content-Type", "text/html");
	    	}
	    	else if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/Preferences" )) {
	    		strStatus= this._showPreferencesEditor( exchange.getRequestURI().getQuery() );
		    	responseHeaders.set("Content-Type", "text/html");
	    	}
	    	else if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/SavePreferences" )) {
	    		strStatus= this._savePreferences( exchange.getRequestURI().getQuery() );
		    	responseHeaders.set("Content-Type", "text/html");
	    	}
	    	else if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/GetImage" )) {
	    		bufJPEG= this._getImage( exchange.getRequestURI().getQuery() );
		    	responseHeaders.set("Content-Type", "text/jpeg");
	    	}
	    	else if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/GetResultData" )) {
	    		strStatus= this._getResultData( exchange.getRequestURI().getQuery() );
		    	responseHeaders.set("Content-Type", "text/html");
	    	}
	    	else if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/DataparamEditor" )) {
	    		strStatus= this._showDataParamEditor( exchange.getRequestURI().getQuery() );
		    	responseHeaders.set("Content-Type", "text/html");
	    	}
	    	else if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/GetDirs" )) {
	    		strStatus= this._getDirContents( exchange.getRequestURI().getQuery(), true );
		    	responseHeaders.set("Content-Type", "text/plain");
	    	}
	    	else if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/GetFiles" )) {
	    		strStatus= this._getDirContents( exchange.getRequestURI().getQuery(), false );
		    	responseHeaders.set("Content-Type", "text/plain");
	    	}
	    	else if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/GetToolboxStatus" )) {
	    		strStatus= System.getProperty("os.name") + " " + System.getProperty("os.version") + " running Toolbox version: " + ToolboxWindow.m_strVersion;
		    	responseHeaders.set("Content-Type", "text/plain");
	    	}
	    	else if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/SaveTestbed" )) {
	    		strStatus= this._SaveTestbed( exchange.getRequestURI().getQuery() );
		    	responseHeaders.set("Content-Type", "text/plain");
	    	}
	    	else if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/GetTestbedValue" )) {
	    		strStatus= this._GetTestbedValue( exchange.getRequestURI().getQuery() );
		    	responseHeaders.set("Content-Type", "text/plain");
	    	}
	    	else if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/UpdateTestbed" )) {
	    		strStatus= this._UpdateTestbed( exchange.getRequestURI().getQuery() );
		    	responseHeaders.set("Content-Type", "text/plain");
	    	}
	    	else if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/DeleteTestbed" )) {
	    		strStatus= this._DeleteTestbed( exchange.getRequestURI().getQuery() );
		    	responseHeaders.set("Content-Type", "text/plain");
	    	}
	    	else if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/SaveJob" )) {
	    		strStatus= this._saveJob( exchange.getRequestURI().getQuery() );
		    	responseHeaders.set("Content-Type", "text/plain");
	    	}
	    	else if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/DeleteJob" )) {
	    		strStatus= this._DeleteJob( exchange.getRequestURI().getQuery() );
		    	responseHeaders.set("Content-Type", "text/plain");
	    	}
	    	else if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/GetJobInfo" )) {
	    		strStatus= this._GetJobInfo( exchange.getRequestURI().getQuery() );
		    	responseHeaders.set("Content-Type", "text/plain");
	    	}
	    	else if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/GetUserJobs" )) {
	    		strStatus= this._GetUserJobs( exchange.getRequestURI().getQuery() );
		    	responseHeaders.set("Content-Type", "text/plain");
	    	}
	    	else if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/LB/GetNumJobs" )) {
	    		strStatus= this.mpLoadBalancer._GetNumJobsRequest( exchange.getRequestURI().getQuery() );
		    	responseHeaders.set("Content-Type", "text/html");
	    	}
	    	else if( exchange.getRequestURI().getPath().equalsIgnoreCase( "/AutoManager/GetTestbedDescriptor" )) {
				strStatus= this._GetTestbedDescriptor( exchange.getRequestURI().getQuery() );
		    	responseHeaders.set("Content-Type", "text/html");
	    	}
	    	else {
	    		strStatus= "Unknown Request: " + exchange.getRequestURI().getPath();
	    	    System.out.println( strStatus );	
	    	}
	    	
	    	exchange.sendResponseHeaders(200, 0);

	    	OutputStream responseBody = exchange.getResponseBody();
	    	if( bufJPEG != null )
	    		responseBody.write( bufJPEG );
	    	else
	    		responseBody.write( strStatus.getBytes() );
	    		
	    	responseBody.close();

			System.out.println( "Done Processing REST Request " + sRequestCounter );
	    }		
	}
	
	/**
	 * 
	 */
	public void _OpenPreferencesPage() {
		this._0penWebPage( "/AutoManager/Preferences" );
	}
	
	/**
	 * 
	 * @param URL
	 */
	private void _0penWebPage( String strURLPartial) {
		java.awt.Desktop myNewBrowserDesktop = java.awt.Desktop.getDesktop();
		java.net.URI myNewLocation;
		try {
			String str= "http://" + InetAddress.getLocalHost().getHostName() + ":" + this.mstrPort + strURLPartial;
			myNewLocation = new java.net.URI( str );
			myNewBrowserDesktop.browse( myNewLocation );		
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private String _HeaderGenerator( String strText ) {
		return
		"<table style=\"width:100%;padding:0px;border:0px solid black;xbackground-color:#eeeeee;\">\n" +
		"	<tr>\n" +
		"		<td style=\"padding:10px;border:0px solid black;width:100px\"><img src=\""+ this.mstrWebServerURL + "/AutoManager/GetImage?" + DatabaseMgr._Preferences()._GetPref( Preferences.DashboardLogo ) + "\" style=\"width:100\"></td>\n" +
		"		<td style=\"padding:10px;border:0px solid black\"><font style=\"font-size:32px;\"><b>" + strText + "</b></font><br>" + 
		"											<a href=\"/AutoManager/JobEditor\"><button style=\"color:#0000ff;\">Create Job</button></a>\n" +
		"											<a href=\"/AutoManager/Status\"><button style=\"color:#0000ff;\">Status Page</button></a>" + 
		"											<a href=\"/AutoManager/Scheduler\"><button style=\"color:#0000ff;\">Scheduler</button></a>" + 
		"											<a href=\"/AutoManager/DataparamEditor?dataparam=\"\"\"><button style=\"color:#0000ff;\">New Dataparam</button></a>\n" +
		"											<a href=\"/AutoManager/Preferences\"><button style=\"color:#0000ff;\">Preferences</button></a>" + 
				"</td>\n" +
		"	</tr>\n" +
		"</table><hr>\n";
	}

	/**
	 * ex: 
	 * http://tskotz-mac-wifi:8080/AutoManager/Run?User=terry&platform=win&testbed=10.211.55.4&dataparamfile=Products/RX3/ParrellesDemo/RX3BatchFileMatrixTestWinCopy.xml&dataparamfile=Products/RX3/ParrellesDemo/RX3PluginHostingTestWinCopy.xml
	 * 
	 * @param strRequestQuery
	 */
	private String _runJob( HttpExchange exchange ) {
		// Try Load Balancer first if this request did not originate from a load balanced request
		if( this.mpLoadBalancer._Distribute( exchange ) )
			return ToolboxHTTPServer.STATUS_SUCCESS;

		String strStatus= ToolboxHTTPServer.STATUS_SUCCESS;
		String strRequestQuery= exchange.getRequestURI().getQuery();
		
		if( strRequestQuery != null ) {				
			try {
				List<String> pDataparamFiles= new ArrayList<String>();

				// root element
				Element eJob = new Element("Job");

				// Insert the Timestamp Element
				eJob.addContent( new Element( JobTags.Timestamp.name() ).setText( TimeUtils.getDateTime().replace( "-", "/" ) ) );
				
				for( String strParam : strRequestQuery.split( "&" ) ) {
					String[] aElementInfo= strParam.split( "=" );
					if( aElementInfo.length == 2 ) {
						if( aElementInfo[0].equals( "jobname" ))
							eJob.addContent( new Element( JobTags.JobName.name() ).setText( aElementInfo[1].trim() ) );
						else if( aElementInfo[0].equals( "user" ))
							eJob.addContent( new Element( JobTags.User.name() ).setText( aElementInfo[1].trim() ) );
						else if( aElementInfo[0].equals( "jobtemplate" ))
							eJob.addContent( new Element( JobTags.JobTemplate.name() ).setText( aElementInfo[1].trim() ) );
						else if( aElementInfo[0].equals( "classpath" ))
							eJob.addContent( new Element( JobTags.Classpath.name() ).setText( aElementInfo[1].trim() ) );
						else if( aElementInfo[0].equals( "commandlineargs" ))
							eJob.addContent( new Element( JobTags.CommandLineArgs.name() ).setText( aElementInfo[1].trim() ) );
						else if( aElementInfo[0].equals( "origin" ))
							eJob.addContent( new Element( JobTags.Origin.name() ).setText( aElementInfo[1].trim() ) );
						else if( aElementInfo[0].equals( "dataparamfile" )) {
							// i.e. dataparamfile=Products/RX3/RX3FileLoadingTestMac.xml;Bank1;dependacyxml;false
							pDataparamFiles.add( aElementInfo[1].trim() );
						}
						else
							throw new Exception( "An error was found parsing REST command:  Unknown parameter: " +  strParam );						
					}
				}
				
				this._ProcessDataparamFiles( pDataparamFiles, eJob );
				
				// write the content into xml file	
				XMLOutputter xmlOutput = new XMLOutputter();
				xmlOutput.setFormat(Format.getPrettyFormat());
				String strFileName= ToolboxWindow._IncomingDir().getAbsolutePath() + "/" +  eJob.getChild( JobTags.JobName.name() ).getText().replace(" ", "_") + ".job.xml";
				Document doc= new Document( eJob );
				xmlOutput.output( doc, new FileWriter( strFileName ) );
				xmlOutput.output( doc, System.out );
				
			} catch( Exception e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				strStatus= e.getMessage() != null ? e.getMessage() : "Run Job Failed";
			}
	
		}
		return strStatus;		
	}
	
	/**
	 * 
	 * @param pDataparamFiles
	 * @param eJob
	 * @throws Exception
	 */
	private void _ProcessDataparamFiles( List<String> pDataparamFiles, Element eJob ) throws Exception
	{
		String strDataparamDir= new File(DatabaseMgr._Preferences()._GetPref( Preferences.DataparamsRootDir )).getCanonicalPath();
		for( String strDPFile : pDataparamFiles ) {
			String[] aTestInfo= strDPFile.trim().split(";");
			String strDataparamFile= aTestInfo[0];
			String strTestbedOrGroup= aTestInfo[1].trim();	
			String strDependency= aTestInfo.length>3 ? aTestInfo[2] : "";
			boolean bParallelize= aTestInfo.length>3 ? aTestInfo[3].equals("true") : false;
			
			// If we have an origin then get the testbed info directly from origin
			TestbedDescriptor pTBDescr= null;
			String strOrigin= eJob.getChildTextTrim( JobTags.Origin.name() );
			if(  strOrigin != null && !strOrigin.isEmpty() ) {
				List<String> pOrigins= Arrays.asList( strOrigin.toLowerCase().split(";") );
				String strResult= LoadBalancer._PostURL(  "http://" + pOrigins.get( 0 ) + "/AutoManager/GetTestbedDescriptor?" + strTestbedOrGroup );
				System.out.println( strResult );
				pTBDescr= new TestbedDescriptor( strResult );
			}
			// If we still don't have a testbed descriptor then get it locally
			if(  pTBDescr == null )
				pTBDescr= DatabaseMgr._Testbeds()._GetTestbedDescriptor( strTestbedOrGroup );
			// Now check it for real
			if( pTBDescr == null )
				throw new Exception(  strTestbedOrGroup + " could not be found!");
			
			String strTestbedLookupValue= pTBDescr.mstrValue;
			
			// i.e. Check if it is a Group : "machine1, machine2, machine3, machine4, machine5"
			for( String strThisTestbed : strTestbedLookupValue.split(",")) {
				Element aElement= new Element( JobTags.DataParamFile.name() );
				aElement.setText( strDataparamDir + "/" + strDataparamFile );
				aElement.setAttribute( "testbed", strThisTestbed.trim() );
				if( strTestbedLookupValue != strThisTestbed )
					aElement.setAttribute( "group", strTestbedOrGroup );
				aElement.setAttribute( "parallelize", bParallelize?"true":"false" );
				aElement.setAttribute( "dependency", strDependency );
				eJob.addContent( aElement );
			}
		}
	}
	
	/**
	 * ex: 
	 * http://tskotz-mac-wifi:8080/AutoManager/Run?User=terry&platform=win&testbed=10.211.55.4&dataparamfile=Products/RX3/ParrellesDemo/RX3BatchFileMatrixTestWinCopy.xml&dataparamfile=Products/RX3/ParrellesDemo/RX3PluginHostingTestWinCopy.xml
	 * 
	 * @param strRequestQuery
	 */
	private String _saveJob( String strRequestQuery ) {
		String strStatus= STATUS_SUCCESS;
		
		if( strRequestQuery != null ) {
			try {
				strStatus= DatabaseMgr._Jobs()._AddJob( new JobDescriptor( strRequestQuery ) );
			} catch( Exception e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				strStatus= e.getMessage();
			}
		}
		return strStatus;		
	}

	/**
	 * ex:
	 * http://tskotz-mac-wifi:8080/AutoManager/JobEditor
	 * http://outside.home:8380/AutoManager/JobEditor?preloaduser=tskotz&preloadjob=test1
	 * 
	 * @param strRequestQuery
	 * @return
	 */
	private String _showJobEditorPage( String strRequestQuery ) {	
		String[] astrJobTemplateInfo= null;
		
		if( strRequestQuery != null ) {
			for( String strParam : strRequestQuery.split( "&" ) ) {
				String[] aElementInfo= strParam.split( "=" );
				if( aElementInfo.length == 2 ) {
					if( aElementInfo[0].equals( "loadtemplate" ))
						astrJobTemplateInfo= aElementInfo[1].split("/");
					else
						System.out.println("Warning: Unknown REST param: " + strParam );
				}
			}
		}
				
		String 	strTemplateFile= this.mstrTemplateDir + "/JobEditor.html";
		String[] strTestbeds= DatabaseMgr._Testbeds()._GetTestbeds();
        StringBuilder sb = new StringBuilder();
		
		BufferedReader br= null;
	    try {
			br= new BufferedReader(new FileReader(strTemplateFile));
	        String line = br.readLine();

	        while (line != null) {
	        	// If we are preloading a job then insert the steps to automate what the user would do on the page
	            if( astrJobTemplateInfo != null && line.contains( "<body>" ))
            		line= line.replace("body", "body onload=\"document.getElementById('UsersMenu').value='"+astrJobTemplateInfo[0]+"';UserSelected();document.getElementById('JobsMenu').value='"+astrJobTemplateInfo[1]+"';JobSelected();LoadJob();\"");

	            sb.append(line+"\n");
	            //System.out.println( line );
	            
	            if( line.equals( "<!-- Insert Header -->" ))
	            	sb.append( this._HeaderGenerator("Job Editor") );
	            else if( line.contains( "id=\"Testbeds\"" )) {
            		sb.append( "  <option value=\"--Select--\">--Select--</option>\n" );
	            	for( String strTestbed : strTestbeds ) {
	            		TestbedDescriptor pTBDescr= DatabaseMgr._Testbeds()._GetTestbedDescriptor(strTestbed);
	            		sb.append( "  <option id=\"" + strTestbed + "\" value=\"" + pTBDescr.mstrValue + "\" type=\"" + 
	            					  pTBDescr.mstrType + "\" descr=\"" + pTBDescr.mstrDescription + "\">" + strTestbed + "</option>\n" );
	            	}
	            }
	            else if( line.contains( "id=\"UsersMenu\"" )) {
            		sb.append( "  <option value=\"--Select--\">--Select--</option>\n" );
            		ArrayList<String> arrUsers= DatabaseMgr._Users()._GetUsers();
	            	for( String strUser : arrUsers )
	            		sb.append( "  <option value=\"" + strUser + "\">" + strUser + "</option>\n" );
	            }
	            else if( line.contains( "id=\"JobsMenu\"" )) {
            		sb.append( "  <option value=\"--Select--\">--Select--</option>\n" );
            		ArrayList<JobDescriptor> arrJobs= DatabaseMgr._Jobs()._GetJobs( null );
	            	for( JobDescriptor pJob : arrJobs )
	            		sb.append( "  <option value=\"" + pJob.mstrJobName + "\">" + pJob.mstrJobName + "</option>\n" );
	            }
	            else if( line.contains( "id=\"TestsRoot\"" )) {
            		sb.append( "  <option value=\"--Select--\">--Select--</option>\n" );
            		File fDataParamsDir= new File(DatabaseMgr._Preferences()._GetPref( Preferences.DataparamsRootDir ));
            		if( fDataParamsDir.exists() ) {
		            	for( File fFile : fDataParamsDir.listFiles() )
		            		if( fFile.isDirectory() )
		            			sb.append( "  <option value=\"" + fFile.getName() + "\">" + fFile.getName() + "</option>\n" );
            		}
            		else
            			System.out.println( "Could not find DataParameters directory: " + fDataParamsDir.getAbsolutePath() );
	            }
	            		
	            line = br.readLine();
	        }
	    } catch( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
	        try {
				br.close();
			} catch( IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    			
		return sb.toString();
	}

	/**
	 * ex:
	 * http://tskotz-mac-wifi:8080/AutoManager/Status
	 * 
	 * @param strRequestQuery
	 * @return
	 */
	private String _showStatusPage( String strRequestQuery ) {		
		String 	strTemplateFile= this.mstrTemplateDir + "/Status.html";
        StringBuilder sb = new StringBuilder();
    	int count= 0;
		
		BufferedReader br= null;
	    try {
			br= new BufferedReader(new FileReader(strTemplateFile));
	        String line = br.readLine();

	        while( line != null ) {
	            sb.append( line+"\n" );	

	            if( line.equals( "<!-- Insert Header -->" ) )
	            	sb.append( this._HeaderGenerator("Status") );
	            else if( line.contains( "id=\"JobTable\"" )) {
	            	// Find the end of the heading row
	            	line = br.readLine();
	            	while( !line.contains( "</tr>" ) ) {
	    	            sb.append(line+"\n");
	    	            line = br.readLine();
	            	}
	            	sb.append( line+"\n" );
	            	
	    			for( File fStagingDir : new File[]{ToolboxWindow._RunningDir(), ToolboxWindow._QueuedDir(), ToolboxWindow._IncomingDir(), ToolboxWindow._CompletedDir()} ) {
		            	// Now insert Job Info		            	
		            	File[] lFiles= fStagingDir.listFiles();
		            	Arrays.sort(lFiles, new Comparator<File>(){
	    				    public int compare(File f1, File f2) {
	    				        return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
	    				    } 
	    				 });
		                
		            	for( File f : lFiles ) {
		            		if( f.isDirectory() )
		            			this._InsertJobInfo( f, sb, count++ );
		            	}
	    			}
	            }

	            line = br.readLine();
	        }
	    } catch( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
	        try {
				br.close();
			} catch( IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    			
		return sb.toString();
	}

	/**
	 * ex:
	 * http://tskotz-mac-wifi:8080/AutoManager/Status
	 * 
	 * @param strRequestQuery
	 * @return
	 */
	private String _showSchedulerEditor( String strRequestQuery ) {		
		String 	strTemplateFile= this.mstrTemplateDir + "/Scheduler.html";
        StringBuilder sb = new StringBuilder();
		
		BufferedReader br= null;
	    try {
			br= new BufferedReader(new FileReader(strTemplateFile));
	        String line = br.readLine();

	        while( line != null ) {
	            sb.append( line+"\n" );	

	            if( line.equals( "<!-- Insert Header -->" ) )
	            	sb.append( this._HeaderGenerator("Toolbox Scheduler") );

	            line = br.readLine();
	        }
	    } catch( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
	        try {
				br.close();
			} catch( IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    			
		return sb.toString();
	}

	/**
	 * ex:
	 * http://tskotz-mac-wifi:8080/AutoManager/Status
	 * 
	 * @param strRequestQuery
	 * @return
	 */
	private String _showPreferencesEditor( String strRequestQuery ) {		
		String 	strTemplateFile= this.mstrTemplateDir + "/PreferencesEditor.html";
        StringBuilder sb = new StringBuilder();
		
		BufferedReader br= null;
	    try {
			br= new BufferedReader(new FileReader(strTemplateFile));
	        String line = br.readLine();

	        while( line != null ) {
	            if( line.equals( "<!-- Insert Header -->" ) )
	            	line= this._HeaderGenerator("Toolbox Preferences");
	            else if( line.contains( "id=\"stagingdir\"" ) )
	            	line= line.replace( "value=\"\"", "value=\"" + DatabaseMgr._Preferences()._GetPref( Preferences.StagingDir ) + "\"" );
	            else if( line.contains( "id=\"dataparamsroot\"" ) )
	            	line= line.replace( "value=\"\"", "value=\"" + DatabaseMgr._Preferences()._GetPref( Preferences.DataparamsRootDir ) + "\"" );	            
	            else if( line.contains( "id=\"dashboardlogo\"" ) )
	            	line= line.replace( "value=\"\"", "value=\"" + DatabaseMgr._Preferences()._GetPref( Preferences.DashboardLogo ) + "\"" );
	            else if( line.contains( "id=\"defaultjars\"" ) )
	            	line= line.replace( "value=\"\"", "value=\"" + DatabaseMgr._Preferences()._GetPref( Preferences.DefaultJars ) + "\"" );
	            else if( line.contains( "id=\"showjobcount\"" ) )
	            	line= line.replace( "value=\"\"", "value=\"" + DatabaseMgr._Preferences()._GetPref( Preferences.ShowJobCount ) + "\"" );
	            else if( line.contains( "id=\"loadsharingmaster\"" ) )
	            	line= line.replace( "value=\"\"", "value=\"" + DatabaseMgr._Preferences()._GetPref( Preferences.AllowJobRequestsFrom ) + "\"" );
	            else if( line.contains( "id=\"loadsharingservers\"" ) )
	            	line= line.replace( "value=\"\"", "value=\"" + DatabaseMgr._Preferences()._GetPref( Preferences.SendJobRequestsTo ) + "\"" );
	            else if( line.contains( "body onload=\"\"" ) ) {
	            	String strOnLoadScript= "";
	            	if( DatabaseMgr._Preferences()._GetPrefBool( Preferences.StartTestManagerOnLaunch ) )
	            		strOnLoadScript+= "document.getElementById( 'starttestmanagercb' ).checked = true;";
	            	if( DatabaseMgr._Preferences()._GetPrefBool( Preferences.AllowJobRequestsFromCB ) )
	            		strOnLoadScript+= "document.getElementById( 'loadsharingmastercb' ).checked = true;";
	            	if( DatabaseMgr._Preferences()._GetPrefBool( Preferences.SendJobRequestsToCB ) )
	            		strOnLoadScript+= "document.getElementById( 'loadsharingserverscb' ).checked = true;";
		            if( DatabaseMgr._Preferences()._GetPrefBool( Preferences.EnableJobLoadBalancing ) )
		            	strOnLoadScript+= "document.getElementById( 'loadbalancingcb' ).checked = true;";
		            strOnLoadScript+= "EnableLoadBalancing()";
	            	line= line.replace( "onload=\"\"", "onload=\"" + strOnLoadScript + "\"" );	            	
	            }
	            	    			
	            sb.append( line+"\n" );	
	            line = br.readLine();
	        }
	    } catch( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
	        try {
				br.close();
			} catch( IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    			
		return sb.toString();
	}
	
	/**
	 * 
	 * @param strRequestQuery
	 * @return
	 */
	private String _savePreferences( String strRequestQuery ) {			
		if( strRequestQuery != null ) {
			for( String strParam : strRequestQuery.split( "&" ) ) {
				String[] aElementInfo= strParam.split( "=" );
				if( aElementInfo.length == 2 ) {
					if( aElementInfo[0].equals( "stagingdir" ))
						DatabaseMgr._Preferences()._PutPref( Preferences.StagingDir, aElementInfo[1] );
					else if( aElementInfo[0].equals( "dataparamsroot" ))
						DatabaseMgr._Preferences()._PutPref( Preferences.DataparamsRootDir, aElementInfo[1] );
					else if( aElementInfo[0].equals( "dashboardlogo" ))
						DatabaseMgr._Preferences()._PutPref( Preferences.DashboardLogo, aElementInfo[1] );
					else if( aElementInfo[0].equals( "defaultjars" ))
						DatabaseMgr._Preferences()._PutPref( Preferences.DefaultJars, aElementInfo[1] );
					else if( aElementInfo[0].equals( "showjobcount" ))
						DatabaseMgr._Preferences()._PutPref( Preferences.ShowJobCount, aElementInfo[1] );
					else if( aElementInfo[0].equals( "starttestmanagercb" ))
						DatabaseMgr._Preferences()._PutPref( Preferences.StartTestManagerOnLaunch, aElementInfo[1] );
					else if( aElementInfo[0].equals( "loadbalancingcb" ))
						DatabaseMgr._Preferences()._PutPref( Preferences.EnableJobLoadBalancing, aElementInfo[1] );
					else if( aElementInfo[0].equals( "loadsharingmastercb" ))
						DatabaseMgr._Preferences()._PutPref( Preferences.AllowJobRequestsFromCB, aElementInfo[1] );
					else if( aElementInfo[0].equals( "loadsharingmaster" ))
						DatabaseMgr._Preferences()._PutPref( Preferences.AllowJobRequestsFrom, aElementInfo[1] );
					else if( aElementInfo[0].equals( "loadsharingserverscb" ))
						DatabaseMgr._Preferences()._PutPref( Preferences.SendJobRequestsToCB, aElementInfo[1] );
					else if( aElementInfo[0].equals( "loadsharingservers" ))
						DatabaseMgr._Preferences()._PutPref( Preferences.SendJobRequestsTo, aElementInfo[1] );
					else
						System.out.println("Warning: Unknown REST param: " + strParam );
				}
			}
		}
				
		return STATUS_SUCCESS;
	}


	/**
	 * 
	 * @param strRequestQuery
	 * @return
	 */
	private byte[] _getImage( String strRequestQuery ) {
		String strResultFile= strRequestQuery;
	    byte[] fileData = null;
		try {
			File file = new File( strResultFile );
		    fileData = new byte[(int) file.length()];
		    DataInputStream dis = new DataInputStream(new FileInputStream(file));
		    dis.readFully(fileData);
		    dis.close();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fileData = new byte[0];
		}
	   
	   return fileData;
	}

	/**
	 * 
	 * @param strRequestQuery
	 * @return
	 */
	private String _getResultData( String strRequestQuery ) {
		String strResultFile= strRequestQuery;
		BufferedReader br= null;
        StringBuilder sb= new StringBuilder();
		try {
			br = new BufferedReader(new FileReader(strResultFile));
		    String line= br.readLine();
		    while( line != null ) {
			   sb.append( line );
			   line= br.readLine();
		    }
		   
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
	        try {
	        	if( br != null )
	        		br.close();
			} catch( IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	   
	   return sb.toString();
	}
	
	/**
	 * ex:
	 * http://tskotz-mac-wifi:8080/AutoManager/DataparameterEditor?dataparam=/Products/Alloy/AlloyPerfTestMac.xml
	 * 
	 * @param strRequestQuery
	 * @return
	 */
	private String _showDataParamEditor( String strRequestQuery ) {		
		String strTemplateFile= this.mstrTemplateDir + "/DataparamEditor.html";
		String strDataParamFile= null;
		File fDataparamterFile= null;
        String strDataParamDir= new File(DatabaseMgr._Preferences()._GetPref( Preferences.DataparamsRootDir )).getAbsolutePath();
        StringBuilder sb = new StringBuilder();
        
		for( String strParam : strRequestQuery.split( "&" ) ) {
			String[] aElementInfo= strParam.split( "=" );
			if( aElementInfo.length == 2 ) {
				if( aElementInfo[0].equals( "dataparam" ))
					strDataParamFile= strDataParamDir + aElementInfo[1];
				else
					System.out.println("Warning: Unknown REST param: " + strParam );
			}
		}

		if( strDataParamFile != null )
			fDataparamterFile= new File( strDataParamFile );
		
		BufferedReader br= null;
	    try {
	    	HashMap<String, String> hmData= TestEditorWindow._createHTML( fDataparamterFile, false );
	    	
			br= new BufferedReader(new FileReader(strTemplateFile));
	        String line = br.readLine();

	        while( line != null ) {
	            if( line.equals( "<!-- Insert Header -->" ) )
	            	sb.append( this._HeaderGenerator("Data Parameter Editor") );
	            else if( line.contains( "<title>" ))
	            	line= "	<title>" + (strDataParamFile != null ? fDataparamterFile.getName() : "Untitled") + "</title>";
	            else if( line.contains( "id=\"DataparamTitle\"" ))
	            	line= line.replace( "><", ">" + (strDataParamFile != null ? fDataparamterFile.getName() : "Untitled") + "<");
	            else if( line.contains( "id=\"Author\"" ))
	            	line= "<td id=\"Author\">" + hmData.get("Author") + "</td>";
	            else if( line.contains( "id=\"Description\"" ))
	            	line= "<td id=\"Description\">" + hmData.get("Description") + "</td>";
	            
	            sb.append( line+"\n" );

	            if( line.contains( "id=\"ParametersTable\"" ))
	            	sb.append( hmData.get("Table") );

	            line = br.readLine();
	        }
	    } catch( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
	        try {
	        	if( br != null )
	        		br.close();
			} catch( IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    			
		return sb.toString();
	}

	/**
	 * ex.
	 * http://tskotz-mac-wifi:8080/AutoManager/Contents?Platforms/BreakTweaker
	 * 
	 * @param strRequestQuery
	 * @return
	 */
	private String _getDirContents( String strRequestQuery, boolean bDirs ) {
		
		StringBuilder sb = new StringBuilder();
        File fDir= new File( DatabaseMgr._Preferences()._GetPref( Preferences.DataparamsRootDir ) + "/" + strRequestQuery );
        
        if( fDir.exists() ) {
	    	for( File fFile : fDir.listFiles() )
	    		if( bDirs && fFile.isDirectory() )
	    			sb.append( fFile.getName() + "\n" );
	    		else if( !bDirs && fFile.isFile() && !fFile.getName().equals( ".DS_Store" ))
	    			sb.append( fFile.getName() + "\n" );
        }
	    else
	    	sb.append( "Dir not found!" );
    	
		return sb.toString();
	}

	/**
	 * 
	 * @param fDir
	 * @param sb
	 * @param jobNum
	 */
	private void _InsertJobInfo( File fDir, StringBuilder sb, int jobNum ) {
		JobData jobData= null;
		String strJobID= fDir.getName();
		for( File f : fDir.listFiles() ) {
			if( f.getName().endsWith( ".job.xml") ) {
				try {
					jobData= new JobData( f );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
		
		if( jobData != null ) {
			String strJobEditorLink= "<a href=\"" + this.mstrWebServerURL + "/AutoManager/JobEditor?loadtemplate=" + jobData.m_strJobTemplate + "\">" + jobData.m_strJobTemplate + "</a>";
			sb.append( "<tr class=d" + (jobNum % 2) + ">\n" );
			sb.append( "<td><input type=\"checkbox\"></td>\n" );
			sb.append( "<td><table style=\"width:100%; border:0px\">\n" );
			sb.append( "	<tr><td jobid=\"" + strJobID + "\">Job: " + strJobEditorLink + "<br>User: " + jobData.m_strUser + "<br>" +
							"Platforms: " + jobData.m_strPlatforms.toString().replace("[", "").replace("]", "") + "</td></tr>" );
			sb.append( "</table></td>" );
			sb.append( "<td>" );
			// Find the resultslog.html files
			for( JobData.DataparamFileInfo dpinfo : jobData.m_fTests ) {
				System.out.println(dpinfo._GetFile().getAbsolutePath());
				File fResDir= new File( fDir.getAbsolutePath() + "/" + JobRunner.GetResultsDirFromTest( dpinfo._GetFile(), dpinfo._GetTestbed() ) );
				File[] aResFiles= fResDir.listFiles( new FilenameFilter() {
				    public boolean accept(File dir, String name) {
				        return name.toLowerCase().endsWith("_resultlog.html");
				    }
				});
				
				sb.append( "<input type=\"checkbox\">" );
				if( aResFiles != null && aResFiles.length > 0 )
					sb.append( "<a style=\"color:black;\" href=\"GetResultData?" + aResFiles[0].getAbsolutePath() + "\">" + dpinfo._GetFile().getName().replace(".xml", "") + "</a> (" + dpinfo._GetTestbedAndGroup() + ")<br>" );
				else
					sb.append( "<i>" + dpinfo._GetFile().getName().replace(".xml", "") + " (" + dpinfo._GetTestbedAndGroup() + ")</i><br>" );

			}
			sb.append( "</td></tr>" );
		}
	}
	
	/**
	 * ex.
	 * http://tskotz-mac-wifi:8080/AutoManager/SaveTestbed?name=New Group&value=127.0.0.1, 127.0.0.2&type=group
	 * 
	 * @param strRequestQuery
	 * @return
	 */
	private String _SaveTestbed( String strRequestQuery ) {
		String[] astrTestbedInfo= new String[5]; // name, value, info, runMode, description
		
		for( String strParam : strRequestQuery.split( "&" ) ) {
			//System.out.println( strParam );
			String[] aTestbedInfo= strParam.split( "=" );
			if( aTestbedInfo.length == 2 ) {
				if( aTestbedInfo[0].equals( "name" ))
					astrTestbedInfo[0]= aTestbedInfo[1];
				else if( aTestbedInfo[0].equals( "value" ))
					astrTestbedInfo[1]= aTestbedInfo[1];
				else if( aTestbedInfo[0].equals( "type" ))
					astrTestbedInfo[2]= aTestbedInfo[1];
				else if( aTestbedInfo[0].equals( "runmode" ))
					astrTestbedInfo[3]= aTestbedInfo[1];
				else if( aTestbedInfo[0].equals( "descr" ))
					astrTestbedInfo[4]= aTestbedInfo[1];
			}
		}
		
		if( DatabaseMgr._Testbeds()._AddTestbed( astrTestbedInfo[0], astrTestbedInfo[1], astrTestbedInfo[2], astrTestbedInfo[3], astrTestbedInfo[4]) )
			return STATUS_SUCCESS;
        
		return STATUS_FAILED;
	}
	
	/**
	 * 
	 * @param strRequestQuery
	 * @return
	 */
	private String _GetTestbedValue( String strRequestQuery ) {
		return DatabaseMgr._Testbeds()._GetTestbedDescriptor( strRequestQuery )._AsBasic();
   	}

	/**
	 * ex.
	 * http://tskotz-mac-wifi:8080/AutoManager/Contents?Platforms/BreakTweaker
	 * 
	 * @param strRequestQuery
	 * @return
	 */
	private String _UpdateTestbed( String strRequestQuery ) {
		String[] astrTestbedInfo= new String[6]; // curname, newname, value, info, runMode, description
		
		for( String strParam : strRequestQuery.split( "&" ) ) {
			//System.out.println( strParam );
			String[] aTestbedInfo= strParam.split( "=" );
			if( aTestbedInfo.length == 2 ) {
				if( aTestbedInfo[0].equals( "oldname" ))
					astrTestbedInfo[0]= aTestbedInfo[1];
				else if( aTestbedInfo[0].equals( "name" ))
					astrTestbedInfo[1]= aTestbedInfo[1];
				else if( aTestbedInfo[0].equals( "value" ))
					astrTestbedInfo[2]= aTestbedInfo[1];
				else if( aTestbedInfo[0].equals( "type" ))
					astrTestbedInfo[3]= aTestbedInfo[1];
				else if( aTestbedInfo[0].equals( "runmode" ))
					astrTestbedInfo[4]= aTestbedInfo[1];
				else if( aTestbedInfo[0].equals( "descr" ))
					astrTestbedInfo[5]= aTestbedInfo[1];
			}
		}
		
		if( DatabaseMgr._Testbeds()._UpdateTestbed( astrTestbedInfo[0], astrTestbedInfo[1], astrTestbedInfo[2], astrTestbedInfo[3], astrTestbedInfo[4], astrTestbedInfo[5]) )
			return STATUS_SUCCESS;
        
		return STATUS_FAILED;
	}

	/**
	 * ex.
	 * http://tskotz-mac-wifi:8080/AutoManager/Contents?Platforms/BreakTweaker
	 * 
	 * @param strRequestQuery
	 * @return
	 */
	private String _DeleteTestbed( String strRequestQuery ) {
		String strTestbedName= "";
		
		for( String strParam : strRequestQuery.split( "&" ) ) {
			//System.out.println( strParam );
			String[] aTestbedInfo= strParam.split( "=" );
			if( aTestbedInfo.length == 2 ) {
				if( aTestbedInfo[0].equals( "name" ))
					strTestbedName= aTestbedInfo[1];
			}
		}
		
		if( DatabaseMgr._Testbeds()._DeleteTestbed( strTestbedName ) )
			return STATUS_SUCCESS;
        
		return STATUS_FAILED;
	}

	/**
	 * ex.
	 * http://tskotz-mac-wifi:8080/AutoManager/GetJobInfo?user=xx&job=yyyy
	 * 
	 * @param strRequestQuery
	 * @return
	 */
	private String _DeleteJob( String strRequestQuery ) {
		String strUser= "";
		String strJobName= "";
		
		for( String strParam : strRequestQuery.split( "&" ) ) {
			//System.out.println( strParam );
			String[] aTestbedInfo= strParam.split( "=" );
			if( aTestbedInfo.length == 2 ) {
				if( aTestbedInfo[0].equals( "user" ))
					strUser= aTestbedInfo[1];
				else if( aTestbedInfo[0].equals( "job" ))
					strJobName= aTestbedInfo[1];
			}
		}
				
		return DatabaseMgr._Jobs()._DeleteJob( strJobName, strUser );
	}

	/**
	 * ex.
	 * http://tskotz-mac-wifi:8080/AutoManager/GetJobInfo?user=xx&job=yyyy
	 * 
	 * @param strRequestQuery
	 * @return
	 */
	private String _GetJobInfo( String strRequestQuery ) {
		String strUser= "";
		String strJobName= "";
		
		for( String strParam : strRequestQuery.split( "&" ) ) {
			//System.out.println( strParam );
			String[] aTestbedInfo= strParam.split( "=" );
			if( aTestbedInfo.length == 2 ) {
				if( aTestbedInfo[0].equals( "user" ))
					strUser= aTestbedInfo[1];
				else if( aTestbedInfo[0].equals( "job" ))
					strJobName= aTestbedInfo[1];
			}
		}
		
		JobDescriptor pJob= DatabaseMgr._Jobs()._GetJob( strJobName, strUser );
		
		if( pJob != null )
			return "Classpath=" + pJob.mstrClasspath + "&OptArgs=" + pJob.mstrOptArgs + "&Dataparams=" + pJob.mstrDataparams;
		else
        	return "Job \"" + strJobName + "\" for user \"" + strUser + "\" not found!";
	}

	/**
	 * ex.
	 * http://tskotz-mac-wifi:8080/AutoManager/GetJobInfo?user=xx&job=yyyy
	 * 
	 * @param strRequestQuery
	 * @return
	 */
	private String _GetUserJobs( String strRequestQuery ) {
		String strUser= "";
		
		for( String strParam : strRequestQuery.split( "&" ) ) {
			//System.out.println( strParam );
			String[] aTestbedInfo= strParam.split( "=" );
			if( aTestbedInfo.length == 2 ) {
				if( aTestbedInfo[0].equals( "user" ))
					strUser= aTestbedInfo[1];
			}
		}
		
		ArrayList<JobDescriptor> arrJobs= DatabaseMgr._Jobs()._GetJobs( strUser );
		
		String strData= "  <option value=\"--Select--\">--Select--</option>\n";
		for( JobDescriptor pJob : arrJobs )
			strData+= "<option value=\"" + pJob.mstrJobName + "\">" + pJob.mstrJobName + "</option>\n";
		
		return strData;
	}
	
	/**
	 * 
	 * @param strRequestQuery
	 * @return
	 */
	private String _GetTestbedDescriptor( String strRequestQuery ) {
		TestbedDescriptor pTBDescr= DatabaseMgr._Testbeds()._GetTestbedDescriptor( strRequestQuery );
		if( pTBDescr != null )
			return pTBDescr._AsREST();
		
		return ToolboxHTTPServer.STATUS_FAILED;		
	}

}