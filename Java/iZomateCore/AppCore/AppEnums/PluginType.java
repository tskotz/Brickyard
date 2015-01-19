package iZomateCore.AppCore.AppEnums;

public enum PluginType
{
	Iris	("Iris"),
	Nectar	("Nectar"),
	Ozone5	("Ozone5"),
	Trash2  ("Trash 2");
	
	private String mValue;
	
	private PluginType(String value)
	{
		this.mValue = value;
	}
	
	public String _getValue()
	{
		return this.mValue;
	}

}
