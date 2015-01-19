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
	
	private final String mstrTestbedTableName= "TestbedTable";
	private final String mstrJobsTableName= "JobsTable";
	private final String mstrUsersTableName= "UsersTable";

	private TestbedsTableWrapper mTestbedsTable= null;
	private JobsTableWrapper mJobsTable= null;
	private UsersTableWrapper mUsersTable= null;
	
	private Connection mConnection = null;

	/**
	 * Test code
	 * @param args
	 */
	public static void main(String[] args)
	{
		DatabaseMgr._Testbeds()._DeleteTestbed( "testbedname5" );
		DatabaseMgr._Testbeds()._DeleteTestbed( "testbedname5" );
		DatabaseMgr._Testbeds()._AddTestbed( "testbedname3", "testbedvalue3", "testbedtype3" );
		DatabaseMgr._Testbeds()._GetTestbeds();
		DatabaseMgr._Testbeds()._AddTestbed( "testbedname4", "testbedvalue4", "testbedtype4" );
		DatabaseMgr._Testbeds()._GetTestbeds();
		DatabaseMgr._Testbeds()._DeleteTestbed( "testbedname4" );
		DatabaseMgr._Testbeds()._GetTestbeds();
		DatabaseMgr._Testbeds()._AddTestbed( "testbedname5", "testbedvalue5", "testbedtype5" );
		DatabaseMgr._Testbeds()._GetTestbeds();
		DatabaseMgr._Testbeds()._AddTestbed( "testbedname3", "testbedvalue3", "testbedtype3" );
		DatabaseMgr._Testbeds()._GetTestbeds();
		DatabaseMgr._Testbeds()._UpdateTestbed( "testbedname3", "testbedname6", "testbedvalue6", "testbedtype6" );
		DatabaseMgr._Testbeds()._GetTestbeds();
		DatabaseMgr._Testbeds()._DeleteTestbed( "testbedname5" );
		DatabaseMgr._Testbeds()._GetTestbeds();
		
		String[] tbs= DatabaseMgr._Testbeds()._GetTestbeds();
		String tbsv= DatabaseMgr._Testbeds()._GetTestbedValue( "testbedname3" );

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
		// Create the testbeds table if it does not exist
		if( DatabaseMgr._GetInstance().mJobsTable == null )
			DatabaseMgr._GetInstance().mJobsTable= DatabaseMgr._GetInstance().new JobsTableWrapper();
		
		return DatabaseMgr._GetInstance().mJobsTable;
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
		         this.mConnection= DriverManager.getConnection( this.mstrProtocol + this.mstrDBName + ";create=true", props );	
		         System.out.println( "Connected to and created database " + this.mstrDBName );
		
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
		private final String NAME_COL=  "name";
		private final int	 NAME_COL_MAX_SIZE= 100;
		private final String VALUE_COL= "value";
		private final int	 VALUE_COL_MAX_SIZE= 32672; //MAX varchar
		private final String TYPE_COL=  "type";
		private final int	 TYPE_COL_MAX_SIZE= 20;
		
		private String mstrMyTable= null;
		
		public TestbedsTableWrapper()
		{
			this.mstrMyTable= DatabaseMgr._GetInstance().mstrTestbedTableName;

			try
			{
				// Add the table to the master db
		        Statement s = DatabaseMgr._GetInstance()._CreateStatement();
		        String strQuery= String.format( "CREATE TABLE %s ( %s varchar(%d), %s varchar(%d), %s varchar(%d))", 
		        		this.mstrMyTable, NAME_COL, NAME_COL_MAX_SIZE, VALUE_COL, VALUE_COL_MAX_SIZE, TYPE_COL, TYPE_COL_MAX_SIZE );
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
		        String strQuery= String.format( "SELECT %s, %s, %s FROM %s ORDER BY %s", NAME_COL, VALUE_COL, TYPE_COL, this.mstrMyTable, NAME_COL );
				ResultSet rs= s.executeQuery( strQuery );

				System.out.println( "****************" );

				while( rs.next() ) {
					arrTestbeds.add( rs.getString(NAME_COL) );
					System.out.println( rs.getString(NAME_COL) );
					System.out.println( rs.getString(VALUE_COL) );
					System.out.println( rs.getString(TYPE_COL) );
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
		public String _GetTestbedValue( String strTestbedName  )
		{
			String strData= null;
			try 
			{
				Statement s= DatabaseMgr._GetInstance()._CreateStatement();
		        String strQuery= String.format( "SELECT * FROM %s WHERE %s='%s'", this.mstrMyTable, NAME_COL, strTestbedName );
				ResultSet rs= s.executeQuery( strQuery );

				if( rs.next() )
					strData= rs.getString(VALUE_COL);
				
				rs.close();
				s.close();
			} 
			catch (SQLException e) 
			{
				DatabaseMgr._GetInstance().printSQLError( e );
			}
			
			return strData;
		}
		
		/**
		 * 
		 * @param strTestbed
		 * @param strValue
		 * @param strType
		 * @return
		 */
		public boolean _AddTestbed( String strTestbed, String strValue, String strType )
		{			
			try 
			{
				if( this._GetTestbedValue(strTestbed) == null )
				{
					Statement s= DatabaseMgr._GetInstance()._CreateStatement();
					s.execute( "INSERT INTO " + this.mstrMyTable + " VALUES ('" + strTestbed + "','" + strValue + "','" + strType + "')" );
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
		public boolean _UpdateTestbed( String strCurTestbed, String strNewTestbed, String strNewValue, String strNewType )
		{			
			try 
			{
				if( this._GetTestbedValue(strNewTestbed) != null )
					System.out.println( "The new testbed/group already exist in the testbeds table: " + strNewTestbed );					
				else if( this._GetTestbedValue(strCurTestbed) != null )
				{
					Statement s= DatabaseMgr._GetInstance()._CreateStatement();
					String strQuery= String.format( "UPDATE %s SET %s='%s',%s='%s',%s='%s' WHERE %s='%s'", 
							this.mstrMyTable, 
							this.NAME_COL, strNewTestbed, this.VALUE_COL, strNewValue, this.TYPE_COL, strNewType, this.NAME_COL, strCurTestbed );
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

}