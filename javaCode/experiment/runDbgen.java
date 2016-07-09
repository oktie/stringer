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