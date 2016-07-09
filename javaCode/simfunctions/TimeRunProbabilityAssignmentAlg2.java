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
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import dbdriver.MySqlDB;
import utility.Config;

public class TimeRunProbabilityAssignmentAlg2 {

	public static boolean debug_mode = true;
	public static int queryTokenLength = 2;
	public static boolean replaceSpaceWithSpecialCharacter = true; 
	public static int qgramSize = 2;

	public static String generateSpecialCharacters(int qgSize){
		String str="";
		for(int i=0; i < qgSize-1; i++)	str += "#";
		return str;
	}
	
	public static void incrementCount(String qgram, HashMap<String, Double> tokenCount) {
		if (tokenCount.containsKey(qgram)) {
			tokenCount.put(qgram, tokenCount.get(qgram) + 1);
		} else {
			tokenCount.put(qgram, 1.0);
		}
	}
	
	// Tokenization function
	public static HashMap<String, Double> getTF(String str) {
		HashMap<String, Double> tokenTF = new HashMap<String, Double>();
		/*
		 * String[] tokens = str.split("\\s+"); for (int i = 0; i <
		 * tokens.length; i++) { incrementCount(tokens[i], tokenTF); }
		 */
		str = str.toLowerCase();
		if(replaceSpaceWithSpecialCharacter){
			str = str.replaceAll(" ", generateSpecialCharacters(qgramSize));
			str = generateSpecialCharacters(qgramSize) + str + generateSpecialCharacters(qgramSize); 
		}
		for (int i = 0; i <= str.length() - qgramSize; i++) {
			String qgram = str.substring(i, i + qgramSize);
			incrementCount(qgram, tokenTF);
		}
		return tokenTF;
	}
	
	
	public static BitSet convertToBitSet(Set<String> stringSet){
		BitSet output = new BitSet();
		for (String qgram : stringSet) {
			output.set((qgram.charAt(1) << 7) | qgram.charAt(0));			
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
	
	
	public static HashMap<Integer, Double> getProbabilitiesAlg2(HashMap<Integer, String> strs,
			Preprocess metric, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights){
		
		int size = 0;
		
		
		/*
		 * 
		 * Finding Cluster Representative
		 * 
		 */
		
		double max_sum = -Double.MAX_VALUE;
		int max_tid = 0;
		for (int sn:strs.keySet()){
			String str1 = strs.get(sn);
			double sum = 0.0;
			for (int tid:strs.keySet()){
				String str2 = strs.get(tid);
				double score = metric.getSimilarityScore(str1, qgramIDF, recordTokenWeights, tid, str2);
				//System.out.println(str1 + " |&&| " + str2 + " : " + score);
				sum += score;
			}
			if (sum>max_sum) {
				max_sum = sum;
				max_tid = sn;
			}
			//System.out.println(str1);
		}
		//System.out.println();
		//System.out.println(max_tid + " " + strs.get(max_tid));
		//System.out.println();
		
		String rep = strs.get(max_tid);
		
		/*
		 * 
		 * Finding similarity of rep. to tuples and calculating probabilities
		 * 
		 */

		HashMap<Integer, Double> probs = new HashMap<Integer, Double>();
		Double sum = 0.0;
		//Random rand = new Random(280);
		for (int tid:strs.keySet()){
			String str = strs.get(tid); 
			double sim = metric.getSimilarityScore(rep, qgramIDF, recordTokenWeights, tid, str);

			//if (debug_mode) {System.out.println(str + " " + (sim/strSet.cardinality()));}
			//System.out.println(str + " " + (sim));
			//probs.put(tid,sim/rep.length());
			probs.put(tid,sim);
			sum += sim;
			//probs.put(sn,rand.nextDouble());
		}

		for (int sn:strs.keySet()){
			//String str = strs.get(sn); 
			probs.put(sn, probs.get(sn)/sum);
			if (debug_mode) {System.out.println(strs.get(sn) + " " + probs.get(sn));}
		}
		if (debug_mode) {System.out.println();}
		
		return probs;
		
	}
	
	public static void main(String[] args) {

		String tablename = "100K";
		//String pairTable = "pairs";
		String probTable = "probs";
		boolean log_sig_to_db = true;
		boolean show_times = true;
		
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		
		long t1, t2, t3, t4, t5, tf;
		
		/*
		 * 
		 * Call Similarity Join
		 * 
		 */
		//t1 = System.currentTimeMillis();
		//RunSignatureGenWSingleSimilarityMatch.run(tablename);
		//t2 = System.currentTimeMillis();
		//System.out.println("Similarity Join (Signature Generation): " + (t2-t1) + "ms");
		
		
		Preprocess metric = null;
		Vector<Preprocess> preprocessVector = new Vector<Preprocess>(); // List of metrics for preprocessing the base relation
		
		Vector<Vector<HashMap<String, Double>>> basetableTokenWeightsVector = new Vector<Vector<HashMap<String, Double>>>();
		Vector<HashMap<String, Double>> qgramIDFVector = new Vector<HashMap<String, Double>>();
	

		
		Preprocess tfidf = new TfIdf();
		Preprocess bm25 = new BM25();
		Preprocess hmm = new HMM();
		//Preprocess ed = new EditDistance();
		Preprocess ges = new GeneralizedEditSimilarity();
		Preprocess softtfidf = new SoftTfIdf();
		Preprocess fms = new ApproximateGES();
		//Preprocess weightedJaccard = new WeightedJaccard();
		//Preprocess jaccard = new Jaccard();
		//Preprocess weightedIntersect = new WeightedIntersect();
		//Preprocess intersect = new Intersect();
		Preprocess bm25WeightedJaccard = new WeightedJaccardBM25();
		//Preprocess bm25weightedIntersect = new WeightedIntersectBM25();
		
		//preprocessVector.add(bm25WeightedJaccard);
		
		preprocessVector.add(tfidf);
		preprocessVector.add(bm25); 
		preprocessVector.add(hmm);
		//preprocessVector.add(ed);
		preprocessVector.add(ges);
		preprocessVector.add(softtfidf);
		preprocessVector.add(fms); 
		//preprocessVector.add(weightedJaccard);
		//preprocessVector.add(jaccard); 
		//preprocessVector.add(weightedIntersect);
		//preprocessVector.add(bm25weightedIntersect);
		//preprocessVector.add(intersect);
		
		
		/*
		Preprocess bm25WeightedJaccard = new WeightedJaccardBM25();
		preprocessVector.add(bm25WeightedJaccard);
		Preprocess hmm = new HMM();
		preprocessVector.add(hmm);
		*/
		
		/*
		 * PREPROCESSING PHASE
		 */
		Vector<String> files = new Vector<String>();
		for (Preprocess preprocess: preprocessVector) {
			// Preprocess.tokenizeUsingQgrams = true;
			files.clear();
			files = new Vector<String>();
			Vector<HashMap<String, Double>> baseTableTokenWeights = new Vector<HashMap<String, Double>>();
			HashMap<String, Double> qgramIDF = new HashMap<String, Double>();
			t1 = System.currentTimeMillis();
			// Toggle this variable to log the preprocessing info to DB
			Preprocess.setLogToDB(false);
			//preprocessVector.get(i).preprocessTable(files, qgramIDF, baseTableTokenWeights, args[0]);
			preprocess.preprocessTable(files, qgramIDF, baseTableTokenWeights, tablename);
			t2 = System.currentTimeMillis();
			long prepTime = t2 - t1;
			System.out.println(preprocess.getClass().getName() + "  Preprocessing Done: took " + prepTime
					+ " ms");
			basetableTokenWeightsVector.addElement(baseTableTokenWeights);
			qgramIDFVector.addElement(qgramIDF);
			//// System.gc();
		}		
		System.out.println("Preprocessing complete!\n");
		for (int pj = 0; pj < preprocessVector.size(); pj++) {
			// Preprocess.tokenizeUsingQgrams = true;
			metric = preprocessVector.get(pj);
			Vector<HashMap<String, Double>> baseTableTokenWeights = basetableTokenWeightsVector.get(pj);
			HashMap<String, Double> qgramIDF = qgramIDFVector.get(pj);
		
		
	
			/*
			 * 
			 * Finding True Clusters
			 * 
			 */
			//HashMap<Integer,Integer> trueCluster = new HashMap<Integer,Integer>();
			//HashMap<Integer,BitSet> trueMembers = new HashMap<Integer,BitSet>();
			//RunClustering.findTrueClusters(tablename, trueCluster, trueMembers);
	
	
			
			//tf = System.currentTimeMillis();
			//if (debug_mode) { System.out.println("Total Time for finding clusters: " + (tf-t1) + "ms" + "\n"); };
	
			String sql = "";
			ResultSet rs;
			//HashMap<Integer, Integer> tid21ton = new HashMap<Integer, Integer>();
			HashMap<Integer, HashMap<Integer, String>> records = new HashMap<Integer, HashMap<Integer, String>>();
			try {
				sql = " SELECT c.tid, c.id, c.string " + //, c.errorp  " +
					  " FROM " + config.dbName + "." + tablename + " c " +
				      " order by id "; //, errorp ";
				rs = mysqlDB.executeQuery(sql);
				
				int cId = 1;
				HashMap<Integer, String> cStrings = new HashMap<Integer, String>();
				int ctid = 0;
				//System.out.println();
				while (rs.next()){
					//System.out.println(rs.getString(1));
					if (cId != rs.getInt(2)){
						records.put(cId, cStrings);
						cId = rs.getInt(2); 
						cStrings = new HashMap<Integer, String>();
						cStrings.put(rs.getInt(1), rs.getString(3));
						ctid = 1;
					}
					else{
						cStrings.put(rs.getInt(1), rs.getString(3));
						ctid++;
					}
					//tid21ton.put(rs.getInt(1), ctid);
				}
				//System.out.println();
				records.put(cId, cStrings);
				//System.out.println();
				//System.out.println(records);
				//System.out.println();
			} catch (Exception e) {
				System.out.println("Database error"); e.printStackTrace();
			}
			
			
			
			/*
			 *  LOGGING PROBABILITY VALUES TO THE PROBABILITY TABLE
			 *  PART 1 - Create Table
			 */
			StringBuffer log_query = new StringBuffer("");
			if (log_sig_to_db) {
				try {				
					String query = "drop table if exists " + config.dbName + "." + probTable;
					mysqlDB.executeUpdate(query);
	
					query = "create table " + config.dbName + "." + probTable 
							+ " (tid int, id int, prob double)";
					mysqlDB.executeUpdate(query);
					log_query.append("INSERT INTO " + config.dbName + "." + probTable + " values "); 
					
				} catch (Exception e) {
					System.err.println("Can't create probabilities log tables");
					e.printStackTrace();
				}
				
			}
			
			/*
			 * 
			 * CALCULATING PROBABILITIES
			 * 
			 */
		
			int numberOfRecords = 0;
			long totalT = 0;
			
			t1 = System.currentTimeMillis();
			for (int cId: records.keySet()){
				HashMap<Integer,String> strs = records.get(cId);
				
				debug_mode = false;		
				long tt=System.currentTimeMillis();
				HashMap<Integer,Double> probs = getProbabilitiesAlg2(strs, metric,
						                         qgramIDF, baseTableTokenWeights);
				totalT += (System.currentTimeMillis() - tt);
				debug_mode = true;
	
				
				/*
				 *  LOGGING PROBABILITY VALUES TO THE PROBABILITY TABLE
				 *  Part 2 - Log probability table to DB - INSERT value of the probability in the prob. table
				 *           Actually adding the '(tid, id, prob),' to the end of the query string 
				 */
				//int j = probs.keySet().size(); //TODO: check
				if (log_sig_to_db) {
					for (int tid:probs.keySet()){
						numberOfRecords++;
						log_query.append(" (" + (tid) + "," + cId + "," + probs.get(tid) + ") ,");
					}
				}
				
				/*
				 *  LOGGING PROBABILITY VALUES TO THE PROBABILITY TABLE
				 *  Part 3 - Log probability table to DB
				 *   INSERT value of the probability in the prob. table for every 10,000 values
				 */
				if ((log_sig_to_db)&&(numberOfRecords % 10000 == 9999)) {
					try {				
						mysqlDB.executeUpdate(log_query.deleteCharAt(log_query.length()-1).toString());
						//System.out.println(log_query.deleteCharAt(log_query.length()-1));
						log_query = new StringBuffer("INSERT INTO " + config.dbName + "." + probTable + " values "); 
					} catch (Exception e) {
						System.err.println("Can't insert into probability log tables");
						e.printStackTrace();
					}
				}
				
			}
			tf = System.currentTimeMillis();
			//System.out.println("Total Time for prob assignment: " + (tf-t1) + "ms");
			System.out.print(metric + " ");
			System.out.println("Total Time for prob assignment: " + (totalT) + "ms");
			
			/*
			 *  LOGGING ...
			 *  Part 4 - Log probability table to DB
			 *   INSERT all remaining values of the probabilities in the prob. table.
			 */
			//System.out.println(log_query.deleteCharAt(log_query.length()-1));
			try {
				mysqlDB.executeUpdate(log_query.deleteCharAt(log_query.length()-1).toString());
			} catch (Exception e) {
				System.err.println("Can't execute Query ");
				e.printStackTrace();
			}
			
			
			
			
			
			/*
			 * 
			 * 
			 * Evaluation
			 * 
			 * 
			 * /
			
			Double sumPr = 0.0;
			int clusterCount = 0;
			
			ResultSet rs2;
			HashMap<Integer, Integer> tid21ton = new HashMap<Integer, Integer>(); // tid21ton Stores the sequence number of each tid in its cluster
			try {
				sql = " SELECT p.tid, p.id, c.`string`, c.errorp, p.prob " +
					  " FROM " + config.dbName + "." + probTable + " p, " + config.dbName + "." + tablename + " c" +
					  " where c.tid = p.tid " +
					  " order by id, prob desc, rand()";
				
				rs = mysqlDB.executeQuery(sql);
				
				sql = " SELECT p.tid, p.id, c.`string`, c.errorp, p.prob " +
					  " FROM " + config.dbName + "." + probTable + " p, " + config.dbName + "." + tablename + " c" +
				      " where c.tid = p.tid " +
				      " order by id, errorp, tid";
				
				rs2 = mysqlDB.executeQuery(sql);
				
				int cId = 1;
				HashMap<Integer, String> cStrings = new HashMap<Integer, String>();
				int ctid = 0;
	
				// Reading data and storing strings in each cluster
				while (rs2.next()){
					if (cId != rs2.getInt(2)){
						cId = rs2.getInt(2); 
						ctid = 1;
					}
					else{
						ctid++;
					}
					tid21ton.put(rs2.getInt(1), ctid);
				}
				
				// Vector 'corr' stores correct order of results
				// Vector 'returned' stores the returned order
				Vector<Integer> corr = new Vector<Integer>();
				Vector<Integer> returned = new Vector<Integer>();
				rs.next();
				cId = rs.getInt(2); 
				ctid = 1;
				corr.add(ctid);
				returned.add(tid21ton.get(rs.getInt(1)));
				while (rs.next()){
					if (cId != rs.getInt(2)){
						cId = rs.getInt(2); 
						ctid = 1;
						
						boolean[][] correctOrder = new boolean[corr.size()][corr.size()];
						for (int i=0; i<corr.size(); i++)
							for (int j=0; j<corr.size(); j++)
								correctOrder[i][j]=false;
						
						int size = 0;
						for (int i=0; i<corr.size(); i++){
							for (int j=i+1; j<corr.size(); j++){
								// TODO: if scores are equal then do not set to true
								size++;
								correctOrder[corr.get(i)-1][corr.get(j)-1] = true;
								//System.out.println(v.get(i) + ", " + v.get(j));
							}
						}
						int correct = 0;
						for (int i=0; i<returned.size(); i++){
							for (int j=i+1; j<returned.size(); j++){
								if (correctOrder[returned.get(i)-1][returned.get(j)-1]) {
									correct++;
									//System.out.println(v2.get(i) + ", " + v2.get(j));
								}
							}
						}		
						//System.out.println("Percentage: " + correct + "/" + size);
						sumPr += (((double)correct) / ((double)size));
						clusterCount++;
						//System.out.println(corr);
						//System.out.println(returned);
						
						corr = new Vector<Integer>();
						returned = new Vector<Integer>();
						corr.add(ctid);
					}
					else{
						ctid++;
						corr.add(ctid);
					}
					returned.add(tid21ton.get(rs.getInt(1)));
				}
				//System.out.println();
				//records.put(cId, cStrings);
				//System.out.println(tid21ton);
				//System.out.println();
				//System.out.println(records);
				//System.out.println();
			} catch (Exception e) {
				System.out.println("Database error"); e.printStackTrace();
			}
			
			
			System.out.print(metric);
			System.out.println(" Total OP rate:" + ( (sumPr / (double)clusterCount) - 0.5) / 0.5 );
			
			
			
			/*
			HashMap<Integer,String> strs = new HashMap<Integer,String>();
			strs.put(2,"Oktie Hassanzadeh Corp");
			strs.put(3,"Oktay Hasanov");
			strs.put(4,"Oktai Hassan zadeh");
			strs.put(5,"Oktide hasssanzadeh");
			strs.put(6,"kalanjoon hoseinpour");
			HashMap<Integer,Double> probs = getProbabilities(strs);
			*/

		}		
		
	}

}
