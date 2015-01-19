package iZomateCore.ServerCore.Notifications;

import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import org.omg.CORBA.IntHolder;

/**
 * The Notification callback interface. The should be implemented by classes
 * looking to register a callback with the NotifyHandler class. Each class
 * should implement their respective callback methods for use by the
 * NotifyHandler class.
 */
public abstract class NotificationCallback
{
	public NotificationType	m_pNotificationType= null;
	public String 		m_strTitle= null;
	public String		m_strMessage= null;
	public String		m_strID= null;
	public IntHolder	m_pIntHolder= null;
	
	public NotificationCallback() {
		
	}
	
	/**
	 * Standard application Notification callback interface
	 *
	 * @param type - notification datum type
	 * @param title - notification title
	 * @param id - notification id
	 * @param message - notification message
	 * @param buttons - notification buttons
	 * @param value - notification int value
	 * @return callbackStatus
	 * @throws Exception
	 */
	// To be implemented by each class that implements NotificationCallback
	public abstract CBStatus _Callback( String title, String id, String message, String[] buttons, int value ) throws Exception;

	/**
	 * The Notification Type property associated with this callback.  This is used for callback filtering in the RPCServer callback mechanism.
	 * This must be set to a valid NotificaitonType
	 * @return NotificationType
	 */
	public NotificationType _GetNotificationType() {
		return this.m_pNotificationType;
	}
	
	public boolean _CompareNotificationType( NotificationType pNotificationType ) {
		return ( this.m_pNotificationType == null || this.m_pNotificationType.equals( pNotificationType ) );	
	}

	/**
	 * The Title property associated with this callback.  This is used for callback filtering in the RPCServer callback mechanism.
	 * Return null to have filter ignore this property
	 * @return Title
	 */
	public String _GetTitle() {
		return this.m_strTitle;
	}
	
	/**
	 * 
	 * @param strTitle
	 * @return
	 */
	public boolean _CompareTitle( String strTitle ) {
		return ( this.m_strTitle == null || this.m_strTitle.equals( strTitle ) );	
	}

	/**
	 * The Title property associated with this callback.  This is used for callback filtering in the RPCServer callback mechanism.
	 * Return null to have filter ignore this property
	 * @return Message
	 */
	public String _GetMessage() {
		return this.m_strMessage;
	}

	public boolean _CompareMessage( String strMessage ) {
		return ( this.m_strMessage == null || this.m_strMessage.equals( strMessage ) );	
	}

	/**
	 * The ID property associated with this callback.  This is used for callback filtering WNDW ID's and EVNT Types's in the RPCServer callback mechanism.
	 * Return null to have filter ignore this property
	 * @return ID
	 */
	public String _GetID() {
		return this.m_strID;
	}

	public boolean _CompareID( String strID ) {
		return ( this.m_strID == null || this.m_strID.equals( strID ) );	
	}

	/**
	 * The Int property associated with this callback.  This is used for callback filtering in the RPCServer callback mechanism.
	 * NOTE:  This value is currently being ignored in the RPC callback filter
	 * @return int
	 */
	public int _GetIntValue() {
		if( this.m_pIntHolder == null)
			return 0;
		return this.m_pIntHolder.value;
	}

	public boolean _CompareIntValue( int nValue ) {
		return ( this.m_pIntHolder == null || this.m_pIntHolder.value == nValue );	
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	public EventSubType _GetEventSubType() throws Exception {
		return EventSubType.getEnum( this.m_strTitle );
	}
	
	public String _ToString() {
		return "TYPE:" + this.m_pNotificationType + ", TITLE:" + this.m_strTitle + ", ID:" + this.m_strID + ", MSG:" + this.m_strMessage;
	}

}
