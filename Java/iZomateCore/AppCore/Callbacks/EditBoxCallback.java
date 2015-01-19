package iZomateCore.AppCore.Callbacks;

import iZomateCore.AppCore.HostApp;
import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.NotificationCallback;

public class EditBoxCallback extends NotificationCallback {
	private	HostApp m_pHostApp;
	private String 	m_strText;
	
	public EditBoxCallback( String strTextToEnter, HostApp pHostApp ) {
		this.m_pHostApp= pHostApp;
		this.m_strText=  strTextToEnter;

		// Override only the values that we care about
		this.m_pNotificationType= 	NotificationType.EVNT;
		this.m_strID= 				EventSubType.EditTextEvent.getValue();
		this.m_strMessage=			"InLineEdit";
	}

	@Override
	public CBStatus _Callback( String title, String id, String message, String[] buttons, int value ) throws Exception {
		this.m_pHostApp._Testbed()._Robot()._keyType( this.m_strText );
		return CBStatus.HANDLED;
	}
}
