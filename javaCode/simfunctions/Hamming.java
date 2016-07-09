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
import java.util.Set;
import java.util.Vector;

import utility.Config;
import utility.Util;
import dbdriver.MySqlDB;
import experiment.IdScore;

public class Hamming extends Preprocess {

	double meanIDF=0;
	
	// Tokenizer based on whitespace; every token is a word
	// Its not required for Edit-Distance metric
	public HashMap<String, Double> getTF(String str) {
		return gettokenTFSingle(str);
	}
	
	public void getDFandTFweight(int recordId, String str, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights) {
		HashMap<String, Double> tokenTF = getTF(str);
		//recordTokenWeights.insertElementAt(tokenTF, recordId);
		// Set df's
		for (String qgram : tokenTF.keySet()) {
			incrementDFCount(qgram, qgramIDF, tokenTF.get(qgram));
		}
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
						records.insertElementAt(str, k - 1);
						// Find the tf's of all the qgrams
						//getDFandTFweight(k - 1, str, qgramIDF, recordTokenWeights);
					}
					numberOfRecords++;
				}
			}
			mysqlDB.close();
		} catch (Exception e) {
			System.out.println("database error: cannot read table");
			e.printStackTrace();
		}
		//convert the Df to IDF
		//convertDFtoIDF(qgramIDF, numberOfRecords);
	}

	// This function is for the metrics which has to go iteratively through all
	// the records to get the final scoreList
	public List<IdScore> getSimilarRecords(String query, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights, Vector<String> recordVector) {
		List<IdScore> scoreList = new ArrayList<IdScore>();
		//HashMap<String, Double> queryWeights = getQueryWeights(query, qgramIDF);
		for (int k = 0; k < recordVector.size(); k++) {
			double score = HammingScore(query, recordVector.get(k));
			if(score > 0){
				scoreList.add(new IdScore(k, score));
			}
		}
		// // System.gc();
		Collections.sort(scoreList);
		return scoreList;
	}
	
	// This function is for the metrics which has to go iteratively through all
	// the records to get the final scoreList
	public List<IdScore> getSimilarRecordsTH(String query, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights, Vector<String> recordVector,
			Double thr, int begin) {
		List<IdScore> scoreList = new ArrayList<IdScore>();
		//HashMap<String, Double> queryWeights = getQueryWeights(query, qgramIDF);
		for (int k = (begin-1); k < recordVector.size(); k++) {
			double score = HammingScore(query, recordVector.get(k));
			if(score <= thr){
				scoreList.add(new IdScore(k, score));
			}
		}
		// // System.gc();
		Collections.sort(scoreList);
		return scoreList;
	}

	public double HammingScore(String s, String t){
		//System.out.println("wj called");
		double sizeS = 0;
		double hammingScore=0;
		Set<String> setS = gettokenTFSingle(s).keySet();
		Set<String> setSMT = gettokenTFSingle(s).keySet();
		sizeS = setS.size();
		//double weightedSumS = Util.getWeightedSumForTokenSet(setS, qgramIDF, meanIDF);
		//<String> setT = gettokenTFSingle(t).keySet();
		Set<String> setTMS = gettokenTFSingle(t).keySet();
		
		//Util.printlnDebug(setS);
		//Util.printlnDebug(setT);
		//double weightedSumT = Util.getWeightedSumForTokenSet(setS, qgramIDF, meanIDF);
		setSMT.removeAll(setTMS);
		setTMS.removeAll(setS);
		Util.printlnDebug(setS);
		//double weightedSumSandT = Util.getWeightedSumForTokenSet(setS, qgramIDF, meanIDF);
		if(setS.size() > 0){
			hammingScore = setTMS.size() + setSMT.size(); 
				//weightedSumSandT / (weightedSumS + weightedSumT - weightedSumSandT);
		}

		return hammingScore;
	}
	
	
}
