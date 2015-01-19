package iZomateCore.iZTests;

import iZomateCore.LogCore.Logs;
import iZomateCore.ServerCore.RPCServer.RemoteServer.RemoteServer;
import iZomateCore.UtilityCore.SSHCore;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import java.io.File;

public class Sandbox
{
	static String sTestbed;
	static String sLogdir;
	static String sApp = null;
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception
	{	
		sshTest( args );
//		processCmdArgs(args);
//		getMouseImage();
		
//		midi();
		
//		String macIris = "/Applications/iZotope Iris.app";
//		String winIris = "C:/Program Files (x86)/iZotope/Iris/win32/iZotope Iris.exe";
//		String winRelIris = "D:/iZotope/RX_Synth/plugin/build/win_vc100/Win32/Release CRTStatic Exe_Iris/iZotope Iris.exe";
//		String winPT = "C:/Program Files (x86)/Avid/Pro Tools/ProTools.exe";
		
//		if (sApp == null)
//			sApp = winRelIris;
		
//		IrisTest irisTest = new IrisTest( args );
//		irisTest._getProcessInfo("blah");
//		irisTest._run(20);
		
//		PTDemo demo = new PTDemo(sTestbed, winPT, sLogdir);
//		demo._run();		
	}
	
	public static void sshTest( String[] args ) throws Exception {
		//scp -r %RESULTSDIR% iztestauto@buildarchive.izotope.int:/build_archive/%REMOTE_BUILD_FOLDER%
		//ssh iztestauto@buildarchive.izotope.int chmod 774 -R /build_archive/%REMOTE_BUILD_FOLDER%/AutomationResults
//		File f= new File("C:/TestAutomation/Projects/Trash/AutomationResults");
//		String strDestinationFile= "build_archive/iZotope_Neptune/Build-robocop_iii-1225-robocop_iii";
//		String strUserMachine= "iztestauto@buildarchive.izotope.int";
		
		String strUsername= "iztestauto";
		String strRemoteMachine= "test-auto-macpro01.izotope.int";
		strRemoteMachine= "buildarchive.izotope.int";
		
		SSHCore ssh= new SSHCore( strUsername, strRemoteMachine, null );
		String res= ssh._ExecCommand( "cd Desktop" )._ExecCommand( "ls -a" )._GetResult();
		System.out.println( res );
		//ssh._SCP_Put( new File("/Users/tskotz/iZotope/Alloy/redist/Output/readme.txt"), "/Users/iztestauto/Desktop", "0644" );
		ssh._SCP_Get( "/build_archive/Trash/Build-installer-28/AutomationResults/127.0.0.1_Trash2_Trash2_BuildTimes.png", new File( "/Users/tskotz/Desktop" ) );
	}
	
	/**
	 * Pulls out and sets the required command line args
	 * @param args
	 * @throws Exception
	 */
	public static void processCmdArgs(String[] args) throws Exception
	{
		for (int i = 0; i < args.length; ++i)
		{
			if (args[i].equals("-testbed"))
				sTestbed = args[++i];
			else if (args[i].equals("-logDir"))
				sLogdir = args[++i];
			else if (args[i].equals("-app"))
				sApp = args[++i]; //optional
		}

		if (sTestbed == null)
			throw new Exception("-testbed arg not specified");

		if (sLogdir == null)
			throw new Exception("-logDir arg not specified");
	}

	/**
	 * Gets the remote systems mouse image and creates a local file : "/IzoImages/MouseImage.bmp"
	 * @throws Exception
	 */
	public static void getMouseImage() throws Exception
	{
		RemoteServer rs = new RemoteServer(sTestbed, new Logs(sLogdir, "Test", 0, true));
		for (int i = 0; i < 20; ++i)
			rs._Robot()._imageGetMouseImage("/IzoImages", "MouseImage.bmp");
	}
	
