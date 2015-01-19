package iZomateCore.TestCore;

import iZomateCore.UtilityCore.HttpUtils;

//! How to talk to hopper
public class Hopper {
	// Status Enum. See result table in hopper
    public final static String STATUS_NONE = "none";
    public final static String STATUS_PASSED = "passed";
    public static final String STATUS_WARNING = "warning";
    public final static String STATUS_FAILED = "failed";
    public final static String STATUS_RUNNING = "running";

    //TODO: change this to use new database
    private static String m_strUrl= "http://127.0.0.1:8000"; //! Where Hopper lives. Localhost might be "http://127.0.0.1:8000"
    private static boolean m_bEnabled = false; //! Disable all hopper functions when false.


    //! Disable/Enables all contact with hopper.
    public static void SetEnabled(boolean bEnabled) { m_bEnabled= bEnabled; }
    public static boolean IsEnabled() { return m_bEnabled; }

    //! Get/Set the url to hopper. ex: 'http://hopper.izotope.int'
    public static void SetUrl(String strUrl ) { m_strUrl = strUrl; }
    public static String GetUrl() { return m_strUrl; }

    //! Register a new Test to hopper
	public static int AddTest( String strTestName, String strHostName, boolean isdev, String strTeam ) {
        if( !m_bEnabled )return 0;

        // Remove the "iZomate.iZTest...."
        String strParsedName = strTestName.substring( strTestName.lastIndexOf('.')+1 );

        String[] names = { "name", "hostname", "isdev", "team" };
        String[] values = { strParsedName, strHostName, isdev ? "1":"0", strTeam };

        String id= HttpUtils.Get( m_strUrl+"/auto/add/testset", names, values).trim();
        return Integer.valueOf(id);
	}

    //! Register a new TestCase to hopper
    public static int AddTestCase( String strTestCaseName, int idTest, String strProductName, String strProductVersion) {
        if( !m_bEnabled )return 0;

        if(idTest == 0) return 0;// throw new Exception("Invalid Test ID");

        String[] names = { "name", "testid", "productname", "productversion" };
        String[] values = { strTestCaseName, String.valueOf(idTest), strProductName, strProductVersion };
        String id= HttpUtils.Get( m_strUrl+"/auto/add/testcase", names, values).trim();
        return Integer.valueOf(id);
    }
	
    //! Tell hopper we are done with this test
    public static void FinishedTestSet( int idTest ) {
        if( !m_bEnabled )return;
        if(idTest == 0) return; //throw new Exception("Invalid Test ID");

        String[] names = { "id" };
        String[] values = { String.valueOf(idTest) };
        String result= HttpUtils.Get( m_strUrl+"/auto/done/testset", names, values).trim();
        //if( !result.equals("1") ) throw new Exception("Finished Test post failed.");
    }

    //! Tell hopper we are done with this test case
    public static void FinishedTestCase( int idTest, String strStatus  ) {
        if( !m_bEnabled )return;

        if(idTest == 0) return; //throw new Exception("Invalid Test ID");
        if( !strStatus.equals(STATUS_NONE) && !strStatus.equals(STATUS_PASSED) && !strStatus.equals(STATUS_FAILED) && !strStatus.equals(STATUS_RUNNING) )
            return; //throw new Exception("Invalid strStatus. Use Hopper.STATUS_NONE,STATUS_PASSED, etc ");

        String[] names = { "id", "result" };
        String[] values = { String.valueOf(idTest), strStatus };
        String result= HttpUtils.Get( m_strUrl+"/auto/done/testcase", names, values).trim();
        //if( !result.equals("1") ) throw new Exception("Finished test case post failed.");
    }

    //! Add a new param to hopper
    public static void PostValue( String strOwnerType, int idOwner, String strContext, String strName, String strType, String strValue ) {
        if( !m_bEnabled )return;

        if( !strOwnerType.equalsIgnoreCase("testset") && !strOwnerType.equalsIgnoreCase("testcase") )
            return; //throw new Exception("Invalid owner type");

        if( !strContext.equalsIgnoreCase("parameter") && !strContext.equalsIgnoreCase("result") )
            return; //throw new Exception("Invalid context");

        if( idOwner == 0 ) return;// throw new Exception("Invalid owner id");

        String[] names = { "ownertype", "ownerid", "paramcontext", "paramname", "paramtype", "paramvalue" };
        String[] values = { strOwnerType, String.valueOf(idOwner), strContext,  strName, strType, strValue };
        String result= HttpUtils.Get( m_strUrl+"/value/add", names, values).trim();
        //if( result.equals("0") ) throw new Exception("Posting Param failed. " + result);
    }
}
