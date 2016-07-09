package simfunctions;

import experiment.SimilarityJoin;

public class RunSimilarityJoinThread extends Thread{

	private String querytablename = "queryTable";
	private String tablename = "baseTable";
	private Preprocess measure;
	private double thr;
	
	public RunSimilarityJoinThread(String querytable, String inputtablename, Preprocess inputmeasure, double inputthr){
		querytablename = querytable;
		tablename = inputtablename;
		measure = inputmeasure;
		thr = inputthr;
	}

	public void run() {

		long t1, t2;
		t1 = System.currentTimeMillis();
		/*
		 * 
		 * Call Similarity Join
		 * 
		 */
		
		t1 = System.currentTimeMillis();
		SimilarityJoin simjoin = new SimilarityJoin();
		simjoin.run(querytablename, tablename, measure, thr);
		t2 = System.currentTimeMillis();
		System.err.println("Similarity Join DONE for: " + tablename +  "," + Preprocess.extractMetricName(measure.getClass().getName()) + " took: " + (t2-t1) + "ms");
		
	}

}