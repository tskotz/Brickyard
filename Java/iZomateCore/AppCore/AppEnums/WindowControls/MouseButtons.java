package iZomateCore.AppCore.AppEnums.WindowControls;

public enum MouseButtons
{
	Left	( java.awt.event.InputEvent.BUTTON1_MASK ),
	Right	( java.awt.event.InputEvent.BUTTON2_MASK ),
	Center	( java.awt.event.InputEvent.BUTTON3_MASK );
	
	private int mValue;
	
	private MouseButtons( int btnMask ) {
		this.mValue= btnMask;
	}
	
	public int _getValue() {
		return this.mValue;
	}
}
