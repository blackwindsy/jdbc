
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class JDBCExec {

	private static String jdbcDriver = null;
	private static String jdbcHost = null;
	private static String jdbcPort = null;
	private static String jdbcUrl = null;

	private static String dbUser;
	private static String dbPassword;

	private static String database = null;
	private static String dbid = null; // SID or Service Name for oracle, Database Name for SQL Server
	private static String dbinst = null; // Database Instance Name for SQL Server

	public static void main(String args[]) {

		Connection conn = null;
		String query = null;
		Properties connProps = new Properties();
		String propFileName = "jdbc.properties";
		// InputStream is = null;
		FileInputStream is = null;

		try {
			// is =
			// JDBCExec.class.getClass().getClassLoader().getResourceAsStream(propFileName);
			is = new FileInputStream(propFileName);
			if (is != null) {
				connProps.load(is);
			} else {
				System.out.println("Configuration file not found: filename = " + propFileName);
				return;
			}
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		database = connProps.getProperty("dbms");
		jdbcUrl = connProps.getProperty("jdbc.url");
		dbUser = connProps.getProperty("jdbc.user");
		dbPassword = connProps.getProperty("jdbc.password");
		query = connProps.getProperty("query");

		if (database == null || database.length() <= 0 || jdbcUrl == null || jdbcUrl.length() <= 0 || dbUser == null
				|| dbUser.length() <= 0 || dbPassword == null || dbPassword.length() <= 0) {
			System.out.println("Required configuration is missing.");
			System.out.println("\t database = " + database);
			System.out.println("\t jdbcUrl  = " + jdbcUrl);
			System.out.println("\t dbUser   = " + dbUser);
			System.out.println("\t dbPassword (hidden) = ");
			return;
		}

		loadJdbcDriver(database);

		// --------------------------------
		// JDBC execution
		// --------------------------------
		try {
			conn = getConnection();
			if (query != null && query.length() > 0) {
				selectTable(conn, query);
			}
			conn.close();
		} catch (SQLException se) {
			System.out.println("Connection Failed!!! JDBC URL = " + jdbcUrl);
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					System.out.println("Exception while closing connection finally as cleanup effort.");
					e.printStackTrace();
					;
				}
			}
		}
	}

	public static void loadJdbcDriver(String dbms) {

		// --------------------------------
		// JDBC Driver setting
		// --------------------------------
		if (dbms.equals("oracle")) {
			jdbcDriver = "oracle.jdbc.OracleDriver";
		} else if (dbms.equals("sqlserver")) {
			jdbcDriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		} else if (dbms.equals("mysql")) {
			jdbcDriver = "com.mysql.jdbc.Driver";
		}

		try {
			Class.forName(jdbcDriver);
			System.out.println("JDBC driver registered: " + jdbcDriver);
		} catch (ClassNotFoundException cnfe) {
			System.out.println("JDBC Driver is not found.");
			cnfe.printStackTrace();
		}
	}

	public static String generateJdbcUrl(String dbms, String dbHost, String dbPort, String dbId) {

		StringBuffer sbJdbcUrl = new StringBuffer(100);

		// --------------------------------
		// JDBC URL setting
		// --------------------------------
		if (dbms.equals("oracle")) {
			// e.g. jdbc:oracle:thin:@dbhost:1521/dbsid
			sbJdbcUrl.append("jdbc:oracle:thin:@").append(dbHost);
			if (dbPort != null && dbPort.trim().length() > 0) {
				sbJdbcUrl.append(":").append(dbPort);
			}
			if (dbId.startsWith(":") || dbId.startsWith("/")) {
				sbJdbcUrl.append(dbId); // SID starts with ":" Service Name starts with "/"
			} else {
				sbJdbcUrl.append(":").append(dbId); // SID assumed
			}
		} else if (dbms.equals("sqlserver")) {
			// e.g. jdbc:sqlserver://dbhost\dbinstance:1433;databaseName=dbname
			sbJdbcUrl.append("jdbc:sqlserver://").append(dbHost);
			if (dbinst != null && dbinst.trim().length() > 0) {
				sbJdbcUrl.append("\\").append(dbinst);
			}
			if (dbPort != null && dbPort.trim().length() > 0) {
				sbJdbcUrl.append(":").append(dbPort);
			}
			if (dbId != null && dbId.trim().length() > 0) {
				sbJdbcUrl.append(";databaseName=").append(dbId);
			}
		} else if (dbms.equals("mysql")) {
			// e.g. jdbc:mysql://dbhost:3306/dbname
			sbJdbcUrl.append("jdbc:mysql://").append(dbHost);
			if (dbPort != null && dbPort.trim().length() > 0) {
				sbJdbcUrl.append(":").append(dbPort);
			}
			if (dbId != null && dbId.trim().length() > 0) {
				sbJdbcUrl.append("/").append(dbId);
			}
		}

		return sbJdbcUrl.toString();
	}

	public static Connection getConnection() {
		Connection conn = null;

		// --------------------------------
		// JDBC Connection
		// --------------------------------
		try {
			System.out.println("Connecting to database");
			conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
			System.out.println("Connected to database");
		} catch (SQLException se) {
			System.out.println("Connection Failed!!! JDBC URL = " + jdbcUrl);
			se.printStackTrace();
		} catch (Exception e) {
			System.out.println("Connection Failed!!! JDBC URL = " + jdbcUrl);
			e.printStackTrace();
			;
		}

		return conn;
	}

	public static void selectTable(Connection conn, String query) throws SQLException {

		Statement stmt = null;
		try {
			System.out.println("\n=============================================");
			System.out.println("Running query: " + query);
			System.out.println("\n---------------------------------------------");

			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			ResultSetMetaData rsmd = rs.getMetaData();
			int recCount = 0;
			StringBuffer sbResult = new StringBuffer(1000);

			while (rs.next()) {
				recCount++;
				String tableName = rsmd.getTableName(1);
				String columnName;
				String columnValue;
				sbResult.append("\n\t ").append(tableName).append(" table record #").append(recCount)
						.append(" --------------");
				// column index in metadata starts from 1
				for (int m = 1; m <= rsmd.getColumnCount(); m++) {
					columnName = rsmd.getColumnName(m);
					columnValue = rs.getString(m);
					sbResult.append("\n\t\t ").append(columnName).append(" = ").append(columnValue);
				}
			}
			System.out.println("\n=============================================");
			System.out.println("\n RESULT");
			System.out.println("\n---------------------------------------------");
			System.out.println(sbResult);

		} catch (SQLException e) {
			System.out.println("SQL execution failed: query = " + query);
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

}
