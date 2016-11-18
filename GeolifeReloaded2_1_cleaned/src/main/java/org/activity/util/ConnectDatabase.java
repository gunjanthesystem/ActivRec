package org.activity.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;

import org.activity.objects.FlatActivityLogEntry;
import org.json.JSONArray;

import com.mchange.v2.c3p0.ComboPooledDataSource;

final public class ConnectDatabase
{
	private static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://localhost:3306/";// "jdbc:mysql://csserver.ucd.ie/";//
	
	private static final String USER = "root";// "gkumar";//"root";
	private static final String PASS = "root";// "V12xt!07";//"root";
	
	private static String databaseName;// = "geolife1";//"dcu_data_2" "start_base_2"; //"gkumar";
	private static final String metaTableName = "meta";
	private static final String keysTableName = "keys_table";
	
	private static final int BATCH_SIZE = 12000;
	
	private static boolean USEPOOLED;// = Constant.USEPOOLED; //pooling is not used while insertion
	private static HashMap<Integer, String> userIDNameMap;
	private static HashMap<Integer, String> activityIDNameMap;
	private static HashMap<String, Integer> activityNameIDMap;
	private static ArrayList<String> dimensionNames;
	
	// static HikariDataSource connectionPool;
	
	// private static DataSource datasource;
	private static ComboPooledDataSource connectionPool;
	
	/**
	 * Don't let anyone instantiate this class.
	 */
	private ConnectDatabase()
	{
	}
	
	// /**
	// * Sets ups the following for the ConnectDatabase databasename, activityid-name maps, userid-name map and dimension names
	// *
	// * @param databaseName
	// */
	// public ConnectDatabase(String databaseName)
	// {
	// USEPOOLED = Constant.USEPOOLED;
	// if (USEPOOLED)
	// {
	// createPooledConnection();
	// }
	// setDatabaseName(Constant.getDatabaseName());// .DATABASE_NAME);
	// setActivityIDNameMaps();
	// setUserIDNameMap();
	// setDimensionNames();
	// }
	
	/**
	 * Sets ups the following for the ConnectDatabase databasename, activityid-name maps, userid-name map and dimension names
	 * 
	 * @param databaseName
	 */
	public static void initialise(String databaseName)
	{
		USEPOOLED = Constant.USEPOOLED;
		if (USEPOOLED)
		{
			createPooledConnection();
		}
		setDatabaseName(Constant.getDatabaseName());// .DATABASE_NAME);
		setActivityIDNameMaps();
		setUserIDNameMap();
		setDimensionNames();
	}
	
	public static void setDatabaseName(String databasename)
	{
		databaseName = databasename;
	}
	
	public static String getDatabaseName()
	{
		return databaseName;
	}
	
	// private void DataSource() throws IOException, SQLException, PropertyVetoException
	// {
	// cpds = new ComboPooledDataSource();
	// cpds.setDriverClass("com.mysql.jdbc.Driver"); //loads the jdbc driver
	// cpds.setJdbcUrl("jdbc:mysql://localhost/test");
	// cpds.setUser("root");
	// cpds.setPassword("root");
	//
	// // the settings below are optional -- c3p0 can work with defaults
	// cpds.setMinPoolSize(5);
	// cpds.setAcquireIncrement(5);
	// cpds.setMaxPoolSize(20);
	// cpds.setMaxStatements(180);
	//
	// }
	
	/**
	 * see Oracle tutorial doing/learning connection pooling
	 */
	public static void createPooledConnection()
	{
		try
		{
			// HikariConfig config = new HikariConfig();
			// config.setJdbcUrl(DB_URL);
			// config.setUsername(USER);
			// config.setPassword(PASS);
			// config.addDataSourceProperty("cachePrepStmts", "true");
			// config.addDataSourceProperty("prepStmtCacheSize", "250");
			// config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
			// config.addDataSourceProperty("useServerPrepStmts", "true");
			
			// HikariDataSource connectionPool = new HikariDataSource();//config);
			
			connectionPool = new ComboPooledDataSource();
			connectionPool.setJdbcUrl(DB_URL);
			connectionPool.setUser(USER);
			connectionPool.setPassword(PASS);
			
			connectionPool.setMinPoolSize(10);
			connectionPool.setAcquireIncrement(100);// 150
			connectionPool.setMaxPoolSize(500);// 5000
			connectionPool.setMaxStatements(100);// 180
			connectionPool.setInitialPoolSize(100);// 120
			
			// done to mitigate the broken pipe exception with no success
			// connectionPool.setTestConnectionOnCheckin(true);
			// connectionPool.setTestConnectionOnCheckout(false);
			// connectionPool.setIdleConnectionTestPeriod(30);
			// // connectionPool.setTestConnectionOnCheckout(true); // ref: http://www.mchange.com/projects/c3p0/index.html#configuring_connection_testing
			// connectionPool.setPreferredTestQuery("SELECT 1");
			/*
			 * 
			 */
			// connectionPool.setNumHelperThreads(1);
			// Connection pooledConnection = connectionPool.getConnection();
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			// try
			// {
			// if(stmt!=null)
			// conn.close();
			// }
			// catch(SQLException se)
			// {}
			// try
			// {
			// if(conn!=null)
			// conn.close();
			// }
			// catch(SQLException se)
			// {
			// se.printStackTrace();
			// }
		}
		
	}
	
