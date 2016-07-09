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


import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.Set;
import java.util.Random;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import dbdriver.MySqlDB;
import evaluation.AccuracyMeasure;

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

public class runSimilarityJoin {
	
	public static HashMap<Integer, Integer> perm = new HashMap<Integer, Integer>();

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

	
	public int hash(String str){
		int h = Integer.valueOf(str);
		return h;
	}
	
	public static BitSet convertToBitSet(Set<String> stringSet){
		BitSet output = new BitSet();
		int c1,c2;
		HashMap<Integer,Integer> pr = permutation((1<<7) -1, 9573);
		for (String qgram : stringSet) {			
			c1 = qgram.charAt(1);
			c2 = qgram.charAt(0);
			c1 = pr.get(c1);
			c2 = pr.get(c2);
			output.set((c1 << 7) | c2);			
		}
		return output;
	}
	
	public static Set<String> convertToStringSet(BitSet bitset){
		Set<String> output = new HashSet<String>();
		int i = bitset.nextSetBit(0);
		while (i != -1){
			char c1 = (char)(127 & i);
			char c2 = (char)(((127 << 7) & i) >> 7);
			String qgram = new String();
			qgram = qgram + c1;
			qgram = qgram + c2;
			output.add(qgram);
			i = bitset.nextSetBit(i+1);
		}
		return output;
	}
	
	public static HashMap<Integer, Integer> permutation(int n){
		HashMap<Integer, Integer> output = new HashMap<Integer, Integer>();
		
		BitSet allnums = new BitSet();
		allnums.set(1, n+1, true);
		
		Random rand = new Random(2353);
		int k=1;
		while (!allnums.isEmpty()){
			int i = rand.nextInt(n);
			//System.out.print(k + " " + i + " ");
			while (!allnums.get(i)) {
				if (i > n) i=0;
				i++;
			}
			allnums.clear(i);
			//System.out.println(i);
			output.put(k, i);
			//System.out.println(output.get(k));
			k++;
		}
		
		return output;
	}
	
	public static HashMap<Integer, Integer> permutation(int n, int rnd){
		HashMap<Integer, Integer> output = new HashMap<Integer, Integer>();
		
		BitSet allnums = new BitSet();
		allnums.set(1, n+1, true);
		
		Random rand = new Random(rnd);
		int k=1;
		while (!allnums.isEmpty()){
			int i = rand.nextInt(n);
			//System.out.print(k + " " + i + " ");
			while (!allnums.get(i)) {
				if (i > n) i=0;
				i++;
			}
			allnums.clear(i);
			//System.out.println(i);
			output.put(k, i);
			//System.out.println(output.get(k));
			k++;
		}
		
		return output;
	}
	
	public static int b(int i, int j, int n1, int n2, int N){
		int n = N + ( (N % (n1*n2) == 0) ? 0 : ((n1*n2)-(N%(n1*n2))) ); // make N divisable by n1xn2
		return n * ( n2*(i-1)+j-1 ) / (n1*n2) + 1; // ?? +1
	}
	public static int e(int i, int j, int n1, int n2, int N){
		int n = N + ( (N % (n1*n2) == 0) ? 0 : ((n1*n2)-(N%(n1*n2))) ); // make N divisable by n1xn2
		return n * ( n2*(i-1)+j ) / (n1*n2) + 1; // ?? +1
	}
	public static BitSet p(int i, int j, int n1, int n2, int N){
		BitSet output = new BitSet();
		//System.out.println("i:" + i + " j:" + j + " e:"+ e(i,j,n1,n2,N));
		for (int t = b(i,j,n1,n2,N); t< e(i,j,n1,n2,N); t++){
			output.set(perm.get(t));
		}
		return output;
	}
	
	public static Vector<Vector<Integer>> subsets(Vector<Integer> set, int size){
		Vector<Vector<Integer>> output = new Vector<Vector<Integer>>();
		//System.out.println();
		
		if (size == 1) {
			for (Integer vi: set){
				Vector<Integer> v = new Vector<Integer>();
				v.add(vi);
				output.add(v);
			}
		}
		else {
			for (int i = 1; i <= set.size()-(size-1); i++){
				Vector<Integer> set2 = new Vector<Integer>();
				for (int j = i+1; j <= set.size(); j++) set2.add(set.elementAt(j-1));
				for (Vector<Integer> sset : subsets(set2, size-1)){
					Vector<Integer> v = new Vector<Integer>();
					v.add(set.elementAt(i-1));
					for (Integer vi:sset) v.add(vi);
					output.add(v);
				}
			}
		}

		return output;
	}
	
	public static void tst(int i, Integer j, Vector<String> vStr, HashMap<Integer, Double> hm){
		i = 1;
		j = 0;
		vStr.add("ey baba!");
		hm.put(1, 2.2);
	}
	
	class pair {
	    private float x;
	    private float y;

	    public pair(float x, float y) {
	        this.x = x;
	        this.y = y;
	    }

	    public float getX() { return x; }
	    public float getY() { return y; }

	    public void setX(float x) { this.x = x; }
	    public void setY(float y) { this.y = y; }
	}
	
	public static void main(String[] args) {
		
		
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		
		
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
		Preprocess measure = bm25WeightedJaccard;
		
		double range = 2.0;
		
		
		String datasetsTable = "datasets";
		
		Vector<String> tables = new Vector<String>();
		HashMap<String, String> tableclass = new HashMap<String, String>();
		String query = "";
		try {
		
			query = "SELECT * FROM "
					+ datasetsTable + " T ";// + " WHERE XXX ";
			//System.out.println("Executing " + query);
			ResultSet rs = mysqlDB.executeQuery(query);
			if (rs != null) {
				while (rs.next()) {
					tables.add(rs.getString(1));
					tableclass.put(rs.getString(1),rs.getString(2));
				}
			}
			mysqlDB.close();
		} catch (Exception e) {
			System.err.println("Can't generate the query");
			e.printStackTrace();
		}
		
		/*
		tables.add("l1"); tables.add("l2");
		tableclass.put("l1", "low"); tableclass.put("l2", "low");
		
		tables.add("m1");	tables.add("m2"); tables.add("m3"); tables.add("m4");
		tableclass.put("m1", "med"); tableclass.put("m2", "med"); tableclass.put("m3", "med"); tableclass.put("m4", "med");
		
		tables.add("h1"); tables.add("h2");
		tableclass.put("h1", "high"); tableclass.put("h2", "high");
		*/
		
		System.out.println(tables);
		System.out.println(tableclass);
		
				
		
		
		
		
		//tables.add("test");
		
		/*
		tables.add("F1"); tables.add("F2");
		tables.add("F3"); tables.add("F4");
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
		
		for (String table : tables){
			// Self join:
			threads.add(new simfunctions.RunSimilarityJoinThread(table, table,bm25WeightedJaccard,0.1));
		}
		
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
		

	}

}