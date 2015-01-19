package iZomateCore.AppCore;

import iZomateCore.AppCore.AppEnums.WindowControls.*;
import iZomateCore.AppCore.Callbacks.EditBoxCallback;
import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.EventSubType;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.NotificationCallback;
import iZomateCore.ServerCore.RPCServer.IncomingReply;
import iZomateCore.ServerCore.RPCServer.OutgoingRequest;
import iZomateCore.ServerCore.RPCServer.RPCServer;
import iZomateCore.ServerCore.RPCServer.RemoteServer.RemoteFile;
import iZomateCore.TestCore.Testbed;
import iZomateCore.UtilityCore.TimeUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;

public class WindowControls
{
	private RPCServer 		mTargetRPCServer;
	private Testbed		 	mTestbed;
	private	int				mYOffset= 0; //TODO: Hack Alert.  Remove this when mac dll is fixed
	
	static private ClickSpot sPrevClickSpot= ClickSpot.Bottom;

			
	public WindowControls( RPCServer pRPCServer, Testbed pTestbed ) {
		this.mTargetRPCServer= 	pRPCServer;
		this.mTestbed= 	pTestbed;
	}
		
	public void _setYOffset( int offset ) {
		//TODO: Hack Alert!  Try to fix this in the plugin dll instead of this hack
		this.mYOffset= offset;
	}
	
	//-----------------------------------
	//       Accessor Methods
	//-----------------------------------

	/**
	 * Provides methods for interacting with buttons.
	 *
	 * @return the Buttons object
	 */
	public Button _Button(Buttons button) {
		return new Button( new String[] {button._getValue()} );
	}

	public Button _Button(String button) {
		return new Button( new String[] {button} );
	}

	/**
	 * Provides methods for interacting with buttons.
	 *
	 * @return the Buttons object
	 */
	public ComboBox _ComboBox(String cbox) {
		return new ComboBox( new String[] {cbox} );
	}

	/**
	 * Provides methods for interacting with DualListBoxes.
	 *
	 * @return the Buttons object
	 */
	public DualListBox _DualListBox(DualListBoxes dualListBox) {
		return new DualListBox( new String[] {dualListBox._getValue()} );
	}

	/**
	 * Provides methods for interacting with FileExplorerView.
	 *
	 * @return the FileExplorerView object
	 */
	public FileExplorerView _FileExplorerView(FileExplorerViews fileExplorerView) {
		return new FileExplorerView( new String[] {fileExplorerView._getValue()} );
	}

	/**
	 * Provides methods for interacting with buttons.
	 *
	 * @return the Buttons object
	 */
	public Knob _Knob(RXSynthKnobs knob) {
		return new Knob( new String[] {knob._getValue()} );
	}

	/**
	 * Provides methods for interacting with sliders.
	 *
	 * @return the Slider object
	 */
	public Slider _Slider( Sliders s ) {
		return new Slider( new String[] {s._getValue()} );
	}

	/**
	 * Provides methods for interacting with text edit boxes.
	 *
	 * @return the Text Edit object
	 */
	public TextEdit _TextEdit( TextEdits te ) {
		return this._TextEdit( te._getValue() );
	}

	/**
	 * Provides methods for interacting with text edit boxes.
	 *
	 * @return the Text Edit object
	 */
	public TextEdit _TextEdit( String te ) {
		return new TextEdit( new String[] {te} );
	}

	/**
	 * Provides methods for interacting with buttons.
	 *
	 * @return the Buttons object
	 */
	public Wheel _Wheel(Wheels wheel) {
		return new Wheel( new String[] {wheel._getValue()} );
	}

	//-----------------------------------
	//       Base Widget Class
	//-----------------------------------

	public class widget
	{
		protected 	String[] 				mWidgetHierarchy;
		protected 	ButtonState				mButtonState = ButtonState.Ignore;
		protected 	ClickSpot 				mClickSpot = ClickSpot.Center;
		protected	WidgetInfo				mInfo= null;
		protected	int 					mTimeOut = TimeUtils._GetDefaultTimeout();
		protected	EventSubType 			mEventType= null;
		protected	String 					mStrEventMsg= null;
		protected	NotificationCallback	mCallback= null;
		protected	Point					mPoint= null;
		
