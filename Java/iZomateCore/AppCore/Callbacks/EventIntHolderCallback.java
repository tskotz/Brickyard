package iZomateCore.AppCore.Callbacks;

import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.NotificationCallback;
import org.omg.CORBA.IntHolder;

//! Simple callback for any event
public class EventIntHolderCallback extends NotificationCallback {

	public EventIntHolderCallback( EventSubType type ) throws Exception {
		this.m_pNotificationType= NotificationType.EVNT;
		this.m_strID= type.getValue();		
	}

	@Override
	public CBStatus _Callback( String title, String id, String message, String[] buttons, int value ) throws Exception {
		if( id != null && id.equals( this.m_strID ) ) {
			//this.m_strTitle = title;
			//this.m_strMessage = message;
			this.m_pIntHolder = new IntHolder( value );
			
			return CBStatus.HANDLED;			
		} 
		else
			return CBStatus.NOTHANDLED;
	}
}
