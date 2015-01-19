package iZomateCore.AppCore.AppEnums;

public enum HostType
{
	// Use the Release version because the d.exe will be stripped!
	// Apps								 Application Name						Plugin Name				Plugin Short Name			Has APP RPCServer	Has Plugin RPCServer
	CrashReporter						( "iZotope Crash Reporter", 			null, 					"iZotope Crash Reporter",	true,				false ),
	Iris								( "iZotope Iris", 						"Iris", 				"Iris",						true,				false ),
	ProToolsMac							( "Pro Tools", 							null, 					null,						false,				true  ),
	ProToolsWin							( "ProTools", 							null, 					"Pro Tools",				false,				true  ),
	ProTools11							( "ProTools11", 						null, 					"Pro Tools",				false,				true  ),
	RX4									( "iZotope RX 4", 						"RX4",	 				"RX4",						true,				false ),
	RX3									( "iZotope RX 3", 						"RX3",	 				"RX3",						true,				false ),
	RX3Mac								( "iZotope RX", 						"RX3",	 				"RX3",						true,				false ),
	RX3ii								( "RestorationApp", 					"RX3",	 				"RX3",						true,				false ),
	SoundForge							( "Sound Forge", 						null, 					"Sound Forge",				false,				true  ),
	AbletonLiveMac						( "Ableton Live 9 Standard",			null,					null,						false,				true  ),
	
	// SA Hooks
	Alloy2SAHook						( "iZAlloy2SAHook", 					"Alloy 2", 				"Alloy2",					true,				true ),
	IrisSAHook							( "iZIrisSAHook", 						"Iris", 				"Iris",						true,				true ),
	iZBreakTweakerSAHook				( "iZBreakTweakerSAHook", 				"BreakTweaker", 		"BreakTweaker",				true,				true ),
	iZDJFXSAHook						( "iZDJFXSAHook", 						"DJFX", 				"DJFX",						true,				true ),
	iZDJFXdSAHook						( "iZDJFXdSAHook", 						"DJFX", 				"DJFX",						true,				true ),
	iZMasteringExciterPluginSAHook		( "iZMasteringExciterPluginSAHook", 	"Mastering Exciter", 	"MasteringExciter",			true,				true ),
	iZMasteringImagerPluginSAHook		( "iZMasteringImagerPluginSAHook", 		"Mastering Imager", 	"MasteringImager",			true,				true ),
	iZMasteringLimiterPluginSAHook		( "iZMasteringLimiterPluginSAHook", 	"Mastering Limiter",	"MasteringLimiter",			true,				true ),
	iZMasteringReverbPluginSAHook		( "iZMasteringReverbPluginSAHook", 		"Mastering Reverb", 	"MasteringReverb",			true,				true ),
	iZDeclickerPluginSAHook				( "iZDeclickerPluginSAHook", 			"Declicker", 			"Declicker",				true,				true ),
	iZDeclipperPluginSAHook				( "iZDeclipperPluginSAHook", 			"Declipper", 			"Declipper",				true,				true ),
	iZDenoiserPluginSAHook				( "iZDenoiserPluginSAHook", 			"Denoiser", 			"Denoiser",					true,				true ),
	iZMasteringEQPluginSAHook			( "iZMasteringEQPluginSAHook", 			"MasteringEQ", 			"MasteringEQ",				true,				true ),
	iZMultibandCompressorPluginSAHook	( "iZMultibandCompressorPluginSAHook", 	"Multiband Compressor", "MultibandCompressor",		true,				true ),
	iZMeterBridgeSAHook					( "iZMeterBridgeSAHook", 				"MeterBridge", 			"MeterBridge",				true,				true ),
	iZNectar2SAHook						( "iZNectar2SAHook", 					"Nectar2", 				"Nectar2",					true,				true ),
	iZNectar2BreathControlSAHook		( "iZNectar2BreathControlSAHook", 		"DeBreath2", 			"DeBreath2",				true,				true ),
	iZNectar2PitchEditorSAHook			( "iZNectar2PitchEditorSAHook", 		"Nectar2PitchEditor", 	"Nectar2PitchEditor",		true,				true ),
	iZInsightSAHook						( "iZInsightSAHook", 					"Insight", 				"Insight",					true,				true ),

