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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import dbdriver.MySqlDB;
import simfunctions.Preprocess;
import simfunctions.WeightedJaccardBM25;
import utility.Config;
import utility.Util;

public class SimilarityJoin {

	public static int queryTokenLength = 2;

	public static boolean readQueriesFromFile = false;

	public static String queryFileName = null;

	public static void logOutput(String filename, String query, Vector records, List<IdScore> scoreList) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			for (IdScore tuple : scoreList) {
				// print query also
				out.write(query + "\t\t" + records.get(tuple.id) + "\t\t" + tuple.id + "\t\t" + tuple.score + "\n");
			}
			out.close();
		} catch (IOException e) {
			System.err.println("Error while writing to  " + filename);
		}
	}

	public static HashMap<Integer, String> testQueries() {
		HashMap<Integer, String> queries = new HashMap<Integer, String>();
		queries.put(2960, "Grandpaw Granny there out beat lived keep never moved one");
		queries.put(2262, "Observer seen sophisticated like Helena cogs most world Sunday computer");
		return queries;
	}

	public static Vector<String> getAllRecords(String tableName) {
		Vector<String> queries = new Vector<String>();
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		//int k = 0;
		String createView = "", dropView = "", query = "";
		try {
			query = "SELECT " + config.preprocessingColumn + " FROM " + config.dbName + "." + tableName;
			//System.out.println("Executing " + query);
			ResultSet rs = mysqlDB.executeQuery(query);
			if (rs != null) {
				while (rs.next()) {
					String searchString = rs.getString(config.preprocessingColumn);
					//k = rs.getInt("id");
					
					//
					// TODO: CHECK (OKTIE: '' should be replaced????)
					//searchString = searchString.replace("'", "''");
					//
					
					queries.add(searchString);
				}
			}
			mysqlDB.close();
		} catch (Exception e) {
			System.err.println(createView + "\n" + query + "\n" + dropView);
			System.err.println("Can't fetch all records");
			e.printStackTrace();
		}
		return queries;
	}

	public static HashSet<Integer> getAllTidsHavingId(int id, String tableName) {
		HashSet<Integer> tidsHavingThisID = new HashSet<Integer>();
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		try {
			String query = "SELECT tid FROM " + config.dbName + "." + tableName + " where id=" + id;
			System.out.println("Executing " + query);
			ResultSet rs = mysqlDB.executeQuery(query);
			if (rs != null) {
				while (rs.next()) {
					tidsHavingThisID.add(rs.getInt("tid"));
				}
			}
			mysqlDB.close();
		} catch (Exception e) {
			System.err.println("Can't generate Random Queries");
			e.printStackTrace();
		}
		return tidsHavingThisID;
	}

	// The sortOrder defines the ordering for the tuples having similar scores
	public static int[] generateBooleanList(HashSet<Integer> actualResult, List<IdScore> scoreList, int sortOrder) {
		int[] booleanList = new int[scoreList.size()];
		int booleanListCounter = 0;
		double oldScore = 0, newScore = 0;
		ArrayList<Integer> tempBooleanList = new ArrayList<Integer>();

		// For the first element
		newScore = scoreList.get(0).score;
		oldScore = scoreList.get(0).score;
		if (actualResult.contains(scoreList.get(0).id + 1)) {
			tempBooleanList.add(1);
			Util.printlnDebug("Got match at position: "+1);
		} else {
			tempBooleanList.add(0);
		}

		for (int i = 1; i < scoreList.size(); i++) {
			newScore = scoreList.get(i).score;
			if (newScore != oldScore) {
				// sort the old list and set the values in the actual
				// booleanList
				Collections.sort(tempBooleanList);
				if (sortOrder != 0) {
					Collections.reverse(tempBooleanList);
				}
				for (int k = 0; k < tempBooleanList.size(); k++) {
					booleanList[booleanListCounter++] = tempBooleanList.get(k);
				}
				
				tempBooleanList = new ArrayList<Integer>();
				oldScore = newScore;

				if (actualResult.contains(scoreList.get(i).id + 1)) {
					tempBooleanList.add(1);
					Util.printlnDebug("Got match at position: "+ (i+1));
				} else {
					tempBooleanList.add(0);
				}
			} else {
				if (actualResult.contains(scoreList.get(i).id + 1)) {
					tempBooleanList.add(1);
					Util.printlnDebug("Got match at position: "+ (i+1));
				} else {
					tempBooleanList.add(0);
				}
			}
		}
		Collections.sort(tempBooleanList);
		if (sortOrder != 0) {
			Collections.reverse(tempBooleanList);
		}
		for (int k = 0; k < tempBooleanList.size(); k++) {
			booleanList[booleanListCounter++] = tempBooleanList.get(k);
		}
		// For the last block of tempBooleanList
		return booleanList;
	}

	public static double[] logMapArray(String filename, Vector<Preprocess> preprocessVector, double[][] mapArray) {
		double[] meanMAP = new double[preprocessVector.size()];
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			for (int a = 0; a < preprocessVector.size(); a++) {
				out.write(preprocessVector.get(a).getClass().getName() + "\t\t");
				meanMAP[a] = 0;
			}
			out.write("\n");
			for (int a = 0; a < mapArray.length; a++) {
				for (int b = 0; b < preprocessVector.size(); b++) {
					out.write(mapArray[a][b] + "\t\t");
					meanMAP[b] += mapArray[a][b];
				}
				out.write("\n");
			}
			out.close();
		} catch (IOException e) {
			System.err.println("Error while writing to  " + filename);
		}
		if (mapArray.length > 0) {
			for (int a = 0; a < preprocessVector.size(); a++) {
				meanMAP[a] = meanMAP[a] / mapArray.length;
			}
		}
		return meanMAP;
	}

	public static void logMapArrayToDB(String tableName, double[][] mapArray, Vector<Preprocess> preprocessVector,
			Vector<String> queryVector, Vector<Integer> queryIdVector) {
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		String query = "";
		try {
			query = "drop table if exists " + config.dbName + "." + Config.expResultTablePrefix + tableName
					+ queryTokenLength;
			mysqlDB.executeUpdate(query);

			query = "create table " + config.dbName + "." + Config.expResultTablePrefix + tableName + queryTokenLength
					+ " (qid int, query text, id int, metric varchar(255), map double)";
			mysqlDB.executeUpdate(query);

			System.out.println("Executing " + query);
			for (int a = 0; a < mapArray.length; a++) {
				for (int b = 0; b < preprocessVector.size(); b++) {
					query = "insert into " + config.dbName + "." + Config.expResultTablePrefix + tableName
							+ queryTokenLength + " values (" + (a + 1) + ",'" + queryVector.get(a) + "',"
							+ queryIdVector.get(a) + ",'" + preprocessVector.get(b).getClass().getName() + "', "
							+ mapArray[a][b] + ")";
					mysqlDB.executeUpdate(query);
				}
				// mysqlDB.executeUpdate(query);
			}
			mysqlDB.close();
		} catch (Exception e) {
			System.err.println("Can't execute Query: " + query);
			e.printStackTrace();
		}
	}

	public void run(String querytablename, String basetablename, Preprocess measure, Double thr) {
		
		boolean log_scores_to_db = true;
		boolean debug_mode = true;
		
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		
		/*
		 * PREPROCESSING PHASE
		 */
		long t1, t4, t2, t3, prepTime = 0, queryTime = 0;
		Vector<String> records = new Vector<String>();
		records.clear();
		records = new Vector<String>();
		Vector<HashMap<String, Double>> baseTableTokenWeights = new Vector<HashMap<String, Double>>();
		HashMap<String, Double> qgramIDF = new HashMap<String, Double>();

		t1 = System.currentTimeMillis();

		// Toggle this variable to log the preprocessing info to DB
		Preprocess.setLogToDB(false);

		measure.preprocessTable(records, qgramIDF, baseTableTokenWeights, basetablename);

		t2 = System.currentTimeMillis();
		prepTime = t2 - t1;
		if (debug_mode) System.out.println(measure.getClass().getName() + "  Preprocessing Done: took " + prepTime
				+ " ms");
		//// System.gc();
		
		if (debug_mode) System.out.println("Preprocessing of base table \"" + basetablename + "\"complete!\n");
		

		/*
		 * GENERATE QEURIES (FETCH ALL RECORDS)
		 */
		
		// todo: oktie: fix
		//Vector<String> queries = getAllRecords("sec_company");
		Vector<String> queries = getAllRecords(querytablename);
		
		
		
		t3 = System.currentTimeMillis();
		if (debug_mode) System.out.println("All records fetched, took: " + (t3 - t2) + " ms \n");
		
		String scoreTable = "scores_" + querytablename + "_" + basetablename + "_" + Preprocess.extractMetricName(measure.getClass().getName());

		
		/*
		 *  LOGGING SCORES TO THE DATABASE
		 *  PART 1 - Create Table
		 */
		StringBuffer log_query = new StringBuffer("");
		int numberOfValues = 0;
		if (log_scores_to_db) {
			try {				
				String query = "drop table if exists " + config.dbName + "." + scoreTable;
				mysqlDB.executeUpdate(query);

				query = "create table " + config.dbName + "." + scoreTable 
						+ " (tid1 int, tid2 int, score double)";
				mysqlDB.executeUpdate(query);
				log_query.append("INSERT INTO " + config.dbName + "." + scoreTable + " values "); 
				
			} catch (Exception e) {
				System.err.println("Can't create score log tables");
				e.printStackTrace();
			}
			
		}
		
		int tid1 = 0;
		for (String queryString : queries) {
			tid1++;
			// The tids in the scoreList is 1 less than the actual tid e.g.
			// tid=1 will be having id=0 in IdScore object
			//System.out.println(" BOOGH ");
			List<IdScore> scoreList = measure.getSimilarRecordsTH(queryString.toLowerCase(), qgramIDF,
					baseTableTokenWeights, records, thr, tid1);
			
			/*
			 *  LOGGING SCORES TO THE DATABASE
			 *  Part 2 - Log score table to DB - INSERT value of the score in the score table
			 *           Actually adding the '(tid1, tid2, score),' to the end of the query string 
			 */
			if (log_scores_to_db) {
				for (IdScore idScore:scoreList){
					numberOfValues++;
					log_query.append(" (" + (tid1) + "," + (idScore.id+1) + "," + idScore.score + ") ,");
				}
			}
			/*
			 *  LOGGING SCORES TO THE DATABASE
			 *  Part 3 - Log score table to DB
			 *   INSERT value of the probability in the prob. table for every 10,000 values
			 */
			if ((log_scores_to_db)&&(numberOfValues >= 20000)) {
				try {			
					numberOfValues = 0;
					mysqlDB.executeUpdate(log_query.deleteCharAt(log_query.length()-1).toString());
					log_query = new StringBuffer("INSERT INTO " + config.dbName + "." + scoreTable + " values "); 
				} catch (Exception e) {
					System.err.println("Can't insert into score log tables");
					e.printStackTrace();
				}
			}
			if (((tid1-1) % 100 == 0) && debug_mode) System.out.println("FOR Query Table: \"" + querytablename + "\"," + Preprocess.extractMetricName(measure.getClass().getName())+ " DONE RECORD #" + tid1);
		}
		
		/*
		 *  LOGGING SCORES TO THE DATABASE
		 *  Part 4 - Log score table to DB
		 *   INSERT all remaining values of the scores in the score table.
		 */
		try {
			mysqlDB.executeUpdate(log_query.deleteCharAt(log_query.length()-1).toString());
		} catch (Exception e) {
			System.err.println("Can't execute Query: \n " + log_query.deleteCharAt(log_query.length()-1).toString());
			e.printStackTrace();
		}

		t4 = System.currentTimeMillis();
		queryTime = t4 - t3;
		if (debug_mode) System.out.println("\nRecords fetch time: " + (t3 - t2) + " ms");
		if (debug_mode) System.out.println("Query Time: " + queryTime + " ms     Preprocess Time : " + prepTime + " ms ");

	}

	
	public static void main(String[] args) {

		Vector<Preprocess> preprocessVector = new Vector<Preprocess>(); // List of metrics for preprocessing the base relation
		String tablename = "cu1";
		
		/*
		Preprocess tfidf = new TfIdf();
		Preprocess bm25 = new BM25();
		Preprocess hmm = new HMM();
		Preprocess ed = new EditDistance();
		Preprocess ges = new GeneralizedEditSimilarity();
		Preprocess softtfidf = new SoftTfIdf();
		Preprocess fms = new ApproximateGES();
		Preprocess weightedJaccard = new WeightedJaccard();
		Preprocess jaccard = new Jaccard();
		Preprocess weightedIntersect = new WeightedIntersect();
		Preprocess intersect = new Intersect();
		Preprocess bm25WeightedJaccard = new WeightedJaccardBM25();
		Preprocess bm25weightedIntersect = new WeightedIntersectBM25();
		*/
		
		/*
		preprocessVector.add(tfidf);
		preprocessVector.add(bm25); 
		preprocessVector.add(hmm);
		preprocessVector.add(ed);
		preprocessVector.add(ges);
		preprocessVector.add(softtfidf);
		preprocessVector.add(fms); 
		preprocessVector.add(weightedJaccard);
		preprocessVector.add(jaccard); 
		preprocessVector.add(weightedIntersect);
		preprocessVector.add(bm25weightedIntersect);
		preprocessVector.add(intersect);
		preprocessVector.add(bm25WeightedJaccard);
		*/
				
		//Preprocess softtfidf = new SoftTfIdf();
		//preprocessVector.add(softtfidf);
		
		Preprocess bm25WeightedJaccard = new WeightedJaccardBM25();
		preprocessVector.add(bm25WeightedJaccard);

		//long t1, t2, t3;

		for (Preprocess preprocess: preprocessVector) {
			//run(tablename, preprocess, 0.1);
		}
	}
}