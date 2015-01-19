package iZomateCore.AppCore.AppEnums;

import java.util.HashMap;

public enum Images
{
	DUMMY						(""),
	IrisTaskBarIcon				("OSX:/TestAutoImages/Iris/IrisTaskBarIcon.bmp"),
	Inst_AgreeBtn				("OSX:/TestAutoImages/Installer/OSX/AgreeButton.bmp"),
	Inst_CloseBtn				("OSX:/TestAutoImages/Installer/OSX/CloseButton.bmp"),
	Inst_ContinueBtn			("OSX:/TestAutoImages/Installer/OSX/ContinueButton.bmp"),
	Inst_DestinationSelectLabel	("OSX:/TestAutoImages/Installer/OSX/DestinationSelectLabel.bmp"),
	Inst_InstallForLabel		("OSX:/TestAutoImages/Installer/OSX/InstallForLabel.bmp"),
	Inst_InstallBtn				("OSX:/TestAutoImages/Installer/OSX/InstallButton.bmp"),
	Inst_NoBtn					("OSX:/TestAutoImages/Installer/OSX/NoButton.bmp"),
	Inst_WYLTCFUpdatesTxt		("OSX:/TestAutoImages/Installer/OSX/WYLTCFUpdatesText.bmp"),
	Inst_PasswordLabel			("OSX:/TestAutoImages/Installer/OSX/PasswordLabel.bmp"),
	
	Inst_NextButton				("Win7:/TestAutoImages/Installer/Win/NextButton.bmp"),
	Inst_AcceptButton			("Win7:/TestAutoImages/Installer/Win/IAcceptTheAgreement.bmp"),
	Inst_SelectSoundLibContFolderText("Win7:/TestAutoImages/Installer/Win/SelectSoundLibContFolderText.bmp"),
	Inst_CheckForUpdatesButton	("Win7:/TestAutoImages/Installer/Win/CheckForUpdatesCBox.bmp"),
	Inst_InstallButton			("Win7:/TestAutoImages/Installer/Win/InstallButton.bmp"),
	Inst_RunButton				("Win7:/TestAutoImages/Installer/Win/RunButton.bmp"),
	Inst_CancelButton			("Win7:/TestAutoImages/Installer/Win/CancelButton.bmp"),
	Inst_CancelButtonDisabled	("Win7:/TestAutoImages/Installer/Win/CancelButtonDisabled.bmp"),
	Inst_FinishButton			("Win7:/TestAutoImages/Installer/Win/FinishButton.bmp"),
	Inst_OKButton				("OSX:/TestAutoImages/Installer/OSX/OKButton.bmp",
								 "Win7:/TestAutoImages/Installer/Win/OKButton.bmp"),
	Inst_YesButton				("OSX:/TestAutoImages/Installer/OSX/YesButton.bmp",
								 "Win7:/TestAutoImages/Installer/Win/YesButton.bmp"),
	Inst_YesToAll				("Win7:/TestAutoImages/Installer/Win/YesToAll.bmp"),
	Inst_UninstallSuccess		("OSX:/TestAutoImages/Installer/OSX/UninstallSuccessful.bmp"),
	Inst_LaunchApplication		("/TestAutoImages/Installer/Win/LaunchApplication.bmp"),
	Inst_Success				("/TestAutoImages/Installer/OSX/SuccessfulInstall.bmp"),
	Inst_UnRemovedFiles			("/TestAutoImages/Installer/OSX/UnremovedFiles.bmp"),
	
	Auth_DemoButton				("OSX:/TestAutoImages/Auth/DemoButton.bmp",
								 "Win7:/TestAutoImages/Auth/DemoButton.bmp"),
	Auth_AuthorizeButton		("OSX:/TestAutoImages/Auth/AuthorizeButton.bmp",
								 "Win7:/TestAutoImages/Auth/Win/AuthorizeButton.bmp"),
	Auth_Successful				("Win7:/TestAutoImages/Auth/Win/AuthorizationSuccessful.bmp",
								 "OSX:/TestAutoImages/Auth/OSX/AuthorizationSuccessful.bmp"),
	Auth_ContinueButton			("Win7:/TestAutoImages/Auth/Win/ContinueButton.bmp",
								 "OSX:/TestAutoImages/Auth/OSX/ContinueButton.bmp"),
	Auth_EmailBox				("Win7:/TestAutoImages/Auth/Win/EmailBox.bmp",
								 "OSX:/TestAutoImages/Auth/OSX/EmailBox.bmp"),
	Auth_NameBox				("Win7:/TestAutoImages/Auth/Win/NameBox.bmp",
								 "OSX:/TestAutoImages/Auth/OSX/NameBox.bmp"),
	Auth_SerialNumberBox		("Win7:/TestAutoImages/Auth/Win/SerialNumberBox.bmp",
								 "OSX:/TestAutoImages/Auth/OSX/SerialNumberBox.bmp"),
	Auth_SubmitButton			("Win7:/TestAutoImages/Auth/Win/SubmitButton.bmp",
								 "OSX:/TestAutoImages/Auth/Win/SubmitButton.bmp"),
	Auth_AuthWizard				("Win7:/TestAutoImages/Auth/Win/AuthWizard.bmp",
								 "OSX:/TestAutoImages/Auth/OSX/AuthWizard.bmp"),
	
