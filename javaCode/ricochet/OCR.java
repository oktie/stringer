package ricochet;

import java.io.*;
import java.sql.ResultSet;
import java.util.*;

import simfunctions.Preprocess;
import simfunctions.WeightedJaccardBM25;
import utility.Config;
import dbdriver.MySqlDB;

class OCR {

	public int myName[];
	public String myNameString[];
	public int myCenter[];
	public int myMark[];
	public double myDegree[];
	public double myDegreeB[];

	public int myAdjDist[][];
	public Vector myAdj[];
	public int mySize;
	public int currentCluster = 1;
	public int maxi = 0;

	public float minValue = 0;
	public float maxValue = 0;
	public float aveValue = 0;
	public float ranValue = 0;

	
	public Vector listCenters = new Vector();
	public Vector listAdjacent = new Vector();
	public Vector changing = new Vector();
	
	 public String nameCat[], clusterInfo[];
	 public Vector memberCat[];
	 public int countMemberCat[];
	 public float precisionInfo[], recallInfo[], FInfo[];
	 public int exactNoCluster = 0;

	 public String naming[] = new String[13100];
	 public double actualDistance[][];
	 public double myAverage[];
	 
	 public static void run (String myargument, String tablename, float threshold, String thresholdS) {
		 
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
		 
		 //String myargument = "1019 google1.txt 15 categoryGoogle1.txt";
		 //String myargument = "1019 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/google1c.txt 15 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/categoryGoogle1c.txt";
		 //String myargument = "4883 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/m3.txt 15 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/categoryGoogle1c.txt";
		 //String myargument = "1936 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/zm3.txt 15 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/categoryGoogle1c.txt";
		 //String myargument = "199 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/zm3c.txt 15 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/categoryGoogle1c.txt";
		 //String myargument = "4 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/test.txt 15 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/categoryGoogle1c.txt";
		 //float threshold = (float)0.1;
		 String args[];
		 
		 if (myargument.length()>0) {
			 StringTokenizer starg = new StringTokenizer(myargument);
			 args = new String[starg.countTokens()];
			 StringTokenizer zt = new StringTokenizer(myargument);
			 int kz=0;
			 while (zt.hasMoreTokens()) {
				 args[kz] = zt.nextToken().trim();
				 kz++;
			 }
			 OCR zozo = new OCR();
			 int k = Integer.valueOf(args[0]).intValue();
			 zozo.intialize(k);
			 long start = System.currentTimeMillis();				
			 zozo.readFirstMyFile(args[1]);
			 long stop = System.currentTimeMillis();
			 long timeRange = stop-start;
			 start = System.currentTimeMillis();
			 zozo.readSecondMyFileFlo(args[1], Float.valueOf(0), threshold);
			 zozo.sorting(0,k-1,k);
			 zozo.sorting3(k);
			 zozo.doClustering();
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
			 //System.out.println(zozo.clusterInfo.length);
			 String cinfo[] = new String[numClusters];
			 //HashMap<Integer,Integer> tidCluster = new HashMap<Integer,Integer>();
			 HashMap<Integer,BitSet> clusterNums = new HashMap<Integer,BitSet>();
			 
			 int countMember = 0;
			 for (int i=0; i<numClusters; i++){				 
				 //System.out.println(zozo.clusterInfo[i]);
				 if (zozo.clusterInfo[i]!=null) 
				 {
					 StringTokenizer clusters = new StringTokenizer(zozo.clusterInfo[i]);
				     while (clusters.hasMoreTokens()) {
					     	int tid = Integer.valueOf((clusters.nextToken().trim()));
					     	if (clusterNums.containsKey(tid))
					     	{
					     		BitSet b = clusterNums.get(tid);
								b.set(i+1);
								clusterNums.put(tid, b);
					     	}
					     	else {
					     		BitSet b = new BitSet();
								b.set(i+1);
					     		clusterNums.put(tid, b);
					     	}
					     	countMember++;
				     }
				 }
				 else System.err.println("null cluster!");
			 }
			 System.out.println(countMember++);
			 
			 saveClusters(clusterNums, tablename, thresholdS);
			 
			 
		 }
	}
	 