	public static void destroyPooledConnection()
	{
		try
		{
			if (connectionPool != null)
			{
				connectionPool.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static String getPrimaryKeyName(String dimensionTableName)
	{
		String primaryKeyName = "";
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultset = null;
		
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			String sqlQuery = "SELECT * from " + databaseName + "." + keysTableName + ";";
			stmt = conn.prepareStatement(sqlQuery);
			
			resultset = stmt.executeQuery();
			
			while (resultset.next())
			{
				if (resultset.getString("Dimension_Table_Name").equals(dimensionTableName.trim()))
				{
					primaryKeyName = resultset.getString("Primary_Key_Name");
				}
			}
		}
		
		catch (SQLException se)
		{
			se.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (stmt != null)
					conn.close();
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		
		return primaryKeyName;
	}
	
	public static String getReferencedFactKeyName(String dimensionTableName)
	{
		String referencedFactKeyName = "";
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultset = null;
		
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			
			String sqlQuery = "SELECT * from " + databaseName + "." + keysTableName + ";";
			stmt = conn.prepareStatement(sqlQuery);
			
			resultset = stmt.executeQuery();
			
			while (resultset.next())
			{
				if (resultset.getString("Dimension_Table_Name").equals(dimensionTableName.trim()))
				{
					referencedFactKeyName = resultset.getString("Referenced_Fact_Key_Name");
				}
			}
		}
		
		catch (SQLException se)
		{
			se.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (stmt != null)
					conn.close();
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		
		return referencedFactKeyName;
	}
	
	public static String getInnerJoinString()
	{
		String innerJoinString = "";
		ArrayList<String> dimensionTableNames;
		ArrayList<String> dimensionNames;
		String factTableName;
		try
		{
			dimensionTableNames = getDimensionTableNames();
			dimensionNames = getDimensionNamesFromDatabase();
			factTableName = getFactTableName();
			
			int counter = 0;
			
			for (String dimensionTableName : dimensionTableNames)
			{
				String dimensionName = dimensionNames.get(counter);
				innerJoinString = innerJoinString + "INNER JOIN " + databaseName + "." + dimensionTableName + " ON " + databaseName + "."
						+ factTableName + "." + getReferencedFactKeyName(dimensionName) + " = " + dimensionTableName + "."
						+ getPrimaryKeyName(dimensionName) + "  ";
				counter++;
			}
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return innerJoinString;
	}
	
	// ////////////
	/**
	 * Fetches and builds the Data Table with the given sql selection of attribute and where-query clause
	 * 
	 * @param selectAttributeString
	 *            whereQueryString
	 * @return
	 * @throws SQLException
	 */
	public static JSONArray getJSONArrayOfDataTable(String selectAttributeString, String whereQueryString, String orderByString)
			throws SQLException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		JSONArray jsonArrayForDataTable = new JSONArray();
		long dt = System.currentTimeMillis();
		try
		{
			
			// $$System.out.println("Connected to database successfully...");
			
			String sqlQuery = "SELECT " + selectAttributeString + " from " + databaseName + "." + getFactTableName() + " "
					+ getInnerJoinString() + whereQueryString;
			if (orderByString.length() > 1)
			{
				sqlQuery = sqlQuery + " order by " + orderByString;
			}
			
			System.out.println("Complete SQL Query in getJSONArrayOfDataTable is:  " + sqlQuery);
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			
			else
			{
				Class.forName(JDBC_DRIVER);
				// $$System.out.println("Connecting to a selected database to get the JSON Array for data table...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// $$System.out.println("got connection...");
				
			}
			
			stmt = conn.prepareStatement(sqlQuery);
			resultSet = stmt.executeQuery();
			jsonArrayForDataTable = ResultsetToJson.convertToJsonArray(resultSet);
			conn.close();
			
			long lt = System.currentTimeMillis();
			System.out.println("getJSONArrayOfDataTable takes " + (lt - dt) / 1000 + " secs");
		}
		
		catch (SQLException se) // Handle errors for JDBC
		{
			se.printStackTrace();
		}
		catch (Exception e) // Handle errors for Class.forName
		{
			e.printStackTrace();
		}
		finally
		// finally block used to close resources
		{
			try
			{
				if (stmt != null)
					conn.close();
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		// %%//$$System.out.println("Exiting getActivityTable(String sqlQuery) from database");
		return jsonArrayForDataTable;
	}
	
	// ////////////
	// ////////////
	/**
	 * Fetches and builds the Activity Table with the given sql where-query
	 * 
	 * @param sqlQuery
	 * @return
	 * @throws SQLException
	 */
	/*
	 * public static ArrayList<ActivityTableRow> getActivityTable(String whereQuery) throws SQLException { Connection conn = null; PreparedStatement stmt = null; ResultSet
	 * resultset = null; ArrayList<ActivityTableRow> activityTableRows=new ArrayList<ActivityTableRow>();
	 * 
	 * try { Class.forName(JDBC_DRIVER); //$$System.out.println("Connecting to a selected database..."); conn = DriverManager.getConnection(DB_URL, USER, PASS);
	 * //$$System.out.println("Connected database successfully...");
	 * 
	 * String sqlQuery = "SELECT * from logdatabase.activitylogs5 "+whereQuery;
	 * 
	 * stmt = conn.prepareStatement(sqlQuery);
	 * 
	 * resultset = stmt.executeQuery();
	 * 
	 * 
	 * while(resultset.next()) { //ActivityTableRow(String user,String day, String activity, String location, String activityStartTime, String activityEndTime, String date)
	 * 
	 * ActivityTableRow activityTableRow=new ActivityTableRow();
	 * 
	 * activityTableRow.setUser(resultset.getString("user_name")); activityTableRow.setDay(resultset.getString("start_day"));
	 * activityTableRow.setActivity(resultset.getString("activity_name")); activityTableRow.setLocation(resultset.getString("location_name"));
	 * activityTableRow.setActivityStartTime(resultset.getString("start_time")); activityTableRow.setActivityEndTime(resultset.getString("end_time"));
	 * activityTableRow.setDate(resultset.getString("start_date"));
	 * 
	 * activityTableRows.add(activityTableRow); } }
	 * 
	 * catch(SQLException se) //Handle errors for JDBC { se.printStackTrace(); } catch(Exception e) //Handle errors for Class.forName { e.printStackTrace(); } finally //finally
	 * block used to close resources { try { if(stmt!=null) conn.close(); } catch(SQLException se) {} try { if(conn!=null) conn.close(); } catch(SQLException se) {
	 * se.printStackTrace(); } } //$$System.out.println("Exiting getActivityTable(String sqlQuery) from database"); return activityTableRows; }
	 */
	
	public static void insertIntoActivityFact(ArrayList<FlatActivityLogEntry> activityLogEntries)
	{
		
		Connection conn = null;
		PreparedStatement stmt = null;
		// final int batchSize = 4000;
		int count = 0;
		System.out.println("inside insertIntoActivityFact");
		long dt = System.currentTimeMillis();
		try
		{
			
			Class.forName(JDBC_DRIVER);
			// $$System.out.println("Connecting to a selected database for insertion in activity_fact_table...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			conn.setAutoCommit(false); // for performance: see http://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html#disable_auto_commit
			// $$System.out.println("Connected database successfully...");
			String sqlInsertion = "INSERT INTO " + databaseName + ".activity_fact_table" + " VALUES (?,?,?,?,?,?,?,?)";
			stmt = conn.prepareStatement(sqlInsertion);
			
			for (FlatActivityLogEntry activityLogEntry : activityLogEntries)
			{
				stmt.setInt(1, activityLogEntry.User_ID);
				stmt.setInt(2, activityLogEntry.Activity_ID);
				stmt.setLong(3, activityLogEntry.Time_ID);
				stmt.setLong(4, activityLogEntry.Date_ID);
				stmt.setLong(5, activityLogEntry.Location_ID);
				stmt.setInt(6, activityLogEntry.Duration);
				stmt.setInt(7, activityLogEntry.Frequency);
				// if (activityLogEntry.Trajectory_ID.length() <= 1)
				// {
				// System.out.println("Alert in insertIntoActivityFact: activityLogEntry.Trajectory_ID = "
				// + activityLogEntry.Trajectory_ID);
				// }
				stmt.setString(8, activityLogEntry.Trajectory_ID);
				
				stmt.addBatch();
				
				// System.out.println("-->"+activityLogEntry.toString()); //gives correct results
				// if(++count % BATCH_SIZE == 0) //Smart Insert: Batch within Batch, ref: http://viralpatel.net/blogs/batch-insert-in-java-jdbc/
				// {
				// stmt.executeBatch();
				// }
			}
			stmt.executeBatch();// insert remaining records
			conn.commit();
			// stmt.executeUpdate();
			long lt = System.currentTimeMillis();
			
			System.out.println(
					"Inserted " + activityLogEntries.size() + " records into the activity_fact_table in " + (lt - dt) / 1000 + " secs");
		}
		
		catch (SQLException se) // Handle errors for JDBC
		{
			se.printStackTrace();
		}
		catch (Exception e) // Handle errors for Class.forName
		{
			e.printStackTrace();
		}
		finally
		// finally block used to close resources
		{
			try
			{
				conn.setAutoCommit(true);
				if (stmt != null)
				{
					conn.close();
				}
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
			
		}
		
	}
	
	// ////
	// ////////////
	
	public static void insertIntoActivityFact(FlatActivityLogEntry activityLogEntry)
	{
		
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try
		{
			Class.forName(JDBC_DRIVER);
			// $$System.out.println("Connecting to a selected database for insertion in activity_fact_table...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			// $$System.out.println("Connected database successfully...");
			String sqlInsertion = "INSERT INTO " + databaseName + ".activity_fact_table" + " VALUES (?,?,?,?,?,?,?)";
			stmt = conn.prepareStatement(sqlInsertion);
			
			stmt.setInt(1, activityLogEntry.User_ID);
			stmt.setInt(2, activityLogEntry.Activity_ID);
			stmt.setLong(3, activityLogEntry.Time_ID);
			stmt.setLong(4, activityLogEntry.Date_ID);
			stmt.setLong(5, activityLogEntry.Location_ID);
			stmt.setInt(6, activityLogEntry.Duration);
			stmt.setInt(7, activityLogEntry.Frequency);
			
			stmt.executeUpdate();
			
			// $$System.out.println("Inserted record into the activity_fact_table...");
		}
		
		catch (SQLException se) // Handle errors for JDBC
		{
			se.printStackTrace();
		}
		catch (Exception e) // Handle errors for Class.forName
		{
			e.printStackTrace();
		}
		finally
		// finally block used to close resources
		{
			try
			{
				if (stmt != null)
					conn.close();
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		
	}
	
	// ////
	
	// ////////////
	public static void insertIntoUserDimension(int userId, String userName, int userAge, String personality, String profession,
			String ageCategory)
	{
		
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try
		{
			
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			
			// Class.forName(JDBC_DRIVER);
			// $$System.out.println("Connecting to a selected database for insertion into user dimension table...");
			// conn = DriverManager.getConnection(DB_URL, USER, PASS);
			// $$System.out.println("Connected database successfully...");
			String sqlInsertion = "INSERT INTO " + databaseName + ".user_dimension_table" + " VALUES (?,?,?,?,?,?)";
			stmt = conn.prepareStatement(sqlInsertion);
			
			stmt.setInt(1, userId);
			stmt.setString(2, userName);
			stmt.setInt(3, userAge);
			stmt.setString(4, personality);
			stmt.setString(5, profession);
			stmt.setString(6, ageCategory);
			
			stmt.executeUpdate();
			
			System.out.println("Inserted record into the user_dimension_table...");
		}
		
		catch (SQLException se) // Handle errors for JDBC
		{
			se.printStackTrace();
		}
		catch (Exception e) // Handle errors for Class.forName
		{
			e.printStackTrace();
		}
		finally
		// finally block used to close resources
		{
			try
			{
				if (stmt != null)
					conn.close();
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		
	}
	
	// ////
	
	// ////////////
	public static void insertIntoActivityDimension(int activityId, String activityName, String activityCategory)
	{
		
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			// Class.forName(JDBC_DRIVER);
			// $$System.out.println("Connecting to a selected database for insertion into actitvity dimension table...");
			// conn = DriverManager.getConnection(DB_URL, USER, PASS);
			// $$System.out.println("Connected database successfully...");
			String sqlInsertion = "INSERT INTO " + databaseName + ".activity_dimension_table" + " VALUES (?,?,?)";
			stmt = conn.prepareStatement(sqlInsertion);
			
			stmt.setInt(1, activityId);
			stmt.setString(2, activityName);
			stmt.setString(3, activityCategory);
			
			stmt.executeUpdate();
			
			// $$System.out.println("Inserted record into the activity_dimension_table...");
		}
		
		catch (SQLException se) // Handle errors for JDBC
		{
			se.printStackTrace();
		}
		catch (Exception e) // Handle errors for Class.forName
		{
			e.printStackTrace();
		}
		finally
		// finally block used to close resources
		{
			try
			{
				if (stmt != null)
					conn.close();
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		
	}
	
	// ////
	
	// ////////////
	/**
	 * note: faster
	 * 
	 * @param activityLogEntries
	 */
	public static void insertIntoTimeDimension(ArrayList<FlatActivityLogEntry> activityLogEntries)
	{
		
		Connection conn = null;
		PreparedStatement stmt = null;
		int count = 0;
		
		System.out.println("inside insertIntoTimeDimension");
		long dt = System.currentTimeMillis();
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			// Class.forName(JDBC_DRIVER);
			// $$System.out.println("Connecting to a selected database for insertion into time dimension table...");
			// conn = DriverManager.getConnection(DB_URL, USER, PASS);
			conn.setAutoCommit(false); // for performance: see http://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html#disable_auto_commit
			// $$System.out.println("Connected database successfully...");
			String sqlInsertion = "INSERT INTO " + databaseName + ".time_dimension_table" + " VALUES (?,?,?,?)";
			stmt = conn.prepareStatement(sqlInsertion);
			
			for (FlatActivityLogEntry activityLogEntry : activityLogEntries)
			{
				stmt.setLong(1, activityLogEntry.Time_ID);
				stmt.setTime(2, activityLogEntry.Start_Time);
				stmt.setTime(3, activityLogEntry.End_Time);
				stmt.setString(4, activityLogEntry.Time_Category);
				
				stmt.addBatch();
				
				// if(++count % BATCH_SIZE == 0) //Smart Insert: Batch within Batch, ref: http://viralpatel.net/blogs/batch-insert-in-java-jdbc/
				// {
				// stmt.executeBatch();
				// }
			}
			stmt.executeBatch();// insert remaining records
			conn.commit();
			long lt = System.currentTimeMillis();
			
			System.out.println(
					"Inserted " + activityLogEntries.size() + " records into the time_dimension_table in " + (lt - dt) / 1000 + " secs");
			
		}
		
		catch (SQLException se) // Handle errors for JDBC
		{
			se.printStackTrace();
		}
		catch (Exception e) // Handle errors for Class.forName
		{
			e.printStackTrace();
		}
		finally
		// finally block used to close resources
		{
			try
			{
				conn.setAutoCommit(true);
				if (stmt != null)
				{
					conn.close();
				}
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		
	}
	
	// ////
	// ////////////
	public static void insertIntoTimeDimension(FlatActivityLogEntry activityLogEntry)
	{
		
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try
		{
			// Class.forName(JDBC_DRIVER);
			// $$System.out.println("Connecting to a selected database for insertion into time dimension table...");
			// conn = DriverManager.getConnection(DB_URL, USER, PASS);
			// $$System.out.println("Connected database successfully...");
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			String sqlInsertion = "INSERT INTO " + databaseName + ".time_dimension_table" + " VALUES (?,?,?,?)";
			stmt = conn.prepareStatement(sqlInsertion);
			
			stmt.setLong(1, activityLogEntry.Time_ID);
			stmt.setTime(2, activityLogEntry.Start_Time);
			stmt.setTime(3, activityLogEntry.End_Time);
			stmt.setString(4, activityLogEntry.Time_Category);
			stmt.executeUpdate();
			
			// $$System.out.println("Inserted record into the time_dimension_table...");
		}
		
		catch (SQLException se) // Handle errors for JDBC
		{
			se.printStackTrace();
		}
		catch (Exception e) // Handle errors for Class.forName
		{
			e.printStackTrace();
		}
		finally
		// finally block used to close resources
		{
			try
			{
				if (stmt != null)
					conn.close();
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		
	}
	
	// ////
	// ////////////
	
	public static void insertIntoDateDimension(ArrayList<FlatActivityLogEntry> activityLogEntries)
	{
		
		Connection conn = null;
		PreparedStatement stmt = null;
		int count = 0;
		long dt = System.currentTimeMillis();
		System.out.println("inside insertIntoDateDimension");
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			// Class.forName(JDBC_DRIVER);
			// $$System.out.println("Connecting to a selected database for inserting into date dimension table...");
			// conn = DriverManager.getConnection(DB_URL, USER, PASS);
			conn.setAutoCommit(false);
			// $$System.out.println("Connected database successfully...");
			String sqlInsertion = "INSERT INTO " + databaseName + ".date_dimension_table" + " VALUES (?,?,?,?,?,?,?,?)";
			stmt = conn.prepareStatement(sqlInsertion);
			
			for (FlatActivityLogEntry activityLogEntry : activityLogEntries)
			{
				stmt.setLong(1, activityLogEntry.Date_ID);
				stmt.setDate(2, activityLogEntry.Start_Date);
				stmt.setString(3, activityLogEntry.Week_Day);
				stmt.setInt(4, activityLogEntry.Week);
				stmt.setString(5, activityLogEntry.Month);
				stmt.setString(6, activityLogEntry.Quarter);
				stmt.setInt(7, activityLogEntry.Year);
				stmt.setDate(8, activityLogEntry.End_Date);
				stmt.addBatch();
				
				// if(++count % BATCH_SIZE == 0)
				// {
				// stmt.executeBatch();
				// }
			}
			
			stmt.executeBatch();// insert remaining records
			conn.commit();
			long lt = System.currentTimeMillis();
			System.out.println(
					"Inserted " + activityLogEntries.size() + " record into the date_dimension_table in " + (lt - dt) / 1000 + " secs");
		}
		
		catch (SQLException se) // Handle errors for JDBC
		{
			se.printStackTrace();
		}
		catch (Exception e) // Handle errors for Class.forName
		{
			e.printStackTrace();
		}
		finally
		// finally block used to close resources
		{
			try
			{
				conn.setAutoCommit(true);
				if (stmt != null)
				{
					conn.close();
				}
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		
	}
	
	// ////
	
	// ////////////
	
	public static void insertIntoDateDimension(FlatActivityLogEntry activityLogEntry)
	{
		
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			// Class.forName(JDBC_DRIVER);
			// $$System.out.println("Connecting to a selected database for inserting into date dimension table...");
			// conn = DriverManager.getConnection(DB_URL, USER, PASS);
			// $$System.out.println("Connected database successfully...");
			String sqlInsertion = "INSERT INTO " + databaseName + ".date_dimension_table" + " VALUES (?,?,?,?,?,?,?)";
			stmt = conn.prepareStatement(sqlInsertion);
			
			stmt.setLong(1, activityLogEntry.Date_ID);
			stmt.setDate(2, activityLogEntry.Start_Date);
			stmt.setString(3, activityLogEntry.Week_Day);
			stmt.setInt(4, activityLogEntry.Week);
			stmt.setString(5, activityLogEntry.Month);
			stmt.setString(6, activityLogEntry.Quarter);
			stmt.setInt(7, activityLogEntry.Year);
			stmt.executeUpdate();
			
			// $$System.out.println("Inserted record into the date_dimension_table...");
		}
		
		catch (SQLException se) // Handle errors for JDBC
		{
			se.printStackTrace();
		}
		catch (Exception e) // Handle errors for Class.forName
		{
			e.printStackTrace();
		}
		finally
		// finally block used to close resources
		{
			try
			{
				if (stmt != null)
					conn.close();
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Return a connection to the data base. <b>Make sure to close this connection after use.</b>
	 * 
	 * @return
	 */
	public static Connection getConnection(boolean autocommit)
	{
		Connection conn = null;
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				conn.setAutoCommit(autocommit);
				// //$$System.out.println("Connected database successfully...");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return conn;
		
	}
	
	// ////
	// Location_iD
	public static void insertIntoLocationDimensionTrajectory(ArrayList<FlatActivityLogEntry> activityLogEntries)
	{
		
		Connection conn = null;
		PreparedStatement stmt = null;
		int count = 0;
		
		System.out.println("inside insertIntoLocationDimensionTrajectory");
		long dt = System.currentTimeMillis();
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER); // //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS); // $$System.out.println("Connected database successfully...");
			}
			// Class.forName(JDBC_DRIVER);// $$System.out.println("Connecting to a selected database for inserting into location dimension table...");
			// conn = DriverManager.getConnection(DB_URL, USER, PASS);
			conn.setAutoCommit(false);
			// $$System.out.println("Connected database successfully...");
			String sqlInsertion =
					"INSERT INTO " + databaseName + ".location_dimension_table" + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			stmt = conn.prepareStatement(sqlInsertion);
			
			System.out.println("Num of location entries to be made: " + activityLogEntries.size());
			
			for (FlatActivityLogEntry activityLogEntry : activityLogEntries)
			{
				// stmt.setLong(1, activityLogEntry.Location_ID);
				// stmt.setString(2, activityLogEntry.Latitudes);
				// stmt.setString(3, activityLogEntry.Longitudes);
				// stmt.setString(4, activityLogEntry.Altitudes);
				//
				// stmt.setString(5, activityLogEntry.Location_Name);
				// stmt.setString(6, activityLogEntry.Location_Category);
				// stmt.setString(7, activityLogEntry.City);
				// stmt.setString(8, activityLogEntry.County);
				// stmt.setString(9, activityLogEntry.Country);
				// stmt.setString(10, activityLogEntry.Continent);
				// stmt.setBigDecimal(11, new BigDecimal(activityLogEntry.Start_Latitude));
				// stmt.setBigDecimal(12, new BigDecimal(activityLogEntry.End_Latitude));
				//
				// stmt.setBigDecimal(13, new BigDecimal(activityLogEntry.Start_Longitude));
				// stmt.setBigDecimal(14, new BigDecimal(activityLogEntry.End_Longitude));
				//
				// stmt.setBigDecimal(15, new BigDecimal(activityLogEntry.Start_Altitude));
				// stmt.setBigDecimal(16, new BigDecimal(activityLogEntry.End_Altitude));
				//
				// stmt.setBigDecimal(17, new BigDecimal(activityLogEntry.Avg_Altitude));
				stmt.setLong(1, activityLogEntry.Location_ID);
				stmt.setString(2, activityLogEntry.Latitudes);// activityLogEntry.Latitudes);
				stmt.setString(3, activityLogEntry.Longitudes);
				stmt.setString(4, activityLogEntry.Altitudes);
				
				stmt.setString(5, activityLogEntry.Location_Name);
				stmt.setString(6, activityLogEntry.Location_Category);
				stmt.setString(7, activityLogEntry.City);
				stmt.setString(8, activityLogEntry.County);
				stmt.setString(9, activityLogEntry.Country);
				stmt.setString(10, activityLogEntry.Continent);
				stmt.setString(11, (activityLogEntry.Start_Latitude));
				stmt.setString(12, (activityLogEntry.End_Latitude));
				
				stmt.setString(13, (activityLogEntry.Start_Longitude));
				stmt.setString(14, (activityLogEntry.End_Longitude));
				
				stmt.setString(15, (activityLogEntry.Start_Altitude));
				stmt.setString(16, (activityLogEntry.End_Altitude));
				
				stmt.setString(17, (activityLogEntry.Avg_Altitude));
				
				try
				{
					stmt.addBatch();
				}
				catch (Exception e)
				{
					System.err.println("Exception in 	stmt.executeBatch()");
					e.printStackTrace();
					System.exit(-6);
					
				}
				// if (count % 200 == 0)
				// {
				// System.out.println("Added " + count + " entry to batch");
				// }
				count++;
				// if (++count % BATCH_SIZE == 0) // Smart Insert: Batch within Batch, ref: http://viralpatel.net/blogs/batch-insert-in-java-jdbc/
				// {
				
				// stmt.executeBatch();
				// }
			}
			
			System.out.println("stmt.getQueryTimeout = " + stmt.getQueryTimeout());
			System.out.println("stmt.isClosed = " + stmt.isClosed());
			System.out.println("conn.isClosed = " + conn.isClosed());
			System.out.println("conn.isValid(100) = " + conn.isValid(100));
			
			long ct = System.currentTimeMillis();
			System.out.println("Time passed since connection created = " + (ct - dt) / 1000 + " secs");
			
			stmt.executeBatch();// insert remaining records
			conn.commit();
			long lt = System.currentTimeMillis();
			// System.out.println("insertion of location takes "
			
			System.out.println("Inserted " + activityLogEntries.size() + " records into the location_dimension_table in " + (lt - dt) / 1000
					+ " secs");
		}
		
		catch (SQLTimeoutException se) // Handle errors for JDBC
		{
			System.err.println("--> SQL timeout exception");
			se.printStackTrace();
		}
		catch (SQLException se) // Handle errors for JDBC
		{
			System.err.println(" --> SQL exception");
			se.printStackTrace();
		}
		catch (Exception e) // Handle errors for Class.forName
		{
			System.err.println("--> Other exception");
			e.printStackTrace();
		}
		finally
		// finally block used to close resources
		{
			System.out.println("--> In finally block");
			try
			{
				conn.setAutoCommit(true);
				if (stmt != null)
				{
					conn.close();
				}
			}
			catch (SQLException se)
			{
				System.err.println("--> SQL exception inside finally block ");
				se.printStackTrace();
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		
	}
	
	// /
	
	// ////
	// Location_iD
	public static void insertIntoLocationDimensionTrajectoryWithoutBatch(ArrayList<FlatActivityLogEntry> activityLogEntries)
	{
		
		Connection conn = null;
		PreparedStatement stmt = null;
		int count = 0;
		
		System.out.println("inside insertIntoLocationDimensionTrajectory");
		long dt = System.currentTimeMillis();
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				System.exit(-9);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			// Class.forName(JDBC_DRIVER);
			// $$System.out.println("Connecting to a selected database for inserting into location dimension table...");
			// conn = DriverManager.getConnection(DB_URL, USER, PASS);
			conn.setAutoCommit(false);
			// $$System.out.println("Connected database successfully...");
			String sqlInsertion =
					"INSERT INTO " + databaseName + ".location_dimension_table" + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			stmt = conn.prepareStatement(sqlInsertion);
			System.out.println("Num of location entries to be made: " + activityLogEntries.size());
			
			for (FlatActivityLogEntry activityLogEntry : activityLogEntries)
			{
				stmt.setLong(1, activityLogEntry.Location_ID);
				
				String latitudeString = activityLogEntry.Latitudes;
				System.out.println("Latitude string is of length" + latitudeString.length());
				// if (latitudeString.length() > 20000)
				// {
				// System.out.println("Greater:+ " + latitudeString);
				// }
				stmt.setString(2, latitudeString);// activityLogEntry.Latitudes);
				
				try
				{
					String longitudeString = activityLogEntry.Longitudes;
					System.out.println("Longitude string is of length" + longitudeString.length());
					// if (longitudeString.length() > 20000)
					// {
					// System.out.println("Greater:+ " + longitudeString);
					// }
					stmt.setString(3, longitudeString);// activityLogEntry.Longitudes);
				}
				catch (Exception e)
				{
					System.err.println("Exception stmt.setString(3, longitudeString);");
					e.printStackTrace();
					System.exit(-8);
				}
				
				stmt.setString(4, activityLogEntry.Altitudes);
				
				stmt.setString(5, activityLogEntry.Location_Name);
				stmt.setString(6, activityLogEntry.Location_Category);
				stmt.setString(7, activityLogEntry.City);
				stmt.setString(8, activityLogEntry.County);
				stmt.setString(9, activityLogEntry.Country);
				stmt.setString(10, activityLogEntry.Continent);
				stmt.setBigDecimal(11, new BigDecimal(activityLogEntry.Start_Latitude));
				stmt.setBigDecimal(12, new BigDecimal(activityLogEntry.End_Latitude));
				
				stmt.setBigDecimal(13, new BigDecimal(activityLogEntry.Start_Longitude));
				stmt.setBigDecimal(14, new BigDecimal(activityLogEntry.End_Longitude));
				
				stmt.setBigDecimal(15, new BigDecimal(activityLogEntry.Start_Altitude));
				stmt.setBigDecimal(16, new BigDecimal(activityLogEntry.End_Altitude));
				
				stmt.setBigDecimal(17, new BigDecimal(activityLogEntry.Avg_Altitude));
				
				try
				{
					stmt.executeUpdate();
				}
				catch (Exception e)
				{
					System.err.println("Exception stmt.executeUpdate");
					e.printStackTrace();
					System.exit(-9);
				}
				System.out.println("Executed  " + ++count + " insertion in location dimension");
				// stmt.addBatch();
				
				// if(++count % BATCH_SIZE == 0) //Smart Insert: Batch within Batch, ref: http://viralpatel.net/blogs/batch-insert-in-java-jdbc/
				// {
				// stmt.executeBatch();
				// }
			}
			// stmt.executeBatch();// insert remaining records
			conn.commit();
			long lt = System.currentTimeMillis();
			// System.out.println("insertion of location takes "
			
			System.out.println("Inserted " + activityLogEntries.size() + " records into the location_dimension_table (no batch update) in "
					+ (lt - dt) / 1000 + " secs");
		}
		
		catch (SQLException se) // Handle errors for JDBC
		{
			se.printStackTrace();
		}
		catch (Exception e) // Handle errors for Class.forName
		{
			e.printStackTrace();
		}
		finally
		// finally block used to close resources
		{
			try
			{
				conn.setAutoCommit(true);
				if (stmt != null)
				{
					conn.close();
				}
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		
	}
	
	// ///////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * For synthetic data only. <font color="red"> THIS MIGHT NEED SOME MODIFICATION. </br>
	 * ONLY TO BE USED FOR SYNTHETIC DATA. </font>
	 * 
	 * @param activityLogEntries
	 */
	public static void insertIntoLocationDimension(FlatActivityLogEntry activityLogEntry)
	{
		
		Connection conn = null;
		PreparedStatement stmt = null;
		int count = 0;
		
		System.out.println("inside insertIntoLocationDimension");
		long dt = System.currentTimeMillis();
		try
		{
			
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			// Class.forName(JDBC_DRIVER);
			// $$System.out.println("Connecting to a selected database for inserting into location dimension table...");
			// conn = DriverManager.getConnection(DB_URL, USER, PASS);
			conn.setAutoCommit(false);
			// $$System.out.println("Connected database successfully...");
			String sqlInsertion =
					"INSERT INTO " + databaseName + ".location_dimension_table" + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			stmt = conn.prepareStatement(sqlInsertion);
			// for (FlatActivityLogEntry activityLogEntry : activityLogEntries)
			{
				stmt.setLong(1, activityLogEntry.Location_ID);
				// stmt.setDouble(2, -99);
				// stmt.setDouble(3, activityLogEntry.Longitudes);
				// stmt.setString(4, activityLogEntry.Altitudes);
				
				stmt.setString(5, activityLogEntry.Location_Name);
				stmt.setString(6, activityLogEntry.Location_Category);
				stmt.setString(7, activityLogEntry.City);
				stmt.setString(8, activityLogEntry.County);
				stmt.setString(9, activityLogEntry.Country);
				stmt.setString(10, activityLogEntry.Continent);
				
				stmt.addBatch();
			}
			stmt.executeBatch();// insert remaining records
			conn.commit();
			long lt = System.currentTimeMillis();
			// System.out.println("insertion of location takes "
			
			System.out.println("Inserted " + 1 + " record into the location_dimension_table in " + (lt - dt) / 1000 + " secs");
		}
		
		catch (SQLException se) // Handle errors for JDBC
		{
			se.printStackTrace();
		}
		catch (Exception e) // Handle errors for Class.forName
		{
			e.printStackTrace();
		}
		finally
		// finally block used to close resources
		{
			try
			{
				conn.setAutoCommit(true);
				if (stmt != null)
				{
					conn.close();
				}
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		
	}
	
	// ///////////////////////////////////////////////////////////////////////////////////////////////////////
	// ////
	// Location_iD
	// public static void insertIntoLocationDimensionTrajectory(FlatActivityLogEntry activityLogEntry)
	// {
	//
	// Connection conn = null;
	// PreparedStatement stmt = null;
	//
	// try
	// {
	// Class.forName(JDBC_DRIVER);
	// //$$System.out.println("Connecting to a selected database for inserting into location dimension table...");
	// conn = DriverManager.getConnection(DB_URL, USER, PASS);
	// //$$System.out.println("Connected database successfully...");
	// String sqlInsertion = "INSERT INTO "+databaseName+".location_dimension_table" +
	// " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	// stmt = conn.prepareStatement(sqlInsertion);
	//
	// stmt.setLong(1,activityLogEntry.Location_ID);
	// stmt.setString(2,activityLogEntry.Latitudes);
	// stmt.setString(3,activityLogEntry.Longitudes);
	// stmt.setString(4,activityLogEntry.Altitudes);
	//
	//
	// stmt.setString(5,activityLogEntry.Location_Name);
	// stmt.setString(6,activityLogEntry.Location_Category);
	// stmt.setString(7,activityLogEntry.City);
	// stmt.setString(8,activityLogEntry.County);
	// stmt.setString(9,activityLogEntry.Country);
	// stmt.setString(10,activityLogEntry.Continent);
	// stmt.setBigDecimal(11,new BigDecimal(activityLogEntry.Start_Latitude,new MathContext(2)));
	// stmt.setBigDecimal(12,new BigDecimal(activityLogEntry.End_Latitude,new MathContext(2)));
	//
	// stmt.setBigDecimal(13,new BigDecimal(activityLogEntry.Start_Longitude,new MathContext(2)));
	// stmt.setBigDecimal(14,new BigDecimal(activityLogEntry.End_Longitude,new MathContext(2)));
	//
	// stmt.setBigDecimal(15,new BigDecimal(activityLogEntry.Start_Altitude,new MathContext(2)));
	// stmt.setBigDecimal(16,new BigDecimal(activityLogEntry.End_Altitude,new MathContext(2)));
	//
	// stmt.setBigDecimal(17,new BigDecimal(activityLogEntry.Avg_Altitude,new MathContext(2)));
	//
	// stmt.executeUpdate();
	//
	// //$$System.out.println("Inserted record into the location_dimension_table...");
	// }
	//
	// catch(SQLException se) //Handle errors for JDBC
	// {
	// se.printStackTrace();
	// }
	// catch(Exception e) //Handle errors for Class.forName
	// {
	// e.printStackTrace();
	// }
	// finally //finally block used to close resources
	// {
	// try
	// {
	// if(stmt!=null)
	// conn.close();
	// }
	// catch(SQLException se)
	// {}
	// try
	// {
	// if(conn!=null)
	// conn.close();
	// }
	// catch(SQLException se)
	// {
	// se.printStackTrace();
	// }
	// }
	//
	// }
	//
	
	// ////////////
	// public static void insertIntoLocationDimension(FlatActivityLogEntry activityLogEntry)
	// {
	//
	// Connection conn = null;
	// PreparedStatement stmt = null;
	//
	// try
	// {
	// Class.forName(JDBC_DRIVER);
	// //$$System.out.println("Connecting to a selected database for inserting into location dimension table...");
	// conn = DriverManager.getConnection(DB_URL, USER, PASS);
	// //$$System.out.println("Connected database successfully...");
	// String sqlInsertion = "INSERT INTO "+databaseName+".location_dimension_table" +
	// " VALUES (?,?,?,?,?,?,?,?,?,?)";
	// stmt = conn.prepareStatement(sqlInsertion);
	//
	// stmt.setInt(1,activityLogEntry.Location_ID);
	// stmt.setBigDecimal(2,new BigDecimal(activityLogEntry.Latitude,new MathContext(2)));
	// stmt.setBigDecimal(3,new BigDecimal(activityLogEntry.Longitude,new MathContext(2)));
	// stmt.setString(4,activityLogEntry.Location_Name);
	// stmt.setString(5,activityLogEntry.Location_Category);
	// stmt.setString(6,activityLogEntry.City);
	// stmt.setString(7,activityLogEntry.County);
	// stmt.setString(8,activityLogEntry.Country);
	// stmt.setString(9,activityLogEntry.Continent);
	// stmt.setBigDecimal(10,new BigDecimal(activityLogEntry.Altitude,new MathContext(2)));
	// stmt.executeUpdate();
	//
	// //$$System.out.println("Inserted record into the location_dimension_table...");
	// }
	//
	// catch(SQLException se) //Handle errors for JDBC
	// {
	// se.printStackTrace();
	// }
	// catch(Exception e) //Handle errors for Class.forName
	// {
	// e.printStackTrace();
	// }
	// finally //finally block used to close resources
	// {
	// try
	// {
	// if(stmt!=null)
	// conn.close();
	// }
	// catch(SQLException se)
	// {}
	// try
	// {
	// if(conn!=null)
	// conn.close();
	// }
	// catch(SQLException se)
	// {
	// se.printStackTrace();
	// }
	// }
	//
	// }
	//
	
	// /
	public static String getFactTableName() throws SQLException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultset = null;
		String factTableName = new String();
		
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			
			String sqlQuery = "SELECT * from " + databaseName + ".meta;";
			stmt = conn.prepareStatement(sqlQuery);
			
			resultset = stmt.executeQuery();
			
			while (resultset.next())
			{
				if (resultset.getString("Element_Type").equals("Fact"))
				{
					factTableName = resultset.getString("Belongs_To_Table");
					break;
				}
			}
		}
		
		catch (SQLException se)
		{
			se.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (stmt != null)
					conn.close();
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		// $$System.out.println("Exiting getFactTableName() from meta base");
		
		return factTableName;
	}
	
	// /
	
	// /
	
	public static ArrayList<String> getDimensionTableNames() throws SQLException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultset = null;
		ArrayList<String> dimensionNames = new ArrayList<String>();
		
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			
			String sqlQuery = "SELECT * from " + databaseName + ".meta;";
			stmt = conn.prepareStatement(sqlQuery);
			
			resultset = stmt.executeQuery();
			
			while (resultset.next())
			{
				if (resultset.getString("Element_Type").equals("Dimension"))
				{
					dimensionNames.add(resultset.getString("Belongs_To_Table"));
				}
			}
		}
		
		catch (SQLException se)
		{
			se.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (stmt != null)
					conn.close();
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		// $$System.out.println("Exiting getDimensionNames() from meta base");
		return dimensionNames;
	}
	
	// /
	
	public static ArrayList<String> getDimensionNamesFromDatabase() throws SQLException // eg. of dimension name 'User_Dimension'
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultset = null;
		ArrayList<String> dimensionNames = new ArrayList<String>();
		
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			
			String sqlQuery = "SELECT * from " + databaseName + ".meta;";
			stmt = conn.prepareStatement(sqlQuery);
			
			resultset = stmt.executeQuery();
			
			while (resultset.next())
			{
				if (resultset.getString("Element_Type").equals("Dimension"))
				{
					dimensionNames.add(resultset.getString("Element_Name"));
				}
			}
		}
		
		catch (SQLException se)
		{
			se.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (stmt != null)
					conn.close();
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		// $$System.out.println("Exiting getDimensionNames() from meta base");
		return dimensionNames;
	}
	
	public static void setDimensionNames()
	{
		try
		{
			dimensionNames = getDimensionNamesFromDatabase();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	// /**
	// * NOT TO BE USED YET, META TABLE IN DATABSE NEED TO BE REFACTORED
	// *
	// * @return
	// */
	// public static ArrayList<String> getDimensionNames()
	// {
	// if (dimensionNames.size() == 0)
	// {
	// System.err.println("Error in org.activity.util.ConnectDatabase.getDimensionNames(): dimensionNames.size()==0");
	// new Exception("Error in org.activity.util.ConnectDatabase.getDimensionNames(): dimensionNames.size()==0");
	// }
	// return dimensionNames;
	// }
	
	public static void setUserIDNameMap()// eg. of dimension name 'User_Dimension'
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultset = null;
		// String name = "Nefertiti";
		// ArrayList<String> dimensionNames=new ArrayList<String>();
		userIDNameMap = new HashMap<Integer, String>();
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			
			String sqlQuery = "SELECT * from " + databaseName + ".user_dimension_table;"; // where User_ID=\"" + userID + "\";";
			stmt = conn.prepareStatement(sqlQuery);
			
			resultset = stmt.executeQuery();
			
			while (resultset.next())
			{
				String name = resultset.getString("User_Name");
				int id = resultset.getInt("User_ID");
				userIDNameMap.put(new Integer(id), name);
				// break;
			}
			// System.out.println("User_Name found from database for useid:" + userID + " is " + name);
		}
		
		catch (SQLException se)
		{
			se.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (stmt != null)
					conn.close();
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		// $$System.out.println("Exiting getDimensionNames() from meta base");
		// return name;
	}
	
	public static String getUserName(int userID)
	{
		String name = userIDNameMap.get(userID);
		
		if (name.length() > 0)
		{
			return name;
		}
		
		else
		{
			new Exception("User name not found for user id:" + userID);
			return "Nefertiti";
		}
	}
	
	public static String getUserNameFromDatabase(int userID) throws SQLException // eg. of dimension name 'User_Dimension'
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultset = null;
		String name = "Nefertiti";
		// ArrayList<String> dimensionNames=new ArrayList<String>();
		
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			
			String sqlQuery = "SELECT User_Name from " + databaseName + ".user_dimension_table where User_ID=\"" + userID + "\";";
			stmt = conn.prepareStatement(sqlQuery);
			
			resultset = stmt.executeQuery();
			
			while (resultset.next())
			{
				name = resultset.getString("User_Name");
				break;
			}
			System.out.println("User_Name found from database for useid:" + userID + " is " + name);
		}
		
		catch (SQLException se)
		{
			se.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (stmt != null)
					conn.close();
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		// $$System.out.println("Exiting getDimensionNames() from meta base");
		return name;
	}
	
	/**
	 * <p>
	 * Set the activityNameID and activityIDName maps (static variables), The activity id and activity names are fetched from the database.
	 * </p>
	 */
	public static int setActivityIDNameMaps()
	{
		// PopUps.showMessage("Inside org.activity.util.ConnectDatabase.setActivityIDNameMaps()");
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultset = null;
		String name = "Nefertiti";
		// ArrayList<String> dimensioactivityNameIDMapnNames=new ArrayList<String>();
		activityIDNameMap = new HashMap<Integer, String>();
		activityNameIDMap = new HashMap<String, Integer>();
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			
			String sqlQuery = "SELECT * from " + databaseName + ".activity_dimension_table;";// where Activity_ID=\"" + activityID + "\";";
			stmt = conn.prepareStatement(sqlQuery);
			
			resultset = stmt.executeQuery();
			
			while (resultset.next())
			{
				name = resultset.getString("Activity_Name");
				int id = resultset.getInt("Activity_ID");
				activityIDNameMap.put(new Integer(id), name);
				activityNameIDMap.put(name, new Integer(id));
				// break;
			}
		}
		
		catch (SQLException se)
		{
			se.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (stmt != null)
					conn.close();
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		// $$System.out.println("Exiting getDimensionNames() from meta base");
		// activityIDNameMap = activityIDNameMap;
		// activityNameIDMap = activityNameIDMap;
		if (activityIDNameMap.size() > 0 && activityNameIDMap.size() > 0)
		{
			return 1;
		}
		else
		{
			new Exception("setActivityIDNameMaps() has activityIDNameMap of size=" + activityIDNameMap.size()
					+ " and activityNameIDMap of size=" + activityNameIDMap.size());
			return -1;
		}
	}
	
	public static String getActivityName(int activityID) // eg. of dimension name 'User_Dimension'
	{
		String name = activityIDNameMap.get(activityID);
		
		if (name.length() > 0)
		{
			return name;
		}
		else
		{
			new Exception(" Activity Name not found for Activity ID:" + activityID);
			return "Nefertiti";
		}
	}
	
	public static String getActivityNameFromDatabase(int activityID) throws SQLException // eg. of dimension name 'User_Dimension'
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultset = null;
		String name = "Nefertiti";
		// ArrayList<String> dimensionNames=new ArrayList<String>();
		
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			
			String sqlQuery =
					"SELECT Activity_Name from " + databaseName + ".activity_dimension_table where Activity_ID=\"" + activityID + "\";";
			stmt = conn.prepareStatement(sqlQuery);
			
			resultset = stmt.executeQuery();
			
			while (resultset.next())
			{
				name = resultset.getString("Activity_Name");
				break;
			}
		}
		
		catch (SQLException se)
		{
			se.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (stmt != null)
					conn.close();
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		// $$System.out.println("Exiting getDimensionNames() from meta base");
		return name;
	}
	
	public static int getActivityID(String activityName)// eg. of dimension name 'User_Dimension'
	{
		if (Constant.getDatabaseName().equals("gowalla1"))
		{
			return Integer.valueOf(activityName);// because in gowalla dataset, we consider activity id as activity name as of 19 Sep 2016
		}
		if (activityNameIDMap.size() == 0)
		{
			new Exception("activityNameIDMap.size()==0");
		}
		
		if (activityName == null)
		{
			new Exception("activityName == null");
		}
		
		if (activityNameIDMap == null)
		{
			new Exception("activityNameIDMap == null");
		}
		// ////////////
		// System.out.println("\nCHeck: inside org.activity.util.ConnectDatabase.getActivityID(String)");
		// System.out.println("Iterating over activitynameid map and checking for activity name: " + activityName + "");
		// for (Entry<String, Integer> entry : activityNameIDMap.entrySet())
		// {
		// System.out.print("ActName = " + entry.getKey() + " ActID = " + entry.getValue());
		// }
		// //////////
		int id = activityNameIDMap.get(activityName);
		
		if (id >= 0)
		{
			return id;
		}
		else
		{
			new Exception(" Activity ID not found for Activity Name:" + activityName);
			return -1;
		}
	}
	
	public static int getActivityIDFromDatabase(String activityName) throws SQLException // eg. of dimension name 'User_Dimension'
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultset = null;
		int id = -99;
		// ArrayList<String> dimensionNames=new ArrayList<String>();
		
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			
			String sqlQuery =
					"SELECT Activity_ID from " + databaseName + ".activity_dimension_table where Activity_Name =\"" + activityName + "\";";
			stmt = conn.prepareStatement(sqlQuery);
			
			resultset = stmt.executeQuery();
			
			while (resultset.next())
			{
				id = resultset.getInt("Activity_ID");
				break;
			}
		}
		
		catch (SQLException se)
		{
			se.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (stmt != null)
					conn.close();
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		// $$System.out.println("Exiting getDimensionNames() from meta base");
		return id;
	}
	
	// /
	
	public static ArrayList<ArrayList<String>> getDimensionAttributes(String dimensionName) throws SQLException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultset = null;
		ArrayList<ArrayList<String>> dimensionAttributes = new ArrayList<ArrayList<String>>();
		
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			
			String sqlQuery = "SELECT * from " + databaseName + ".meta;";
			stmt = conn.prepareStatement(sqlQuery);
			
			resultset = stmt.executeQuery();
			
			while (resultset.next())
			{
				if (resultset.getString("Element_Type").equals("Attribute")
						&& resultset.getString("Belongs_To_Table").equals(dimensionName.toLowerCase() + "_table"))
				{
					ArrayList<String> listOfAttributeParameters = new ArrayList<String>();
					listOfAttributeParameters.add(resultset.getString("Element_Name"));
					listOfAttributeParameters.add(resultset.getString("Element_Datatype"));
					listOfAttributeParameters.add(resultset.getString("Element_Hierarchy"));
					
					dimensionAttributes.add(listOfAttributeParameters);
					
					// LOGG //$$System.out.println("Dimensional attribute found for "+dimensionName);
				}
			}
		}
		
		catch (SQLException se) // Handle errors for JDBC
		{
			se.printStackTrace();
		}
		catch (Exception e) // Handle errors for Class.forName
		{
			e.printStackTrace();
		}
		finally
		// finally block used to close resources
		{
			try
			{
				if (stmt != null)
					conn.close();
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		// %%//$$System.out.println("Exiting getDimensionNames() from meta base");
		return dimensionAttributes;
	}
	
	// /
	
	// ////
	/**
	 * 
	 * @return HashMap <Column_name, ArrayList of column values> from columns_ui table
	 * @throws SQLException
	 */
	
	public static HashMap<String, ArrayList<String>> getColumnsUI() throws SQLException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultset = null;
		HashMap<String, ArrayList<String>> allValues = new HashMap<String, ArrayList<String>>();
		
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			
			String sqlQuery = "SELECT * from logdatabase.columns_ui;";
			stmt = conn.prepareStatement(sqlQuery);
			
			resultset = stmt.executeQuery();
			
			ArrayList<String> columnNames = new ArrayList<String>();
			ArrayList<String> aliasNames = new ArrayList<String>();
			ArrayList<String> dataTypes = new ArrayList<String>();
			
			while (resultset.next())
			{
				columnNames.add(resultset.getString("column_name"));
				aliasNames.add(resultset.getString("ui_alias_name"));
				dataTypes.add(resultset.getString("data_type"));
				
			}
			
			allValues.put("column_name", columnNames);
			allValues.put("ui_alias_name", aliasNames);
			allValues.put("data_type", dataTypes);
			// first ArrayList of allValues is an ArrayList of values in the first column of resultset
			// allValues.put("column_name",(ArrayList)Arrays.asList(resultset.getArray("column_name"))) ; // get the values of first column
			// allValues.put("ui_alias_name",(ArrayList)Arrays.asList(resultset.getArray("ui_alias_name"))) ; // get the values of second column
			// allValues.put("data_type",(ArrayList)Arrays.asList(resultset.getArray("data_type"))) ; // get the values of third column
		}
		
		catch (SQLException se) // Handle errors for JDBC
		{
			se.printStackTrace();
		}
		catch (Exception e) // Handle errors for Class.forName
		{
			e.printStackTrace();
		}
		finally
		// finally block used to close resources
		{
			try
			{
				if (stmt != null)
					conn.close();
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		// $$System.out.println("Exiting getColumnsUI() from database");
		return allValues;
	}
	
	/**
	 * Returns a ResultSet object of a given query output formed from given selectString, fromTableString and whereString.
	 * 
	 * @param selectString
	 * @param fromTableString
	 * @param whereString
	 * @return
	 * @throws SQLException
	 */
	public static ResultSet getSQLResultSet(String selectString, String fromTableString, String whereString) throws SQLException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultset = null;
		// ArrayList<String> resultStrings=new ArrayList<String>();
		
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			
			if (selectString.trim().length() == 0)
			{
				selectString = "*";
			}
			
			String sqlQuery = "SELECT " + selectString + " from " + databaseName + "." + fromTableString;
			
			// System.out.println("Query="+sqlQuery);
			
			if (whereString.trim().length() > 0)
			{
				sqlQuery = sqlQuery + " where " + whereString;
			}
			
			// sqlQuery+= ";";
			
			stmt = conn.prepareStatement(sqlQuery);
			resultset = stmt.executeQuery();
			
		}
		
		catch (SQLException se) // Handle errors for JDBC
		{
			se.printStackTrace();
		}
		catch (Exception e) // Handle errors for Class.forName
		{
			e.printStackTrace();
		}
		finally
		// finally block used to close resources
		{
			try
			{
				if (stmt != null)
					conn.close();
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		// //$$System.out.println("Exiting getSQLResultSet() from database");
		
		// ResultSetMetaData rsmd = resultset.getMetaData();
		// int columnCount = rsmd.getColumnCount();
		//
		// // The column count starts from 1
		// for (int i = 1; i < columnCount + 1; i++ )
		// {
		// String name = rsmd.getColumnName(i);
		// System.out.println("- "+name);
		// // Do stuff with name
		// }
		
		return resultset;
	}
	
	public static ArrayList<String> getSQLStringResultSingleColumn(String query) throws SQLException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultset = null;
		ArrayList<String> resultStrings = new ArrayList<String>();
		
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				// //$$System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// //$$System.out.println("Connected database successfully...");
			}
			String sqlQuery = query;
			stmt = conn.prepareStatement(sqlQuery);
			resultset = stmt.executeQuery();
			while (resultset.next())
			{
				resultStrings.add(resultset.getString(1).trim());
			}
		}
		
		catch (SQLException se) // Handle errors for JDBC
		{
			se.printStackTrace();
		}
		catch (Exception e) // Handle errors for Class.forName
		{
			e.printStackTrace();
		}
		finally
		// finally block used to close resources
		{
			try
			{
				if (stmt != null)
					conn.close();
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
		// $$System.out.println("Exiting getSQLStringResult() from database");
		return resultStrings;
	}
	
	/**
	 * 
	 * @param databaseName
	 */
	public static void truncateAllData(String databaseName)
	{
		ArrayList<String> truncateStatement = new ArrayList<String>();
		
		truncateStatement.add("set SQL_SAFE_UPDATES=0;");
		truncateStatement.add("truncate table " + databaseName + ".activity_dimension_table;");
		truncateStatement.add("truncate table " + databaseName + ".activity_fact_table;");
		truncateStatement.add("truncate table " + databaseName + ".date_dimension_table;");
		truncateStatement.add("truncate table " + databaseName + ".location_dimension_table;");
		truncateStatement.add("truncate table " + databaseName + ".time_dimension_table;");
		truncateStatement.add("truncate table " + databaseName + ".user_dimension_table;");
		truncateStatement.add("truncate table " + databaseName + ".activity_fact_table;");
		truncateStatement.add("set SQL_SAFE_UPDATES=1;");
		
		System.out.println("Truncating  " + databaseName + " database by executing " + truncateStatement);
		
		try
		{
			executeUpdates(truncateStatement);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		System.out.println("------------Truncation done--------------------");
	}
	
	/**
	 * Returns a ResultSet object of a given query output formed from given selectString, fromTableString and whereString.
	 * 
	 * @param selectString
	 * @param fromTableString
	 * @param whereString
	 * @return
	 * @throws SQLException
	 */
	public static void executeUpdates(ArrayList<String> updateStatements) throws SQLException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultset = null;
		
		try
		{
			if (USEPOOLED)
			{
				conn = connectionPool.getConnection();
			}
			else
			{
				Class.forName(JDBC_DRIVER);
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
			}
			
			for (String s : updateStatements)
			{
				stmt = conn.prepareStatement(s);
				int numOfLinesChanged = stmt.executeUpdate();
				System.out.println(numOfLinesChanged + " lines updated.");
			}
		}
		
		catch (SQLException se) // Handle errors for JDBC
		{
			se.printStackTrace();
		}
		catch (Exception e) // Handle errors for Class.forName
		{
			e.printStackTrace();
		}
		finally
		// finally block used to close resources
		{
			try
			{
				if (stmt != null)
					conn.close();
			}
			catch (SQLException se)
			{
			}
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (SQLException se)
			{
				se.printStackTrace();
			}
		}
	}
	
	/*
	 * public static ArrayList<String> getUserNames() throws SQLException { Connection conn = null; PreparedStatement stmt = null; ResultSet resultset = null; ArrayList<String>
	 * userNames=new ArrayList<String>();
	 * 
	 * try { Class.forName(JDBC_DRIVER); //$$System.out.println("Connecting to a selected database..."); conn = DriverManager.getConnection(DB_URL, USER, PASS);
	 * //$$System.out.println("Connected database successfully...");
	 * 
	 * String sqlQuery = "SELECT distinct(userId) from logdatabase.dummy;";//activitylog;"; stmt = conn.prepareStatement(sqlQuery);
	 * 
	 * resultset = stmt.executeQuery();
	 * 
	 * while(resultset.next()) { userNames.add(resultset.getString(1).trim()); } }
	 * 
	 * catch(SQLException se) //Handle errors for JDBC { se.printStackTrace(); } catch(Exception e) //Handle errors for Class.forName { e.printStackTrace(); } finally //finally
	 * block used to close resources { try { if(stmt!=null) conn.close(); } catch(SQLException se) {} try { if(conn!=null) conn.close(); } catch(SQLException se) {
	 * se.printStackTrace(); } } //$$System.out.println("Exiting getUserName() from database"); return userNames; }
	 */
	
	/*
	 * public static String getLocationName(int userId) { String locationName="";
	 * 
	 * switch(userId) { case 0: locationName= "act1"; break; case 1: locationName= "UCD Office"; break; case 2: locationName= "Park"; break; case 3: locationName= "Road"; break;
	 * case 4: locationName= "Pub"; break; case 5: locationName= "Market"; break; case 6: locationName= "Gym/exercise"; break; case 7: locationName= "Office other"; break; case 8:
	 * locationName= "Unidentified"; break; default: locationName= "Defaut location"; break; } return locationName; }
	 */
	
	// public static int getBias
	
	/*
	 * Older one: before May 13, 2014 public static void generateUserDataInDatabase()//static void main(String[] args) { Connection conn = null; PreparedStatement stmt = null; try
	 * { Class.forName(JDBC_DRIVER); //$$System.out.println("Connecting to a selected database..."); conn = DriverManager.getConnection(DB_URL, USER, PASS);
	 * //$$System.out.println("Connected database successfully..."); String sqlInsertion = "INSERT INTO LogDatabase.ActivityLogs5" + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"; stmt
	 * = conn.prepareStatement(sqlInsertion); for(int userId=0;userId<=5;userId++) // 6 Users { String userName=getUserName(userId); for(int day=1;day<=30;day++)// 30 days { int
	 * numberOfActivities=randomInRange(3,5);// a user performs 3 to 5 activities per day. Min + (int)(Math.random() * ((Max - Min) + 1)) for(int
	 * activityIterator=0;activityIterator<=numberOfActivities;activityIterator++) { int activityId=randomInRange(0,14); //15 Activities String
	 * activityName=getActivityName(activityId);
	 * 
	 * int locationId=randomInRange(0,8); // 8 different locations String locationName=getLocationName(locationId);
	 * 
	 * int startHour=randomInRange(0,16); int startMinute=randomInRange(0,59);
	 * 
	 * int endHour=startHour+randomInRange(0,7); int endMinute=randomInRange(0,59);
	 * 
	 * Timestamp startTimestamp=new Timestamp(114,2,day,startHour,startMinute,0,0);//Timestamp(int year, int month, int date, int hour, int minute, int second, int nano) Time
	 * startTime=new Time(startHour,startMinute,0);
	 * 
	 * Timestamp endTimestamp=new Timestamp(114,2,day,endHour,endMinute,0,0); Time endTime=new Time(endHour,endMinute,0);
	 * 
	 * String startDay=getSQLStringResultSingleColumn("SELECT DAYNAME('"+startTimestamp+"');").get(0); String
	 * endDay=getSQLStringResultSingleColumn("SELECT DAYNAME('"+endTimestamp+"');").get(0);
	 * 
	 * Date startDate=new Date(114,2,day); Date endDate=new Date(114,2,day);
	 * 
	 * //$$System.out.println("userId= "+userId+" activityId="+activityId+" startTimestamp="+startTimestamp.toString()+ " endTimestamp="+endTimestamp.toString()+
	 * " location id="+locationId+" start day="+startDay+" end day="+endDay+" start time="+startTime+" end time="+endTime);
	 * 
	 * stmt.setString(1,userName); stmt.setString(2,activityName); stmt.setTimestamp(3,startTimestamp); stmt.setTimestamp(4,endTimestamp); stmt.setString(5,locationName);
	 * stmt.setString(6,startDay); stmt.setString(7,endDay); stmt.setTime(8,startTime); stmt.setTime(9,endTime); stmt.setDate(10,startDate); stmt.setDate(11,endDate);
	 * stmt.setInt(12,1); stmt.setInt(13,userId); stmt.setInt(14,activityId); stmt.setInt(15,locationId);
	 * 
	 * stmt.executeUpdate();
	 * 
	 * //$$System.out.println("Inserted record into the table..."); } } } } catch(SQLException se) //Handle errors for JDBC { se.printStackTrace(); } catch(Exception e) //Handle
	 * errors for Class.forName { e.printStackTrace(); } finally //finally block used to close resources { try { if(stmt!=null) conn.close(); } catch(SQLException se) {} try {
	 * if(conn!=null) conn.close(); } catch(SQLException se) { se.printStackTrace(); } } //$$System.out.println("Exiting generateUserDataInDatabase() in database"); }
	 */
	
}