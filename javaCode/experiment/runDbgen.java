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
package experiment;


import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.Set;
import java.util.Random;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import dbdriver.MySqlDB;
import evaluation.AccuracyMeasure;
import utility.Config;
import utility.Util;

public class runDbgen {
	
	public static void main(String[] args) {
		
		
		if (args.length < 1) {
			System.err.println("runDbgen:: no arguments! \n Usage: runDbgen dbname.datasets_definitions_table");
		} else {
			
			try {
				Process p = Runtime.getRuntime().exec("python");
				System.out.println(p.getOutputStream().toString());
				//Runtime.getRuntime().exec("export GENERATOR_DIR=.");
			}
			catch (Exception e) {
				System.err.println(" Unable to run the datagenerator ");
			}
			
			
			Config config = new Config();
			MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
			String datasetsTable = args[0];
			try {
				//String query = "DROP TABLE IF EXISTS " + config.dbName + "." + resultTable;
				//mysqlDB.executeUpdate(query);

				String query = "SELECT * FROM " + datasetsTable;
				System.out.println(query);
				ResultSet rs = mysqlDB.executeQuery(query);
				
				while (rs.next()) {
					System.out.println(rs.getString(1));
					
					//$GENERATOR_DIR/dbgen -n 5000 -c 500 -s 53421 -i -q -e 90 -u 30 -x 24 -d 24 -r 24 -v 14 -w 14 -o ~/data/cname-datasets/titles-u_90_30_2424241414.txt
					
				}
				
				mysqlDB.close();
			
			} catch (Exception e) {
				System.err.println("DB Error");
				e.printStackTrace();
			} 
			
		}
	
		
		
		
		

	}

}