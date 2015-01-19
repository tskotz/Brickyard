package AutomationToolbox.src;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class JobData {
	
	private File m_fJobXML= null;
	public String m_strTimestamp;
	public String m_strUser;
	public String m_strCmdLineArgs= "";
	public	List<String> 		m_strPlatforms= new ArrayList<String>();
	public	List<String> 		m_strTestbeds= new ArrayList<String>();
	public	List<String>		m_strClasspath= new ArrayList<String>();
	public	List<DataparamFileInfo>	m_fTests= new ArrayList<DataparamFileInfo>();
	
	public JobData( File fJobRequest ) throws Exception {
		this._readJobFile( fJobRequest );
	}
	
	/**
	 * @throws Exception 
	 * @throws JDOMException 
	 * 
	 */
	private void _readJobFile( File fJobRequest ) throws Exception {						
        SAXBuilder builder = new SAXBuilder(false);
        Document doc;

		this.m_fJobXML= fJobRequest;
		doc= builder.build( this.m_fJobXML );
		//Get the root element
	    Element root= doc.getRootElement();
	        	      
	    this.m_strPlatforms.add( "TBD" );
        this.m_strTimestamp= 	this._getChildText( JobTags.Timestamp.toString(), root, null );
	    this.m_strUser= 	 	this._getChildText( JobTags.User.toString(), root, null );
	        
		List<?> strItems= root.getChildren( JobTags.CommandLineArgs.toString() );
        for( Object objItem : strItems )
        	if( !((Element)objItem).getText().isEmpty() )
        		this.m_strCmdLineArgs+= (this.m_strCmdLineArgs.isEmpty() ? "" : " ") + ((Element)objItem).getText();

        strItems= root.getChildren( JobTags.Platform.toString() );
        for( Object objItem : strItems )
        	if( !((Element)objItem).getText().isEmpty() )
        		this.m_strPlatforms.add( ((Element)objItem).getText() );

        strItems= root.getChildren( JobTags.Classpath.toString() );
        for( Object objItem : strItems )
        	if( !((Element)objItem).getText().isEmpty() )
        		this.m_strClasspath.add( ((Element)objItem).getText() );
	        
        strItems= root.getChildren( JobTags.DataParamFile.toString() );
        for( Object objItem : strItems ) {
        	if( !((Element)objItem).getText().isEmpty() ) {
        		DataparamFileInfo pDPInfo= new DataparamFileInfo( (Element)objItem );
        		this.m_fTests.add( pDPInfo );
        		if( pDPInfo._GetGroup() != null ) {
        			if( !this.m_strTestbeds.contains( pDPInfo._GetGroup() ) )
        					this.m_strTestbeds.add( pDPInfo._GetGroup() );
        		}
        		else
        			this.m_strTestbeds.add( pDPInfo._GetTestbed() ); 
        	}
        }
	}

	/**
	 * 
	 * @param strChild
	 * @param eRoot
	 * @return
	 */
	private String _getChildText( String strChild, Element eRoot, String strDefault ) {
		try {
			return eRoot.getChild( strChild ).getText();
		} catch ( Exception e ) {
			return strDefault;
		}
	}
	
	public class DataparamFileInfo {
		Element m_E;
		File m_f;
		
		public DataparamFileInfo( Element e ) {
			m_E= e;
			m_f= new File(m_E.getText());
		}
		
		public File _GetFile() {
			return m_f;
		}

		public String _GetTestbed() {
			return m_E.getAttributeValue("testbed");
		}

		public String _GetGroup() {
			return m_E.getAttributeValue("group");
		}
		
		public String _GetTestbedAndGroup() {
			return (this._GetGroup() != null ? this._GetGroup()+":" : "") + this._GetTestbed();
		}

	}

}
