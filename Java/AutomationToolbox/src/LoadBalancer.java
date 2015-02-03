package AutomationToolbox.src;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;

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
		
		String strCaller= exchange.getRequestHeaders().get( "Host" ).get( 0 );
		String strAllow= DatabaseMgr._Preferences()._GetPref( Preferences.AllowJobRequestsFrom );
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
	public boolean _Distribute( String strRequestQuery ) {
		boolean bDistributed= false;

		if( strRequestQuery == null )
			return bDistributed;
		
		if( !DatabaseMgr._Preferences()._GetPrefBool( Preferences.EnableJobLoadBalancing ) )
			return bDistributed;

		int iNumJobs= this._GetNumJobs();
		// If we aren't busy then don't try to distribute.  Run locally
		if( iNumJobs == 0 )
			return bDistributed;
		
		String strFarmToThisToolbox= null;
		String[] aFarm= DatabaseMgr._Preferences()._GetPref( Preferences.SendJobRequestsTo ).replace( " ", "" ).split( "," );
		// Let's find the toolbox with the least going on
		for( String strToolbox : aFarm ) {
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
		
		if( strFarmToThisToolbox != null ) {
			System.out.println( "Farming job out to: " + strFarmToThisToolbox );
			String strStatus= this._PostURL( "http://" + strFarmToThisToolbox + "/AutoManager/RunLoadBalancedJob?" + strRequestQuery );
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
