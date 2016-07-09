package simfunctions;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import dbdriver.MySqlDB;
import utility.Config;

public class TimeRunCLINKClustering {

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
	
	public static void evaluate(HashMap<Integer,BitSet> clusters,
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
				//System.out.print(clusters.containsKey(pair.get(1)) + " ");
				//System.out.print(clusters.get(pair.get(1)) + " ");
				//System.out.println(pair.get(1) + " ");
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
		System.out.println( " Ground Truth Clusters with size >= 2: " + ClusterCount );
		//System.out.println( " Average CPrecision for all records: " + (SumP/(double)trueMembers.keySet().size()) );
		double penalty = (double)trueMembers.size()/members.size();
		if (penalty >=1) penalty = (double)members.size()/trueMembers.size();
		double pcp = (SumP/(double)ClusterCount)*penalty;
		System.out.println( " Average Penalized CPrecision for all records: " + pcp );		
	
		System.out.println( " Average CPrecision for all records: " + (SumP/(double)ClusterCount) );
	
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
			double precision = ((double) inm.cardinality() / csize);
			double recall = ((double) inm.cardinality() / gc.cardinality());
			SumP += gc.cardinality()*precision;
			SumR += gc.cardinality()*recall;
			
			//System.out.println("re: " + recall );
			
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
				//System.out.print(clusters.containsKey(pair.get(1)) + " ");
				//System.out.print(clusters.get(pair.get(1)) + " ");
				//System.out.println(pair.get(1) + " ");
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
		
		
		System.out.println( " Ground Truth Clusters with size >= 2: " + ClusterCount );
		//System.out.println( " Average CPrecision for all records: " + (SumP/(double)trueMembers.keySet().size()) );
		double penalty = (double)trueMembers.size()/members.size();
		if (penalty >=1) penalty = (double)members.size()/trueMembers.size();
		double pcp = (SumP/(double)ClusterCount)*penalty;
		System.out.println( pcp );		

		System.out.println( (SumP/(double)ClusterCount) );

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
			double precision = ((double) inm.cardinality() / csize);
			double recall = ((double) inm.cardinality() / gc.cardinality());
			SumP += gc.cardinality()*precision;
			SumR += gc.cardinality()*recall;
			
			//System.out.println("re: " + recall );
			
			totalSize += gc.cardinality();
			
			
		}
			
