package simfunctions;

import java.sql.ResultSet;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Vector;

import dbdriver.MySqlDB;
import utility.Config;

public class RunClusteringThread extends Thread{

	private String tablename;
	private Preprocess measure;
	private double thr = 0.8;
	
	public static int queryTokenLength = 2;
	
	public RunClusteringThread(String inputtablename, Preprocess inputmeasure, double threshold){
		tablename = inputtablename;
		measure = inputmeasure;
		thr = threshold;
	}

	public static Vector<Vector<Integer>> subsets(Vector<Integer> set, int size){
		Vector<Vector<Integer>> output = new Vector<Vector<Integer>>();
		
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
	
	public void findTrueClusters(String tablename, 
			                            HashMap<Integer,Integer> trueCluster,	
			                            HashMap<Integer,BitSet> trueMembers) {
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		String sql = "";
		try {
			sql = " SELECT c1.tid as tid1, c2.tid as tid2 FROM " + config.dbName +
			      "." + tablename + " c1," + config.dbName + "." + tablename + " c2" +
				  " where c1.id=c2.id ";
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
	
	public static void findPairsWSign(String tablename, Preprocess measure, String pairTable){
		
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		
		long t2, t3;
		t2 = System.currentTimeMillis();
		boolean log_pairs_to_db = true;
		
		String sql = " SELECT s1.tid as tid1, s2.tid as tid2 " +
                     " FROM cnamesu.`sign` s1, cnamesu.`sign` s2 " +
                     " WHERE  s1.sign = s2.sign and s1.tid <= s2.tid " +
                     " GROUP BY tid1,tid2 ";
		
		try {
			
			if (log_pairs_to_db){
				String query = "drop table if exists " + config.dbName + "." + pairTable;
				mysqlDB.executeUpdate(query);
				
				query = "create table " + config.dbName + "." + pairTable +
				        " (tid1 int, tid2 int)";
				mysqlDB.executeUpdate(query);
				
				query = "INSERT INTO " + config.dbName + "." + pairTable +
				        "( " + sql + " )";
				mysqlDB.executeUpdate(query);
				
				t3 = System.currentTimeMillis();
				System.out.println("Similar Pairs Generation: " + (t3-t2) + "ms");
			}

		} catch (Exception e) {
			System.out.println("Database error"); e.printStackTrace();
		}
	}
	
	public void findPairs(String tablename, Preprocess measure, String pairTable, Double thr){
		
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		
		String scoreTable = "scores_" + tablename + "_" + Preprocess.extractMetricName(measure.getClass().getName());
		boolean log_pairs_to_db = true;
		long t2, t3;
		t2 = System.currentTimeMillis();
		
		
		String sql = " SELECT s.tid1 as tid1, s.tid2 as tid2 " +
                     " FROM " + scoreTable + " s " +
                     " WHERE  s.score >= " + thr ;
		
		try {
			
			if (log_pairs_to_db){
				String query = "drop table if exists " + config.dbName + "." + pairTable;
				mysqlDB.executeUpdate(query);
				
				query = "create table " + config.dbName + "." + pairTable +
				        " (tid1 int, tid2 int)";
				mysqlDB.executeUpdate(query);
				
				query = "INSERT INTO " + config.dbName + "." + pairTable +
				        "( " + sql + " )";
				mysqlDB.executeUpdate(query);
				
				t3 = System.currentTimeMillis();
				System.out.println("Similar Pairs Generation: " + (t3-t2) + "ms");
			}

		} catch (Exception e) {
			System.out.println("Database error"); e.printStackTrace();
		}
	}
	
	public void evaluate(HashMap<Integer,Integer> cluster, 
			                    HashMap<Integer,BitSet> members,
			                    HashMap<Integer,Integer> trueCluster, 
			                    HashMap<Integer,BitSet> trueMembers){
		
		/*
		 * 
		 * Evaluation of clustering - Precision and Recall
		 * 
		 */
		int ClusterCount = members.keySet().size();
		double SumP = 0;
		double SumR = 0, SumR2 = 0;
		
		for (Integer cId:members.keySet()){
			//
			// Precision:
			//
			int correctCount = 0;
			Vector<Integer> v = new Vector<Integer>();
			BitSet mems = members.get(cId);
			int t = mems.nextSetBit(0);
			while (t != -1){
				v.add(t);
				t = mems.nextSetBit(t+1);
			}
			//System.out.println(v);
			int count = 0 ;
			for (Vector<Integer> pair:subsets(v,2)){
				count ++;
				//System.out.println(pair);
				//if (cluster.get(pair.get(0)) != cluster.get(pair.get(1))) System.out.print(" " + pair.get(0) + "," + pair.get(1) + " ");
				//if (cluster.get(pair.get(0)) != cluster.get(pair.get(1))) System.out.println(" " + cluster.get(pair.get(0)) + "," + cluster.get(pair.get(1)) + " ");
				if ((trueCluster.get(pair.get(0)) - trueCluster.get(pair.get(1))) == 0) {
					correctCount ++;
					//System.out.print("*" + correctCount + "*");
				}
				//System.out.println(pair);
			}
			//System.out.print( (double)(1.0*correctCount ) );
			//System.out.println( " "  + (double)(1.0*count ) );
			//System.out.println( " " + (double)(1.0*v.size() ) );
			double precision = (count!=0) ? Math.min((double)((1.0*correctCount )/(1.0*count)),1) : 0;
			//System.out.println( " " + precision );
			SumP += precision;
			if (count==0) ClusterCount--;
			//
			// Recall:
			//
			int maxInter = -1;
			int maxCidMin = -1;
			int maxCidMax = -1;
			for (Integer tcId:trueMembers.keySet()){
				int val;
				BitSet intersect = new BitSet(); 
				intersect = (BitSet)trueMembers.get(tcId).clone();
				intersect.and(members.get(cId));
				val = intersect.cardinality();
				if (val > maxInter) {
					maxInter = val;
					maxCidMin = tcId;
					maxCidMax = tcId;
				}
				else if (val == maxInter) {
					if (trueMembers.get(tcId).size() > members.get(cId).size()) maxCidMax = tcId;
					else maxCidMin = tcId; 
				}
				
				if (trueMembers.get(tcId).size() < 3) SumP += 1.0;
				
			}
			
			//System.out.println(maxCidMin);
			BitSet rec = new BitSet();
			rec = (BitSet) trueMembers.get(maxCidMin).clone();
			int ctSize = rec.cardinality();
			rec.and(members.get(cId));
			//System.out.println((double)(rec.cardinality()*1.0 / ctSize*1.0));
			//System.out.println(members.get(cId));
			//System.out.println(trueMembers.get(maxCidMin));
			//System.out.println(rec);
			if (count!=0) SumR2 += (double)(rec.cardinality()*1.0 / ctSize*1.0); 
			
			SumR += (double)(rec.cardinality()*1.0 / ctSize*1.0); 
			
		}
		
		for (Integer cId:trueMembers.keySet()){
			if (trueMembers.get(cId).cardinality() == 1) {SumP++; ClusterCount++; SumR2++;}
		}
		
		System.out.println( " Sum " + SumP );
		System.out.println( " Clusters with size >= 2: " + ClusterCount );
		System.out.println( " Average Precision for all records: " + (SumP/(double)members.keySet().size()) );
		System.out.println( " Average Precision for records present in clusters: " + (SumP/(double)ClusterCount) );
		
		System.out.println( " Average Recall for all records: " + (SumR/(double)members.keySet().size()) );
		System.out.println( " Average Recall for records present in clusters: " + (SumR2/(double)ClusterCount) );
		//System.out.println(members.keySet().size());
		//System.out.println(trueMembers.keySet().size());

		
	}
	
	public void findClusters(String pairTable, HashMap<Integer,Integer> cluster, 
                                    HashMap<Integer,BitSet> members){
		ResultSet rs;			
		
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		
		try {	
			/*
			 * 
			 * From output of similarity join, i.e., (tid1,tid2)'s 
			 * form clusters of related records
			 * 
			 */
			
			String sql = "SELECT * from " + config.dbName + "." + pairTable;
			
			rs = mysqlDB.executeQuery(sql);
			
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
						cluster.put(tid1,maxId);
						cluster.put(tid2,maxId);
						
						BitSet mems = new BitSet();
						mems.set(tid1);
						mems.set(tid2);
						members.put(maxId, mems);
						
						isThere.set(tid1);
						isThere.set(tid2);
					}
					else {
						int cId = cluster.get(tid2);
						
						cluster.put(tid1, cId);
						
						BitSet mems = members.get(cId);
						mems.set(tid1);
						members.put(cId, mems);
						
						isThere.set(tid1);
					}
				}
				else {
					if (!isThere.get(tid2)) {
						int cId = cluster.get(tid1);
						
						cluster.put(tid2, cId);
												
						BitSet mems = members.get(cId);
						mems.set(tid2);
						members.put(cId, mems);
						
						isThere.set(tid2);
					}
				}
			}
			
			//System.out.println(cluster);
			//System.out.println(members);
			
			
		} catch (Exception e) {
			System.out.println("Database error"); e.printStackTrace();
		}

	}
	