	void intialize(int k) {
		mySize = k;
		myName = new int[k];
		myNameString = new String[k];
		myCenter = new int[k];
		myMark = new int[k];
		myDegree = new double[k];
		myDegreeB = new double[k];
		
		myAdjDist = new int[k][k];
		myAdj = new Vector[k];
		for (int i = 0; i<k; i++) {
			myCenter[i] = 0;
			myMark[i] = 0;
			myDegree[i] = 0;
			myDegreeB[i] = 0;
			myAdj[i] = new Vector();
		}
		myAverage = new double[k];
		actualDistance = new double[k][k];
		for (int i=0; i<k; i++) {
			myAverage[i] = 0;
			for (int j=0; j<k; j++) {
				actualDistance[i][j] = 0.1;
			}
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


	   void readFirstMyFile(String myString) {
			String filename=myString;
	        DataInputStream dis = null;
	        String record = null;
	        try {
	        	File f = new File(filename);
	        	FileInputStream fis = new FileInputStream(f);
	        	BufferedInputStream bis = new BufferedInputStream(fis);
	        	dis = new DataInputStream(bis);
	        	int i=0;
	        	float totValue = 0;
	        	while ( (record=dis.readLine()) != null ) {
	        		StringTokenizer st = new StringTokenizer(record);
	        		String one = st.nextToken().trim();
	        		one = st.nextToken().trim();
	        		String three = st.nextToken().trim();
	        		float distance = Float.valueOf(three).floatValue();
	        		if (distance<minValue) minValue = distance;
	        		if (distance>maxValue) maxValue = distance;
	        		totValue = totValue + distance;
	        		i++;
	        	}
	        	aveValue = totValue/(1F*i);
	        	ranValue = aveValue - minValue;

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
	    }
	      
    void readSecondMyFileFlo(String myString, float flo, float threshold) {
		String filename=myString;
        DataInputStream dis = null;
        String record = null;
        float cutValue;
        cutValue = (ranValue * flo) + minValue;
        if (cutValue>maxValue) {
        	cutValue = maxValue+1;
        	maxi = 1;
        }
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
    				dist[i][j] = (float)0.0;
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
			        			naming[index]=temp1;
			        			o = index;
			        			index++;
			        		}
			        		if (indexNameFlo[t]>=0) t = indexNameFlo[t];
			        		else {
			        			indexNameFlo[t]=index;
			        			naming[index]=temp2;
			        			t = index;
			        			index++;
			        		}
			    			myName[o] = o;
			    			myNameString[o] = temp1;
			    			
			    			if (distance>=cutValue) {
			    				actualDistance[o][t] = distance;
			        			actualDistance[t][o] = distance;
			        			myAverage[o] = myAverage[o] + distance;
			            		myDegree[o]++;
			        			myAdjDist[o][t] = t;
			        			myAdjDist[t][o] = o;
			    			}
    				 }
    			}
    			
        	}
        	for (int i=0; i<myDegree.length; i++) {
        		if (myDegree[i]>0) myDegree[i] = myAverage[i]/myDegree[i];
        		myDegreeB[i] = myDegree[i];
        	}

