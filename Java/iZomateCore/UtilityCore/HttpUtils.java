package iZomateCore.UtilityCore;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


public class HttpUtils {
	public static String CHARSET= "UTF-8";
	
	/*
	 * Helper to make a query string for a url. ie param1=val1&param2=val2&param3=val3
	 */

	public static String CreateQuery( String[] names, String[] values ) {
        try {
            if( names.length != values.length ) throw new Exception("Unequal length of names and values");

            String query = "";
            for( int i=0; i<names.length; ++i) {
                query += URLEncoder.encode(names[i], CHARSET) + "=" + URLEncoder.encode(values[i], CHARSET);
                if(i<names.length-1) query += "&";
            }
		    return query;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
	
	/*
	 * GET a HTTP request and return the result
	 */
    // Note: removing exception for testing. might want to put it back in or clean it up
	public static String Get(String url, String[] names, String[] values) /* throws Exception*/  {
        String query = CreateQuery(names, values);

		URLConnection connection;
		try {
            System.out.println("==> HTTP::Get( " + url + "?" + query + " )");
			connection = new URL(url + "?" + query).openConnection();					
			connection.setRequestProperty("Accept-Charset", "UTF-8");
			 InputStream is = connection.getInputStream();
		      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		      String line;
		      StringBuffer response = new StringBuffer(); 
		      while((line = rd.readLine()) != null) {
		        response.append(line);
		        response.append('\r');
		      }
		      rd.close();
              System.out.println("<== " + response.toString() );
		      return response.toString();
		} catch ( IOException e1) {
			e1.printStackTrace();
			//throw new Exception("Get Failed.\n" + e1.getMessage() );
		}
        return "";
	}
	
	/*
	 * POST a HTTP request and return the result
	 */
	public static String Post(String targetURL, String urlParameters)
	  {
	    URL url;
	    HttpURLConnection connection = null;  
	    try {
	      //Create connection
	      url = new URL(targetURL);
	      connection = (HttpURLConnection)url.openConnection();
	      connection.setRequestMethod("POST");
	      connection.setRequestProperty("Content-Type", 
	           "application/x-www-form-urlencoded");
				
	      connection.setRequestProperty("Content-Length", "" + 
	               Integer.toString(urlParameters.getBytes().length));
	      connection.setRequestProperty("Content-Language", "en-US");  
				
	      connection.setUseCaches (false);
	      connection.setDoInput(true);
	      connection.setDoOutput(true);

	      //Send request
	      DataOutputStream wr = new DataOutputStream (
	                  connection.getOutputStream ());
	      wr.writeBytes (urlParameters);
	      wr.flush ();
	      wr.close ();

	      //Get Response	
	      InputStream is = connection.getInputStream();
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	      String line;
	      StringBuffer response = new StringBuffer(); 
	      while((line = rd.readLine()) != null) {
	        response.append(line);
	        response.append('\r');
	      }
	      rd.close();
	      return response.toString();

	    } catch (Exception e) {

	      e.printStackTrace();
	      return null;

	    } finally {

	      if(connection != null) {
	        connection.disconnect(); 
	      }
	    }
	  }
	
	
}