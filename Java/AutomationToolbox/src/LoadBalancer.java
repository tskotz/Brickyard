package AutomationToolbox.src;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;

public class LoadBalancer {
	private final String m_strFromToolbox= "fromtoolbox";

	/**
	 * @param args
	 */
	public static void main( String[] args ) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * 
	 */
	LoadBalancer() {
	}
	
	/**
	 * 
	 * @param strRequestQuery
	 * @return
	 */
	public String _GetNumJobsRequest( String strRequestQuery ) {
		if( !DatabaseMgr._Preferences()._GetPrefBool( Preferences.EnableJobLoadBalancing ) )
			return "Load Balancing is not enabled";
		
		String strFromToolbox= null;
		// Look for the name of the toolbox this request is from 
		for( String strParam : strRequestQuery.split( "&" ) ) {
			String[] aElementInfo= strParam.split( "=" );
			if( aElementInfo.length == 2 )
				if( aElementInfo[0].equals( this.m_strFromToolbox ))
					strFromToolbox= aElementInfo[1];
		}

		List<String> pBosses= Arrays.asList( DatabaseMgr._Preferences()._GetPref( Preferences.AllowJobRequestsFrom ).replace( " ", "" ).toLowerCase().split( "," ) );

		if( strFromToolbox == null )
			return "No Toolbox was specified";
		if( !pBosses.contains( strFromToolbox.toLowerCase() ) )
			return strFromToolbox + " is not authorized to Load Balance with this Toolbox";
			
		return "NumJobs:" + String.valueOf( this._GetNumJobs() );
	}

	/**
	 * 
	 * @param strRequestQuery
	 * @return
	 */
	public int _GetNumJobs() {
		int iNumJobs= 0;
		
		for( File stagingDir : new File[]{ToolboxWindow._RunningDir(), ToolboxWindow._QueuedDir()} ) {
			File[] fFiles= stagingDir.listFiles();
			
			for( File fItem : fFiles) {
				if( fItem.getName().equals( ".DS_Store" ))
					continue;  // ignore it
				else if( fItem.isDirectory() )
					++iNumJobs;
			}
		}

		return iNumJobs;
	}

	/**
	 * 
	 * @param strRequestQuery
	 * @return
	 */
	public boolean _Distribute( HttpExchange exchange ) {
		String strRequestQuery= exchange.getRequestURI().getQuery();

		if( strRequestQuery == null )
			return false;
		
		if( !DatabaseMgr._Preferences()._GetPrefBool( Preferences.EnableJobLoadBalancing ) )
			return false;

		System.out.println( "\nRunning Load Balancer:" );

		int iNumJobs= this._GetNumJobs();
		// If we aren't busy then don't try to distribute.  Run locally
		if( iNumJobs == 0 ) {
			System.out.println( "0 jobs running locally so deferring to local toolbox" );
			return false;
		}

		String strOrigin= null;
		// Look for the origin of this request  i.e  origin=192.168.1.1:8080,192.168.1.5:8321
		for( String strParam : strRequestQuery.split( "&" ) ) {
			String[] aElementInfo= strParam.split( "=" );
			if( aElementInfo.length == 2 )
				if( aElementInfo[0].equals( "origin" ))
					strOrigin= aElementInfo[1];
		}

		List<String> pOrigins= Arrays.asList( (strOrigin!=null ? strOrigin.toLowerCase().split(";") : new String[]{} ));
		String strThisOrigin= exchange.getRequestHeaders().get( "Host" ).get( 0 );

		// Let's find the toolbox with the least going on
		String strFarmToThisToolbox= null;
		String[] aFarm= DatabaseMgr._Preferences()._GetPref( Preferences.SendJobRequestsTo ).replace( " ", "" ).split( "," );
		for( String strToolbox : aFarm ) {
			
			if( pOrigins.contains( strToolbox.toLowerCase() ) ) {
				System.out.println( "Load Balancing is skipping " + strToolbox + " because it is one of the job rquest originators" );
				continue;
			}
			
			// See how many running and queued jobs it has
			String strReply= this._PostURL( "http://" + strToolbox + "/AutoManager/LB/GetNumJobs?" + this.m_strFromToolbox + "=" + strThisOrigin );
			System.out.println( strToolbox + ": " + strReply );
			if( strReply.startsWith( "NumJobs:" ) ) {
				int iRemoteNumJobs= Integer.valueOf( strReply.replace( "NumJobs:", "" ) );
				if( iRemoteNumJobs < iNumJobs ) {
					strFarmToThisToolbox= strToolbox;
					iNumJobs= iRemoteNumJobs;
				}
			}
		}
		
		boolean bDistributed= false;

		if( strFarmToThisToolbox != null ) {
			System.out.println( "Farming job out to: " + strFarmToThisToolbox );
			
			// To prevent Load Balancing feedback loops we need to set the request's origin hierarchy
			if( strOrigin == null )
				strRequestQuery+= "&origin=" + strThisOrigin;
			else	
				strRequestQuery.replace( "&origin=" + strOrigin, "&origin=" + strOrigin + ";" + strThisOrigin );

			String strStatus= this._PostURL( "http://" + strFarmToThisToolbox + "/AutoManager/RunJob?" + strRequestQuery );
			if( strStatus == ToolboxHTTPServer.STATUS_SUCCESS )
				bDistributed= true;
			else
				System.out.println( strStatus );	
		}
		else
			System.out.println( "deferring to local toolbox" );
			
		return bDistributed;
	}
	
	/**
	 * 
	 * @param strURL
	 * @return
	 */
	private String _PostURL( String strURL ) {
		String strResponse= "";
		
		try {
			URL url= new URL( strURL );
			BufferedReader br= new BufferedReader( new InputStreamReader( url.openStream() ) );
			String strBuff;
			while( null != (strBuff= br.readLine()) )
				strResponse+= strBuff;
		} catch( java.net.UnknownHostException e ) {
			strResponse= "Unknown host: " + e.getMessage();
		} catch( Exception e ) {
			if( !e.getMessage().equals( "Connection refused" ) )
				e.printStackTrace();
			strResponse= e.getMessage();
		}
		
		return strResponse;
	}
}
