package simfunctions;


import java.sql.ResultSet;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import dbdriver.MySqlDB;
import utility.Config;

public class RunProbabilityAssignment {

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
	
	public static BitSet convertToBitSet1(Set<String> stringSet){
		BitSet output = new BitSet();
		for (String qgram : stringSet) {
			output.set( qgram.charAt(0));			
		}
		return output;
	}
	
	public static BitSet convertToBitSet(Set<String> stringSet){
		BitSet output = new BitSet();
		for (String qgram : stringSet) {
			output.set((qgram.charAt(1) << 7) | qgram.charAt(0));			
		}
		return output;
	}
	
	public static BitSet convertToBitSet3(Set<String> stringSet){
		BitSet output = new BitSet();
		for (String qgram : stringSet) {
			output.set((qgram.charAt(2) << 14) | (qgram.charAt(1) << 7) | qgram.charAt(0));			
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
	
	
	public static HashMap<Integer, Double> getProbabilities1(HashMap<Integer, String> strs){
		

		
		int size = 0;
		BitSet repSet = new BitSet();
		HashMap<Integer, Double> repWeights = new HashMap<Integer, Double>();
		
		
		
		
		
		
		
		
		
		
		
		/*
		 
		// initialize clusters
		BitSet X = new BitSet();
		BitSet Y = new BitSet();
		
		HashMap<Integer, Double> Yidf = new HashMap<Integer, Double>();
		HashMap<Integer, Double> SumIdf = new HashMap<Integer, Double>();
		int totalsize = 0;
		for (int tid: strs.keySet()){
			X.set(tid);
			String str = strs.get(tid); 
			BitSet strSet = convertToBitSet(RunProbabilityAssignment.getTF(str).keySet());
			int i = strSet.nextSetBit(0);
			double sumidf = 0.0;
			while (i!=-1){
				Y.set(i);
				if (!Yidf.containsKey(i)) { Yidf.put(i, Math.log(1.0 + 1.0)); }
				else {
					Yidf.put( i, (1.0 / Math.log(1.0 + ((1.0/Yidf.get(i))+1.0) )) );
				}
				sumidf += Yidf.get(i);
				i = strSet.nextSetBit(i+1);
			}
			SumIdf.put(tid, sumidf);
			totalsize += strSet.cardinality();
		}
		
		
		*/
		
		
		
		
		
		
		
		
		
		
		
		
		
		/*
		 * 
		 * Finding Cluster Representative
		 * 
		 */
		
		HashMap<Integer, BitSet> strGrams = new HashMap<Integer, BitSet>();
		/*
		int n=0;
		BitSet striSet = new BitSet();
		HashMap<Integer, Integer> strLens = new HashMap<Integer, Integer>(); 
		for (int sin:strs.keySet()){
			String stri = strs.get(sin);
			
			BitSet bb = convertToBitSet(RunProbabilityAssignment.getTF(stri).keySet());
			strGrams.put(sin, bb);
			
			//strLens.put(sin, bb.cardinality());
			strLens.put(sin, stri.length());
			striSet.or(bb);
		}
		int ii = striSet.nextSetBit(0);
		while (ii!=-1){
			n++;
			ii = striSet.nextSetBit(ii+1);
		}
		//System.out.println("n: " + n);
		*/
		Double repP = 0.0;
		
		for (int sn:strs.keySet()){
			String str = strs.get(sn); 
			BitSet strSet = convertToBitSet(RunProbabilityAssignment.getTF(str).keySet());
			strGrams.put(sn, strSet);
			//BitSet strSet = strGrams.get(sn);
			HashMap<Integer, Double> strWeights = new HashMap<Integer, Double>();
			
			//Double strP = ((double)strLens.get(sn)/n);
			
			/****  TODO: Jadid -- check /n ****/
			
			Double strP = ((double)strs.get(sn).length());
			
			int i = strSet.nextSetBit(0);
			while (i!=-1){
				//strWeights.put(i, (1.0/(double)strSet.cardinality()) );
				strWeights.put(i, (1.0/str.length()) );
				//strWeights.put(i, Yidf.get(i)/(double)SumIdf.get(sn));
				
				if (!repSet.get(i)) {repWeights.put(i, 0.0);}
				
				i = strSet.nextSetBit(i+1);
			}
			
			//Set<String> s = convertToStringSet(repSet);
			//System.out.println(s);
			//System.out.println(repWeights);
			
			repSet.or(strSet);
			
			i = repSet.nextSetBit(0);
			while (i!=-1){
				if (strSet.get(i)){
					//System.out.println(repWeights.get(i));
					//repWeights.put(i, (((double)size / ((double)size+1)) * repWeights.get(i)) + (1.0/((double)size+1))*strWeights.get(i) );
					repWeights.put(i, ((repP / (repP+strP)) * repWeights.get(i)) + (strP/(repP+strP))*strWeights.get(i) );
					//System.out.println(repWeights.get(i));
				}
				else {
					//System.out.println(repWeights.get(i));
					//repWeights.put(i, (((double)size / ((double)size+1)) * repWeights.get(i)));
					repWeights.put(i, ( (repP / (repP+strP)) * repWeights.get(i)));
					//System.out.println(repWeights.get(i));
				}
				i = repSet.nextSetBit(i+1);
			}
			
			repP += strP;
			
			//System.out.println(repSet);
			//Set<String> s = convertToStringSet(strSet);
			//System.out.println(s);
			//System.out.println(strWeights); 
			//s =	convertToStringSet(repSet);
			//System.out.println(s);
			//System.out.println(repWeights);
			size++;
		}
		//System.out.println();
		if (debug_mode) {
			System.out.println(repWeights);
			double m = 0;
			BitSet b = new BitSet();
			for (int i:repWeights.keySet()){
				b.set(i);
				//System.out.println(convertToStringSet(b) + " " + repWeights.get(i));
				b.clear();
				m += repWeights.get(i);
			}
			//System.out.println(m);
		}
		
		/*
		 * 
		 * Finding similarity of rep. to tuples and calculating probabilities
		 * 
		 */

		HashMap<Integer, Double> probs = new HashMap<Integer, Double>();
		Double sum = 0.0;
		
		for (int sn:strs.keySet()){
			String str = strs.get(sn); 
			//BitSet strSet = convertToBitSet(RunProbabilityAssignment.getTF(str).keySet());
			BitSet strSet = strGrams.get(sn);  
			HashMap<Integer, Double> strWeights = new HashMap<Integer, Double>();
			
			double sim = 0.0;
			double total = 0.0;
			int i = repSet.nextSetBit(0);
			while (i!=-1){
				if (strSet.get(i)) {sim += repWeights.get(i);}
				total += repWeights.get(i);
				i = repSet.nextSetBit(i+1);
			}
			/*
			int i = strSet.nextSetBit(0);
			while (i!=-1){
				if (repSet.get(i)) {sim += repWeights.get(i);}
				i = strSet.nextSetBit(i+1);
			}
			*/
			
			//if (debug_mode) {System.out.println(str + " " + (sim/strSet.cardinality()));}
			
			probs.put(sn,sim/strSet.cardinality());
			//probs.put(sn,sim);
			
			//probs.put(sn,1.0/strSet.cardinality());
			
			sum += sim/strSet.cardinality();
			//sum += sim;
			//System.out.println(strSet.cardinality());
			//System.out.println(total); //TODO: Mashkook sum is not 1 all the time
			//sum += sim/total;
			
			//sum += 1.0;
			//BitSet copy = new BitSet();
			//copy = (BitSet)(strSet.clone());
			//copy.and(repSet);
			//probs.put(sn,sim/copy.cardinality());
			//sum += sim/copy.cardinality();
			//probs.put(sn,(sim/total));
			//sum += (sim/total);
			
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

		String tablename = "pe";
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
		t1 = System.currentTimeMillis();
		//SignatureGen.run(tablename);
		//t2 = System.currentTimeMillis();
		//System.out.println("Similarity Join (Signature Generation): " + (t2-t1) + "ms");
		
	
		/*
		 * 
		 * Finding True Clusters
		 * 
		 */
		HashMap<Integer,Integer> trueCluster = new HashMap<Integer,Integer>();
		HashMap<Integer,BitSet> trueMembers = new HashMap<Integer,BitSet>();
		//String clustTable = "cluster_pe02";
		//findClusters(clustTable, trueCluster, trueMembers);
		//RunClustering.findTrueClusters(tablename, trueCluster, trueMembers);


		
		tf = System.currentTimeMillis();
		if (debug_mode) { System.out.println("Total Time for finding clusters: " + (tf-t1) + "ms" + "\n"); };

		String sql = "";
		ResultSet rs;
		//HashMap<Integer, Integer> tid21ton = new HashMap<Integer, Integer>();
		HashMap<Integer, HashMap<Integer, String>> records = new HashMap<Integer, HashMap<Integer, String>>();
		try {
			sql = " SELECT c.tid, c.id, c.string, c.errorp  " +
			//sql = " SELECT c.tid, c.id, c.string  " +
				  " FROM " + config.dbName + "." + tablename + " c " +
			      " order by id, errorp ";
				  //" order by id ";
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
		
		t1 = System.currentTimeMillis();
	
		int numberOfRecords = 0;
		
		
		long t = 0;
		for (int cId: records.keySet()){
			HashMap<Integer,String> strs = records.get(cId);
			
			debug_mode = false;
			// System.gc();
			long tt = System.currentTimeMillis();
			HashMap<Integer,Double> probs = getProbabilities1(strs);
			t += ( System.currentTimeMillis() - tt );
			debug_mode = true;

			
			/*
			 *  LOGGING PROBABILITY VALUES TO THE PROBABILITY TABLE
			 *  Part 2 - Log probability table to DB - INSERT value of the probability in the prob. table
			 *           Actually adding the '(tid, id, prob),' to the end of the query string 
			 */
			int j = probs.keySet().size();
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
		System.out.println("Total Time for prob assignment: " + (t) + "ms" + "\n");
		System.out.println("Total Time for prob assignment and log: " + (tf-t1) + "ms" + "\n");
		
		/*
		 *  LOGGING PROBABILITY VALUES TO THE PROBABILITY TABLE
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
		 */
		
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

	public static void findClusters(String clusterTable, 
			                            HashMap<Integer,Integer> trueCluster,	
			                            HashMap<Integer,BitSet> trueMembers) {
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		String sql = "";
		try {
			sql = " SELECT c1.tid as tid1, c2.tid as tid2 FROM " + config.dbName +
			      "." + clusterTable + " c1," + config.dbName + "." + clusterTable + " c2" +
				  " where c1.cid=c2.cid ";
			System.out.println(sql);
			ResultSet rs = mysqlDB.executeQuery(sql);
	
			
			//t4 = System.currentTimeMillis();
			//System.out.println("DEBUG INFO: " + (t4-t3) + "ms");
			//BitSet isThere = new BitSet(count); 
			BitSet isThere = new BitSet();
			int maxId = 0;
			
			rs.beforeFirst();
			while (rs.next()){
				//System.out.println(rs.getInt(1) + " " + rs.getInt(2));
				int tid1 = rs.getInt(1);
				int tid2 = rs.getInt(2);
				
				if (!isThere.get(tid1)) {
					if (!isThere.get(tid2)) {
						maxId++;
						trueCluster.put(tid1,maxId);
						trueCluster.put(tid2,maxId);
						
						BitSet mems = new BitSet();
						mems.set(tid1);
						mems.set(tid2);
						trueMembers.put(maxId, mems);
						
						isThere.set(tid1);
						isThere.set(tid2);
					}
					else {
						int cId = trueCluster.get(tid2);
						
						trueCluster.put(tid1, cId);
						
						BitSet mems = trueMembers.get(cId);
						mems.set(tid1);
						trueMembers.put(cId, mems);
						
						isThere.set(tid1);
					}
				}
				else {
					if (!isThere.get(tid2)) {
						int cId = trueCluster.get(tid1);
						
						trueCluster.put(tid2, cId);
												
						BitSet mems = trueMembers.get(cId);
						mems.set(tid2);
						trueMembers.put(cId, mems);
						
						isThere.set(tid2);
					}
				}
			}
			
			//System.out.println(trueCluster);
			//System.out.println(trueMembers);
			
			
		} catch (Exception e) {
			System.out.println("Database error"); e.printStackTrace();
		}
	
	}

}