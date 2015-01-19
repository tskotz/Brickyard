package iZomateCore.AppCore;

import iZomateCore.AppCore.AppEnums.HostType;
import iZomateCore.AppCore.NotificationCallbacks.DLOGNotificationCallback;
import iZomateCore.LogCore.Logs;
import iZomateCore.ServerCore.RPCServer.IncomingReply;
import iZomateCore.ServerCore.RPCServer.OutgoingRequest;
import iZomateCore.ServerCore.RPCServer.RPCServer;
import iZomateCore.TestCore.TestCaseParameters;
import iZomateCore.UtilityCore.GenericTable;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Plugin {
	protected HostApp				m_pHostApp;
	protected List<String>			m_strPresets= new LinkedList<String>();
	protected int					mPresetChangeCount= 0;
	protected ArrayList<Double> 	mPresetLoadTimes= new ArrayList<Double>();
	public ArrayList<ArrayList<Double>> mProcessApplyTimes= new ArrayList<ArrayList<Double>>();
	public ArrayList<ArrayList<Double>> mProcessTrainTimes= new ArrayList<ArrayList<Double>>();
	
	private PluginActions			m_pPluginActions= null;
	private	Console					m_pConsole= null;
	private	Profilers				m_pProfilers= null;
	private	PluginInfo				m_pPluginInfo= null;
	private	WindowControls			m_pWindowControls= null;
	private RPCServer				m_pPluginServer= null;
	private	String					m_strPluginName= null;
	
	private double 					m_dInstMin= -1, m_dInstMax= -1, m_dInstAvg= -1;
	private double 					m_dBuildMin= -1, m_dBuildMax= -1, m_dBuildAvg= -1;
	
	
	public Plugin( String strPlugin, HostApp pHostApp ) throws Exception {
		this.m_pHostApp= pHostApp;
		this.m_strPluginName= strPlugin;
		this._GetPluginServer(); //This will log the plugin info
	}
	
	public RPCServer _GetPluginServer() throws Exception {
		if( this.m_pPluginServer == null ) {
			// Instantiate Plugin RPC Server
			if( HostType.getEnumFromPlugin( this.m_strPluginName )._HasPluginRPCServer() )
				this.m_pPluginServer= new RPCServer( this.m_pHostApp._Testbed()._GetMachineName(), "PLG:"+this.m_strPluginName, this.m_pHostApp._Logs() );
			else
				this.m_pPluginServer= this.m_pHostApp._GetAppServer();
			
			this.m_pPluginServer._connectToClient(); //TODO:  Figure why we block to socket if we don't do this.  we should not have to do this.  Only seems to happen on plugins

			// Register Custom Notification Callback objects
			this.m_pPluginServer._RegisterNotificationCallback( new DLOGNotificationCallback( this.m_pHostApp ) );
			this._Logs()._ResultLog()._LogPluginInfo( this._GetPluginInfo() );
		}
		return this.m_pPluginServer;
	}
		
	public PluginActions _Actions() {
		if( this.m_pPluginActions == null )
			this.m_pPluginActions= new PluginActions( this );
		return this.m_pPluginActions;
	}
	
	public Console _Console() {
		if( this.m_pConsole == null )
			this.m_pConsole= new Console( this );
		return this.m_pConsole;
	}
	
	public Profilers _Profilers() {
		if( this.m_pProfilers == null )
			this.m_pProfilers= new Profilers( this );
		return this.m_pProfilers;
	}
	
	public WindowControls _Controls() throws Exception {
		if( this.m_pWindowControls == null )
			this.m_pWindowControls= new WindowControls( this._GetPluginServer(), this.m_pHostApp._Testbed() );
		return this.m_pWindowControls;
	}
	
	public Logs _Logs() throws Exception {
		return this.m_pHostApp._Logs();
	}
	
	public PluginInfo _GetPluginInfo() throws Exception {
		if( this.m_pPluginInfo == null )
			this.m_pPluginInfo= new PluginInfo( this._GetPluginServer() );
		return this.m_pPluginInfo;
	}
	
	public void _CreateGraphs( TestCaseParameters pParams ) throws IOException, Exception {
		String strPluginProfiler= this._Console()._Perf_List().length>0 ? this._Console()._Perf_List()[0] : null;
		int graphWidth= 560;
		int graphHeight = 300;
		
		if( pParams._GetDataDir() == null )
			this._Logs()._ResultLog()._logMessage( "Skipping graphs because Output dir was not specified" );
		else {
			// Graph Test Case Stats
			this._Logs()._ResultLog()._logString( "</pre>\n<center><h2>" + this._GetPluginInfo().m_strShortName + " (b" + this._GetPluginInfo().m_nBuildNumber + ")" + " Test Case Statistics</h2></center><hr>\n" );
			
			if( strPluginProfiler != null ) {
				GenericTable profilerTable= new GenericTable( strPluginProfiler + " Profiler", "Test Case", "CPU %", new String[]{"Min", "Max", "Avg"} )._SetXAxisStartVal( 1 )
																																						._SetDataFromArray( this._Profilers()._Get( strPluginProfiler ), false );		
				this._Logs()._ResultLog()._logImage( profilerTable._CreateImageFile( this._generateNameStem( pParams._GetLogDir(), "prof" + strPluginProfiler ) ).getName(), graphWidth, graphHeight );	
			}
			
			this._LogPresetMemoryGraph( pParams._GetLogDir(), graphWidth, graphHeight );
			this._LogPresetLoadGraph( pParams._GetLogDir(), graphWidth, graphHeight );
			this._LogProcessTrainGraph( pParams._GetLogDir(), graphWidth, graphHeight );
			this._LogProcessApplyGraph( pParams._GetLogDir(), graphWidth, graphHeight );

			// Graph Test Case Profilers
			this.m_pProfilers._CreateGraphs( this._generateNameStem( pParams._GetLogDir(), "prof" ), graphWidth, graphHeight );

			// Graph Plugin Build History Stats
			this._Logs()._ResultLog()._logString( "<br><center><h2>" + this._GetPluginInfo().m_strShortName + " History Statistics</h2></center><hr>\n" );

			if( strPluginProfiler != null ) {
				String strHistoryFile= this._generateNameStem( pParams._GetDataDir(), "PlgProf.txt" );
				GenericTable buildProfilerTable= new GenericTable( "Plugin Profiler History", "Build Number", "CPU %", new String[]{"Min", "Max", "Avg"} );				
				this._Logs()._ResultLog()._logImage( buildProfilerTable._SetXAxisStartVal( this._GetPluginInfo().m_nBuildNumber )
																					 ._SetData( this._Profilers()._GetOverallPerformanceStats(), strHistoryFile, true )
																					 ._CreateImageFile( this._generateNameStem( pParams._GetLogDir(), "_PlgProf" ) ).getName(), graphWidth, graphHeight );
			}
			
			this._LogBuildMemoryGraph( pParams, graphWidth, graphHeight );
			this._LogBuildPresetLoadGraph( pParams, graphWidth, graphHeight );
			this._LogBuildDSPInstantiationGraph( pParams, graphWidth, graphHeight );
			this._LogBuildUIInstantiationGraph( pParams, graphWidth, graphHeight );

			if( pParams._GetBuildTime() != -1 ) {
				String strBuildTimesFile= this._generateNameStem( pParams._GetDataDir(), "BuildTimes.txt" );
				GenericTable buildTimeTable= new GenericTable( "Build Times History", "Build Number", "Minutes", new String[]{"Build Times"} );		
				this._Logs()._ResultLog()._logImage( buildTimeTable._SetXAxisStartVal( this._GetPluginInfo().m_nBuildNumber )
																			 ._SetData( new double[]{pParams._GetBuildTime()} , strBuildTimesFile, true )
																			 ._CreateImageFile( this._generateNameStem( pParams._GetLogDir(), "BuildTimes" ) ).getName(), graphWidth, graphHeight );
			}	
		}
	}

	/**
	 * 
	 * @param lMemoryOffset
	 * @param strOutputDir
	 * @param width
	 * @param height
	 * @throws Exception
	 */
	public void _LogPresetMemoryGraph( String strOutputDir, int width, int height ) throws Exception {
		this._GetMemoryMinMaxAvg();
		
		double minYRange= this.m_pHostApp._GetStartMemory()<this.m_dBuildMin?this.m_pHostApp._GetStartMemory():this.m_dBuildMin;
		GenericTable memoryRunTable= new GenericTable( "Test Case Memory Usage", "Test Case", "MB", new String[]{"Preset Memory"} )._SetXAxisStartVal( 0 )
																																   ._SetYAxisRange( minYRange, this.m_dBuildMax )
																																   ._SetData( this.m_pHostApp.m_pMemoryArray, true );				
		this._Logs()._ResultLog()._logImage( memoryRunTable._CreateImageFile( this._generateNameStem( this._Logs()._GetLogsDir(), "PresetMem" ) ).getName(), width, height );		
	}
	
	/**
	 * 
	 * @param strOutputDir
	 * @param width
	 * @param height
	 * @throws Exception
	 */
	public void _LogProcessApplyGraph( String strOutputDir, int width, int height ) throws Exception {
		if( !this.mProcessApplyTimes.isEmpty() ) {
			GenericTable instTable= new GenericTable( "Apply Times", "Test Case", "ms", new String[]{"Fail Thrsh", "Warn Thrsh", "Apply Times"} )._SetXAxisStartVal( 1 )
																														  ._SetLineColors( Color.RED, Color.YELLOW, Color.BLUE )
																														  ._SetDataFromArray( this.mProcessApplyTimes, true );		
			this._Logs()._ResultLog()._logImage( instTable._CreateImageFile( this._generateNameStem( this._Logs()._GetLogsDir(), "ProcessApply" ) ).getName(), width, height );
		}		
	}	
	
	/**
	 * 
	 * @param strOutputDir
	 * @param width
	 * @param height
	 * @throws Exception
	 */
	public void _LogProcessTrainGraph( String strOutputDir, int width, int height ) throws Exception {
		if( !this.mProcessTrainTimes.isEmpty() ) {
			GenericTable instTable= new GenericTable( "Train Times", "Test Case", "ms", new String[]{"Fail Thrsh", "Warn Thrsh", "Train Times"} )._SetXAxisStartVal( 1 )
																														  ._SetLineColors( Color.RED, Color.YELLOW, Color.BLUE )
																														  ._SetDataFromArray( this.mProcessTrainTimes, true );		
			this._Logs()._ResultLog()._logImage( instTable._CreateImageFile( this._generateNameStem( this._Logs()._GetLogsDir(), "ProcessTrain" ) ).getName(), width, height );
		}
	}

	/**
	 * 
	 * @param strOutputDir
	 * @param width
	 * @param height
	 * @throws Exception
	 */
	public void _LogPresetLoadGraph( String strOutputDir, int width, int height ) throws Exception {
		GenericTable instTable= new GenericTable( "Preset Load Times", "Test Case", "ms", new String[]{"Load Times"} )._SetXAxisStartVal( 1 )
																													  ._SetData( this.mPresetLoadTimes, true );		
		this._Logs()._ResultLog()._logImage( instTable._CreateImageFile( this._generateNameStem( this._Logs()._GetLogsDir(), "PresetLoad" ) ).getName(), width, height );
	}

	/**
	 * 
	 * @param strOutputDir
	 * @param width
	 * @param height
	 * @throws Exception
	 */
	public void _LogBuildMemoryGraph( TestCaseParameters pParams, int width, int height ) throws Exception {
		this._GetMemoryMinMaxAvg();
		String strHistoryFile= this._generateNameStem( pParams._GetDataDir(), "MemUsage.txt" );
		GenericTable memoryHistoryTable= new GenericTable(  "Plugin Memory Usage History", "Build Number", "MB", new String[]{"Min", "Max", "Avg"} );				
		this._Logs()._ResultLog()._logImage( memoryHistoryTable._SetXAxisStartVal( this._GetPluginInfo().m_nBuildNumber )
																		  ._SetData( new double[]{this.m_dBuildMin, this.m_dBuildMax, this.m_dBuildAvg}, strHistoryFile, true )
																		  ._CreateImageFile( this._generateNameStem( pParams._GetLogDir(), "MemUsage" ) ).getName(), width, height );
	}
	
	/**
	 * 
	 * @param strOutputDir
	 * @param width
	 * @param height
	 * @throws Exception
	 */
	public void _LogBuildPresetLoadGraph( TestCaseParameters pParams, int width, int height ) throws Exception {
		this._GetMinMaxAvg( this.mPresetLoadTimes );	
		String strHistoryFile= this._generateNameStem( pParams._GetDataDir(), "PresetLoadTimes.txt" );
		GenericTable instTable= new GenericTable(  "Plugin Preset Load Times History", "Build Number", "ms", new String[]{"Min", "Max", "Avg"} );				
		this._Logs()._ResultLog()._logImage( instTable._SetXAxisStartVal( this._GetPluginInfo().m_nBuildNumber )
																		  ._SetData( new double[]{this.m_dInstMin, this.m_dInstMax, this.m_dInstAvg}, strHistoryFile, true )
																		  ._CreateImageFile( this._generateNameStem( pParams._GetLogDir(), "PresetLoadTimes" ) ).getName(), width, height );
	}

	/**
	 * 
	 * @param strOutputDir
	 * @param width
	 * @param height
	 * @throws Exception
	 */
	public void _LogBuildDSPInstantiationGraph( TestCaseParameters pParams, int width, int height ) throws Exception {
		String strHistoryFile= this._generateNameStem( pParams._GetDataDir(), "DSPInstTimes.txt" );
		GenericTable instTable= new GenericTable(  "Plugin DSP Instantiation Times History", "Build Number", "ms", new String[]{"DSP Instantiation Time"} );				
		this._Logs()._ResultLog()._logImage( instTable._SetXAxisStartVal( this._GetPluginInfo().m_nBuildNumber )
																		  ._SetData( new double[]{this._GetPluginInfo()._GetDSPInstantionTime()}, strHistoryFile, true )
																		  ._CreateImageFile( this._generateNameStem( pParams._GetLogDir(), "DSPInstTimes" ) ).getName(), width, height );
	}
	
	/**
	 * 
	 * @param strOutputDir
	 * @param width
	 * @param height
	 * @throws Exception
	 */
	public void _LogBuildUIInstantiationGraph( TestCaseParameters pParams, int width, int height ) throws Exception {
		String strHistoryFile= this._generateNameStem( pParams._GetDataDir(), "UIInstTimes.txt" );
		GenericTable instTable= new GenericTable(  "Plugin UI Instantiation Times History", "Build Number", "ms", new String[]{"UI Instantiation Time"} );				
		this._Logs()._ResultLog()._logImage( instTable._SetXAxisStartVal( this._GetPluginInfo().m_nBuildNumber )
																		  ._SetData( new double[]{this._GetPluginInfo()._GetUIInstantionTime()}, strHistoryFile, true )
																		  ._CreateImageFile( this._generateNameStem( pParams._GetLogDir(), "UIInstTimes" ) ).getName(), width, height );
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void _GetMemoryMinMaxAvg() throws Exception {
		if( this.m_dBuildAvg == -1 ) {
			// Prepend the initial memory so we can see the delta for the 1st preset in the graph
			this.m_pHostApp.m_pMemoryArray.add( 0, (double) this.m_pHostApp._GetStartMemory() );

			double dTotal= 0;
			// Find min/max/avg of presets only so skip index 0
			for( int i= 1; i < this.m_pHostApp.m_pMemoryArray.size(); i++ ) {
				double dVal= this.m_pHostApp.m_pMemoryArray.get( i ) - this.m_pHostApp._GetAudioMemoryOverhead();
				// Update table with the memory offset.  (Audio file overhead)
				this.m_pHostApp.m_pMemoryArray.set( i, dVal );
				dTotal+= dVal;
				if( dVal < this.m_dBuildMin || this.m_dBuildMin == -1 )
					this.m_dBuildMin= dVal;
				if( dVal > this.m_dBuildMax || this.m_dBuildMax == -1 )
					this.m_dBuildMax= dVal;
			}
			if( this.m_pHostApp.m_pMemoryArray.size() > 1 )
				this.m_dBuildAvg= dTotal/(this.m_pHostApp.m_pMemoryArray.size()-1); // -1 to skip start memory stuffed into index 0
			else
				this.m_dBuildMin= this.m_dBuildMax= this.m_dBuildAvg= this.m_pHostApp.m_pMemoryArray.get( 0 );
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void _GetMinMaxAvg( ArrayList<Double> dataArray ) throws Exception {
		if( this.m_dInstAvg == -1 ) {
			double dTotal= 0;
			for( int i= 0; i < dataArray.size(); i++ ) {
				double dVal= dataArray.get( i );
				dTotal+= dVal;
				if( dVal < this.m_dInstMin || this.m_dInstMin == -1 )
					this.m_dInstMin= dVal;
				if( dVal > this.m_dInstMax || this.m_dInstMax == -1 )
					this.m_dInstMax= dVal;
			}
			if( dataArray.size() > 0 )
				this.m_dInstAvg= dTotal/dataArray.size();
			else
				this.m_dInstMin= this.m_dInstMax= this.m_dInstAvg= 0;
		}
	}
	
	/**
	 * 
	 * @param strOutputDir
	 * @param strFileSuffix
	 * @return
	 */
	private String _generateNameStem( String strOutputDir, String strFileSuffix ) {
		return strOutputDir + "/" + this.m_pHostApp._Testbed()._GetMachineName() + "_" + this.m_pHostApp._HostType()._GetInternalName() + "_" + this.m_strPluginName + "_" + strFileSuffix;
	}

	/**
	 * 
	 * @param strRequest
	 * @return
	 * @throws Exception
	 */
	public IncomingReply _pluginAutomationRequest( String strRequest ) throws Exception {
		OutgoingRequest req= this.m_pPluginServer._createRequest( "pluginAutomationRequest" );
		req._addString( strRequest, "req" );
		return this.m_pPluginServer._processRequest( req );
	}

}