		private widget( String[] widgetHierarchy ) { 
			this.mWidgetHierarchy= widgetHierarchy;
		}
		
		public widget _setEventNotification( EventSubType eventType, String strEventMsg ) {
			this.mEventType= eventType;
			this.mStrEventMsg= strEventMsg;
			return this;
		}
		
		public widget _setTimeOut( int seconds )  {
			this.mTimeOut= seconds;
			return this;
		}
		
		public widget _setClickPoint( Point p ) {
			this.mPoint= p;
			return this;
		}
		
		public widget _setClickPointOffset(ClickSpot clickSpot, int offset  ) throws Exception {
			if( this.mInfo == null )
				this.mInfo = this._info();
			
			if (clickSpot == ClickSpot.Left)
				this.mInfo.mClickOffsetL= offset;
			else if (clickSpot == ClickSpot.Top)
				this.mInfo.mClickOffsetT= offset;
			else if (clickSpot == ClickSpot.Bottom)
				this.mInfo.mClickOffsetB= offset;
			else if (clickSpot == ClickSpot.Right)
				this.mInfo.mClickOffsetR= offset;
			return this;
		}		
		
		public widget _setClickSpot( ClickSpot clickSpot ) {
			this.mClickSpot= clickSpot;
			return this;
		}

		public void _click() throws Exception {
			if( this.mEventType == null )
				this._setEventNotification( EventSubType.MouseEvent, "LeftUp" );
			this._clickImpl();
		}

		protected void _click( Point p ) throws Exception {
			if( this.mEventType == null )
				this._setEventNotification( EventSubType.MouseEvent, "LeftUp" );
			this._setClickPoint( p )._clickImpl();
		}

		public void _click( EventSubType eventType, String strEventMsg ) throws Exception {
			this._setEventNotification( eventType, strEventMsg )._clickImpl();
		}
		
		protected void _clickImpl() throws Exception {
			if( this.mInfo == null )
				this.mInfo = this._info();

			if (this.mButtonState == ButtonState.Ignore || this.mButtonState != this.mInfo.mIsOn) {
				if( this.mPoint == null )
					this.mPoint= this.mInfo._point( this.mClickSpot );
							
				WindowControls.this.mTestbed._Robot()._mouseClick(this.mTimeOut, this.mPoint, 1, MouseButtons.Left);
				
				if( this.mEventType != null ) {
					// Add the coords if LeftUp or LeftDown
					if( this.mStrEventMsg.equals( "LeftUp" ) || this.mStrEventMsg.equals( "LeftDown" ) ) 
						this.mStrEventMsg= this.mStrEventMsg + "(" + this.mPoint.x + "," + (this.mPoint.y+WindowControls.this.mYOffset) +")";
					
					WindowControls.this.mTargetRPCServer._waitForEvent( this.mEventType, this.mStrEventMsg, this.mCallback, this.mTimeOut);				
				}
			}
		}
		
		public String _GrabScreenImage( String strDir ) throws Exception {
			WidgetInfo info= this._info();
			String fname= Arrays.deepToString( this.mWidgetHierarchy );
			return WindowControls.this.mTestbed._Robot()._imageGrabAndSave( new Rectangle(info.mX, info.mY, info.mWidth, info.mHeight), strDir, fname );
		}
		
		public WidgetInfo _info() throws Exception {
			try {
				OutgoingRequest req = WindowControls.this.mTargetRPCServer._createRequest("getWidgetInfo");
				for (String s : this.mWidgetHierarchy)
					req._addString(s, "widget");
				req._setTimeoutVal( this.mTimeOut );
				return new WidgetInfo( WindowControls.this.mTargetRPCServer._processRequest(req) );
			}
			catch( Exception e ) {
				String strWidget= "";
				for (String s : this.mWidgetHierarchy)
					strWidget+= (strWidget.isEmpty()?"":":") + s;
				throw new Exception( e.getMessage().replaceFirst( "Widget", "Widget '" + strWidget + "'" ) );
			}
		}

