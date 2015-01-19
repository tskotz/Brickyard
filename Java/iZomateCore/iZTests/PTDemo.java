package iZomateCore.iZTests;

import iZomateCore.AppCore.AppEnums.WindowControls.MouseButtons;
import iZomateCore.ServerCore.RPCServer.OutgoingRequest;
import iZomateCore.ServerCore.RPCServer.RPCServer;
import iZomateCore.ServerCore.RPCServer.RemoteServer.ProcessInfo;
import iZomateCore.ServerCore.RPCServer.RemoteServer.RemoteFile;
import iZomateCore.ServerCore.RPCServer.RemoteServer.RemoteServer;
import iZomateCore.TestCore.Test;
import iZomateCore.TestCore.TestCaseParameters;
import iZomateCore.UtilityCore.TimeUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;

public class PTDemo extends Test
{
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception	{
		new PTDemo( args ).run();
	}

	private RPCServer 		mTestAppServer;
	private RemoteServer 	mRmtServer;
	private RemoteFile 		mPTTestSession;
	private RemoteFile 		mPTExe;
	private long			mPID = 0;
	private	ProcessInfo		mPrevPI;
	private LinkedHashMap<String,ProcessInfo> mPIMap = new LinkedHashMap<String,ProcessInfo>();
	DecimalFormat 			mDecFrmtr;

	
	public PTDemo(String[] args) throws Exception	{
		super( args );
	}
	
	
	public void _testAppCheck() throws Exception
	{
		if (this.mTestAppServer._isConnected())
		{
			//Test App
			OutgoingRequest req = this.mTestAppServer._createRequest("TestHook");
			req._addString("Hello World", "astring");
			req._addBoolean(true, "true");
			req._addInt32(12345, "int32");
			req._addUInt32(-12345, "uint32");
			this.mTestAppServer._processRequest(req);
		}
	}
	
	public void _quitAppIfRunning() throws Exception
	{		
		if (this.mRmtServer._IsAppRunning(this.mPTExe._getName()) > 0)
		{
			this.mRmtServer._Robot()._keyTypePause(.25, KeyEvent.VK_WINDOWS, KeyEvent.VK_M);
			this.mRmtServer._Robot()._imageClickII("/IzoImages/PTicon.bmp", 1);
			TimeUtils.sleep(.5);
			this.mRmtServer._Robot()._keyTypePause(.25, KeyEvent.VK_CONTROL, KeyEvent.VK_Q);
			TimeUtils.sleep(.7);
			this.mRmtServer._Robot()._imageClick("/IzoImages/PTDontSaveBtn.bmp", 1);
			TimeUtils.sleep(1);
			while (this.mRmtServer._IsAppRunning(this.mPTExe._getName()) > 0)
				TimeUtils.sleep(1);
		}
	}
	
	public void _copyInSession(String goldSession) throws Exception
	{
		RemoteFile theGoldSession = this.mRmtServer._createRemoteFile(goldSession);
		if (!theGoldSession._exists())
			throw new Exception("The PT Session was not found: " + theGoldSession._getPathAndName());

		this.mPTTestSession = this.mRmtServer._createRemoteFile("D:/Sessions/Test/" + theGoldSession._getName());

		this.mPTTestSession._delete(); //delete if it exists
		this.mPTTestSession._getParent()._mkdirs(); //make sure the parent dir structure exists
		theGoldSession._copyTo(this.mPTTestSession, 10); //make the copy	
	}
	
	public void _startApp() throws Exception
	{			
		if (this.mRmtServer._IsAppRunning(this.mPTExe._getName()) == 0)
		{
			if (!this.mPTExe._exists())
				throw new Exception(this.mPTExe._getPathAndName() + " not found!");
			
			this.mRmtServer._LaunchApp( this.mRmtServer._createRemoteFile( this.mPTExe._getPathAndName() ), "\"" + this.mPTTestSession._getPathAndName() + "\"" );

			//this.mRmtServer._commandLine("\"" + this.mPTExe._getPathAndName() + "\" \"" + this.mPTTestSession._getPathAndName() + "\"", false);
			TimeUtils.sleep(21);
			for (int c = 0; c < 20; ++c)
			{
				if (this.mRmtServer._Robot()._imageClick("/IzoImages/MixBlankSession.bmp", 0) != null)
					break;
				else
					TimeUtils.sleep(1.5);
			}
		}
		
		this.mPID = this.mRmtServer._SysInfo()._getProcPid(this.mPTExe._getName());
	}

