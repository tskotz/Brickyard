package iZomateCore.AppCore.HostApps;

import java.awt.Point;
import java.awt.event.KeyEvent;

import iZomateCore.AppCore.*;
import iZomateCore.AppCore.AppEnums.Images;
import iZomateCore.AppCore.AppEnums.TrackFormat;
import iZomateCore.AppCore.AppEnums.TrackType;
import iZomateCore.TestCore.Testbed;
import iZomateCore.UtilityCore.TimeUtils;

public class ProTools11 extends HostApp {
	private int 	m_CmdCtrlKey= KeyEvent.VK_META; // default Mac
	private double	m_KeyTypePause= 0; // default Mac
	
	/**
	 * 
	 * @param strHostApp
	 * @param pTestbed
	 * @throws Exception
	 */
	public ProTools11( String strHostApp, Testbed pTestbed ) throws Exception {
		super( strHostApp, pTestbed);
		
		if( this._Testbed()._RemoteServer()._SysInfo()._isWin() ) {
			this.m_CmdCtrlKey= KeyEvent.VK_CONTROL;
			this.m_KeyTypePause= 0.25;
		}
	}
	
	/**
	 * 
	 */
	public void _createNewTrack( TrackFormat trackFormat, TrackType trackType ) throws Exception {
		// Open the "New Tracks" window
		int nThrottle= this._Testbed()._RemoteServer()._GetProcessRequestThrottle();
		this._Testbed()._RemoteServer()._SetProcessRequestThrottle( 50 );
		try {
			this._Testbed()._RemoteServer()._Robot()._keyTypePause( .5, this.m_CmdCtrlKey, KeyEvent.VK_SHIFT, KeyEvent.VK_N );
			
			// Track Format
			if( trackFormat._getMenuPos() > 1 ) {
				if( this._Testbed()._RemoteServer()._SysInfo()._isWin() ) {
					this._Testbed()._RemoteServer()._Robot()._imageClick( Images.Host_ProTools11_TrackFormatPopupMenu, 1, 1, true );
					TimeUtils.sleep( .25 );
					for( int i= 0; i < trackFormat._getMenuPos(); i++ )
						this._Testbed()._RemoteServer()._Robot()._keyType( KeyEvent.VK_DOWN );
					this._Testbed()._RemoteServer()._Robot()._keyType(KeyEvent.VK_ENTER);
					TimeUtils.sleep( .25 );
				}
				else { //Mac
					for( int i= 0; i < trackFormat._getMenuPos()-1; i++ )
						this._Testbed()._RemoteServer()._Robot()._keyType( this.m_CmdCtrlKey, KeyEvent.VK_DOWN );
				}
			}
	
			// Track Type
			if( trackType._getMenuPos() > 1 ) {
				if( this._Testbed()._RemoteServer()._SysInfo()._isWin() ) {
					this._Testbed()._RemoteServer()._Robot()._imageClick( Images.Host_ProTools11_TrackTypePopupMenu, 1, 1, true );
					TimeUtils.sleep( .25 );
					for( int i= 0; i < trackType._getMenuPos(); i++ )
						this._Testbed()._RemoteServer()._Robot()._keyType( KeyEvent.VK_DOWN );
					this._Testbed()._RemoteServer()._Robot()._keyType(KeyEvent.VK_ENTER);
					TimeUtils.sleep( .25 );
				}
				else { // Mac
					for( int i= 0; i < trackType._getMenuPos()-1; i++ )
						this._Testbed()._RemoteServer()._Robot()._keyType( this.m_CmdCtrlKey, KeyEvent.VK_DOWN );
				}
			}
	
			this._Testbed()._RemoteServer()._Robot()._keyType(KeyEvent.VK_ENTER);
			this._Testbed()._RemoteServer()._Robot()._mouseMove(1, 1);
		}
		finally {
			this._Testbed()._RemoteServer()._SetProcessRequestThrottle( nThrottle );
		}
	}
			
