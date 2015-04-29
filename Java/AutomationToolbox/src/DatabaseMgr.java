package AutomationToolbox.src;

/*

Derby - Class SimpleApp

Copyright 2001, 2004 The Apache Software Foundation or its licensors, as applicable.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;


/**
* This sample program is a minimal JDBC application showing
* JDBC access to Derby.
*
* Instructions for how to run this program are
* given in <A HREF=example.html>example.html</A>.
*
* Derby applications can run against Derby running in an embedded
* or a client/server framework. When Derby runs in an embedded framework,
* the Derby application and Derby run in the same JVM. The application
* starts up the Derby engine. When Derby runs in a client/server framework,
* the application runs in a different JVM from Derby. The application only needs
* to start the client driver, and the connectivity framework provides network connections.
* (The server must already be running.)
*
* <p>When you run this application, give one of the following arguments:
*    * embedded (default, if none specified)
*    * derbyclient (will use the Net client driver to access Network Server)
*    * jccjdbcclient (if Derby is running embedded in the JCC Server framework)
*
* @author janet
*/
public class DatabaseMgr
{
	// Singleton instance
	private static DatabaseMgr sInstance= null;
	
	/* the default framework is embedded*/
	private final String mstrDriver=	"org.apache.derby.jdbc.EmbeddedDriver";
	private final String mstrProtocol= 	"jdbc:derby:";
	private final String mstrDBName=	"dbAutoToolbox";
	
	private final String mstrTableVersionsName= "TableVersions";
	private final String mstrTestbedTableName= "TestbedTable3";
	private final String mstrJobsTableName= "JobsTable";
	private final String mstrUsersTableName= "UsersTable";
	private final String mstrPreferencesTableName= "PreferencesTable4";
	private final String mstrDataParametersTableName= "DataParametersTable2";

	private TableVersionsWrapper mTableVersionsTable= null;
	private TestbedsTableWrapper mTestbedsTable= null;
	private JobsTableWrapper mJobsTable= null;
	private UsersTableWrapper mUsersTable= null;
	private PreferencesTableWrapper mPreferencesTable= null;
	private DataParametersTableWrapper mDataParametersTable= null;
	
	private Connection mConnection = null;

	/**
	 * Test code
	 * @param args
	 */
	public static void main(String[] args)
	{
		DatabaseMgr._Testbeds()._DeleteTestbed( "testbedname5" );
		DatabaseMgr._Testbeds()._DeleteTestbed( "testbedname5" );
		DatabaseMgr._Testbeds()._AddTestbed( "testbedname3", "testbedvalue3", "testbedtype3", "testbedRunMode3", "testbedDescription3" );
		DatabaseMgr._Testbeds()._GetTestbeds();
		DatabaseMgr._Testbeds()._AddTestbed( "testbedname4", "testbedvalue4", "testbedtype4", "testbedRunMode4", "testbedDescription4" );
		DatabaseMgr._Testbeds()._GetTestbeds();
		DatabaseMgr._Testbeds()._DeleteTestbed( "testbedname4" );
		DatabaseMgr._Testbeds()._GetTestbeds();
		DatabaseMgr._Testbeds()._AddTestbed( "testbedname5", "testbedvalue5", "testbedtype5", "testbedRunMode5", "testbedDescription5" );
		DatabaseMgr._Testbeds()._GetTestbeds();
		DatabaseMgr._Testbeds()._AddTestbed( "testbedname3", "testbedvalue3", "testbedtype3", "testbedRunMode3", "testbedDescription3" );
		DatabaseMgr._Testbeds()._GetTestbeds();
		DatabaseMgr._Testbeds()._UpdateTestbed( "testbedname3", "testbedname6", "testbedvalue6", "testbedtype6", "testbedRunMode6", "testbedDescription6" );
		DatabaseMgr._Testbeds()._GetTestbeds();
		DatabaseMgr._Testbeds()._DeleteTestbed( "testbedname5" );
		DatabaseMgr._Testbeds()._GetTestbeds();
		
		String[] tbs= DatabaseMgr._Testbeds()._GetTestbeds();
		TestbedDescriptor tbsv= DatabaseMgr._Testbeds()._GetTestbedDescriptor( "testbedname3" );

		System.out.println( tbs.toString() );
		System.out.println( tbsv );
		//DatabaseMgr.go( "dbAutoToolbox", "Testbeds" );
	}
	
	/**
	 * make the constructor private so that this class cannot be instantiated
	 */
	private DatabaseMgr()
	{	
	}
	
	/**
	 * Get the private instance of the DatabaseMgr singleton
	 * @return
	 */
	private static DatabaseMgr _GetInstance()
	{
		if( DatabaseMgr.sInstance == null )
		{
			DatabaseMgr.sInstance= new DatabaseMgr();
			DatabaseMgr.sInstance._initDB();
		}
		return DatabaseMgr.sInstance;
	}
	
	/**
	 * 
	 * @return
	 * @throws SQLException
	 */
	private Statement _CreateStatement() throws SQLException
	{
		return DatabaseMgr._GetInstance().mConnection.createStatement();
	}
	
