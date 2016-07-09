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

public class test {
	
	public static void create_relationship_table(String DBname, String inputtable, String col1, String col2, String outputtable) {
		Config config = new Config();
		config.setDbName(DBname);
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		
		String inputdbtable = "`" + config.dbName + "`.`" + inputtable + "`";
		String outputdbtable = "`" + config.dbName + "`.`" + outputtable + "`";
		
		try{
			String sql = "DROP TABLE IF EXISTS " + outputdbtable;
			
			mysqlDB.executeUpdate(sql);
			
			sql = " CREATE TABLE  " + outputdbtable + "  (" +
					"  `" + col1 + "` varchar(100) NOT NULL," +
					"  `" + col2 + "` varchar(200) NOT NULL," +
					"   KEY `" + col1 + "` (`" + col1 + "`), " +
					"   KEY `" + col2 + "` (`" + col2 + "`), " +
					"   PRIMARY KEY (`" + col1 + "`,`" + col2 + "`) " +
					") ";
			
			System.out.println(sql);
			mysqlDB.executeUpdate(sql);
			
			sql = " SELECT `" + col1 + "`, `" + col2 + "` FROM " + inputdbtable + " r";
			
			ResultSet rs;
			
			System.out.println(sql);
			rs = mysqlDB.executeQuery(sql);
			
			StringBuffer valsquery = new StringBuffer(" INSERT INTO " + outputdbtable + " VALUES ");
			
			int numberOfValues = 0;
			while (rs.next()){
				
				String val1 = rs.getString(1);
				String val2 = rs.getString(2);
			    StringTokenizer st = new StringTokenizer(val2, ",");
			    if (val1.charAt(0)!='/') System.err.print("BAD ID: " + val1 );			    
			    //System.out.print(val1 + ": ");
			    Vector<String> strs = new Vector<String>();
			    if (!val2.trim().equals("")) {
				    String[] result = val2.split(",");
				    String str;
				    int x = 0;				    
				    while (x<result.length){
			    		str = result[x];
			    		int dig = (int) str.charAt(str.length()-1);
					    if ( dig >= 48 && dig <=57 )
			    		if (((x+1)<result.length)){		
			    			dig = (int) result[x+1].charAt(0);			    			
			    			while ( dig >= 48 && dig <=57 ) {
			    				str = str + "," + result[x+1];
			    				x++;
			    				if ((x>0) && ((x+1)<result.length)) dig = (int) result[x+1].charAt(0);
			    				else dig=0;
			    				//System.out.println(str);
			    			}
			    		}
			    		//System.out.println(str);
			    		//if (str.charAt(0)=='/')			    		
			    		if (!strs.contains(str)){
			    			valsquery.append( "( '" + val1 + "','" + str.replace("'", "''") + "' )," );
			    			numberOfValues ++;	
				    		strs.add(str);
			    		}

			    		//if (str.charAt(0)!='/') System.err.println("BAD ID: " + str);
			    		x++;
				    }
			    }
			    //else {
			    	//valsquery.append( "( '" + val1 + "'," + "null" + " )," );
			    	//numberOfValues++;
			    //}
			    if ((numberOfValues >= 10)) {
			    	//System.out.println(valsquery);
					mysqlDB.executeUpdate(valsquery.deleteCharAt(valsquery.length()-1).toString());
					valsquery = new StringBuffer(" REPLACE INTO " + outputdbtable + " VALUES ");
					numberOfValues = 0;
				}
			    //System.out.println();
			}
			
			//System.out.println(valsquery.deleteCharAt(valsquery.length()-1).toString());
			if ((numberOfValues != 0)) mysqlDB.executeUpdate(valsquery.deleteCharAt(valsquery.length()-1).toString());
			
		} catch(Exception e){
			System.err.println("Error: " + e.toString());
		}		
	}
	
