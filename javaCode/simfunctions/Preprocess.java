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
package simfunctions;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import utility.Config;
import dbdriver.MySqlDB;
import experiment.IdScore;

public class Preprocess {

	public static boolean logToDB = false;
	
	public boolean tokenizeUsingQgrams = true;
	
	public int qgramSize = 2;
	
	public static boolean debugModeOn = true;
	
	public static boolean replaceSpaceWithSpecialCharacter = true; 
	
	public static void setLogToDB(boolean logToDB) {
		Preprocess.logToDB = logToDB;
	}

	public static boolean logToDB() {
		return logToDB;
	}
	
	public boolean tokenizeUsingQgrams(){
		return tokenizeUsingQgrams;
	}
	
	public static String generateSpecialCharacters(int qgSize){
		String str="";
		for(int i=0; i < qgSize-1; i++){
			str += "#";
		}
		//System.out.println(str);
		return str;
	}
	
	public static String extractMetricName(String fullMetricName){
		String str[] = fullMetricName.toLowerCase().split("\\.");
		return str[str.length-1];
	}
	
	public void incrementCount(String qgram, HashMap<String, Double> tokenCount) {
		if (tokenCount.containsKey(qgram)) {
			tokenCount.put(qgram, tokenCount.get(qgram) + 1);
		} else {
			tokenCount.put(qgram, 1.0);
		}
	}

	public void incrementDFCount(String qgram, HashMap<String, Double> tokenCount, double qgramTF) {
		incrementCount(qgram, tokenCount);
	}

	// Tokenization function
	public HashMap<String, Double> getTF(String str) {
		HashMap<String, Double> tokenTF = new HashMap<String, Double>();
		/*
		 * String[] tokens = str.split("\\s+"); for (int i = 0; i <
		 * tokens.length; i++) { incrementCount(tokens[i], tokenTF); }
		 */
		str = str.toLowerCase();
		if(replaceSpaceWithSpecialCharacter){
			str = str.replaceAll(" ", generateSpecialCharacters(qgramSize));
			str = generateSpecialCharacters(qgramSize) + str + generateSpecialCharacters(qgramSize); 
		}
		for (int i = 0; i <= str.length() - qgramSize; i++) {
			String qgram = str.substring(i, i + qgramSize);
			incrementCount(qgram, tokenTF);
		}
		return tokenTF;
	}


	// Tokenizer based on whitespace; every token is a word
	public HashMap<String, Double> gettokenTFSingle(String str) {
		HashMap<String, Double> tokenTF = new HashMap<String, Double>();
		if (!tokenizeUsingQgrams ) {
			String[] tokens = str.toLowerCase().split(" ");
			for (int i = 0; i < tokens.length; i++) {
				// incrementCount(tokens[i], tokenTF);
				tokenTF.put(tokens[i], 1.0);
			}
		} else {
			str = str.toLowerCase();
			if(replaceSpaceWithSpecialCharacter){
				str = str.replaceAll(" ", generateSpecialCharacters(qgramSize));
				str = generateSpecialCharacters(qgramSize) + str + generateSpecialCharacters(qgramSize); 
			}
			for (int i = 0; i <= str.length() - qgramSize; i++) {
				String qgram = str.substring(i, i + qgramSize);
				tokenTF.put(qgram, 1.0);
			}
		}
		return tokenTF;
	}

	

	// Tokenizer based on whitespace; every token is a word
	public HashMap<String, Double> getTFWsign(String str, Long signature) {
		HashMap<String, Double> tokenTF = new HashMap<String, Double>();
		if (!tokenizeUsingQgrams ) {
			String[] tokens = str.toLowerCase().split(" ");
			for (int i = 0; i < tokens.length; i++) {
				// incrementCount(tokens[i], tokenTF);
				tokenTF.put(tokens[i], 1.0);
			}
		} else {
			str = str.toLowerCase();
			if(replaceSpaceWithSpecialCharacter){
				str = str.replaceAll(" ", generateSpecialCharacters(qgramSize));
				str = generateSpecialCharacters(qgramSize) + str + generateSpecialCharacters(qgramSize); 
			}
			for (int i = 0; i <= str.length() - qgramSize; i++) {
				String qgram = str.substring(i, i + qgramSize);
				tokenTF.put(qgram, 1.0);
			}
		}
		return tokenTF;
	}
	
	
	
	// Tokenizer based on whitespace; evry token is a word
	public HashMap<String, Double> getTokenTFMultiple(String str) {
		HashMap<String, Double> tokenTF = new HashMap<String, Double>();
		if (! tokenizeUsingQgrams) {
			String[] tokens = str.toLowerCase().split(" ");
			for (int i = 0; i < tokens.length; i++) {
				incrementCount(tokens[i], tokenTF);
			}
		} else {
			str = str.toLowerCase();
			if(replaceSpaceWithSpecialCharacter){
				str = str.replaceAll(" ", generateSpecialCharacters(qgramSize));
				str = generateSpecialCharacters(qgramSize) + str + generateSpecialCharacters(qgramSize); 
			}
			for (int i = 0; i <= str.length() - qgramSize; i++) {
				String qgram = str.substring(i, i + qgramSize);
				incrementCount(qgram, tokenTF);
			}
		}
		return tokenTF;
	}

