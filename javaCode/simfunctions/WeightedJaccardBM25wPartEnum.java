package simfunctions;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Random;
import utility.Config;
import utility.Util;
import dbdriver.MySqlDB;
import experiment.IdScore;

public class WeightedJaccardBM25wPartEnum extends Preprocess {

	double meanIDF=0;
	
	public static HashMap<Integer, Integer> perm = new HashMap<Integer, Integer>();
	
	private static HashMap<String, Integer> qgramNumber = new HashMap<String, Integer>();
	public static void setQgramNumbers(Set<String> qgrams){
		int i = 1;
		for (String qgram: qgrams){
			qgramNumber.put(qgram, i);
			i++;
		}
	}
	public static BitSet convertToBitSet(Set<String> stringSet){
		BitSet output = new BitSet();
		for (String qgram : stringSet) {			
			output.set(qgramNumber.get(qgram));
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
		
		Random rand = new Random(23453);
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
			Vector<HashMap<String, Double>> recordTokenWeights,
			HashMap<Integer, Long> signature) {
		Long sign = new Long(0);
		HashMap<String, Double> tokenTF = getTFWsign(str, sign);
		signature.put(recordId, sign);
		recordTokenWeights.insertElementAt(tokenTF, recordId); // OKTIE: ???
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

	
	public int hash(String str){
		int h = str.charAt (1) << 7 | str.charAt (0);
		return h;
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

	public double weightedJaccard(String s, String t, HashMap<String, Double> qgramIDF, double meanIDF){
		//System.out.println("wj called");
		double sizeS = 0;
		double weightedJaccardScore=0;
		Set<String> setS = gettokenTFSingle(s).keySet();
		sizeS = setS.size();
		double weightedSumS = Util.getWeightedSumForTokenSet(setS, qgramIDF, meanIDF);
		Set<String> setT = gettokenTFSingle(t).keySet();
		double weightedSumT = Util.getWeightedSumForTokenSet(setT, qgramIDF, meanIDF);
		
		Util.printlnDebug(setS);
		Util.printlnDebug(setT);
		
		setS.retainAll(setT);
		Util.printlnDebug(setS);
		double weightedSumSandT = Util.getWeightedSumForTokenSet(setS, qgramIDF, meanIDF);
		if(setS.size() > 0){
			weightedJaccardScore = weightedSumSandT / (weightedSumS + weightedSumT - weightedSumSandT);
		}
		Util.printlnDebug("Weighted Jaccard Score: "+weightedJaccardScore);
		Util.printlnDebug("Jaccard : "+( setS.size()/ (sizeS + setT.size() - setS.size())));
		return weightedJaccardScore;
	}
	
	

	public void preprocessTableWSign(Vector<String> records, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights, 
			HashMap<Integer, Long> signature,
			Integer par1,
			Integer par2,
			Integer par3,
			String tableName) {
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
						getDFandTFweight(k - 1, str, qgramIDF, recordTokenWeights, signature);
					}
					numberOfRecords++;
					//System.out.println(numberOfRecords);
				}
			}
			//mysqlDB.close();
		} catch (Exception e) {
			System.out.println("database error: cannot read table");
			e.printStackTrace();
		}
		//convert the Df to IDF
		convertDFtoIDF(qgramIDF, numberOfRecords);
		//recordTokenWeights.clear();
		
		setQgramNumbers(qgramIDF.keySet());

		//int k1=5, n1=2, n2=3;
		int k1=par1, n1=par2, n2=par3;
		int k2 = (k1+1)/n1 - 1;
		int N = qgramNumber.values().size();
		System.out.println();
		System.out.println(N);
		System.out.println();
		perm = permutation(N<<1);	
		
		
		
		
		
		
		
		
		
		
		boolean debug_mode = true;
		

		/*
		 * Log signature table to DB - Create Signature Table
		 */
		//String sigTable = "sign";
		String sigTable = "signs_" + tableName + "_" + Preprocess.extractMetricName(this.getClass().getName()) + "_" + par1 + "_" + par2 + "_" + par3;
		boolean log_sig_to_db = true;
		StringBuffer log_query = new StringBuffer("");
		
		/*
		 *  LOGGING SIGNATURE VALUES TO THE SIGNATURE TABLE
		 *  PART 1 - Create Table
		 */
		if (log_sig_to_db) {
			try {				
				String query = "drop table if exists " + config.dbName + "." + sigTable;
				mysqlDB.executeUpdate(query);

				query = "create table " + config.dbName + "." + sigTable 
						+ " (tid int, sign int)";
				mysqlDB.executeUpdate(query);
				log_query.append("INSERT INTO " + config.dbName + "." + sigTable + " values "); 
				
			} catch (Exception e) {
				System.err.println("Can't create signature log tables");
				e.printStackTrace();
			}
			
		}


