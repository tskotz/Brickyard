package AutomationToolbox.src;

/**
 * 
 * @author terryskotz
 *
 */
public class JobDescriptor
{
	public String mstrJobName;
	public String mstrUser;
	public String mstrDataparams;
	public String mstrClasspath;
	public String mstrOptArgs;
	
	/**
	 * 
	 */
	public JobDescriptor()
	{
		this(null, null, null, null, null);
	}
	
	/**
	 * 
	 * @param strRESTQuery
	 */
	public JobDescriptor( String strRESTQuery )
	{
		for( String strParam : strRESTQuery.split( "&" ) ) {
			System.out.println( strParam );
			String[] aElementInfo= strParam.split( "=" );
			if( aElementInfo.length == 2 ) {
				if( aElementInfo[0].equalsIgnoreCase("jobname") )
					this.mstrJobName= aElementInfo[1];
				else if( aElementInfo[0].equalsIgnoreCase("user") )
					this.mstrUser= aElementInfo[1];
				else if( aElementInfo[0].equalsIgnoreCase("dataparamfile") )
					this.mstrDataparams= (this.mstrDataparams==null ? "" : this.mstrDataparams+"\n") + aElementInfo[1];
				else if( aElementInfo[0].equalsIgnoreCase("classpath") )
					this.mstrClasspath= aElementInfo[1];
				else if( aElementInfo[0].equalsIgnoreCase("commandlineargs") )
					this.mstrOptArgs= aElementInfo[1];					
			}
		}
	}
	
	/**
	 * 
	 * @param strJobName
	 * @param strUser
	 * @param strDataparams
	 * @param strClasspath
	 * @param strOptArgs
	 */
	public JobDescriptor( String strJobName, String strUser, String strDataparams, String strClasspath, String strOptArgs )
	{
		this.mstrJobName= strJobName.equals("null") ? "" : strJobName;
		this.mstrUser= strUser.equals("null") ? "" : strUser;
		this.mstrDataparams= strDataparams.equals("null") ? "" : strDataparams;
		this.mstrClasspath= strClasspath.equals("null") ? "" : strClasspath;
		this.mstrOptArgs= strOptArgs.equals("null") ? "" : strOptArgs;
	}
}
