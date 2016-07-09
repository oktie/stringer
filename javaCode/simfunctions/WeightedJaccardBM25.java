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

public class WeightedJaccardBM25 extends Preprocess {

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
						//System.out.println(str);
						records.insertElementAt(str, k - 1);
						// Find the tf's of all the qgrams
						getDFandTFweight(k - 1, str, qgramIDF, recordTokenWeights);
					}
					else if (str.equals("")) records.insertElementAt("NULL", k - 1); // todo: oktie: check! is NULL a good idea? 
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
		//System.out.println(qgramIDF);
		//recordTokenWeights.clear();
	}

	// This function is for the metrics which has to go iteratively through all
	// the records to get the final scoreList
	public List<IdScore> getSimilarRecords(String query, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights, Vector<String> recordVector) {
		List<IdScore> scoreList = new ArrayList<IdScore>();
		//HashMap<String, Double> queryWeights = getQueryWeights(query, qgramIDF);
		double meanIDF = Util.getMeanIDF(qgramIDF);
		for (int k = 0; k < recordVector.size(); k++) {
			double score = weightedJaccard(query, recordVector.get(k), qgramIDF, meanIDF);
			if(score > 0){
				scoreList.add(new IdScore(k, score));
			}
		}
		// // System.gc();
		Collections.sort(scoreList);
		//System.err.println(" result size: "+scoreList.size());
		return scoreList;
	}
	
	public List<IdScore> getSimilarRecordsTH(String query, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights, Vector<String> recordVector, Double thr, int begin) {
		List<IdScore> scoreList = new ArrayList<IdScore>();
		//HashMap<String, Double> queryWeights = getQueryWeights(query, qgramIDF);
		double meanIDF = Util.getMeanIDF(qgramIDF);
		for (int k = (begin-1); k < recordVector.size(); k++) {
			double score = weightedJaccard(query, recordVector.get(k), qgramIDF, meanIDF);
			if(score >= thr){
				scoreList.add(new IdScore(k, score));
			}
		}
		// // System.gc();
		Collections.sort(scoreList);
		//System.err.println(" result size: "+scoreList.size());
		return scoreList;
	}
	
	// Returns single score
	public double getSimilarityScore(String query, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights, int tid, String record) {
		double meanIDF = Util.getMeanIDF(qgramIDF);
		double score = weightedJaccard(query, record, qgramIDF, meanIDF);
		return score;
	}

	public double weightedJaccard(String s, String t, HashMap<String, Double> qgramIDF, double meanIDF){
		//System.out.println("wj called");
		//long time=System.currentTimeMillis();
		double weightedJaccardScore=0;
		Set<String> setS = gettokenTFSingle(s).keySet();
		double weightedSumS = Util.getWeightedSumForTokenSet(setS, qgramIDF, meanIDF);
		Set<String> setT = gettokenTFSingle(t).keySet();
		double weightedSumT = Util.getWeightedSumForTokenSet(setT, qgramIDF, meanIDF);
		
		Util.printlnDebug(setS);
		Util.printlnDebug(setT);
		
		setS.retainAll(setT);
		//Util.printlnDebug(setS);
		double weightedSumSandT = Util.getWeightedSumForTokenSet(setS, qgramIDF, meanIDF);
		if(setS.size() > 0){
			//System.out.println(s + " "  + weightedSumS);
			//System.out.println(t + " "  + weightedSumT);
			//System.out.println("qgram" + " "  + qgramIDF);
			//System.out.println("sum" + " "  + (weightedSumS + weightedSumT));
			//System.out.println("intersect" + " "  + weightedSumSandT);
			//System.out.println("union" + " "  + (weightedSumS + weightedSumT - weightedSumSandT));
			weightedJaccardScore = weightedSumSandT / (weightedSumS + weightedSumT - weightedSumSandT);
			//System.out.println("score" + " "  + weightedJaccardScore);
			//System.out.println("*** " + (Math.exp(-2.1972245773362196)));
		}
		//Util.printlnDebug("Weighted Jaccard Score: "+weightedJaccardScore);
		//Util.printlnDebug("Jaccard : "+( setS.size()/ (sizeS + setT.size() - setS.size())));
		//System.out.println((System.currentTimeMillis() - time));
		return weightedJaccardScore;
	}
	
	
}
