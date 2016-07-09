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

public class EditDistance extends Preprocess {

	// Tokenizer based on whitespace; every token is a word
	// Its not required for Edit-Distance metric
	public HashMap<String, Double> getTF(String str) {
		return getTokenTFMultiple(str);
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
	}

	// This function is for the metrics which has to go iteratively through all
	// the records to get the final scoreList
	public List<IdScore> getSimilarRecords(String query, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights, Vector<String> recordVector) {
		List<IdScore> scoreList = new ArrayList<IdScore>();
		//HashMap<String, Double> queryWeights = getQueryWeights(query, qgramIDF);
		for (int k = 0; k < recordVector.size(); k++) {
			double score = editSimilarity(query, recordVector.get(k));
			// for similar records score must be greater than zero
			
			//if(score > 0){
				scoreList.add(new IdScore(k, score));
			//}
		}
		// // System.gc();
		Collections.sort(scoreList);
		return scoreList;
	}

	// This function is for the metrics which has to go iteratively through all
	// the records to get the final scoreList
	public List<IdScore> getSimilarRecordsTH(String query, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights, Vector<String> recordVector, Double thr, int begin) {
		List<IdScore> scoreList = new ArrayList<IdScore>();
		//HashMap<String, Double> queryWeights = getQueryWeights(query, qgramIDF);
		for (int k = (begin-1); k < recordVector.size(); k++) {
		
		//for (int k = (begin-1); k < recordTokenWeights.size(); k++) {
			double score = editSimilarity(query, recordVector.get(k));
			//System.out.println(" GOOOOZ " + score);
			// for similar records score must be greater than zero
			
			if(score >= thr){
				scoreList.add(new IdScore(k, score));
			}
		}
		// // System.gc();
		Collections.sort(scoreList);
		return scoreList;
	}

	public double editSimilarity(String s, String t){
		//return 1.0 - normalizedEditDistance(s,t);
		/*
		 *  Taking just the negative of the EditDistance
		 *  Note that its not normalized
		 */
		
		return 1-normalizedEditDistance(s, t);
	}
	
	public double normalizedEditDistance(String s, String t) {
		int ed = LD(s,t);
		return ed*1.0/Math.max(s.length(), t.length());
	}
	/*
	 * The code below has been copied from
	 * http://www.merriampark.com/ld.htm#JAVA
	 */
	public static void print2DMatrix(int[][] matrix, String s, String t) {
		System.out.print("- - ");
		for(int i=0; i < t.length(); i++){
			System.out.print(t.charAt(i) + " ");
		}
		System.out.println();
		
		for (int i = 0; i < Math.min(matrix.length, s.length()+1); i++) {
			if(i==0){
				System.out.print("- ");
			}else{
				System.out.print(s.charAt(i-1) + " ");
			}
			for (int j = 0; j < matrix[i].length; j++) {
				System.out.print(matrix[i][j] + " ");
			}
			System.out.println();
		}
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
		// For debug purpose
		//print2DMatrix(d, s, t);
		// Step 7
		return d[n][m];
	}
}