	iZRX4DeclickerSAHook				( "iZRX4DeclickerSAHook", 				"RX4Declicker", 		"RX4Declicker",				true,				true ),
	iZRX4DeclipperSAHook				( "iZRX4DeclipperSAHook", 				"RX4Declipper", 		"RX4Declipper",				true,				true ),
	iZRX4DecracklerSAHook				( "iZRX4DecracklerSAHook", 				"RX4Decrackler", 		"RX4Decrackler",			true,				true ),
	iZRX4DenoiserSAHook					( "iZRX4DenoiserSAHook", 				"RX4Denoiser", 			"RX4Denoiser",				true,				true ),
	iZRX4DenoiserZLSAHook				( "iZRX4DenoiserZLSAHook", 				"RX4DenoiserZL", 		"RX4DenoiserZL",			true,				true ),
	iZRX4DialogueDenoiserSAHook			( "iZRX4DialogueDenoiserSAHook", 		"RX4DlgDenoiser",		"RX4DlgDenoiser",			true,				true ),
	iZRX4DereverbSAHook					( "iZRX4DereverbSAHook", 				"RX4Dereverb", 			"RX4Dereverb",				true,				true ),
	iZRX4HumRemovalSAHook				( "iZRX4HumRemovalSAHook", 				"RX4HumRemoval", 		"RX4HumRemoval",			true,				true ),
	iZRX4SpectralRepairSAHook			( "iZRX4SpectralRepairSAHook", 			"RX4SpectralRepair", 	"RX4SpectralRepair",		true,				true ),

	// Pre GYP RX 3
	iZRX3DeclickerSAHook				( "iZRX3DeclickerSAHook", 				"RX3Declicker", 		"RX3Declicker",				true,				true ),
	iZRX3DeclipperSAHook				( "iZRX3DeclipperSAHook", 				"RX3Declipper", 		"RX3Declipper",				true,				true ),
	iZRX3DecracklerSAHook				( "iZRX3DecracklerSAHook", 				"RX3Decrackler", 		"RX3Decrackler",			true,				true ),
	iZRX3DenoiserSAHook					( "iZRX3DenoiserSAHook", 				"RX3Denoiser", 			"RX3Denoiser",				true,				true ),
	iZRX3DenoiserZLSAHook				( "iZRX3DenoiserZLSAHook", 				"RX3DenoiserZL", 		"RX3DenoiserZL",			true,				true ),
	iZRX3DialogueDenoiserSAHook			( "iZRX3DialogueDenoiserSAHook", 		"RX3DlgDenoiser",		"RX3DlgDenoiser",			true,				true ),
	iZRX3DereverbSAHook					( "iZRX3DereverbSAHook", 				"RX3Dereverb", 			"RX3Dereverb",				true,				true ),
	iZRX3HumRemovalSAHook				( "iZRX3HumRemovalSAHook", 				"RX3HumRemoval", 		"RX3HumRemoval",			true,				true ),
	iZRX3SpectralRepairSAHook			( "iZRX3SpectralRepairSAHook", 			"RX3SpectralRepair", 	"RX3SpectralRepair",		true,				true ),
	// Post GYP RX 3
	RXDeclickerPluginSAHook				( "RXDeclickerPluginSAHook", 			"RX3Declicker", 		"RX3Declicker",				true,				true ),
	RXDeclipperPluginSAHook				( "RXDeclipperPluginSAHook", 			"RX3Declipper", 		"RX3Declipper",				true,				true ),
	RXDecracklerPluginSAHook			( "RXDecracklerPluginSAHook", 			"RX3Decrackler", 		"RX3Decrackler",			true,				true ),
	RXDenoiserPluginSAHook				( "RXDenoiserPluginSAHook", 			"RX3Denoiser", 			"RX3Denoiser",				true,				true ),
	RXDenoiserZLPluginSAHook			( "RXDenoiserZLPluginSAHook", 			"RX3DenoiserZL", 		"RX3DenoiserZL",			true,				true ),
	RXHumRemovalPluginSAHook			( "RXHumRemovalPluginSAHook", 			"RX3HumRemoval", 		"RX3HumRemoval",			true,				true ),
	RXSpectralRepairPluginSAHook		( "RXSpectralRepairPluginSAHook", 		"RX3SpectralRepair", 	"RX3SpectralRepair",		true,				true ),

	
	RX4DeclickerPluginSAHook			( "RXDeclickerPluginSAHook", 			"RX4Declicker", 		"RX4Declicker",				true,				true ),
	RX4DeclipperPluginSAHook			( "RXDeclipperPluginSAHook", 			"RX4Declipper", 		"RX4Declipper",				true,				true ),
	RX4DecracklerPluginSAHook			( "RXDecracklerPluginSAHook", 			"RX4Decrackler", 		"RX4Decrackler",			true,				true ),
	RX4DenoiserPluginSAHook				( "RXDenoiserPluginSAHook", 			"RX4Denoiser", 			"RX4Denoiser",				true,				true ),
	RX4DenoiserZLPluginSAHook			( "RXDenoiserZLPluginSAHook", 			"RX4DenoiserZL", 		"RX4DenoiserZL",			true,				true ),
	RX4HumRemovalPluginSAHook			( "RXHumRemovalPluginSAHook", 			"RX4HumRemoval", 		"RX4HumRemoval",			true,				true ),
	RX4SpectralRepairPluginSAHook		( "RXSpectralRepairPluginSAHook", 		"RX4SpectralRepair", 	"RX4SpectralRepair",		true,				true ),
	
