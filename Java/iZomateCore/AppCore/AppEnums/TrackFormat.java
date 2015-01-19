package iZomateCore.AppCore.AppEnums;

public enum TrackFormat
{
	DEFAULT		("DEFAULT", 0),
	Mono		("Mono", 	1),
	Stereo		("Stereo",	2),
	LCR			("LCR",		3),
	LCRS		("LCRS",	4),
	Quad		("Quad",	5),
	_5_0		("5.0",		6),
	_5_1		("5.1",		7),
	_6_0		("6.0",		8),
	_6_1		("6.1",		9),
	_7_0_SDDS	("7.0 SDDS",10),
	_7_1_SDDS	("7.1 SDDS",11),
	_7_0		("7.0",		12),
	_7_1		("7.1",		13);
	
	private String 	m_strValue;
	private int 	m_nMenuPosition;
	
	private TrackFormat(String strValue, int nPos) {
		this.m_strValue = strValue;
		this.m_nMenuPosition= nPos;
	}
	
	public String _getValue() {
		return this.m_strValue;
	}

	public int _getMenuPos() {
		return this.m_nMenuPosition;
	}
}
