package AutomationToolbox.src;

import java.io.File;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class Preferences {
	
	static enum TYPES {
		DataparamsRootDir,
		DefaultJars,
		StagingDir,
		ShowJobCount,
		StartTestManagerOnLaunch;		
	}
	
	static final private String  	m_strPrefsFile= System.getProperty("user.dir") + "/AutomationToolbox/Preferences/Prefs.xml";
	static private Element 			m_eRoot= null;
	
	/**
	 * Constructor
	 */
	public Preferences() {
	}
	
	/**
	 * Get the string data for the specified preference name returning strDefaultValue if not found
	 * 
	 * @param strPrefName
	 * @return
	 * @throws Exception
	 */
	static public String _GetPref( Preferences.TYPES prefType, String strDefaultValue ) {
		if( m_eRoot == null )
			_readPrefsFile();
		
		try {
			return m_eRoot.getChild( prefType.name() ).getText();
		} catch ( Exception e ) {
			return strDefaultValue;
		}
	}

	/**
	 * Get the string data for the specified preference name or return null
	 * 
	 * @param strPrefName
	 * @return
	 * @throws Exception
	 */
	static public String _GetPref( Preferences.TYPES prefType ) {
		return _GetPref( prefType, null );
	}

	/**
	 * Forces a rescan of prefs file on next call to _GetPrefs
	 */
	static public void _Refresh() {
		m_eRoot= null;
	}
	
	/**
	 * Read data from prefs file
	 * 
	 * @throws Exception
	 * 
	 */
	static private void _readPrefsFile() {		
		File fPrefsFile= new File( m_strPrefsFile );	
        SAXBuilder builder = new SAXBuilder(false);
        Document doc;
		if( fPrefsFile.exists() ) {
			try {
				doc= builder.build( fPrefsFile );
		        m_eRoot= doc.getRootElement();
			} catch( Exception e ) {
				e.printStackTrace();
			}
		}
	}

}
