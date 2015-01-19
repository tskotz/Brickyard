package iZomateCore.AppCore.Callbacks;

import iZomateCore.AppCore.AppEnums.SAHookMenuBar;
import iZomateCore.AppCore.AppEnums.WindowControls.Buttons;
import iZomateCore.AppCore.AppEnums.WindowControls.TextEdits;
import iZomateCore.AppCore.HostApp;
import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.NotificationCallback;

public class RXMarkersAndRegionsWindowCallback extends NotificationCallback{
	private HostApp m_pHostApp;
	private String 	m_strMarker;
	
	public RXMarkersAndRegionsWindowCallback( String strMarker, HostApp pHostApp ) {
		this.m_strMarker= 	strMarker;
		this.m_pHostApp= 	pHostApp;
		
		// Override only the values that we care about
		this.m_pNotificationType= 	NotificationType.WNDW;
		this.m_strTitle= 			"Markers and Regions";
		this.m_strMessage= 			"Show";
	}

	@Override
	public CBStatus _Callback( String title, String id, String message, String[] buttons, int value ) throws Exception {
		int i= 0;
		try {
			while( !this.m_strMarker.equals( this.m_pHostApp._Plugin()._Controls()._TextEdit( TextEdits.MarkerRowNameEdit._getValue().replace( "#", "" + ++i ) )._info().mText ) ) {;}
			this.m_pHostApp._Plugin()._Controls()._Button( Buttons.RX_MarkerRowFindButton._getValue().replace( "#", ""+i ) )._click();
		} catch( Exception e ) {
			throw new Exception( "The specified Marker/Region '" + this.m_strMarker + "' was not found" );
		} finally { // Always close the window
			this.m_pHostApp._Actions()._SAHookMenuBar( SAHookMenuBar.RX_View_Markers_And_Regions );
		}
		return CBStatus.HANDLED;
	}

}
