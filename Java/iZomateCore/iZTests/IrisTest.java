package iZomateCore.iZTests;

import iZomateCore.AppCore.WindowControls;
import iZomateCore.AppCore.WindowControls.ButtonState;
import iZomateCore.AppCore.WindowControls.ClickSpot;
import iZomateCore.AppCore.AppEnums.Insert;
import iZomateCore.AppCore.AppEnums.PluginType;
import iZomateCore.AppCore.AppEnums.TrackFormat;
import iZomateCore.AppCore.AppEnums.TrackType;
import iZomateCore.AppCore.AppEnums.WindowControls.Buttons;
import iZomateCore.TestCore.Test;
import iZomateCore.TestCore.TestCaseParameters;
import iZomateCore.UtilityCore.TimeUtils;

import javax.sound.midi.ShortMessage;


public class IrisTest extends Test
{	
	private Buttons[] 				mPickerBtns= new Buttons[]{ Buttons.Picker1, Buttons.Picker2, Buttons.Picker3, Buttons.PickerSub };
	private boolean 				mPickerLtoR= true;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception	{
		new IrisTest( args ).run();
	}
	
	/**
	 * 
	 * @param testbed
	 * @param appPath
	 * @param logDir
	 * @throws Exception
	 */
	public IrisTest( String[] args ) throws Exception {
		super( args );
	}
		

	@Override
	protected void _StartUp( TestCaseParameters pCommonParameters ) throws Exception {
		// TODO Auto-generated method stub
	}

    @Override
    protected void _SetupTestCase( TestCaseParameters pTestcaseParameters ) throws Exception {
        // TODO Auto-generated method stub
    }

	@Override
	protected void _TestCase( TestCaseParameters pTestcaseParameters ) throws Exception {
		this._Testbed( pTestcaseParameters._GetTestbed() )._HostApp( pTestcaseParameters._GetApp() )._Launch(3, null, false, pTestcaseParameters._GetHideAllWinAtStart());

		this._Testbed( pTestcaseParameters._GetTestbed() )._HostApp( pTestcaseParameters._GetApp() )._GetProcessInfo("Memory Check before starting preset switching", true);
		WindowControls Controls= this._Testbed()._HostApp()._Plugin( pTestcaseParameters._GetPlugin() )._Controls();
		Controls._Button(Buttons.GlobalDrawerButton)._setState(ButtonState.ON);
		Controls._Button(Buttons.KeyboardDrawerButton)._setState(ButtonState.ON, ClickSpot.Top);
		if( pTestcaseParameters._GetPitchMode() != null ) {
			Controls._Button(Buttons.SynthDrawerButton)._setState(ButtonState.ON, ClickSpot.Left);
			Controls._Button( Buttons.MixUndock )._setState( ButtonState.OFF );
		}

		
		int f1 = 60;
		int f2 = 62;
		boolean isEven = false;
				
		//this._Setup();
		
		this._Logs()._ResultLog()._logGeneric("Current preset is '" + Controls._Button(Buttons.Presets)._info().mCaption + "'", "PresetChange");
		
		this._Testbed()._HostApp()._sendMidiNotes(isEven?f1:f2, 10, ShortMessage.NOTE_ON);

		long stoptime = pTestcaseParameters._GetTestDuration()*1000 + System.currentTimeMillis();
	
		while (stoptime > System.currentTimeMillis())
		{	
			this._setPitchMode( pTestcaseParameters, Controls );
			//this._getHost()._cycleThruPresetsInMenu(1);
			this._Testbed()._HostApp()._nextPreset( pTestcaseParameters._GetUseListBox(), false/*Current Present*/ );
			TimeUtils.sleep( pTestcaseParameters._GetPresetPlayTime() );
//			this._getProcessInfo("Memory Check");
			this._Testbed()._HostApp()._sendMidiNotes(!isEven?f1:f2, 10, ShortMessage.NOTE_ON);
			this._Testbed()._HostApp()._sendMidiNotes(isEven?f1:f2, 10, ShortMessage.NOTE_OFF);
			isEven = !isEven;
		}
		
		//Make sure everything is off
		this._Testbed()._HostApp()._sendMidiNotes(f1, 10, ShortMessage.NOTE_OFF);
		this._Testbed()._HostApp()._sendMidiNotes(f2, 10, ShortMessage.NOTE_OFF);		
	}

	/**
	 * 
	 * @throws Exception
	 */
	private void _setPitchMode( TestCaseParameters pTestcaseParameters, WindowControls Controls ) throws Exception {
		if( pTestcaseParameters._GetPitchMode() == null )
			return;
		
		int x= this.mPickerLtoR?0:this.mPickerBtns.length-1;
		for( int loop=0; loop<this.mPickerBtns.length; ++loop) {
			Controls._Button( this.mPickerBtns[x] )._setState( ButtonState.ON );
			Controls._ComboBox( "Pitch Mode ComboBox" )._Select( pTestcaseParameters._GetPitchMode() );
			x= this.mPickerLtoR?++x:--x;
		}
		this.mPickerLtoR= !this.mPickerLtoR;
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

