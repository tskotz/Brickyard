package iZomateCore.AppCore.AppEnums;

public enum Insert
{
	A	(1),
	B	(2),
	C	(3),
	D	(4),
	E	(5);
	
	private int mValue;
	
	private Insert(int value)
	{
		this.mValue = value;
	}
	
	public int _getValue()
	{
		return this.mValue;
	}

}
