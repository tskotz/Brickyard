package iZomateCore.AppCore;

import iZomateCore.UtilityCore.GenericTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Profilers {
	private Plugin		m_pPlugin;
	private Map<String, ArrayList<ArrayList<Double>>> m_mProfilersMap = new LinkedHashMap<String, ArrayList<ArrayList<Double>>>();

	public static int			MIN= 0, MAX= 1, AVG= 2;

	
	public Profilers( Plugin pPlugin ) {
		this.m_pPlugin= pPlugin;
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void _LogToResultsFile() throws Exception {
		if( this.m_pPlugin._Console()._Perf_List().length > 0 ) {
			this.m_pPlugin._Console()._Perf_PauseLogging( true );
			String strDivID= this.m_pPlugin.m_pHostApp._Logs()._ResultLog()._logDivStart( "Profiler Data", false );
			for( String strProfiler : this.m_pPlugin._Console()._Perf_List() )
				this._logProfilerData( strProfiler );
			this.m_pPlugin.m_pHostApp._Logs()._ResultLog()._logDivEnd( strDivID );
			this.m_pPlugin._Console()._Perf_ClearLog();
		}
	}
	
	/**
	 * 
	 * @param strProfiler
	 * @throws Exception
	 */
	private void _logProfilerData( String strProfiler ) throws Exception {
		this.m_pPlugin.m_pHostApp._Logs()._ResultLog()._logString( "\t\t" );
		this.m_pPlugin.m_pHostApp._Logs()._ResultLog()._logDivData( "<h5>" + this._getStats( strProfiler ) + "</h5>", strProfiler + " Profiler Log Data", false );
	}

	/**
	 * 
	 * @param profilerData
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private String _getStats( String strProfiler ) throws Exception {
		String profilerData= this.m_pPlugin._Console()._Perf_GetLog( strProfiler );

		String str;
		int count= 0;
		double total= 0;
		double min= -1, max= -1;
		boolean bStart= false;
		BufferedReader reader = new BufferedReader( new StringReader( profilerData ) );
		while ((str = reader.readLine()) != null) {
			if( bStart ) {
				double val= Double.valueOf( str.split( "\t" )[3] );
				if( min == -1 ) {
					min= val;
					max= val;				
				}
				else if( val < min )
					min= val;
				else if( val > max )
					max= val;
				total+= val;
				count++;
			}
			else if ( str.contains( "% CPU" ) )
				bStart= true;
		}
		
		ArrayList<ArrayList<Double>> profilerArray= this.m_mProfilersMap.get( strProfiler );
		if( profilerArray == null ) {
			profilerArray= new ArrayList<ArrayList<Double>>();
			profilerArray.add( new ArrayList<Double>() ); // Min
			profilerArray.add( new ArrayList<Double>() ); // Max
			profilerArray.add( new ArrayList<Double>() ); // Avg
			this.m_mProfilersMap.put( strProfiler, profilerArray );
		}
		
		profilerArray.get( MIN ).add( min );
		profilerArray.get( MAX ).add( max );
		profilerArray.get( AVG ).add( total/count );
		
		return profilerData;
	}
	
	/**
	 * 
	 * @param strProfiler
	 * @return
	 */
	public ArrayList<ArrayList<Double>> _Get( String strProfiler ) {
		return this.m_mProfilersMap.get( strProfiler );
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	public double[] _GetOverallPerformanceStats() throws Exception {
		ArrayList<ArrayList<Double>> profilerArray= this.m_mProfilersMap.get( this.m_pPlugin._Console()._Perf_List()[0] );
		
		if( profilerArray == null )
			return null;

		double min= profilerArray.get( MIN ).get( 0 );
		double max= profilerArray.get( MAX ).get( 0 );
		double avgTotal=0;

		for( int i= 1; i< profilerArray.get( MIN ).size(); i++ )
			if( profilerArray.get( MIN ).get( i ) < min )
				min= profilerArray.get( MIN ).get( i );

		for( int i= 1; i< profilerArray.get( MAX ).size(); i++ )
			if( profilerArray.get( MAX ).get( i ) > max )
				max= profilerArray.get( MAX ).get( i );

		for( int i= 0; i< profilerArray.get( AVG ).size(); i++ )
			avgTotal+= profilerArray.get( AVG ).get( i );
				
		return new double[]{ min, max, avgTotal/profilerArray.get( AVG ).size() };
	}


	/**
	 * 
	 * @throws Exception
	 */
	public void _CreateGraphs( String strOutputFileStem, int nGraphWidth, int nGraphHeight ) throws Exception {
		// Graph Test Case Profilers
		String strDivID= this.m_pPlugin._Logs()._ResultLog()._logDivStart( "<center><h2>Test Case Profilers</h2></center>", false );

		this.m_pPlugin._Logs()._ResultLog()._logString( "<hr>\n" );
		
		boolean bTCProfilersDetected= false; 
		for( String s : this.m_mProfilersMap.keySet() ) {
			if( !s.equals( "Plugin" )) {
				GenericTable table= new GenericTable( s + " Profiler", "Test Case", "CPU %", new String[]{"Min", "Max", "Avg"} )._SetXAxisStartVal( 1 )._SetDataFromArray( this.m_mProfilersMap.get( s ), false );		
				this.m_pPlugin._Logs()._ResultLog()._logImage( table._CreateImageFile( strOutputFileStem + s ).getName(), nGraphWidth, nGraphHeight );
				bTCProfilersDetected= true;
			}
		}
		
		if( !bTCProfilersDetected )
			this.m_pPlugin._Logs()._ResultLog()._logLine( "<H3 align=\"center\">No Test Case Profiler Data to Report</H3>" );
		
		this.m_pPlugin._Logs()._ResultLog()._logDivEnd( strDivID );

	}

}
