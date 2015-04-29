package AutomationToolbox.src;

public class DataParameter {
	public String mstrName;
	public String mstrValue;
	public String mstrType;
	public boolean mbAsList;
	public String mstrDescription;
		
	/**
	 * 
	 */
	public DataParameter()
	{
		this(null, null, null, false, null);
	}
	
	/**
	 * 
	 * @param strRESTQuery
	 */
	public DataParameter( String strRESTQuery )
	{
		for( String strParam : strRESTQuery.split( "&" ) ) {
			System.out.println( strParam );
			String[] aElementInfo= strParam.split( "=" );
			if( aElementInfo.length == 2 ) {
				if( aElementInfo[0].equalsIgnoreCase("name") )
					this.mstrName= aElementInfo[1];
				else if( aElementInfo[0].equalsIgnoreCase("value") )
					this.mstrValue= aElementInfo[1];
				else if( aElementInfo[0].equalsIgnoreCase("type") )
					this.mstrType= aElementInfo[1];
				else if( aElementInfo[0].equalsIgnoreCase("runmode") )
					this.mbAsList= Boolean.getBoolean(aElementInfo[1]);
				else if( aElementInfo[0].equalsIgnoreCase("descr") )
					this.mstrDescription= aElementInfo[1];					
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
	public DataParameter( String strTestbed, String strType, String strValue, boolean bAsList, String strDescription )
	{
		this.mstrName= strTestbed.equals("null") ? "" : strTestbed;
		this.mstrValue= strValue.equals("null") ? "" : strValue;
		this.mstrType= strType.equals("null") ? "" : strType;
		this.mbAsList= bAsList;
		this.mstrDescription= strDescription.equals("null") ? "" : strDescription.replace(";", " ");
	}
	
	public String _AsBasic()
	{
		return this.mstrName + ";" + this.mstrValue + ";" + this.mstrType + ";" + this.mbAsList + ";" + this.mstrDescription;		
	}

	public String _AsREST()
	{
		return "name=" + this.mstrName + "&value=" + this.mstrValue + "&type=" + this.mstrType + "&aslist=" + this.mbAsList + "&descr=" + this.mstrDescription;		
	}

}
