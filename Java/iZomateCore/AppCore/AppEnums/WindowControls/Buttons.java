package iZomateCore.AppCore.AppEnums.WindowControls;

import iZomateCore.AppCore.Plugin;

public enum Buttons
{
	UNKNOWN			("UNKNOWN"),
	//GlobalView
	Options			("Options Button"),
	Tutorial		("Tutorial Button"),
	Undo			("Undo Button"),
	Redo			("Redo Button"),
	Solo			("Solo Button"),
	Mute			("Mute Button"),
	Presets			("Preset OnOff Button"),
	PresetNext		("Preset Next Button"),
	PresetPrevious	("Preset Previous Button"),
	OptionsClose	("Close Button"),
	GlobalDrawerButton	("Global Drawer Button"),
	KeyboardDrawerButton("Keyboard Drawer Button"),
	SynthDrawerButton	("Synth Drawer Button"),
	ToolsDrawerButton	("Tools Drawer Button"),
	//GenPickerView Buttons
	Picker1			("1 Full Picker Button"),
	Picker2			("2 Full Picker Button"),
	Picker3			("3 Full Picker Button"),
	PickerSub		("Sub Full Picker Button"),
	PickerAll		("All Full Picker Button"),
	PickerMix		("Mix Full Picker Button"),
	Link1			("1 Full Link Button"),
	Link2			("2 Full Link Button"),
	Link3			("3 Full Link Button"),
	LinkSub			("Sub Full Link Button"),
	MixUndock		("Full Mix Undock Button"),	
	//Tutorial Dialog Buttons
	IntroScreenDismiss	("Intro Screen Dismiss Button"),
	IntroScreenTour		("Intro Screen Dismiss Button"),
	IntroScreenBrowse	("Intro Screen Dismiss Button"),
	//Preset Dialog
	PresetClose			("Preset View|Close Button"),
	// Crash Reporter
	CR_SendToiZotope	("Submit Button"),
	CR_IgnoreError		("Ignore Button"),
	CR_CommentsBox		("Comments Box"),
	
	//Alloy
	GlobalPreset		("Global Preset Button", "RX3:Preset PushButton", "RX4:Preset PushButton", "Nectar2:Genre and Style Text"),
	ModulePreset		("Module Preset Button"),
	AlloyPresetClose	("Preset|Close Button", "Nectar2:Preset Window Base|Close Button"),
	OverView			("Nav Button 7"),
	Equalizer			("Nav Button 0"),
	Transient			("Nav Button 1"),
	Exciter				("Nav Button 3"),
	Dynamics1			("Nav Button 4"),
	Dynamics2			("Nav Button 5"),
	DeEsser				("Nav Button 6"),
	Limiter				("Nav Button 2"),
	Bypass				("Bypass Button"),
	
	//Vinyl
	VinylStereoSwitch	("Stereo Switch"),
	VinylBypassSwitch	("Bypass Switch"),
	
	//RX3
	RX_MarkerRowFindButton	("MarkerRow#|MarkerRow Find Button"),
	RX_OptionTab_Misc		("Misc OptionTab (Index 4) Button"),	
	RX_Ok					("OK Button"),
	RX_Batch_AddFiles		("Batch Process Window|Batch Process Detail|Add Input Files"),
	
	//Common
	Module_BatchButton 		("Batch Button"),
	Module_CompareButton 	("Queue Button"),
	Module_ProcessButton 	("Apply Button"),
	Module_LearnButton 		("Train Button"),
	Module_PreviewButton 	("Preview Button"),	
	;
	
	private String m_strValue;
	private String m_strAltValue[];
	
	/**
	 * 
	 * @param strValue
	 */
	private Buttons(String strValue) {
		this( strValue, new String[]{} );
	}

	/**
	 * 
	 * @param strValue
	 * @param strAltValue
	 */
	private Buttons(String strValue, String... strAltValue) {
		this.m_strValue = strValue;
		this.m_strAltValue= strAltValue;
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
	public Buttons _for( Plugin pPlugin ) throws Exception {
		for( String strAltValue :  this.m_strAltValue ) {
			String strHostPrefix= strAltValue.substring( 0, strAltValue.indexOf( ":" ) );
			if( strHostPrefix != null && pPlugin._GetPluginInfo().m_strShortName.startsWith( strHostPrefix ) ) {
				this.m_strValue= strAltValue.substring( strHostPrefix.length() + 1 );
				break;
			}
		}
		return this;
	}

}
