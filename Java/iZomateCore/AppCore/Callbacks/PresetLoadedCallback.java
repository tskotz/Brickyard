package iZomateCore.AppCore.Callbacks;

import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.NotificationCallback;

public class PresetLoadedCallback extends NotificationCallback {
	private String m_strPreset;
	public	String m_strErrMsg= null;
	
	public PresetLoadedCallback( String strPreset )  {
		// If partial path then get file name only
		this.m_strPreset= strPreset.contains( "/" ) ? strPreset.substring( strPreset.lastIndexOf( "/" )+1 ) : strPreset;
		
		// Override only the values that we care about
		this.m_pNotificationType= 	NotificationType.EVNT;
		this.m_strID= 				EventSubType.PresetLoaded.getValue();
	}

	@Override
	public CBStatus _Callback( String title, String id, String message, String[] buttons, int value ) throws Exception {
		// Make sure the correct preset was loaded
		if( !this.m_strPreset.equals( message ) && !this.m_strPreset.equals( message + ".xml" ) )
			this.m_strErrMsg= " The expected preset was not loaded.  Expected: '" + this.m_strPreset + "' but '" + message + "' was loaded instead.";
		
		return CBStatus.HANDLED;
	}

}
