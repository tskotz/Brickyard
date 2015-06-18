/**
 * 
 */
package AutomationToolbox.src;

/**
 * @author tskotz
 *
 */
public enum JobTags {
	Job( "Job" ),
	JobName( "JobName" ),
	JobTemplate( "JobTemplate" ),
	JobUID( "JobUID" ),
	Timestamp( "Timestamp" ),
	User( "User" ),
	Platform( "Platform" ), // obsolete
	Testbed( "Testbed" ), //obsolete
	Classpath( "Classpath" ),
	CommandLineArgs( "CommandLineArgs" ),
	DataParamsDir( "DataParamsDir" ),
	DataParamFile( "DataParamFile" ),
	Origin( "Origin" )
	;
	
	private final String mData;
	
	/**
	 * 
	 * @param strTag
	 */
	JobTags( String strTag ) {
		this.mData= strTag;
	}

	/**
	 * 
	 * @param strTagToFind
	 * @return
	 * @throws Exception
	 */
	public static String _GetTag( String strTagToFind ) throws Exception {
		for( JobTags val : JobTags.values() )
			if( val.mData.equalsIgnoreCase( strTagToFind ) )
				return val.mData;
    	
		throw new Exception( "Unsupported JobTag: " + strTagToFind );
	}

}