	// Tokenizer based on conditional occurance; evry token is a word
	public HashMap<String, Double> getConditionalTF(String str) {
		HashMap<String, Double> tokenTF = new HashMap<String, Double>();
		if (! tokenizeUsingQgrams ) {
			String[] tokens = str.toLowerCase().split(" ");
			for (int i = 0; i < tokens.length; i++) {
				// incrementCount(tokens[i], tokenTF);
				int j=1;
				String modToken = tokens[i]+"_"+j;
				while(tokenTF.containsKey(modToken)){
					j++;
					modToken = tokens[i]+"_"+j;
				}
				tokenTF.put(modToken, 1.0);
			}
		} else {
			str = str.toLowerCase();
			if(replaceSpaceWithSpecialCharacter){
				str = str.replaceAll(" ", generateSpecialCharacters(qgramSize));
				str = generateSpecialCharacters(qgramSize) + str + generateSpecialCharacters(qgramSize); 
			}
			for (int i = 0; i <= str.length() - qgramSize; i++) {
				String qgram = str.substring(i, i + qgramSize);
				int j=1;
				String modToken = qgram+"_"+j;
				while(tokenTF.containsKey(modToken)){
					j++;
					modToken = qgram+"_"+j;
				}
				tokenTF.put(modToken, 1.0);
			}
		}
		return tokenTF;
	}

	// This is an implementation for tfidf weights
	public HashMap<String, Double> getWeights(HashMap<String, Double> tokenTF, HashMap<String, Double> qgramIDF) {
		double sum = 0, tfidf;
		for (String qgram : tokenTF.keySet()) {
			if (qgramIDF.containsKey(qgram)) {
				// This is a slight variant of TfIdf
				// w(u,C) = log(1 + tf)*idf
            /*
               Lets change this formula to w(u,c) = tf*idf , tf is raw tf
            */
				//tfidf =(1 + Math.log( tokenTF.get(qgram)) ) * qgramIDF.get(qgram);
				tfidf =  tokenTF.get(qgram) * qgramIDF.get(qgram);
				sum += tfidf * tfidf;
				tokenTF.put(qgram, tfidf);
			}
		}
		sum = Math.sqrt(sum);
		for (String qgram : tokenTF.keySet()) {
			tokenTF.put(qgram, tokenTF.get(qgram) / sum);
		}
		return tokenTF;
	}

	public HashMap<String, Double> getWeights(HashMap<String, Double> tokenTF, HashMap<String, Double> qgramIDF,
			double averageLength) {
		return getWeights(tokenTF, qgramIDF);
	}

	public HashMap<String, Double> getWeights(String str, HashMap<String, Double> qgramIDF) {
		return getWeights(getTF(str), qgramIDF);
	}

	public HashMap<String, Double> getQueryWeights(String str, HashMap<String, Double> qgramIDF) {
		return getWeights(getTF(str), qgramIDF);
	}

	public void convertDFtoIDF(HashMap<String, Double> qgramIDF, int size) {
		for (String qgram : qgramIDF.keySet()) {
         // changing idf to log(N/n)
			//qgramIDF.put(qgram, Math.log(1 + size / qgramIDF.get(qgram)));
			qgramIDF.put(qgram, Math.log( size / qgramIDF.get(qgram)));
		}
	}

	public void convertDFtoIDF(HashMap<String, Double> qgramIDF, int size, Vector<HashMap<String, Double>> recordTokenWeights) {
		convertDFtoIDF(qgramIDF, size);
	}

	
	public void getDFandTFweight(int recordId, String str, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights) {
		HashMap<String, Double> tokenTF = getTF(str);
		recordTokenWeights.insertElementAt(tokenTF, recordId);
		// Set df's
		for (String qgram : tokenTF.keySet()) {
			incrementDFCount(qgram, qgramIDF, tokenTF.get(qgram));
		}
	}

	public double getTotalTokens(HashMap<String, Double> tokenTf) {
		double totalTF = 0;
		for (String str : tokenTf.keySet()) {
			totalTF += tokenTf.get(str);
		}
		return totalTF;
	}

	public double getAverageRecordLength(Vector<HashMap<String, Double>> recordTokenWeights) {
		double aveLength = 0;
		for (int i = 0; i < recordTokenWeights.size(); i++) {
			HashMap<String, Double> tokenTf = recordTokenWeights.get(i);
			aveLength += getTotalTokens(tokenTf);
		}
		return aveLength/recordTokenWeights.size();
	}

