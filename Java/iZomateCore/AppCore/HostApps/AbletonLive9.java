package iZomateCore.AppCore.HostApps;

import iZomateCore.AppCore.HostApp;
import iZomateCore.AppCore.AppEnums.TrackFormat;
import iZomateCore.AppCore.AppEnums.TrackType;
import iZomateCore.TestCore.Testbed;
import iZomateCore.UtilityCore.TimeUtils;
import iZomateCore.AppCore.AppEnums.Images;

import java.awt.Point;
import java.awt.event.KeyEvent;

public class AbletonLive9 extends HostApp {
	private boolean m_bClipSelected;
	private boolean m_bPlaying;
	
	// Ableton-specific setup steps:
	// 1. Add the folder containing our audio files to Ableton's "Places" via "Add Folder..."
	// 2. Goto Preferences -> Record Warp Launch and disable Auto-Warp Long Samples 
	public AbletonLive9(String strHostApp, Testbed pTestbed) throws Exception {
		super(strHostApp, pTestbed);
		this.m_bClipSelected= false;
		this.m_bPlaying= false;
		// TODO Auto-generated constructor stub
	}
	
	// Keep track of whether a clip is selected
	private void _setClipSelected( boolean bSelected ) { this.m_bClipSelected= bSelected; }
	@SuppressWarnings("unused")
	private boolean _isClipSelected() { return this.m_bClipSelected; }
	
	// Keep track of whether we are playing
	private void _setPlaying( boolean bPlaying ) { this.m_bPlaying= bPlaying; }
	
	// Searches for text in Ableton's search and selects the top-most result
	private void _searchText( String strText ) throws Exception {
		this._cmdCtrlKey( true );
		this._Testbed()._Robot()._keyPress( KeyEvent.VK_F );
		this._cmdCtrlKey( false );
		this._Testbed()._Robot()._keyRelease( KeyEvent.VK_F );
		
		this._Testbed()._Robot()._keyType( strText );
		TimeUtils.sleep( 0.5 );
		this._Testbed()._Robot()._keyType( KeyEvent.VK_ENTER );
	}
	
	// Toggles visibility of selected plugins
	private void _togglePluginVisibility() throws Exception {
		// Show/hide plugins
		this._cmdCtrlKey( true );
		this._Testbed()._Robot()._keyPress( KeyEvent.VK_ALT );
		this._Testbed()._Robot()._keyPress( KeyEvent.VK_P );
		this._cmdCtrlKey( false );
		this._Testbed()._Robot()._keyRelease( KeyEvent.VK_ALT );
		this._Testbed()._Robot()._keyRelease( KeyEvent.VK_P );
	}
	
	// Presses or releases CMD if mac or CTRL if win
	private void _cmdCtrlKey( boolean bPress ) throws Exception {
		// TODO: Add mac/win logic to determine keyCode
		int keyCode= KeyEvent.VK_META;
		if( bPress )
			this._Testbed()._Robot()._keyPress( keyCode );
		else
			this._Testbed()._Robot()._keyRelease( keyCode );
	}
	
	// Toggles visibility of plugins
	@Override
	public void _createNewTrack(TrackFormat trackFormat, TrackType trackType) throws Exception {
		if( trackType == TrackType.AudioTrack ) {
			this._cmdCtrlKey( true );
			this._Testbed()._Robot()._keyPress( KeyEvent.VK_T );
			this._cmdCtrlKey( false );
			this._Testbed()._Robot()._keyRelease( KeyEvent.VK_T );
		} else if( trackType == TrackType.MIDITrack ){
			this._cmdCtrlKey( true );
			this._Testbed()._Robot()._keyPress( KeyEvent.VK_SHIFT );
			this._Testbed()._Robot()._keyPress( KeyEvent.VK_T );
			this._cmdCtrlKey( false );
			this._Testbed()._Robot()._keyRelease( KeyEvent.VK_SHIFT );
			this._Testbed()._Robot()._keyRelease( KeyEvent.VK_T );
		}
	}

	@Override
	// This requires us to have \a audioFile's parent folder included in Live's
	// "Places" so we can access it via search.  Also, make sure Preferences ->
	// Auto-warp Long Samples is disabled as we do not want to warp by default.
	public void _importAudioFile(String audioFile) throws Exception {
		// Search for the filename in Ableton's search field
		this._searchText( audioFile );

		// Press enter to load the highlighted file on a new track
		this._Testbed()._Robot()._keyType( KeyEvent.VK_ENTER );

		// Now our clip is selected (highlighted)
		this._setClipSelected( true );
	}

	
	@Override
	public void _togglePlay() throws Exception {
		// If we're not playing, click on the play clip image (4 potential states to try)
		if( !this._isPlaying() ) {
			Point p= this._Testbed()._Robot()._imageClick(Images.Host_Live9_PlayClip2, 1);
			if( p == null )
				p= this._Testbed()._Robot()._imageClick(Images.Host_Live9_PlayClip2, 1);
			if( p == null )
				p= this._Testbed()._Robot()._imageClick(Images.Host_Live9_PlayClip3, 1);
			if( p == null )
				p= this._Testbed()._Robot()._imageClick(Images.Host_Live9_PlayClip4, 1);
			if( p != null )
				this._setPlaying( true );
		// If we are playing, then just press space bar
		} else {
			this._Testbed()._Robot()._keyType( KeyEvent.VK_SPACE );
			this._setPlaying( false );
		}
	}

	@Override
	public void _instantiatePlugin(String strPluginName, int insert) throws Exception {
		this._searchText( strPluginName );
		this._Testbed()._Robot()._keyType( KeyEvent.VK_ENTER );
		
		TimeUtils.sleep( 2.0 );
		
		this._Testbed()._Robot()._imageClick( Images.Auth_DemoButton, 1 );
		
		TimeUtils.sleep( 0.5 );
		
		// Hide the plugin so it's not in the way of play clip button, etc.
		this._togglePluginVisibility();
	}
	

	@Override
	public void _uninstantiatePlugin(int insertNumber) throws Exception {
		// Clicking this image will move selection to the empty area next to the plugin.
		// TODO: I am unsure how this will behave if multiple plugins are instantiated in a track
		Point p= this._Testbed()._Robot()._imageClick( Images.Host_Live9_PluginSelected, 1 );
		if( p == null )
			p= this._Testbed()._Robot()._imageClick( Images.Host_Live9_PluginUnselected, 1 );
		if( p == null )
			throw new Exception( "Robot couldn't find image used to select plugin for deletion!" );
		
		// Press <- to select plugin, then DELETE to delete it
		this._Testbed()._Robot()._keyType( KeyEvent.VK_LEFT );
		this._Testbed()._Robot()._keyType( KeyEvent.VK_DELETE );
	}
	
	@Override
	public boolean _isPlaying() { return this.m_bPlaying; }
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