		public widget _setTO( int timeout ) throws Exception {
			this.mTimeOut= timeout;
			return this;
		}
		
		public widget _SetCallback( NotificationCallback callback ) {
			this.mCallback= callback;
			return this;
		}

	}

	//-----------------------------------
	//          Button
	//-----------------------------------

	/**
	 * Button methods.
	 */
	public class Button extends widget
	{
		EventSubType eventType= null;
		String strEventMsg= null;
		
		/**
		 * Constructor.
		 */
		private Button( String[] widgetHierarchy ) { 
			super( widgetHierarchy );
		}
		
		public Button _SetCallback( NotificationCallback callback ) {
			super._SetCallback( callback );
			return this;
		}
		
		public Button _SetEventNotification( EventSubType eventType, String strEventMsg ) {
			super._setEventNotification( eventType, strEventMsg );
			return this;
		}

		public void _setState(ButtonState buttonState) throws Exception {
			this._setState( buttonState, ClickSpot.Center );
		}

		public void _setState( ButtonState buttonState, ClickSpot clickSpot ) throws Exception {
			this.mButtonState = buttonState;
			this.mClickSpot = clickSpot;
			this._click();
			
			// If we didn't have to click it then just call the callback directly.  i.e Global Preset Manager window
			if( this.mCallback != null && this.mButtonState == ButtonState.ON && this.mInfo.mIsOn == ButtonState.ON )
				this.mCallback._Callback( this.mCallback._GetTitle(), this.mCallback._GetID(), this.mCallback._GetMessage(), null, this.mCallback._GetIntValue() );
		}
	}
	
	/**
	 * 
	 * ComboBox methods.
	 */
	public class ComboBox extends widget
	{
		private String mItem;

		/**
		 * Constructor.
		 */
		private ComboBox( String[] widgetHierarchy ) { 
			super( widgetHierarchy );
		}
		
		/**
		 * 
		 * @param item
		 * @throws Exception
		 */
		public void _Select(String item) throws Exception
		{
			this.mItem= item;
			this.mInfo= this._info();
			if( !item.equals(this.mInfo.mSelectedItem) ) {
				if( !this.mInfo.mCBoxItems.contains( item ))
					throw new Exception( "Then item '" + item + "' was not found in the ComboBox" );
				// Cycle between left -> Center -> Right -> left --> center -> right -> etc
				WindowControls.sPrevClickSpot= WindowControls.sPrevClickSpot.equals( ClickSpot.Left ) ? ClickSpot.Center : (WindowControls.sPrevClickSpot.equals( ClickSpot.Center ) ? ClickSpot.Right : ClickSpot.Left);
				// We don't seem to get a LeftUp event so use the LeftDown instead
				this._setEventNotification( EventSubType.MouseEvent, "LeftDown" )._SetCallback( new ComboBoxCB( this.mItem, this ) )._setClickSpot( WindowControls.sPrevClickSpot )._clickImpl();
			}
		}
		
		private class ComboBoxCB extends NotificationCallback {
			private	String		m_strItem;
			private ComboBox	m_pComboBox;
			
			
			public ComboBoxCB( String strItem, ComboBox pComboBox ) {
				this.m_strItem= strItem;
				this.m_pComboBox= pComboBox;

				// Override only the values that we care about
				this.m_pNotificationType= 	NotificationType.EVNT;
				this.m_strID= 				"MouseEvent";
				//this.m_strMessage= 		"Invoking";
			}

