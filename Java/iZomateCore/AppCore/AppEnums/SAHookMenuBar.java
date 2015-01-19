package iZomateCore.AppCore.AppEnums;

import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.Notifications.NotificationCallback;
import iZomateCore.ServerCore.RPCServer.RPCServer;
import iZomateCore.TestCore.Testbed;
import iZomateCore.UtilityCore.TimeUtils;

import java.awt.event.KeyEvent;

public enum SAHookMenuBar {
	File_Open					( "Open...", 				new int[]{KeyEvent.VK_META, KeyEvent.VK_O},						new int[]{KeyEvent.VK_CONTROL, KeyEvent.VK_O}  ),
	File_Close					( "Close", 					new int[]{KeyEvent.VK_META, KeyEvent.VK_W}, 					new int[]{KeyEvent.VK_CONTROL, KeyEvent.VK_W} ),
	File_Save					( "Save", 					new int[]{KeyEvent.VK_META, KeyEvent.VK_S}, 					new int[]{KeyEvent.VK_CONTROL, KeyEvent.VK_S} ),
	File_SaveAs					( "Save As...", 			new int[]{KeyEvent.VK_META, KeyEvent.VK_SHIFT, KeyEvent.VK_O}, 	new int[]{KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_O} ),
	File_Import_Params			( "Import Params...", 		new int[]{KeyEvent.VK_META, KeyEvent.VK_SHIFT, KeyEvent.VK_P}, 	new int[]{KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_P} ),
	File_Export_Params			( "Export Params...", 		new int[]{KeyEvent.VK_META, KeyEvent.VK_O}, 					new int[]{KeyEvent.VK_CONTROL, KeyEvent.VK_O} ),
	File_Quit					( "Quit", 					new int[]{KeyEvent.VK_META, KeyEvent.VK_Q}, 					new int[]{KeyEvent.VK_CONTROL, KeyEvent.VK_Q} ),
	Audio_Play					( "Play", 					new int[]{KeyEvent.VK_SPACE}, 									null ),
	Audio_Loop					( "Loop", 					new int[]{KeyEvent.VK_META, KeyEvent.VK_SPACE}, 				new int[]{KeyEvent.VK_CONTROL, KeyEvent.VK_SPACE} ),
	Audio_Stop					( "Stop", 					new int[]{KeyEvent.VK_SPACE}, 									null ),
	Audio_Hardware_Setup		( "Audio Hardware Setup...",null, null ),
	Audio_Enable_Input			( "Enable Input", 			null, null ),
	View_Parameter_Panel		( "Parameter Panel", 		null, null ),
	View_Auto_broadcast_Panel	( "Auto-broadcast Panel", 	null, null ),
	View_System_Delay_Panel		( "System Delay Panel", 	null, null ),
	View_Performance_Panel		( "Performance Panel", 		null, null ),
	View_Memory_Panel			( "Memory Panel", 			null, null ),
	View_Stress_Test_Panel		( "Stress Test Panel", 		null, null ),
	View_State_History_Panel	( "State History Panel",	null, null ),
	View_Melody_Panel			( "Melody Panel", 			null, null ),
	RX_Edit_Deselect			( "Deselect", 				new int[]{KeyEvent.VK_META, KeyEvent.VK_D}, 						new int[]{KeyEvent.VK_CONTROL, KeyEvent.VK_D} ),
	RX_View_Markers_And_Regions( "Markers and Regions", 	new int[]{KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_M}, 	null ),
	RX_Edit_Preferences			( "Preferences", 			new int[]{KeyEvent.VK_META, KeyEvent.VK_COMMA}, 	 				new int[]{KeyEvent.VK_CONTROL, KeyEvent.VK_COMMA} ),
	RX_File_Batch_Processing	( "Batch Processing",		new int[]{KeyEvent.VK_META, KeyEvent.VK_B}, 						new int[]{KeyEvent.VK_CONTROL, KeyEvent.VK_B}),
	;
	
	private String mValue;
	private int[] mHotKey_Mac;
	private int[] mHotKey_Win;
	
	private SAHookMenuBar(String value, int[] macHotKey, int[] winHotKey ) {
		this.mValue = value;
		this.mHotKey_Mac= macHotKey;
		if( winHotKey == null)
			this.mHotKey_Win= macHotKey;
		else
			this.mHotKey_Win= winHotKey;			
	}
	
	public String _getValue() {
		return this.mValue;
	}
	
	public int[] _getHotKey( Testbed pTestbed ) throws Exception {
		if( pTestbed._SysInfo()._isWin() ) 
			return this.mHotKey_Win;
		else
			return this.mHotKey_Mac;
	}
	
	public void _doHotKey( Testbed pTestbed ) throws Exception {
		this._doHotKey( pTestbed, null, null, null, null );
	}

	public void _doHotKey( Testbed pTestbed, RPCServer pRPCServer, EventSubType pEventType, String strMessage, NotificationCallback pCallback ) throws Exception {
		if( pTestbed._SysInfo()._isWin() ) 
			pTestbed._Robot()._keyType( this.mHotKey_Win );
		else
			pTestbed._Robot()._keyType( this.mHotKey_Mac );

		if( pRPCServer != null )
			pRPCServer._waitForEvent( pEventType, strMessage, pCallback, TimeUtils._GetDefaultTimeout() );
	}

}