	public static void midi() throws Exception
	{
		@SuppressWarnings("unused")
		Info info = null;
		
		for (Info i : MidiSystem.getMidiDeviceInfo())
		{
			System.out.println(i.getName() + " Desr: " + i.getDescription() + " Vendor: " + i.getVendor() + " Version: " + i.getVersion());
			if (i.getName().equals("General"))
			{
				info = i;
				//break;
			}
		}

		MidiDevice dev = null;
		for (Info i : MidiSystem.getMidiDeviceInfo())
		{
    		if (i.getName().equals("RPCServer MIDI Input"))
    		{
    			dev = MidiSystem.getMidiDevice(i);
    			if (!dev.isOpen())
    				dev.open();
    			break;
    		}
		}
		
		ShortMessage msg = new ShortMessage();		  
		msg.setMessage(ShortMessage.NOTE_ON, 0, 60, 111);
		dev.getReceiver().send(msg, -1L /*timeStamp*/);
		msg.setMessage(ShortMessage.NOTE_OFF, 0, 60, 111);
		dev.getReceiver().send(msg, -1L /*timeStamp*/);
		
				
		int nVelocity = 50;
		int nDuration = 250;
		//MidiDevice device = MidiSystem.getMidiDevice(info);

		
		long timeStamp = -1;
		ShortMessage myMsg = new ShortMessage();
		Receiver rcvr = MidiSystem.getReceiver();
		  
		//Note 60 == Middle C 
		int notes[] = {57,59,61,63,64};

		myMsg.setMessage(ShortMessage.NOTE_ON, 0, 70, nVelocity);
	//	int c = ShortMessage.NOTE_ON;
		int c2 = myMsg.getCommand();
		myMsg.setMessage(c2, 0, 70, nVelocity);
		
		for (int i = 0; 1 < 2; i++)
		{	
			int cmd = i==0?ShortMessage.NOTE_ON:ShortMessage.NOTE_OFF;
			for (int note : notes)
			{
				myMsg.setMessage(cmd, 0, note, nVelocity);
				rcvr.send(myMsg, timeStamp);
				Thread.sleep(nDuration);
			}
			Thread.sleep(1000);
		}

	}
	
	public void _izoMgrTest() throws Exception {
//		TestCallback testcb = new TestCallback();
				
/*		WidgetInfo info = this.mWindowControls._DualListBox(DualListBoxes.Preset)._info();
		for (int i = 0; i < 20; i++)
			for (String entry: info.mDLBRightItems.keySet())
				this.mWindowControls._Button(Buttons.Presets)._SetCallback( new PresetsCallback(null, entry, 0))._click();
		
		this.mWindowControls._Knob(RXSynthKnobs.VelocityAmount)._click();
		this.mWindowControls._Knob(RXSynthKnobs.VelocityAmount)._Label()._click();
		this.mWindowControls._Knob(RXSynthKnobs.VelocityAmount)._ReadOut()._click();
		this.mWindowControls._Knob(RXSynthKnobs.VelocityAmount)._Control()._click();
		
		this.mWindowControls._Knob(RXSynthKnobs.FilterKeyboardTracking)._click();
		this.mWindowControls._Knob(RXSynthKnobs.FilterKeyboardTracking)._Label()._click();
		this.mWindowControls._Knob(RXSynthKnobs.FilterKeyboardTracking)._ReadOut()._click();
		this.mWindowControls._Knob(RXSynthKnobs.FilterKeyboardTracking)._Control()._click();
		
		this.mWindowControls._Button(Buttons.Link1)._setState(ButtonState.ON);
		this.mWindowControls._Button(Buttons.Link1)._setState(ButtonState.ON);
		this.mWindowControls._Button(Buttons.Link1)._setState(ButtonState.OFF);
		this.mWindowControls._Button(Buttons.Link2)._click();
		this.mWindowControls._Button(Buttons.Link3)._click();
		this.mWindowControls._Button(Buttons.LinkSub)._click();
		this.mWindowControls._Button(Buttons.MixUndock)._click();
		this.mWindowControls._Button(Buttons.Mute)._click();
		this.mWindowControls._Button(Buttons.Solo)._click();
		this.mWindowControls._Button(Buttons.Picker1)._click();
		this.mWindowControls._Button(Buttons.Picker2)._click();
		this.mWindowControls._Button(Buttons.Picker3)._click();
		this.mWindowControls._Button(Buttons.PickerAll)._click();
		this.mWindowControls._Button(Buttons.PickerMix)._click();
		this.mWindowControls._Button(Buttons.PickerSub)._click();
		this.mWindowControls._Button(Buttons.Redo)._click();
		this.mWindowControls._Button(Buttons.Undo)._click();
		this.mWindowControls._Button(Buttons.Options)._click();
		this.mWindowControls._Button(Buttons.OptionsClose)._click();
		this.mWindowControls._Button(Buttons.Tutorial)._click();
		this.mWindowControls._Button(Buttons.IntroScreenDismiss)._click();
		
		this.mWindowControls._Wheel(Wheels.Pitch)._click();
		this.mWindowControls._Wheel(Wheels.Pitch)._Label()._click();

		this.mWindowControls._Wheel(Wheels.Mod)._click();
		this.mWindowControls._Wheel(Wheels.Mod)._Label()._click();

		this.mWindowControls._Button(Buttons.MixUndock)._setState(ButtonState.OFF);
		this.mWindowControls._Button(Buttons.MixUndock)._SetCallback( new TestCallback() )._click();
		this.mWindowControls._Button(Buttons.MixUndock)._setState(ButtonState.OFF);

		
		System.out.println("\nTest 0");

		System.out.println("\nTest 1");
		OutgoingRequest req2;
		IncomingReply reply = null;

		req2 = this.m_pAppServer._createRequest("RPC_NotificationTest");
		req2._setEventNotification(EventType.TestEventNotification, testcb);
		reply = this.m_pAppServer._processRequest(req2);
		
		System.out.println("\nTest 2");
		req2 = this.m_pAppServer._createRequest("RPC_NotificationTest");
		req2._setEventNotification(EventType.TestEventNotification, "Ending Test", testcb);
		reply = this.m_pAppServer._processRequest(req2);

		System.out.println("\nTest 3");
		req2 = this.m_pAppServer._createRequest("RPC_NotificationTest");
		req2._setEventNotification("TestEventNotificationII", "Hello World", testcb);
		reply = this.m_pAppServer._processRequest(req2);

		System.out.println("\nTest 4");
		req2 = this.m_pAppServer._createRequest("RPC_NotificationTest");
		req2._setEventNotification("TestEventNotificationII", testcb);
		reply = this.m_pAppServer._processRequest(req2);

		OutgoingRequest req = this.m_pAppServer._createRequest("TestHook");
		req._addString("HelloWorld", "stringParam");
		
		reply = this.m_pAppServer._processRequest(req);
		String s = reply._getString("stringParam");
				
		this.m_pAppServer._createAndProcessRequest("RPC_ExceptionTest");
		
		reply = this.m_pAppServer._processRequest(req);
		*/
	}
	