			@Override
			public CBStatus _Callback( String title, String id, String message, String[] buttons, int value ) throws Exception {
				int iTarget= -1;
				int iCurrent= -1;
				int keyCode= KeyEvent.VK_DOWN;
				
				if( this.m_pComboBox.mInfo.mSelectedItem.equals( "<no item selected>" ))
					iCurrent= 0;
				
				for( int i= 0; i < this.m_pComboBox.mInfo.mCBoxItems.size(); ++i ) {
					if( iCurrent == -1 && this.m_pComboBox.mInfo.mCBoxItems.get( i ).equals( this.m_pComboBox.mInfo.mSelectedItem )) {
						keyCode= KeyEvent.VK_DOWN;
						iCurrent= i+1;
					}
					else if( iTarget == -1 && this.m_pComboBox.mInfo.mCBoxItems.get( i ).equals( this.m_strItem )) {
						iTarget= i+1;
					}
					
					if( iTarget != -1 && iCurrent != -1 )
						break;
				}
				
				int iMoves= iTarget-iCurrent;
				int iNumItems= this.m_pComboBox.mInfo.mCBoxItems.size();
				int iDirectionThresh= iNumItems==0 ? 0 : (iNumItems==1 ? 1 : iNumItems/2);
				if( Math.abs( iMoves ) > iDirectionThresh ) {
					if( iCurrent == 0 )
						iNumItems+= 2; // This accounts for the behavior when nothing is selected

					iMoves= (iNumItems - Math.abs( iMoves )) * (iMoves<0?1:-1); // Reverse direction
				}
				
				if( iMoves < 0 )
					keyCode= KeyEvent.VK_UP;

				iMoves= Math.abs( iMoves );
				while ( iMoves-- > 0 )
					WindowControls.this.mTestbed._Robot()._keyType( keyCode );
				WindowControls.this.mTestbed._Robot()._keyType( KeyEvent.VK_ENTER );
				WindowControls.this.mTargetRPCServer._waitForEvent( EventSubType.KeyboardEvent, "KeyPress", null, TimeUtils._GetDefaultTimeout());


				if( !this.m_pComboBox._info().mSelectedItem.equals( this.m_strItem ) ) {
					//Try again after a little sleep
					TimeUtils.sleep( 1 );
					if( !this.m_pComboBox._info().mSelectedItem.equals( this.m_strItem ) ) 
						throw new Exception( "Failed to select " + this.m_strItem + " from ComboBox: " + this.m_pComboBox.mWidgetHierarchy );
				}
				return CBStatus.HANDLED;
			}

		}


	}

	//-----------------------------------
	//          DualListBox
	//-----------------------------------

	/**
	 * DualListBox methods.
	 */
	public class DualListBox extends widget
	{
		/**
		 * Constructor.
		 */
		private DualListBox( String[] widgetHierarchy ) { 
			super( widgetHierarchy );
		}
		
		public void _select(String item, Side side) throws Exception
		{
			Point p= this._EnsureVisible(item, side);
			this._click( p );
		}

		public Set<String> _getContents(Side side) throws Exception
		{
			if (side == Side.Left)
				return this._info().mDLBLeftItems.keySet();
			else
				return this._info().mDLBRightItems.keySet();
		}
		
		public Point _EnsureVisible( String item, Side side ) throws Exception {
			OutgoingRequest req = WindowControls.this.mTargetRPCServer._createRequest("dualListBoxEnsureVis");
			for (String s : this.mWidgetHierarchy)
				req._addString(s, "widget");
			req._addString(item, "item");
			req._addBoolean(side==Side.Left, "leftSide");
			req._setTimeoutVal( this.mTimeOut );
			IncomingReply reply= WindowControls.this.mTargetRPCServer._processRequest(req);
			return new Point(reply._getInt32("x"), reply._getInt32("y"));
		}
	}

	//-----------------------------------
	//          FileExplorerView
	//-----------------------------------

	/**
	 * FileExplorerView methods.
	 */
	public class FileExplorerView extends widget
	{
		/**
		 * Constructor.
		 */
		private FileExplorerView( String[] widgetHierarchy ) { 
			super( widgetHierarchy );
		}
		