	public void _importAudioFile(String audioFile) throws Exception
	{
		this.mRmtServer._Robot()._keyTypePause(.25, KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_I); //Import Audio
		TimeUtils.sleep(.5);

		while (this.mRmtServer._Robot()._imageClick("/IzoImages/UpOneLevelBtn.bmp", 1) != null)
		{
			this.mRmtServer._Robot()._mouseMove(0, 0);
			TimeUtils.sleep(.2);
		}
		
		for (String s : audioFile.split("/"))
		{
			this.mRmtServer._Robot()._keyType(s);
			this.mRmtServer._Robot()._keyTypePause(.5, KeyEvent.VK_ENTER);
		}
		
		this.mRmtServer._Robot()._imageClickII("/IzoImages/DoneBtn.bmp", 1);
		this.mRmtServer._Robot()._imageClickII("/IzoImages/OKBtn.bmp", 1);	
		TimeUtils.sleep(1);

		if (this.mRmtServer._Robot()._imageClick("/IzoImages/MixBlankSession.bmp", 0) == null)
			this.mRmtServer._Robot()._keyTypePause(.25, KeyEvent.VK_CONTROL, KeyEvent.VK_EQUALS);
	}

	public void _createNewTrack() throws Exception
	{
		this.mRmtServer._Robot()._keyTypePause(.25, KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_N);
		this.mRmtServer._Robot()._keyTypePause(.25, KeyEvent.VK_ENTER);
		TimeUtils.sleep(1);
	}
	
	public void _togglePlay() throws Exception
	{
		this.mRmtServer._Robot()._keyTypePause(.25, KeyEvent.VK_SPACE);
	}
	
	public void _instantiatePlugin(String pluginName, int insertNumber) throws Exception
	{
		this.mRmtServer._Robot()._imageClickII("/IzoImages/INSERTS_A-E.bmp", 		1, new Point(5, 20*insertNumber));
		this.mRmtServer._Robot()._imageClickII("/IzoImages/plug-in.bmp", 			0);
		this.mRmtServer._Robot()._imageClickII("/IzoImages/Dynamics.bmp", 			0);
		this.mRmtServer._Robot()._imageClickII("/IzoImages/" + pluginName + ".bmp", 	1);
		this.mRmtServer._Robot()._imageClickII("/IzoImages/AuthDemoBTN.bmp", 	1);
		TimeUtils.sleep(1);
	}
	
	public void _uninstantiatePlugin(int insertNumber) throws Exception
	{
		this.mRmtServer._Robot()._imageClickII("/IzoImages/INSERTS_A-E.bmp", 	1, new Point(5, 20*insertNumber));
		Point p= this.mRmtServer._Robot()._mouseGetLocation();
		this.mRmtServer._Robot()._mouseMove( p.x+1, p.y );
		this.mRmtServer._Robot()._mouseClick( 1, MouseButtons.Left );
		TimeUtils.sleep(1.5);
	}
	
	public void _tweakNectar() throws Exception
	{
		this._twiddleVSlider("/IzoImages/NectarWetDryLabel.bmp", 1);
		this._twiddleVSlider("/IzoImages/NectarPresenceLabel.bmp", 1);
	}
	
	public void _tweakOzone() throws Exception
	{
		TimeUtils.sleep(.3);
		if (this.mRmtServer._Robot()._imageClick("/IzoImages/Ozone3BndMstrInstrSlowDy.bmp", 1) == null)
		{
			this.mRmtServer._Robot()._imageClickII("/IzoImages/OzoneMaximizerBtn.bmp", 	1, new Point(-55, 20));
			TimeUtils.sleep(.3);
			this.mRmtServer._Robot()._imageClick("/IzoImages/Ozone3BndMstrInstrSlowDy.bmp", 1);	
		}
		
		this.mRmtServer._Robot()._imageClickII("/IzoImages/OzonePresetsBTN.bmp", 1);		

		this._twiddleHSlider("/IzoImages/OzoneMaximizerBtn.bmp", 	false, 1);
		this._twiddleHSlider("/IzoImages/OzoneEQBTN.bmp", 			false, 2);
		this._twiddleHSlider("/IzoImages/OzoneStImagingBTN.bmp", 	false, 3);	
		this._twiddleHSlider("/IzoImages/OzoneReverbBTN.bmp", 		true,  4);
		this._twiddleOzoneVSlider("/IzoImages/OzoneWetMixSlider.bmp", 10);
	}
	
