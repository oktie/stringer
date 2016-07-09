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


import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import dbdriver.MySqlDB;
import simfunctions.ApproximateGES;
import simfunctions.BM25;
import simfunctions.EditDistance;
import simfunctions.EvaluateSJoinThread;
import simfunctions.GeneralizedEditSimilarity;
import simfunctions.HMM;
import simfunctions.Intersect;
import simfunctions.Jaccard;
import simfunctions.Preprocess;
import simfunctions.SoftTfIdf;
import simfunctions.TfIdf;
import simfunctions.WeightedIntersect;
import simfunctions.WeightedIntersectBM25;
import simfunctions.WeightedJaccard;
import simfunctions.WeightedJaccardBM25;
import utility.Config;
import utility.Util;

public class evalSingleSimilarityJoin {
	
	public static int queryTokenLength = 2;

	public static String getQuery(int tid, String tableName) {
		String resultQuery = "";
		String query = "";
		Config config = new Config();

		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		try {
		
			query = "SELECT " + config.preprocessingColumn + ", id FROM " + config.dbName + "."
					+ tableName + " T WHERE T.tid = " + tid;
			
			//System.out.println("Executing " + query);
			ResultSet rs = mysqlDB.executeQuery(query);
			rs.next();
			resultQuery = rs.getString(config.preprocessingColumn);
			mysqlDB.close();
		} catch (Exception e) {
			System.err.println("Can't generate the query");
			e.printStackTrace();
		}

		return resultQuery;
	}