		public void _select(String item) throws Exception
		{
			Point p= this._EnsureVisible(item);
			this._click( p );
		}

		public String[] _getContents() throws Exception
		{
			RemoteFile rootDirFile= WindowControls.this.mTestbed._CreateRemoteFile( this._info().mRootDir );
			return rootDirFile._list( true /*recursive*/ );
		}
		
		public Point _EnsureVisible( String item ) throws Exception {
			OutgoingRequest req = WindowControls.this.mTargetRPCServer._createRequest("fileExplorerViewEnsureVis");
			for (String s : this.mWidgetHierarchy)
				req._addString(s, "widget");
			req._addString(item, "item");
			req._setTimeoutVal( this.mTimeOut );
			IncomingReply reply= WindowControls.this.mTargetRPCServer._processRequest(req);
			return new Point(reply._getInt32("x"), reply._getInt32("y"));
		}
	}

	//-----------------------------------
	//          Knobs
	//-----------------------------------

	/**
	 * Knob methods.
	 */
	public class Knob extends widget
	{
		/**
		 * Constructor.
		 */
		private Knob( String[] widgetHierarchy ) { 
			super( widgetHierarchy );
		}
		
		public widget _Label()
		{
			this.mWidgetHierarchy[this.mWidgetHierarchy.length-1] += " Label Text";
			return this;
		}
		
		public widget _ReadOut()
		{
			this.mWidgetHierarchy[this.mWidgetHierarchy.length-1] += " Readout Text";
			return this;
		}

		public widget _Control()
		{
			this.mWidgetHierarchy[this.mWidgetHierarchy.length-1] += " MIDI Map Control";
			return this;
		}

	}

	//-----------------------------------
	//          Sliders
	//-----------------------------------

	/**
	 * Slider methods.
	 */
	public class Slider extends widget {
		/**
		 * Constructor.
		 */
		private Slider( String[] widgetHierarchy ) {
			super( widgetHierarchy );
		}

		public void _MoveToValue( float fNewValue, int nMaxPixelsPerStep ) throws Exception {
			// First get the value of our slider and its global position
			float fCurrentValue= this._getValue();
			Point screenPos= this._getScreenPos();

			// Now find our start point and end point in slider local coordinates
			Point startPoint= this._getMousePosFromValue( fCurrentValue );
			Point endPoint=   this._getMousePosFromValue( fNewValue );

			// Translate our slider local coordinates to screen coordinates
			startPoint.translate(screenPos.x, screenPos.y);
			endPoint.translate(screenPos.x, screenPos.y);

			// Move the slider
			WindowControls.this.mTestbed._Robot()._mouseDragAndDrop( startPoint, endPoint, nMaxPixelsPerStep, true, true );
		}

		private Point _getMousePosFromValue( float fValue ) throws Exception {
			OutgoingRequest req = WindowControls.this.mTargetRPCServer._createRequest("windowAutomationRequest");
			for( String s : this.mWidgetHierarchy )
				req._addString( s, "widget" );
			req._addString( "GetMouseCoordFromValue", "request" );
			req._addFloat( fValue, "fValue" );
			req._setTimeoutVal( this.mTimeOut );

			IncomingReply pReply= WindowControls.this.mTargetRPCServer._processRequest(req);
			return new Point( pReply._getInt32( "x" ), pReply._getInt32( "y") );
		}

		private float _getValue() throws Exception {
			OutgoingRequest req = WindowControls.this.mTargetRPCServer._createRequest("windowAutomationRequest");
			for( String s : this.mWidgetHierarchy )
				req._addString( s, "widget" );
			req._addString( "GetValue", "request" );
			req._setTimeoutVal( this.mTimeOut );

			IncomingReply pReply= WindowControls.this.mTargetRPCServer._processRequest(req);
			return pReply._getFloat( "fValue" );
		}

