/*******************************************************************************
 * Copyright (c) 2006-2007 University of Toronto Database Group
 *     
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
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

