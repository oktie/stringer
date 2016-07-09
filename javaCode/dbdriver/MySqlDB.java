package dbdriver;

import java.sql.*;
//import com.mysql.jdbc.Driver;

public class MySqlDB
{
	//String url, usr, passwd;
	Connection con=null;
	Statement st=null;

	public MySqlDB(String url, String user, String passwd) {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch (Exception E) {
			System.err.println("Unable to load driver.");
			E.printStackTrace();
			System.exit(1);
		}
		try {
			Connection con2 = DriverManager.getConnection(url, user, passwd);
			//System.out.println(url+" "+user+" "+passwd);
			con = con2;
		}
		catch (SQLException E) {
			System.out.println("SQLException: " + E.getMessage());
			System.out.println("SQLState:     " + E.getSQLState());
			System.out.println("VendorError:  " + E.getErrorCode());
			E.printStackTrace();
			con=null;
			System.exit(1);
		}



	}

	public ResultSet executeQuery(String query) throws java.lang.Exception {
		return con.createStatement().executeQuery(query);
	}

	public DatabaseMetaData getMetaData() throws java.lang.Exception {
		return con.getMetaData();
	}

	public int executeUpdate(String query) throws java.lang.Exception {
		return con.createStatement().executeUpdate(query);
	}

	public PreparedStatement prepareStatement(String query)
		throws java.lang.Exception {
			return con.prepareStatement(query);
		}

	public void close() throws Exception {
		if(con != null)
			con.close();
	}
} 

