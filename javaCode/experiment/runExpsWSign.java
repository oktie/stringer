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

import simfunctions.EvaluateSignThread;
import simfunctions.Preprocess;
import simfunctions.WeightedJaccardBM25wPartEnum;
import utility.Config;
import utility.Util;

public class runExpsWSign {
	
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
		
		/*
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		String resultTable = "signresults";
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
		*/
		
		/*
		Preprocess bm25WeightedJaccardSimHash = new WeightedJaccardBM25wSimhash();
		Preprocess measure = bm25WeightedJaccardSimHash;
		*/
		
		
		Preprocess bm25WeightedJaccardPartEnum = new WeightedJaccardBM25wPartEnum();
		Preprocess measure = bm25WeightedJaccardPartEnum;
		
		
		/*
		Preprocess bm25WeightedJaccardMinHash = new WeightedJaccardBM25wMinhash();
		Preprocess measure = bm25WeightedJaccardMinHash;
		*/
		
		Vector<String> tables = new Vector<String>();
		tables.add("cu1"); 
		/*
		tables.add("cu2"); tables.add("cu3");
		tables.add("cu4"); tables.add("cu5"); tables.add("cu6");
		tables.add("cu7"); tables.add("cu8");
		*/
		
		Vector<Thread> threads = new Vector<Thread>();
		
		for (String table : tables){
			//threads.add(new SignatureGenThread(table,measure,2,2,0));
			//threads.add(new SignatureGenThread(table,measure,2,5,0));
			//threads.add(new SignatureGenThread(table,measure,4,5,0));
			//threads.add(new SignatureGenThread(table,measure,3,7,0));
			threads.add(new SignatureGenThread(table,measure,11,2,6));
		}
		//threads.add(new RunSimilarityJoinThread("cu8",measure,0.1));
		
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
		for (String table : tables){
/*
			threads.add(new EvaluateSignThread(table,measure,2,2,0));
			threads.add(new EvaluateSignThread(table,measure,2,5,0));
			threads.add(new EvaluateSignThread(table,measure,4,5,0));
			threads.add(new EvaluateSignThread(table,measure,3,7,0));
			*/
			threads.add(new EvaluateSignThread(table,measure,5,2,3));
//			threads.add(new EvaluateSignThread(table,measure,11,2,6));

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