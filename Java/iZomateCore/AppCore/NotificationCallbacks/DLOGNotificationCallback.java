package iZomateCore.AppCore.NotificationCallbacks;

import iZomateCore.AppCore.HostApp;
import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.NotificationCallback;
import iZomateCore.UtilityCore.TimeUtils;

import java.awt.event.KeyEvent;

public class DLOGNotificationCallback extends NotificationCallback {
	private HostApp	m_pHostApp= null;
	public String 	m_strException= null;
	public String 	m_strWarning= null;
	public String 	m_strLastDLOG= "";

	/**
	 * Constructor.
	 *
	 * @param ptApp the Pro Tools application
	 */
	public DLOGNotificationCallback( HostApp pHostApp ) {
		this.m_pHostApp = pHostApp;
		
		// Override only the values that we care about
		this.m_pNotificationType= NotificationType.DLOG;
	}
	
	@Override
	public CBStatus _Callback( String title, String id, String message, String[] buttons, int value ) throws Exception {
		this.m_strException= null;
		this.m_strWarning= null;
		this.m_strLastDLOG= "Title: " + title + " MSG: " + message;
		CBStatus cbStatus=  CBStatus.HANDLED;
		int keyCode= 0;
		
		if( title.equals( "Profiler Not Found" ) ) {
			keyCode= KeyEvent.VK_ENTER;
			this.m_strException= message;
		}
		else if( message.equals( "The Mastering Imager plug-in is functioning in Demo mode. It will output silence until it is authorized." )) {
			keyCode= KeyEvent.VK_ENTER;
			this.m_strWarning= "Encountered '" + title + "' dialog.  Message: " + message;
		}
		else if( message.startsWith( "Offline processing failed" ) || 
				 message.startsWith( "An error occurred while setting the selected preset." ) ) {
			keyCode= KeyEvent.VK_ENTER;
			this.m_strException= "Encountered '" + title + "' dialog.  Message: " + message;
		}
		else if( title.equals( "Session Recovery" ) ||
				(message.startsWith( "The current file " ) && message.endsWith( "has not been saved.\nDo you wish to save changes?" ) ) ) {
			if( this.m_pHostApp._Testbed()._SysInfo()._isMac() ) {
				keyCode= KeyEvent.VK_SPACE; // Until mac gets a hot key we have to press space.  This assumes the Keyboard Shortcuts are enabled in Access system prefs
			}
			else
				keyCode= KeyEvent.VK_N;
			this.m_strWarning= "Encountered '" + title + "' dialog.  Message: " + message;
		}
		else if( message.contains( "has no results to report" ) ) {
			keyCode= KeyEvent.VK_ENTER;
			this.m_strWarning= "Encountered '" + title + "' dialog.  Message: " + message;
		}
		else if( message.contains( "An error occurred while opening the selected file." ) ) {
			keyCode= KeyEvent.VK_ENTER;
			this.m_strException= "Encountered '" + title + "' dialog.  Message: " + message;
		} else if( title.equals( "Save File As") ) {
			// Do nothing on Save As, we handle it specifically in cases when it appears
		} else if( message.contains( "Are you sure you want to overwrite it?" ) ) {
			keyCode= KeyEvent.VK_ENTER;
		} else {
			this.m_strException= "Unexpected dialog: " + (message.isEmpty()?title:message);
		}
			
		if( this.m_strException != null || this.m_strWarning != null ) {
			TimeUtils.sleep( 0.3 ); // sleep so that the ui has time to draw
			this.m_pHostApp._Logs()._ResultLog()._logScreenShot();
		}

		if( keyCode > 0 ) {
			this.m_pHostApp._Testbed()._Robot()._keyType( keyCode );
			// Make sure nobody else is waiting for this event before waiting for it here
			if( !this.m_pHostApp._GetAppServer()._isWaitForEventActive( EventSubType.DLOGDismissed.getValue(), message ) )
				this.m_pHostApp._GetAppServer()._waitForEvent( EventSubType.DLOGDismissed, message, null, 5 );
		}
			
		if( this.m_strWarning != null )
			this.m_pHostApp._Logs()._ResultLog()._logWarning( this.m_strWarning );

		if( this.m_strException != null )
			throw new Exception( this.m_strException );
		
		return cbStatus;
	}

}
