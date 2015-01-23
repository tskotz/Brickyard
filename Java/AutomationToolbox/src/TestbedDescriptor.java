package AutomationToolbox.src;

public class TestbedDescriptor {
	public String mstrTestbed;
	public String mstrValue;
	public String mstrType;
	public String mstrRunMode;
	public String mstrDescription;
		
	/**
	 * 
	 */
	public TestbedDescriptor()
	{
		this(null, null, null, null, null);
	}
	
	/**
	 * 
	 * @param strRESTQuery
	 */
	public TestbedDescriptor( String strRESTQuery )
	{
		for( String strParam : strRESTQuery.split( "&" ) ) {
			System.out.println( strParam );
			String[] aElementInfo= strParam.split( "=" );
			if( aElementInfo.length == 2 ) {
				if( aElementInfo[0].equalsIgnoreCase("name") )
					this.mstrTestbed= aElementInfo[1];
				else if( aElementInfo[0].equalsIgnoreCase("value") )
					this.mstrValue= aElementInfo[1];
				else if( aElementInfo[0].equalsIgnoreCase("type") )
					this.mstrType= aElementInfo[1];
				else if( aElementInfo[0].equalsIgnoreCase("runmode") )
					this.mstrRunMode= aElementInfo[1];
				else if( aElementInfo[0].equalsIgnoreCase("descr") )
					this.mstrDescription= aElementInfo[1].replace(";", " ");					
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
	public TestbedDescriptor( String strTestbed, String strValue, String strType, String strRunMode, String strDescription )
	{
		this.mstrTestbed= strTestbed.equals("null") ? "" : strTestbed;
		this.mstrValue= strValue.equals("null") ? "" : strValue;
		this.mstrType= strType.equals("null") ? "" : strType;
		this.mstrRunMode= strRunMode.equals("null") ? "" : strRunMode;
		this.mstrDescription= strDescription.equals("null") ? "" : strDescription.replace(";", " ");
	}
	
	public String _ToRESTReply()
	{
		return this.mstrTestbed + ";" + this.mstrValue + ";" + this.mstrType + ";" + this.mstrRunMode + ";" + this.mstrDescription;		
	}

}
