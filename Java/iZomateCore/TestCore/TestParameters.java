package iZomateCore.TestCore;

import iZomateCore.UtilityCore.TimeUtils;
import iZomateCore.TestCore.Hopper;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestParameters {
	private HashMap<String, TestParameter> 		m_CommonParams= new HashMap<String, TestParameter>();
	private ArrayList< TestCaseParameters > 	m_TestCases= new ArrayList<TestCaseParameters>();
	private	int									m_NextTC= 0;
	private TestCaseParameters					m_CommonTestCaseParameters= null;

	/**
	 * 
	 * @param args : A list of key value pairs in the format set to "-<key>", "value".
     *             : '-paramsFile' will load up the xml file. Any params pass into args will overwrite what is in the params file.
	 * @throws Exception
	 */

	public TestParameters( String[] args ) throws Exception {
        // Check to see if a paramsFile was specified. We do this first so that other params will overwrite if needed.
        for (int i = 0; i < args.length-1; ++i) {
            if( args[i].equals( "-paramsFile" ))
                this._importParamsFile( args[i+1].toString() );
        }

        // Load other params
		for (int i = 0; i < args.length; ++i) {
			// Filter out the starting '-'
			if ( i+1 < args.length && !args[i+1].startsWith( "-" ))
				this._addUndefinedTypeParam( args[i].substring( 1 ), args[++i]);
			else // treat everything else as a boolean
				this._addCommonParam(args[i].substring( 1 ), true);
		}

        // Should we post to hopper?
        if( this.m_CommonParams.containsKey( "nohopper") ){
            Hopper.SetEnabled( !this.m_CommonParams.get( "nohopper" )._GetBoolValue() );
        }

        // Set the url for hopper to post to. Default is hopper.izotope.int
        if( this.m_CommonParams.containsKey( "hopperurl") ){
            Hopper.SetUrl( this.m_CommonParams.get( "hopperurl" )._GetStrValue() );
        }

        // Set the default timeout
		if( this.m_CommonParams.containsKey( "defaultTimeout") ) {
			TimeUtils._SetDefaultTimeout( this.m_CommonParams.get( "defaultTimeout" )._GetIntValue() );
        }
	}
	
	/**
	 * 
	 * @param strParam
	 * @param strVal
	 */
	private void _addUndefinedTypeParam( String strParam, String strVal ) {
		this._addCommonParam( strParam, new TestParameter( strParam, "UNDEFINED", strVal ) );
	}

	/**
	 * 
	 * @param strParam
	 * @param bVal
	 */
	private void _addCommonParam( String strParam, boolean bVal ) {
		this._addCommonParam( strParam, new TestParameter( strParam, "Boolean", bVal?"true":"false" ) );
	}

	/**
	 * 
	 * @param strParam
	 * @param eParam
	 */
	private void _addCommonParam( String strParam, TestParameter eParam ) {
		System.out.println( "Adding: " + strParam + " : " + eParam._ToString() );
		this.m_CommonParams.put( strParam, eParam );
	}
	
	/**
	 * 
	 * @return
	 */
	public TestCaseParameters _GetCommonParams() {
		if( this.m_CommonTestCaseParameters == null )
			this.m_CommonTestCaseParameters= new TestCaseParameters( this.m_CommonParams );
		return this.m_CommonTestCaseParameters;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean _HasNextTestCase() {
		return ( this.m_NextTC < this.m_TestCases.size() );
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public TestCaseParameters _GetNextTestCase() throws Exception {
		if( this.m_TestCases != null &&	 this.m_NextTC < this.m_TestCases.size() ) {			 			
 			@SuppressWarnings("unchecked")
			TestCaseParameters tcParams= new TestCaseParameters( (HashMap<String, TestParameter>) this.m_CommonParams.clone() );
 			tcParams._AddParams( this.m_TestCases.get(this.m_NextTC++).m_TestcaseParams );
				 	
 			return tcParams;
		}
		return null;
	}
	
	/**
	 * 
	 * @param elem
	 * @return
	 */
	private String _getTestCaseName( Element elem ) {
		if( elem != null) {		
			List<?> eTCParams= elem.getChildren( "Parameter" );
			 for( Object o : eTCParams ) {
				 if( ((Element)o).getAttributeValue("name").equals ("testcaseName") )
					return ((Element)o).getAttributeValue("value").toString();					 
			 }
		}
		return "";
	}
	
	/**
	 * 
	 * @param strTestCaseName
	 * @return
	 * @throws Exception
	 */
	private int _getTestCase( String strTestCaseName ) throws Exception {
		if( ! strTestCaseName.isEmpty() ) {			
			 for( int i= 0; i < this.m_TestCases.size(); ++i ) {
				 if( this.m_TestCases.get(i)._GetTestCaseName().equals( strTestCaseName ) )
					 return i;			
			 }
		}
		return -1;
	}
	
	/**
	 * 
	 * @param elemTestCase
	 * @throws Exception
	 */
	private void _addTestCase( Element elemTestCase ) throws Exception {
		HashMap<String, TestParameter> tcParams= new HashMap<String, TestParameter>();				
		List<?> eTCParams= elemTestCase.getChildren( "Parameter" );
		for( Object o : eTCParams )
		     tcParams.put( ((Element)o).getAttributeValue( "name" ), new TestParameter( (Element)o ) );
		
		int iElem= _getTestCase( this._getTestCaseName(elemTestCase) );
		if( iElem < 0 )
			this.m_TestCases.add( new TestCaseParameters(tcParams) );				
		else
			this.m_TestCases.get(iElem)._AddParams( tcParams ); // Append a Test case with these new params.
	}

	/**
	 * 
	 * @param strFilepath
	 * @throws Exception
	 */
	private void _importParamsFile( String strFilepath ) throws Exception {
		if( strFilepath == null )
			return;
		
		File paramsFile= new File( strFilepath );
		if( !paramsFile.exists() )
			throw new Exception( "The Params File '" + strFilepath + "' could not be found" );
		
		SAXBuilder builder = new SAXBuilder( false );
		Document doc= builder.build( paramsFile );
        Element root= doc.getRootElement();
	        
		List<?> eCommonParams= root.getChildren( "Parameter" );
		for( Object o : eCommonParams ) {
			Element elem= ((Element)o);
			String strName= elem.getAttributeValue( "name" );
			if( strName.equals("include") ) {				
				String path= paramsFile.getParent() + File.separator + elem.getAttributeValue( "value" );
				if( paramsFile.equals( path ) )
					throw new Exception( "The Params File '" + strFilepath + "' included itself." );
				this._importParamsFile( path );
			} 
			else
				this.m_CommonParams.put( strName, new TestParameter( elem) );
		}		
		
		List<?> eTestcases= root.getChildren( "Testcase" );
        for( int i= 0; i < eTestcases.size(); ++i )
        	this._addTestCase( (Element)eTestcases.get( i ) );
	}

}
