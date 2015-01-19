package iZomateCore.AppCore;

import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.NotificationCallback;
import iZomateCore.ServerCore.RPCServer.OutgoingRequest;
import iZomateCore.UtilityCore.TimeUtils;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Console {
	private Plugin		m_pPlugin= null;
	private String[] 	m_strProfilers= null;
		
	public Console( Plugin pPlugin ) {
		this.m_pPlugin= pPlugin;
	}

	public void _Perf_ClearLog() throws Exception {
		this._Console( "perf clearlog", "Logs Cleared", "Logged data has been cleared." );
	}
	
	public String[] _Perf_List() throws Exception {
		if( this.m_strProfilers == null ) {
			try {
				List<String> lProfilers= new ArrayList<String>();
				String strMessage= this._Console( "perf list", "Profiler List", null )._GetMessage();
						
				for( String strLine : strMessage.split("\n") ){
					if( strLine.indexOf( ": " ) != -1 ) {
						String strProfilerGroup= strLine.substring( strLine.indexOf( ": " )+2 );
						for( String strProfiler : strProfilerGroup.split( ", " ) )
							if( ! lProfilers.contains( strProfiler ))
								lProfilers.add( strProfiler );
					}
				}
				
				if( lProfilers.size() > 0 )
					this.m_strProfilers= lProfilers.toArray( new String[ lProfilers.size() ]);
				
			} catch ( Exception e ) {
				this.m_pPlugin._Logs()._ResultLog()._LogException( e, true );
			}
		}
		
		if( this.m_strProfilers == null )
			return new String[0];
		else
			return this.m_strProfilers;
	}
	
	public void _Perf_EnableLogging( boolean bOn ) throws Exception {
		String strStatus= this._Perf_Status();
		if( ( bOn && strStatus.contains( "logging: off")) || 
			(!bOn && strStatus.contains( "logging: on") ) ||
			(!bOn && strStatus.contains( "logging: paused")) )
			this._Perf_SetLog( bOn );
		else if ( bOn && strStatus.contains( "logging: paused" ))
			this._Perf_PauseLogging( true );
	}
	
	public void _Perf_PauseLogging( boolean bOn ) throws Exception {
		ConsoleCallback cb= this._Console( "perf pause " + (bOn?1:0), "Perf Usage", null );
		if( !cb._GetMessage().contains( "logging: " + (bOn?"paused":"on") ) )
				throw new Exception( "Unable to set 'Perf Pause " + (bOn?1:0) + "'.  Log returned: " + cb._GetMessage() );
	}

	public String _Perf_GetLog( String profilerName ) throws Exception {
		ConsoleCallback cbConsole= this._Console( "perf copylog " + profilerName, "Performance Logged", "Profiler " + profilerName + " results are on the clipboard." );
		if( cbConsole.m_strWarningMessage != null )
			return (cbConsole.m_strWarningMessage.contains( "no results to report" ) ? "No results to report." : cbConsole.m_strWarningMessage);
		else
			return this.m_pPlugin.m_pHostApp._Testbed()._Robot()._clipboardGetText();
	}
	
	public String _Perf_Status( ) throws Exception {
		return this._Console( "perf status", "Perf Usage", null )._GetMessage();
	}

	private void _Perf_SetLog( boolean bOn ) throws Exception {
		this._Console( "perf log " + (bOn?1:0), "Reinstantiation Required", "You must reinstantiate for logging enable/disable to take effect." );
	}


	private ConsoleCallback _Console( String command, String dlogTitle, String dlogMessage ) throws Exception {
		ConsoleCallback callback= null;
		if( dlogTitle != null || dlogMessage != null )
			callback= new ConsoleCallback( dlogTitle, dlogMessage );
		
		//TODO: More AppCore cleanup
		OutgoingRequest req= this.m_pPlugin._GetPluginServer()._createRequest( "executeConsoleCommand" );
		req._addString( command, "command" );
		req._setDialogNotification( dlogTitle, dlogMessage, callback );
		TimeUtils.sleep( .1 );
		try {
			this.m_pPlugin._GetPluginServer()._processRequest( req );
		} catch ( Exception e ) {
			if( e.getMessage().equals( "timed out" ))
				this.m_pPlugin._GetPluginServer()._processRequest( req );
			
			throw e;
		}

		return callback;
	}

	/**
	 * 
	 * @author tskotz
	 *
	 */
	public class ConsoleCallback extends NotificationCallback {
		private String m_strWarningMessage= null;
		private String m_strMyTitle= null;
		private String m_strMyMessage= null;
		
		ConsoleCallback( String strTitle, String strMessage ) {
			// Override only the values that we care about
			this.m_pNotificationType= 	NotificationType.DLOG;
			// Don't specify a Title or message because we need to filter on everything to handle no profile data messages as well
			
			// Private copies.  Not super members!
			this.m_strMyTitle= strTitle;
			this.m_strMyMessage= strMessage;
		}

		@Override
		public CBStatus _Callback( String title, String id, String message, String[] buttons, int value ) throws Exception {
			CBStatus status= CBStatus.NOTHANDLED;
						
			//Check to see if we got the "No Events" dialog
			if( "No Events".equals( title ) ) {
				Console.this.m_pPlugin.m_pHostApp._Testbed()._Robot()._keyType( KeyEvent.VK_ENTER );
				Console.this.m_pPlugin._GetPluginServer()._removeWaitForEvent( this.m_strMyTitle, this.m_strMyMessage );
				this.m_strWarningMessage= message;
				status= CBStatus.HANDLED;
			}
			else if( this.m_strMyTitle.equals( title ) && ( this.m_strMyMessage == null || this.m_strMyMessage.equals( message ) ) ) { // We got what we were expecting
				Console.this.m_pPlugin.m_pHostApp._Testbed()._Robot()._keyType( KeyEvent.VK_ENTER );
				Console.this.m_pPlugin._GetPluginServer()._waitForEvent( EventSubType.DLOGDismissed, this.m_strMessage, null, 5 );
				this.m_strTitle= title;
				this.m_strMessage= message;
				status= CBStatus.HANDLED;
			}
			
			return status;
		}
	}

}
