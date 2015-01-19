package iZomateCore.AppCore.AppEnums;

public enum RXModule {

	DECLIP				("Declip",				"Element Panel Button Declip"),
	DECLICK_DECRACKLE	("Declick & Decrackle", "Element Panel Button Declick & Decrackle"),
	REMOVE_HUM			("Remove Hum",			"Element Panel Button Remove Hum"),
	DENOISE				("Denoise",				"Element Panel Button Denoise"),
	SPECTRAL_REPAIR		("Spectral Repair",		"Element Panel Button Spectral Repair"),
	GAIN				("Gain",				"Element Panel Button Gain"),
	EQ					("Equalizer",			"Element Panel Button Equalizer"),
	CHANNEL_OPS			("Channel Operations",	"Element Panel Button Channel Operations"),
	SPECTRUM_ANALYZER	("Spectrum Analyzer",	"Element Panel Button Spectrum Analyzer"),
	RESAMPLE			("Resample",			"Element Panel Button Resample"),
	DITHER				("Dither",				"Element Panel Button Dither"),
	PLUG_IN				("Plug-In",				"Element Panel Button Plug-In"),
	TIME_PITCH			("Time/Pitch",			"Element Panel Button Time & Pitch"),
	DE_CONSTRUCT		("Deconstruct",			"Element Panel Button Deconstruct"),
	DEREVERB			("Dereverb",			"Element Panel Button Dereverb"),;

	private String m_strValue;
	private String m_strButtonID;
	
	private RXModule(String strValue, String strButtonID ) {
		this.m_strValue = strValue;
		this.m_strButtonID= strButtonID;
	}
	
	public String _getValue() {
		return this.m_strValue;
	}

	public String _getButtonID() {
		return this.m_strButtonID;
	}

	/**
     * Returns the enum corresponding to the passed in string
     *
     * @param RXModule the RXModule value to convert to an Enum
     * @return the enum
     * @throws Exception
     */
	public static RXModule _getEnum( String strRXModule ) throws Exception {
		for( RXModule val : RXModule.values() )
			if( val.m_strValue.equalsIgnoreCase(strRXModule) )
				return val;
    	
		throw new Exception( "Unsupported RX Module: " + strRXModule );
    }

}
