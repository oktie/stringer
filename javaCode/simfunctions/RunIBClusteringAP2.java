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

public class RunIBClusteringAP2 {

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
		System.out.println( " Average CPrecision for all records: " + (SumP/(double)ClusterCount) );
		double penalty = (double)trueMembers.size()/members.size();
		double pcp = (penalty>=1) ? (SumP/(double)ClusterCount) : (SumP/(double)ClusterCount)*penalty;
		System.out.println( " Average Penalized CPrecision for all records: " + pcp );		
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
	
	public static BitSet getCluster(HashMap<Integer, String> strs,
            Vector<Integer> tids, HashMap<Integer, Double> scores,
            BitSet pr, Double thr2){

		boolean debug_mode = false;
		
		HashMap<Integer, BitSet> C = new HashMap<Integer, BitSet>();
		
		HashMap<Integer, Double> p_x = new HashMap<Integer, Double>();
		
		HashMap<Integer, HashMap<Integer,Double>> p_y_x = new HashMap<Integer, HashMap<Integer,Double>>();
		
		// initialize clusters
		BitSet X = new BitSet();
		BitSet Y = new BitSet();
		
		HashMap<Integer, Double> Yidf = new HashMap<Integer, Double>();
		HashMap<Integer, Double> SumIdf = new HashMap<Integer, Double>();
		int totalsize = 0;
		for (int tid: strs.keySet()){
			X.set(tid);
			String str = strs.get(tid); 
			BitSet strSet = convertToBitSet2(RunProbabilityAssignment.getTF(str).keySet());
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
		
		
		for (int tid: strs.keySet()){
			X.set(tid);
			
			BitSet cMember = new BitSet();
			cMember.set(tid);
			C.put(tid, cMember);
			
			String str = strs.get(tid); 
			BitSet strSet = convertToBitSet2(RunProbabilityAssignment.getTF(str).keySet());
			HashMap<Integer,Double> py = new HashMap<Integer,Double>(); 
			int i = strSet.nextSetBit(0);
			while (i!=-1){
				
				Y.set(i);
				//py.put(i, 1.0/strSet.cardinality());
				py.put(i, Yidf.get(i)/SumIdf.get(tid));
				
				i = strSet.nextSetBit(i+1);
				
			}
			
			p_x.put(tid, 1.0/strs.size());
			//p_x.put(tid, ((double)strSet.cardinality())/totalsize*1.0 );
			p_y_x.put(tid, py);
		
		}
		
		HashMap<Integer, Double> p_c = new HashMap<Integer, Double>();
		HashMap<Integer, HashMap<Integer, Double>> p_y_c = new HashMap<Integer, HashMap<Integer, Double>>();
		for (int cid: C.keySet()){
			BitSet xs = C.get(cid);
			
			Double spx = 0.0;
			int i = xs.nextSetBit(0);
			while (i!=-1){
				//Y.set(i);
				
				spx += p_x.get(i);
				
				i = xs.nextSetBit(i+1);
			}
			p_c.put(cid, spx);
			
			
			HashMap<Integer,Double> sum_px_pyx = new HashMap<Integer,Double>();
			i = xs.nextSetBit(0);
			while (i!=-1){
				//Y.set(i);
				
				for (int y:p_y_x.get(i).keySet()){
					if (sum_px_pyx.containsKey(y)) {
						sum_px_pyx.put(y,  sum_px_pyx.get(cid) + ( p_x.get(i)*p_y_x.get(i).get(y) / p_c.get(cid) ) );
					}
					else sum_px_pyx.put(y, ( p_x.get(i)*p_y_x.get(i).get(y) / p_c.get(cid) ) );
				}
				
				i = xs.nextSetBit(i+1);
			}
			p_y_c.put(cid, sum_px_pyx );
		}
		
		if (debug_mode) {
			System.out.println("X: " + X);
			System.out.println("Y: " + Y);
			System.out.println("Yidf: " + Yidf);
			System.out.println("Sumidf: " + SumIdf);
			System.out.println("C: " + C);
			
			System.out.println("p_y_c: " + p_y_c);
			System.out.println("p_y_x: " + p_y_x);
			System.out.println();
		}
		
		// Merge clusters ci,cj :: merge(clusters, p_c, p_y_c, i, j)
		int cid1 = tids.firstElement();
		pr.set(cid1);
		
		//merge(C, cid1, cid2, p_c, p_y_c);
		double il = 0.0;
		//il = infoLoss(C, cid1, 2, p_c, p_y_c);
		//System.out.println(" 2: " + il);
		
		double avgil = 0.0;
		
		int i = 0;
		
		BitSet cluster = new BitSet();

		
		HashMap<Integer, Double> ils = new HashMap<Integer, Double>();
		int count = 0;
		for (int tid2: tids){
			i++;
			if (scores.get(tid2) >= thr2){
				merge(C, cid1, tid2, p_c, p_y_c);
				cluster.set(tid2);
				pr.set(tid2);
				if (debug_mode) {
					System.out.println(" " + tid2 + " merged.");
				}
			} else {
				il = infoLoss(C, cid1, tid2, p_c, p_y_c);
				ils.put(tid2,il);
				avgil += il;
				count++;
				//System.out.println(" " + tid2 + ": " + il);
			}
		}
		//avgil = avgil/(strs.size()-1);
		if (debug_mode) {
			avgil = avgil/(count);
			System.out.println("Avg: " + (avgil));
			System.out.println();
		}
		
		//if (count == 0) System.out.println( " ** ** " + tids);
		
		//Collections.sort(ils);
		if (debug_mode) {
			ils = getSortedMap(ils);
			int ii = 0;
			for (Integer d: ils.keySet()){
				System.out.println(" " + d + ": " + ils.get(d));
				ii++;
			}
		}
		//System.out.println(ils);
		double stddev = 0.0;
		for (Integer d: ils.keySet()){
			double ili = ils.get(d);
			stddev += ((avgil - ili)*(avgil - ili));
		}
		stddev = Math.sqrt((stddev/ils.size()));
		
		for (Integer tid: ils.keySet()) {
			if (ils.get(tid) < avgil - 1.0*stddev*stddev) 
				cluster.set(tid);
		}
		
		/*
		merge(C, cid1, cid2, p_c, p_y_c);
		
		System.out.println("C: " + C);
		
		System.out.println("p_y_c: " + p_y_c);
		System.out.println("p_c: " + p_c);
		*/
		
		
		return cluster;

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
	
	
	public static void findClustersM(String tablename, Preprocess measure, 
			                            HashMap<Integer,Integer> cluster, 
                             HashMap<Integer,BitSet> members, Double thr1, Double thr2){
		boolean debug_mode = true;
		ResultSet rs;			
		
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		
		String scoreTable = "scores_" + tablename + "_" + Preprocess.extractMetricName(measure.getClass().getName());
		boolean log_pairs_to_db = true;
		//long t2, t3;
		//t2 = System.currentTimeMillis();
		
		
		String sql = " SELECT s.tid1, s.tid2, s.score, c.`string` " +
				     " FROM " + scoreTable + " s, " + tablename + " c " +
				     " where s.tid2 = c.tid and score >= " + thr1 +
				     " order by tid1, score desc";
			
		/*
			" SELECT s.tid1 as tid1, s.tid2 as tid2, score " +
                     " FROM " + scoreTable + " s " +
                     " WHERE  s.score >= " + thr +
                     " order by tid1, score desc";
		*/
		
		HashMap<Integer, HashMap<Integer, Double>> scores = new HashMap<Integer, HashMap<Integer, Double>>();
		HashMap<Integer, Vector<Integer>> tid2sOrder = new HashMap<Integer, Vector<Integer>>();
		Vector<Integer> tid1sOrder = new Vector<Integer>();
		HashMap<Integer, String> data = new HashMap<Integer, String>(); 

		try {
			rs = mysqlDB.executeQuery(sql);
			while (rs.next()){
				int tid1 = rs.getInt(1);
				int tid2 = rs.getInt(2);
				double score = rs.getDouble(3);
				
				tid1sOrder.add(tid1);
				String str = rs.getString(4);
				data.put(tid2, str);
				
				if (!scores.containsKey(tid1)){
					HashMap<Integer, Double> tscores = new HashMap<Integer, Double>();
					tscores.put(tid2, score);
					scores.put(tid1, tscores);
					Vector<Integer> tids = new Vector<Integer>();
					tids.add(tid2);
					tid2sOrder.put(tid1, tids);
				} else{
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
		
	
		
		BitSet pr = new BitSet(); 
		for (int tid1: tid1sOrder){
			if (!pr.get(tid1)){
			
				Vector<Integer> tids = tid2sOrder.get(tid1);
				HashMap<Integer, Double> tscores = scores.get(tid1);

				Vector<Integer> marked = new Vector<Integer>();
				for (int tid: tids){
					if (pr.get(tid)) {
						marked.add(tid);
					}
				}
				for (int tid:marked){
					tids.removeElement(tid);
					tscores.remove(tid);	
				}
				
				
				
				HashMap<Integer,String> strs = new HashMap<Integer,String>();
				for (int tid:tids){
					if (!pr.get(tid)) strs.put(tid, data.get(tid));
				}
				
				BitSet clusterMems = getCluster(strs, tids, tscores, pr, thr2);
				if (debug_mode) System.out.println("Cluster Mem.s: " + clusterMems);
				
				members.put(tid1, clusterMems);
				/*
				int i = clusterMems.nextSetBit(0);
				while (i != -1){
					pr.set(i);
					tid2sOrder.remove(i);
					
					cluster.put(i, tid1);
					
					i = clusterMems.nextSetBit(i+1);
				}
				*/
			}
		}
		
		
		
		
		
	}
	
	public static void main(String[] args) {

		String tablename = "cu6";
		
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
		//SimilarityJoin.run(tablename, measure, 0.2);
		t2 = System.currentTimeMillis();
		System.out.println("Similarity Join: " + (t2-t1) + "ms");
		
		HashMap<Integer,Integer> cluster = new HashMap<Integer,Integer>();
		HashMap<Integer,BitSet> members = new HashMap<Integer,BitSet>();
		
		
		findClustersM(tablename, measure, cluster, members, 0.2, 0.4);
		
		
		System.out.println("Cluster Count before merge: " + members.size() );
		Vector<Integer> cidlist = new Vector<Integer>();
		for (int cid1:members.keySet()){
			for (int cid2:members.keySet()){
				if (cid1!=cid2) {
					BitSet c1 = members.get(cid1);
					BitSet c2 = members.get(cid2);
					if ((c1.cardinality()>2)&&(c2.cardinality()>2)){
						BitSet inter = new BitSet();
						inter = (BitSet) c2.clone();
						inter.and(c1);
						if (inter.cardinality() >= (c2.cardinality()-2) ){
							if (!cidlist.contains(cid1)){
								c1.or(c2);
								cidlist.add(cid2);
								members.put(cid1, c1);
							}
						}
					}
				}
			}
		}
		
		for (int cid2:cidlist) members.remove(cid2);
		
		
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
		
		System.out.println("Cluster Count: " + members.size() );
		t3 = System.currentTimeMillis();
		System.out.println("Finding Clusters took: " + (t3-t2) + "ms");

		
		//		 Finding clusters from pairs:


		
		t4 = System.currentTimeMillis();
		System.out.println("Time for clustering: " + (t4-t1) + "ms");
		//System.out.println(members.size());
		
		/*
		 * Evaluation of clustering - Precision and Recall
		 */
		
		// Finding true clusters:
		HashMap<Integer,Integer> trueCluster = new HashMap<Integer,Integer>();
		HashMap<Integer,BitSet> trueMembers = new HashMap<Integer,BitSet>();
		findTrueClusters(tablename, trueCluster, trueMembers);

		//System.out.println(trueMembers);
		//System.out.println(trueMembers.get(223));
		//System.out.println(members);
		
		/*
		int count = 0;
		int sumSize = 0;
		for (int cid:trueMembers.keySet()){
			if (trueMembers.get(cid).cardinality() >= 0) count++;
			sumSize += trueMembers.get(cid).cardinality();
		}
		System.out.println("** " + ((double)sumSize/count) );
		*/
		System.out.println();
		
		
		evaluate(clusterNums, members, trueCluster, trueMembers);
		
		//tf = System.currentTimeMillis();
		//System.out.println("Total Time: " + (tf-t1) + "ms");
		
		
	}

}