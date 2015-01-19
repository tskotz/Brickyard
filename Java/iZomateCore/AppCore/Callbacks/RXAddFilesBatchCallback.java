package iZomateCore.AppCore.Callbacks;

import java.awt.event.KeyEvent;

import iZomateCore.AppCore.AppEnums.HostType;
import iZomateCore.AppCore.HostApp;
import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.NotificationCallback;
import iZomateCore.ServerCore.RPCServer.RemoteServer.RemoteFile;
import iZomateCore.UtilityCore.TimeUtils;

/**
 * 
 * @author tskotz and mzapp
 *
 */
public class RXAddFilesBatchCallback extends NotificationCallback {
	private String 	m_strDirectory;
	private HostApp	m_pHostApp;

	
	
	public RXAddFilesBatchCallback( String strDirectory, HostApp pHostApp ) {
		this.m_pHostApp= pHostApp;
		this.m_strDirectory= strDirectory;
		
		// Override only the values that we care about
		this.m_pNotificationType= 	NotificationType.DLOG;
		String strName= this.m_pHostApp._HostType()._GetInternalName();
		this.m_strTitle= strName.equals( HostType.RX3._GetInternalName() ) || strName.equals( HostType.RX4._GetInternalName() ) ? 
				"Select audio files" : "Select an audio file";
	}

	@Override
	public CBStatus _Callback( String title, String id, String message, String[] buttons, int value ) throws Exception {
		TimeUtils.sleep( this.m_pHostApp._Testbed()._SysInfo()._isMac() ? .6 : 1.5 );
		
		if(this.m_pHostApp._Testbed()._SysInfo()._isMac()){ //MAC dialogue
			// Get the list of files in this directory
			RemoteFile dir= this.m_pHostApp._Testbed()._CreateRemoteFile( m_strDirectory );
			String[] files= dir._list( false );
			
			this.m_pHostApp._Testbed()._Robot()._keyType( m_strDirectory );// Enter the directory
			this.m_pHostApp._Testbed()._Robot()._keyType( KeyEvent.VK_ENTER);// Go to the directory
			TimeUtils.sleep(0.5);
			this.m_pHostApp._Testbed()._Robot()._keyPress( KeyEvent.VK_SHIFT );//Hold down shift
			for (int i = 0; i<= files.length ; i ++){
				this.m_pHostApp._Testbed()._Robot()._keyType( KeyEvent.VK_DOWN ); //Since we can't tell how many audio files are present, but we can tell how many total files, just press down until all possible files could be selected.
				TimeUtils.sleep(0.5);
			}
			this.m_pHostApp._Testbed()._Robot()._keyRelease( KeyEvent.VK_SHIFT );//Release shift
			TimeUtils.sleep(0.1);
			this.m_pHostApp._Testbed()._Robot()._keyType( KeyEvent.VK_ENTER );//Add the files
			
		} else { //PC dialogue

			this.m_pHostApp._Testbed()._Robot()._keyType(".. \n"); //this is to work around the windows weirdness of tab-order changing if you type the name of the current path into the file name dialogue box by forcing the directory to change to the directory above first.  DO NOT USE THIS METHOD AT A ROOT LEVEL DIRECTORY.
			TimeUtils.sleep(0.1);
			
			this.m_pHostApp._Testbed()._Robot()._keyType( this.m_strDirectory + "\n" );  //enter the path from the parameters xml file, hit return
			TimeUtils.sleep( 0.5 );

			for( int i= 0; i <= 8; ++i ) 
				this.m_pHostApp._Testbed()._Robot()._keyType( KeyEvent.VK_TAB );				//Tab around a whole bunch to get to the files window
			this.m_pHostApp._Testbed()._Robot()._keyType( KeyEvent.VK_CONTROL, KeyEvent.VK_A);  //Select everything and open it
			this.m_pHostApp._Testbed()._Robot()._keyType( KeyEvent.VK_ENTER );
		}

		this.m_pHostApp._GetAppServer()._waitForEvent( EventSubType.DLOGDismissed, null, null, 10 );  //Prep the server to wait for the AddFiles dialogue to be dismissed.
		return CBStatus.HANDLED;
	}

}

