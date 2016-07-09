package ricochet;

import java.io.*;
import java.util.*;


import utility.Config;
import dbdriver.MySqlDB;

class TimeSR {

	public int myName[];
	public StringBuffer myNameString[];
	public int myCenter[];
	public int myMark[];
	public double myDegree[];
	public StringBuffer myAdj[];
	public int mySize;
	public int currentCluster = 1;
	public int maxi = 0;

	public Vector listCenters = new Vector();
	public Vector listAdjacent = new Vector();
	public Vector changing = new Vector();
	
	 public String nameCat[], clusterInfo[];
	 public Vector memberCat[];
	 public int countMemberCat[];
	 public float precisionInfo[], recallInfo[], FInfo[];
	 public int exactNoCluster = 0;

	 public StringBuffer naming[] = new StringBuffer[13100];
	
	 public double actualDistance[][];
	 public double myAverage[];
	 
	 public static void run (String myargument, String tablename, float threshold, String thresholdS) {
		 
		 String args[];
			
		 /* input is specified here in "myargument" , each in the form of: A B C D
		  * where A = number of objects to cluster e.g. 1010
		  * 	  B = name of file containing the similarities (weights of edges) between each pair of objects (directed edge) 
		  * 	      hence, if object 1 has an undirected edge to object 2, this file must contain both these lines:
		  * 		  fil:1 fil:2 sim_between_1_and_2 
		  * 		  fil:2 fil:1 sim_between_2_and_1
		  * 		  "fil:x" is the notation used to specify an object; where x is the (integer) index of the object
		  *       C = number of clusters to expect in the correct clustering (this is for measuring of precision, recall)
		  *       D = name of file containing the correct clustering
		  *       	  each line of this file contains the information about a cluster: 
		  *           i.e. the cluster label and the objects who are member of the cluster
		  *           e.g. babel fil:1 fil:13 fil:20 fil:55
		  *           (in this case babel is the label of the cluster and objects with index 1, 13, 20, 55 are its members)
		  * the program will produce the average precision, recall, and F1 value for the input you specify
		  */
		 //String tablename = "m3";
		 //String thresholdS = "01";
		 //double threshold = 0.1;
		 
		 //String myargument = "1019 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/google1c.txt 15 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/categoryGoogle1c.txt";
		 //String myargument = "4883 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/m3.txt 15 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/categoryGoogle1c.txt";
		 //String myargument = "1936 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/zm3.txt 15 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/categoryGoogle1c.txt";
		 //String myargument = "199 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/zm3c.txt 15 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/categoryGoogle1c.txt";
		 //String myargument = "1936 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/test.txt 15 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/categoryGoogle1c.txt";
		 
		 
		 if (myargument.length()>0) {
			 StringTokenizer starg = new StringTokenizer(myargument);
			 args = new String[starg.countTokens()];
			 StringTokenizer zt = new StringTokenizer(myargument);
			 int kz=0;
			 while (zt.hasMoreTokens()) {
				 args[kz] = zt.nextToken().trim();
				 kz++;
			 }
			 TimeSR zozo = new TimeSR();
			 int k = Integer.valueOf(args[0]).intValue();
			 zozo.intialize(k);
			 long start = System.currentTimeMillis();				
			 long stop = System.currentTimeMillis();
			 long timeRange = stop-start;
			 start = System.currentTimeMillis();
			 zozo.readSecondMyFileFlo(args[1], Float.valueOf(0).floatValue(), threshold);
			 zozo.processing();
			 long t1 = System.currentTimeMillis();
			 zozo.sorting(0,k-1,k);
			 long t2 = System.currentTimeMillis();
			 zozo.doClustering();
			 long t3 = System.currentTimeMillis();
			 stop = System.currentTimeMillis();
			 timeRange = stop - start;
			 //String temp=zozo.calculatePRF2(args[2], args[3],k);
			 //StringTokenizer stemp = new StringTokenizer(temp);
			 //float precision = Float.valueOf(stemp.nextToken().trim()).floatValue();
			 //float recall = Float.valueOf(stemp.nextToken().trim()).floatValue();
			 //float Fvalue = 0;
			 //if ((precision+recall)>0) Fvalue = (2*precision*recall)/(precision+recall);
			 //System.out.println("precision : "+precision+"\nrecall : "+recall+"\nF1 : "+Fvalue+"\ntime : "+timeRange);
			 System.out.println("time : "+timeRange + "ms");
			 
			 int numClusters = zozo.exactNoCluster;
			 String cinfo[] = new String[numClusters];
			 HashMap<Integer,Integer> tidCluster = new HashMap<Integer,Integer>();
			 int countMember = 0;
			 for (int i=0; i<numClusters; i++){
				 //if (zozo.clusterInfo[i].equals("null")) System.out.println(i + ": " + zozo.clusterInfo[i]);
				 StringTokenizer clusters = new StringTokenizer(zozo.clusterInfo[i]);
			     while (clusters.hasMoreTokens()) {
			     	int tid = Integer.valueOf((clusters.nextToken().trim()));
			     	tidCluster.put(tid, i+1);
			     	countMember++;
			     }
			 }
			 //System.out.println(tidCluster);
			 
			 System.out.println("time for sorting : "+ (t2-t1) + "ms");
			 System.out.println("time for doClustering : "+ (t3-t2) + "ms");
			 System.out.println("time for all clustering : "+timeRange + "ms");
			 
			 //System.out.println(countMember);
			 
			 //for (int i=1; i<=k; i++){
				 //System.out.println(tidCluster.get(i));
			 //}
			 //System.out.println(tidCluster.keySet().size());
			 
			 //System.out.println(tidCluster.size());
			 
			 saveClusters(tidCluster, tablename, thresholdS);
			 

			 
		 }
	}
	 
