package iZomateCore.TestCore;

import iZomateCore.UtilityCore.TimeUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import iZomateCore.TestCore.TestParameter;

public class TestCaseParameters {
	HashMap<String, TestParameter>	m_TestcaseParams;
	
	public TestCaseParameters( HashMap<String, TestParameter> TestParameters ) {
		this.m_TestcaseParams= TestParameters;
	}
	
	public void _AddParams( HashMap<String, TestParameter> TestParameters ) throws Exception {
		this.m_TestcaseParams.putAll( TestParameters );	
//		System.out.print( _ToString("_AddParams") );
	}
	
	public String _ToString( String strCustom ) throws Exception {
		String str= "TestCase: " + _GetTestCaseName() + " - " + strCustom + "\n";
	    Iterator<Entry<String, TestParameter>> it = this.m_TestcaseParams.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, TestParameter> pairs = (Map.Entry<String, TestParameter>)it.next();
	        str += "  " + pairs.getValue()._ToString() + "\n";	        
	    }
	    return str;
	}
	
	/////////////////////////////////////////////////////////////////////////////////
	// Generic Get Methods
	/////////////////////////////////////////////////////////////////////////////////
	public boolean _HasParam( String strParam ) {
		return this.m_TestcaseParams.get( strParam ) != null;
	}

    public TestParameter _GetParam( String strParam ){
        return this.m_TestcaseParams.get( strParam );
    }

	public String _GetString( String strParam ) throws Exception {
		TestParameter pParam= this.m_TestcaseParams.get( strParam );
		
		if( pParam == null)
			throw new Exception( "Param not found: " + strParam );

		return pParam._GetStrValue();
	}
	
	public String _GetString( String strParam, String strDefault ) throws Exception {
		TestParameter pParam= this.m_TestcaseParams.get( strParam );
		
		if( pParam == null)
			return strDefault;
		
		return pParam._GetStrValue();
	}
	
	public int _GetInt( String strParam ) throws Exception {
		TestParameter pParam= this.m_TestcaseParams.get( strParam );
		
		if( pParam == null)
			throw new Exception( "Param not found: " + strParam );
		
		return pParam._GetIntValue();
	}

	public int _GetInt( String strParam, int nDefault ) throws Exception {
		TestParameter pParam= this.m_TestcaseParams.get( strParam );
		
		if( pParam == null || pParam.m_strValue.isEmpty())
			return nDefault;
		
		return pParam._GetIntValue();
	}

	public boolean _GetBool( String strParam ) throws Exception {
		TestParameter pParam= this.m_TestcaseParams.get( strParam );
		
		if( pParam == null)
			throw new Exception( "Param not found: " + strParam );
		
		return pParam._GetBoolValue();
	}

	public boolean _GetBool( String strParam, boolean bDefault ) throws Exception {
		TestParameter pParam= this.m_TestcaseParams.get( strParam );
		
		if( pParam == null)
			return bDefault;
		
		return pParam._GetBoolValue();
	}

	public float _GetFloat( String strParam ) throws Exception {
		TestParameter pParam= this.m_TestcaseParams.get( strParam );

		if( pParam == null)
			throw new Exception( "Param not found: " + strParam );
		
		return pParam._GetFloatValue();
	}

	public float _GetFloat( String strParam, float nDefault ) throws Exception {
		TestParameter pParam= this.m_TestcaseParams.get( strParam );

		if( pParam == null)
			return nDefault;

		return pParam._GetFloatValue();
	}

	public double _GetDouble( String strParam, double nDefault ) throws Exception {
		TestParameter pParam= this.m_TestcaseParams.get( strParam );
		
		if( pParam == null)
			return nDefault;
		
		return pParam._GetDoubleValue();
	}

	/////////////////////////////////////////////////////////////////////////////////
	// Specific Parameter Get Methods
	/////////////////////////////////////////////////////////////////////////////////

	public String _GetMainClass() throws Exception {
		return this._GetString( "mainClass" );		
	}
	
	public String _GetTestScriptName() throws Exception {
		return this._GetString( "mainClass" ).substring( this._GetString( "mainClass" ).lastIndexOf( "." )+1 );		
	}

	public String _GetTestCaseName() throws Exception {
		return this._GetString( "testcaseName", "UNNAMED TESTCASE" );		
	}

	public String _GetApp() throws Exception {
		return this._GetString( "app", "" ).replace("\\", "/");
	}
	
	public String _GetLogDir() throws Exception {
		return this._GetString( "logDir" ).replace("\\", "/");
	}

	public String _GetTestbed() throws Exception {
		return this._GetString( "testbed" );
	}
	
	public boolean _GetSubmitCrashReport() throws Exception {
		return this._GetBool( "submitCrashReport", false );
	}

	public boolean _GetEcho() throws Exception {
		return this._GetBool( "echo", false );
	}

	public boolean _GetHideAllWinAtStart() throws Exception {
		return this._GetBool( "hideAllWinAtStart", false );
	}
	
	public boolean _GetQuitWhenComplete() throws Exception {
		return this._GetBool( "quitWhenComplete", false );
	}
	
	public int _GetTestDuration() throws Exception {
		return this._GetInt( "testDuration", 30 );
	}
	
	public int _GetPresetPlayTime() throws Exception {
		return this._GetInt( "presetPlayTime", 0 );
	}

	public double _GetBuildTime() throws Exception {
		return this._GetDouble( "buildTime", -1 );
	}

	public int _GetDefaultTimeout() throws Exception {
		int nTimeOut= this._GetInt( "defaultTimeout", TimeUtils._GetDefaultTimeout() );
		if( nTimeOut != TimeUtils._GetDefaultTimeout() )
			TimeUtils._SetDefaultTimeout( nTimeOut );
		
		return nTimeOut;
	}
	
	public boolean _GetUseListBox() throws Exception {
		return this._GetBool( "useListBox", false );
	}
	
	public String _GetEmailRecipients() throws Exception {
		return this._GetString( "emailRecipients", null );
	}

	public String _GetEmailSender() throws Exception {
		return this._GetString( "emailSender", "TestAutomation" ).replace( " ", "" ); // no whitespace
	}

    public boolean _GetSendPassedEmails() throws Exception {
        return this._GetBool( "sendPassedEmails", true);
    }

	public String _GetResultsLink( String strPlatform ) throws Exception {
		// Check for platform specific results link
		if( this._GetString( "resultsLink" + strPlatform, null ) != null )
			return this._GetString( "resultsLink" + strPlatform, null );
		else // Old generic results link
			return this._GetString( "resultsLink", null );
	}
	
	public String _GetPitchMode() throws Exception {
		//TODO: Return enum
		return this._GetString( "pitchMode", null );
	}

	public String _GetAudioFile() throws Exception {
		return this._GetString( "audioFile", null );
	}

	public String _GetDataDir() throws Exception {
		return this._GetString( "dataDir", null );
	}

    public Boolean _GetIsGold() throws Exception {
        return this._GetBool( "isGold", false );
    }

	public String _GetCPUPerformanceFile() throws Exception {
		String strDefault= this._GetDataDir()==null ? null : (this._GetDataDir() + "/" + this._GetString( "plugin", "" ) + "_CPUPerformance.txt");
		return this._GetString( "cpuPerformanceFile", strDefault );
	}

	public String _GetBuildTimesFile() throws Exception {
		String strDefault= this._GetDataDir()==null ? null : (this._GetDataDir() + "/" + this._GetString( "plugin", "" ) + "_BuildTimes.txt");
		return this._GetString( "buildTimesFile", strDefault );
	}

	public String _GetMemoryUsageFile() throws Exception {
		String strDefault= this._GetDataDir()==null ? null : (this._GetDataDir() + "/" + this._GetString( "plugin", "" ) + "_MemoryUsage.txt");
		return this._GetString( "memoryUsageFile", strDefault );
	}

	public String _GetPrestLoadTimesFile() throws Exception {
		String strDefault= this._GetDataDir()==null ? null : (this._GetDataDir() + "/" + this._GetString( "plugin", "" ) + "_PresetLoadTimes.txt");
		return this._GetString( "presetLoadTimesFile", strDefault );
	}

	public String _GetUIInstantiationTimesFile() throws Exception {
		String strDefault= this._GetDataDir()==null ? null : (this._GetDataDir() + "/" + this._GetString( "plugin", "" ) + "_UIInstantationTimes.txt");
		return this._GetString( "uiInstantiationTimesFile", strDefault );
	}

	public String _GetDSPInstantiationTimesFile() throws Exception {
		String strDefault= this._GetDataDir()==null ? null : (this._GetDataDir() + "/" + this._GetString( "plugin", "" ) + "_DSPInstantationTimes.txt");
		return this._GetString( "dspInstantiationTimesFile", strDefault );
	}

	public String _GetPlugin() throws Exception {
		return this._GetString( "plugin", null );
	}

	public boolean _GetForceNewInstanceOnStart() throws Exception {
		return this._GetBool( "forceNewInstanceOnStart", false );
	}

	public String _GetSSHUserAtHost() throws Exception {
		return this._GetString( "sshUser@Host", null );
	}

	public String _GetSSHRemoteArchiveDir() throws Exception {
		return this._GetString( "sshRemoteArchiveDir", null );
	}

}
