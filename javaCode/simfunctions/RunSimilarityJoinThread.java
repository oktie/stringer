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