	public void convertTFtoWeights(Vector<String> records, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights) {
		double averageLength = 0;
		averageLength = getAverageRecordLength(recordTokenWeights);
		//System.out.println(" avgDL: "+averageLength);
		for (int i = 0; i < records.size(); i++) {
			HashMap<String, Double> tokenTfidf = recordTokenWeights.get(i);
			tokenTfidf = getWeights(tokenTfidf, qgramIDF, averageLength);
			recordTokenWeights.setElementAt(tokenTfidf, i);
		}
	}

	public void preprocessTable(HashMap<Integer,String> records, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights, String tableName) {
	
	}

	public void preprocessTable(Vector<String> records, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights, String tableName) {
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		int numberOfRecords = 0, k = 0;
		try {
			String query = "select tid, "+config.preprocessingColumn+" from " + config.dbName + "." + tableName + " order by tid asc";
			ResultSet rs = mysqlDB.executeQuery(query);
			if (rs != null) {
				while (rs.next()) {
					String str = rs.getString(config.preprocessingColumn);
					k = rs.getInt("tid");
					if ((str != null) && (!str.equals(""))) {
						//System.out.println(k+":  ["+str+"]  :"+str.length() +"   {"+str.trim()+"} "+str.trim().length());
						//str = str.replaceAll("'", "''");
						str = str.toLowerCase();
						records.insertElementAt(str, k - 1);
						// Find the tf's of all the qgrams
						getDFandTFweight(k - 1, str, qgramIDF, recordTokenWeights);
					}
					numberOfRecords++;
				}
			}
			mysqlDB.close();
		} catch (Exception e) {
			System.out.println("database error: cannot read table");
			e.printStackTrace();
		}
		
		if(logToDB){
			storePreprocessedDataToDB(tableName, recordTokenWeights, extractMetricName(this.getClass().getName())+"_tf" );
		}
		// convert the df into IDF
		//System.out.println("N: " + numberOfRecords);
		convertDFtoIDF(qgramIDF, numberOfRecords, recordTokenWeights);
		if(logToDB){
			storePreprocessedIDF(tableName, qgramIDF, extractMetricName(this.getClass().getName())+"_idf" );
		}
		// Now convert the tf in fileQrmanWeight to tfidf weights
		convertTFtoWeights(records, qgramIDF, recordTokenWeights);
		if(logToDB){
			storePreprocessedDataToDB(tableName, recordTokenWeights, extractMetricName(this.getClass().getName())+"_wt" );
		}
		//System.exit(0);
	}

	public void preprocessTableWSign(Vector<String> records, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights, 
			HashMap<Integer, Long> signature, 
			Integer par1,
			Integer par2,
			Integer par3,
			String tableName) {
	}
	
	
	
	
	
	public List<IdScore> getSimilarRecords(String query, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights) {
		//query = query.replaceAll("'", "''");
		List<IdScore> scoreList = new ArrayList<IdScore>();
		HashMap<String, Double> queryWeights = getQueryWeights(query, qgramIDF);
		for (int k = 0; k < recordTokenWeights.size(); k++) {
			double score = 0;
			boolean haveCommonToken = false;
			HashMap<String, Double> fileWeights = recordTokenWeights.get(k);
			for (String qgram : queryWeights.keySet()) {
				if (fileWeights.containsKey(qgram)) {
					score += fileWeights.get(qgram) * queryWeights.get(qgram);
					haveCommonToken = true;
				}
			}
			if (haveCommonToken) {
				scoreList.add(new IdScore(k, score));
			}
		}
		// // System.gc();
		Collections.sort(scoreList);
		
		return scoreList;
	}

	// This function is for the metrics which has to go iteratively through all the records to get the final scoreList
	public List<IdScore> getSimilarRecords(String query, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights, Vector<String> recordVector) {
		return getSimilarRecords(query, qgramIDF, recordTokenWeights);
	}
	
	// TODO: Check if the threshold version works
	public List<IdScore> getSimilarRecordsTH(String query, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights, Vector<String> recordVector, Double thr, int begin) {
		List<IdScore> scoreList = new ArrayList<IdScore>();
		HashMap<String, Double> queryWeights = getQueryWeights(query, qgramIDF);
		for (int k = (begin-1); k < recordTokenWeights.size(); k++) {
			double score = 0;
			//boolean haveCommonToken = false;
			HashMap<String, Double> fileWeights = recordTokenWeights.get(k);
			for (String qgram : queryWeights.keySet()) {
				if (fileWeights.containsKey(qgram)) {
					score += fileWeights.get(qgram) * queryWeights.get(qgram);
					//haveCommonToken = true;
				}
			}
			if (score >= thr) {
				scoreList.add(new IdScore(k, score));
			}
		}
		// // System.gc();
		Collections.sort(scoreList);
		
		return scoreList;	
	}

