package iZomateCore.iZTests.RX;

import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.NotificationCallback;

import java.util.Vector;

//! Callback to handle messages sent when our plug-in context menu has been constructed
public class RXPluginMenuCallback extends NotificationCallback {

	private Vector<Vector<Integer>> m_pluginMenuSizes;
	
	public RXPluginMenuCallback() throws Exception {
		this.m_pNotificationType= 	NotificationType.EVNT;
		this.m_strTitle= EventSubType.PluginMenuConstructed.getValue();		
	}

	@Override
	public CBStatus _Callback( String title, String id, String message, String[] buttons, int value ) throws Exception {
		
		if( id.equals( EventSubType.PluginMenuConstructed.getValue() ) ) {
			// The PluginMenuConstructed event sends us a list of numbers separated by commas as its message.
			// We have to turn a string like "2,1,15,3,20,25,30," to a vector of vectors like ((15), (20,25,30)).
			String[] strValues= message.split(",");
			this.m_pluginMenuSizes= new Vector<Vector<Integer>>();
			int nNumTypes= Integer.parseInt(strValues[0]);
			
			// We should have at least one plug-in per type per manufacturer
			if( strValues.length < nNumTypes * 2 + 1 ) {
				throw new Exception("Error parsing message '" + message + "' from PluginMenuConstructed event");
			}
			// Store our tree of values
			for( int iIndex= 1; iIndex < strValues.length; ) {
				Vector<Integer> pluginsPerManufacturer= new Vector<Integer>();
				int nManufacturers= Integer.parseInt(strValues[iIndex]);
				iIndex++;
				for( int iManufacturer= 0; iManufacturer < nManufacturers; ++iManufacturer ) {
					Integer nPlugins= Integer.parseInt(strValues[iIndex]);
					pluginsPerManufacturer.add( nPlugins );
					iIndex++;
				}
				this.m_pluginMenuSizes.add( pluginsPerManufacturer );
			}
			return CBStatus.HANDLED;
		}
		return CBStatus.NOTHANDLED;
	}
	
	public Vector<Vector<Integer>> _GetPluginMenuSizes() throws Exception {
		return this.m_pluginMenuSizes;
	}
}
