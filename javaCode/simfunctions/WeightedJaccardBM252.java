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
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import utility.Config;
import dbdriver.MySqlDB;

public class WeightedJaccardBM252 extends Preprocess {

	double meanIDF=0;
	
	// Tokenizer based on whitespace; every token is a word
	// Its not required for Edit-Distance metric
	public HashMap<String, Double> getTF(String str) {
		return gettokenTFSingle(str);
	}
	
	public void convertDFtoIDF(HashMap<String, Double> qgramIDF, int size) {
		for (String qgram : qgramIDF.keySet()) {
			qgramIDF.put(qgram, Math.log((size - qgramIDF.get(qgram) + 0.5) / (qgramIDF.get(qgram) + 0.5)));
		}
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
		//convert the Df to IDF
		convertDFtoIDF(qgramIDF, numberOfRecords);
		//recordTokenWeights.clear();
	}

	// Returns single score
	public double getSimilarityScore(String query, HashMap<String, Double> qgramIDF, double meanIDF,
			Vector<HashMap<String, Double>> recordTokenWeights, int tid, String record) {
		//double score = weightedJaccard(query, record, qgramIDF);
		String s = query;
		String t = record;
		//return score;
		double weightedJaccardScore=0;
		HashMap<String, Double> fileWeights = recordTokenWeights.get(tid-1);
		Set<String> setS = gettokenTFSingle(s).keySet();
		//Set<String> setS = queryWeights.keySet();
		

		double weightedSumS =0;
		for(String token: setS){
			weightedSumS  += qgramIDF.get(token);
		}

		//Set<String> setT = gettokenTFSingle(t).keySet();
		Set<String> setT = fileWeights.keySet();
		
		double weightedSumT =0;
		for(String token: setT){
			weightedSumT  += qgramIDF.get(token);
		}
		
		setS.retainAll(setT);

		double weightedSumSandT=0;
		for(String token: setS){
			weightedSumSandT += qgramIDF.get(token);
		}

		if(weightedSumS > 0){
			weightedJaccardScore = weightedSumSandT / (weightedSumS + weightedSumT - weightedSumSandT);
		}

		return weightedJaccardScore;
	}
	
	
}