	public static HashSet<Integer> getAllTidsHavingIdSameAs(int tid, String tableName) {
		HashSet<Integer> tidsHavingThisID = new HashSet<Integer>();
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		try {
			String query = "SELECT tid FROM " + config.dbName + "." + tableName + " where id=" + 
			               "(SELECT id FROM " + config.dbName + "." + tableName + " t where t.tid= " + tid +")";
			//System.out.println("Executing " + query);
			ResultSet rs = mysqlDB.executeQuery(query);
			if (rs != null) {
				while (rs.next()) {
					tidsHavingThisID.add(rs.getInt("tid"));
				}
			}
			mysqlDB.close();
		} catch (Exception e) {
			System.err.println("Can't run query");
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

	

	
	public static void main(String[] args) {
		
		
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		String resultTable = "ssingle_sjoinresults";
		try {
			//String query = "DROP TABLE IF EXISTS " + config.dbName + "." + resultTable;
			//mysqlDB.executeUpdate(query);

			String query = "CREATE TABLE IF NOT EXISTS " + config.dbName + "." + resultTable 
					+ " (tbl varchar(10), simfunc varchar(50), thr double, pr double, re double, f1 double, " +
					  " PRIMARY KEY (tbl, simfunc, thr) )";
			System.out.println(query);
			mysqlDB.executeUpdate(query);
			
			mysqlDB.close();
		} catch (Exception e) {
			System.err.println("DB Error");
			e.printStackTrace();
		} 
		
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
		
		//Preprocess bm25WeightedJaccard = new WeightedJaccardBM25();
		//Preprocess measure = bm25WeightedJaccard;
		//Preprocess measure = tfidf;
		//Preprocess measure = ges;
		// measure = ed;
		//Preprocess hmm = new HMM();
		//Preprocess measure = softtfidf;
		//Preprocess measure = bm25weightedIntersect;
		//Preprocess measure = bm25;
		//Preprocess measure = jaccard;
		//Preprocess measure = new HMM();
		Preprocess measure = ges;
		
		double range = 2.0;
		
		Vector<String> tables = new Vector<String>();
		
		tables.add("cu1");
		
		/*
		tables.add("cu1"); 
		tables.add("cu2"); 
		tables.add("cu3");
		tables.add("cu4");
		tables.add("cu5");
		tables.add("cu6");
		tables.add("cu7");
		tables.add("cu8");
		
		tables.add("F1"); 
		tables.add("F2");
		tables.add("F3"); 
		tables.add("F4");
		tables.add("F5");
		*/
		
		/*
		tables.add("5K");
		tables.add("10K");
		tables.add("20K");
		tables.add("50K");
		*/
		//tables.add("100K");
		
		
		Vector<Thread> threads = new Vector<Thread>();
		
		/*
		for (String table : tables){
			threads.add(new RunSimilarityJoinThread(table,ed,0.1));
		}
		
		for (String table : tables){
			threads.add(new RunSimilarityJoinThread(table,tfidf,0.1));
		}
		
		for (String table : tables){
			threads.add(new RunSimilarityJoinThread(table,softtfidf,0.1));
		}
		
		for (String table : tables){
			threads.add(new RunSimilarityJoinThread(table,jaccard,0.1));
		}
		
		for (String table : tables){
			threads.add(new RunSimilarityJoinThread(table,bm25WeightedJaccard,0.1));
		}
		
		for (String table : tables){
			threads.add(new RunSimilarityJoinThread(table,ges,0.1));
		}
		
		 
		
		for (String table : tables){
			threads.add(new RunSimilarityJoinThread(table,hmm,0.1));
		}
		
		for (String table : tables){
			threads.add(new RunSimilarityJoinThread(table,bm25,0.1));
		}
		
		*/
		
		//threads.add(new RunSimilarityJoinThread("cu1",ges,0.1));
		for (Thread thread: threads){
			thread.start();
		}
		for (Thread thread: threads){
			try{
				thread.join();
			} catch (Exception e) {
				System.out.println("Error");
			}
		}
		
		
		
		threads = new Vector<Thread>();
		/* threads.add(new RunClusteringThread("cu1",hmm));
		//threads.add(new RunClusteringThread("cu1",measure,0.0));
		threads.add(new RunClusteringThread("cu1",measure,0.2));
		threads.add(new RunClusteringThread("cu1",measure,0.4));
		threads.add(new RunClusteringThread("cu1",measure,0.6));
		threads.add(new RunClusteringThread("cu1",measure,0.8));
		/*
		threads.add(new RunClusteringThread("cu2",measure,0.6));
		threads.add(new RunClusteringThread("cu3",measure,0.6));
		threads.add(new RunClusteringThread("cu4",measure,0.6));
		threads.add(new RunClusteringThread("cu5",measure,0.6));
		threads.add(new RunClusteringThread("cu6",measure,0.6));
		threads.add(new RunClusteringThread("cu7",measure,0.6));
		threads.add(new RunClusteringThread("cu8",measure,0.6));
		* /
		threads.add(new EvaluateSJoinThread("cu8",measure,0.01));
		threads.add(new EvaluateSJoinThread("cu8",measure,0.15));
		threads.add(new EvaluateSJoinThread("cu8",measure,0.20));
		threads.add(new EvaluateSJoinThread("cu8",measure,0.25)); */
		//threads.add(new EvaluateSJoinThread("cu8",measure,0.30));
		//threads.add(new EvaluateSJoinThread("cu1",measure,0.4));
		//threads.add(new EvaluateSJoinThread("cu1",measure,0.6));
		//threads.add(new EvaluateSJoinThread("cu1",measure,0.8));
		/*
		 * 
		 * 
		 */
		double startThr = 0.1;
		double stopThr = 0.9;
		double step = 0.05;
		
		for (String table : tables){
			double thr = startThr;
			while (thr < stopThr){
				threads.add(new EvaluateSJoinThread(table,measure,thr*range, resultTable));
				thr += step;
			}
		}
		
		for (Thread thread: threads){
			thread.start();
			try{
				thread.join();
			} catch (Exception e) {
				System.out.println("Error");
			}
		}
		
		
		
		
		/*
		Vector<Integer> v = new Vector<Integer>();
		v.add(1);
		v.add(2);
		v.add(3);
		v.add(4);
		System.out.println(v);
		
		boolean[][] correctOrder = new boolean[v.size()][v.size()];
		for (int i=0; i<v.size(); i++)
			for (int j=0; j<v.size(); j++)
				correctOrder[i][j]=false;
		
		int size = 0;
		for (int i=0; i<v.size(); i++){
			for (int j=i+1; j<v.size(); j++){
				size++;
				correctOrder[v.get(i)-1][v.get(j)-1] = true;
				System.out.println(v.get(i) + ", " + v.get(j));
			}
		}
		
		Vector<Integer> v2 = new Vector<Integer>();
		v2.add(4);
		v2.add(3);
		v2.add(1);
		v2.add(2);
		//Collections.sort(v2);
		//v2 = new Vector<Integer>();
		System.out.println(v2);
		
		/*
		HashMap<Integer, Double> probs = new HashMap<Integer, Double>();
		probs.put(1, 0.5);
		probs.put(2, 0.2);
		probs.put(3, 0.2);
		probs.put(4, 0.6);
		
		List mapValues = new ArrayList(probs.values());
		
		Vector<Double> sortedProbs = new Vector<Double>();
		for (Double prob:probs.values()){
			sortedProbs.add(prob);
		}
		Collections.sort(sortedProbs);
		
		
		for (Double prob:sortedProbs){
			//System.out.println(prob);
			int m = mapValues.indexOf(prob);
			System.out.println(m+1);
			mapValues.set(m, -1);
		}
		* /
		
		int correct = 0;
		for (int i=0; i<v2.size(); i++){
			for (int j=i+1; j<v2.size(); j++){
				if (correctOrder[v2.get(i)-1][v2.get(j)-1]) {
					correct++;
					System.out.println(v2.get(i) + ", " + v2.get(j));
				}
			}
		}		
		System.out.println("Percentage: " + correct + "/" + size);
		
		
		
		/*

		int i = -1;
		Integer j = -1;
		Vector<String> vStr = new Vector<String>();
		HashMap<Integer, Double> hm = new HashMap<Integer, Double>();
		System.out.println(i + " " + j + " " + vStr + " " + hm);
		tst(i,j,vStr,hm);
		System.out.println(i + " " + j + " " + vStr + " " + hm);
		
		
		
		/*
		String str1 = "ab";
		String str2 = "$$";
		

		char[] chars = str1.toCharArray();		
		int h1 = str1.charAt (1) << 7 | str1.charAt (0);
		int h2 = str2.charAt (0) << 7 | str2.charAt (1);
		

		System.out.println("hash(" + str1 + ") = " + (h1) );
		System.out.println("hash(" + str1 + ") = " + (h2) );
		
		BitSet b = new BitSet();
		long n = 327544767;
		//for (int i=0; i<32; i++)
		//	System.out.println("bit(" + i + ") = " + ( ((n & (1 << i)) >>> i) == 1 ? 1 : 0   ) );
		
		long one = 1;
		long n2 = (one << 40);
		System.out.println(n2);
		
		for (int i=63; i>=0; i--)
			System.out.print(( ((n2 & (one << i)) >>> i) == 1 ? 1 : 0   ) );
		
	  	*/
		
		/*
		Vector<Integer> m1 = new Vector<Integer>(2);
		m1.add(1);
		m1.add(2);
		m1.add(3);
		
		Vector<Integer> m2 = new Vector<Integer>();
		m2.add(1);
		m2.add(2);
		m2.add(3);
		
		System.out.println(m1.equals(m2));
		* /
		
		
		Set<String> qgrams = new HashSet<String>();
		qgrams.add("ab");
		qgrams.add("df");
		qgrams.add("et");
		qgrams.add("se");
		qgrams.add("df");
		qgrams.add("gh");
		
		System.out.println(qgrams.toString());
		
		BitSet b = convertToBitSet(qgrams);
		System.out.println(b.toString());
		
		qgrams = convertToStringSet(b);
		System.out.println(qgrams.toString());
		
		/*
		
		//System.out.println(1 << 14);
		//HashMap<Integer, Integer> perm = permutation(1 << 14);
		//System.out.println(perm.toString());

		BitSet v = new BitSet();
		int N = 6;
		v.set(2); v.set(4); v.set(5);
		int k=3, n1=2, n2=3;
		int k2 = (k+1)/n1 - 1;
		
		//HashMap<Integer, Integer> perm = permutation(6);
		
		//for (int i=0; i<=N; i++)	perm.put(i, i);
		 * 
		 * /
		perm = permutation(6);
		System.out.println(perm);
		
		Vector<Integer> v = new Vector<Integer>();
		v.add(2); v.add(7); v.add(9);
		System.out.println(subsets(v,2));
		
		/*
		//BitSet[] p = new BitSet[65];
		for (int i=1; i<=n1; i++){
			BitSet bb1 = p(i,1,n1,n2,N);
			BitSet bb2 = p(i,2,n1,n2,N);
			bb1.or(bb2);
			System.out.println(bb1);
			System.out.println(p(i,3,n1,n2,N));
		}
		* /
		
		BitSet test = new BitSet();
		test.set(1, 7, true);
		
		//Vector<Integer> vv = new Vector<Integer>();
		//vv.add(1); vv.add(2); vv.add(3); vv.add(4); vv.add(5); vv.add(6);
		//System.out.println(subsets(vv, 2));

		System.out.println(v.toString());
		
		Vector<Integer> vv = new Vector<Integer>();
		for (int i=1; i<=n2; i++) vv.add(i);
		
		for (int i=1; i<=n1; i++){
			
			for (Vector<Integer> subset:subsets(vv, n2-k2)){
				BitSet P = new BitSet();
				for (Integer j: subset){
					P.or(p(i,j,n1,n2,N));
				}
				
				HashMap<BitSet, BitSet> sign = new HashMap<BitSet, BitSet>();
				BitSet proj = new BitSet();
				proj = (BitSet) v.clone();
				proj.and(P);
				System.out.println("<" + proj + "," + P + ">");
				sign.put(proj, P);
				
				Random rand = new Random();
				Long hash= new Long(0);
				int t = P.nextSetBit(0);
				while (t!=-1){
					rand = new Random(t);
					hash += rand.nextLong();
					t=P.nextSetBit(t+1);
				}
				t = proj.nextSetBit(0);
				while (t!=-1){
					rand = new Random(t);
					hash += rand.nextLong();
					t=proj.nextSetBit(t+1);
				}
				System.out.println(hash);
				//System.out.println((int)(P.hashCode()+proj.hashCode()));
				//System.out.println(P.hashCode());System.out.println(proj.hashCode());
				//System.out.println(sign.hashCode());
			}
			
		}
		    
		*/
		
		

	}

}