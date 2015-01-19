package iZomateCore.TestCore;

import org.jdom.Element;

/**
 * 
 */
public class TestParameter {
	public String m_strName;
	public String m_strType;
	public String m_strValue;
	
	public TestParameter( String strName, String strType, String strValue ) {
		this.m_strName= strName;
		this.m_strType= strType;
		this.m_strValue= strValue;
	}
	
	public TestParameter( Element e ) {
		this( e.getAttributeValue( "name" ), e.getAttributeValue( "type" ), e.getAttributeValue( "value" ));
	}

    public boolean _Equals(TestParameter param) {
        return _ToString().equals( param._ToString() );
    }
	
	public boolean _GetBoolValue() throws Exception {
		if( !this.m_strType.equalsIgnoreCase( "Boolean" ) && !this.m_strType.equalsIgnoreCase( "UNDEFINED" ) )
			throw new Exception( "TestParameter " + this.m_strName + " is trying to be accessed as a bool when its type is " + this.m_strType );
		
		return Boolean.parseBoolean( this.m_strValue );
	}

	public float _GetFloatValue() throws Exception {
		if( !this.m_strType.equalsIgnoreCase( "Float" ) && !this.m_strType.equalsIgnoreCase( "UNDEFINED" ) )
			throw new Exception( "TestParameter " + this.m_strName + " is trying to be accessed as a float when its type is " + this.m_strType );

		return Float.valueOf( this.m_strValue );
	}

	public double _GetDoubleValue() throws Exception {
		if( !this.m_strType.equalsIgnoreCase( "Double" ) && !this.m_strType.equalsIgnoreCase( "UNDEFINED" ) )
			throw new Exception( "TestParameter " + this.m_strName + " is trying to be accessed as a double when its type is " + this.m_strType );

		return Double.valueOf( this.m_strValue );
	}

	public int _GetIntValue() throws Exception {
		if( !this.m_strType.equalsIgnoreCase( "Int" ) && !this.m_strType.equalsIgnoreCase( "UNDEFINED" ) )
			throw new Exception( "TestParameter " + this.m_strName + " is trying to be accessed as an int when its type is " + this.m_strType );
		
		return Integer.parseInt( this.m_strValue );
	}

	public String _GetStrValue() throws Exception {
		if( !this.m_strType.equalsIgnoreCase( "String" ) && !this.m_strType.equalsIgnoreCase( "UNDEFINED" ) )
			throw new Exception( "TestParameter " + this.m_strName + " is trying to be accessed as a String when its type is " + this.m_strType );
		
		return this.m_strValue;
	}

	public String _ToString() {
		return "name: " + this.m_strName + ", type: " + this.m_strType + ", value: " + this.m_strValue;
	}

}