	public void _twiddleHSlider(String image, boolean enable, int speed) throws Exception
	{
		//Click big button
		this.mRmtServer._Robot()._imageClickII(image, 1, new Point(-55, 20));
		
		if (enable)
			this.mRmtServer._Robot()._imageClickII(image, 1, new Point(-33, 20));

		Point cp = this.mRmtServer._Robot()._imageClickII(image, 1, new Point(75, 26));
		this.mRmtServer._Robot()._mouseDragAndDrop(null, new Point(cp.x+60, cp.y), speed, true, false);
		this.mRmtServer._Robot()._mouseDragAndDrop(null, new Point(cp.x-60, cp.y), speed, false, false);
		this.mRmtServer._Robot()._mouseDragAndDrop(null, new Point(cp.x+60, cp.y), speed, false, false);
		this.mRmtServer._Robot()._mouseDragAndDrop(null, new Point(cp.x-60, cp.y), speed, false, false);
		this.mRmtServer._Robot()._mouseDragAndDrop(null, new Point(cp.x-30, cp.y), speed, false, true);
	}

	public void _twiddleVSlider(String image, int speed) throws Exception
	{
		Point cp = this.mRmtServer._Robot()._imageClickII(image, 1, new Point(20, 24));
		this.mRmtServer._Robot()._mouseDragAndDrop(null, new Point(cp.x, cp.y+65), 	speed, true,  false);
		this.mRmtServer._Robot()._mouseDragAndDrop(null, new Point(cp.x, cp.y), 		speed, false, false);
		this.mRmtServer._Robot()._mouseDragAndDrop(null, new Point(cp.x, cp.y+65), 	speed, false, false);
		this.mRmtServer._Robot()._mouseDragAndDrop(null, new Point(cp.x, cp.y), 		speed, false, false);
		this.mRmtServer._Robot()._mouseDragAndDrop(null, new Point(cp.x, cp.y+30), 	speed, false, true);
	}

	public void _twiddleOzoneVSlider(String image, int speed) throws Exception
	{
		Point cp = this.mRmtServer._Robot()._imageClickII(image, 1, new Point(10, 35));
		this.mRmtServer._Robot()._mouseDragAndDrop(null, new Point(cp.x, cp.y+130), 	speed, true,  false);
		this.mRmtServer._Robot()._mouseDragAndDrop(null, new Point(cp.x, cp.y), 		speed, false, false);
		this.mRmtServer._Robot()._mouseDragAndDrop(null, new Point(cp.x, cp.y+130), 	speed, false, false);
		this.mRmtServer._Robot()._mouseDragAndDrop(null, new Point(cp.x, cp.y), 		speed, false, false);
		this.mRmtServer._Robot()._mouseDragAndDrop(null, new Point(cp.x, cp.y+130), 	speed, false, false);
		this.mRmtServer._Robot()._mouseDragAndDrop(null, new Point(cp.x, cp.y), 		speed, false, false);
		this.mRmtServer._Robot()._mouseDragAndDrop(null, new Point(cp.x, cp.y+20), 	speed, false, true);
	}
	