	/**
	 * Table Versions Table Accessor
	 * @return
	 */
	public static TableVersionsWrapper _Versions()
	{
		// Create the Versions table if it does not exist
		if( DatabaseMgr._GetInstance().mTableVersionsTable == null )
			DatabaseMgr._GetInstance().mTableVersionsTable= DatabaseMgr._GetInstance().new TableVersionsWrapper();
		
		return DatabaseMgr._GetInstance().mTableVersionsTable;
	}

	/**
	 * Testbeds Table Accessor
	 * @return
	 */
	public static TestbedsTableWrapper _Testbeds()
	{
		// Create the testbeds table if it does not exist
		if( DatabaseMgr._GetInstance().mTestbedsTable == null )
			DatabaseMgr._GetInstance().mTestbedsTable= DatabaseMgr._GetInstance().new TestbedsTableWrapper();
		
		return DatabaseMgr._GetInstance().mTestbedsTable;
	}

	/**
	 * Jobs Table Accessor
	 * @return
	 */
	public static JobsTableWrapper _Jobs()
	{
		// Create the Jobs table if it does not exist
		if( DatabaseMgr._GetInstance().mJobsTable == null )
			DatabaseMgr._GetInstance().mJobsTable= DatabaseMgr._GetInstance().new JobsTableWrapper();
		
		return DatabaseMgr._GetInstance().mJobsTable;
	}

	/**
	 * Preferences Table Accessor
	 * @return
	 */
	public static PreferencesTableWrapper _Preferences()
	{
		// Create the Preferences table if it does not exist
		if( DatabaseMgr._GetInstance().mPreferencesTable == null )
			DatabaseMgr._GetInstance().mPreferencesTable= DatabaseMgr._GetInstance().new PreferencesTableWrapper();
		
		return DatabaseMgr._GetInstance().mPreferencesTable;
	}

	/**
	 * Users Table Accessor
	 * @return
	 */
	public static UsersTableWrapper _Users()
	{
		// Create the testbeds table if it does not exist
		if( DatabaseMgr._GetInstance().mUsersTable == null )
			DatabaseMgr._GetInstance().mUsersTable= DatabaseMgr._GetInstance().new UsersTableWrapper();
		
		return DatabaseMgr._GetInstance().mUsersTable;
	}

	/**
	 * DataParameters Table Accessor
	 * @return
	 */
	public static DataParametersTableWrapper _DataParameters()
	{
		// Create the DataParameters table if it does not exist
		if( DatabaseMgr._GetInstance().mDataParametersTable == null )
			DatabaseMgr._GetInstance().mDataParametersTable= DatabaseMgr._GetInstance().new DataParametersTableWrapper();
		
		return DatabaseMgr._GetInstance().mDataParametersTable;
	}

