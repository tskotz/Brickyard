package iZomateCore.iZTests.RX;

import iZomateCore.AppCore.AppEnums.RXModule;
import iZomateCore.AppCore.AppEnums.WindowControls.Buttons;
import iZomateCore.AppCore.WindowControls.ButtonState;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.TestCore.TestCaseParameters;
import iZomateCore.UtilityCore.TimeUtils;

import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.Vector;

//! General test of plug-in hosting in RX 3
public class RXPluginHostingTest extends RXAppTest {
	
	private Vector<Vector<Integer>> m_pluginMenuSizes;
	private String[] m_strTypes;
	
	protected RXPluginHostingTest( String[] args ) throws Exception {
		super( args );
	}
	
	//! Run the test
	public static void main(String[] args) throws Exception	{
		new RXPluginHostingTest( args ).run();
	}
	
	//! Called on startup
	@Override
	protected void _onStartUp(TestCaseParameters pCommonParameters) throws Exception {
		
		// Load an audio file
		String audioFile= pCommonParameters._GetString( "audioFile" );
		this._Testbed()._HostApp()._Actions()._UnloadAudioFile();
		this._Testbed()._HostApp()._Actions()._LoadAudioFile( audioFile, true, false );
		this._Logs()._ResultLog()._logMessage( "Loaded audio file " + audioFile + "." );
		
		// Open the plug-in module and get the number of plug-ins in its menu
		RXPluginMenuCallback callback= new RXPluginMenuCallback();
		
		// Open the plug-in module
		this._Testbed()._HostApp()._Plugin()._Controls()._Button( RXModule._getEnum( "Plug-In" )._getButtonID() )._setState( ButtonState.ON );
		
		TimeUtils.sleep( 0.5 );
		
		this._Testbed()._HostApp()._Plugin()._Controls()._Button( "EffectPanel Plug-In|Select Button" )._click( null, null ); // Click Select plug-in
		this._Testbed()._HostApp()._GetAppServer()._waitForEvent(EventSubType.PluginMenuConstructed, null, callback, 5); // Wait until the plug-in menu is constructed
		this._Testbed()._Robot()._keyType( KeyEvent.VK_ESCAPE );	// Dismiss the menu
		
		this.m_pluginMenuSizes= callback._GetPluginMenuSizes();
		String[] strTypesMac= {"AU","VST"};
		String[] strTypesWin= {"VST","DX"};
		this.m_strTypes= this._Testbed()._SysInfo()._isMac() ? strTypesMac : strTypesWin;
	}