	public void run() {

		long t1, t2, t3, t4, t5, tf;
		t1 = System.currentTimeMillis();
		/*
		 * 
		 * Call Similarity Join
		 * 
		 * /
		
		t1 = System.currentTimeMillis();
		SimilarityJoin simjoin = new SimilarityJoin();
		simjoin.run(tablename, measure, 0.2);
		t2 = System.currentTimeMillis();
		System.out.println("Similarity Join: " + (t2-t1) + "ms");
		*/
		t2 = System.currentTimeMillis();
		
		System.err.println("Clustering for: " + tablename + "," + Preprocess.extractMetricName(measure.getClass().getName()));
		
		String pairTable = "pairs_" + tablename + "_" + Preprocess.extractMetricName(measure.getClass().getName());
		findPairs(tablename, measure, pairTable, thr);
		t3 = System.currentTimeMillis();
		System.out.println("Finding Pairs took: " + (t3-t2) + "ms");

		//		 Finding clusters from pairs:
		HashMap<Integer,Integer> cluster = new HashMap<Integer,Integer>();
		HashMap<Integer,BitSet> members = new HashMap<Integer,BitSet>();

		findClusters(pairTable, cluster, members);
		
		t4 = System.currentTimeMillis();
		System.out.println("Time for clustering: " + (t4-t1) + "ms");
		
		/*
		 * Evaluation of clustering - Precision and Recall
		 */
		
		// Finding true clusters:
		HashMap<Integer,Integer> trueCluster = new HashMap<Integer,Integer>();
		HashMap<Integer,BitSet> trueMembers = new HashMap<Integer,BitSet>();
		findTrueClusters(tablename, trueCluster, trueMembers);
		
		evaluate(cluster, members, trueCluster, trueMembers);
		
		//tf = System.currentTimeMillis();
		//System.out.println("Total Time: " + (tf-t1) + "ms");
		System.err.println();
		
	}

}