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

import java.sql.ResultSet;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Vector;

import dbdriver.MySqlDB;
import simfunctions.Preprocess;
import simfunctions.WeightedJaccardBM25;
import utility.Config;

public class RunCntrClustering {

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

	public static void evaluateClustering(HashMap<Integer,BitSet> clusters,
            HashMap<Integer,Integer> trueCluster, Vector<Double> output){
		

		/*
		 * 
		 * Evaluation of clustering - Precision and Recall
		 * 
		 */
		
		double SumP = 0;
		double SumR = 0;
		int totalSize = 0;
		double cpr = 0;
		
		HashMap<Integer,BitSet> members = new HashMap<Integer,BitSet>();
		for (Integer tid:clusters.keySet()){
			BitSet clusts = clusters.get(tid);
			int c = clusts.nextSetBit(0);
			while (c != -1){
				if (members.containsKey(c)){
					BitSet b = members.get(c);
					b.set(tid);
					members.put(c, b);
				} else {
					BitSet b = new BitSet();
					b.set(tid);
					members.put(c, b);
				}
				c = clusts.nextSetBit(c+1);
			}
		}
		
		HashMap<Integer,BitSet> trueMembers = new HashMap<Integer,BitSet>();
		for (Integer tid:trueCluster.keySet()){
			int cid = trueCluster.get(tid);
			if (trueMembers.containsKey(cid)){
				BitSet b = trueMembers.get(cid);
				b.set(tid);
				trueMembers.put(cid, b);
			} else {
				BitSet b = new BitSet();
				b.set(tid);
				trueMembers.put(cid, b);
			}
		}
		//System.out.println(clusters);
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
			int count = 0;
			
			for (Vector<Integer> pair:subsets(v,2)){
				count ++;
				//System.out.println(pair);
				//if (cluster.get(pair.get(0)) != cluster.get(pair.get(1))) System.out.print(" " + pair.get(0) + "," + pair.get(1) + " ");
				//if (cluster.get(pair.get(0)) != cluster.get(pair.get(1))) System.out.println(" " + cluster.get(pair.get(0)) + "," + cluster.get(pair.get(1)) + " ");
				//System.out.println(pair.get(0));
				BitSet m = (BitSet) clusters.get(pair.get(0)).clone();
				if (clusters.get(pair.get(1)) == null){
					System.err.println("ERROR: tuple " + pair.get(1) + " not in any cluster");
					return;
				}
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
		
		cpr = (SumP/(double)ClusterCount);
		System.out.println( cpr );
		
				
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
		
		
		output.add( pcp );
		output.add( cpr );
		output.add(Pr);
		output.add(Re);
		output.add((2*Pr*Re)/(Pr+Re));
		output.add((double)members.size());
		
		
	}
	
	
	public static void saveClusters(HashMap<Integer,BitSet> clusterNums, String tablename){
		
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		Boolean log_pairs_to_db = true;
		
		String clusterTable = "Cluster_" + tablename; // + "_" + Preprocess.extractMetricName(measure.getClass().getName());
		
		try {
			
			if (log_pairs_to_db){
				String query = "drop table if exists " + config.dbName + "." + clusterTable;
				mysqlDB.executeUpdate(query);
				
				query = "create table " + config.dbName + "." + clusterTable +
				        " (tid int, cid int)";
				mysqlDB.executeUpdate(query);
				
				for (Integer tid:clusterNums.keySet()){
					BitSet clusts = clusterNums.get(tid);
					int c = clusts.nextSetBit(0);
					while (c != -1){
						query = "INSERT INTO " + config.dbName + "." + clusterTable +
				        " VALUES ( " + tid + "," + c +  "  ) ";
						//System.out.println(query);
						mysqlDB.executeUpdate(query);
						c = clusts.nextSetBit(c+1);
					}
				}
				//t3 = System.currentTimeMillis();
				//System.out.println("Similar Pairs Generation: " + (t3-t2) + "ms");
			}

		} catch (Exception e) {
			System.out.println("Database error"); e.printStackTrace();
		}
	}
	
	public static void loadClusters(HashMap<Integer,BitSet> clusterNums, String  clusterTable){
		
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		Boolean log_pairs_to_db = true;
		
		//String clusterTable = "Cluster_" + tablename; // + "_" + Preprocess.extractMetricName(measure.getClass().getName());
		
		try {
			
			String sql = "SELECT * from " + config.dbName + "." + clusterTable;
			
			ResultSet rs = mysqlDB.executeQuery(sql);
			
			rs.beforeFirst();
			while (rs.next()){

				int tid = rs.getInt(1);
				int cid = rs.getInt(2);
				
				if (clusterNums.containsKey(tid)){
					BitSet b = clusterNums.get(tid);
					b.set(cid);
					clusterNums.put(tid, b);
				} else {
					BitSet b = new BitSet();
					b.set(cid);
					clusterNums.put(tid, b);
				}
			}

		} catch (Exception e) {
			System.out.println("Database error"); e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {

		String algorithm = "center";
		double startThr = 0.1, stopThr = 0.91, step = 0.1;
		boolean show_times = false;

		boolean log_results_to_db = true;
		String resultsTable = "clustering_results";
		
		String datasetsTable = "datasets";
		
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		Vector<String> tables = new Vector<String>();
		HashMap<String, String> tableclass = new HashMap<String, String>();
		
		try {
			String query = "";
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
		
		
		//tables.add("m1");

		
		config = new Config();
		mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		
		if (log_results_to_db) 
		try {
			String query = "";
			
			//query = "DROP TABLE IF EXISTS " + config.dbName + "." + resultsTable; mysqlDB.executeUpdate(query);
			
			query = "CREATE TABLE IF NOT EXISTS " + config.dbName + "." + resultsTable 
					+ " (alg varchar(10),tbl varchar(10), class varchar(10), thr1 double, thr2 double, pcpr double, cpr double, pr double, re double, f1 double, ccount int, " +
					  " PRIMARY KEY (alg, tbl, class, thr1, thr2) )";
			//System.out.println(query);
			mysqlDB.executeUpdate(query);
			//mysqlDB.close();
		} catch (Exception e) {
			System.err.println("DB Error");
			e.printStackTrace();
		} 
		
		
		//
		//
		//
		
		
		for (String tablename: tables) {
			
			System.out.println("*********************");
			System.out.println("Table: " + tablename);
			System.out.println("*********************");
			
			double thr = startThr;
			
			while (thr <= stopThr) {			
				
				long t1, t2, t3, t4, t5, tf;
				t1 = System.currentTimeMillis();
				/*
				 * 
				 * Call Similarity Join
				 * 
				 */
				
				Preprocess bm25WeightedJaccard = new WeightedJaccardBM25();
				Preprocess measure = bm25WeightedJaccard;
				
				
				
				String pairTable = "pairs_" + tablename + "_" + Preprocess.extractMetricName(measure.getClass().getName());
	
				//		 Finding clusters from pairs:
				HashMap<Integer,Integer> cluster = new HashMap<Integer,Integer>();
				HashMap<Integer,BitSet> members = new HashMap<Integer,BitSet>();

				//findPairs(tablename, measure, pairTable, thr);
				//findClusters(pairTable, cluster, members);
				RunCLINKClustering.findClustersCLINK(tablename, measure, cluster, members, thr);
				
				t4 = System.currentTimeMillis();
				if (show_times) System.out.println("Time for clustering: " + (t4-t1) + "ms");
				//System.out.println(members.size());
				/*
				 * Evaluation of clustering - Precision and Recall
				 */
				
				// Finding true clusters:
				HashMap<Integer,Integer> trueCluster = new HashMap<Integer,Integer>();
				HashMap<Integer,BitSet> trueMembers = new HashMap<Integer,BitSet>();
				findTrueClusters(tablename, trueCluster, trueMembers);
		
				//System.out.println(members);
				//System.out.println(trueMembers);
				
				/*
				int count = 0;
				int sumSize = 0;
				for (int cid:trueMembers.keySet()){
					if (trueMembers.get(cid).cardinality() >= 0) count++;
					sumSize += trueMembers.get(cid).cardinality();
				}
				System.out.println("** " + ((double)sumSize/count) );
				*/
				//System.out.println();
				
				
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
				
				//saveClusters(clusterNums, tablename);
				
				//clusterNums = new HashMap<Integer, BitSet>();
				//String clusterTable = "Cluster_" + tablename;
				//String clusterTable = "artpt_m3_05";
				//loadClusters(clusterNums,clusterTable);
				
				System.out.println(tablename + " Thr: " + thr);
				//System.out.println(trueCluster);
				//evaluate2(clusterNums, members, trueCluster, trueMembers);
				Vector<Double> output = new Vector<Double>();
				
				evaluateClustering(clusterNums, trueCluster, output);
				
				if (log_results_to_db) try {
					String query = "REPLACE INTO " + config.dbName + "." + resultsTable 
							+ " VALUES ( \"" + algorithm + "\", \""
							+ tablename + "\" , \"" + tableclass.get(tablename) + "\" , "
							+ thr + " , " + 0.0 + " , " +  output.get(0) + " , " +  output.get(1)
							+ " , " + output.get(2) + " , " + output.get(3) +  " , " + output.get(4) + " , " + output.get(5) +  " )";
					//System.out.println(query);
					mysqlDB.executeUpdate(query);
					
				} catch (Exception e) {
					System.err.println("DB Error");
					e.printStackTrace();
				}
				
				
				//evaluate2(members, trueCluster, trueMembers);
				
				//tf = System.currentTimeMillis();
				//System.out.println("Total Time: " + (tf-t1) + "ms");
				
				thr += step;
				
			}
		
		}
		
		try {
			mysqlDB.close();
		} catch (Exception e) {
			System.err.println("DB Error");
		}
	}
}