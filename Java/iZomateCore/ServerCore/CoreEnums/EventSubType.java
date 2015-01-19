package iZomateCore.ServerCore.CoreEnums;

import iZomateCore.AppCore.Plugin;


public enum EventSubType
{
	/** All event notifications for the specific NotificationType 	*/
	DEFAULT						("DEFAULT"),
	/** A NULL eventType 											*/
	NULL						(null),
	/** RPCServer communication event								*/
	RPCServerEvent				("RPCServer"),
	/** C++ Test Event 												*/
	TestEventNotification		("TestEventNotification"),
	/** Preset Manager Animation event type							*/
	PresetManager				("Preset Manager", "RX3:Presets", "RX4:Presets", "Nectar2:Preset Manager Dialog"),
	/** Preset Load notification event type							*/
	PresetLoaded				("PresetLoaded"),
	/** Mouse event type											*/
	MenuInvoke					("MenuInvoke"),
	/** Mouse event type											*/
	MouseEvent					("MouseEvent"),
	/** Keyboard event type											*/
	KeyboardEvent				("KeyboardEvent"),
	/** The event that gets sent upon an open folder dialog window when it is ready	*/
	DLOGInited					("DLOG Inited"),
	/** The event that gets sent upon dismissing a dialog window	*/
	DLOGDismissed				("DLOG Dismissed"),
	/** RX 3 Status Bar Panel										*/
	StatusBarPanel				("StatusBarPanel"),
	/** RX 3 Effect Panel Status Bar								*/
	EffectPanelStatus			("EffectPanelStatus"),
	/** RX 3 finished loading plug-in								*/
	PluginLoaded				("PluginLoaded"),
	/** RX 3 constructed its plug-in context menu					*/
	PluginMenuConstructed		("PluginMenuConstructed"),
	/** RX 3 module finished training 								*/
	FinishedTraining			("FinishedTraining"),
	/** RX 3 module finished processing								*/
	FinishedProcessing			("FinishedProcessing"),
	/** RX 3 finished initializing a module							*/
	InitializedModule			("InitializedModule"),
	/** RX 3 finished opening a file								*/
	OpenedFile					("OpenedFile"),
	/** RX 3 finished saving a file									*/
	SavedFile					("SavedFile"),
	/** Text Edit event												*/
	EditTextEvent				("EditText"),
	/** Add Files event												*/
	BatchAddFilesEvent			("BatchAddFiles"),
	/** Batch File Finished Processing event						*/
	BatchFileFinished			("BatchFileFinished"),
	/** Batch Job Finished Processing event							*/
	BatchJobFinished			("BatchJobFinished"),
	;

	private String		m_strValue;
	private String		m_strAltValue[]= new String[]{};

   /**
    * Constructor
    * @param value the transfer type ID
    */
	EventSubType(String value) {
		this.m_strValue = value;
	}
	
   /**
	* Constructor
	* @param value the transfer type ID
	*/
	EventSubType(String value, String... altValue) {
		this.m_strValue = value;
		this.m_strAltValue= altValue;
	}
	

   /**
    * Returns the enum corresponding to the passed in string
    *
    * @param key the string to convert to an Enum
    * @return the enum
    * @throws Exception
    */
	public static EventSubType getEnum(String key) throws Exception {
		for( EventSubType val : EventSubType.values() ) {
			if( val.m_strValue != null && val.m_strValue.equalsIgnoreCase(key) )
				return val;
		}
		throw new Exception( "Invalid EventType: " + key );
	}
	
	/**
	 * 
	 * @param pPlugin
	 * @return
	 * @throws Exception
	 */
	public EventSubType _for( Plugin pPlugin ) throws Exception {
		for( String strAltValue :  this.m_strAltValue ) {
			String strHostPrefix= strAltValue.substring( 0, strAltValue.indexOf( ":" ) );
			if( strHostPrefix != null && pPlugin._GetPluginInfo().m_strShortName.startsWith( strHostPrefix ) ) {
				this.m_strValue= strAltValue.substring( strHostPrefix.length() + 1 );
				break;
			}
		}
		return this;
	}
	
   /**
    * Returns the value for the EventType
    *
    * @return the value for the EventType
    */
	public String getValue() {
		return this.m_strValue;
	}
}