	// TODO: Check if the threshold version works
	public List<IdScore> getSimilarRecordsTH2(String query, HashMap<String, Double> qgramIDF,
			HashMap<Integer, String> records, Double thr) {
		return null;
	}

	public IdScore getMaxSimilarRecord(String query, HashMap<String, Double> qgramIDF,
			HashMap<Integer, String> records, Double thr) {
		return null;		
	}	
	
	
	public IdScore getMaxSimilarRecord2(String query, HashMap<String, Double> qgramIDF,
			HashMap<Integer, String> records, Vector<HashMap<String, Double>> recordTokenWeights, Double thr) {
		IdScore max = null;
		HashMap<String, Double> queryWeights = getQueryWeights(query, qgramIDF);
		//double meanIDF = Util.getMeanIDF(qgramIDF);
		double maxscore = thr;
		for (int k:records.keySet()) {
			double score = 0;
			HashMap<String, Double> fileWeights = recordTokenWeights.get(k);
			for (String qgram : queryWeights.keySet()) {
				if (fileWeights.containsKey(qgram)) {
					score += fileWeights.get(qgram) * queryWeights.get(qgram);
					//haveCommonToken = true;
				}
			}
			//System.out.println(score);
			if(score >= maxscore){
				max = new IdScore(k, score);
				maxscore = score;
			}
		}
		// // System.gc();
		
		//if (scoreList.size()!=0) max = Collections.max(scoreList);
		return max;		
	}	
	

	
	
	// Returns single score
	// TODO: check
	public double getSimilarityScore(String query, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights, int tid, String record) {
		HashMap<String, Double> queryWeights = getQueryWeights(query, qgramIDF);
		double score = 0;
		HashMap<String, Double> fileWeights = recordTokenWeights.get(tid-1);
		for (String qgram : queryWeights.keySet()) {
			if (fileWeights.containsKey(qgram)) {
				score += fileWeights.get(qgram) * queryWeights.get(qgram);
			}
		}
		return score;
	}
	
	public double getSimilarityScore(String query, HashMap<String, Double> qgramIDF, double meanIDF,
			Vector<HashMap<String, Double>> recordTokenWeights, int tid, String record) {
		HashMap<String, Double> queryWeights = getQueryWeights(query, qgramIDF);
		double score = 0;
		HashMap<String, Double> fileWeights = recordTokenWeights.get(tid-1);
		for (String qgram : queryWeights.keySet()) {
			if (fileWeights.containsKey(qgram)) {
				score += fileWeights.get(qgram) * queryWeights.get(qgram);
			}
		}
		return score;
	}
	
	public static void storePreprocessedDataToDB(String tableName, Vector<HashMap<String, Double>> recordTokenWeights,
			String metric) {
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		String query = "";
		try {
			query = "drop table if exists " + config.dbName + ".PP_" + tableName + "_" + metric;
			mysqlDB.executeUpdate(query);

			query = "create table " + config.dbName + ".PP_" + tableName + "_" + metric
					+ " (rid int, token varchar(255), weight double)";
			mysqlDB.executeUpdate(query);

			System.err.println("Executing " + query);
			for (int a = 0; a < recordTokenWeights.size(); a++) {
				HashMap<String, Double> tokenWeights = recordTokenWeights.get(a);
				for (String token : tokenWeights.keySet()) {
					query = "insert into " + config.dbName + ".PP_" + tableName + "_" + metric + " values (" + (a + 1)
							+ " ,'" + token.replaceAll("'", "''") + "', "+ tokenWeights.get(token) + ")";
					mysqlDB.executeUpdate(query);
				}
			}
			mysqlDB.close();
		} catch (Exception e) {
			System.err.println("Can't execute Query: " + query);
			e.printStackTrace();
		}
	}

	public static void storePreprocessedIDF(String tableName, HashMap<String, Double> qgramIDF, String metric) {
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		String query = "";
		try {
			query = "drop table if exists " + config.dbName + ".PP_" + tableName + "_" + metric;
			mysqlDB.executeUpdate(query);

			query = "create table " + config.dbName + ".PP_" + tableName + "_" + metric
					+ " (token varchar(255), idf double)";
			mysqlDB.executeUpdate(query);

			System.err.println("Executing " + query);
			for (String token: qgramIDF.keySet()) {
					query = "insert into " + config.dbName + ".PP_" + tableName + "_" + metric + " values ('" 
							 + token.replaceAll("'", "''") + "', "+ qgramIDF.get(token) + ")";
					mysqlDB.executeUpdate(query);
			}
			mysqlDB.close();
		} catch (Exception e) {
			System.err.println("Can't execute Query: " + query);
			e.printStackTrace();
		}
	}
}
