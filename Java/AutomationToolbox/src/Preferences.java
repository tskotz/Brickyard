package AutomationToolbox.src;

/**
 * Enum for preference names stored in 
 * @author terryskotz
 *
 */
public enum Preferences {
	// Name						  Default value
	DataparamsRootDir			( "../DataParams" ),
	DefaultJars					( "./Jars" ),
	StagingDir					( "./AutomationToolbox/ManagerStagingDirs" ),
	ShowJobCount				( "20" ),
	StartTestManagerOnLaunch	( "true" ),
	EnableJobLoadBalancing		( "false" ),
	AllowJobRequestsFrom		( "192.168.1.1:8380, 192.168.1.2:8380" ),
	SendJobRequestsTo			( "192.168.1.11:8380, 192.168.1.12:8380, 192.168.1.13:8380" ),
	pvtWebServerPort			( "8380" ),
	pvtRemoteServerPort			( "54320" ),
	DashboardLogo				( "./AutomationToolbox/Preferences/Templates/Images/holiday-spirit-anastasiya-malakhova.jpg" );
	
	private final String mDefaultData;
	
	/**
	 * 
	 * @param strDefaultData
	 */
	Preferences( String strDefaultData ) {
		this.mDefaultData= strDefaultData;
	}

	/**
	 * 
	 * @param _GetDefaultData
	 * @return
	 * @throws Exception
	 */
	public String _GetDefaultData() {    	
		return this.mDefaultData;
	}

}