	Host_Live9_PlayClip1		("/TestAutoImages/Hosts/Live_9/PlayClip_1.bmp"),
	Host_Live9_PlayClip2		("/TestAutoImages/Hosts/Live_9/PlayClip_2.bmp"),
	Host_Live9_PlayClip3		("/TestAutoImages/Hosts/Live_9/PlayClip_3.bmp"),
	Host_Live9_PlayClip4		("/TestAutoImages/Hosts/Live_9/PlayClip_4.bmp"),
	Host_Live9_PluginSelected	("/TestAutoImages/Hosts/Live_9/Plugin_Selected.bmp"),
	Host_Live9_PluginUnselected	("/TestAutoImages/Hosts/Live_9/Plugin_Unselected.bmp"),
	
	Host_ProTools11_TrackFormatPopupMenu			("/TestAutoImages/Hosts/ProTools11/Win7/Track Format Popup Menu.bmp"),
	Host_ProTools11_TrackTimebasePopupMenu			("/TestAutoImages/Hosts/ProTools11/Win7/Track Timebase Popup Menu.bmp"),
	Host_ProTools11_TrackTypePopupMenu				("/TestAutoImages/Hosts/ProTools11/Win7/Track Type Popup Menu.bmp"),
	Host_ProTools11_CreateButtonHighlighted			("/TestAutoImages/Hosts/ProTools11/Create Button Highlighted.bmp"),
	Host_ProTools11_CreateBlankSessionRadioButton 	("/TestAutoImages/Hosts/ProTools11/CreateBlankSessionRadioButton.bmp"),
	Host_ProTools11_ImportAudioDone					("OSX:/TestAutoImages/Hosts/ProTools11/ImportAudioDone.bmp",
													 "Win7:/TestAutoImages/Hosts/ProTools11/PT11_Done_Win8.bmp"),
	Host_ProTools11_InsertMultichannel	 			("/TestAutoImages/Hosts/ProTools11/Insert_Multichannel.bmp"),
	Host_ProTools11_Inserts							("OSX:/TestAutoImages/Hosts/ProTools11/Inserts.bmp", "Win7:/TestAutoImages/Hosts/ProTools11/Win7/Inserts.bmp"),
	Host_ProTools11_MenubarTrack					("/TestAutoImages/Hosts/ProTools11/Menubar_Track.bmp"),
	Host_ProTools11_NewSessionDialog				("/TestAutoImages/Hosts/ProTools11/New Session Dialog.bmp"),
	Host_ProTools11_NewTracksDialog					("/TestAutoImages/Hosts/ProTools11/New Tracks Dialog.bmp"),
	Host_ProTools11_OKButtonHighlighted				("/TestAutoImages/Hosts/ProTools11/OKButtonHighlighted.bmp", "Win7:/TestAutoImages/Hosts/ProTools11/Win7/OKButtonHighlighted.bmp" ),
	Host_ProTools11_PluginExists					("/TestAutoImages/Hosts/ProTools11/PluginExists.bmp", "Win7:/TestAutoImages/Hosts/ProTools11/Win7/PluginExists.bmp" ),
	Host_ProTools11_PluginExistsHighlighted			("/TestAutoImages/Hosts/ProTools11/PluginExists Highlighted.bmp", "Win7:/TestAutoImages/Hosts/ProTools11/Win7/PluginExists Highlighted.bmp" ),
	Host_ProTools11_PluginWindowClose				("/TestAutoImages/Hosts/ProTools11/PluginWindowClose.bmp", "Win7:/TestAutoImages/Hosts/ProTools11/Win7/PluginWindowClose.bmp"),
	Host_ProTools11_PluginWindowCloseRed			("/TestAutoImages/Hosts/ProTools11/PluginWindowCloseRed.bmp"),
	Host_ProTools11_TransportPlaying				("/TestAutoImages/Hosts/ProTools11/PT11_Playing.bmp"),
	Host_ProTools11_SaveButtonGlow					("/TestAutoImages/Hosts/ProTools11/SaveButtonGlow.bmp"),
	Host_ProTools11_SaveSessionDialog				("/TestAutoImages/Hosts/ProTools11/SaveSessionDialog.bmp"),
	Host_ProTools11_BreakTweakerPluginGraphic		("Win7:/TestAutoImages/Hosts/ProTools11/Win7/BreakTweakerPluginGraphic.bmp"),
	Host_ProTools11_Ozone5PluginGraphic				("Win7:/TestAutoImages/Hosts/ProTools11/Win7/Ozone5PluginGraphic.bmp"),
	Host_ProTools11_BreakTweakerStereo				("Win7:/TestAutoImages/Hosts/ProTools11/Win7/Inserts/iZotope BreakTweaker (stereo).bmp"),
	Host_ProTools11_BreakTweakerStereoHighlighted	("Win7:/TestAutoImages/Hosts/ProTools11/Win7/Inserts/iZotope BreakTweaker (stereo) Highlighted.bmp"),
	Host_ProTools11_Ozone5Stereo					("Win7:/TestAutoImages/Hosts/ProTools11/Win7/Inserts/iZotope Ozone 5 (stereo).bmp"),
	Host_ProTools11_Ozone5StereoHighlighted			("Win7:/TestAutoImages/Hosts/ProTools11/Win7/Inserts/iZotope Ozone 5 (stereo) Highlighted.bmp"),
	Host_ProTools11_AvidSplashScreenLogo			("/TestAutoImages/Hosts/ProTools11/Win7/PT Avid Splash Screen Logo.bmp"),
	Host_ProTools11_InsertWindowTopLeft				("/TestAutoImages/Hosts/ProTools11/Win7/InsertWindowTopLeft.bmp"),
	Host_ProTools11_DeleteButton					("/TestAutoImages/Hosts/ProTools11/Win7/DeleteButton.bmp"),
	Host_ProTools11_ImportCancel					("/TestAutoImages/Hosts/ProTools11/ImportCancel.bmp"),
	Host_ProTools11_ImportAudioProcessing			("/TestAutoImages/Hosts/ProTools11/ImportAudioProcessing.bmp"),
	Host_ProTools11_CategiZotope					("/TestAutoImages/Hosts/ProTools11/InsertCat_iZotope.bmp"),
	;
	
