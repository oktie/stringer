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
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import utility.Config;
import dbdriver.MySqlDB;
import experiment.IdScore;

public class ApproximateGES extends Preprocess {

	public static double insertionCost = 0.5;

	public String similarityMetric = "editdistance";

	public static boolean localDebugMode = true;
	
	public void printDebug(String str){
	//	if(localDebugMode)
	//		System.out.print(str);
	}
	
	// Tokenizer based on whitespace; every token is a word
	// Its not required for Edit-Distance metric
	public HashMap<String, Double> getTF(String str) {
		tokenizeUsingQgrams = false;
		return getTokenTFMultiple(str);
	}

	public Vector<String> getTokenVector(String str) {
		// Generalized edit distance considers word based tokens at the first
		// level
		Vector<String> tokenVector = new Vector<String>();
		String[] tokens = str.toLowerCase().split(" ");
		for (int i = 0; i < tokens.length; i++) {
			tokenVector.add(tokens[i]);
		}
		return tokenVector;
	}
	
	public void preprocessTable(Vector<String> records, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights, String tableName) {
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		int numberOfRecords = 0, k = 0;
		try {
			String query = "select tid, " + config.preprocessingColumn + " from " + config.dbName + "." + tableName
					+ " order by tid asc";
			ResultSet rs = mysqlDB.executeQuery(query);
			if (rs != null) {
				while (rs.next()) {
					String str = rs.getString(config.preprocessingColumn);
					k = rs.getInt("tid");
					str=str.toLowerCase();
					if ((str != null) && (!str.equals(""))) {
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
		if (logToDB) {
			storePreprocessedDataToDB(tableName, recordTokenWeights, extractMetricName(this.getClass().getName())
					+ "_tf");
		}
		// convert the df into IDF
		convertDFtoIDF(qgramIDF, numberOfRecords, recordTokenWeights);
		if (logToDB) {
			storePreprocessedIDF(tableName, qgramIDF, extractMetricName(this.getClass().getName()) + "_idf");
		}
		// Now convert the tf in fileQrmanWeight to tfidf weights
		/*
		 * convertTFtoWeights(records, qgramIDF, recordTokenWeights); if
		 * (logToDB) { storePreprocessedDataToDB(tableName, recordTokenWeights,
		 * extractMetricName(this.getClass().getName()) + "_wt"); }
		 */
	}

	// This function is for the metrics which has to go iteratively through all
	// the records to get the final scoreList
	public List<IdScore> getSimilarRecords(String query, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights, Vector<String> recordVector) {
		List<IdScore> scoreList = new ArrayList<IdScore>();
		//t1 = System.currentTimeMillis();
		//initializeTokenTokenEDMap(query, qgramIDF);
		//t2 = System.currentTimeMillis();
		//System.out.println("intialization time: "+(t2-t1));
		double meanIDF = 0;
		for (double idf : qgramIDF.values()) {
			meanIDF += idf;
		}
		meanIDF = meanIDF / qgramIDF.size();
		// System.out.println("meanIDF: "+meanIDF);
		// find the weighted sum for the tokens in query

		for (int k = 0; k < recordVector.size(); k++) {
			
			
			if(recordVector.get(k).toLowerCase().contains("union")){
				localDebugMode = true;
			}else{
				localDebugMode = false;
			}
			printDebug("QUERY: "+query+"   RECORD: "+(k+1) + "=====================================\n");
			
			// find meanIDF value
			double score = generalizedEditSimilarity(query, recordVector.get(k), qgramIDF, meanIDF);
			
			if(score > 0.5){
				localDebugMode = true;
			}
			printDebug("QUERY: "+query+"   RECORD: "+(k+1) + "=====================================\n");
			printDebug("final Score: "+score+"\n");
			printDebug("===========================================================================\n");
			// for similar records score must be greater than zero
			if (score > 0) {
				scoreList.add(new IdScore(k, score));
			}
		}
		// // System.gc();
		Collections.sort(scoreList);
		//t3 = System.currentTimeMillis();
		//System.out.println("Query time: "+(t3-t1));
		return scoreList;
	}

	public double generalizedEditSimilarity(String s, String t, HashMap<String, Double> qgramIDF, double meanIDF) {
		Vector<String> queryVector = getTokenVector(s);
		Vector<String> recordVector = getTokenVector(t);
		double queryTokenWeightSum=0, tokenweight=0, score = 0, scoreContribution=0;
		for(int i=0; i < queryVector.size(); i++){
			tokenweight = getIDFWeight(queryVector.get(i), qgramIDF, meanIDF);
			queryTokenWeightSum += tokenweight;
			printDebug(queryVector.get(i)+" : "+tokenweight+" \n");
			scoreContribution = getMaxAdjustedJaccardScore(queryVector.get(i), recordVector);
			score += scoreContribution* tokenweight;
			printDebug(queryVector.get(i)+" "+qgramIDF.get(queryVector.get(i))+" :: ");
			printDebug(" ::  adjustedJaccardScore "+ scoreContribution +"\n");
		}
		return score/ queryTokenWeightSum;
	}

	public double getMaxAdjustedJaccardScore(String token, Vector<String> recordVector){
		double maxScore=0, curScore=0;
		String maxSimilarToken = "";
		for(String tok: recordVector){
			curScore = getJaccardScore(token, tok);
			printDebug(tok+" "+curScore+" :: ");
			if(curScore>maxScore){
				maxScore = curScore;
				maxSimilarToken = tok;
			}
		}
		printDebug("\nMaxSimilar  "+maxSimilarToken+" "+maxScore+" :: \n");
		return ( (maxScore*2.0/qgramSize) + (1 - 1.0/qgramSize) );
	}
	
	public double getJaccardScore(String tok1, String tok2){
		HashSet<String> tokenSet1 = generateQgrams(tok1);
		HashSet<String> tokenSet2 = generateQgrams(tok2);
		int intersect=0;
		for(String tok: tokenSet1){
			if(tokenSet2.contains(tok)){
				intersect++;
			}
		}
		int denominator = tokenSet1.size() + tokenSet2.size() - intersect;
		if(denominator > 0)
			return intersect*1.0/denominator;
		else
			return 0;
	}
	
	public HashSet<String> generateQgrams(String str){
		HashSet<String> qgramSet = new HashSet<String>();
		str = str.toLowerCase();
		for (int i = 0; i <= str.length() - qgramSize; i++) {
			String qgram = str.substring(i, i + qgramSize);
			qgramSet.add(qgram);
		}
		return qgramSet;
	}

	public double getIDFWeight(String token, HashMap<String, Double> qgramIDF, double meanIDF) {
		if (qgramIDF.containsKey(token)) {
			return qgramIDF.get(token);
		} else {
			return meanIDF;
		}
	}

	public void printTokenIDFs(Vector<String> tokenVector, HashMap<String, Double> qgramIDF, double meanIDF) {
		for (String token : tokenVector) {
			System.out.print(token + ": " + getIDFWeight(token, qgramIDF, meanIDF) + "  ");
		}
		System.out.println();
	}

}
