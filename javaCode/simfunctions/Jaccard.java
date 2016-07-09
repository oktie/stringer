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

public class Jaccard extends Preprocess {

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
			double score = jaccardScore(query, recordVector.get(k));
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
			double score = jaccardScore(query, recordVector.get(k));
			if(score >= thr){
				scoreList.add(new IdScore(k, score));
			}
		}
		// // System.gc();
		Collections.sort(scoreList);
		return scoreList;
	}

	public double jaccardScore(String s, String t){
		//System.out.println("wj called");
		double sizeS = 0;
		double jaccardScore=0;
		Set<String> setS = gettokenTFSingle(s).keySet();
		sizeS = setS.size();
		//double weightedSumS = Util.getWeightedSumForTokenSet(setS, qgramIDF, meanIDF);
		Set<String> setT = gettokenTFSingle(t).keySet();
		
		Util.printlnDebug(setS);
		Util.printlnDebug(setT);
		//double weightedSumT = Util.getWeightedSumForTokenSet(setS, qgramIDF, meanIDF);
		setS.retainAll(setT);
		Util.printlnDebug(setS);
		//double weightedSumSandT = Util.getWeightedSumForTokenSet(setS, qgramIDF, meanIDF);
		if(setS.size() > 0){
			jaccardScore = setS.size()/( sizeS + setT.size() - setS.size()); 
				//weightedSumSandT / (weightedSumS + weightedSumT - weightedSumSandT);
		}
		Util.printlnDebug("Jaccard: "+jaccardScore);
		Util.printlnDebug("Intersect: "+setS.size() );
		return jaccardScore;
	}

	public IdScore getMaxSimilarRecord(String query, HashMap<String, Double> qgramIDF,
			HashMap<Integer,String> records, Double thr) {
		IdScore max = null;
		//double meanIDF = Util.getMeanIDF(qgramIDF);
		double maxscore = thr;
		for (int k:records.keySet()) {
			double score = jaccardScore(query, records.get(k));
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

	public void preprocessTable(HashMap<Integer,String> records, HashMap<String, Double> qgramIDF,
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
						records.put(k, str);
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
		convertDFtoIDF(qgramIDF, numberOfRecords);
		//System.out.println(qgramIDF);
		//recordTokenWeights.clear();
	}
	
	
}
