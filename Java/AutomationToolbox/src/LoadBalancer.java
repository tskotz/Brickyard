package AutomationToolbox.src;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;

public class LoadBalancer {

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
	public String _GetNumJobsRequest( HttpExchange exchange ) {
		if( !DatabaseMgr._Preferences()._GetPrefBool( Preferences.EnableJobLoadBalancing ) )
			return "Load Balancing is not enabled";
		
		String strCaller= exchange.getRequestHeaders().get( "Origin" ) != null ? exchange.getRequestHeaders().get( "Origin" ).get( 0 ).replace( "http://", "" ) 
																			   : exchange.getRequestHeaders().get( "Host" ).get( 0 );
		if( !DatabaseMgr._Preferences()._GetPref( Preferences.AllowJobRequestsFrom ).contains( strCaller ) )
			return exchange.getRequestHeaders().get( "Host" ).get( 0 ) + " is not authorized to Load Balance with this Toolbox";
			
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

		int iNumJobs= this._GetNumJobs();
		// If we aren't busy then don't try to distribute.  Run locally
		if( iNumJobs == 0 )
			return false;

		String strOrigin= null;
		// Look for the origin of this request 
		for( String strParam : strRequestQuery.split( "&" ) ) {
			String[] aElementInfo= strParam.split( "=" );
			if( aElementInfo.length == 2 )
				if( aElementInfo[0].equals( "origin" ))
					strOrigin= aElementInfo[1];
		}
		
		List<String> pOrigins= Arrays.asList( (strOrigin!=null ? strOrigin.split(";") : new String[]{} ));

		String strFarmToThisToolbox= null;
		String[] aFarm= DatabaseMgr._Preferences()._GetPref( Preferences.SendJobRequestsTo ).replace( " ", "" ).split( "," );
		// Let's find the toolbox with the least going on
		for( String strToolbox : aFarm ) {
			
			if( pOrigins.contains( strToolbox ) ) {
				System.out.println( "Load Balancing is skipping " + strToolbox + " because it is one of the job rquest originators" );
				continue;
			}
			
			// See how many running and queued jobs it has
			String strReply= this._PostURL( "http://" + strToolbox + "/AutoManager/LB/GetNumJobs" );
			System.out.println( strToolbox + " " + strReply );
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
			
			String strThisOrigin= exchange.getRequestHeaders().get( "Origin" ) != null ? exchange.getRequestHeaders().get( "Origin" ).get( 0 ).replace( "http://", "" )
																					   : exchange.getRequestHeaders().get( "Host" ).get( 0 );
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
		} catch( Exception e ) {
			e.printStackTrace();
			strResponse= e.getMessage();
		}
		
		return strResponse;
	}
}
