package iZomateCore.AppCore.AppEnums.WindowControls;

public enum DualListBoxes
{
	Preset			("Right Scroll View"),
	UNKNOWN			("UNKNOWN");
	
	private String mValue;
	
	private DualListBoxes(String value)
	{
		this.mValue = value;
	}
	
	public String _getValue()
	{
		return this.mValue;
	}

}