	void intialize(int k) {
		mySize = k;
		myName = new int[k];
		myNameString = new StringBuffer[k];
		myCenter = new int[k];
		myMark = new int[k];
		myDegree = new double[k];
		myAdj = new StringBuffer[k];
		for (int i = 0; i<k; i++) {
			myCenter[i] = 0;
			myMark[i] = 0;
			myDegree[i] = 0;
			myAdj[i] = new StringBuffer("");
		}
		myAverage = new double[k];
		actualDistance = new double[k][k];
		for (int i=0; i<k; i++) {
			myAverage[i] = 0;
			for (int j=0; j<k; j++) actualDistance[i][j] = -1.0;
		}
	}


	
	 String calculatePRF2(String cs, String ms,int sizeDoc) {
		 sizeDoc = 0;
		 	int noCat = Integer.valueOf(cs).intValue();
		 	nameCat=new String[noCat];
		 	memberCat=new Vector[noCat];
		 	countMemberCat=new int[noCat];
		 	for (int j=0; j<noCat; j++) { memberCat[j]=new Vector(); countMemberCat[j]=0; }
		String filename=ms;
      DataInputStream dis = null; 
      String record = null; 
      Vector ignore = new Vector();
      try { 
      	File f = new File(filename); 
      	FileInputStream fis = new FileInputStream(f); 
      	BufferedInputStream bis = new BufferedInputStream(fis); 
      	dis = new DataInputStream(bis); 
      	int i=0;
      	
      	while ( (record=dis.readLine()) != null ) { 
      		StringTokenizer st = new StringTokenizer(record);
      		String hnameCat = st.nextToken().trim();
      		
      		if (hnameCat.equalsIgnoreCase("none")) {
      			while (st.hasMoreTokens()) {
      				String temp = st.nextToken().trim();
      				ignore.add(temp);
      			}
      		} else {
      		
      			nameCat[i]=hnameCat;
      			while (st.hasMoreTokens()) {
      				String temp = st.nextToken().trim();
      				memberCat[i].add(temp);
      				countMemberCat[i]++;
      				sizeDoc++;
      			}
      			i++;
      		}
      	}               
      } catch (IOException e) { 
         System.out.println("Uh oh, got an IOException error!" + e.getMessage()); 
      } finally { 
      	if (dis != null) { 
      		try {
      			dis.close(); 
      		} catch (IOException ioe) {
      		}
      	} 
      }
      float weightedPrecision=0, weightedRecall=0, weightedFvalue=0;
      for (int newc=0; newc<noCat; newc++) {
	            float maxPrecision=0, maxRecall=0, maxFvalue=0;
      	int i = 0;
      	while (i<exactNoCluster) {
      	int temp=0;
      	StringTokenizer zt = new StringTokenizer(clusterInfo[i]);
      	int countMember = 0;
      	String temps;
      	while (zt.hasMoreTokens()) {
      		temps = zt.nextToken().trim();
      		if (ignore.indexOf(temps)<0) {
      			countMember++;
				if (memberCat[newc].indexOf(temps)>=0) temp++;
      		}
      	}
      	if (countMember>0) {
	            	float tempP=0;
	            	float tempR=0;
	            	float tempFvalue=0;
	            	
	            	tempP=(1F*temp)/(1F*countMember);
	            	tempR=(1F*temp)/(1F*countMemberCat[newc]);
	            		if ((tempP+tempR)>0) tempFvalue = (tempP*tempR)/(tempP+tempR);
	            		else tempFvalue = 0;
	            		if (tempFvalue>maxFvalue) {
	            			maxFvalue=tempFvalue;
	            			maxRecall=tempR;
	            			maxPrecision=tempP;
	            		}		            	
      	}
      	i++;
      	}
      	weightedPrecision = weightedPrecision + (maxPrecision*(1F*countMemberCat[newc]/sizeDoc));
      	weightedRecall = weightedRecall + (maxRecall*(1F*countMemberCat[newc]/sizeDoc));
      	weightedFvalue = weightedFvalue + (maxFvalue*(1F*countMemberCat[newc]/sizeDoc));
	    
      }
      weightedFvalue = (weightedPrecision*weightedRecall)/(weightedPrecision+weightedRecall);
      
      String temp=weightedPrecision+" "+weightedRecall+" "+weightedFvalue;
      return temp;
	 }


    
 
