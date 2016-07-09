/*******************************************************************************
 * Please cite: 
 * Derry Tanti Wijaya, Stéphane Bressan:
 * Ricochet: A Family of Unconstrained Algorithms for Graph Clustering. DASFAA 2009: 153-167
 * 
 * Copyright (c) Derry Tanti Wijaya
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
package ricochet;

import java.io.*;
import java.sql.ResultSet;
import java.util.*;

import simfunctions.Preprocess;
import simfunctions.WeightedJaccardBM25;
import utility.Config;
import dbdriver.MySqlDB;

class BSR {

	public int myName[];
	public String myNameString[];
	public int myCenter[];
	public int myMark[];
	public double myDegree[];
	public double myDegreeB[];
	
	public StringBuffer myAdj[];
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
		 //String myargument = "199 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/zm3c.txt 15 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/categoryGoogle1c.txt";
		 //String myargument = "4883 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/m3.txt 15 C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/categoryGoogle1c.txt";
		 
		 //float threshold = (float) 0.1;
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
			 BSR zozo = new BSR();
			 int k = Integer.valueOf(args[0]).intValue();
			 zozo.intialize(k);
			 long start = System.currentTimeMillis();				
			 zozo.readFirstMyFile(args[1]);
			 long stop = System.currentTimeMillis();
			 long timeRange = stop-start;
			 start = System.currentTimeMillis();
			 zozo.readSecondMyFileFlo(args[1], Float.valueOf(0).floatValue(), threshold);
			 zozo.sorting(0,k-1,k);
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
			 String cinfo[] = new String[numClusters];
			 HashMap<Integer,Integer> tidCluster = new HashMap<Integer,Integer>();
			 for (int i=0; i<numClusters; i++){				 
				 int countMember = 0;
				 //System.out.println(zozo.clusterInfo[i]);
				 StringTokenizer clusters = new StringTokenizer(zozo.clusterInfo[i]);
			     while (clusters.hasMoreTokens()) {
			     	int tid = Integer.valueOf((clusters.nextToken().trim()));
			     	tidCluster.put(tid, i+1);
			     	countMember++;
			     }
			 }
			 System.out.println(tidCluster);
			 
			 saveClusters(tidCluster, tablename, thresholdS);
			 
		 }
				
	}
	 
	void intialize(int k) {
		q = new Vector();
		averageValue = 0;
		listCenters = new Vector();
		listAdjacent = new Vector();
		changing = new Vector();
		currentCluster = 1;
		
		mySize = k;
		myName = new int[k];
		myNameString = new String[k];
		myCenter = new int[k];
		myMark = new int[k];
		myDegree = new double[k];
		myDegreeB = new double[k];
		
		myAdj = new StringBuffer[k];
		for (int i = 0; i<k; i++) {
			myCenter[i] = 0;
			myMark[i] = 0;
			myDegree[i] = 0;
			myDegreeB[i] = 0;
			
			myAdj[i] = new StringBuffer("");
		}
		myAverage = new double[k];
		actualDistance = new double[k][k];
		for (int i=0; i<k; i++) {
			myAverage[i] = 0;
			for (int j=0; j<k; j++) actualDistance[i][j] = 0;
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
        	
        	for (int i=0; i<mySize; i++) {
    			for (int j=0; j<mySize; j++) {
    				 if (i!=j) {
			        		//StringTokenizer st = new StringTokenizer(record);
			        		//String one = st.nextToken().trim();
			        		//String two = st.nextToken().trim();
			        		//String three = st.nextToken().trim();
			        		float distance = dist[i][j];// Float.valueOf(three).floatValue();
			        		//StringTokenizer st1 = new StringTokenizer(one,":");
			        		//StringTokenizer st2 = new StringTokenizer(two,":");
			        		//String temp1 = st1.nextToken();
			        		//String temp2 = st2.nextToken();
			        		//String temp3 = st1.nextToken();
			        		//String temp4 = st2.nextToken();
			
			        		//int o = Integer.valueOf(temp3).intValue();
			        		//int t = Integer.valueOf(temp4).intValue();
			        		
			        		int o = i+1; //Integer.valueOf(one).intValue();
			        		int t = j+1; // Integer.valueOf(two).intValue();
			        		
			        		
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
			            		myAdj[o] = myAdj[o].append(t +" ");
			    				
			    			}
    				 }
    			}
        	}
        	double totalDegree=0;
        	for (int i=0; i<myDegree.length; i++) {
        		myDegree[i] = myAverage[i]/(1F*myDegree[i]);
        		myAverage[i] = myDegree[i];
        		totalDegree = totalDegree+myDegree[i];
        		
        	}
        	for (int i=0; i<myDegree.length; i++) {
        		myDegree[i] = myDegree[i]/(1F*totalDegree);
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

    public Vector q = new Vector();
	
    int doClustering() {
    	
    	int i = mySize-1;
    	int currentProcess = myName[i];
    	q.add(currentProcess);
    	while (q.size()>0) {
    		currentProcess = (Integer) q.get(0);
    		process(currentProcess);
    		q.remove(0);
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

    void process(int cp) {
    	if (listAdjacent.size()==0) {
    		StringTokenizer st = new StringTokenizer(myAdj[cp].toString());
    		Vector v = new Vector();
    		while (st.hasMoreTokens()) {
				String temp = st.nextToken().trim();
				int t = Integer.valueOf(temp).intValue();
				if (v.indexOf(t)>=0) {}
				else v.add(t);
			}
    		if (v.size()>0) {
    			v.add(cp);
				int member[] = new int[v.size()];
        		double dists[] = new double[v.size()];
        		for (int k=0; k<v.size(); k++) {
        			int m = (Integer) v.get(k);
        			member[k] = m;
        		}        		
        		double totaldist1=0;
        		int newcent1 = cp;
    			for (int k=0; k<v.size(); k++) {
        			dists[k] = 0;
        			int examined = member[k];
        			for (int kk=0; kk<v.size(); kk++) {
        				if (k!=kk) {
        					dists[k] = dists[k] + actualDistance[examined][member[kk]];
        				}
        			}
        			if (dists[k]>totaldist1) {
        				totaldist1 = dists[k];
        				newcent1 = examined;
        			}    		
            	}
    			cp = newcent1;
    			v.removeElement(cp);
    			listCenters.add(cp);
        		listAdjacent.add(v);  
        		q.add(1);
    		}
    	} else {
    		Vector potential = new Vector();
    		for (int i=0; i<mySize; i++) {
    			if (listCenters.indexOf(i)>=0) {} else {
    				potential.add(i);
    			}
    		}
    		double least = -1;
    		int next = -1;
    		for (int i=0; i<potential.size(); i++) {
    			int one = (Integer) potential.get(i);
    			double score = 0;
    			for (int j=0; j<listCenters.size(); j++) {
    				int two = (Integer) listCenters.get(j);
    				
    				score = score + actualDistance[one][two];
    			}
    			score = score /(1F * myDegreeB[one]);
    			
    			if (least<0) {
    				least = score;
    				next = one;
    			} else {
    				if (score<least) {
    					least = score;
    					next = one;
    				}
    			}
    		}
    		
    		if (next>=0) {
    			Vector adjNext = new Vector();
    			Vector centToRemove = new Vector();
    			for (int i=0; i<listAdjacent.size(); i++) {
    				int cent = (Integer) listCenters.get(i);
    				Vector v = (Vector) listAdjacent.get(i);
    				Vector temp = new Vector();
    				for (int j=0; j<v.size(); j++) {
    					int adj = (Integer) v.get(j);
    					if (next!=adj && actualDistance[next][adj]>actualDistance[cent][adj]) {
    						if (adjNext.indexOf(adj)>=0) {} else adjNext.add(adj);
    						temp.add(adj);
    					}
    				}
    				for (int j=0; j<temp.size(); j++) {
    					int adj = (Integer) temp.get(j);
    					v.removeElement(adj);
    				}
    				
    				if (adjNext.size()>0) {
    					v.removeElement(next);
    				}
    				
    				if (v.size()==0) {
    					
    					centToRemove.add(cent);
    				}
    				listAdjacent.setElementAt(v, i);
    			}
    			for (int i=0; i<centToRemove.size(); i++) {
    				int cent = (Integer) centToRemove.get(i);
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
    				if (actualDistance[cent][next]>dist) {
    					if (adjNext.indexOf(cent)>=0) {} else adjNext.add(cent);
    				} else {
    					Vector v = (Vector) listAdjacent.get(belongTo);
    					if (v.indexOf(cent)>=0) {} else v.add(cent);
    					listAdjacent.setElementAt(v, belongTo);
    				}
    			}
    			if (adjNext.size()>0) {
    				q.add(1);
    				adjNext.add(next);
    				int member[] = new int[adjNext.size()];
            		double dists[] = new double[adjNext.size()];
            		for (int k=0; k<adjNext.size(); k++) {
            			int m = (Integer) adjNext.get(k);
            			member[k] = m;
            		}        		
            		double totaldist1=0;
            		int newcent1 = next;
        			for (int k=0; k<adjNext.size(); k++) {
            			dists[k] = 0;
            			int examined = member[k];
            			for (int kk=0; kk<adjNext.size(); kk++) {
            				if (k!=kk) {
            					dists[k] = dists[k] + actualDistance[examined][member[kk]];
            				}
            			}
            			if (dists[k]>totaldist1) {
            				totaldist1 = dists[k];
            				newcent1 = examined;
            			}    		
                	}
        			next = newcent1;
        			adjNext.removeElement(next);
    				listCenters.add(next);
    				listAdjacent.add(adjNext);
    			}
    		}
    	}
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
		
		String clusterTable = "bsr_" + tablename + "_" + thresholdS; // + "_" + Preprocess.extractMetricName(measure.getClass().getName());
		
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

		String algorithm = "bsr";
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
			
			while (thr <= stopThr) {			
				System.out.println(" ** Threshold:" + thr +  " **");
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