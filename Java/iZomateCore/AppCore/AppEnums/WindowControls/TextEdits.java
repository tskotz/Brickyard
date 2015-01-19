package iZomateCore.AppCore.AppEnums.WindowControls;

public enum TextEdits {
	//RX3
	SelectionStartTime	("Selection start time"),
	SelectionEndTime	("Selection end time"),
	SelectionLength		("Selection length"),
	SelectionStartFreq	("Selection starting frequency"),
	SelectionEndFreq	("Selection ending frequency"),
	SelectionRangeFreq	("Selection bandwidth"),
	MarkerRowNameEdit	("MarkerRow#|MarkerRow Name Edit")
	;
	
	private String m_strValue;
	
	private TextEdits(String strValue) {
		this.m_strValue= strValue;
	}

	public String _getValue() {
		return this.m_strValue;
	}

}