    void readSecondMyFileFlo(String myString, float flo, double threshold) {
		String filename=myString;
        DataInputStream dis = null;
        String record = null;
       try {
        	File f = new File(filename);
        	FileInputStream fis = new FileInputStream(f);
        	BufferedInputStream bis = new BufferedInputStream(fis);
        	dis = new DataInputStream(bis);
        	int index = 0;
        	int indexNameFlo[]=new int[13100];
        	for (int z = 0; z < 13100; z++) indexNameFlo[z]=-1;
        	
        	
    		float dist[][] = new float[mySize][mySize];
    		
    		for (int i=0; i<mySize; i++) {
    			for (int j=0; j<mySize; j++) {
    				dist[i][j] = (float)0.05;
    			}
    		} 
    		    	
        	while ( (record=dis.readLine()) != null ) {
        		StringTokenizer st = new StringTokenizer(record);
        		String one = st.nextToken().trim();
        		String two = st.nextToken().trim();
        		String three = st.nextToken().trim();
        		float distance = Float.valueOf(three).floatValue();
        		int o = Integer.valueOf(one).intValue();
        		int t = Integer.valueOf(two).intValue();
        		
        		if (distance > threshold) 
        			dist[o-1][t-1] = distance;
        		
        	}
        	
        	int cntr=1;
        	for (int i=0; i<mySize; i++) {
    			for (int j=0; j<mySize; j++) {
    				 if (i!=j) {
    		        		//cntr++;
    		        		//record=dis.readLine();
    		        		//System.out.println(record);
    		        		//StringTokenizer st = new StringTokenizer(record);
    		        		//String one = st.nextToken().trim();
    		        		//String two = st.nextToken().trim();
    		        		//String three = st.nextToken().trim();
    		        		float distance = dist[i][j];//Float.valueOf(three).floatValue();
    		        		
    		        		
    		        		
    		        		//StringTokenizer st1 = new StringTokenizer(one,":");
    		        		//StringTokenizer st2 = new StringTokenizer(two,":");
    		        		//String temp1 = st1.nextToken();
    		        		//String temp2 = st2.nextToken();
    		        		//String temp3 = st1.nextToken();
    		        		//String temp4 = st2.nextToken();

    		        		//int o = Integer.valueOf(temp3).intValue();
    		        		//int t = Integer.valueOf(temp4).intValue();
    		        		
    		        		//int o = Integer.valueOf(one).intValue();
    		        		//int t = Integer.valueOf(two).intValue();
    		        		int o = i+1;
    		        		int t = j+1;
    		        		
    		        		
    		        		//temp1 = temp1 + ":" +o;
    		        		//temp2 = temp2 + ":" +t;
    		        		String temp1 = String.valueOf(o);
    		        		String temp2 = String.valueOf(t);
    		        		
    		        		if (indexNameFlo[o]>=0) o = indexNameFlo[o];
    		        		else {
    		        			indexNameFlo[o]=index;
    		        			naming[index]= new StringBuffer(temp1);
    		        			o = index;
    		        			index++;
    		        		}
    		        		if (indexNameFlo[t]>=0) t = indexNameFlo[t];
    		        		else {
    		        			indexNameFlo[t]=index;
    		        			naming[index]= new StringBuffer(temp2);
    		        			t = index;
    		        			index++;
    		        		}
    		    			myName[o] = o;
    		    			myNameString[o] = new StringBuffer(temp1);
    		    			
    		    			actualDistance[o][t] = distance;
    		    			actualDistance[t][o] = distance;
    		    			
    		    			myAverage[o] = myAverage[o] + distance;
    		        		myDegree[o]++;
    		        		
    		        		
    		        		myAdj[o] = myAdj[o].append(t +" ") ;
    		
    		        		
    		        	 
    				 }
    			}
        	}
    			
        	
      
        	//System.out.println(index);
        	System.out.println("my size " + mySize);
        	
        	for (int i=0; i<myDegree.length; i++) {
        		myDegree[i] = myAverage[i]/myDegree[i];
        	}
        	
        	
    		
        	

        	indexNameFlo = null;
        	Runtime.getRuntime().gc();
        	
        } catch (IOException e) {
           System.out.println("Uh oh, got an IOException error! " + e.getMessage());
        } finally {
        	if (dis != null) {
        		try {
        			dis.close();
        		} catch (IOException ioe) {
        		}
        	}
        }
    }
    
    
    void sorting(int left, int right, int size){
    	double v ;
    	int i ;
    	int j ;
    	double t ;
    	boolean cont01;
    	boolean cont02;
    	double aux03 ;
    	t = 0 ;
    	int temp=0;

    	if (left < right){
    	    v = myDegree[right] ;
    	    i = left - 1 ;
    	    j = right ;
    	    cont01 = true ;
    	    while (cont01){
    		cont02 = true ;
    		while (cont02 && (i+1<size)){
    		    i = i + 1 ;
    		    aux03 = myDegree[i] ;
    		    if (!(aux03<v)) cont02 = false ;
    		    else cont02 = true ;
    		}
    		cont02 = true ;
    		while (cont02 && (j>0)){
    		    j = j - 1 ;
    		    aux03 = myDegree[j] ;
    		    if (!(v < aux03)) cont02 = false ;
    		    else cont02 = true ;
    		}


    		t = myDegree[i] ;
    		temp = myName[i];
    		myDegree[i] = myDegree[j] ;
    		myName[i] = myName[j];
    		myDegree[j] = t ;
    		myName[j] = temp;
    		if ( j < (i+1)) cont01 = false ;
    		else cont01 = true ;
    	    }
    	    myDegree[j] = myDegree[i] ;
    	    myName[j] = myName[i];
    	    myDegree[i] = myDegree[right] ;
    	    myName[i] = myName[right];
    	    myDegree[right] = t ;
    	    myName[right] = temp;
     	    this.sorting(left,i-1,size);
    	    this.sorting(i+1,right,size);
    	}
    	}