	/**
	 * 
	 */
	private boolean _initDB()
	{
		boolean bStatus= false;

		if( this.mConnection != null )
			bStatus= true;
		else
		{
		    try
		    {
		    	/*
		           The driver is installed by loading its class.
		           In an embedded environment, this will start up Derby, since it is not already running.
		         */
		         Class.forName( this.mstrDriver ).newInstance();
		         System.out.println( "Loaded the appropriate driver: " + this.mstrDriver );
		
		         Properties props = new Properties();
		         props.put("user", "user1");
		         props.put("password", "user1");
		
		         /*
		            The connection specifies create=true to cause the database to be created. To remove the database,
		            remove the directory derbyDB and its contents. The directory derbyDB will be created under
		            the directory that the system property derby.system.home points to, or the current
		            directory if derby.system.home is not set.
		          */
		         this.mConnection= DriverManager.getConnection( this.mstrProtocol + this.mstrDBName + ";create=true;upgrade=true", props );	
		         System.out.println( "Connected to and created database " + this.mstrDBName );
		         
		         // this will print the name and version of the software used for running this Derby system
		         DatabaseMetaData dbmd = this.mConnection.getMetaData();
		         String productName = dbmd.getDatabaseProductName();
		         String productVersion = dbmd.getDatabaseProductVersion();
		         System.out.println("Using " + productName + " " + productVersion);
		
		         this.mConnection.setAutoCommit( true );
		
		         bStatus= true;
		     }
		     catch (Throwable e)
		     {
		         System.out.println("exception thrown:");
		
		         if (e instanceof SQLException)
		             printSQLError((SQLException) e);
		         else
		             e.printStackTrace();
		     }
		}
	    
	    return bStatus;
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private void _Close()
	{
        try 
        {
			this.mConnection.commit();
	        this.mConnection.close();
		} 
        catch (SQLException e) 
		{
            this.printSQLError((SQLException) e);
		}
	}
	
	/**
	 * 
	 * @param e
	 */
	private void printSQLError(SQLException e)
	{
	    while (e != null)
	    {
	        System.out.println(e.toString());
	        e = e.getNextException();
	    }
	}

	/**
	 * 
	 * @author terryskotz
	 *
	 */
	public class TableVersionsWrapper
	{	
		private final String TABLENAME_COL=  "tablename";
		private final int	 TABLENAME_COL_MAX_SIZE= 100;
		private final String TABLEVERSION_COL=  "tableversion";
		private final int	 TABLEVERSION_COL_MAX_SIZE= 100;
		
		private String mstrMyTable= null;
		
		public TableVersionsWrapper()
		{
			this.mstrMyTable= DatabaseMgr._GetInstance().mstrTableVersionsName;
			try
			{
				// Add the table to the master db
		        Statement s = DatabaseMgr._GetInstance()._CreateStatement();
		        String strQuery= String.format( "CREATE TABLE %s ( %s varchar(%d), %s varchar(%d))", 
		        		 this.mstrMyTable, TABLENAME_COL, TABLENAME_COL_MAX_SIZE, TABLEVERSION_COL, TABLEVERSION_COL_MAX_SIZE);
		        //System.out.println( strQuery );
		        s.execute( strQuery );
			}
		    catch (Throwable e)
		    {
		        if( e.getMessage().contains( "already exists in Schema") )
		        	;// this is ok
		        else 
		        {
			        System.out.println("exception thrown:");
		        	if (e instanceof SQLException)
		        		DatabaseMgr._GetInstance().printSQLError((SQLException) e);
		        	else
		        		e.printStackTrace();
		        }
		    }
		}
		
		public boolean _SetVersion( String strTableName, String strVersion )
		{
			try 
			{
				Statement s= DatabaseMgr._GetInstance()._CreateStatement();
				String strQuery;
				if( this._GetVersion(strTableName) == null )
					strQuery= String.format( "INSERT INTO %s VALUES ('%s','%s')", 
						this.mstrMyTable, strTableName, strVersion );
				else
					strQuery= String.format( "UPDATE %s SET %s='%s',%s='%s' WHERE %s='%s'", 
							this.mstrMyTable, 
							this.TABLENAME_COL, strTableName, this.TABLEVERSION_COL, strVersion, this.TABLENAME_COL, strTableName );					
				s.execute( strQuery );
				s.close();
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
				return false;
			}
			
	        return true;
		}
		
		public String _GetVersion( String strTableName )
		{
			String strVersion= null;
			try 
			{
				Statement s= DatabaseMgr._GetInstance()._CreateStatement();
		        String strQuery= String.format( "SELECT * FROM %s WHERE %s='%s'", this.mstrMyTable, this.TABLENAME_COL, strTableName );
				ResultSet rs= s.executeQuery( strQuery );

				if( rs.next() )
					strVersion= rs.getString(this.TABLENAME_COL);
				rs.close();
				s.close();
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
			}
			
	        return strVersion;
		}
	}

	/**
	 * 
	 * @author terryskotz
	 *
	 */
	public class UsersTableWrapper
	{	
		private final String USER_COL=  "username";
		private final int	 USER_COL_MAX_SIZE= 100;
		
		private String mstrMyTable= null;
		
		public UsersTableWrapper()
		{
			this.mstrMyTable= DatabaseMgr._GetInstance().mstrUsersTableName;
			try
			{
				// Add the table to the master db
		        Statement s = DatabaseMgr._GetInstance()._CreateStatement();
		        String strQuery= String.format( "CREATE TABLE %s ( %s varchar(%d))", 
		        		 this.mstrMyTable, USER_COL, USER_COL_MAX_SIZE );
		        //System.out.println( strQuery );
		        s.execute( strQuery );
			}
		    catch (Throwable e)
		    {
		        if( e.getMessage().contains( "already exists in Schema") )
		        	;// this is ok
		        else 
		        {
			        System.out.println("exception thrown:");
		        	if (e instanceof SQLException)
		        		DatabaseMgr._GetInstance().printSQLError((SQLException) e);
		        	else
		        		e.printStackTrace();
		        }
		    }
			DatabaseMgr._Versions()._SetVersion(this.mstrMyTable, "1.0");
		}
		
		public String _AddUser( String strUser )
		{	
			String strStatus= "Unknown _AddJob Error";
			try 
			{
				if( this._UserExists( strUser ) )
					strStatus= "The user \"" + strUser + "\" already exists";
				else
				{
					Statement s= DatabaseMgr._GetInstance()._CreateStatement();
			        String strQuery= String.format( "INSERT INTO %s VALUES ('%s')", this.mstrMyTable, strUser );
					s.execute( strQuery );
					s.close();
					strStatus= ToolboxHTTPServer.STATUS_SUCCESS;
				}
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
				strStatus= e.getLocalizedMessage();
			}
			
	        return strStatus;
		}
		
		public boolean _UserExists( String strUser  )
		{
			boolean bFound= false;
			try 
			{
				Statement s= DatabaseMgr._GetInstance()._CreateStatement();
		        String strQuery= String.format( "SELECT * FROM %s WHERE %s='%s'", this.mstrMyTable, this.USER_COL, strUser );
				ResultSet rs= s.executeQuery( strQuery );

				bFound= rs.next();
					
				rs.close();
				s.close();
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
			}
			
			return bFound;
		}
		
		public ArrayList<String> _GetUsers()
		{
			ArrayList<String> arrUsers= new ArrayList<String>();
			try 
			{
				Statement s= DatabaseMgr._GetInstance()._CreateStatement();
		        String strQuery= String.format( "SELECT * FROM %s", this.mstrMyTable );
				ResultSet rs= s.executeQuery( strQuery );

				while( rs.next() ) {
					arrUsers.add( rs.getString(this.USER_COL) );
				}
				
				rs.close();
				s.close();
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
			}
			
			return arrUsers;
		}
	}

	/**
	 * 
	 * @author terryskotz
	 *
	 */
	public class JobsTableWrapper
	{	
		private final String JOB_NAME_COL= "jobname";
		private final int	 JOB_NAME_COL_MAX_SIZE= 255;
		private final String USER_COL=  "username";
		private final int	 USER_COL_MAX_SIZE= 100;
		private final String DATAPARAMS_COL=  "dataparams";
		private final int	 DATAPARAMS_COL_MAX_SIZE= 32672; //MAX varchar
		private final String CLASSPATH_COL=  "classpath";
		private final int	 CLASSPATH_COL_MAX_SIZE= 500;
		private final String OPTARGS_COL=  "optargs";
		private final int	 OPTARGS_COL_MAX_SIZE= 32672; //MAX varchar
		
		private String mstrMyTable= null;
		
		public JobsTableWrapper()
		{
			this.mstrMyTable= DatabaseMgr._GetInstance().mstrJobsTableName;
			try
			{
				// Add the table to the master db
		        Statement s = DatabaseMgr._GetInstance()._CreateStatement();
		        String strQuery= String.format( "CREATE TABLE %s ( %s varchar(%d), %s varchar(%d), %s varchar(%d), %s varchar(%d), %s varchar(%d))", 
		        		 this.mstrMyTable, JOB_NAME_COL, JOB_NAME_COL_MAX_SIZE, USER_COL, USER_COL_MAX_SIZE, 
		        		 DATAPARAMS_COL, DATAPARAMS_COL_MAX_SIZE, CLASSPATH_COL, CLASSPATH_COL_MAX_SIZE, OPTARGS_COL, OPTARGS_COL_MAX_SIZE );
		        System.out.println( strQuery );
		        s.execute( strQuery );
			}
		    catch (Throwable e)
		    {
		        if( e.getMessage().contains( "already exists in Schema") )
		        	;// this is ok
		        else 
		        {
			        System.out.println("exception thrown:");
		        	if (e instanceof SQLException)
		        		DatabaseMgr._GetInstance().printSQLError((SQLException) e);
		        	else
		        		e.printStackTrace();
		        }
		    }
			DatabaseMgr._Versions()._SetVersion(this.mstrMyTable, "1.0");
		}
		
		public String _AddJob( JobDescriptor pJobDescr )
		{	
			String strStatus= "Unknown _AddJob Error";
			try 
			{
				if( this._GetJob( pJobDescr.mstrJobName, pJobDescr.mstrUser ) != null )
					strStatus= "The job \"" + pJobDescr.mstrJobName + "\" already exists for this user: " + pJobDescr.mstrUser;
				else
				{
					DatabaseMgr._Users()._AddUser(pJobDescr.mstrUser);
					
					Statement s= DatabaseMgr._GetInstance()._CreateStatement();
			        String strQuery= String.format( "INSERT INTO %s VALUES ('%s','%s','%s','%s','%s')", this.mstrMyTable, 
			        		 pJobDescr.mstrJobName, pJobDescr.mstrUser,	pJobDescr.mstrDataparams, pJobDescr.mstrClasspath, pJobDescr.mstrOptArgs );
					s.execute( strQuery );
					s.close();
					strStatus= ToolboxHTTPServer.STATUS_SUCCESS;
				}
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
				strStatus= e.getLocalizedMessage();
			}
			
	        return strStatus;
		}
		
		/**
		 * 
		 * @param strJobName
		 * @param strUser
		 * @return
		 */
		public String _DeleteJob( String strJobName, String strUser  )
		{
			try 
			{
				Statement s= DatabaseMgr._GetInstance()._CreateStatement();
		        String strQuery= String.format( "DELETE FROM %s WHERE %s='%s' AND %s='%s'", this.mstrMyTable, this.JOB_NAME_COL, strJobName, this.USER_COL, strUser );
				s.execute( strQuery );
				s.close();
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
				return "Failed to delete job \"strJobName\" from user " + strUser + ":\n\n" + e.getMessage();
			}
			
			return ToolboxHTTPServer.STATUS_SUCCESS;
		}
		
		/**
		 * 
		 * @param strJobName
		 * @param strUser
		 * @return
		 */
		public JobDescriptor _GetJob( String strJobName, String strUser  )
		{
			JobDescriptor pJobDescr= null;
			try 
			{
				Statement s= DatabaseMgr._GetInstance()._CreateStatement();
		        String strQuery= String.format( "SELECT * FROM %s WHERE %s='%s' AND %s='%s'", this.mstrMyTable, this.JOB_NAME_COL, strJobName, this.USER_COL, strUser );
				ResultSet rs= s.executeQuery( strQuery );

				if( rs.next() ) {
					pJobDescr= new JobDescriptor( rs.getString(this.JOB_NAME_COL), rs.getString(this.USER_COL), rs.getString(this.DATAPARAMS_COL), 
												  rs.getString(this.CLASSPATH_COL), rs.getString(this.OPTARGS_COL) );
				}
				
				rs.close();
				s.close();
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
			}
			
			return pJobDescr;
		}

		
		//TODO: make this more efficient!
		public ArrayList<JobDescriptor> _GetJobs( String strUser )
		{
			ArrayList<JobDescriptor> arrJobDescriptors= new ArrayList<JobDescriptor>();
			try 
			{
				Statement s= DatabaseMgr._GetInstance()._CreateStatement();
				String strQuery;
				if( strUser == null || strUser.isEmpty() )
					strQuery= String.format( "SELECT * FROM %s", this.mstrMyTable );
				else
					strQuery= String.format( "SELECT * FROM %s WHERE %s='%s'", this.mstrMyTable, this.USER_COL, strUser );
				ResultSet rs= s.executeQuery( strQuery );

				while( rs.next() ) {
					arrJobDescriptors.add( new JobDescriptor( rs.getString(this.JOB_NAME_COL), rs.getString(this.USER_COL), rs.getString(this.DATAPARAMS_COL), 
												  rs.getString(this.CLASSPATH_COL), rs.getString(this.OPTARGS_COL) ) );
				}
				
				rs.close();
				s.close();
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
			}
			
			return arrJobDescriptors;
		}
	}
	
	/**
	 * 
	 * @author terryskotz
	 *
	 */
	public class TestbedsTableWrapper
	{	
		private final String NAME_COL=  "name"; // the testbed or group name
		private final int	 NAME_COL_MAX_SIZE= 100;
		private final String VALUE_COL= "value"; // the testbed or list of testbeds in group
		private final int	 VALUE_COL_MAX_SIZE= 32672; //MAX varchar
		private final String TYPE_COL=  "type"; // "testbed" or "group"
		private final int	 TYPE_COL_MAX_SIZE= 20;
		private final String RUN_MODE_COL=  "RunMode"; // Serialize, Parallelize, First Available
		private final int	 RUN_MODE_COL_MAX_SIZE= 20;
		private final String DESCRIPTION_COL=  "descr"; // a description of the testbed or group
		private final int	 DESCRIPTION_COL_MAX_SIZE= 500;
		
		private String mstrMyTable= null;
		
		public TestbedsTableWrapper()
		{
			this.mstrMyTable= DatabaseMgr._GetInstance().mstrTestbedTableName;

			try
			{
				// Add the table to the master db
		        Statement s = DatabaseMgr._GetInstance()._CreateStatement();
		        String strQuery= String.format( "CREATE TABLE %s ( %s varchar(%d), %s varchar(%d), %s varchar(%d), %s varchar(%d), %s varchar(%d))", 
		        		this.mstrMyTable, NAME_COL, NAME_COL_MAX_SIZE, VALUE_COL, VALUE_COL_MAX_SIZE, TYPE_COL, TYPE_COL_MAX_SIZE, RUN_MODE_COL, RUN_MODE_COL_MAX_SIZE, DESCRIPTION_COL, DESCRIPTION_COL_MAX_SIZE );
		        s.execute( strQuery );
			}
		    catch (Throwable e)
		    {
		        if( e.getMessage().contains( "already exists in Schema") )
		        	;// this is ok
		        else 
		        {
			        System.out.println("exception thrown:");
		        	if (e instanceof SQLException)
		        		DatabaseMgr._GetInstance().printSQLError((SQLException) e);
		        	else
		        		e.printStackTrace();
		        }
		    }
			DatabaseMgr._Versions()._SetVersion(this.mstrMyTable, "1.0");
		}
		
		/**
		 * 
		 * @return
		 */
		public String[] _GetTestbeds( )
		{
			ArrayList<String> arrTestbeds= new ArrayList<String>();
			
			try 
			{
		        Statement s = DatabaseMgr._GetInstance()._CreateStatement();
		        String strQuery= String.format( "SELECT %s, %s, %s, %s, %s FROM %s ORDER BY %s", 
		        								 NAME_COL, VALUE_COL, TYPE_COL, RUN_MODE_COL, DESCRIPTION_COL, 
		        								 this.mstrMyTable, NAME_COL );
				ResultSet rs= s.executeQuery( strQuery );

				System.out.println( "****************" );

				while( rs.next() ) {
					arrTestbeds.add( rs.getString(NAME_COL) );
					System.out.println( rs.getString(NAME_COL) );
					System.out.println( rs.getString(VALUE_COL) );
					System.out.println( rs.getString(TYPE_COL) );
					System.out.println( rs.getString(RUN_MODE_COL) );
					System.out.println( rs.getString(DESCRIPTION_COL) );
				}
				
				rs.close();
				s.close();
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
			}
			
			return arrTestbeds.toArray(new String[arrTestbeds.size()]);
		}
		
		/**
		 * 
		 * @param strTestbedName
		 * @return
		 */
		public TestbedDescriptor _GetTestbedDescriptor( String strTestbedName  )
		{
			TestbedDescriptor pTBDescr= null;
			try 
			{
				Statement s= DatabaseMgr._GetInstance()._CreateStatement();
		        String strQuery= String.format( "SELECT * FROM %s WHERE %s='%s'", this.mstrMyTable, NAME_COL, strTestbedName );
				ResultSet rs= s.executeQuery( strQuery );

				if( rs.next() )
					pTBDescr= new TestbedDescriptor( rs.getString(NAME_COL), rs.getString(VALUE_COL), rs.getString(TYPE_COL),
							 						 rs.getString(RUN_MODE_COL), rs.getString(DESCRIPTION_COL));
				rs.close();
				s.close();
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
			}
			
			return pTBDescr;
		}
		
		/**
		 * 
		 * @param strTestbed
		 * @param strValue
		 * @param strType
		 * @return
		 */
		public boolean _AddTestbed( String strTestbed, String strValue, String strType, String strRunMode, String strDescription )
		{			
			try 
			{
				if( this._GetTestbedDescriptor(strTestbed) == null )
				{
					Statement s= DatabaseMgr._GetInstance()._CreateStatement();
					s.execute( "INSERT INTO " + this.mstrMyTable + 
							   " VALUES ('" + strTestbed + "','" + strValue + "','" + strType + "','" + strRunMode + "','" + strDescription + "')" );
					s.close();
				}
				else
					System.out.println( "The testbed/group already exists in the testbeds table: " + strTestbed );
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
				return false;
			}
			
	        return true;
		}

		/**
		 * 
		 * @param strTestbed
		 * @param strValue
		 * @param strType
		 * @return
		 */
		public boolean _UpdateTestbed( String strCurTestbed, String strNewTestbed, String strNewValue, String strNewType, String strRunMode, String strDescription )
		{			
			try 
			{
				if( this._GetTestbedDescriptor(strNewTestbed) != null )
					System.out.println( "The new testbed/group already exist in the testbeds table: " + strNewTestbed );					
				else if( this._GetTestbedDescriptor(strCurTestbed) != null )
				{
					Statement s= DatabaseMgr._GetInstance()._CreateStatement();
					String strQuery= String.format( "UPDATE %s SET %s='%s',%s='%s',%s='%s',%s='%s',%s='%s' WHERE %s='%s'", 
							this.mstrMyTable, 
							this.NAME_COL, strNewTestbed, this.VALUE_COL, strNewValue, this.TYPE_COL, strNewType,
							this.RUN_MODE_COL, strRunMode, this.DESCRIPTION_COL, strDescription, this.NAME_COL, strCurTestbed );
					s.execute( strQuery );
					s.close();
				}
				else
					System.out.println( "The testbed/group does not exist in the testbeds table: " + strCurTestbed );
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
				return false;
			}
			
	        return true;
		}
		
		/**
		 * 
		 * @param strTestbed
		 * @param strValue
		 * @param strType
		 * @return
		 */
		public boolean _DeleteTestbed( String strTestbedName )
		{			
			try 
			{
				Statement s= DatabaseMgr._GetInstance()._CreateStatement();
				String strQuery= String.format( "DELETE FROM %s WHERE %s='%s'", this.mstrMyTable, this.NAME_COL, strTestbedName );
				s.execute( strQuery );
				s.close();
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
				return false;
			}
			return true;
		}

	}
	
	/**
	 * 
	 * @author terryskotz
	 *
	 */
	public class PreferencesTableWrapper
	{	
		private final String PREF_NAME_COL=  "name";
		private final int	 PREF_NAME_COL_MAX_SIZE= 100;
		private final String PREF_VALUE_COL=  "value";
		private final int	 PREF_VALUE_COL_MAX_SIZE= 500;
		
		private String mstrMyTable= null;
		
		/**
		 * 
		 */
		public PreferencesTableWrapper()
		{
			this.mstrMyTable= DatabaseMgr._GetInstance().mstrPreferencesTableName;
			try
			{
				// Add the table to the master db
		        Statement s = DatabaseMgr._GetInstance()._CreateStatement();
		        String strQuery= String.format( "CREATE TABLE %s ( %s varchar(%d), %s varchar(%d))", 
		        		 this.mstrMyTable, PREF_NAME_COL, PREF_NAME_COL_MAX_SIZE, PREF_VALUE_COL, PREF_VALUE_COL_MAX_SIZE);
		        //System.out.println( strQuery );
		        s.execute( strQuery );		        
			}
		    catch (Throwable e)
		    {
		        if( e.getMessage().contains( "already exists in Schema") )
		        	;// this is ok
		        else 
		        {
			        System.out.println("exception thrown:");
		        	if (e instanceof SQLException)
		        		DatabaseMgr._GetInstance().printSQLError((SQLException) e);
		        	else
		        		e.printStackTrace();
		        }
		    }
			DatabaseMgr._Versions()._SetVersion(this.mstrMyTable, "1.0");
			// Store default preferences if not already set
			for( Preferences pref : Preferences.values() )
		        this._PutPrefImpl( pref, pref._GetDefaultData(), false );
		}
		
		/**
		 * 
		 * @param prefType
		 * @param strValue
		 */
		private void _PutPrefImpl( Preferences prefType, String strValue, boolean bUpdateIfExist )
		{	
			try 
			{
				String strQuery= null;

				if( this._GetPref( prefType ) == null ) 
					strQuery= String.format( "INSERT INTO %s VALUES ('%s','%s')", this.mstrMyTable, prefType.name(), strValue );
				else if ( bUpdateIfExist )
					strQuery= String.format( "UPDATE %s SET %s='%s',%s='%s' WHERE %s='%s'", 
							this.mstrMyTable, 
							this.PREF_NAME_COL, prefType.name(), this.PREF_VALUE_COL, strValue, this.PREF_NAME_COL, prefType.name() );

				if( strQuery != null )
				{
					Statement s= DatabaseMgr._GetInstance()._CreateStatement();
					s.execute( strQuery );
					s.close();
				}
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
			}			
		}
		
		/**
		 * 
		 * @param prefType
		 * @param strValue
		 */
		public void _PutPref( Preferences prefType, String strValue )
		{	
			this._PutPrefImpl( prefType, strValue, true );
		}

		
		/**
		 * 
		 * @param prefType
		 * @param bValue
		 */
		public void _PutPrefBool( Preferences prefType, boolean bValue )
		{
			this._PutPref( prefType, bValue?"true":"false" );
		}

		/**
		 * 
		 * @param prefType
		 * @param bValue
		 */
		public void _PutPrefInt( Preferences prefType, int iValue )
		{
			this._PutPref( prefType, String.valueOf( iValue ) );
		}
		
		/**
		 * 
		 * @param prefType
		 * @return
		 */
		public String _GetPref( Preferences prefType  )
		{
			String strSettingValue= null;
			try 
			{
				Statement s= DatabaseMgr._GetInstance()._CreateStatement();
		        String strQuery= String.format( "SELECT * FROM %s WHERE %s='%s'", this.mstrMyTable, PREF_NAME_COL, prefType.name() );
				ResultSet rs= s.executeQuery( strQuery );

				if( rs.next() )
					strSettingValue= rs.getString( PREF_VALUE_COL );
				rs.close();
				s.close();
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
			}
			
			return strSettingValue;
		}
		
		/**
		 * 
		 * @param prefType
		 * @return
		 */
		public boolean _GetPrefBool( Preferences prefType  )
		{
			return this._GetPref( prefType ).equalsIgnoreCase( "true" );
		}

		/**
		 * 
		 * @param prefType
		 * @return
		 */
		public int _GetPrefInt( Preferences prefType  )
		{
			return Integer.valueOf( this._GetPref( prefType ) );
		}
	}
	
	/**
	 * 
	 * @author terryskotz
	 *
	 */
	public class DataParametersTableWrapper
	{
		private final String DP_NAME_COL=  "name";
		private final int	 DP_NAME_COL_MAX_SIZE= 100;
		private final String DP_TYPE_COL=  "type";
		private final int	 DP_TYPE_COL_MAX_SIZE= 10;
		private final String DP_AS_LIST_COL=  "aslist";
		private final String DP_VALUE_COL=  "value";
		private final int	 DP_VALUE_COL_MAX_SIZE= 500;
		private final String DP_DESC_COL=  "descr";
		private final int	 DP_DESC_COL_MAX_SIZE= 500;
		
		private String mstrMyTable= null;
		
		/**
		 * 
		 */
		public DataParametersTableWrapper()
		{
			this.mstrMyTable= DatabaseMgr._GetInstance().mstrDataParametersTableName;
			try
			{
				// Add the table to the master db
		        Statement s = DatabaseMgr._GetInstance()._CreateStatement();
		        String strQuery= String.format( "CREATE TABLE %s ( %s varchar(%d), %s varchar(%d), %s varchar(%d), %s boolean, %s varchar(%d))", 
		        		 this.mstrMyTable, DP_NAME_COL, DP_NAME_COL_MAX_SIZE, DP_TYPE_COL, DP_TYPE_COL_MAX_SIZE,
		        		 				   DP_VALUE_COL, DP_VALUE_COL_MAX_SIZE, DP_AS_LIST_COL, DP_DESC_COL, DP_DESC_COL_MAX_SIZE);
		        //System.out.println( strQuery );
		        s.execute( strQuery );		        
			}
		    catch (Throwable e)
		    {
		        if( e.getMessage().contains( "already exists in Schema") )
		        	;// this is ok
		        else 
		        {
			        System.out.println("exception thrown:");
		        	if (e instanceof SQLException)
		        		DatabaseMgr._GetInstance().printSQLError((SQLException) e);
		        	else
		        		e.printStackTrace();
		        }
		    }
			DatabaseMgr._Versions()._SetVersion(this.mstrMyTable, "1.0");
		}
		
		/**
		 * 
		 * @return
		 */
		public String[] _GetDataParameterNames( )
		{
			ArrayList<String> arrTestbeds= new ArrayList<String>();
			
			try 
			{
		        Statement s = DatabaseMgr._GetInstance()._CreateStatement();
		        String strQuery= String.format( "SELECT %s, %s, %s, %s, %s FROM %s ORDER BY %s", 
		        								 DP_NAME_COL, DP_TYPE_COL, DP_VALUE_COL, DP_AS_LIST_COL, DP_DESC_COL, 
		        								 this.mstrMyTable, DP_NAME_COL );
				ResultSet rs= s.executeQuery( strQuery );

				System.out.println( "****************" );

				while( rs.next() ) {
					arrTestbeds.add( rs.getString(DP_NAME_COL) );
					System.out.println( rs.getString(DP_NAME_COL) );
					System.out.println( rs.getString(DP_VALUE_COL) );
					System.out.println( rs.getString(DP_TYPE_COL) );
					System.out.println( rs.getString(DP_AS_LIST_COL) );
					System.out.println( rs.getString(DP_DESC_COL) );
				}
				
				rs.close();
				s.close();
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
			}
			
			return arrTestbeds.toArray(new String[arrTestbeds.size()]);
		}

		/**
		 * 
		 * @param strName
		 * @return
		 */
		public DataParameter _GetDataParameter( String strName  )
		{
			DataParameter pDataParameter= null;
			try 
			{
				Statement s= DatabaseMgr._GetInstance()._CreateStatement();
		        String strQuery= String.format( "SELECT * FROM %s WHERE %s='%s'", this.mstrMyTable, DP_NAME_COL, strName );
				ResultSet rs= s.executeQuery( strQuery );

				if( rs.next() )
					pDataParameter= new DataParameter( rs.getString(DP_NAME_COL), rs.getString(DP_TYPE_COL), rs.getString(DP_VALUE_COL),
	 						 						   rs.getBoolean(DP_AS_LIST_COL), rs.getString(DP_DESC_COL));
				rs.close();
				s.close();
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
			}
			
			return pDataParameter;
		}
		
		/**
		 * 
		 * @param strName
		 * @param strValue
		 * @param strType
		 * @param bAsList
		 * @param strDescription
		 * @return
		 */
		public boolean _AddDataParameter( String strName, String strValue, String strType, boolean bAsList, String strDescription )
		{			
			try 
			{
				if( this._GetDataParameter(strName) == null )
				{
					Statement s= DatabaseMgr._GetInstance()._CreateStatement();
					s.execute( "INSERT INTO " + this.mstrMyTable + 
							   " VALUES ('" + strName + "','" + strType + "','" + strValue + "','" + bAsList + "','" + strDescription + "')" );
					s.close();
				}
				else
					System.out.println( "The data parameter already exists: " + strName );
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
				return false;
			}
			
	        return true;
		}

		/**
		 * 
		 * @param strCurName
		 * @param strNewName
		 * @param strNewValue
		 * @param strNewType
		 * @param bAsList
		 * @param strDescription
		 * @return
		 */
		public boolean _UpdateDataParameter( String strCurName, String strNewName, String strNewValue, String strNewType, boolean bAsList, String strDescription )
		{			
			try 
			{
				if( this._GetDataParameter(strNewName) != null )
					System.out.println( "The new data parameter already exists: " + strNewName );					
				else if( this._GetDataParameter(strCurName) != null )
				{
					Statement s= DatabaseMgr._GetInstance()._CreateStatement();
					String strQuery= String.format( "UPDATE %s SET %s='%s',%s='%s',%s='%s',%s='%s',%s='%s' WHERE %s='%s'", 
							this.mstrMyTable, 
							DP_NAME_COL, strNewName, DP_TYPE_COL, strNewType, DP_VALUE_COL, strNewValue,
							DP_AS_LIST_COL, bAsList, DP_DESC_COL, strDescription, DP_NAME_COL, strCurName );
					s.execute( strQuery );
					s.close();
				}
				else
					System.out.println( "The data parameter does not exist: " + strCurName );
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
				return false;
			}
			
	        return true;
		}
		
		/**
		 * 
		 * @param strName
		 * @return
		 */
		public boolean _DeleteDataParameter( String strName )
		{			
			try 
			{
				Statement s= DatabaseMgr._GetInstance()._CreateStatement();
				String strQuery= String.format( "DELETE FROM %s WHERE %s='%s'", this.mstrMyTable, DP_NAME_COL, strName );
				s.execute( strQuery );
				s.close();
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
				return false;
			}
			return true;
		}

	}

}
