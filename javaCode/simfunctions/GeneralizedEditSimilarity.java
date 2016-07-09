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

public class GeneralizedEditSimilarity extends Preprocess {

	public static double insertionCost = 0.5;

	public String similarityMetric = "editdistance";

	public static HashMap<String, HashMap<String, Double>> tokenStokenTEDst=null;
	
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

	//This stores the normalized ed for querytokens with all other tokens
	public static void initializeTokenTokenEDMap(String query, HashMap<String, Double> qgramIDF){
		tokenStokenTEDst = new HashMap<String, HashMap<String, Double>>();
		String[] tokens = query.toLowerCase().split(" ");
		for(int i=0; i < tokens.length; i++){
			HashMap<String, Double> tokenEDMap = new HashMap<String, Double>();
			for(String token: qgramIDF.keySet()){
				tokenEDMap.put(token, normalizedEditDistance(tokens[i], token));
			}
			tokenStokenTEDst.put(tokens[i], tokenEDMap);
		}
	}
	
	public double getNormalizedEditDistanceFromEDMap(String s, String t){
		double ed =0;
		try{
			ed = tokenStokenTEDst.get(s).get(t);
		}catch (Exception e) {
			System.err.println("The ED MAP does not contain entry for "+s+" and "+t);
			// TODO: handle exception
			ed = normalizedEditDistance(s, t);
		}
		return ed; 
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
		initializeTokenTokenEDMap(query, qgramIDF);
		//t2 = System.currentTimeMillis();
		//System.out.println("intialization time: "+(t2-t1));
		double meanIDF = 0;
		for (double idf : qgramIDF.values()) {
			meanIDF += idf;
		}
		meanIDF = meanIDF / qgramIDF.size();
		// System.out.println("meanIDF: "+meanIDF);
		// find the weighted sum for the tokens in query
		Vector<String> inputTokenVector = getTokenVector(query);
		Vector<Double> weightedTokenVector = incrementalWeightedSumVector(inputTokenVector, qgramIDF, meanIDF);
	
		for (int k = 0; k < recordVector.size(); k++) {
			// find meanIDF value		
			double score = generalizedEditSimilarity(query, recordVector.get(k), qgramIDF, meanIDF, weightedTokenVector);
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

	public double generalizedEditSimilarity(String s, String t, HashMap<String, Double> qgramIDF, double meanIDF,
			Vector<Double> weightedTokenVector) {
		int weightVectorSize = weightedTokenVector.size();
		if ((weightVectorSize <= 1) || (weightedTokenVector.get(weightVectorSize - 1) <= 0)) {
			return 0;
		} else {
			double transformationCost = weightedLD(s, t, qgramIDF, meanIDF, weightedTokenVector);
			
			return 1.0 - Math.min(1, transformationCost / weightedTokenVector.get(weightVectorSize - 1));
		}
	}

	public double weightedSumTokens(Vector<String> tokenVector, HashMap<String, Double> qgramIDF, double meanIDF) {
		double weightedTokenVectorSum = 0;
		for (String token : tokenVector) {
			if (qgramIDF.containsKey(token)) {
				weightedTokenVectorSum += qgramIDF.get(token);
			} else {
				weightedTokenVectorSum += meanIDF;
			}
		}
		return weightedTokenVectorSum;
	}

	// it returns the vector "v" such that v(i) = \sum_{0 <= j < i} weight(u(j))
	// where u is the vector of tokens
	public Vector<Double> incrementalWeightedSumVector(Vector<String> tokenVector, HashMap<String, Double> qgramIDF,
			double meanIDF) {
		Vector<Double> incrementalTokenWeightSumVector = new Vector<Double>();
		double weightedTokenVectorSum = 0;
		incrementalTokenWeightSumVector.add(0.0);
		for (int i = 0; i < tokenVector.size(); i++) {
			String token = tokenVector.get(i);
			weightedTokenVectorSum += getIDFWeight(token, qgramIDF, meanIDF);
			incrementalTokenWeightSumVector.add(weightedTokenVectorSum);
		}
		return incrementalTokenWeightSumVector;
	}

	public double getIDFWeight(String token, HashMap<String, Double> qgramIDF, double meanIDF) {
		if (qgramIDF.containsKey(token)) {
			return qgramIDF.get(token);
		} else {
			return meanIDF;
		}

	}

	public double weightedLD(String s, String t, HashMap<String, Double> qgramIDF, double meanIDF,
			Vector<Double> weightedTokenVector) {
		Vector<String> inputTokenVector = getTokenVector(s);
		Vector<String> tupleTokenVector = getTokenVector(t);
		Vector<Double> tupleTokenIncrementalWeightedSum = incrementalWeightedSumVector(tupleTokenVector, qgramIDF,
				meanIDF);
		double d[][];
		double ed[][];
		double track[][];
		int n, m, i, j;
		String tok_i, tok_j;
		double cost = 0, wt_i, wt_j;
		// Step 1
		n = inputTokenVector.size();
		m = tupleTokenVector.size();

		if (n == 0) {
			return insertionCost * tupleTokenIncrementalWeightedSum.get(m);
		}
		if (m == 0) {
			return weightedTokenVector.get(n);
		}
		d = new double[n + 1][m + 1];
		//FOR DEBUG PURPOSE
		ed = new double[n][m];
		track = new double[n][m];

		// Step 2
		for (i = 0; i <= n; i++) {
			d[i][0] = weightedTokenVector.get(i);
		}
		for (j = 0; j <= m; j++) {
			d[0][j] = insertionCost * tupleTokenIncrementalWeightedSum.get(j);
		}
		// Step 3
		for (i = 1; i <= n; i++) {
			tok_i = inputTokenVector.get(i - 1);
			wt_i = getIDFWeight(tok_i, qgramIDF, meanIDF);
			// Step 4
			for (j = 1; j <= m; j++) {
				tok_j = tupleTokenVector.get(j - 1);
				wt_j = getIDFWeight(tok_j, qgramIDF, meanIDF);
				ed[i - 1][j - 1] = normalizedEditDistance(tok_i, tok_j);
				cost = wt_i * ed[i - 1][j - 1];
				//cost = wt_i* normalizedEditDistance(tok_i, tok_j);
				//cost = wt_i*getNormalizedEditDistanceFromEDMap(tok_i, tok_j);
				// Step 6
				d[i][j] = Minimum(d[i - 1][j] + wt_i, d[i][j - 1] + insertionCost*wt_j, d[i - 1][j - 1] + cost);
				// to track the path FOR DEBUG PURPOSE
				
				track[i - 1][j - 1] = trackMinimum(d[i - 1][j] + wt_i, d[i][j - 1] + insertionCost*wt_j, d[i - 1][j - 1] + cost);
				
				if ((track[i - 1][j - 1] == 211) && (ed[i - 1][j - 1] == 0)) {
					track[i - 1][j - 1] = 200;
				}
				
			}
		}

		/*
		 * FOR DEBUGGING PURPOSE
		 */
		
		if (s.contains("union") && t.contains("union") && t.contains("elec")) {
			System.out.println("QUERY TOKENS");
			printTokenIDFs(inputTokenVector, qgramIDF, meanIDF);
			System.out.println("TUPLE TOKENS");
			printTokenIDFs(tupleTokenVector, qgramIDF, meanIDF);
			System.out.println("Edit Distance Metric");
			print2DMatrix(ed);
			System.out.println("GENERALIZED Edit Distance Metric");
			print2DMatrix(d);
			System.out.println("GENERALIZED Edit Distance TRACK Metric");
			print2DMatrix(track);
		}
		
		// Step 7
		return d[n][m];
	}

	public void printTokenIDFs(Vector<String> tokenVector, HashMap<String, Double> qgramIDF, double meanIDF) {
		for (String token : tokenVector) {
			System.out.print(token + ": " + getIDFWeight(token, qgramIDF, meanIDF) + "  ");
		}
		System.out.println();
	}

	public void print2DMatrix(double[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				System.out.print(matrix[i][j] + " ");
			}
			System.out.println();
		}
	}

	private double trackMinimum(double a, double b, double c) {
		/*
		 * 211 fro diagonal, 210 for up, 201 for left
		 */
		double mi, mj;
		mi = a;
		mj = 210;
		if (b < mi) {
			mj = 201;
			mi = b;
		}
		if (c < mi) {
			mj = 211;
		}
		return mj;
	}

	/*
	 * The code below has been copied from
	 * http://www.merriampark.com/ld.htm#JAVA
	 */
	private double Minimum(double a, double b, double c) {
		double mi;
		mi = a;
		if (b < mi) {
			mi = b;
		}
		if (c < mi) {
			mi = c;
		}
		return mi;
	}

	public static double normalizedEditDistance(String s, String t) {
		int ed = LD(s, t);
		return ed * 1.0 / Math.max(s.length(), t.length());
	}

	private static int Minimum(int a, int b, int c) {
		int mi;
		mi = a;
		if (b < mi) {
			mi = b;
		}
		if (c < mi) {
			mi = c;
		}
		return mi;
	}

	// *****************************
	// Compute Levenshtein distance
	// *****************************

	public static int LD(String s, String t) {
		int d[][]; // matrix
		int n; // length of s
		int m; // length of t
		int i; // iterates through s
		int j; // iterates through t
		char s_i; // ith character of s
		char t_j; // jth character of t
		int cost; // cost

		// Step 1
		n = s.length();
		m = t.length();
		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}
		d = new int[n + 1][m + 1];

		// Step 2
		for (i = 0; i <= n; i++) {
			d[i][0] = i;
		}
		for (j = 0; j <= m; j++) {
			d[0][j] = j;
		}
		// Step 3
		for (i = 1; i <= n; i++) {
			s_i = s.charAt(i - 1);

			// Step 4
			for (j = 1; j <= m; j++) {
				t_j = t.charAt(j - 1);

				// Step 5
				if (s_i == t_j) {
					cost = 0;
				} else {
					cost = 1;
				}

				// Step 6
				d[i][j] = Minimum(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost);
			}
		}

		// Step 7
		return d[n][m];
	}
}