	public static void create_relationship_table_id2id(String DBname, String inputtable, String col1, String col2, String outputtable) {
		Config config = new Config();
		config.setDbName(DBname);
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		
		String inputdbtable = "`" + config.dbName + "`.`" + inputtable + "`";
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
			
			System.out.println(sql);
			rs = mysqlDB.executeQuery(sql);
			
			StringBuffer valsquery = new StringBuffer(" INSERT INTO " + outputdbtable + " VALUES ");
			
			int numberOfValues = 0;
			while (rs.next()){
				
				String val1 = rs.getString(1);
				String val2 = rs.getString(2);
			    StringTokenizer st = new StringTokenizer(val2, ",");
			    if (val1.charAt(0)!='/') System.err.print("BAD ID: " + val1 );			    
			    //System.out.print(val1 + ": ");
			    String str;			    
			    if (val2.trim()!="") {
			    	while (st.hasMoreTokens()) {
			    		str = st.nextToken();
			    		if (str.charAt(0)=='/') valsquery.append( "( '" + val1 + "','" + str + "' )," );
			    		numberOfValues ++;
			    		if (str.charAt(0)!='/') System.err.println("BAD ID: " + str); 
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
	}

	
	public static void read_relationship_table_from_file(String DBname, String outputtable, String inputfile) {
		
		Config config = new Config();
		config.setDbName(DBname);
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		String sql;
		
		try{
			sql = "DROP TABLE IF EXISTS `swmdb`.`tmp`";
			mysqlDB.executeUpdate(sql);

			sql = "CREATE TABLE  `swmdb`.`tmp` (" +
				  "	  `id1` varchar(200) NOT NULL," +
				  "	  `pred` varchar(200) NOT NULL," +
				  "	  `id2` varchar(200) NOT NULL" +
				  "	)";
			mysqlDB.executeUpdate(sql);
			
			sql = "DROP TABLE IF EXISTS `swmdb`.`"+ outputtable +"`";
			mysqlDB.executeUpdate(sql);

			sql = "CREATE TABLE  `swmdb`.`"+ outputtable +"` (" +
				  "  `id1` varchar(200) NOT NULL," +
				  "  `id2` varchar(200) NOT NULL," +
				  "	  PRIMARY KEY  (`id1`,`id2`)," +
				  "	  KEY `id1` (`id1`)," +
				  "	  KEY `id2` (`id2`)" +
				  "	)";
			mysqlDB.executeUpdate(sql);

			sql = "LOAD DATA LOCAL INFILE '"+ inputfile + "'" +
				  " INTO TABLE `swmdb`.`tmp`" +
				  "	FIELDS TERMINATED BY '\t'" +
				  " LINES TERMINATED BY '\n'";
			mysqlDB.executeUpdate(sql);
			
			sql = "INSERT INTO `swmdb`.`"+ outputtable +"`" +
				  "	SELECT id1, id2" +
				  "	FROM `swmdb`.`tmp`";
			mysqlDB.executeUpdate(sql);
		} catch(Exception e) {
			System.err.println("Error: " + e.toString());
		}
		
		
	}
	
	public static void main(String[] args) {
		
		String DBname = "swmdb";		
		
		//create_relationship_table(DBname, "rawdata_actor", "id", "film", "actor2performance");
		
		//create_relationship_table(DBname, "rawdata_film", "id", "starring", "film2performance");
		
		//create_relationship_table(DBname, "rawdata_film", "id", "sequel", "film2sequel");
		
		//create_relationship_table(DBname, "rawdata_film", "id", "country", "film2country");
		
		//create_relationship_table(DBname, "rawdata_film", "id", "language", "film2language");
		
		/*
		 * film2cinematography.tsv
		 * film2country.tsv
		 * film2director.tsv
		 * film2genre.tsv
		 * film2language.tsv
		 * film2music.tsv
		 * film2producer.tsv
		 * film2sequel.tsv
		 * film2writer.tsv
		 */
		//read_relationship_table_from_file(DBname, "film2director" , "X:/xml project/datasets/freebase/fetch/film2director.tsv");
		//read_relationship_table_from_file(DBname, "film2sequel" , "X:/xml project/datasets/freebase/fetch/film2sequel.tsv");
		//read_relationship_table_from_file(DBname, "film2producer" , "X:/xml project/datasets/freebase/fetch/film2producer.tsv");
		//read_relationship_table_from_file(DBname, "film2cinematography" , "X:/xml project/datasets/freebase/fetch/film2cinematography.tsv");
		//read_relationship_table_from_file(DBname, "film2country" , "X:/xml project/datasets/freebase/fetch/film2country.tsv");
		//read_relationship_table_from_file(DBname, "film2genre" , "X:/xml project/datasets/freebase/fetch/film2genre.tsv");
		//read_relationship_table_from_file(DBname, "film2language" , "X:/xml project/datasets/freebase/fetch/film2language.tsv");
		//read_relationship_table_from_file(DBname, "film2music" , "X:/xml project/datasets/freebase/fetch/film2music.tsv");
		//read_relationship_table_from_file(DBname, "film2writer" , "X:/xml project/datasets/freebase/fetch/film2writer.tsv");
		
		//read_relationship_table_from_file(DBname, "film2collections" , "X:/xml project/datasets/freebase/fetch/film2collections.tsv");
		//read_relationship_table_from_file(DBname, "film2editor" , "X:/xml project/datasets/freebase/fetch/film2editor.tsv");
		//read_relationship_table_from_file(DBname, "film2imdbid" , "X:/xml project/datasets/freebase/fetch/film2imdbid.tsv");
		//read_relationship_table_from_file(DBname, "film2story_contributor" , "X:/xml project/datasets/freebase/fetch/film2story-contributor.tsv");
		//read_relationship_table_from_file(DBname, "film2subjects" , "X:/xml project/datasets/freebase/fetch/film2subjects.tsv");
		//read_relationship_table_from_file(DBname, "film2locations" , "X:/xml project/datasets/freebase/fetch/film2locations.tsv");
		//read_relationship_table_from_file(DBname, "film2costume" , "X:/xml project/datasets/freebase/fetch/film2costume.tsv");
		//read_relationship_table_from_file(DBname, "film2country" , "X:/xml project/datasets/freebase/fetch/film2country.tsv");
		//read_relationship_table_from_file(DBname, "film2film_format" , "X:/xml project/datasets/freebase/fetch/film2film_format.tsv");
		//read_relationship_table_from_file(DBname, "film_series2film" , "X:/xml project/datasets/freebase/fetch/film_series2film.tsv");
		//read_relationship_table_from_file(DBname, "film2rating" , "X:/xml project/datasets/freebase/fetch/film2rating.tsv");
		//read_relationship_table_from_file(DBname, "film2soundtrack" , "X:/xml project/datasets/freebase/fetch/film2soundtrack.tsv");
		//read_relationship_table_from_file(DBname, "film2trailers" , "X:/xml project/datasets/freebase/fetch/film2trailers.tsv");
		//read_relationship_table_from_file(DBname, "film_cut2film" , "X:/xml project/datasets/freebase/fetch/film2film_cut.tsv");
		
		//read_relationship_table_from_file(DBname, "film2" , "X:/xml project/datasets/freebase/fetch/film2.tsv");
		

		
	}

}