		private Point _getScreenPos() throws Exception {
			OutgoingRequest req = WindowControls.this.mTargetRPCServer._createRequest("windowAutomationRequest");
			for( String s : this.mWidgetHierarchy )
				req._addString( s, "widget" );
			req._addString( "GetScreenPos", "request" );
			req._setTimeoutVal( this.mTimeOut );

			IncomingReply pReply= WindowControls.this.mTargetRPCServer._processRequest(req);
			return new Point( pReply._getInt32( "x" ), pReply._getInt32( "y") );
		}
	}

	//-----------------------------------
	//          Text Edit
	//-----------------------------------

	/**
	 * TextEdit methods.
	 */
	public class TextEdit extends widget {
		/**
		 * Constructor.
		 */
		private TextEdit( String[] widgetHierarchy ) {
			super( widgetHierarchy );
		}
		
		public String _SetText( String strText ) throws Exception {
			if( strText == null || strText.isEmpty() )
				return strText;
			
			this.mInfo= this._info();
			//Check current text
			if( !strText.equals( this.mInfo.mText) ) {
				this._SetCallback( new EditBoxCallback( strText + (strText.contains( "\n" ) ? "" : "\n"), WindowControls.this.mTestbed._HostApp() ) );
				this._click( EventSubType.MouseEvent, "LeftDown" );
				// Validate
				this.mInfo= this._info();
				if( !strText.equals( this.mInfo.mText) )
					throw new Exception( "Tried to set edit box '" + this.mInfo.mHierarchy + "' text to '" + strText + "' but it returned '" + this.mInfo.mText + "' instead" );
			}
			return this.mInfo.mText;
		}

	}
	
	//-----------------------------------
	//          Wheel
	//-----------------------------------

	/**
	 * Wheel methods.
	 */
	public class Wheel extends widget
	{
		/**
		 * Constructor.
		 */
		private Wheel( String[] widgetHierarchy ) { 
			super( widgetHierarchy );
		}

		public widget _Label()
		{
			//this.mWidgetHierarchy[this.mWidgetHierarchy.length-1] += " Wheel Label";
			this.mWidgetHierarchy[this.mWidgetHierarchy.length-1] = "Wheel Label";
			return this;
		}
	}

	//-----------------------------------
	//          Widget info
	//-----------------------------------

	/**
	 * Info Class.
	 */
	public class WidgetInfo
	{
		//Generic
		public String 	mClassname;
		public String	mHierarchy;
		public int 		mX;
		public int 		mY;
		public int 		mWidth;
		public int 		mHeight;
		public boolean 	mVisible;
		public boolean 	mEnabled;
		public boolean 	mFocus;
		
		// Click offset for clicking Top, Bottom, Left or right. See _point()
		public int		mClickOffsetL= 4;
		public int		mClickOffsetR= 4;
		public int		mClickOffsetT= 4;
		public int		mClickOffsetB= 4;
		
		//Buttons
		public ButtonState mIsOn = ButtonState.Ignore;
		//ComboBoxes
		public Vector<String> 	mCBoxItems= null;
		public String		mSelectedItem= null;
		public String		mLabel= null;
		//Knobs
		public float 		mValue;
		public float 		mValueMin;
		public float 		mValueMax;
		//DualListBoxes
		public LinkedHashMap<String, Point> 	mDLBLeftItems = null;
		public LinkedHashMap<String, Point> 	mDLBRightItems = null;
		public String							mDLBLeftCurSel = null;
		public String							mDLBRightCurSel = null;
		//RXSynthPresetControl
		public String	mCaption;
		//FileExplorerView
		public String	mRootDir= null;
		//Text Edits and Text objects
		public String	mText= null;
		
