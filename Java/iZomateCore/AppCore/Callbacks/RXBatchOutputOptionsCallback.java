package iZomateCore.AppCore.Callbacks;


import iZomateCore.AppCore.HostApp;
import iZomateCore.AppCore.WindowControls;
import iZomateCore.ServerCore.CoreEnums.CBStatus;
import iZomateCore.ServerCore.CoreEnums.NotificationType;
import iZomateCore.ServerCore.Notifications.NotificationCallback;
import iZomateCore.UtilityCore.TimeUtils;

/**
 * 
 * @author mzapp
 *
 */
public class RXBatchOutputOptionsCallback extends NotificationCallback {
	private HostApp	m_pHostApp;
	private String	m_strFileType;
	private String	m_strParamsArray;

	public RXBatchOutputOptionsCallback( String strFileType, String strParamsArray, HostApp pHostApp) throws Exception {
		this.m_pHostApp= pHostApp;
		this.m_strParamsArray = strParamsArray;
		this.m_strFileType = strFileType;
		// Override only the values that we care about
		
		/*
		 * NOTE: This is a hack.  RX does not send identical notifications when opening the output options window per platform.
		 * This method should be updated once RX is updated to send the correct notifications, or at least the same ones.
		 */
		
		if( pHostApp._Testbed()._SysInfo()._isMac() ) {	
			this.m_pNotificationType= 	NotificationType.WNDW;	
			this.m_strTitle= "Set Output Options";
			this.m_strMessage= "Show";
			}
		else {
			this.m_pNotificationType= 	NotificationType.EVNT;
			this.m_strID= "MenuInvoke";
			}
		//this.m_strTitle= strTitle;
		}

	@Override
	public CBStatus _Callback( String title, String id, String message, String[] buttons, int value ) throws Exception {
		TimeUtils.sleep( this.m_pHostApp._Testbed()._SysInfo()._isMac() ? .6 : 1.5 );
		
		WindowControls.Button bOkButton = this.m_pHostApp._Plugin()._Controls()._Button("OutputFormatPanel|DialogButtonGroup|OK Button");

		this.m_pHostApp._Logs()._ResultLog()._logLine( "Parsing Parameters... \n" );
		String[] strArrParams = this.m_strParamsArray.split(","); //split the array along comma delimiters
		
		try{
			if ( this.m_strFileType.equals("WAVE") || this.m_strFileType.equals("Broadcast Wave (BWF)") || this.m_strFileType.equals("AIFF") ){ //These three formats have the same parameters, we can parse the string thusly.
				this.m_pHostApp._Logs()._ResultLog()._logLine( "Setting PCM parameters... \n" );
				WindowControls.ComboBox cbOutputFormat= this.m_pHostApp._Plugin()._Controls()._ComboBox( "OutputFormatPanel|Output Format ComboBox" ); //Create a reference to the Output Format Combo Box
				WindowControls.ComboBox cbDithering= this.m_pHostApp._Plugin()._Controls()._ComboBox( "OutputFormatPanel|Dithering Preset ComboBox" ); //Create a reference to the Dithering Combo Box
				//TODO:Need a method for dealing with check-boxes before the "preserve non-audio data" check-box can be properly addressed.
				
				cbOutputFormat._Select( strArrParams[0] );
				cbDithering._Select( strArrParams[1] );	
			}
			
			if ( this.m_strFileType.equals("FLAC")){// String Parameters for FLAC: sample format, dithering options, Compression level
				this.m_pHostApp._Logs()._ResultLog()._logLine( "Setting FLAC parameters... \n" );
				WindowControls.ComboBox cbOutputFormat= this.m_pHostApp._Plugin()._Controls()._ComboBox( "OutputFormatPanel|Output Format ComboBox" ); //Create a reference to the Output Format Combo Box
				WindowControls.ComboBox cbDithering= this.m_pHostApp._Plugin()._Controls()._ComboBox( "OutputFormatPanel|Dithering Preset ComboBox" ); //Create a reference to the Dithering Combo Box
				WindowControls.ComboBox cbCompressionLevel= this.m_pHostApp._Plugin()._Controls()._ComboBox( "OutputFormatPanel|FLAC Compression ComboBox" ); //Create a reference to the Dithering Combo Box
				
				cbOutputFormat._Select( strArrParams[0] );
				cbDithering._Select( strArrParams[1] );
				cbCompressionLevel._Select( strArrParams[2] );
			}
			
			if ( this.m_strFileType.equals("MP3")){// String parameters: Bit Rate Mode, Bit Rate, VBR Quality.  Bit rate is blocked by Bit Mode (CBR only)
				this.m_pHostApp._Logs()._ResultLog()._logLine( "Setting MP3 parameters... \n" );
				WindowControls.ComboBox cbBRMode= this.m_pHostApp._Plugin()._Controls()._ComboBox( "OutputFormatPanel|MP3 Bit Rate Mode ComboBox" ); //Create a reference to the Bit Rate Mode Combo Box
				WindowControls.ComboBox cbBitRate= this.m_pHostApp._Plugin()._Controls()._ComboBox( "OutputFormatPanel|MP3 Bit Rate ComboBox" ); //Create a reference to the Bit Rate kbit/s Combo Box
				WindowControls.ComboBox cbQuality= this.m_pHostApp._Plugin()._Controls()._ComboBox( "OutputFormatPanel|MP3 Quality ComboBox" ); //Create a reference to the Quality Combo Box
				
				cbBRMode._Select( strArrParams[0] ); // Select the mode
				
				if ( !strArrParams[0].equals("Variable bit rate (VBR)")){ //Bit Rate isn't available for VBR.  block it off.
					cbBitRate._Select( strArrParams[1] );	
				}else{
					cbQuality._Select( strArrParams[1] ); // if the bit rate is variable, then the second parameter of the string will be the quality
				}
			}
			
			if ( this.m_strFileType.equals("MP4") || this.m_strFileType.equals("M4A")){// String parameters: 
				this.m_pHostApp._Logs()._ResultLog()._logLine( "Setting AAC parameters... \n" );
				WindowControls.ComboBox cbAACProfile= this.m_pHostApp._Plugin()._Controls()._ComboBox( "OutputFormatPanel|AAC Profile ComboBox" ); //Create a reference to the Output Format Combo Box
				WindowControls.ComboBox cbBitRate= this.m_pHostApp._Plugin()._Controls()._ComboBox( "OutputFormatPanel|MP4 Bit Rate ComboBox" ); //Create a reference to the Dithering Combo Box
				cbAACProfile._Select( strArrParams[0] );
				cbBitRate._Select( strArrParams[1] );	
			}
			
			if ( this.m_strFileType.equals("Ogg Vorbis")){
				this.m_pHostApp._Logs()._ResultLog()._logLine( "Setting Ogg arameters... \n" );
				WindowControls.ComboBox cbQuality= this.m_pHostApp._Plugin()._Controls()._ComboBox( "OutputFormatPanel|Ogg Quality ComboBox" ); //Create a reference to the Quality Combo Box
				cbQuality._Select( strArrParams[0] );	
			}
		}catch (Exception e){
			this.m_pHostApp._Logs()._ResultLog()._logError("Selected output options do not exist for output format, please re-check your testcase.", false);
		}
		bOkButton._click(null, null); //We don't always get the appropriate notification depending on the system but since we're just clicking an OK button we can throw a small bit of caution to the wind
		this.m_pHostApp._Logs()._ResultLog()._logLine( "Parameter settings complete! \n" );
		return CBStatus.HANDLED;
	}

}

