package iZomateCore.AppCore.AppEnums.WindowControls;

import iZomateCore.AppCore.Plugin;

public enum FileExplorerViews {
	//TODO: RX3 Preset List Box is a list box and not a FileExplorer!  
	Preset			("Preset FileExplorer", "RX3:Preset List Box", "RX4:Preset List Box"),
	UNKNOWN			("UNKNOWN", null, null);
	
	private String m_strValue;
	private String m_strAltValue;
	private String m_strAltValue2;
	
	/**
	 * 
	 * @param value
	 * @param strAltValue
	 */
	private FileExplorerViews(String value, String strAltValue, String strAltValue2 )	{
		this.m_strValue= value;
		this.m_strAltValue= strAltValue;
		this.m_strAltValue2= strAltValue2;
	}
	
	/**
	 * 
	 * @return
	 */
	public String _getValue() {
		return this.m_strValue;
	}

	/**
	 * 
	 * @param pPlugin
	 * @return
	 * @throws Exception
	 */
	public FileExplorerViews _for( Plugin pPlugin ) throws Exception {
		if( this.m_strAltValue != null ) {
			String strHostPrefix= this.m_strAltValue.substring( 0, this.m_strAltValue.indexOf( ":" ) );
			if( strHostPrefix != null && pPlugin._GetPluginInfo().m_strShortName.startsWith( strHostPrefix ) )
				this.m_strValue= this.m_strAltValue.substring( strHostPrefix.length() + 1 );
		}
		if( this.m_strAltValue2 != null ) {
			String strHostPrefix= this.m_strAltValue2.substring( 0, this.m_strAltValue2.indexOf( ":" ) );
			if( strHostPrefix != null && pPlugin._GetPluginInfo().m_strShortName.startsWith( strHostPrefix ) )
				this.m_strValue= this.m_strAltValue2.substring( strHostPrefix.length() + 1 );
		}
		return this;
	}
}
