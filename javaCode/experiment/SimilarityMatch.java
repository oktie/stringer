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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import dbdriver.MySqlDB;
import evaluation.AccuracyMeasure;

import simfunctions.BM25;
import simfunctions.CondBM25;
import simfunctions.CondHMM;
import simfunctions.CondIdf;
import simfunctions.DistinctBM25;
import simfunctions.DistinctHMM;
import simfunctions.DistinctTfIdf;
import simfunctions.HMM;
import simfunctions.Idf;
import simfunctions.ModBM25;
import simfunctions.ModHMM;
import simfunctions.Preprocess;
import simfunctions.TfIdf;
import utility.Config;

public class SimilarityMatch {

	public static int queryTokenLength = 10;

	public static boolean readQueriesFromFile = true;
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
			Random generator = new Random();
			int k = 0;
			try {
				String query = "SELECT DISTINCT " + config.preprocessingColumn + ", id FROM " + config.dbName + "."
						+ tableName + " where tid in (select max(tid) as tid from " + config.dbName + "." + tableName
						+ " group by id) " + " ORDER BY RAND()" + " LIMIT " + maxQueries;
				System.err.println("Executing " + query);
				ResultSet rs = mysqlDB.executeQuery(query);
				if (rs != null) {
					while (rs.next()) {
						String searchString = rs.getString(config.preprocessingColumn);
						k = rs.getInt("id");
						
						// 
						// TODO: CHECK (OKTIE: '' should be replaced????)
						// searchString = searchString.replace("'", "''");
						//
						
						String newQueryString = searchString;
						if (!Config.tokenizeUsingQgrams ) {
							newQueryString = "";
							String[] tokenArray = searchString.toLowerCase().split("\\s+");

							HashMap<String, Double> tokenTF = new HashMap<String, Double>();
							for (int i = 0; i < tokenArray.length; i++) {
								if (tokenTF.containsKey(tokenArray[i])) {
									tokenTF.put(tokenArray[i], tokenTF.get(tokenArray[i]) + 1);
								} else {
									tokenTF.put(tokenArray[i], 1.0);
								}
							}

							HashSet<String> tokenSet = new HashSet<String>();
							int repeatingTokenTFSum = 0;
							for (String myToken : tokenTF.keySet()) {
								if (tokenTF.get(myToken) > 1) {
									tokenSet.add(myToken);
									repeatingTokenTFSum += tokenTF.get(myToken);
								}
							}
							tokenArray = tokenSet.toArray(tokenArray);
							int tokenArrayLength = tokenSet.size();
							int randomTokenArrayLength = Math.min(queryTokenLength, tokenArrayLength);
							String randomTokens[] = new String[randomTokenArrayLength];
							// The code below choose the random tokens
							for (int i = 0; i < randomTokenArrayLength; i++) {
								randomTokens[i] = tokenArray[i];
							}
							for (int i = randomTokenArrayLength; i < tokenArrayLength; i++) {
								int randomPosition = generator.nextInt(i);
								if (randomPosition < randomTokenArrayLength) {
									randomTokens[randomPosition] = tokenArray[i];
								}
							}

							newQueryString = randomTokens[0];
							for (int i = 1; i < randomTokenArrayLength; i++) {
								newQueryString += " " + randomTokens[i];
							}

							/*
							 * Now lets insert the tokens to be repeated
							 */
							for (int i = 0; i < randomTokenArrayLength; i++) {
								double mytf = tokenTF.get(randomTokens[i]);
								for (int j = 0; j < mytf; j++) {
									if (generator.nextInt(repeatingTokenTFSum) < mytf) {
										newQueryString += " " + randomTokens[i];
									}
								}
							}
						}
						searchString = newQueryString;
						queries.put(k, searchString);
					}
				}
				mysqlDB.close();

			} catch (Exception e) {
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
					System.out.println("id: "+tokens[0]+"  query: "+tokens[1]);
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
			System.err.println("Executing " + query);
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

	public static int[] generateBooleanList(HashSet<Integer> actualResult, List<IdScore> scoreList) {
		int[] booleanList = new int[scoreList.size()];
		for (int i = 0; i < scoreList.size(); i++) {
			if (actualResult.contains(scoreList.get(i).id + 1)) {
				booleanList[i] = 1;
			} else {
				booleanList[i] = 0;
			}
		}
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
			query = "drop table if exists " + config.dbName + "." + Config.expResultTablePrefix + tableName + queryTokenLength;
			mysqlDB.executeUpdate(query);

			query = "create table " + config.dbName + "." + Config.expResultTablePrefix + tableName + queryTokenLength
					+ " (qid int, query text, id int, metric varchar(255), map double)";
			mysqlDB.executeUpdate(query);

			System.err.println("Executing " + query);
			for (int a = 0; a < mapArray.length; a++) {
				for (int b = 0; b < preprocessVector.size(); b++) {
					query = "insert into " + config.dbName + "." + Config.expResultTablePrefix + tableName + queryTokenLength + " values ("
							+ (a + 1) + ",'" + queryVector.get(a) + "'," + queryIdVector.get(a) + ",'"
							+ preprocessVector.get(b).getClass().getName() + "', " + mapArray[a][b] + ")";
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
		// TfIdf tfidf = new TfIdf();
		Preprocess metric = null;
		// Vector<Preprocess> preprocessVector = new Vector<Preprocess>();
		Vector<Preprocess> preprocessVector = new Vector<Preprocess>();
		Vector<Vector<HashMap<String, Double>>> basetableTokenWeightsVector = new Vector<Vector<HashMap<String, Double>>>();
		Vector<HashMap<String, Double>> qgramIDFVector = new Vector<HashMap<String, Double>>();
		double[][] mapArray;
		int maxQueries = 100;
		if (args.length > 2) {
			queryTokenLength = Integer.parseInt(args[2]);
			System.err.println("Setting queryTokenLength to " + queryTokenLength);
		}

		Preprocess tfidf = new TfIdf();
		Preprocess idf = new Idf();
		Preprocess condidf = new CondIdf();
		Preprocess disttfidf = new DistinctTfIdf();
		
		Preprocess bm25 = new BM25();
		Preprocess modbm25 = new ModBM25();
		Preprocess condbm25 = new CondBM25();
		Preprocess distbm25 = new DistinctBM25();
		
		Preprocess hmm = new HMM();
		Preprocess modhmm = new ModHMM();
		Preprocess condhmm = new CondHMM();
		Preprocess disthmm = new DistinctHMM();
		
		preprocessVector.add(tfidf);
		preprocessVector.add(idf);
		preprocessVector.add(condidf);
		preprocessVector.add(disttfidf);
		
		preprocessVector.add(bm25);
		preprocessVector.add(modbm25);
		preprocessVector.add(condbm25);
		preprocessVector.add(distbm25);
		
		preprocessVector.add(hmm);
		preprocessVector.add(modhmm);
		preprocessVector.add(condhmm);
		preprocessVector.add(disthmm);

		long t1, t4, t2, t3, prepTime = 0, queryTime = 0;
		Vector<String> files = new Vector<String>();
		for (int i = 0; i < preprocessVector.size(); i++) {
			files = new Vector<String>();
			Vector<HashMap<String, Double>> baseTableTokenWeights = new Vector<HashMap<String, Double>>();
			HashMap<String, Double> qgramIDF = new HashMap<String, Double>();

			t1 = System.currentTimeMillis();

			Preprocess.setLogToDB(false);
			// Toggle this variable to log the preprocessing info to DB
			/*if( preprocessVector.get(i).getClass().getName().equals(distbm25.getClass().getName()) )
				Preprocess.setLogToDB(true);
			if( preprocessVector.get(i).getClass().getName().equals(disthmm.getClass().getName()) )
				Preprocess.setLogToDB(true);
			*/
			
			preprocessVector.get(i).preprocessTable(files, qgramIDF, baseTableTokenWeights, args[0]);
			
			t2 = System.currentTimeMillis();
			prepTime = t2 - t1;
			System.err.println(preprocessVector.get(i).getClass().getName()+"  Preprocessing Done: took " + prepTime + " ms");
			basetableTokenWeightsVector.insertElementAt(baseTableTokenWeights, i);
			qgramIDFVector.insertElementAt(qgramIDF, i);
			//// System.gc();
		}
		
		// For each query, order the filenames by their jaccard score
		int i = 1;
		String query;
		t1 = System.currentTimeMillis();
		if(readQueriesFromFile){
			queryFileName = Config.queryFileName + args[1]+args[2]+ ".txt";
		}
		HashMap<Integer, String> queries = getRandomQueries(maxQueries, args[1]);
		// HashMap<Integer, String> queries = testQueries();
		t2 = System.currentTimeMillis();
		System.err.println("Collected Random Queries: took " + (t2 - t1) + " ms");

		/*
		 * If due to some reason we cant get maxQueries qeuries then readjust
		 * this number
		 */
		maxQueries = queries.size();

		mapArray = new double[maxQueries][preprocessVector.size()];
		for (int a = 0; a < maxQueries; a++) {
			for (int b = 0; b < preprocessVector.size(); b++) {
				mapArray[a][b] = 0;
			}
		}

		Vector<String> queryVector = new Vector<String>();
		Vector<Integer> queryIdVector = new Vector<Integer>();

		String baseDir = Config.storeResultDirectory + args[0] + "_" + queryTokenLength + "/";
		File baseDirPath = new File(baseDir);
		baseDirPath.mkdirs();

		for (Integer queryId : queries.keySet()) {
			query = queries.get(queryId);
			HashSet<Integer> actualResult = getAllTidsHavingId(queryId, args[1]);
			queryVector.add(query);
			queryIdVector.add(queryId);
			for (int j = 0; j < preprocessVector.size(); j++) {
				metric = preprocessVector.get(j);
				Vector<HashMap<String, Double>> baseTableTokenWeights = basetableTokenWeightsVector.get(j);
				HashMap<String, Double> qgramIDF = qgramIDFVector.get(j);
				t3 = System.currentTimeMillis();
				// The tids in the scoreList is 1 less than the actual tid e.g.
				// tid=1 will be having id=0 in IdScore object
				List<IdScore> scoreList = metric.getSimilarRecords(query, qgramIDF, baseTableTokenWeights, files);
				logOutput(baseDir + i + "." + Config.queryClass +"."+ metric.getClass().getName() + ".txt", query, files, scoreList);
				t4 = System.currentTimeMillis();
				queryTime += (t4 - t3);
				// Compute Accuracy
				int[] booleanList = generateBooleanList(actualResult, scoreList);
				double map = AccuracyMeasure.meanAveragePrecision(booleanList, actualResult.size());
				mapArray[i - 1][j] = map;
				// System.err.println("Query Time: " + (t4-t1));
				System.err.println("query: " + i + "  metric: " + metric.getClass().getName() + "      MAP: " + map);
			}
			System.err.println("DONE QUERY: " + i);
			i++;
		}

		Preprocess.setLogToDB(true);
		if (Preprocess.logToDB()) {
			logMapArrayToDB(args[0], mapArray, preprocessVector, queryVector, queryIdVector);
		}
		double[] meanMAP = logMapArray(baseDir + "map.txt", preprocessVector, mapArray);

		for (int a = 0; a < preprocessVector.size(); a++) {
			System.err.println(preprocessVector.get(a).getClass().getName() + " - meanMAP: " + meanMAP[a]);
		}

		System.err.println("\nQuery Time: " + queryTime + " ms     Preprocess Time : " + prepTime + " ms ");
	}

}
