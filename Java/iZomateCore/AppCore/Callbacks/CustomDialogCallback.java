package iZomateCore.AppCore.Callbacks;

import iZomateCore.AppCore.HostApp;
import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.NotificationCallback;
import iZomateCore.UtilityCore.TimeUtils;

import java.awt.event.KeyEvent;

public class CustomDialogCallback extends NotificationCallback {
	private HostApp	m_pHostApp;
	private int	m_nKeyCode= 0;
	private double	m_dSleep= 0;
	private String	m_strTextToType= null;
	private double	m_dDismissalTime= 0;
	private boolean m_bIsGoToFolder= false;
	
	/**
	 * 
	 * @param strTitle
	 * @param strMessage
	 * @param nKeyCode
	 * @param pHostApp
	 * @param dSleep
	 */
	public CustomDialogCallback( String strTitle, String strMessage, int nKeyCode, HostApp pHostApp ) {
		this.m_pHostApp= pHostApp;
		this.m_nKeyCode= nKeyCode;
		
		// Override only the values that we care about
		this.m_pNotificationType= 	NotificationType.DLOG;
		this.m_strTitle= 			strTitle;
		this.m_strMessage= 			strMessage;
	}

	/**
	 * 
	 * @param strTitle
	 * @param strMessage
	 * @param strTextToType
	 * @param pHostApp
	 * @param dSleep
	 */
	public CustomDialogCallback( String strTitle, String strMessage, String strTextToType, int nKeyCode, HostApp pHostApp ) {
		this( strTitle, strMessage, nKeyCode, pHostApp );
		this.m_strTextToType= strTextToType;
	}

	/**
	 * 
	 * @param title
	 * @param id
	 * @param message
	 * @param buttons
	 * @param value
	 * @return
	 * @throws Exception
	 */
	@Override
	public CBStatus _Callback( String title, String id, String message, String[] buttons, int value ) throws Exception {
		if( this.m_pHostApp._Testbed()._SysInfo()._isWin() )
			TimeUtils.sleep( .5 );
		
		if( this.m_strTextToType != null )
			this.m_pHostApp._Testbed()._Robot()._keyType( this.m_strTextToType );
		
		// This dismisses the Mac "Go To Folder" dialog that pops up
		if( this.m_bIsGoToFolder && this.m_pHostApp._Testbed()._SysInfo()._isMac() ) {
			this.m_pHostApp._Testbed()._Robot()._keyType( KeyEvent.VK_ENTER );
			TimeUtils.sleep( 1 + (this.m_strTextToType.length() * .02) );
		}
		
		// This dismisses the native dialog
		if( this.m_nKeyCode != 0 )
			this.m_pHostApp._Testbed()._Robot()._keyType( this.m_nKeyCode );
		
		this.m_dDismissalTime= System.currentTimeMillis();
		
		if( this.m_dSleep > 0)
			TimeUtils.sleep( this.m_dSleep );
		
		return CBStatus.HANDLED;
	}
	
	/**
	 * 
	 * @return
	 */
	public double _GetDismissalTime() {
		return this.m_dDismissalTime;
	}
	
	public void _SetSleep( double dSleepTime ) {
		this.m_dSleep= dSleepTime;
	}

	public CustomDialogCallback _IsGoToFolder() {
		this.m_bIsGoToFolder= true;
		return this;
	}
}