	private HashMap<String, String> m_Values= new HashMap<String, String>();
	private String 					m_strOS= "DEFAULT";
	
	/**
	 * Constructor
	 * 
	 * @param imagePath
	 */
	private Images( String... imagePath ) {
		for( int i= 0; i < imagePath.length; i++ ) {
			String[] strTokens= imagePath[i].split( ":" );
			if( strTokens.length > 1 )
				this.m_Values.put( strTokens[0], strTokens[1] );
			else
				this.m_Values.put( "DEFAULT", strTokens[0] );
		}
	}
		
	/**
	 * Returns the String image file path for the enum applying the _For( strOS ) if specified or the default if not specified.
	 * 
	 * @return String
	 * @throws Exception
	 */
	public String _GetValue() throws Exception {
		// Check for explicitly defined m_strOS or DEFAULT
		if( this.m_Values.containsKey(this.m_strOS) )
			return System.getProperty("user.dir").replace("\\", "/") + this.m_Values.get( this.m_strOS );
		// If DEFAULT but no DEFAULT explicitly defined then return the first key value
		if( this.m_strOS == "DEFAULT" ) {
			return System.getProperty("user.dir").replace("\\", "/") + this.m_Values.entrySet().iterator().next().getValue();
		}
		// The explicitly defined m_strOS was not found so throw an exception
		throw new Exception( "Image not defined for specified OS: " + this.m_strOS );	
	}
	
	/**
	 * Sets the desired OS image path to return for this image when _GetValue() is called.
	 * Maps to values returned by _Testbed()._RemoteServer()._SysInfo()._GetOSName()
	 * 
	 * @param strOS : "OSX", "WIN7", "WIN8"  
	 * @return this Enum
	 */
	public Images _For( String strOS ) {
		this.m_strOS= strOS;
		return this;
	}
	
	/**
	 * Searches the list of images for one with a specified filename
	 * 
	 * @param strFileName The filename of the image to find
	 * @return Enum with matching filename
	 * @throws Exception
	 */
	public Images _GetEnumByFileName( String strFileName ) throws Exception {
		for( Images val : Images.values() )
			for( String strVal : val.m_Values.keySet() ) 
				if( val.m_Values.get( strVal ).endsWith( "/" + strFileName + ".bmp" ))
					return val;
    	
		throw new Exception( "Image with filename not found: " + strFileName );	
	}

}
