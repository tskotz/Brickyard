package iZomateCore.iZTests.RX;

import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.NotificationCallback;

//! Callback to get information when a plug-in finishes loading
public class RXPluginLoadCallback extends NotificationCallback {

	private String m_strPluginName;
	private int m_nLoadMillis;
	
	public RXPluginLoadCallback() throws Exception {
		this.m_pNotificationType= NotificationType.EVNT;
		this.m_strTitle= EventSubType.PluginLoaded.getValue();		
	}

	@Override
	public CBStatus _Callback( String title, String id, String message, String[] buttons, int value ) throws Exception {
		if( id.equals( EventSubType.PluginLoaded.getValue() ) ) {
			this.m_strPluginName= message;
			this.m_nLoadMillis= value;
			return CBStatus.HANDLED;
		}
		return CBStatus.NOTHANDLED;
	}
	
	public String _GetPluginName() throws Exception {
		return this.m_strPluginName;
	}
	
	public int _GetLoadMillis() throws Exception {
		return this.m_nLoadMillis;
	}
}
