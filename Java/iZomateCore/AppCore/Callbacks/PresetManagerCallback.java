package iZomateCore.AppCore.Callbacks;

import iZomateCore.AppCore.AppEnums.WindowControls.Buttons;
import iZomateCore.AppCore.AppEnums.WindowControls.FileExplorerViews;
import iZomateCore.AppCore.Plugin;
import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.NotificationCallback;

public class PresetManagerCallback extends NotificationCallback {
	private 	String 					m_strPreset;
	public 		long 					m_lTime= 0;
	private 	PresetLoadedCallback 	m_cbPresetLoaded;
	private		Plugin					m_pPlugin;
	
	public PresetManagerCallback( String strPreset, Plugin pPlugin )  {
		this.m_pPlugin= pPlugin;
		this.m_strPreset= (strPreset.startsWith( "\\" )||strPreset.startsWith( "/" ))?strPreset.substring( 1 ):strPreset;
		

		// Override only the values that we care about
		this.m_pNotificationType= 	NotificationType.EVNT;
		this.m_strTitle=	EventSubType.PresetManager.getValue();
		this.m_strMessage= 	"FullyVisible";
	}
	
	public String _GetErrorMsg() {
		if( this.m_cbPresetLoaded != null )
			return this.m_cbPresetLoaded.m_strErrMsg;
		return null;
	}
	
	@Override
	public CBStatus _Callback( String title, String id, String message, String[] buttons, int value ) throws Exception {
		long lStartTime= System.currentTimeMillis();
		this.m_cbPresetLoaded= new PresetLoadedCallback( this.m_strPreset );
		this.m_pPlugin._Controls()._FileExplorerView( FileExplorerViews.Preset._for( this.m_pPlugin ) )._select( this.m_strPreset );
		
		String strName= this.m_pPlugin._GetPluginInfo().m_strShortName;
		if( strName.startsWith( "RX" ))
			this.m_pPlugin._Controls()._Button( Buttons.GlobalPreset._for( this.m_pPlugin ) )._SetCallback( this.m_cbPresetLoaded )._click( EventSubType.PresetManager._for( this.m_pPlugin ), "FullyHidden" );
		else
			this.m_pPlugin._Controls()._Button( Buttons.AlloyPresetClose._for( this.m_pPlugin ) )._SetCallback( this.m_cbPresetLoaded )._click( EventSubType.PresetManager._for( this.m_pPlugin ), "FullyHidden" );
			
		this.m_lTime= System.currentTimeMillis() - lStartTime;
		return CBStatus.HANDLED;
	}
	
}
