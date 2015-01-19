package iZomateCore.AppCore.AppEnums;

public enum TrackType
{
	DEFAULT				("DEFAULT", 			0),
	AudioTrack			("Audio Track", 		1),
	AuxTrack			("Aux Input", 			2),
	MasterFader			("Master Fader", 		3),
	VCAMaster			("VCA Master", 			4),
	MIDITrack			("MIDI Track", 			5),
	InstrumentTrack		("Instrument Track", 	6),
	VideoTrack			("Video Track", 		7);
	
	private String 	m_strValue;
	private int 	m_nMenuPosition;
	
	private TrackType(String strValue, int nPos) {
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
