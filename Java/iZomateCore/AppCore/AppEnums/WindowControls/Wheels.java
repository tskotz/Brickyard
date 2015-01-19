package iZomateCore.AppCore.AppEnums.WindowControls;

public enum Wheels
{
	Pitch			("Pitch Wheel"),
	Mod				("Mod Wheel"),
	UNKNOWN			("UNKNOWN");
	
	private String mValue;
	
	private Wheels(String value)
	{
		this.mValue = value;
	}
	
	public String _getValue()
	{
		return this.mValue;
	}

}
