package AutomationToolbox.src;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class Sandbox {

	/**
	 * @param args
	 */
	public static void main( String[] args ) {
		
		List<String> pBosses= Arrays.asList( "aaaaa, bbbbb, ccccc".replace( " ", "" ).split( "," ) );
		boolean b= pBosses.contains( "a" );
		b= pBosses.contains( "aa" );
		b= pBosses.contains( "aaaaaa" );
		
		System.out.println( pBosses.toString().replace( "[", "" ));

		
		// TODO Auto-generated method stub
		String strURL= "http://macmini.local:8000/AutoManager/LB/GetNumJobs";
		strURL= "http://outside.local:8380/AutoManager/LB/GetNumJobs";
	    URL xurl;
	    HttpURLConnection connection = null;  
	      //Create connection
	      try {
			xurl = new URL(strURL);
		      connection = (HttpURLConnection)xurl.openConnection();
		      connection.setRequestMethod("GET");
		      connection.setRequestProperty("Content-Type", 
		           "text/plain");

		      connection.setRequestProperty("Content-Language", "en-US");  
		      connection.setRequestProperty("Origin", "12345");  
		      connection.setRequestProperty("Test", "aaaaaaaaaaa");  

		      connection.setUseCaches (false);
		      connection.setDoInput(true);
		      connection.setDoOutput(true);
		      
		      int status = connection.getResponseCode();
		      System.out.println( "status:=" + status );

		}
		catch( MalformedURLException e1 ) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
