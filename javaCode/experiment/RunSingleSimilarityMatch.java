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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import dbdriver.MySqlDB;
import evaluation.AccuracyMeasure;

import simfunctions.ApproximateGES;
import simfunctions.BM25;
import simfunctions.EditDistance;
import simfunctions.GeneralizedEditSimilarity;
import simfunctions.HMM;
import simfunctions.Intersect;
import simfunctions.Jaccard;
import simfunctions.Preprocess;
import simfunctions.SoftTfIdf;
import simfunctions.TfIdf;
import simfunctions.WeightedIntersect;
import simfunctions.WeightedIntersectBM25;
import simfunctions.WeightedJaccard;
import simfunctions.WeightedJaccardBM25;
import utility.Config;
import utility.Util;

public class RunSingleSimilarityMatch {

	public static int queryTokenLength = 2;

	public static String getQuery(int tid, String tableName) {
		String resultQuery = "";
		String query = "";
		Config config = new Config();

		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		try {
		
			query = "SELECT " + config.preprocessingColumn + ", id FROM " + config.dbName + "."
					+ tableName + " T WHERE T.tid = " + tid;
			
			//System.out.println("Executing " + query);
			ResultSet rs = mysqlDB.executeQuery(query);
			rs.next();
			resultQuery = rs.getString(config.preprocessingColumn);
			mysqlDB.close();
		} catch (Exception e) {
			System.err.println("Can't generate the query");
			e.printStackTrace();
		}

		return resultQuery;
	}

