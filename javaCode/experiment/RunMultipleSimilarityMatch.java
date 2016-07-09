package experiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
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
import evaluation.AccuracyMeasure;

import simfunctions.Preprocess;
import simfunctions.SoftTfIdf;
import simfunctions.WeightedJaccardBM25;
import utility.Config;
import utility.Util;

public class RunMultipleSimilarityMatch {

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

	public static HashMap<Integer, String> getRandomQueries(int maxQueries, String tableName) {
		HashMap<Integer, String> queries = new HashMap<Integer, String>();
		Config config = new Config();
		if (!readQueriesFromFile) {
			MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
			int k = 0;
			String createView = "", dropView = "", query = "";
			try {
				/*
				 * String query = "SELECT DISTINCT " +
				 * config.preprocessingColumn + ", id FROM " + config.dbName +
				 * "." + tableName + " where tid in (select max(tid) as tid from " +
				 * config.dbName + "." + tableName + " group by id) " + " ORDER
				 * BY RAND()" + " LIMIT " + maxQueries;
				 */
				
				
			
				
				createView = "create view maxtid as select max(tid) as mtid from " + config.dbName + "." + tableName
						+ " group by id;";
				dropView = "drop view maxtid;";

				mysqlDB.executeUpdate(createView);

				query = "SELECT DISTINCT " + config.preprocessingColumn + ", id FROM " + config.dbName + "."
						+ tableName + " as r, maxtid as s where r.tid=s.mtid order by RAND() limit " + maxQueries + ";";
				
				query = "SELECT string, id from " + " (SELECT id, string FROM " + config.dbName + "." + tableName
						+ "	order by tid desc) A" + // *** desc 
						" group by id "  +
						// + //" ORDER BY RAND()" +
					    " ORDER BY id" + " LIMIT " + maxQueries;

						System.out.println("Executing " + query);
				ResultSet rs = mysqlDB.executeQuery(query);
				if (rs != null) {
					while (rs.next()) {
						String searchString = rs.getString(config.preprocessingColumn);
						k = rs.getInt("id");
						// TODO: CHECK (OKTIE: '' should be replaced????)
						//searchString = searchString.replace("'", "''");
						//
						queries.put(k, searchString);
					}
				}
				mysqlDB.executeUpdate(dropView);
				mysqlDB.close();
			} catch (Exception e) {
				System.err.println(createView + "\n" + query + "\n" + dropView);
				System.err.println("Can't generate Random Queries");
				e.printStackTrace();
			}
		} else {

			try {
				System.out.println(queryFileName);
				BufferedReader in = new BufferedReader(new FileReader(queryFileName));
				String str;
				int k = 0;
				while ((str = in.readLine()) != null) {
					String[] tokens = str.split("\t");
					System.out.println("id: " + tokens[0] + "  query: " + tokens[1]);
					k = Integer.parseInt(tokens[0]);
					queries.put(k, tokens[1]);
				}
				in.close();
			} catch (IOException e) {
			}
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

	public static void main(String[] args) {

		Preprocess metric = null;
		Vector<Preprocess> preprocessVector = new Vector<Preprocess>(); // List of metrics for preprocessing the base relation
		
		Vector<Vector<HashMap<String, Double>>> basetableTokenWeightsVector = new Vector<Vector<HashMap<String, Double>>>();
		Vector<HashMap<String, Double>> qgramIDFVector = new Vector<HashMap<String, Double>>();
		double[][] mapArray;
		double[][] minMapArray;
		double[][] maxMapArray;
		
		int maxQueries = 1;
		
		String tablename = "cu1";
		
		/*
		if (args.length > 2) {
			queryTokenLength = Integer.parseInt(args[2]);
			System.out.println("Setting queryTokenLength to " + queryTokenLength);
		}
		*/

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
		
		/*
		 *  LM is missing from here
		 */
				
		Preprocess softtfidf = new SoftTfIdf();
		preprocessVector.add(softtfidf);
		
		Preprocess bm25WeightedJaccard = new WeightedJaccardBM25();
		preprocessVector.add(bm25WeightedJaccard);
		
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
			// System.gc();
		}
		
		System.out.println("Preprocessing complete!\n");

		/*
		 * GENERATE QEURIES
		 */
		// For each query, order the filenames by their jaccard score
		int i = 1;
		String query;
		t1 = System.currentTimeMillis();
		
		/* ooo
		if (readQueriesFromFile) {
			queryFileName = Config.queryFileName + args[1] + args[2] + ".txt";
		}
		
		
		HashMap<Integer, String> queries = getRandomQueries(maxQueries, args[1]);
		*/
		HashMap<Integer, String> queries = getRandomQueries(maxQueries, tablename);
		
		
		// HashMap<Integer, String> queries = testQueries();
		t2 = System.currentTimeMillis();
		System.out.println("Collected Random Queries: took " + (t2 - t1) + " ms \n");

		/*
		 * adding one more query
		 */
		 //queries.put(422, "New York University");

		/*
		 * If due to some reason we cant get maxQueries qeuries then readjust
		 * this number
		 */
		maxQueries = queries.size();

		mapArray = new double[maxQueries][preprocessVector.size()];
		
		minMapArray = new double[maxQueries][preprocessVector.size()];
		maxMapArray = new double[maxQueries][preprocessVector.size()];
		
		for (int a = 0; a < maxQueries; a++) {
			for (int b = 0; b < preprocessVector.size(); b++) {
				mapArray[a][b] = 0;
				maxMapArray[a][b] = 0;
				minMapArray[a][b] = 0;
			}
		}

		/*
		 * FIND THE SIMILAR RECORD FOR EACH QUERY AND EVALUATE THE METRICS
		 */
		Vector<String> queryVector = new Vector<String>();
		Vector<Integer> queryIdVector = new Vector<Integer>();

		//String baseDir = Config.storeResultDirectory + args[0] + "_" + queryTokenLength + "/";
		String baseDir = Config.storeResultDirectory + tablename + "_" + queryTokenLength + "/";
		
		File baseDirPath = new File(baseDir);
		baseDirPath.mkdirs();

		double meanMaxMap=0, meanMinMap=0;
		for (Integer queryId : queries.keySet()) {
			query = queries.get(queryId);
			//HashSet<Integer> actualResult = getAllTidsHavingId(queryId, args[1]);
			HashSet<Integer> actualResult = getAllTidsHavingId(queryId, tablename);
			queryVector.add(query);
			queryIdVector.add(queryId);
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
				// Compute Accuracy
				int[] booleanList = generateBooleanList(actualResult, scoreList, 1);
				double map1 = AccuracyMeasure.meanAveragePrecision(booleanList, actualResult.size());
				booleanList = generateBooleanList(actualResult, scoreList, 0);
				double map2 = AccuracyMeasure.meanAveragePrecision(booleanList, actualResult.size());
				double map = (map1 + map2) / 2;
				meanMaxMap += map2;
				meanMinMap += map1;
				mapArray[i - 1][j] = map;
				minMapArray[i-1][j] = map1;
				maxMapArray[i-1][j] = map2;
				// System.out.println("Query Time: " + (t4-t1));
				System.out.println("query: " + i + "  metric: " + metric.getClass().getName() + "      MAP: " + map
						+ "   map1: " + map1 + "   map2: " + map2);
			}
			System.out.println("DONE QUERY: " + i);
			i++;
		}

		Preprocess.setLogToDB(true);
		if (Preprocess.logToDB()) {
			//logMapArrayToDB(args[0], mapArray, preprocessVector, queryVector, queryIdVector);
			logMapArrayToDB(tablename, mapArray, preprocessVector, queryVector, queryIdVector);
		}
		double[] meanMAP1 = logMapArray(baseDir + "map.txt", preprocessVector, minMapArray);
		double[] meanMAP2 = logMapArray(baseDir + "map.txt", preprocessVector, maxMapArray);
		double[] meanMAP = logMapArray(baseDir + "map.txt", preprocessVector, mapArray);

		for (int a = 0; a < preprocessVector.size(); a++) {
			System.out.println(preprocessVector.get(a).getClass().getName() + " - meanMAP: " + meanMAP[a] + " - minMeanMAP: " + meanMAP1[a] + " - maxMeanMAP: " + meanMAP2[a]);
		}

		System.out.println("\nQuery Time: " + queryTime + " ms     Preprocess Time : " + prepTime + " ms ");
	}

}