	/**
	 * 
	 */
	public void _removeTrack() throws Exception {
		if( this._Testbed()._RemoteServer()._SysInfo()._isMac() ) {
			this._Testbed()._RemoteServer()._Robot()._imageClick(Images.Host_ProTools11_MenubarTrack, 1);
			this._Testbed()._RemoteServer()._Robot()._keyType("Delete"); // Select Delete Menu Bar item
			this._Testbed()._RemoteServer()._Robot()._keyType( KeyEvent.VK_ENTER);
		}
		else {
			this._Testbed()._RemoteServer()._Robot()._keyType( KeyEvent.VK_ALT ); // Jump to Menu Bar		
			this._Testbed()._RemoteServer()._Robot()._keyType("T"); // Expand Track Menu
			this._Testbed()._RemoteServer()._Robot()._keyType("DD"); // Select Delete Menu Bar item by typing 2 D's
		}
		this._Testbed()._RemoteServer()._Robot()._keyType( KeyEvent.VK_ENTER);
		this._Testbed()._RemoteServer()._Robot()._imageClick(Images.Host_ProTools11_DeleteButton, 1, 2); //Track "xxx" contains active clips.  Do you still want to delete it? dialog
	}

	/**
	 * 
	 */
	public void _importAudioFile(String audioFile) throws Exception {
		this._Testbed()._RemoteServer()._Robot()._keyTypePause( this.m_KeyTypePause, this.m_CmdCtrlKey, KeyEvent.VK_SHIFT, KeyEvent.VK_I);
		
		if( this._Testbed()._RemoteServer()._SysInfo()._isMac() ) {
			this._Testbed()._RemoteServer()._Robot()._keyType("/");
			this._Testbed()._RemoteServer()._Robot()._keyType(audioFile);
			this._Testbed()._RemoteServer()._Robot()._keyType(KeyEvent.VK_ENTER);
			TimeUtils.sleep(.5);
			this._Testbed()._RemoteServer()._Robot()._keyType(KeyEvent.VK_ENTER);
			TimeUtils.sleep(.5);
			this._Testbed()._RemoteServer()._Robot()._imageClick("C:/AutoImages/HostAPI/PT11OSX/ImportAudioDone.bmp", 1);
			this._Testbed()._RemoteServer()._Robot()._keyType(KeyEvent.VK_ENTER);
			TimeUtils.sleep(5);
			this._Testbed()._RemoteServer()._Robot()._keyType(KeyEvent.VK_ENTER);
		}
		else {
			// Reduce the throttle and tab to the directory combo box
			int nThrottle= this._Testbed()._RemoteServer()._GetProcessRequestThrottle();
			this._Testbed()._RemoteServer()._SetProcessRequestThrottle( 30 );
			for( int i=0; i < 16; i++ )
				this._Testbed()._RemoteServer()._Robot()._keyType( KeyEvent.VK_TAB );

			TimeUtils.sleep( .5 );
			this._Testbed()._RemoteServer()._Robot()._keyType( "Computer" );
			// Tab to the directory panel
			for( int i=0; i < 2; i++ )
				this._Testbed()._RemoteServer()._Robot()._keyType( KeyEvent.VK_TAB );

			this._Testbed()._RemoteServer()._SetProcessRequestThrottle( nThrottle );
			
			//Type each part of the file path and hit enter in between
			for( String s : audioFile.split( "/" ) ) {
				if( s.contains( ":" ) )
					this._Testbed()._RemoteServer()._Robot()._keyType( "LocalDisk (" + s );
				else	
					this._Testbed()._RemoteServer()._Robot()._keyType( s );
				
				this._Testbed()._RemoteServer()._Robot()._keyType( KeyEvent.VK_ENTER);
			}
				
			TimeUtils.sleep( .5 );
			this._Testbed()._RemoteServer()._Robot()._keyType( KeyEvent.VK_ALT, KeyEvent.VK_E ); // Done button
			TimeUtils.sleep( 1 );
			this._Testbed()._RemoteServer()._Robot()._keyType( KeyEvent.VK_ALT, KeyEvent.VK_C ); // Use Current Location button
			TimeUtils.sleep( 3 );
			this._Testbed()._RemoteServer()._Robot()._imageClick( Images.Host_ProTools11_OKButtonHighlighted._For( "Win7" ), 1, 60, true );
		}
	}
	
