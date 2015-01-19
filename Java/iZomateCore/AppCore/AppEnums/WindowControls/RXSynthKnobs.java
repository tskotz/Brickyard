package iZomateCore.AppCore.AppEnums.WindowControls;

public enum RXSynthKnobs
{
	CourseTune				("Course Tune Knob"),
	FineTune				("Fine Tune Knob"),
	Gain					("Gain Knob"),
	Pan						("Pan Knob"),
	LFODepth				("LFO Depth Knob"),
	LFORate					("LFO Rate Knob"),
	LFODelay				("LFO Delay Knob"),
	DistortionWetDry		("Distortion Wet/Dry Knob"),
	ChorusWetDry			("Chorus Wet/Dry Knob"),
	DelayWetDry				("Delay Wet/Dry Knob"),
	ReverbWetDry			("Reverb Wet/Dry Knob"),
	GlideTime				("Glide Time Knob"),
	FilterKeyboardTracking	("Filter Keyboard Tracking Knob"),
	VelocityAmount			("Velocity Amount Knob"),
	AftertouchAmount		("Aftertouch Amount Knob"),
	UNKNOWN					("UNKNOWN");
	
	private String mValue;
	
	private RXSynthKnobs(String value)
	{
		this.mValue = value;
	}
	
	public String _getValue()
	{
		return this.mValue;
	}

}
