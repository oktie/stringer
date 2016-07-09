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
import java.util.Vector;

import dbdriver.MySqlDB;
import utility.Config;

public class EvaluateSignThread extends Thread{

	private String tablename;
	private Preprocess measure;
	private int par1;
	private int par2;
	private int par3;
	
	public static int queryTokenLength = 2;
	
	public EvaluateSignThread(String inputtablename, Preprocess inputmeasure,
			                  int param1, int param2, int param3){
		tablename = inputtablename;
		measure = inputmeasure;
		par1 = param1;
		par2 = param2;
		par3 = param3;
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
	
	public void findPairsWSign(String tablename, Preprocess measure, String pairTable){
		
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		
		long t2, t3;
		t2 = System.currentTimeMillis();
		boolean log_pairs_to_db = true;
		String sigTable = "signs_" + tablename + "_" + Preprocess.extractMetricName(measure.getClass().getName()) + "_" + par1 + "_" + par2 + "_" + par3;

		sigTable = config.dbName + "." + sigTable;
		
		String sql = " SELECT s1.tid as tid1, s2.tid as tid2 " +
                     " FROM " + sigTable + " s1, " + sigTable + " s2 " +
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
		
		System.out.println( " Average Recall for all records: " + (SumR/(double)members.keySet().size()) );
		System.out.println( " Average Recall for records present in clusters: " + (SumR2/(double)ClusterCount) );
		//System.out.println(members.keySet().size());
		//System.out.println(trueMembers.keySet().size());

		
	}
	
	public static void findClusters(String pairTable, HashMap<Integer,Integer> cluster, 
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
	
	public void evaluateAc(String tablename, String pairTable, Vector<Double> output){
		
		int pr = 0, re = 0, total = 0, totalr = 0;
		
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		ResultSet rs;
		
		HashMap<Integer, BitSet> truePairs = new HashMap<Integer, BitSet>();
		HashMap<Integer, BitSet> pairs = new HashMap<Integer, BitSet>();
		
		String sql = " SELECT c1.tid as tid1, c2.tid as tid2 FROM " 
			          + config.dbName + "." + tablename + " c1, " + config.dbName + "." + tablename + " c2 " +
				     " WHERE  c1.id = c2.id and c1.tid < c2.tid ";
		
		try {
			rs = mysqlDB.executeQuery(sql);
			
			while (rs.next()){
				
				int tid1 = rs.getInt(1);
				int tid2 = rs.getInt(2);
				BitSet b = new BitSet();				
				
				if (!(truePairs.keySet().contains(tid1))) {
					b.set(tid2);
					truePairs.put(tid1, b);
				}
				else {
					b.set(tid2);
					b.or(truePairs.get(tid1));
					truePairs.put(tid1, b);
				}
			}
			
			sql = " SELECT * FROM " + config.dbName + "." + pairTable +
			      " WHERE tid1 < tid2 ";
			rs = mysqlDB.executeQuery(sql);
			
			
			while (rs.next()){
				
				total++;
				int tid1 = rs.getInt(1);
				int tid2 = rs.getInt(2);
				BitSet b = new BitSet();

				//System.out.println(tid1 + "," + tid2);
				if ((truePairs.keySet().contains(tid1)))
				if (truePairs.get(tid1).get(tid2)){
					pr++;
				}
				
				if (!(pairs.keySet().contains(tid1))) {
					b.set(tid2);
					pairs.put(tid1, b);
				}
				else {
					b.set(tid2);
					b.or(pairs.get(tid1));
					pairs.put(tid1, b);
				}
				
			}
			
			for (Integer tid1:truePairs.keySet()){
				int tid2 = truePairs.get(tid1).nextSetBit(0);
				while (tid2 != -1){
					totalr ++;
					if ((pairs.keySet().contains(tid1)))
					if (pairs.get(tid1).get(tid2)){
							re++;
						}
					tid2 = truePairs.get(tid1).nextSetBit(tid2+1);
				}
			}

			
			
		} catch (Exception e) {
			System.out.println("Database error"); e.printStackTrace();
		}
		//System.out.println(pairs);
		
		double Precision = ((double)pr/(double)total);
		double Recall = ((double)re/(double)totalr);
		//System.out.println((double)pr);
		//System.out.println((double)total);
		//System.out.print(" Precision: "  + Precision);
		//System.out.println();
		//System.out.println((double)re);
		//System.out.println((double)totalr);
		//System.out.println(" Recall: "  + Recall );
		
		Double F1 = (2*Precision*Recall / (Precision+Recall));
		//System.out.println(" F1: "  + F1 );
		output.add(Precision);
		output.add(Recall);
		output.add(F1);
		
	}

	
	public void run() {

		//String tablename = "cu7";
		
		long t1, t2, t3, t4, t5, tf;
		t1 = System.currentTimeMillis();
		/*
		 * 
		 * Call Similarity Join
		 * 
		 */
		

		/*
		t1 = System.currentTimeMillis();
		SignatureGen.generateSignatures(tablename,measure);
		t2 = System.currentTimeMillis();
		System.out.println("Similarity Join (Signature Generation): " + (t2-t1) + "ms");
		*/
		t2 = System.currentTimeMillis();
		
		String pairTable = "pairs_" + tablename + "_" + Preprocess.extractMetricName(measure.getClass().getName());
		findPairsWSign(tablename, measure, pairTable);
		t3 = System.currentTimeMillis();
		System.out.println("Finding Pairs took: " + (t3-t2) + "ms");

		/*		 Finding clusters from pairs:
		HashMap<Integer,Integer> cluster = new HashMap<Integer,Integer>();
		HashMap<Integer,BitSet> members = new HashMap<Integer,BitSet>();

		findClusters(pairTable, cluster, members);
		
		t4 = System.currentTimeMillis();
		System.out.println("Time for clustering: " + (t4-t1) + "ms");
		
		/*
		 * Evaluation of clustering - Precision and Recall
		 * /
		
		// Finding true clusters:
		HashMap<Integer,Integer> trueCluster = new HashMap<Integer,Integer>();
		HashMap<Integer,BitSet> trueMembers = new HashMap<Integer,BitSet>();
		findTrueClusters(tablename, trueCluster, trueMembers);
		
		evaluate(cluster, members, trueCluster, trueMembers);
		
		//tf = System.currentTimeMillis();
		//System.out.println("Total Time: " + (tf-t1) + "ms");
		*/
		
		System.out.println("T/M/p1/p2/p3: " + tablename + " / " + Preprocess.extractMetricName(measure.getClass().getName()) + " / " + par1 + " / " + par2 + " / " + par3);
		Vector<Double> output = new Vector<Double>();
		evaluateAc(tablename, pairTable, output);
				
		System.out.print(" Precision: "  + output.get(0));
		System.out.println(" Recall: "  + output.get(1) );
		System.out.println(" F1: "  + output.get(2) );
		
		/*
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		String resultTable = "sjoinresults";
		try {
			String query = "REPLACE INTO " + config.dbName + "." + resultTable 
					+ " VALUES ( \"" + tablename + "\", \""
					+ Preprocess.extractMetricName(measure.getClass().getName())
					+ "\" , " + thr + " , " +  output.get(0) + " , " +  output.get(1)
					+ " , " + output.get(2) +  " )";
			//System.out.println(query);
			mysqlDB.executeUpdate(query);
			
			mysqlDB.close();
		} catch (Exception e) {
			System.err.println("DB Error");
			e.printStackTrace();
		}
		*/
		
	}

}