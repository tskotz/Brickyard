package iZomateCore.AppCore;

import iZomateCore.AppCore.AppEnums.Insert;
import iZomateCore.AppCore.AppEnums.PluginType;
import iZomateCore.AppCore.AppEnums.TrackFormat;
import iZomateCore.AppCore.AppEnums.TrackType;
import iZomateCore.UtilityCore.TimeUtils;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class ProTools extends HostApp
{
	private String			mBMP_SessionStartWin = "/IzoImages/MixBlankSession.bmp";
	private String			mBMP_AuthContinueBTN = "/IzoImages/AuthContinueBTN.bmp";


	public ProTools(String strTestbed, String strAppPath, String logDir) throws Exception
	{
		//TODO:  Fix this
		super(strAppPath, null);
	}

	public void _Launch(int sleep, String optionalFile) throws Exception
	{
		super._Launch(sleep, optionalFile, false, false);
		
		if (optionalFile.endsWith("MIDI Stress Test.ptx"))
			this.mBMP_SessionStartWin = "/IzoImages/EditMIDIStressTestSession.bmp";
		
		for (int c = 0; c < 20; ++c)
		{
			if (this._Testbed()._Robot()._imageClick(this.mBMP_SessionStartWin, 0) != null)
				break;
			else
			{
				this._Testbed()._Robot()._imageClick(this.mBMP_AuthContinueBTN, 1);
				TimeUtils.sleep(1.5);
			}
		}
		
	}
	
	public void _quit() throws Exception
	{
		if (this._Testbed()._IsAppRunning(this.m_fAppFile._getName()) > 0)
		{
			this._Testbed()._Robot()._keyTypePause(.25, KeyEvent.VK_WINDOWS, KeyEvent.VK_M);
			this._Testbed()._Robot()._imageClick("/IzoImages/PTicon.bmp", 1);
			TimeUtils.sleep(.5);
			this._Testbed()._Robot()._keyTypePause(.25, KeyEvent.VK_CONTROL, KeyEvent.VK_Q);
			TimeUtils.sleep(.7);
			this._Testbed()._Robot()._imageClick("/IzoImages/PTDontSaveBtn.bmp", 1);
			TimeUtils.sleep(1);
			while (this._Testbed()._IsAppRunning(this.m_fAppFile._getName()) > 0)
				TimeUtils.sleep(1);
		}
	}

	public void _importAudioFile(String audioFile) throws Exception
	{
		this._Testbed()._Robot()._keyTypePause(.25, KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_I); //Import Audio
		TimeUtils.sleep(.5);

		while (this._Testbed()._Robot()._imageClick("/IzoImages/UpOneLevelBtn.bmp", 1) != null)
		{
			this._Testbed()._Robot()._mouseMove(0, 0);
			TimeUtils.sleep(.2);
		}
		
		for (String s : audioFile.split("/"))
		{
			this._Testbed()._Robot()._keyType(s);
			this._Testbed()._Robot()._keyTypePause(.5, KeyEvent.VK_ENTER);
		}
		
		this._Testbed()._Robot()._imageClick("/IzoImages/DoneBtn.bmp", 1);
		this._Testbed()._Robot()._imageClick("/IzoImages/OKBtn.bmp", 1);	
		TimeUtils.sleep(1);

		if (this._Testbed()._Robot()._imageClick("/IzoImages/MixBlankSession.bmp", 0) == null)
			this._Testbed()._Robot()._keyTypePause(.25, KeyEvent.VK_CONTROL, KeyEvent.VK_EQUALS);
	}

	public void _createNewTrack(TrackFormat trackFormat, TrackType trackType) throws Exception
	{
		String ttype = null;
		String tformat = null;
		
		if (trackFormat == TrackFormat.Stereo)
			tformat ="/IzoImages/PTNewTracksStereoMenuSel.bmp";
		else
			throw new Exception("Unsupported track format:" + trackFormat._getValue());

		if (trackType == TrackType.InstrumentTrack)
			ttype = "/IzoImages/PTNewTracksInstrMenuSel.bmp";
		else
			throw new Exception("Unsupported track type:" + trackType._getValue());

		this._Testbed()._Robot()._keyTypePause(.25, KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_N);
		TimeUtils.sleep(.2);
		this._Testbed()._Robot()._imageClick("/IzoImages/PTNewTracksNewLabel.bmp", 		1, new Point(80,5));
		this._Testbed()._Robot()._imageClick(tformat, 1);
		this._Testbed()._Robot()._imageClick("/IzoImages/PTNewTracksNewLabel.bmp", 		1, new Point(200,5));
		this._Testbed()._Robot()._imageClick(ttype, 	1);		
		this._Testbed()._Robot()._keyTypePause(.25, KeyEvent.VK_ENTER);
		TimeUtils.sleep(1);
	}

	public void _instantiatePlugin(PluginType plugin, Insert insert) throws Exception
	{
		this._Testbed()._Robot()._imageClick("/IzoImages/INSERTS_A-E.bmp", 			1, new Point(5, 20*insert._getValue()));
		this._Testbed()._Robot()._imageClick("/IzoImages/plug-in.bmp", 				0);
		this._Testbed()._Robot()._imageClick("/IzoImages/Dynamics.bmp", 			0);
		this._Testbed()._Robot()._imageClick("/IzoImages/" + plugin._getValue() + ".bmp", 	1);
		this._Testbed()._Robot()._imageClick("/IzoImages/AuthContinueBTN.bmp", 		1);
		TimeUtils.sleep(1);
	}
	
	public void _uninstantiatePlugin(int insertNumber) throws Exception
	{
		this._Testbed()._Robot()._imageClick("/IzoImages/INSERTS_A-E.bmp", 	1, new Point(5, 20*insertNumber));
		this._Testbed()._Robot()._mouseClick(InputEvent.BUTTON1_MASK);
		TimeUtils.sleep(1.5);
	}

	@Override
	public void _togglePlay()
	{
		// TODO Auto-generated method stub
		
	}
	
}