        	indexNameFlo = null;
        	Runtime.getRuntime().gc();
        	
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
    }
    
    
    void sorting3(int size) {
    	for (int i=0; i<size; i++) {
    		sorting2(i,0,size-1,size);
    	}
    }
    
    void sorting2(int index, int left, int right, int size) {
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
    	    v = actualDistance[index][right] ;
    	    i = left - 1 ;
    	    j = right ;
    	    cont01 = true ;
    	    while (cont01){
    		cont02 = true ;
    		while (cont02 && (i+1<size)){
    		    i = i + 1 ;
    		    aux03 = actualDistance[index][i] ;
    		    if (!(aux03<v)) cont02 = false ;
    		    else cont02 = true ;
    		}
    		cont02 = true ;
    		while (cont02 && (j>0)){
    		    j = j - 1 ;
    		    aux03 = actualDistance[index][j] ;
    		    if (!(v < aux03)) cont02 = false ;
    		    else cont02 = true ;
    		}


    		t = actualDistance[index][i] ;
    		temp = myAdjDist[index][i];
    		actualDistance[index][i] = actualDistance[index][j] ;
    		myAdjDist[index][i] = myAdjDist[index][j];
    		actualDistance[index][j] = t ;
    		myAdjDist[index][j] = temp;
    		if ( j < (i+1)) cont01 = false ;
    		else cont01 = true ;
    	    }
    	    actualDistance[index][j] = actualDistance[index][i] ;
    	    myAdjDist[index][j] = myAdjDist[index][i];
    	    actualDistance[index][i] = actualDistance[index][right] ;
    	    myAdjDist[index][i] = myAdjDist[index][right];
    	    actualDistance[index][right] = t ;
    	    myAdjDist[index][right] = temp;
     	    this.sorting2(index,left,i-1,size);
    	    this.sorting2(index,i+1,right,size);
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

    public double myAB[];
    public int myA[];
    public int myB[];
    
    void sortingSmall(int left, int right, int size){
    	double v ;
    	int i ;
    	int j ;
    	double t ;
    	boolean cont01;
    	boolean cont02;
    	double aux03 ;
    	t = 0 ;
    	int temp=0;
    	int temp2=0;

    	if (left < right){
    	    v = myAB[right] ;
    	    i = left - 1 ;
    	    j = right ;
    	    cont01 = true ;
    	    while (cont01){
    		cont02 = true ;
    		while (cont02 && (i+1<size)){
    		    i = i + 1 ;
    		    aux03 = myAB[i] ;
    		    if (!(aux03<v)) cont02 = false ;
    		    else cont02 = true ;
    		}
    		cont02 = true ;
    		while (cont02 && (j>0)){
    		    j = j - 1 ;
    		    aux03 = myAB[j] ;
    		    if (!(v < aux03)) cont02 = false ;
    		    else cont02 = true ;
    		}


    		t = myAB[i] ;
    		temp = myA[i];
    		temp2 = myB[i];
    		myAB[i] = myAB[j] ;
    		myA[i] = myA[j];
    		myB[i] = myB[j];
    		myAB[j] = t ;
    		myA[j] = temp;
    		myB[j] = temp2;

    		if ( j < (i+1)) cont01 = false ;
    		else cont01 = true ;
    	    }
    	    myAB[j] = myAB[i] ;
    	    myA[j] = myA[i];
    	    myB[j] = myB[i];
    	    myAB[i] = myAB[right] ;
    	    myA[i] = myA[right];
    	    myB[i] = myB[right];
    	    myAB[right] = t ;
    	    myA[right] = temp;
    	    myB[right] = temp2;
     	    this.sortingSmall(left,i-1,size);
    	    this.sortingSmall(i+1,right,size);
    	}
    	}

    int doClustering() {
    	int change = 1;
    	int index = mySize - 1;
    	int iter=0;
    	while (change>0 && index>=0) {
    		iter++;
    		int numCenter = 0;
    		for (int k=0; k<mySize; k++) {
    			if (myMark[k]==0) numCenter++; 
    		}
    		myAB = new double[numCenter];
    		myA = new int[numCenter];
    		myB = new int[numCenter];
    		int i=mySize - 1;
    		int co=0;
    		while (i>=0) {
    			int currentProcess = myName[i];
        		if (myMark[currentProcess]==0) {
        			int it = myAdjDist[currentProcess][index];
        			myAB[co] = actualDistance[currentProcess][index];
        			myA[co] = currentProcess;
        			myB[co] = it;
        			co++;
        		}
        		i--;
    		}
    		sortingSmall(0,numCenter-1,numCenter);
    		i=numCenter - 1;
    		changing.removeAllElements();
        	while (i>=0 && myAB[i]>0) {
        		int currentProcess = myA[i];
        		if (myMark[currentProcess]==0) {
        			int currentMark = 1;
        			int it = myB[i];
        				if (myCenter[it]>0) {
            				if (myDegreeB[it]>=myDegreeB[currentProcess]) {
            					currentMark = 0;
                				if (myAdj[it].indexOf(currentProcess)>=0) {
                				} else {
                					myMark[currentProcess]++;
                					myAdj[it].add(currentProcess);
                				}
                				if (myCenter[currentProcess]==1) {
                					for (int kk=0; kk<myAdj[currentProcess].size(); kk++) {
                    					int it1 = (Integer) myAdj[currentProcess].get(kk);
                    					if (myAdj[it].indexOf(it1)>=0) {myMark[it1]--;} else {
                    						myAdj[it].add(it1);
                    					}
                    				}
                					
                				}
            				} else {
            					myCenter[it] = 0;
            					changing.add(1);
            					if (myAdj[currentProcess].indexOf(it)>=0) {
                				} else {
                					myMark[it]++;
                					myAdj[currentProcess].add(it);
                				}
                				for (int kk=0; kk<myAdj[it].size(); kk++) {
                					int it1 = (Integer) myAdj[it].get(kk);
                					if (myAdj[currentProcess].indexOf(it1)>=0) {myMark[it1]--;} else {
                						myAdj[currentProcess].add(it1);
                					}
                				}
                				myAdj[it].removeAllElements();
            				}
            			}
            			if (currentMark!=1) {
            				if (myCenter[currentProcess]==1) changing.add(1);
            				myCenter[currentProcess] = 0;
            				myAdj[currentProcess].removeAllElements();
            			} else {
            				if (myCenter[currentProcess]==0) {
            					changing.add(1);            					
            				}
            				myCenter[currentProcess] = 1;
        					int ito = myAdjDist[currentProcess][index];
        					if (myAdj[currentProcess].indexOf(ito)>=0) {
            				} else {
            					myMark[ito]++;
            					myAdj[currentProcess].add(ito);
            				}
            			}
	    		} 
        		i--;
        	}
        	index--;
        	change = changing.size();
    	}

    	
   	    clusterInfo=new String[mySize];
	    for (int ix=0; ix<currentCluster; ix++) clusterInfo[ix]="";
	    int ci=0;
	    for (int j=0; j<mySize; j++) {
    		int cluster = myCenter[j];
    		if (cluster>0 && myAdj[j].size()>0) {
    			exactNoCluster++;
    			String members = myNameString[j]+" ";
    			for (int k=0; k<myAdj[j].size(); k++) {
    				int t = (Integer) myAdj[j].get(k);
    				members = members + myNameString[t] +" ";
    			}
    			clusterInfo[ci]=members;
    			ci++;
    		}
    	}
	    System.out.print("num of cluster produced : "+exactNoCluster+"\n");
	    return maxi;
    }
    
    public double actualDistanceB[][];
    void processing() {
    	actualDistanceB = new double[mySize][mySize];
    	double totalDistance = 0;
    	double totalEdges = 0;
    	for (int i=0; i<mySize; i++) {
    		for (int j=i+1; j<mySize; j++) {
    			double r = actualDistance[i][j];
    			r = (1f/r) * (1f/r);
    			r = (myDegree[i]*myDegree[j])/r;
    			actualDistanceB[i][j] = r;
    			actualDistanceB[j][i] = r;
    			totalEdges = totalEdges + 2;
    			totalDistance = totalDistance + r + r;
    		}
    	}
    	totalDistance = totalDistance/(1F*totalEdges);
    }
    
	public static void saveClusters(HashMap<Integer,BitSet> clusterNums, String tablename, String thresholdS){
		
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		Boolean log_pairs_to_db = true;
		
		String clusterTable = "ocr_" + tablename + "_" + thresholdS; // + "_" + Preprocess.extractMetricName(measure.getClass().getName());
		
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

	public static void main(String[] args) {

		String algorithm = "ocr";
		double startThr = 0.1, stopThr = 0.91, step = 0.1;
		boolean show_times = false;

		String datasetsTable = "datasets";
		
		Config config = new Config();
		MySqlDB mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		Vector<String> tables = new Vector<String>();
		HashMap<String, String> tableclass = new HashMap<String, String>();
		
		try {
			String query = "";
			query = "SELECT * FROM "
					+ datasetsTable + " T ";
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
		
		
		//tables.add("h1");

		
		config = new Config();
		mysqlDB = new MySqlDB(config.returnURL(), config.user, config.passwd);
		
	
		
		for (String tablename: tables) {
			
			System.out.println("*********************");
			System.out.println("Table: " + tablename);
			System.out.println("*********************");
			
			double thr = startThr;
			System.out.println(" ** Threshold:" + thr +  " **");
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
				
				String scoreFile = "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/" + tablename + "scores.txt"; 
				
				String tidCountS = "0";
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
				run(argument, tablename, (float)thr, thresholdS);
				
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