		//Long oldSig = new Long(0);
		
		
		try {
			k = 0;
			numberOfRecords = 0;
			while (k < records.size()) {
				k++;

				
				HashMap<String, Double> recordsQgram = recordTokenWeights.get(k-1);
				// for each qgram (set element):
				Set<String> qgrams = recordsQgram.keySet();
				//System.out.println(qgrams.toString());
				BitSet v = convertToBitSet(qgrams);
				//System.out.println(b.toString());
				//qgrams = convertToStringSet(b);
				//System.out.println(qgrams.toString());
				
				//System.out.println(1 << 14);
				//HashMap<Integer, Integer> perm = permutation(1 << 14);
				//System.out.println(perm.toString());

				//HashMap<Integer, Integer> perm = permutation(6);
				
				//for (int i=0; i<=N; i++)	perm.put(i, i);
				
				//System.out.println(perm);
				//System.out.println(v.toString());
				
				Integer sig = new Integer(0);
				
				Vector<Integer> vv = new Vector<Integer>();
				for (int i=1; i<=n2; i++) vv.add(i);
				int TEMP =0;
				for (int i=1; i<=n1; i++){
					for (Vector<Integer> subset:subsets(vv, n2-k2)){
						BitSet P = new BitSet();
						for (Integer j: subset){
							P.or(p(i,j,n1,n2,N));
						}
						
						TEMP ++;
						
						HashMap<BitSet, BitSet> sign = new HashMap<BitSet, BitSet>();
						BitSet proj = new BitSet();
						proj = (BitSet) v.clone();
						proj.and(P);
						//System.out.println("<" + proj + "," + P + ">");
						sign.put(proj, P);
						//System.out.println((int)(P.hashCode()+proj.hashCode()));
						//System.out.println(P.hashCode());System.out.println(proj.hashCode());
						//System.out.println(sign.hashCode());
						
						//sig = (long)(P.hashCode()+ proj.hashCode());
						
						Random rand = new Random();
						Integer hash = new Integer(0);
						int t = P.nextSetBit(0);
						while (t!=-1){
							rand = new Random(t);
							if (rand.nextBoolean()) hash += 2*rand.nextInt() + 1;
							else hash -= 5*rand.nextInt() + 1;
							t=P.nextSetBit(t+1);
						}
						t = proj.nextSetBit(0);
						while (t!=-1){
							rand = new Random(t);
							if (rand.nextBoolean()) hash += rand.nextInt() + 1;
							else hash -= 2*rand.nextInt() + 1;
							t=proj.nextSetBit(t+1);
						}
						
						sig = hash;
						
						/*
						rand = new Random(TEMP);
						sig = rand.nextLong();
						*/
						
						//sig = ( ((long)(P.hashCode())) << 32 ) | (long)(proj.hashCode());
						
						/*
						 *  LOGGING SIGNATURE VALUES TO THE SIGNATURE TABLE
						 *  Part 2 - Log signature table to DB - INSERT value of the fingerprint in the sign. table
						 *           Actually adding the '(tid, sign),' to the end of the query string 
						 */
						if (log_sig_to_db) {
							
							try {
								log_query.append(" (" + (numberOfRecords+1) + "," + sig + ") ,");
							} catch (Exception e) {
								System.err.println("Can't execute Query ");
								e.printStackTrace();
							}
							
						}
					
					}
					
				}
				
				
			

					
					


				
				
				
				//System.out.println(numberOfRecords+1 + " " + sig );
				
				if (debug_mode) {
					System.out.print((numberOfRecords+1) + " " );
					//Long sigD = new Long(sig - oldSig);
				
					System.out.println(sig);
				
					//oldSig = sig;
				}
				
				
				signature.put(k-1, (long)sig);
				
				/*
				 *  LOGGING SIGNATURE VALUES TO THE SIGNATURE TABLE
				 *  Part 3 - Log signature table to DB
				 *   INSERT value of the fingerprint in the sign. table for every 10,000 values
				 */
				if ((log_sig_to_db)&&(numberOfRecords % 1000 == 999)) {
					try {				
						mysqlDB.executeUpdate(log_query.deleteCharAt(log_query.length()-1).toString());
						//System.out.println(log_query.deleteCharAt(log_query.length()-1));
						log_query = new StringBuffer("INSERT INTO " + config.dbName + "." + sigTable + " values "); 
						
					} catch (Exception e) {
						System.err.println("Can't insert into signature log tables");
						e.printStackTrace();
					}
				}
				
				
				numberOfRecords++;
				//System.out.println(numberOfRecords);
			}

			/*
			 *  LOGGING SIGNATURE VALUES TO THE SIGNATURE TABLE
			 *  Part 4 - Log signature table to DB
			 *   INSERT all remaining values of the fingerprint in the sign. table.
			 */				
			//System.out.println(log_query.deleteCharAt(log_query.length()-1));
			mysqlDB.executeUpdate(log_query.deleteCharAt(log_query.length()-1).toString());

			
			mysqlDB.close();
			
		} catch (Exception e) {
			System.out.println("cannot create signatures");
			e.printStackTrace();
		}
	}








}