	//! Run a test case - just load several random plug-ins one after another
	@Override
	protected void _TestCase(TestCaseParameters pTestcaseParameters) throws Exception {

		TimeUtils.sleep( 1.0 );
		
		int nSeed= pTestcaseParameters._GetInt("seed", 0);
		Random rand = new Random(nSeed);
		
		int nCurrentType= 0, nCurrentManufacturer= 0, nCurrentPlugin= 0;
		
		// If we're not loading randomly, just load the number of total plug-ins in the menu
		boolean bRandom= pTestcaseParameters._GetBool("random", false);
		int nPluginsToLoad= pTestcaseParameters._GetInt("pluginsToLoad", 10);	// Number of plug-ins we should load
		if( !bRandom ) {
			nPluginsToLoad= 0;
			for( int i= 0; i < this.m_pluginMenuSizes.size(); ++i ) {
				Vector<Integer> vec= this.m_pluginMenuSizes.elementAt(i);
				for( int j= 0; j < vec.size(); ++j ) {
					nPluginsToLoad+= vec.elementAt(j);
				}
			}
		}
		
		if( nPluginsToLoad > pTestcaseParameters._GetInt("pluginsToLoad", 10) )
			nPluginsToLoad= pTestcaseParameters._GetInt("pluginsToLoad", 10);
			
		boolean bProcess= pTestcaseParameters._GetBool("process", false);
		
		// Load plug-ins
		for( int i= 0; i < nPluginsToLoad; ++i ) {
			// Click 'Select plug-in'. Use a null event type because the context menu prevents us from getting a mouse LeftUp event.
			this._Testbed()._HostApp()._Plugin()._Controls()._Button( "EffectPanel Plug-In|Select Button" )._click( null, null ); 
			
			TimeUtils.sleep( 0.5 );
			
			this._Testbed()._Robot()._keyType( KeyEvent.VK_DOWN ); 		// Move down to get into the menu
			this._Testbed()._Robot()._keyType( KeyEvent.VK_DOWN ); 		// Move down to VST or AU
			int nType= bRandom ? rand.nextInt(2) : nCurrentType;
			// Choose a random type or the current one in order
			if( nType == 1 ) {
				this._Testbed()._Robot()._keyType( KeyEvent.VK_DOWN );	// Move down again to get to DX or VST if necessary
			}
			this._Testbed()._Robot()._keyType( KeyEvent.VK_RIGHT ); 	// Move right to get into the list of plug-in manufacturers for this type
			
			// Choose a random manufacturer or the current one in order
			int nManufacturers= this.m_pluginMenuSizes.elementAt(nType).size();
			int nManufacturer= bRandom ? rand.nextInt(nManufacturers) : nCurrentManufacturer;
			for( int j= 0; j < nManufacturer; ++j ) {
				this._Testbed()._Robot()._keyType( KeyEvent.VK_DOWN ); 	// Move down to the selected manufacturer
			}
			this._Testbed()._Robot()._keyType( KeyEvent.VK_RIGHT ); 	// Move right to get into the list of plug-ins of this type
			
			// Choose a random plug-in or the current one in order
			int nPlugins= this.m_pluginMenuSizes.elementAt(nType).elementAt(nManufacturer);
			int nPlugin= bRandom ? rand.nextInt(nPlugins) : nCurrentPlugin;
			for( int j= 0; j < nPlugin; ++j ) {
				this._Testbed()._Robot()._keyType( KeyEvent.VK_DOWN ); 	// Move down to the selected plug-in
			}
			this._Testbed()._Robot()._keyType( KeyEvent.VK_ENTER ); 	// Select this plug-in
			
			// Wait until the plug-in loads and log its name, type, and initialization time.
			RXPluginLoadCallback pluginCallback= new RXPluginLoadCallback();
			this._Testbed()._HostApp()._GetAppServer()._waitForEvent(EventSubType.PluginLoaded, null, pluginCallback, 5);
			this._Testbed()._ForceSocketShutdown( 54324 );
			this._Testbed()._ForceSocketShutdown( 54325 );
			String strName= pluginCallback._GetPluginName();
			int nLoadMillis= pluginCallback._GetLoadMillis();
			String strType= this.m_strTypes[nType];
			this._Logs()._ResultLog()._logMessage( "Loaded plug-in " + strName + " (" + strType + ") in " + nLoadMillis + " ms." );
			
			// Process if necessary
			if( bProcess ) {
				// Preview for a few seconds, this is good to test plus it clears the status panel, which is necessary
				_Controls()._Button( "EffectPanel Plug-In|" + Buttons.Module_PreviewButton._getValue() )._click();
				TimeUtils.sleep( 5.0 );
				// Process with this plug-in and log the time it takes.
				float fSeconds= _Process( pTestcaseParameters, "Plug-In", false );
				String strMessage= "    Processing took " + Float.toString( fSeconds ) + " seconds.";
				this._Logs()._ResultLog()._logData( strMessage );
				TimeUtils.sleep( 0.5 );
			}
			
			// Move to the next plug-in
			nCurrentPlugin++;
			if( nCurrentPlugin >= nPlugins ) {
				nCurrentPlugin= 0;
				nCurrentManufacturer++;
			}
			if( nCurrentManufacturer >= nManufacturers ) {
				nCurrentManufacturer= 0;
				nCurrentType++;
			}
		}
	}

	//! Gets called if an exception is caught by the Test base class while processing _TestCase
	@Override
	protected void _OnTestCaseException( TestCaseParameters pTestcaseParameters, Exception e ) throws Exception {		
		// Dismiss the crash reporter, submitting a crash report if we wanted to
		this._Testbed()._DismissCrashReporter( pTestcaseParameters._GetSubmitCrashReport() );
		this._Logs()._ResultLog()._logLine( "\n" );
	}

	//! Shut down, quitting the app if desired
	@Override
	protected void _onShutDown(TestCaseParameters pCommonParameters) throws Exception {
		// Close the plug-in module
		this._Testbed()._HostApp()._Plugin()._Controls()._Button( RXModule._getEnum( "Plug-In" )._getButtonID() )._setState( ButtonState.OFF );
	}
}
