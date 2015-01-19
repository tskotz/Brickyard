package iZomateCore.AppCore.AppEnums.WindowControls;

public enum Sliders {
	MechanicalNoiseSlider ( "Mechanical Noise Slider" ),
	WearSlider            ( "Wear Slider" ),
	ElectricalNoiseSlider ( "Electrical Noise Slider" ),
	DustSlider            ( "Dust Slider" ),
	ScratchSlider         ( "Scratch Slider" ),
	WarpDepthSlider       ( "Warp Depth Slider" ),
	InputGainlider        ( "Input Gain Slider" ),
	OutputGainSlider      ( "Output Gain Slider" );

	private String mValue;

	private Sliders(String value) {
		this.mValue = value;
	}
	
	public String _getValue() {
		return this.mValue;
	}
}
