package experiment;


import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Set;
import java.util.Random;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import dbdriver.MySqlDB;
import evaluation.AccuracyMeasure;
import simfunctions.RunProbabilityAssignment;
import utility.Config;
import utility.Util;

public class test {
	
	public static boolean debug_mode = true;
	
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
	
	
	public static BitSet convertToBitSet2(Set<String> stringSet){
		BitSet output = new BitSet();
		for (String qgram : stringSet) {
			output.set((qgram.charAt(1) << 7) | qgram.charAt(0));			
		}
		return output;
	}
	
	
	public int hash(String str){
		int h = Integer.valueOf(str);
		return h;
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
	
	
	public static HashMap<Integer, Integer> perm = new HashMap<Integer, Integer>();

	public static int queryTokenLength = 2;

	public static String getQuery(int tid, String tableName) {
		String resultQuery = "";
		String query = "";
		Config config = new Config();

		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		try {
		
			query = "SELECT " + config.preprocessingColumn + ", id FROM " + config.dbName + "."
					+ tableName + " T WHERE T.tid = " + tid;
			
			//System.out.println("Executing " + query);
			ResultSet rs = mysqlDB.executeQuery(query);
			rs.next();
			resultQuery = rs.getString(config.preprocessingColumn);
			mysqlDB.close();
		} catch (Exception e) {
			System.err.println("Can't generate the query");
			e.printStackTrace();
		}

		return resultQuery;
	}

	public static HashSet<Integer> getAllTidsHavingIdSameAs(int tid, String tableName) {
		HashSet<Integer> tidsHavingThisID = new HashSet<Integer>();
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		try {
			String query = "SELECT tid FROM " + config.dbName + "." + tableName + " where id=" + 
			               "(SELECT id FROM " + config.dbName + "." + tableName + " t where t.tid= " + tid +")";
			//System.out.println("Executing " + query);
			ResultSet rs = mysqlDB.executeQuery(query);
			if (rs != null) {
				while (rs.next()) {
					tidsHavingThisID.add(rs.getInt("tid"));
				}
			}
			mysqlDB.close();
		} catch (Exception e) {
			System.err.println("Can't run query");
			e.printStackTrace();
		}
		return tidsHavingThisID;
	}

	// The sortOrder defines the ordering for the tuples having similar scores
	public static int[] generateBooleanList(HashSet<Integer> actualResult, List<IdScore> scoreList, int sortOrder) {
		int[] booleanList = new int[scoreList.size()];
		int booleanListCounter = 0;
		double oldScore = 0, newScore = 0;
		ArrayList<Integer> tempBooleanList = new ArrayList<Integer>();

		// For the first element
		newScore = scoreList.get(0).score;
		oldScore = scoreList.get(0).score;
		if (actualResult.contains(scoreList.get(0).id + 1)) {
			tempBooleanList.add(1);
			Util.printlnDebug("Got match at position: "+1);
		} else {
			tempBooleanList.add(0);
		}

		for (int i = 1; i < scoreList.size(); i++) {
			newScore = scoreList.get(i).score;
			if (newScore != oldScore) {
				// sort the old list and set the values in the actual
				// booleanList
				Collections.sort(tempBooleanList);
				if (sortOrder != 0) {
					Collections.reverse(tempBooleanList);
				}
				for (int k = 0; k < tempBooleanList.size(); k++) {
					booleanList[booleanListCounter++] = tempBooleanList.get(k);
				}
				
				tempBooleanList = new ArrayList<Integer>();
				oldScore = newScore;

				if (actualResult.contains(scoreList.get(i).id + 1)) {
					tempBooleanList.add(1);
					Util.printlnDebug("Got match at position: "+ (i+1));
				} else {
					tempBooleanList.add(0);
				}
			} else {
				if (actualResult.contains(scoreList.get(i).id + 1)) {
					tempBooleanList.add(1);
					Util.printlnDebug("Got match at position: "+ (i+1));
				} else {
					tempBooleanList.add(0);
				}
			}
		}
		Collections.sort(tempBooleanList);
		if (sortOrder != 0) {
			Collections.reverse(tempBooleanList);
		}
		for (int k = 0; k < tempBooleanList.size(); k++) {
			booleanList[booleanListCounter++] = tempBooleanList.get(k);
		}
		// For the last block of tempBooleanList
		return booleanList;
	}

	
	
	public static HashMap<Integer, Integer> permutation(int n){
		HashMap<Integer, Integer> output = new HashMap<Integer, Integer>();
		
		BitSet allnums = new BitSet();
		allnums.set(1, n+1, true);
		
		Random rand = new Random(2353);
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
	
	public static HashMap<Integer, Integer> permutation(int n, int rnd){
		HashMap<Integer, Integer> output = new HashMap<Integer, Integer>();
		
		BitSet allnums = new BitSet();
		allnums.set(1, n+1, true);
		
		Random rand = new Random(rnd);
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
	
	public static void tst(int i, Integer j, Vector<String> vStr, HashMap<Integer, Double> hm){
		i = 1;
		j = 0;
		vStr.add("ey baba!");
		hm.put(1, 2.2);
	}
	
	class pair {
	    private float x;
	    private float y;

	    public pair(float x, float y) {
	        this.x = x;
	        this.y = y;
	    }

	    public float getX() { return x; }
	    public float getY() { return y; }

	    public void setX(float x) { this.x = x; }
	    public void setY(float y) { this.y = y; }
	}
	
	public static HashMap<Integer, Double> getProbabilities(HashMap<Integer, String> strs){
		

		
		int size = 0;
		BitSet repSet = new BitSet();
		HashMap<Integer, Double> repWeights = new HashMap<Integer, Double>();
		
		
		/*
		 * 
		 * Finding Cluster Representative
		 * 
		 */
		int n=0;
		BitSet striSet = new BitSet();
		HashMap<Integer, Integer> strLens = new HashMap<Integer, Integer>(); 
		for (int sin:strs.keySet()){
			String stri = strs.get(sin);
			BitSet bb = convertToBitSet2(RunProbabilityAssignment.getTF(stri).keySet());
			strLens.put(sin, bb.cardinality());
			striSet.or(bb);
		}
		int ii = striSet.nextSetBit(0);
		while (ii!=-1){
			n++;
			ii = striSet.nextSetBit(ii+1);
		}
		//System.out.println("n: " + n);
		
		Double repP = 0.0;
		
		for (int sn:strs.keySet()){
			String str = strs.get(sn); 
			BitSet strSet = convertToBitSet2(RunProbabilityAssignment.getTF(str).keySet());
			HashMap<Integer, Double> strWeights = new HashMap<Integer, Double>();
			
			Double strP = ((double)strLens.get(sn)/n);
			
			int i = strSet.nextSetBit(0);
			while (i!=-1){
				strWeights.put(i, (1.0/(double)strSet.cardinality()) );
				
				if (!repSet.get(i)) {repWeights.put(i, 0.0);}
				
				i = strSet.nextSetBit(i+1);
			}
			
			Set<String> s = convertToStringSet(repSet);
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
			BitSet strSet = convertToBitSet2(RunProbabilityAssignment.getTF(str).keySet());
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
			//probs.put(sn,sim/strSet.cardinality());
			//sum += sim/strSet.cardinality();
			//BitSet copy = new BitSet();
			//copy = (BitSet)(strSet.clone());
			//copy.and(repSet);
			//probs.put(sn,sim/copy.cardinality());
			//sum += sim/copy.cardinality();
			probs.put(sn,(sim/total));
			sum += (sim/total);
		}

		
		for (int sn:strs.keySet()){
			//String str = strs.get(sn); 
			probs.put(sn, probs.get(sn)/sum);
			if (debug_mode) {System.out.println(strs.get(sn) + " " + probs.get(sn));}
		}
		if (debug_mode) {System.out.println();}
		return probs;
		
	}
	
	
	
	class ipair {
	    private int x;
	    private int y;

	    public ipair(int x, int y) {
	        this.x = x;
	        this.y = y;
	    }
	    
	    public void set(int x, int y) {
	        this.x = x;
	        this.y = y;
	    }

	    public int getX() { return x; }
	    public int getY() { return y; }

	    public void setX(int x) { this.x = x; }
	    public void setY(int y) { this.y = y; }
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
//			a) Ascending sort
	 
			for (int i=0; i<size; i++)	{
				map.put(mapKeys.get(mapValues.indexOf(sortedArray[i])), sortedArray[i]);
			}
			return map;
		}

	
	
	public static HashMap<Integer, String> getClusters(HashMap<Integer, String> strs,
			                           Vector<Integer> tids){
		
		
		HashMap<Integer, BitSet> C = new HashMap<Integer, BitSet>();
		
		HashMap<Integer, Double> p_x = new HashMap<Integer, Double>();
		
		HashMap<Integer, HashMap<Integer,Double>> p_y_x = new HashMap<Integer, HashMap<Integer,Double>>();
		
		// initialize clusters
		BitSet X = new BitSet();
		BitSet Y = new BitSet();
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
				
				py.put(i, 1.0/strSet.cardinality());
				
				i = strSet.nextSetBit(i+1);
			}
			
			p_x.put(tid, 1.0/strs.size());
			p_y_x.put(tid, py);
			
		}
		
		HashMap<Integer, Double> p_c = new HashMap<Integer, Double>();
		HashMap<Integer, HashMap<Integer, Double>> p_y_c = new HashMap<Integer, HashMap<Integer, Double>>();
		for (int cid: C.keySet()){
			BitSet xs = C.get(cid);
			
			Double spx = 0.0;
			int i = xs.nextSetBit(0);
			while (i!=-1){
				Y.set(i);
				
				spx += p_x.get(i);
				
				i = xs.nextSetBit(i+1);
			}
			p_c.put(cid, spx);
			
			
			HashMap<Integer,Double> sum_px_pyx = new HashMap<Integer,Double>();
			i = xs.nextSetBit(0);
			while (i!=-1){
				Y.set(i);
				
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
		
		System.out.println("X: " + X);
		System.out.println("Y: " + Y);
		System.out.println("C: " + C);
		
		System.out.println("p_y_c: " + p_y_c);
		System.out.println("p_y_x: " + p_y_x);
		System.out.println();
			
		// Merge clusters ci,cj :: merge(clusters, p_c, p_y_c, i, j)
		int cid1 = 1;
		int cid2 = 2;
		
		//merge(C, cid1, cid2, p_c, p_y_c);
		double il = 0.0;
		//il = infoLoss(C, cid1, 2, p_c, p_y_c);
		//System.out.println(" 2: " + il);
		
		double avgil = 0.0;
		
		int i = 0;
		
		HashMap<Integer, Double> ils = new HashMap<Integer, Double>(); 
		for (int tid2: tids){
			i++;
			if (i<4){
				merge(C, cid1, tid2, p_c, p_y_c);
				System.out.println(" " + tid2 + " merged.");
			} else {
				il = infoLoss(C, cid1, tid2, p_c, p_y_c);
				ils.put(tid2,il);
				avgil += il;
				//System.out.println(" " + tid2 + ": " + il);
			}
		}
		System.out.println("Avg: " + (avgil/(strs.size()-1)));
		System.out.println();
		
		
		//Collections.sort(ils);
		ils = getSortedMap(ils);
		int ii = 0;
		for (Integer d: ils.keySet()){
			System.out.println(" " + d + ": " + ils.get(d));
			ii++;
		}
		//System.out.println(ils);
		
		/*
		merge(C, cid1, cid2, p_c, p_y_c);
		
		System.out.println("C: " + C);
		
		System.out.println("p_y_c: " + p_y_c);
		System.out.println("p_c: " + p_c);
		*/
		HashMap<Integer,String> clusters = new HashMap<Integer,String>();
		return clusters;
		
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
	
	public static void main(String[] args) {
		
		/*
		StringBuffer slist[] = new StringBuffer[5000];
		double list[][] = new double[5000][5000];
		for (int i = 0; i<5000; i++){
			slist[i] = new StringBuffer("*");
			for (int j = 0; j<5000; j++){
				list[i][j] = 0.5;				
				slist[i] = slist[i].append(" *");
				//if (j%1000 == 0) System.out.println(slist[i]);
				
			}
			System.out.println(i);
			if (i%10 == 0) System.out.println(slist[i]);
		}
		*/
		HashMap<Integer, Double> ils = new HashMap<Integer, Double>();
		ils.put(1,1.0);
		ils.put(2,3.0);
		ils.put(3,2.0);
		for (Integer d: ils.keySet()){
			System.out.println(" " + d + ": " + ils.get(d));
		}
		System.out.println("Sorted:");
		ils = getSortedMap(ils);
		for (Integer d: ils.keySet()){
			System.out.println(" " + d + ": " + ils.get(d));
		}
		
		/*
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		
		String sql = " SELECT c.tid, c.`string`, s.score" +
		" FROM scores_cu1_weightedjaccardbm25 s, cnamesu.cu1 c" +
		" where tid1=1 and s.tid2 = c.tid and score > 0.15" +
		" order by score desc";
		
		ResultSet rs;
		
		HashMap<Integer,String> strs = new HashMap<Integer,String>();
		
		try{
			rs = mysqlDB.executeQuery(sql);
			
			rs.next();
			strs.put(rs.getInt(1), rs.getString(2));
			
			HashMap<Integer,Double> probs = getProbabilities(strs);
			int prob = (rs.getInt(1));
			
			System.out.println(rs.getString(2) + ":" + ((probs.get(prob)) - probs.get(rs.getInt(1))) );
			//int i=1;
			//debug_mode = true;
			rs.next();
			strs.put(rs.getInt(1), rs.getString(2));
			probs = getProbabilities(strs);
			prob = (rs.getInt(1));			
			System.out.println(rs.getString(2) + ":" + ((probs.get(prob)) - probs.get(rs.getInt(1))) );
			
			rs.next();
			strs.put(rs.getInt(1), rs.getString(2));
			probs = getProbabilities(strs);
			prob = (rs.getInt(1));			
			System.out.println(rs.getString(2) + ":" + ((probs.get(prob)) - probs.get(rs.getInt(1))) );

			
			
			while (rs.next()){
				strs.put(rs.getInt(1), rs.getString(2));
				//debug_mode=true;
				probs = getProbabilities(strs);
				//debug_mode=false;

				System.out.println(rs.getString(2) + ":" + Math.abs((probs.get(prob)) - probs.get(rs.getInt(1))) );

				if (Math.abs((probs.get(prob)) - probs.get(rs.getInt(1))) > 0.02) {
					strs.remove(rs.getInt(1));
				}
				else prob = (rs.getInt(1));

				//i++;
			}
		} catch(Exception e){
			System.out.println("Error: " + e.toString());
		}
		
		System.out.println();
		System.out.println();
		System.out.println();
		debug_mode = true;
		HashMap<Integer,Double> probs = getProbabilities(strs);
		
		
		
		
		/*/
		 
		
		/*
		    William Turner     
			Willaim Turner
			Willliam Turnet
			Will Turner
		 */
		
/*
		HashMap<Integer, BitSet> pairs = new HashMap<Integer, BitSet>();
		System.out.println(pairs);
		BitSet x = new BitSet();
		x.set(1);
		x.set(2);
		pairs.put(1, x);
		System.out.println(pairs);
		if (pairs.keySet().contains(1)) {System.out.println("1");}
		if (pairs.keySet().contains(2)) {System.out.println("2");}
	*/	
		/*
		HashMap<Integer,String> strs = new HashMap<Integer,String>();
		strs.put(1,"Willaim Turner");		
		HashMap<Integer,Double> probs = getProbabilities(strs);
		System.out.println();

		strs.put(2,"William Turner");
		probs = getProbabilities(strs);
		System.out.println();
		
		strs.put(3,"Willliam Turnet");
		probs = getProbabilities(strs);
		System.out.println();
		
		strs.put(4,"will turn");
		probs = getProbabilities(strs);
		System.out.println();
		*/
		
/*
		HashMap<Integer,String> strs = new HashMap<Integer,String>();
		strs.put(1,"William Turner");		
		strs.put(2,"Willaim Turner");
		strs.put(3,"Willliam Turnet");
		strs.put(4,"will turn");
		strs.put(5,"oks s");
		
		strs.put(1,"Oktie Hassanzadeh");		
		strs.put(2,"Oktai Hassan zadeh");
		strs.put(3,"Oktide hasssanzadeh");
		strs.put(4,"kalanjoon hoseinpour");
		strs.put(5,"Oktay Hasanov");

		HashMap<Integer,String> clusters = new HashMap<Integer,String>();
		clusters = getClusters(strs);
		//System.out.println();
		

		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		
		String sql = " SELECT c.tid, c.`string`, s.score" +
		" FROM scores_cu5_weightedjaccardbm25 s, cnamesu.cu5 c" +
		" where tid1=1 and s.tid2 = c.tid and score > 0.2" +
		" order by score desc";
		
		ResultSet rs;
		
		HashMap<Integer,String> strs = new HashMap<Integer,String>();
		Vector<Integer> tids = new Vector<Integer>();
		
		try{
			rs = mysqlDB.executeQuery(sql);
			while (rs.next()){
				strs.put(rs.getInt(1), rs.getString(2));
				tids.add(rs.getInt(1));
			}
		}
		catch(Exception e){
			System.out.println("Error: " + e.toString());
		}		
		
		HashMap<Integer,String> clusters = new HashMap<Integer,String>();
		//clusters = getClusters(strs, tids);
		
		HashMap<Integer,BitSet> p = new HashMap<Integer,BitSet>();
		BitSet b = new BitSet();
		b.set(1); b.set(3); b.set(4); b.set(15);
		p.put(1, b);
		System.out.println(p);
		
		BitSet m = (BitSet) p.get(1).clone();
		 
		m.set(12);
		System.out.println(p);
		
		
/*		
		HashMap<Integer,String> strs = new HashMap<Integer,String>();
		strs.put(2,"Oktie Hassanzadeh Corp");		
		HashMap<Integer,Double> probs = getProbabilities(strs);
		System.out.println();

		strs.put(6,"kalanjoon hoseinpour");
		probs = getProbabilities(strs);
		System.out.println();
		
		strs.put(3,"Oktay Hasanov");
		probs = getProbabilities(strs);
		System.out.println();
		

		
		strs.put(4,"Oktai Hassan zadeh");
		probs = getProbabilities(strs);
		System.out.println();
		strs.put(5,"Oktide hasssanzadeh");
		probs = getProbabilities(strs);
		System.out.println();		
		/*
		Vector<Integer> v = new Vector<Integer>();
		v.add(1);
		v.add(2);
		v.add(3);
		v.add(4);
		System.out.println(v);
		
		boolean[][] correctOrder = new boolean[v.size()][v.size()];
		for (int i=0; i<v.size(); i++)
			for (int j=0; j<v.size(); j++)
				correctOrder[i][j]=false;
		
		int size = 0;
		for (int i=0; i<v.size(); i++){
			for (int j=i+1; j<v.size(); j++){
				size++;
				correctOrder[v.get(i)-1][v.get(j)-1] = true;
				System.out.println(v.get(i) + ", " + v.get(j));
			}
		}
		
		Vector<Integer> v2 = new Vector<Integer>();
		v2.add(4);
		v2.add(3);
		v2.add(1);
		v2.add(2);
		//Collections.sort(v2);
		//v2 = new Vector<Integer>();
		System.out.println(v2);
		
		/*
		HashMap<Integer, Double> probs = new HashMap<Integer, Double>();
		probs.put(1, 0.5);
		probs.put(2, 0.2);
		probs.put(3, 0.2);
		probs.put(4, 0.6);
		
		List mapValues = new ArrayList(probs.values());
		
		Vector<Double> sortedProbs = new Vector<Double>();
		for (Double prob:probs.values()){
			sortedProbs.add(prob);
		}
		Collections.sort(sortedProbs);
		
		
		for (Double prob:sortedProbs){
			//System.out.println(prob);
			int m = mapValues.indexOf(prob);
			System.out.println(m+1);
			mapValues.set(m, -1);
		}
		* /
		
		int correct = 0;
		for (int i=0; i<v2.size(); i++){
			for (int j=i+1; j<v2.size(); j++){
				if (correctOrder[v2.get(i)-1][v2.get(j)-1]) {
					correct++;
					System.out.println(v2.get(i) + ", " + v2.get(j));
				}
			}
		}		
		System.out.println("Percentage: " + correct + "/" + size);
		
		
		
		/*

		int i = -1;
		Integer j = -1;
		Vector<String> vStr = new Vector<String>();
		HashMap<Integer, Double> hm = new HashMap<Integer, Double>();
		System.out.println(i + " " + j + " " + vStr + " " + hm);
		tst(i,j,vStr,hm);
		System.out.println(i + " " + j + " " + vStr + " " + hm);
		
		
		
		/*
		String str1 = "ab";
		String str2 = "$$";
		

		char[] chars = str1.toCharArray();		
		int h1 = str1.charAt (1) << 7 | str1.charAt (0);
		int h2 = str2.charAt (0) << 7 | str2.charAt (1);
		

		System.out.println("hash(" + str1 + ") = " + (h1) );
		System.out.println("hash(" + str1 + ") = " + (h2) );
		
		BitSet b = new BitSet();
		long n = 327544767;
		//for (int i=0; i<32; i++)
		//	System.out.println("bit(" + i + ") = " + ( ((n & (1 << i)) >>> i) == 1 ? 1 : 0   ) );
		
		long one = 1;
		long n2 = (one << 40);
		System.out.println(n2);
		
		for (int i=63; i>=0; i--)
			System.out.print(( ((n2 & (one << i)) >>> i) == 1 ? 1 : 0   ) );
		
	  	*/
		
		/*
		Vector<Integer> m1 = new Vector<Integer>(2);
		m1.add(1);
		m1.add(2);
		m1.add(3);
		
		Vector<Integer> m2 = new Vector<Integer>();
		m2.add(1);
		m2.add(2);
		m2.add(3);
		
		System.out.println(m1.equals(m2));
		* /
		
		
		Set<String> qgrams = new HashSet<String>();
		Set<String> qgrams1 = new HashSet<String>();
		Set<String> qgrams2 = new HashSet<String>();
		qgrams1.add("ab");
		qgrams1.add("df");
		qgrams1.add("et");
		qgrams2.add("se");
		qgrams2.add("df");
		qgrams2.add("gh");
		
		qgrams.add("ab");
		qgrams.add("df");
		qgrams.add("et");
		qgrams.add("se");
		qgrams.add("df");
		qgrams.add("gh");
		
		System.out.println(qgrams1.toString());
		System.out.println(qgrams2.toString());
		
		setQgramNumbers(qgrams);
		BitSet b1 = convertToBitSet(qgrams1);
		BitSet b2 = convertToBitSet(qgrams2);
		System.out.println(b1.toString());
		System.out.println(b2.toString());
		
		qgrams = convertToStringSet(b1);
		System.out.println(qgrams.toString());
		
		/*
		
		//System.out.println(1 << 14);
		//HashMap<Integer, Integer> perm = permutation(1 << 14);
		//System.out.println(perm.toString());

		BitSet v = new BitSet();
		int N = 6;
		v.set(2); v.set(4); v.set(5);
		int k=3, n1=2, n2=3;
		int k2 = (k+1)/n1 - 1;
		
		//HashMap<Integer, Integer> perm = permutation(6);
		
		//for (int i=0; i<=N; i++)	perm.put(i, i);
		 * 
		 * /
		perm = permutation(6);
		System.out.println(perm);
		
		Vector<Integer> v = new Vector<Integer>();
		v.add(2); v.add(7); v.add(9);
		System.out.println(subsets(v,2));
		
		/*
		//BitSet[] p = new BitSet[65];
		for (int i=1; i<=n1; i++){
			BitSet bb1 = p(i,1,n1,n2,N);
			BitSet bb2 = p(i,2,n1,n2,N);
			bb1.or(bb2);
			System.out.println(bb1);
			System.out.println(p(i,3,n1,n2,N));
		}
		* /
		
		BitSet test = new BitSet();
		test.set(1, 7, true);
		
		//Vector<Integer> vv = new Vector<Integer>();
		//vv.add(1); vv.add(2); vv.add(3); vv.add(4); vv.add(5); vv.add(6);
		//System.out.println(subsets(vv, 2));

		System.out.println(v.toString());
		
		Vector<Integer> vv = new Vector<Integer>();
		for (int i=1; i<=n2; i++) vv.add(i);
		
		for (int i=1; i<=n1; i++){
			
			for (Vector<Integer> subset:subsets(vv, n2-k2)){
				BitSet P = new BitSet();
				for (Integer j: subset){
					P.or(p(i,j,n1,n2,N));
				}
				
				HashMap<BitSet, BitSet> sign = new HashMap<BitSet, BitSet>();
				BitSet proj = new BitSet();
				proj = (BitSet) v.clone();
				proj.and(P);
				System.out.println("<" + proj + "," + P + ">");
				sign.put(proj, P);
				
				Random rand = new Random();
				Long hash= new Long(0);
				int t = P.nextSetBit(0);
				while (t!=-1){
					rand = new Random(t);
					hash += rand.nextLong();
					t=P.nextSetBit(t+1);
				}
				t = proj.nextSetBit(0);
				while (t!=-1){
					rand = new Random(t);
					hash += rand.nextLong();
					t=proj.nextSetBit(t+1);
				}
				System.out.println(hash);
				//System.out.println((int)(P.hashCode()+proj.hashCode()));
				//System.out.println(P.hashCode());System.out.println(proj.hashCode());
				//System.out.println(sign.hashCode());
			}
			
		}
		    
		*/
		
		

	}

}