    int doClustering() {
    	int mySizeCopy = mySize;
    	int i=mySizeCopy-1;
    	while (i>=0) {
    		changing.removeAllElements();
    		int currentProcess = myName[i];
    		if (listAdjacent.size()==0) {
        		StringTokenizer st = new StringTokenizer(myAdj[currentProcess].toString());
        		Vector v = new Vector();
        		while (st.hasMoreTokens()) {
    				String temp = st.nextToken().trim();
    				int t = Integer.valueOf(temp).intValue();
    				v.add(t);
    				changing.add(t);
    			}
        		if (v.size()>0) {
        			listCenters.add(currentProcess);
            		listAdjacent.add(v);            		
        		}
    		} else {
    			Vector v1 = new Vector();
    			Vector centToRemove = new Vector();
    			for (int k=0; k<listAdjacent.size(); k++) {
    				Vector v = (Vector) listAdjacent.get(k);
    				Vector sementara = new Vector();
    				Integer it = (Integer) listCenters.get(k);
    				int sizev = v.size();
    				for (int kk=0; kk<sizev; kk++) {
    					Integer ito = (Integer) v.get(kk);
    					if (currentProcess!=ito && actualDistance[it][ito]<actualDistance[currentProcess][ito]) {
    						v1.add(ito);
    						sementara.add(ito);
    						changing.add(ito);
    					}
    				}
    				for (int kk=0; kk<sementara.size(); kk++) {
    					Integer ito = (Integer) sementara.get(kk);
    					v.removeElement(ito);
    				}
    				if (v1.size()>0) {
    					v.removeElement(currentProcess);
    				}
    				if (v.size()==0) {
    					
    					centToRemove.add(it);
    				}
    				listAdjacent.setElementAt(v, k);
    			}
    			for (int j=0; j<centToRemove.size(); j++) {
    				int cent = (Integer) centToRemove.get(j);
    				int idx = listCenters.indexOf(cent);
    				listCenters.remove(idx);
    				listAdjacent.remove(idx);
    				int belongTo = -1;
    				double dist = 0;
    				for (int k=0; k<listCenters.size(); k++) {
    					int cent2 = (Integer) listCenters.get(k);
						double dist2 = actualDistance[cent2][cent];
						if (dist2>dist) {
							dist = dist2;
							belongTo = k;
						}
    				}
    				if (actualDistance[cent][currentProcess]>dist) {
    					if (v1.indexOf(cent)>=0) {} else v1.add(cent);
    				} else {
    					Vector v = (Vector) listAdjacent.get(belongTo);
    					if (v.indexOf(cent)>=0) {} else v.add(cent);
    					listAdjacent.setElementAt(v, belongTo);
    				}
    			}
        		if (v1.size()>0) {
        			listCenters.add(currentProcess);
            		listAdjacent.add(v1);            		
        		}
    		}
    		i--;
    		mySizeCopy = changing.size();
    	}
    	currentCluster = listCenters.size();
   	    clusterInfo=new String[currentCluster];
	    for (int ix=0; ix<currentCluster; ix++) clusterInfo[ix]="";
	    int ci=0;
	    for (int j=0; j<currentCluster; j++) {
	    	Integer it = (Integer) listCenters.get(j);
	    	Vector v = (Vector) listAdjacent.get(j);
    		int cluster = v.size();
    		if (cluster>0) {
    			exactNoCluster++;
    			String members = myNameString[it]+" ";
    			for (int kk=0; kk<cluster; kk++) {
    				Integer ito = (Integer) v.get(kk);
    				members = members + myNameString[ito] +" ";
    			}
    			clusterInfo[ci]=members;
    			
    			ci++;
    		}
    	}
	    System.out.println("num of cluster produced : "+exactNoCluster);
	    return maxi;
    }
    