	public ProcessInfo _getProcessInfo(String memo) throws Exception
	{
		if (this.mPID == 0)
			this.mPID = this.mRmtServer._SysInfo()._getProcPid(this.mPTExe._getName());

		ProcessInfo pi = this.mRmtServer._SysInfo()._getProcInfo(this.mPID);
//		SystemInfo si = this.mRmtServer._SysInfo()._getSystemInfo();
		this.mPIMap.put(memo, pi);
		System.out.println(String.format("%1$-25s", memo) + 
				"	Mem: " 			+ String.format("%1$-12s", this.mDecFrmtr.format(pi.mMemRes)) + 
				" (" 				+ String.format("%1$-12s", this.mDecFrmtr.format(pi.mMemRes - this.mPrevPI.mMemRes)) + ")" +
				" 	CPU %: " 		+ String.format("%1$-3s", this.mDecFrmtr.format(pi.mCPUPercentage*100)) +
				" 	CPU Time: " 	+ String.format("%1$-3s", this.mDecFrmtr.format(pi.mCPUTime)) +
				" 	Threads: " 		+ String.format("%1$-3s", this.mDecFrmtr.format(pi.mThreads)) +
				" 	Pages: " 	+ String.format("%1$-3s", this.mDecFrmtr.format(pi.mMemPageFaults))	);
		this.mPrevPI = pi;
//		System.out.println(pi.toString());
		return pi;
	}
	
	public void _reportProcessInfo()
	{
		ProcessInfo prevVal = new ProcessInfo();
		ProcessInfo startVal = null;
	
		System.out.println(String.format("\n\n%1$-25s", "Memo") + String.format("%1$-24s", "Proc Memory") + String.format("	%1$-9s", "Proc Threads"));
		System.out.println("---------------------------------------------------------------------------------");
		for (String s : this.mPIMap.keySet())
		{
			ProcessInfo val = this.mPIMap.get(s);
			System.out.println(String.format("%1$-25s", s) + String.format("%1$-12s", this.mDecFrmtr.format(val.mMemRes)) + "(" + String.format("%1$-12s", this.mDecFrmtr.format(val.mMemRes - prevVal.mMemRes)) + ")" + "	" +
					String.format("%1$-3s", this.mDecFrmtr.format(val.mThreads)) + "(" + String.format("%1$-3s", this.mDecFrmtr.format(val.mThreads - prevVal.mThreads)) + ")");
			prevVal = val;
			if (startVal == null)
				startVal = val;
		}
		
		String msg = (prevVal.mMemRes - startVal.mMemRes) == 0 ? ":-)" : ":-(";
		System.out.println(String.format("\n%1$-25s", ("Final delta: " + msg)) + String.format("%1$-24s", this.mDecFrmtr.format(prevVal.mMemRes - startVal.mMemRes)) + String.format("	%1$-9s", this.mDecFrmtr.format(prevVal.mThreads - startVal.mThreads)));
	}

	@Override
	protected void _StartUp( TestCaseParameters pCommonParameters ) throws Exception {	
		;
	}

    @Override
    protected void _SetupTestCase( TestCaseParameters pTestcaseParameters )
            throws Exception {
        // TODO Auto-generated method stub

    }


	@Override
	protected void _TestCase( TestCaseParameters pTestcaseParameters ) throws Exception {
		this._testAppCheck();
		this._quitAppIfRunning();
		this._copyInSession("D:/Sessions/Gold/Blank.ptx");
		this._startApp();
		this._importAudioFile("Terry Skotz/My Music/iTunes/iTunes Media/Music/Joe Sample/Old Places Old Faces/03 Clifton's Gold.wav");
		this._createNewTrack();
		this._getProcessInfo("Initial");
		this._togglePlay();
		this._getProcessInfo("Play Started");
		this._instantiatePlugin("Nectar", 1);
		this._getProcessInfo("Nectar Instantiation");
		this._tweakNectar();
		this._getProcessInfo("Nectar tweaks");
		this._instantiatePlugin("Ozone5", 2);
		this._getProcessInfo("Ozone Instantiation");
		this._tweakOzone();
		this._getProcessInfo("Ozone tweaks");
		this._uninstantiatePlugin(2);
		this._getProcessInfo("Ozone Uninstantiation");
		this._uninstantiatePlugin(1);
		this._getProcessInfo("Nectar Uninstantiation");
		this._togglePlay();
		this._getProcessInfo("Play stopped");
		TimeUtils.sleep(5);
		this._getProcessInfo("After 5 sec sleep");
		this._reportProcessInfo();
		
	}

	@Override
	protected void _OnTestCaseException(
			TestCaseParameters pTestcaseParameters, Exception e )
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void _ShutDown( TestCaseParameters pCommonParameters )
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}