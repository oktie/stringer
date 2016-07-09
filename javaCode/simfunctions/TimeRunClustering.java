package simfunctions;

import java.sql.ResultSet;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Vector;

import dbdriver.MySqlDB;
import experiment.SimilarityJoin;

import utility.Config;

public class TimeRunClustering {

	public static int queryTokenLength = 2;

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
	
	public static void findTrueClusters(String tablename, 
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
	
	public static void findPairs(String tablename, Preprocess measure, String pairTable, Double thr){
		
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		
		String scoreTable = "scores_" + tablename + "_" + Preprocess.extractMetricName(measure.getClass().getName());
		boolean log_pairs_to_db = true;
		//long t2, t3;
		//t2 = System.currentTimeMillis();
		
		
		String sql = " SELECT s.tid1 as tid1, s.tid2 as tid2 " +
                     " FROM " + scoreTable + " s " +
                     " WHERE  s.score >= " + thr;
		
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
				
				//t3 = System.currentTimeMillis();
				//System.out.println("Similar Pairs Generation: " + (t3-t2) + "ms");
			}

		} catch (Exception e) {
			System.out.println("Database error"); e.printStackTrace();
		}
	}
	
	public static void evaluate(HashMap<Integer,Integer> cluster, 
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
		
		//System.out.println("ClusterCount: " + ClusterCount);
		
		System.out.println( " Average Recall for all records: " + (SumR/(double)members.keySet().size()) );
		System.out.println( " Average Recall for records present in clusters: " + (SumR2/(double)ClusterCount) );
		//System.out.println(members.keySet().size());
		//System.out.println(trueMembers.keySet().size());

		
	}
	
	public static Vector<Long> findClusters(String tablename, Preprocess measure, HashMap<Integer,Integer> cluster, 
                                    HashMap<Integer,BitSet> members, double thr){
		ResultSet rs;			
		Vector<Long> times = new Vector<Long>();
		String scoreTable = "scores_" + tablename + "_" + Preprocess.extractMetricName(measure.getClass().getName());
		
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		long t1 = 0, t2=0;		
		try {	
			/*
			 * 
			 * From output of similarity join, i.e., (tid1,tid2)'s 
			 * form clusters of related records
			 * 
			 */
			
			t1 = System.currentTimeMillis();
			
			//String sql = "SELECT * from " + config.dbName + "." + pairTable;
			String sql = " SELECT s.tid1 as tid1, s.tid2 as tid2 " +
            " FROM " + scoreTable + " s " +
            " WHERE  s.score >= " + thr;
			
			rs = mysqlDB.executeQuery(sql);
			
			t2= System.currentTimeMillis();
			System.out.println("Read pairs took: " + (t2-t1) + "ms");
			times.add((t2-t1));
			
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
					else { // clusters are the same => merge  //TODO: CHECK
						int cId1 = cluster.get(tid1);
						int cId2 = cluster.get(tid2);
						
						if (cId1 != cId2) {
							int i = members.get(cId2).nextSetBit(0);
							while ((i != -1)){
								cluster.put(i, cId1);
								i = members.get(cId2).nextSetBit(i+1);
							}
							cluster.put(tid2, cId1);
							
							BitSet mems = new BitSet();
							mems = members.get(cId1);
							mems.or(members.get(cId2));
							
							members.put(cId1, mems);
							members.remove(cId2);
						}
						
					}
				}				
			}
			
			//System.out.println(cluster);
			//System.out.println(members);
			
			
		} catch (Exception e) {
			System.out.println("Database error"); e.printStackTrace();
		}

		long t3 = System.currentTimeMillis();
		times.add((t3-t2));
		System.out.println("Time for clustering only:" + (t3-t2));
		
		return times;
		
	}
	
	public static void evaluate2(HashMap<Integer,BitSet> clusters,
	        HashMap<Integer,BitSet> members,
	        HashMap<Integer,Integer> trueCluster, 
	        HashMap<Integer,BitSet> trueMembers){
		/*
		 * 
		 * Evaluation of clustering - Precision and Recall
		 * 
		 */
		
		double SumP = 0;
		double SumR = 0;
		int totalSize = 0;
		
		int ClusterCount = trueMembers.keySet().size();
		for (Integer cId:trueMembers.keySet()){
			int correctCount = 0;
			Vector<Integer> v = new Vector<Integer>();
			BitSet mems = trueMembers.get(cId);
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
				BitSet m = (BitSet) clusters.get(pair.get(0)).clone();
				m.and(clusters.get(pair.get(1)));
				if ( m.cardinality() != 0) {
					correctCount ++;
					//System.out.print("*" + correctCount + "*" + count + "* ");
				}
				//System.out.println(pair);
			}
			double precision = (count!=0) ? Math.min((double)((1.0*correctCount )/(1.0*count)),1) : 0;
			//System.out.println( " " + precision );
			SumP += precision;
			//if (precision != 1) System.err.println("oops!");
			if (count==0) ClusterCount--;
		}	
		
		System.out.println( " Number of Clusters: " + members.size() );
		//System.out.println( " Ground Truth Clusters with size >= 2: " + ClusterCount );
		//System.out.println( " Average CPrecision for all records: " + (SumP/(double)trueMembers.keySet().size()) );
		double penalty = (double)trueMembers.size()/members.size();
		if (penalty >=1) penalty = (double)members.size()/trueMembers.size();
		double pcp = (SumP/(double)ClusterCount)*penalty;
		System.out.println( " Average Penalized CPrecision for all records: " + pcp );
		
		System.out.println( " Average CPrecision for all records: " + (SumP/(double)ClusterCount) );
				
		//System.out.println( " Average CPrecision for records present in clusters: " + (SumP/(double)ClusterCount) );
		//System.out.println( " Average CPrecision for records present in clusters: " + (SumP/(double)ClusterCount) );
	
		ClusterCount = members.keySet().size();
		SumP = 0;
		SumR = 0;
		totalSize = 0;
		
		
		// For each ground truth cluster g_i:
		for (Integer gcId:trueMembers.keySet()){
			
			//
			// Find argmax_{c_j} ( | intersect(c_j,g_i) | / union(c_j,g_i)  || )
			//
			BitSet gc = trueMembers.get(gcId);
			
			int argmax = 0;
			double max = 0.0;
			
			BitSet inm = new BitSet();
			//BitSet unm = new BitSet();
			for (Integer cId:members.keySet()){
				
				BitSet in = (BitSet) members.get(cId).clone();
				BitSet un = (BitSet) members.get(cId).clone();
				in.and(gc);
				un.or(gc);
				double jc = (double) in.cardinality() / (double) un.cardinality();
				if (jc > max) {
					inm = in;
					//unm = un;
					max = jc;
					argmax = cId;
				}
				
			}
			
			if (argmax == 0){
				System.err.println("Cluster " + gcId + " doesn't match ");
			}
			//else System.out.println("Cluster " + gcId + " matches " + argmax);
			
			double csize = members.get(argmax).cardinality();
			double precision = gc.cardinality()*((double) inm.cardinality() / csize);
			double recall = gc.cardinality()*((double) inm.cardinality() / gc.cardinality());
			SumP += precision;
			SumR += recall;
			
			totalSize += gc.cardinality();
			
			
		}
			
		double Pr = (SumP/totalSize);
		double Re = (SumR/totalSize);
		System.out.println( " Average Precision for all records: " + Pr );
		System.out.println( " Average Recall for all records: " + Re );
		System.out.println( " Average F1 for all records: " + (2*Pr*Re)/(Pr+Re) );   
	}

	public static void evaluatePR(HashMap<Integer,BitSet> clusters,
            HashMap<Integer,BitSet> members,
            HashMap<Integer,Integer> trueCluster, 
            HashMap<Integer,BitSet> trueMembers){
		/*
		 * 
		 * Evaluation of clustering - Precision and Recall
		 * 
		 */
		
		double SumP = 0;
		double SumR = 0;
		int totalSize = 0;
		
		int ClusterCount = trueMembers.keySet().size();
		for (Integer cId:trueMembers.keySet()){
			int correctCount = 0;
			Vector<Integer> v = new Vector<Integer>();
			BitSet mems = trueMembers.get(cId);
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
				BitSet m = (BitSet) clusters.get(pair.get(0)).clone();
				m.and(clusters.get(pair.get(1)));
				if ( m.cardinality() != 0) {
					correctCount ++;
					//System.out.print("*" + correctCount + "*" + count + "* ");
				}
				//System.out.println(pair);
			}
			double precision = (count!=0) ? Math.min((double)((1.0*correctCount )/(1.0*count)),1) : 0;
			//System.out.println( " " + precision );
			SumP += precision;
			//if (precision != 1) System.err.println("oops!");
			if (count==0) ClusterCount--;
		}	
		
		//System.out.println( " Number of Clusters: " + members.size() );
		//System.out.println( " Ground Truth Clusters with size >= 2: " + ClusterCount );
		//System.out.println( " Average CPrecision for all records: " + (SumP/(double)trueMembers.keySet().size()) );
		double penalty = (double)trueMembers.size()/members.size();
		if (penalty >=1) penalty = (double)members.size()/trueMembers.size();
		double pcp = (SumP/(double)ClusterCount)*penalty;
		System.out.println( pcp );
		
		System.out.println( (SumP/(double)ClusterCount) );
				
		//System.out.println( " Average CPrecision for records present in clusters: " + (SumP/(double)ClusterCount) );
		//System.out.println( " Average CPrecision for records present in clusters: " + (SumP/(double)ClusterCount) );

		ClusterCount = members.keySet().size();
		SumP = 0;
		SumR = 0;
		totalSize = 0;
		
		
		// For each ground truth cluster g_i:
		for (Integer gcId:trueMembers.keySet()){
			
			//
			// Find argmax_{c_j} ( | intersect(c_j,g_i) | / union(c_j,g_i)  || )
			//
			BitSet gc = trueMembers.get(gcId);
			
			int argmax = 0;
			double max = 0.0;
			
			BitSet inm = new BitSet();
			//BitSet unm = new BitSet();
			for (Integer cId:members.keySet()){
				
				BitSet in = (BitSet) members.get(cId).clone();
				BitSet un = (BitSet) members.get(cId).clone();
				in.and(gc);
				un.or(gc);
				double jc = (double) in.cardinality() / (double) un.cardinality();
				if (jc > max) {
					inm = in;
					//unm = un;
					max = jc;
					argmax = cId;
				}
				
			}
			
			if (argmax == 0){
				System.err.println("Cluster " + gcId + " doesn't match ");
			}
			//else System.out.println("Cluster " + gcId + " matches " + argmax);
			
			double csize = members.get(argmax).cardinality();
			double precision = gc.cardinality()*((double) inm.cardinality() / csize);
			double recall = gc.cardinality()*((double) inm.cardinality() / gc.cardinality());
			SumP += precision;
			SumR += recall;
			
			totalSize += gc.cardinality();
			
			
		}
			
		double Pr = (SumP/totalSize);
		double Re = (SumR/totalSize);
		System.out.println( Pr );
		System.out.println( Re );
		System.out.println( (2*Pr*Re)/(Pr+Re) );
		System.out.println( members.size() );
	}
	
	public static void main(String[] args) {
		String tablename = "time";
		int N = 20;
		
		Vector<Vector<Long>> v = new Vector<Vector<Long>>();
		for (int i=0; i<=N+1; i++){
		// System.gc();
			v.add(run(tablename));
			
		}
		
		long sumPtime = 0;
		long sumCtime = 0;
		v.removeElementAt(0);
		v.removeElementAt(1);
		for (Vector<Long> tv: v){
			sumPtime += tv.get(0);
			sumCtime += tv.get(1);
		}
		System.out.println( "Times: " + (sumPtime*1.0/N) + " " + (sumCtime*1.0/N) );
		
	}
	
	public static Vector<Long> run(String tablename) {

		double thr = 0.4;
		boolean show_times = true;
		Vector<String> tables = new Vector<String>();
		
		Vector<Long> times = new Vector<Long>();
		
		long t1, t2, t3, t4, t5, tf;
		t1 = System.currentTimeMillis();
		/*
		 * 
		 * Call Similarity Join
		 * 
		 */
		
		Preprocess bm25WeightedJaccard = new WeightedJaccardBM25();
		Preprocess measure = bm25WeightedJaccard;
		
		
		t1 = System.currentTimeMillis();
		SimilarityJoin simjoin = new SimilarityJoin();
		//simjoin.run(tablename, measure, 0.2);
		t2 = System.currentTimeMillis();
		if (show_times) System.out.println("Similarity Join: " + (t2-t1) + "ms");
		
		//String pairTable = "pairs_" + tablename + "_" + Preprocess.extractMetricName(measure.getClass().getName());
		//findPairs(tablename, measure, pairTable, thr);
		//t3 = System.currentTimeMillis();
		//if (show_times) System.out.println("Finding Pairs took: " + (t3-t2) + "ms");

		//		 Finding clusters from pairs:
		HashMap<Integer,Integer> cluster = new HashMap<Integer,Integer>();
		HashMap<Integer,BitSet>  members = new HashMap<Integer,BitSet>();
		
		t3 = System.currentTimeMillis();
		times = findClusters(tablename, measure, cluster, members, thr);
		t4 = System.currentTimeMillis();
		
		
		if (show_times) System.out.println("Time for clustering: " + (t4-t3) + "ms");
		//System.out.println(members.size());
		/*
		 * Evaluation of clustering - Precision and Recall
		 */
		
		/* Finding true clusters:
		HashMap<Integer,Integer> trueCluster = new HashMap<Integer,Integer>();
		HashMap<Integer,BitSet> trueMembers = new HashMap<Integer,BitSet>();
		findTrueClusters(tablename, trueCluster, trueMembers);

		//System.out.println(members);
		//System.out.println(trueMembers);
		
		
		int count = 0;
		int sumSize = 0;
		for (int cid:trueMembers.keySet()){
			if (trueMembers.get(cid).cardinality() >= 0) count++;
			sumSize += trueMembers.get(cid).cardinality();
		}
		System.out.println("** " + ((double)sumSize/count) );
		*/
		//System.out.println();
		
		/*
		HashMap<Integer,BitSet> clusterNums = new HashMap<Integer,BitSet>();
		for (Integer cid:members.keySet()){
			BitSet mems = members.get(cid);
			int t = mems.nextSetBit(0);
			while (t != -1){
				
				if (clusterNums.containsKey(t)){
					BitSet b = clusterNums.get(t);
					b.set(cid);
					clusterNums.put(t, b);
				} else {
					BitSet b = new BitSet();
					b.set(cid);
					clusterNums.put(t, b);
				}
				
				t = mems.nextSetBit(t+1);
			}
		}
		
		
		System.out.println(tablename + " Thr: " + thr);
		//evaluate2(clusterNums, members, trueCluster, trueMembers);
		evaluatePR(clusterNums, members, trueCluster, trueMembers);
		//evaluate2(members, trueCluster, trueMembers);
		*/
		//tf = System.currentTimeMillis();
		//System.out.println("Total Time: " + (tf-t1) + "ms");
		
		return times;
		
	}		
	
}