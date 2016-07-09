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
package swmdb;


import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Set;
import java.util.Random;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.StringTokenizer;

import dbdriver.MySqlDB;
import evaluation.AccuracyMeasure;
import simfunctions.RunProbabilityAssignment;
import utility.Config;
import utility.Util;

public class test2 {
	
	public static void main(String[] args) {
		
		String DBname = "swmdb";
		
		String tablename = "rawdata_film";
		String col1 = "id";
		String col2 = "starring";
		String outputtable = "film" + "2" + "performance";
		
		Config config = new Config();
		config.setDbName(DBname);
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		
		String inputdbtable = "`" + config.dbName + "`.`" + tablename + "`";
		String outputdbtable = "`" + config.dbName + "`.`" + outputtable + "`";
		
		try{
			String sql = "DROP TABLE IF EXISTS " + outputdbtable;
			
			mysqlDB.executeUpdate(sql);
			
			sql = " CREATE TABLE  " + outputdbtable + "  (" +
					"  `" + col1 + "` varchar(100) NOT NULL," +
					"  `" + col2 + "` varchar(100) NOT NULL," +
					"   KEY `" + col1 + "` (`" + col1 + "`), " +
					"   KEY `" + col2 + "` (`" + col2 + "`), " +
					"   PRIMARY KEY (`" + col1 + "`,`" + col2 + "`) " +
					") ";
			
			System.out.println(sql);
			mysqlDB.executeUpdate(sql);
			
			sql = " SELECT `" + col1 + "`, `" + col2 + "` FROM " + inputdbtable + " r";
			
			ResultSet rs;
			
			HashMap<String,String> strs = new HashMap<String,String>();
			
			System.out.println(sql);
			rs = mysqlDB.executeQuery(sql);
			
			StringBuffer valsquery = new StringBuffer(" INSERT INTO " + outputdbtable + " VALUES ");
			
			int numberOfValues = 0;
			while (rs.next()){
				
				String val1 = rs.getString(1);
				String val2 = rs.getString(2);
				//strs.put(val1, val2);
			    StringTokenizer st = new StringTokenizer(val2, ",");
			    if (val1.charAt(0)!='/') System.err.print("BAD ID: " + val1 );			    
			    //System.out.print(val1 + ": ");
			    String str;			    
			    if (val2.trim()!="") {
			    	while (st.hasMoreTokens()) {
			    		str = st.nextToken();
			    		if (str.charAt(0)=='/') valsquery.append( "( '" + val1 + "','" + str + "' )," );
			    		numberOfValues ++;
			    		if (str.charAt(0)!='/') System.err.print("BAD ID: " + str); 
			    		//System.err.print(str + " ");
			    	}
			    }
			    else {
			    	valsquery.append( "( '" + val1 + "'," + "null" + " )," );
			    	numberOfValues++;
			    }
			    if ((numberOfValues >= 100)) {
			    	//System.out.println(valsquery);
					mysqlDB.executeUpdate(valsquery.deleteCharAt(valsquery.length()-1).toString());
					valsquery = new StringBuffer(" INSERT INTO " + outputdbtable + " VALUES ");
					numberOfValues = 0;
				}
			    //System.out.println();
			}
			
			//System.out.println(valsquery.deleteCharAt(valsquery.length()-1).toString());
			if ((numberOfValues != 0)) mysqlDB.executeUpdate(valsquery.deleteCharAt(valsquery.length()-1).toString());
			
		} catch(Exception e){
			System.err.println("Error: " + e.toString());
		}
		
		System.out.println();
		System.out.println();
		System.out.println();

		

	}

}