	public static HashSet<Integer> getAllTidsHavingIdSameAs(int tid, String tableName) {
		HashSet<Integer> tidsHavingThisID = new HashSet<Integer>();
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		try {
			String query = "SELECT tid FROM " + config.dbName + "." + tableName + " where id=" + 
			               "(SELECT id FROM " + config.dbName + "." + tableName + " t where t.tid= " + tid +")";
			//System.out.println("Executing " + query);
			ResultSet rs = mysqlDB.executeQuery(query);
			if (rs != null) {
				while (rs.next()) {
					tidsHavingThisID.add(rs.getInt("tid"));
				}
			}
			mysqlDB.close();
		} catch (Exception e) {
			System.err.println("Can't run query");
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

	public static void logMapArrayToDB(String tableName, double[] mapArray, Vector<Preprocess> preprocessVector,
			String queryString) {
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		String query = "";
		try {
			query = "drop table if exists " + config.dbName + "." + Config.expResultTablePrefix + tableName
					+ queryTokenLength;
			mysqlDB.executeUpdate(query);

			query = "create table " + config.dbName + "." + Config.expResultTablePrefix + tableName + queryTokenLength
					+ " (query text, metric varchar(255), map double)";
			mysqlDB.executeUpdate(query);

			System.out.println("Executing " + query);
			for (int b = 0; b < preprocessVector.size(); b++) {
				query = "insert into " + config.dbName + "." + Config.expResultTablePrefix + tableName
						+ queryTokenLength + " values ('" + queryString + "','" + preprocessVector.get(b).getClass().getName() + "', "
						+ mapArray[b] + ")";
				mysqlDB.executeUpdate(query);
			}
				// mysqlDB.executeUpdate(query);
			mysqlDB.close();
		} catch (Exception e) {
			System.err.println("Can't execute Query: " + query);
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		Preprocess metric = null;
		Vector<Preprocess> preprocessVector = new Vector<Preprocess>(); // List of metrics for preprocessing the base relation
		
		Vector<Vector<HashMap<String, Double>>> basetableTokenWeightsVector = new Vector<Vector<HashMap<String, Double>>>();
		Vector<HashMap<String, Double>> qgramIDFVector = new Vector<HashMap<String, Double>>();
		double[] mapArray;
		double[] minMapArray;
		double[] maxMapArray;
		
		//int maxQueries = 1;
		
		String tablename = "cu1";
		
		/*
		if (args.length > 2) {
			queryTokenLength = Integer.parseInt(args[2]);
			System.out.println("Setting queryTokenLength to " + queryTokenLength);
		}
		*/

		
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
		
		
		preprocessVector.add(ed);
		
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
		
		/*
		 *  LM is missing from here
		 */
				
		//Preprocess softtfidf = new SoftTfIdf();
		//preprocessVector.add(softtfidf);
		
		//Preprocess bm25WeightedJaccard = new WeightedJaccardBM25();
		//preprocessVector.add(bm25WeightedJaccard);
		
		/*
		 * PREPROCESSING PHASE
		 */
		long t1, t4, t2, t3, prepTime = 0, queryTime = 0;
		Vector<String> files = new Vector<String>();
		//for (int i = 0; i < preprocessVector.size(); i++) {
		for (Preprocess preprocess: preprocessVector) {
			// Preprocess.tokenizeUsingQgrams = true;
			files.clear();
			files = new Vector<String>();
			Vector<HashMap<String, Double>> baseTableTokenWeights = new Vector<HashMap<String, Double>>();
			HashMap<String, Double> qgramIDF = new HashMap<String, Double>();

			t1 = System.currentTimeMillis();

			// Toggle this variable to log the preprocessing info to DB
			Preprocess.setLogToDB(false);

			
			/*
			 * if(
			 * preprocessVector.get(i).getClass().getName().equals(distbm25.getClass().getName()) )
			 * Preprocess.setLogToDB(true); if(
			 * preprocessVector.get(i).getClass().getName().equals(disthmm.getClass().getName()) )
			 * Preprocess.setLogToDB(true);
			 */
			
			//preprocessVector.get(i).preprocessTable(files, qgramIDF, baseTableTokenWeights, args[0]);
			preprocess.preprocessTable(files, qgramIDF, baseTableTokenWeights, tablename);

			t2 = System.currentTimeMillis();
			prepTime = t2 - t1;
			System.out.println(preprocess.getClass().getName() + "  Preprocessing Done: took " + prepTime
					+ " ms");
			//basetableTokenWeightsVector.insertElementAt(baseTableTokenWeights, i);
			basetableTokenWeightsVector.addElement(baseTableTokenWeights);
			qgramIDFVector.addElement(qgramIDF);
			//// System.gc();
		}
		
		System.out.println("Preprocessing complete!\n");

		/*
		 * GENERATE THE QUERY
		 */
		// For the query, order the filenames by their jaccard score

		String query;
		t1 = System.currentTimeMillis();

		int tid = 1;
		query = getQuery(tid, tablename);
		
		
		// HashMap<Integer, String> queries = testQueries();
		t2 = System.currentTimeMillis();
		System.out.println("Query tid " + tid + " : " + query);

		/*
		 * adding one more query
		 */
		 //queries.put(422, "New York University");

		/*
		 * If due to some reason we cant get maxQueries qeuries then readjust
		 * this number
		 */
		//maxQueries = queries.size();

		mapArray = new double[preprocessVector.size()];
		
		minMapArray = new double[preprocessVector.size()];
		maxMapArray = new double[preprocessVector.size()];
		
		for (int b = 0; b < preprocessVector.size(); b++) {
			mapArray[b] = 0;
			maxMapArray[b] = 0;
			minMapArray[b] = 0;
		}

		/*
		 * FIND THE SIMILAR RECORD FOR THE QUERY AND EVALUATE THE METRICS
		 */

		String baseDir = Config.storeResultDirectory + tablename + "_" + queryTokenLength + "/";
		
		File baseDirPath = new File(baseDir);
		baseDirPath.mkdirs();

		double meanMaxMap=0, meanMinMap=0;

		HashSet<Integer> actualResult = getAllTidsHavingIdSameAs(tid, tablename);
		for (int j = 0; j < preprocessVector.size(); j++) {
			// Preprocess.tokenizeUsingQgrams = true;
			metric = preprocessVector.get(j);
			Vector<HashMap<String, Double>> baseTableTokenWeights = basetableTokenWeightsVector.get(j);
			HashMap<String, Double> qgramIDF = qgramIDFVector.get(j);
			t3 = System.currentTimeMillis();
			// The tids in the scoreList is 1 less than the actual tid e.g.
			// tid=1 will be having id=0 in IdScore object
			List<IdScore> scoreList = metric.getSimilarRecords(query.toLowerCase(), qgramIDF,
					baseTableTokenWeights, files);

			/*
			 * logOutput(baseDir + i + "." + Config.queryClass + "." +
			 * metric.getClass().getName() + ".txt", query, files,
			 * scoreList);
			 */
			t4 = System.currentTimeMillis();
			queryTime += (t4 - t3);

			
			
			/* 
			 * 
			 * Write Similarity Match Score Results to database 
			 * 
			 */
			boolean log_score_table_to_db = false;
			if (log_score_table_to_db) {
				Config config = new Config();
				MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
				//String query = "";
				try {
					query = "drop table if exists " + config.dbName + ".results";
					mysqlDB.executeUpdate(query);
	
					query = "create table " + config.dbName + ".results "
							+ " (tid int, score double)";
					mysqlDB.executeUpdate(query);
	
					System.err.println("Executing " + query);
					int k = 0;
					query = "insert into " + config.dbName + ".results " + " values";
					for (IdScore listElement: scoreList){
						k++;
						query += "( " + listElement.id + ", "  + listElement.score + " )";					
						if (k == 40) break;
						query += ",";
						
					}
					mysqlDB.executeUpdate(query);
					mysqlDB.close();
				} catch (Exception e) {
					System.err.println("Can't execute Query: " + query);
					e.printStackTrace();
				}			
				/*
				int k=0;
				for (IdScore listElement: scoreList){
					k++;
					System.err.println(listElement.toString());
					if (k == 40) break;
				}
				*/
			}
			
			
			
			
			
			
			
			// Compute Accuracy
			int[] booleanList = generateBooleanList(actualResult, scoreList, 1);
			double map1 = AccuracyMeasure.meanAveragePrecision(booleanList, actualResult.size());
			booleanList = generateBooleanList(actualResult, scoreList, 0);
			double map2 = AccuracyMeasure.meanAveragePrecision(booleanList, actualResult.size());
			double map = (map1 + map2) / 2;
			meanMaxMap += map2;
			meanMinMap += map1;
			mapArray[j] = map;
			minMapArray[j] = map1;
			maxMapArray[j] = map2;
			// System.out.println("Query Time: " + (t4-t1));
			System.out.println("Similarity Function: " + metric.getClass().getName() + "      MAP: " + map
					+ "   map1: " + map1 + "   map2: " + map2);
		}
		System.out.println("Query Complete!");

		
		Preprocess.setLogToDB(false);
		if (Preprocess.logToDB()) {
			//logMapArrayToDB(args[0], mapArray, preprocessVector, queryVector, queryIdVector);
			logMapArrayToDB(tablename, mapArray, preprocessVector, query);
		}

		System.out.println("\nQuery Time: " + queryTime + " ms     Preprocess Time : " + prepTime + " ms ");
	}

}