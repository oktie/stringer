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

public class WeightedJaccardBM25wMinhash extends Preprocess {

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

	
		
		
		
		
		
		boolean debug_mode = false;
		

		/*
		 * Log signature table to DB - Create Signature Table
		 */
		String sigTable = "signs_" + tableName + "_" + Preprocess.extractMetricName(this.getClass().getName()) + "_" + par1 + "_" + par2 + "_" + par3;
		//System.out.println(sigTable);
		
		//String sigTable = "sign";
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
						+ " (tid int, sign long)";
				mysqlDB.executeUpdate(query);
				log_query.append("INSERT INTO " + config.dbName + "." + sigTable + " values "); 
				
			} catch (Exception e) {
				System.err.println("Can't create signature log tables");
				e.printStackTrace();
			}
		}


		//Long oldSig = new Long(0);
		
		// 'l' is the number of LSH signatures
		int l = par1; 
		//System.out.println("l:" + par1);
		// 'm' is the number of minhash signatures
		int m = par2;
		//System.out.println("m:" + par2);
		
		try {
			k = 0;
			numberOfRecords = 0;
			//System.out.println(records.size());
			while (k < records.size()) {
				k++;
				
				
				Integer[][] hashP = 
				 {
						{2203,2207,2213,2221,2237,2239,2053},
						{1999,2003,2011,2017,2027,2029,2039},
						{2063,2069,2081,2083,2087,2089,2099},
						{2113,2129,2131,2137,2141,2143,2221}
				 };
				//  2153 2161 2179 2203 2207 2213  2237 2239 2243 2251 2267 2269 2273 2281
				
				for (int lh=0; lh < l; lh++){


					Vector<Integer> MH = new Vector<Integer>(m);
					HashMap<String, Double> recordsQgram = recordTokenWeights.get(k-1);
					//
					// for each qgram (set element):
					// hash the qgram (set element) with 'm' different hash functions 
					// put the hash value in list of hash values for the record
					// we will have 'm' list of hash values
					//
					Integer[][] hash_list = new Integer[m][recordsQgram.keySet().size()];
					for (Integer[] hlist : hash_list){
						for (int d=0; d<hlist.length; d++){
							hlist[d] = 0;
						}
					}
					int q=0;
					for (String qgram : recordsQgram.keySet()) {
						for (int mh=0; mh<m; mh++){
							// (How to hash?)
							//int ggramHash = hash(qgram);
							//long qgramHash = ( (qgram.charAt(2) << 14) | (qgram.charAt(1) << 7) | qgram.charAt(0));
							int qgramHash = ( (qgram.charAt(1) << 7) | qgram.charAt(0));
							//int qgramHash =  101*qgram.charAt(1) + 5*qgram.charAt(0);
							//qgramHash = (int) (( ((double)qgramHash - 12000.0) / 15300.0 ) * Integer.MAX_VALUE);

							//Random rand = new Random(qgramHash*(mh+1)+(lh+1));
							//qgramHash = rand.nextInt();
							//rand = new Random(qgramHash);
							//qgramHash = Math.abs((rand.nextInt() * qgramHash + rand.nextInt())) % 2039;
							qgramHash = Math.abs( qgramHash*Integer.MAX_VALUE % hashP[lh][mh] );

							//System.err.println(qgramHash);
							//System.loadLibrary("NumberList");
						
							hash_list[mh][q] = qgramHash;
							
						}
						q++;
					}
					
					//
					// Finding minimum hash values for each of 'm' lists
					// we will have 'm' values for each record then
					//
					Integer[] min = new Integer[m];
					for (int d=0; d<m; d++) min[d]=0;
					for (int d1=0; d1<m; d1++){
						min[d1] = hash_list[d1][0];
						//for (Integer h:hlist){
						for (int d2=0; d2<hash_list[d1].length; d2++){
							if (hash_list[d1][d2] < min[d1]) min[d1] = hash_list[d1][d2];
							//System.out.println(hlist[d]);
						}
					}
					
					Long sig = new Long(0);
					
					int f = 12; // max number of bits of each hash value
					for (int i=0; i<m; i++) {						
						sig = (sig << f) | min[i];
						//System.out.print(min[i] + " ");
					}
					
					
					/*
					long one = 1;
					for (int i=0; i<f; i++) {						
						if (V[i] < 0) sig = sig | (one << i);
						//System.out.print(V[i] + " ");
					}
					*/
					//System.out.println();
					
	
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
					
					
					
					//System.out.println(numberOfRecords+1 + " " + sig );
					
					if (debug_mode) {
						System.out.print((numberOfRecords+1) + " " + sig );
					
						System.out.println();
					}
					
					
					signature.put(k-1, sig);
					
					/*
					 *  LOGGING SIGNATURE VALUES TO THE SIGNATURE TABLE
					 *  Part 3 - Log signature table to DB
					 *   INSERT value of the fingerprint in the sign. table for every 10,000 values
					 */
					if ((log_sig_to_db)&&(numberOfRecords % 10000 == 9999)) {
						try {				
							mysqlDB.executeUpdate(log_query.deleteCharAt(log_query.length()-1).toString());
							//System.out.println(log_query.deleteCharAt(log_query.length()-1));
							log_query = new StringBuffer("INSERT INTO " + config.dbName + "." + sigTable + " values "); 
							
						} catch (Exception e) {
							System.err.println("Can't insert into signature log tables");
							e.printStackTrace();
						}
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
	
	
}
