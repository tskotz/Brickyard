package iZomateCore.AppCore.Callbacks;

import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.NotificationCallback;

public class RXStatusPanelEventCallback extends NotificationCallback {
	private String 	m_strThisMessage;
	private String 	m_strActualMessage= "";

	public RXStatusPanelEventCallback( String strMessage ) throws Exception {
		this.m_strThisMessage= strMessage;
		
		// Override only the values that we care about
		this.m_pNotificationType= 	NotificationType.EVNT;
		this.m_strTitle= EventSubType.StatusBarPanel.getValue();		
	}

	@Override
	public CBStatus _Callback( String title, String id, String message, String[] buttons, int value ) throws Exception {
		if( message.contains( this.m_strThisMessage ) ) {
			this.m_strActualMessage= message;			
			return CBStatus.HANDLED;			
		}
		return CBStatus.NOTHANDLED;
	}
	
	public float _GetProcessTime() throws Exception  {
		if( this.m_strActualMessage.isEmpty() )
			throw new Exception("StatusMessage is Empty");
		
		String strNum= this.m_strActualMessage.substring( this.m_strActualMessage.indexOf( "(" )+1, this.m_strActualMessage.indexOf( ")" ) );
		String[] strParts= strNum.split( " " );
		if( strParts.length == 0 )
			throw new Exception("Unable to split string. " + this.m_strActualMessage );
		
		float fVal= 0.0f;		
		for(int i=0; i<strParts.length; i+=2) {
			String strVal=  strParts[i];
			String strType= strParts[i+1];
			if( strType.equals("min") ) 		fVal+= Float.valueOf(strVal)*60.0f*1000.0f;
			else if( strType.equals("s") )   	fVal+= Float.valueOf(strVal)*1000.0f;
			else if( strType.equals("ms") )  	fVal+= Float.valueOf(strVal);
			else
				throw new Exception("Unknown string type. " + this.m_strActualMessage );
		}
		
		return fVal;
	}
	
	public String _GetStatusMessage() {
		return this.m_strActualMessage;
	}
	
}