	public boolean _isPlaying() throws Exception {		
		return this._Testbed()._RemoteServer()._Robot()._imageClick( Images.Host_ProTools11_TransportPlaying, 0, 0, false ) != null;		
	}
	
	public void _togglePlay() throws Exception {
		this._Testbed()._RemoteServer()._Robot()._keyType( KeyEvent.VK_SPACE );
	}
	
	public void _instantiatePlugin( String pluginName, String pluginCategory ) throws Exception {
		int nOrigFuzz= 	this._Testbed()._RemoteServer()._Robot()._GetImageClickFuzzFactor();
		String strOS= this._Testbed()._RemoteServer()._SysInfo()._GetOSName();
		this._Testbed()._RemoteServer()._Robot()._imageClick( Images.Host_ProTools11_Inserts._For( strOS ) , 1, new Point(5,21) );
		
		if( this._Testbed()._RemoteServer()._SysInfo()._isMac() ) {
			this._Testbed()._RemoteServer()._Robot()._keyType("multich");
			this._Testbed()._RemoteServer()._Robot()._keyType( KeyEvent.VK_ENTER );
			this._Testbed()._RemoteServer()._Robot()._keyType(pluginCategory);
			this._Testbed()._RemoteServer()._Robot()._keyType( KeyEvent.VK_ENTER );
			this._Testbed()._RemoteServer()._Robot()._keyType(pluginName);
			this._Testbed()._RemoteServer()._Robot()._keyType( KeyEvent.VK_ENTER );
		}
		else {
			// UUGGHH!
			// Windows only responds to first letter in menu selection.  If you type the whole word then the selection will change for every key type that matches the first word in a selection.
			// If multiple selections tart with same letter then each time that letter is pressed the selection will switch to next item in the list that starts with that letter.
			this._Testbed()._RemoteServer()._Robot()._keyType("m"); // One m for multichannel plug-in
			this._Testbed()._RemoteServer()._Robot()._keyType( KeyEvent.VK_ENTER );
			int loop= 1;
			
			// Special cases Categories
			if( pluginCategory.equals( "Delay" ) )
				loop=2;
			else if( pluginCategory.equals( "Dither" ) )
				loop=3;
			
			for( int i= 0; i<loop; i++ )
				this._Testbed()._RemoteServer()._Robot()._keyType( pluginCategory.charAt( 0 ) );
			
			// Only press enter if this is a category with multiple entries that begin with same letter 
			if( pluginCategory.charAt( 0 ) == 'D' )
				this._Testbed()._RemoteServer()._Robot()._keyType( KeyEvent.VK_ENTER );				
	
			//BLAAAGGHH We have to use images because of the keyboard typing issue mentioned above!
			if( null == this._Testbed()._RemoteServer()._Robot()._imageClick( Images.DUMMY._GetEnumByFileName( pluginName )._For( strOS ), 1, 2, false ) )		
				this._Testbed()._RemoteServer()._Robot()._imageClick( Images.DUMMY._GetEnumByFileName( pluginName + " Highlighted" )._For( strOS ), 1, 2, true );		
		}
		
		Images pPluginGraphic= null;
		if( pluginName.contains( "BreakTweaker" ) )
			pPluginGraphic= Images.Host_ProTools11_BreakTweakerPluginGraphic;
		else if( pluginName.contains( "Ozone" ) )
			pPluginGraphic= Images.Host_ProTools11_Ozone5PluginGraphic;
		else
			throw new Exception( "No Plugin Graphic defined for plugin:" + pluginName );
			
		// Wait for the Plugin window to pop up
		this._Testbed()._RemoteServer()._Robot()._imageClick( pPluginGraphic, 0, 60, true );
		this._Testbed()._RemoteServer()._Robot()._SetImageClickFuzzFactor( nOrigFuzz );
	}
	