    public double averageValue = 0;
    void processing() {
    	double totalDistance = 0;
    	double totalEdges = 0;
    	for (int i=0; i<mySize; i++) {
    		for (int j=i+1; j<mySize; j++) {
    			double r = actualDistance[i][j];
    			r = (1f/r) * (1f/r);
    			r = (myDegree[i]*myDegree[j])/r;
    			actualDistance[i][j] = r;
    			actualDistance[j][i] = r;
    			totalEdges = totalEdges + 2;
    			totalDistance = totalDistance + r + r;
    		}
    	}
    	totalDistance = totalDistance/(1F*totalEdges);
    	averageValue = totalDistance;
    }
    
	public static void saveClusters(HashMap<Integer,Integer> tidClusters, String tablename, String thresholdS){
		
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		Boolean log_pairs_to_db = true;
		
		String clusterTable = "sr_" + tablename + "_" + thresholdS; // + "_" + Preprocess.extractMetricName(measure.getClass().getName());
		
		try {
			
			if (log_pairs_to_db){
				String query = "drop table if exists " + config.dbName + "." + clusterTable;
				mysqlDB.executeUpdate(query);
				
				query = "create table " + config.dbName + "." + clusterTable +
				        " (tid int, cid int)";
				mysqlDB.executeUpdate(query);
				
				for (Integer tid:tidClusters.keySet()){
					int cid = tidClusters.get(tid);
					query = "INSERT INTO " + config.dbName + "." + clusterTable +
					" VALUES ( " + tid + "," + cid +  "  ) ";
					//System.out.println(query);
					mysqlDB.executeUpdate(query);
				}
				//t3 = System.currentTimeMillis();
				//System.out.println("Similar Pairs Generation: " + (t3-t2) + "ms");
			}

		} catch (Exception e) {
			System.out.println("Database error"); e.printStackTrace();
		}
	}    
	
	
	public static void main(String[] args) {

		double startThr = 0.1, stopThr = 0.11, step = 0.1;
		boolean show_times = true;

		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		Vector<String> tables = new Vector<String>();
		
		tables.add("time5K");

		
		config = new Config();
		mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		
	
		
		for (String tablename: tables) {
			
			System.out.println("*********************");
			System.out.println("Table: " + tablename);
			System.out.println("*********************");
			
			double thr = startThr;
			
			while (thr <= stopThr) {			
				System.out.println(" ** Threshold:" + thr +  " **");
				long t1, t2;
				
			
				String scoreFile = "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/" + tablename + "scores.txt"; 
				
				String tidCountS = "4883";
				/*
				try {
					String query = "";
					query = "SELECT count(*) FROM "
							+ config.dbName + "." + tablename;
					ResultSet rs = mysqlDB.executeQuery(query);
					rs.next();
					tidCountS = rs.getString(1);

					
				} catch (Exception e) {
					System.err.println("Can't count tids");
					e.printStackTrace();
				}
				*/
				
				String thresholdS = "";
				if (thr>0.09 && thr<0.11) thresholdS = "01";
				else if (thr>0.19 && thr<0.21) thresholdS = "02";
				else if (thr>0.29 && thr<0.31) thresholdS = "03";
				else if (thr>0.39 && thr<0.41) thresholdS = "04";
				else if (thr>0.49 && thr<0.51) thresholdS = "05";
				else if (thr>0.59 && thr<0.61) thresholdS = "06";
				else if (thr>0.69 && thr<0.71) thresholdS = "07";
				else if (thr>0.79 && thr<0.81) thresholdS = "08";
				else if (thr>0.89 && thr<0.91) thresholdS = "09";
				
				String argument = tidCountS + " " + scoreFile + " " + "1 c:/whatever";
				t1 = System.currentTimeMillis();
				run(argument, tablename, (float)thr, thresholdS);
				t2 = System.currentTimeMillis();
				if (show_times) System.out.println("Time for clustering total: " + (t2-t1) + "ms");
				
				thr += step;
				
			}
			
		}
		
		try {
			mysqlDB.close();
		} catch (Exception e) {
			System.err.println("DB Error");
		}
		
		System.gc();
		
	}
		
		
		
	

    
}