	public void _appCheck() throws Exception {
/*		if( this.m_pAppServer._isConnected() ) {
			String testStr = "Hello World";
			boolean testTrue = true;
			boolean testFalse = false;
			short testInt16Min = -32768;
			short testInt16Max = 32767;
			short testUInt16Min = 0;
			int testUInt16Max = 65535;
			int testInt32Min = -2147483648;
			int testInt32Max = 2147483647;
			long testUInt32Min = 0;
			long testUInt32Max = 4294967295L;
			long testInt64Min = -9223372036854775808L;
			long testInt64Max = 9223372036854775807L;
			BigInteger testUInt64Min = BigInteger.valueOf(0);
			BigInteger testUInt64Max = new BigInteger("18446744073709551615");
			float testFloatPos = (float)12345.6789;
			float testFloatNeg = (float)-98765.4321;
			double testDoublePos = 112345.6789;
			double testDoubleNeg = -998765.4321;
			byte[] testBuff = new byte[]{1,2,3,4,5,6,7,8,9};
			
			
			//Test App
			OutgoingRequest req = this.m_pAppServer._createRequest("RPC_RoundTripTest");
			req._addString(testStr, "stringParam");
			req._addBoolean(testTrue, "true");
			req._addBoolean(testFalse, "false");
			req._addInt16(testInt16Min, "int16Min");
			req._addInt16(testInt16Max, "int16Max");
			req._addUInt16(testUInt16Min, "uint16Min");
			req._addUInt16(testUInt16Max, "uint16Max");
			req._addInt32(testInt32Min, "int32Min");
			req._addInt32(testInt32Max, "int32Max");
			req._addUInt32(testUInt32Min, "uint32Min");
			req._addUInt32(testUInt32Max, "uint32Max");
			req._addInt64(testInt64Min, "int64Min");
			req._addInt64(testInt64Max, "int64Max");
			req._addUInt64(testUInt64Min, "uint64Min");
			req._addUInt64(testUInt64Max, "uint64Max");
			req._addFloat(testFloatPos, "floatPos");
			req._addFloat(testFloatNeg, "floatNeg");
			req._addDouble(testDoublePos, "doublePos");
			req._addDouble(testDoubleNeg, "doubleNeg");
			req._addBuffer(testBuff, "buffer");
			
			IncomingReply reply = this.m_pAppServer._processRequest(req);
						
			for (int i = 0; i < reply._getCount(); ++i) {
				String name = reply._getName(i);
				if (name.equals("stringParam")) {
					if (!testStr.equals(reply._getString(name)))
						System.out.println("Error with " + name);
					if (!testStr.equals(reply._getString(i)))
						System.out.println("Error with " + name + " at pos " + i);
				}
				else if (name.equals("true")) {
					if (testTrue != reply._getBoolean(name))
						System.out.println("Error with " + name);
					if (testTrue != reply._getBoolean(i))
						System.out.println("Error with " + name + " at pos " + i);
				}
				else if (name.equals("false")) {
					if (testFalse != reply._getBoolean(name))
						System.out.println("Error with " + name);
					if (testFalse != reply._getBoolean(i))
						System.out.println("Error with " + name + " at pos " + i);
				}
				else if (name.equals("buffer")) {
					byte[] bbuff = reply._getBuffer(name);
					if (!Arrays.equals(testBuff, reply._getBuffer(name)))
						System.out.println("Error with " + name);
					if (!Arrays.equals(testBuff, reply._getBuffer(name)))
						System.out.println("Error with " + name + " at pos " + i);
				}
				else if (name.equals("doublePos")) {
					if (testDoublePos != reply._getDouble(name))
						System.out.println("Error with " + name);
					if (testDoublePos != reply._getDouble(i))
						System.out.println("Error with " + name + " at pos " + i);
				}
				else if (name.equals("doubleNeg")) {
					if (testDoubleNeg != reply._getDouble(name))
						System.out.println("Error with " + name);
					if (testDoubleNeg != reply._getDouble(i))
						System.out.println("Error with " + name + " at pos " + i);
				}
				else if (name.equals("floatPos")) {
					if (testFloatPos != reply._getFloat(name))
						System.out.println("Error with " + name);
					if (testFloatPos != reply._getFloat(i))
						System.out.println("Error with " + name + " at pos " + i);
				}
				else if (name.equals("floatNeg")) {
					if (testFloatNeg != reply._getFloat(name))
						System.out.println("Error with " + name);
					if (testFloatNeg != reply._getFloat(i))
						System.out.println("Error with " + name + " at pos " + i);
				}
				else if (name.equals("int16Min")) {
					if (testInt16Min != reply._getInt16(name))
						System.out.println("Error with " + name);
					if (testInt16Min != reply._getInt16(i))
						System.out.println("Error with " + name + " at pos " + i);
				}
				else if (name.equals("int16Max")) {
					if (testInt16Max != reply._getInt16(name))
						System.out.println("Error with " + name);
					if (testInt16Max != reply._getInt16(i))
						System.out.println("Error with " + name + " at pos " + i);
				}
				else if (name.equals("uint16Min")) {
					if (testUInt16Min != reply._getUInt16(name))
						System.out.println("Error with " + name);
					if (testUInt16Min != reply._getUInt16(i))
						System.out.println("Error with " + name + " at pos " + i);
				}
				else if (name.equals("uint16Max")) {
					if (testUInt16Max != reply._getUInt16(name))
						System.out.println("Error with " + name);
					if (testUInt16Max != reply._getUInt16(i))
						System.out.println("Error with " + name + " at pos " + i);
				}
				else if (name.equals("int32Min")) {
					if (testInt32Min != reply._getInt32(name))
						System.out.println("Error with " + name);
					if (testInt32Min != reply._getInt32(i))
						System.out.println("Error with " + name + " at pos " + i);
				}
				else if (name.equals("int32Max")) {
					if (testInt32Max != reply._getInt32(name))
						System.out.println("Error with " + name);
					if (testInt32Max != reply._getInt32(i))
						System.out.println("Error with " + name + " at pos " + i);
				}
				else if (name.equals("uint32Min")) {
					if (testUInt32Min != reply._getUInt32(name))
						System.out.println("Error with " + name);
					if (testUInt32Min != reply._getUInt32(i))
						System.out.println("Error with " + name + " at pos " + i);
				}
				else if (name.equals("uint32Max")) {
					if (testUInt32Max != reply._getUInt32(name))
						System.out.println("Error with " + name);
					if (testUInt32Max != reply._getUInt32(i))
						System.out.println("Error with " + name + " at pos " + i);
				}
				else if (name.equals("int64Min")) {
					if (testInt64Min != reply._getInt64(name))
						System.out.println("Error with " + name);
					if (testInt64Min != reply._getInt64(i))
						System.out.println("Error with " + name + " at pos " + i);
				}
				else if (name.equals("int64Max")) {
					if (testInt64Max != reply._getInt64(name))
						System.out.println("Error with " + name);
					if (testInt64Max != reply._getInt64(i))
						System.out.println("Error with " + name + " at pos " + i);
				}
				else if (name.equals("uint64Min")) {
					if (testUInt64Min.compareTo(reply._getUInt64(name)) != 0)
						System.out.println("Error with " + name);
					if (testUInt64Min.compareTo(reply._getUInt64(i)) != 0)
						System.out.println("Error with " + name + " at pos " + i);
				}
				else if (name.equals("uint64Max")) {
					if (testUInt64Max.compareTo(reply._getUInt64(name)) != 0)
						System.out.println("Error with " + name);
					if (testUInt64Max.compareTo(reply._getUInt64(i)) != 0)
						System.out.println("Error with " + name + " at pos " + i);
				}
				else
					System.out.println("Unknown param " + name + " at pos " + i);
			}
		}
		*/
	}


}