		double Pr = (SumP/totalSize);
		double Re = (SumR/totalSize);
		System.out.println( Pr );
		System.out.println( Re );
		System.out.println( (2*Pr*Re)/(Pr+Re) );       
		System.out.println( members.size() );
		
	}
	
	public static BitSet convertToBitSet2(Set<String> stringSet){
		BitSet output = new BitSet();
		for (String qgram : stringSet) {
			output.set((qgram.charAt(1) << 7) | qgram.charAt(0));			
		}
		return output;
	}
	
	
	public static HashMap getSortedMap(HashMap hmap)
	{
		HashMap map = new LinkedHashMap();
		List mapKeys = new ArrayList(hmap.keySet());
		List mapValues = new ArrayList(hmap.values());
		hmap.clear();
		TreeSet sortedSet = new TreeSet(mapValues);
		Object[] sortedArray = sortedSet.toArray();
		int size = sortedArray.length;
		//		a) Ascending sort
 
		for (int i=0; i<size; i++)	{
			map.put(mapKeys.get(mapValues.indexOf(sortedArray[i])), sortedArray[i]);
		}
		return map;
	}
	
	public static void merge (HashMap<Integer, BitSet> C, 
	           Integer cidi, Integer cidj,
	           HashMap<Integer, Double> p_c,
	           HashMap<Integer, HashMap<Integer, Double>> p_y_c){

		BitSet cmems = C.get(cidi);
		cmems.or(C.get(cidj));
		
		C.remove(cidj);
		C.put(cidi, cmems);
		
		double pci = p_c.get(cidi);
		double pcj = p_c.get(cidj);
		double pcs = pci + pcj;
		
		HashMap<Integer,Double> py = new HashMap<Integer,Double>();
		for (int y:p_y_c.get(cidj).keySet()){
			if (!p_y_c.get(cidi).containsKey(y)){
				p_y_c.get(cidi).put(y,0.0);
			}
		}
		for (int y:p_y_c.get(cidi).keySet()){
			if (!p_y_c.get(cidj).containsKey(y)){
				p_y_c.get(cidj).put(y,0.0);
			}
			Double p = ( (pci/pcs) * p_y_c.get(cidi).get(y) ) + ( (pcj/pcs) * p_y_c.get(cidj).get(y) );
			py.put(y,p);
		}
		p_y_c.remove(cidj);
		p_y_c.put(cidi, py);
		
		p_c.remove(cidj);
		p_c.put(cidi, pcs);

	}
	
	public static double infoLoss(HashMap<Integer, BitSet> C, 
	           Integer cidi, Integer cidj,
	           HashMap<Integer, Double> p_c,
	           HashMap<Integer, HashMap<Integer, Double>> p_y_c){

		double il = 0.0;
		
		double pci = p_c.get(cidi);
		double pcj = p_c.get(cidj);
		double pcs = pci + pcj;
		
		HashMap<Integer,Double> pi = p_y_c.get(cidi);
		HashMap<Integer,Double> pj = p_y_c.get(cidj);
		HashMap<Integer,Double> ph = new HashMap<Integer,Double>();
		for (int y:p_y_c.get(cidj).keySet()){
			if (!p_y_c.get(cidi).containsKey(y)){
				p_y_c.get(cidi).put(y,0.0);
			}
		}
		for (int y:p_y_c.get(cidi).keySet()){
			if (!p_y_c.get(cidj).containsKey(y)){
				p_y_c.get(cidj).put(y,0.0);
			}
			Double p = ( (pci/pcs) * pi.get(y) ) + ( (pcj/pcs) * pj.get(y) );
			ph.put(y,p);
		}
		
		double djs = (pci/pcs)*dkl(pi,ph) + (pcj/pcs)*dkl(pj,ph); 
		//System.out.println("pj:" + pj);
		//System.out.println("ph:" + ph);
		//System.out.println("djs:" + dkl(pj,ph));
		return (pci+pcj)*djs;

	}
	
	public static double dkl (HashMap<Integer,Double> p1, HashMap<Integer,Double> p2){
		double sum = 0.0;
		for (int x:p1.keySet()){
			//System.out.println("*: " + sum);
			if ((p2.containsKey(x))&&(p1.get(x)!=0)) sum += p1.get(x) * Math.log( p1.get(x)/p2.get(x) );
		}
		return sum;
	}			
	
	
	public static Vector<Long> findClustersCLINK(String tablename, Preprocess measure, 
			                            HashMap<Integer,Integer> cluster, 
                             HashMap<Integer,BitSet> members, Double thr){
		boolean debug_mode = false;
		ResultSet rs;			
		Vector<Long> times = new Vector<Long>();
		
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		
		String scoreTable = "scores_" + tablename + "_" + Preprocess.extractMetricName(measure.getClass().getName());
		
		long t1 = System.currentTimeMillis();
		
		double thrL = (thr >= 0.5) ? 0.5 : thr; 
		String sql = " SELECT s.tid1, s.tid2, s.score " +
				     " FROM " + scoreTable + " s " +
				     " where score >= " + thrL +
				     " order by tid1, score desc";
		
		HashMap<Integer, HashMap<Integer, Double>> scores = new HashMap<Integer, HashMap<Integer, Double>>();
		HashMap<Integer, Vector<Integer>> tid2sOrder = new HashMap<Integer, Vector<Integer>>();
		Vector<Integer> tid1sOrder = new Vector<Integer>(); 

		try {
			rs = mysqlDB.executeQuery(sql);
			while (rs.next()){
				int tid1 = rs.getInt(1);
				int tid2 = rs.getInt(2);
				double score = rs.getDouble(3);
				
				BitSet Marked1 = new BitSet();
				if (!Marked1.get(tid1)) { tid1sOrder.add(tid1); Marked1.set(tid1); }
				//String str = rs.getString(4);
				
				BitSet Marked2 = new BitSet();
				
				if (!Marked2.get(tid1)) {
					HashMap<Integer, Double> tscores = new HashMap<Integer, Double>();
					tscores.put(tid2, score);
					scores.put(tid1, tscores);
					Vector<Integer> tids = new Vector<Integer>();
					tids.add(tid2);
					tid2sOrder.put(tid1, tids);
				} else {
					HashMap<Integer, Double> tscores = scores.get(tid1);
					tscores.put(tid2, score);
					scores.put(tid1, tscores);
					Vector<Integer> tids = tid2sOrder.get(tid1);
					tids.add(tid2);
					tid2sOrder.put(tid1, tids);
				}
			}
		} catch (Exception e) {
			System.out.println("Database error"); e.printStackTrace();
		}
		
		long t2 = System.currentTimeMillis();
		
		System.out.println(" reading pairs took: " + (t2-t1));
		
		times.add((t2-t1));
		
		t2=System.currentTimeMillis();
		
		BitSet pr = new BitSet(); 
		for (int tid1: tid1sOrder){
			if (!pr.get(tid1)){ // If the record is not already in a cluster:
			
				Vector<Integer> tids = tid2sOrder.get(tid1);
				HashMap<Integer, Double> tscores = scores.get(tid1);
				
				BitSet clusterMems = new BitSet();
				clusterMems.set(tid1);
				for (int tid2: tids){
					if ((tscores.get(tid2) > thr)&&(!pr.get(tid2))){
						clusterMems.set(tid2);
					}
				}
				
				if (debug_mode) System.out.println("Cluster Mem.s: " + clusterMems);
				
				members.put(tid1, clusterMems);
				
				int i = clusterMems.nextSetBit(0);
				while (i != -1){
					pr.set(i);
					tid2sOrder.remove(i);
					
					cluster.put(i, tid1);
					
					i = clusterMems.nextSetBit(i+1);
				}
			
			}
		}
		
		long t3 = System.currentTimeMillis();
		
		times.add((t3-t2));
		System.out.println(" clustering took: " + (t3-t2));
		
		return times;
	}
	
	public static void main(String[] args) {
		String tablename = "time";
		int N = 20;
		
		Vector<Vector<Long>> v = new Vector<Vector<Long>>();
		for (int i=0; i<=N+1; i++){
			
			//Vector<Double> m = new Vector<Double>();
			//for (long j=0; j<2000000; j++)
				//for (long k=0; k<10000000; k++)
					//for (long l=0; l<100000000; l++)
					//	m.add(1231232.232);
			//Vector<Double> n = new Vector<Double>();
			//for (long j=0; j<200000; j++)
				//for (long k=0; k<10000000; k++)
					//for (long l=0; l<100000000; l++)
				//		n.add(1231232.232);
			// System.gc();
			
			
			v.add(run(tablename));
			System.out.println();
				
			
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

		Vector<Long> times = new Vector<Long>();
		
		double thr = 0.4;
		boolean show_times = true;

		long t1, t2, t3, t4, t5, tf;
		t1 = System.currentTimeMillis();
		/*
		 * Call Similarity Join
		 */
		
		Preprocess bm25WeightedJaccard = new WeightedJaccardBM25();
		Preprocess measure = bm25WeightedJaccard;
		t1 = System.currentTimeMillis();
		//SimilarityJoin.run(tablename, measure, 0.2);
		t2 = System.currentTimeMillis();
		if (show_times) System.out.println("Similarity Join: " + (t2-t1) + "ms");

		
		/*
		 * Finding Clusters:
		 */
		
		HashMap<Integer,Integer> cluster = new HashMap<Integer,Integer>();
		HashMap<Integer,BitSet> members = new HashMap<Integer,BitSet>();
		
		t2 = System.currentTimeMillis();
		times = findClustersCLINK(tablename, measure, cluster, members, thr);
		t3 = System.currentTimeMillis();
		
		if (show_times) System.out.println("Finding Clusters took: " + (t3-t2) + "ms");

		System.out.println("Cluster Count: " + members.size() );

		//tf = System.currentTimeMillis();
		//System.out.println("Total Time: " + (tf-t1) + "ms");
		
		/*
		 * Evaluation of clustering - Precision and Recall
		 * /
		
		int ccc = 0;
		for (Integer cid:members.keySet()){
			ccc += members.get(cid).cardinality();
		}
		System.out.println("Tuples in clusters Count: " + ccc );
		
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
		
		// Finding clusters from pairs:
		t4 = System.currentTimeMillis();
		if (show_times) System.out.println("Time for clustering: " + (t4-t1) + "ms");
		//System.out.println(members.size());
		
		// Finding true clusters:
		HashMap<Integer,Integer> trueCluster = new HashMap<Integer,Integer>();
		HashMap<Integer,BitSet> trueMembers = new HashMap<Integer,BitSet>();
		findTrueClusters(tablename, trueCluster, trueMembers);

		//System.out.println(trueMembers);
		//System.out.println(trueMembers.get(223));
		//System.out.println(members);
		
		System.out.println(tablename + " - Thr: " + thr);
		//evaluate(clusterNums, members, trueCluster, trueMembers);
		evaluatePR(clusterNums, members, trueCluster, trueMembers);
		
		System.out.println();
	*/
		return times;
	}
	
}