	Trash2SAHook						( "iZTrash2SAHook", 					"Trash 2", 				"Trash2",					true,				true ),
	VinylSAHook							( "iZVinylPluginSAHook", 				"Vinyl", 				"Vinyl",					true,				true ),
	VinylSAHookMac						( "iZVinylSAHook", 						"Vinyl", 				"Vinyl",					true,				true ),

	UNKNOWN			("UNKNOWN", "", "", false, false );
	
	private String m_strAppName;
	private String m_strPluginName;
	private String m_strShortPluginName;
	private boolean	m_bHasAppRPCServer;
	private boolean m_bHasPluginRPCServer;
	
	private HostType( String strAppName, String strPluginName, String strInternalName, boolean bHasAppRPCServer, boolean bHasPluginRPCServer ) {
		this.m_strAppName = strAppName;
		this.m_strPluginName= strPluginName;
		this.m_strShortPluginName= strInternalName;
		this.m_bHasAppRPCServer= bHasAppRPCServer;
		this.m_bHasPluginRPCServer= bHasPluginRPCServer;
	}
	
	public String _GetAppName() {
		return this.m_strAppName;
	}
	
	public String _GetPluginName() {
		return this.m_strPluginName;
	}

	public String _GetInternalName() {
		return this.m_strShortPluginName;
	}
	
	public boolean _HasAppRPCServer() {
		return this.m_bHasAppRPCServer;
	}

	public boolean _HasPluginRPCServer() {
		return this.m_bHasPluginRPCServer;
	}
	
	public boolean _IsSAHook() {
		return this.m_strAppName.endsWith( "SAHook" );
	}

	/**
     * Returns the enum corresponding to the passed in string
     *
     * @param hostname the app/exe/Host value to convert to an Enum
     * @return the enum
     * @throws Exception
     */
	public static HostType getEnum( String hostname ) throws Exception {
		int sepLoc= hostname.lastIndexOf("/");
		if( sepLoc != -1 )
			hostname= hostname.substring( sepLoc+1 );
				
		// Strip off the extension
		if( hostname.endsWith(".app") ) {
			hostname= hostname.replace(".app", "");
		}
		else {
			hostname= hostname.replace(".exe", "");
		}

		for( HostType val : HostType.values() )
			if( val.m_strAppName.equalsIgnoreCase(hostname) )
				return val;
    	
		throw new Exception( "Unsupported Host Application: " + hostname );
    }

	/**
     * Returns the enum corresponding to the passed in string
     *
     * @param hostname the app/exe/Host value to convert to an Enum
     * @return the enum
     * @throws Exception
     */
	public static HostType getEnumFromPlugin( String strPluginName ) throws Exception {
		for( HostType val : HostType.values() )
			if( (val.m_strPluginName != null      && val.m_strPluginName.equalsIgnoreCase( strPluginName ) ) || 
				(val.m_strShortPluginName != null && val.m_strShortPluginName.equalsIgnoreCase( strPluginName ) ) )
				return val;
    	
		throw new Exception( "Unsupported Plugin: " + strPluginName );
    }

}