		private WidgetInfo(IncomingReply reply) throws Exception 
		{ 
			this.mClassname = reply._getString("classname");
			this.mHierarchy= reply._getString("hierarchy");
			this.mX = reply._getInt32("x");
			this.mY = reply._getInt32("y");
			this.mWidth = reply._getInt32("width");
			this.mHeight = reply._getInt32("height");
			this.mEnabled = reply._getBoolean("enabled");
			this.mVisible = reply._getBoolean("visible");
			this.mFocus = reply._getBoolean("focus");
			
			if (reply._exists("isOn"))
				this.mIsOn = reply._getBoolean("isOn") ? WindowControls.ButtonState.ON : WindowControls.ButtonState.OFF;
			else if (reply._exists("isPressed"))
				this.mIsOn = reply._getBoolean("isPressed") ? WindowControls.ButtonState.ON : WindowControls.ButtonState.OFF;
			
			if (reply._exists("min"))
			{
				this.mValue = 	 reply._getFloat("value");
				this.mValueMin = reply._getFloat("min");
				this.mValueMax = reply._getFloat("max");
			}
			
			if (this.mClassname.equals("UI::RXSynthProperComboBox") || reply._exists( "label" )) {
				this.mSelectedItem= reply._getString( "selectedItem" );
				this.mLabel = reply._getString( "label" );
				this.mCaption = reply._getString("caption");
				
				this.mCBoxItems = new Vector<String>();
				for (int i = 0; i < reply._getCount(); ++i) {							
					if (reply._getName(i).equals( "item" ))
						this.mCBoxItems.add( reply._getString(i) );
				}
			}
			
			if ( reply._exists("rootDir") )
				this.mRootDir= reply._getString( "rootDir" );
			
			if ( reply._exists("selectedItem") )
				this.mSelectedItem= reply._getString( "selectedItem" );

			if ( reply._exists("text") )
				this.mText= reply._getString( "text" );

			if (reply._exists("parentClass"))
			{
				String parent = reply._getString("parentClass");
				if (parent.equals("UI::DualListBox"))
				{
					this.mDLBLeftItems = new LinkedHashMap<String, Point>();
					this.mDLBRightItems = new LinkedHashMap<String, Point>();
					for (int i = 0; i < reply._getCount(); ++i)
					{
						String name= reply._getName(i);
							
						if (name.startsWith("l-item")) {
							if( name.equals("l-item*") )
								this.mDLBLeftCurSel= reply._getString(i);
							this.mDLBLeftItems.put(reply._getString(i), new Point(reply._getInt32(++i), reply._getInt32(++i)));
						}
						else if (name.startsWith("r-item")) {
							if( name.equals("r-item*") )
								this.mDLBRightCurSel= reply._getString(i);
							this.mDLBRightItems.put(reply._getString(i), new Point(reply._getInt32(++i), reply._getInt32(++i)));
						}
					}
				}
				else if (parent.equals("UI::RXSynthPresetControl"))
					this.mCaption = reply._getString("caption");
			}
		}
		
		public Point _point(ClickSpot clickSpot)
		{
			int centerX= this.mX + this.mWidth/2;
			int centerY= this.mY + this.mHeight/2;
			
			//TODO: These numbers are all over the road because of the drawer button rects.  See if we can reign those in.
			if (clickSpot == ClickSpot.Left)
				return new Point(centerX + this.mClickOffsetL, centerY);
			else if (clickSpot == ClickSpot.Top)
				return new Point(centerX, centerY + this.mClickOffsetT);
			else if (clickSpot == ClickSpot.Bottom)
				return new Point(centerX, centerY - this.mClickOffsetB);
			else if (clickSpot == ClickSpot.Right)
				return new Point(centerX - this.mClickOffsetR, centerY);
			
			return new Point(centerX, centerY );
		}
	}

	public class EventInfo
	{
		public NotificationCallback m_Callback;
		public EventSubType m_EventType;
		public String m_EventMessage;
		
		public EventInfo(EventSubType eventType, String eventMessage, NotificationCallback eventCallback)
		{
			this.m_EventType = eventType;
			this.m_EventMessage = eventMessage;
			this.m_Callback = eventCallback;
		}
	}
	
	public enum ButtonState {ON, OFF, Ignore};
	public enum Side {Left, Right};
	public enum ClickSpot {Center, Top, Bottom, Left, Right};
	public enum ClickEventType {Up, Down, None};

}
