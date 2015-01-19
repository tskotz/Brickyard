package iZomateCore.AppCore.Callbacks;


import iZomateCore.AppCore.HostApp;
import iZomateCore.AppCore.WindowControls;
import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.NotificationCallback;
import iZomateCore.UtilityCore.TimeUtils;

/**
 * 
 * @author mzapp
 *
 */
public class RXBatchJobCompleteCallback extends NotificationCallback {
	private HostApp	m_pHostApp;
	private Boolean m_bAllowFails;
	public RXBatchJobCompleteCallback( Boolean bAllowFails, HostApp pHostApp) throws Exception {
		this.m_pHostApp= pHostApp;
		// Override only the values that we care about
		this.m_pNotificationType= 	NotificationType.EVNT;
		this.m_strID= "BatchJobFinished";		
		this.m_bAllowFails= bAllowFails;
	}

	@Override
	public CBStatus _Callback( String title, String id, String message, String[] buttons, int value ) throws Exception {
		TimeUtils.sleep( this.m_pHostApp._Testbed()._SysInfo()._isMac() ? .6 : 1.5 );
			
		if (m_bAllowFails = true){
			if(message.equals("") || message.equals(null)){
				this.m_pHostApp._Logs()._ResultLog()._logLine( "Batch job complete! \n" );
			}else{
				this.m_pHostApp._Logs()._ResultLog()._logLine( "Batch job complete! \n" );
				this.m_pHostApp._Logs()._ResultLog()._logLine( "The following file errors occurred: \n" );
				this.m_pHostApp._Logs()._ResultLog()._logLine( message + "\n" );
			}
		}else{
			this.m_pHostApp._Logs()._ResultLog()._logLine( "The following file errors occurred: \n" );
			this.m_pHostApp._Logs()._ResultLog()._logLine( message + "\n" );
			return CBStatus.NOTHANDLED; // let the callback timeout and fail if errors occurred and error catching isn't turned on.
			}
		return CBStatus.HANDLED;
		}
	}