	public void _uninstantiatePlugin() throws Exception {
		String strOS= this._Testbed()._RemoteServer()._SysInfo()._GetOSName();
		if( this._Testbed()._RemoteServer()._Robot()._imageClick( Images.Host_ProTools11_PluginExists._For( strOS ), 1, new Point(0,20) ) != null ||
		    this._Testbed()._RemoteServer()._Robot()._imageClick( Images.Host_ProTools11_PluginExistsHighlighted._For( strOS ), 1, new Point(0,20) ) != null ) {
			if( this._Testbed()._RemoteServer()._SysInfo()._isMac() ) {
				this._Testbed()._RemoteServer()._Robot()._keyType( "no insert" );
				this._Testbed()._RemoteServer()._Robot()._keyType( KeyEvent.VK_ENTER );
			}
			else
				this._Testbed()._RemoteServer()._Robot()._keyType( "no" );
		}
	}
	
	public void _hidePluginUI() throws Exception {
		String strOS= this._Testbed()._RemoteServer()._SysInfo()._GetOSName();
		if( this._Testbed()._RemoteServer()._SysInfo()._isMac() ) {
			if( this._Testbed()._RemoteServer()._Robot()._imageClick( Images.Host_ProTools11_PluginWindowClose._For( strOS ), 1, new Point(5,6)  ) == null )
				this._Testbed()._RemoteServer()._Robot()._imageClick( Images.Host_ProTools11_PluginWindowCloseRed, 1, new Point(5,5) );
		}
		else
			this._Testbed()._RemoteServer()._Robot()._imageClick( Images.Host_ProTools11_InsertWindowTopLeft, 1, new Point(1130,-14) );
	}
	
	public void _showPluginUI() throws Exception {
		String strOS= this._Testbed()._RemoteServer()._SysInfo()._GetOSName();
		if( this._Testbed()._RemoteServer()._Robot()._imageClick( Images.Host_ProTools11_PluginExists._For( strOS ), 1, new Point(20,20) ) == null )
		    this._Testbed()._RemoteServer()._Robot()._imageClick( Images.Host_ProTools11_PluginExistsHighlighted._For( strOS ), 1, new Point(0,20) );
	}
	
	public void _newSession(String sessionName, String sampleRate, String bitDepth) throws Exception {
		this._Testbed()._RemoteServer()._Robot()._keyTypePause( this.m_KeyTypePause, this.m_CmdCtrlKey, KeyEvent.VK_N); // New Session Shortcut
		
		this._Testbed()._RemoteServer()._Robot()._keyType(KeyEvent.VK_ENTER); // This is where session setup parameters will happen
		
		this._Testbed()._RemoteServer()._Robot()._keyType("/"); //Custom Directory
		
		this._Testbed()._RemoteServer()._Robot()._keyType("session name"); // Name of the session
		
		// Click the Save button
		
		// Dismiss the OverwriteDialog if it appears
	}
	
	public void _closeSession() throws Exception {
		this._Testbed()._RemoteServer()._Robot()._keyTypePause( this.m_KeyTypePause, this.m_CmdCtrlKey, KeyEvent.VK_SHIFT, KeyEvent.VK_W);  // Close Session Shortcut		
		//Dismiss "Save Settings" Dialog if it appears
	}
	
	public void _saveSession() throws Exception {
		this._Testbed()._RemoteServer()._Robot()._keyTypePause( this.m_KeyTypePause, this.m_CmdCtrlKey, KeyEvent.VK_S);
	}

}
