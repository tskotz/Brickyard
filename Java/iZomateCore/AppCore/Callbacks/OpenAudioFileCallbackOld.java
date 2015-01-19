package iZomateCore.AppCore.Callbacks;

import iZomateCore.AppCore.HostApp;
import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.NotificationCallback;
import iZomateCore.UtilityCore.TimeUtils;

public class OpenAudioFileCallbackOld extends NotificationCallback {
	private String 	m_strFilename;
	private HostApp	m_pHostApp;

	public OpenAudioFileCallbackOld( String strFilename, HostApp pHostApp ) {
		this.m_pHostApp= pHostApp;
		this.m_strFilename= strFilename;
		
		// Override only the values that we care about
		this.m_pNotificationType= 	NotificationType.EVNT;
		this.m_strID= 		"MenuInvoke";
		this.m_strMessage= 	"Invoking";	
	}

	@Override
	public CBStatus _Callback( String title, String id, String message, String[] buttons, int value ) throws Exception {
		TimeUtils.sleep( .6 );
		this.m_pHostApp._Testbed()._Robot()._keyType( this.m_strFilename + "\n" );
		return CBStatus.HANDLED;
	}
}
