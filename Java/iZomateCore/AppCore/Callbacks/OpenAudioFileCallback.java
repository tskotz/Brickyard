package iZomateCore.AppCore.Callbacks;

import iZomateCore.AppCore.AppEnums.HostType;
import iZomateCore.AppCore.HostApp;
import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.NotificationCallback;
import iZomateCore.UtilityCore.TimeUtils;

/**
 * 
 * @author tskotz
 *
 */
public class OpenAudioFileCallback extends NotificationCallback {
	private String 	m_strFilename;
	private HostApp	m_pHostApp;

	public OpenAudioFileCallback( String strFilename, HostApp pHostApp ) {
		this.m_pHostApp= pHostApp;
		this.m_strFilename= strFilename;
		
		// Override only the values that we care about
		this.m_pNotificationType= 	NotificationType.DLOG;
		String strName= this.m_pHostApp._HostType()._GetInternalName();
		this.m_strTitle= strName.equals( HostType.RX3._GetInternalName() ) || strName.equals( HostType.RX4._GetInternalName() ) ? 
						 "Select audio files" : "Select an audio file";
	}

	@Override
	public CBStatus _Callback( String title, String id, String message, String[] buttons, int value ) throws Exception {
		TimeUtils.sleep( 1.5 );
		this.m_pHostApp._Testbed()._Robot()._keyType( this.m_strFilename + "\n" );
		String strName= this.m_pHostApp._HostType()._GetInternalName();
		if( strName.equals( HostType.RX3._GetInternalName() ) || strName.equals( HostType.RX4._GetInternalName() ) ) {
			// In RX, wait for the specific opened file event (which comes after scanning if we scan)
			this.m_pHostApp._GetAppServer()._waitForEvent( EventSubType.OpenedFile, null, null, 60 );
		} else {
			this.m_pHostApp._GetAppServer()._waitForEvent( EventSubType.DLOGDismissed, null, null, 10 );
		}
		return CBStatus.HANDLED;
	}

}

