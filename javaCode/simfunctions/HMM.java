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

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import evaluation.AccuracyMeasure;
import experiment.IdScore;
import experiment.SimilarityMatch;


public class HMM extends Preprocess {

	private static double a0 = 0.8;

	private static double a1 = 0.2;

	public HashMap<String, Double> getTF(String str) {
		return getTokenTFMultiple(str);
	}

	public void incrementDFCount(String qgram, HashMap<String, Double> tokenCount, double qgramTF) {
		if (tokenCount.containsKey(qgram)) {
			tokenCount.put(qgram, tokenCount.get(qgram) + qgramTF);
		} else {
			tokenCount.put(qgram, qgramTF);
		}
	}

	public void convertDFtoIDF(HashMap<String, Double> qgramIDF, int size) {
		double numTokens = 0;
		for (String qgram : qgramIDF.keySet()) {
			numTokens += qgramIDF.get(qgram);
		}
		for (String qgram : qgramIDF.keySet()) {
			qgramIDF.put(qgram, qgramIDF.get(qgram) / numTokens);
		}
	}

	public HashMap<String, Double> getWeights(HashMap<String, Double> tokenTF, HashMap<String, Double> qgramIDF) {
		double sum = 0;
		double weight=0;
		for (String qgram : tokenTF.keySet()) {
			sum += tokenTF.get(qgram);
		}
		for (String qgram : tokenTF.keySet()) {
			weight = ( a1 * tokenTF.get(qgram) / sum )/( a0 * qgramIDF.get(qgram));
			tokenTF.put(qgram, Math.log( 1 +  weight ) );
		}
		return tokenTF;
	}

	public HashMap<String, Double> getQueryWeights(String str, HashMap<String, Double> qgramIDF) {
		HashMap<String, Double> tokenTF = getTF(str);
		for (String qgram : tokenTF.keySet()) {
			tokenTF.put(qgram, 1.0);
		}
		return tokenTF;
	}

	public static void main(String[] args) {
		double[] mapArray;
		int maxQueries = 100;
		HMM hmm = new HMM();
		long t1, t4, t2, t3, prepTime = 0, queryTime = 0;
		Vector<String> files = new Vector<String>();
		Vector<HashMap<String, Double>> baseTableTokenWeights = new Vector<HashMap<String, Double>>();
		HashMap<String, Double> qgramIDF = new HashMap<String, Double>();

		t1 = System.currentTimeMillis();
		// Toggle this variable to log the preprocessing info to DB
		Preprocess.setLogToDB(false);
		hmm.preprocessTable(files, qgramIDF, baseTableTokenWeights, args[0]);
		t2 = System.currentTimeMillis();
		prepTime = t2 - t1;
		System.err.println("Preprocessing Done: took " + prepTime + " ms");

		// System.gc();
		// For each query, order the filenames by their jaccard score
		int i = 1;
		String query;
		t1 = System.currentTimeMillis();
		HashMap<Integer, String> queries = SimilarityMatch.testQueries();
		t2 = System.currentTimeMillis();
		System.err.println("Collected Random Queries: took " + (t2 - t1) + " ms");

		
		//If due to some reason we cant get maxQueries qeuries then readjust this number
		maxQueries = queries.size();

		mapArray = new double[maxQueries];
		for (int a = 0; a < maxQueries; a++) {
			mapArray[a] = 0;
		}

		Vector<String> queryVector = new Vector<String>();
		Vector<Integer> queryIdVector = new Vector<Integer>();
		String baseDir = "/home/oktie/dcp-workspace/log/res/" + args[0] + "/";
		File baseDirPath = new File(baseDir);
		baseDirPath.mkdirs();

		for (Integer queryId : queries.keySet()) {
			query = queries.get(queryId);
			HashSet<Integer> actualResult = SimilarityMatch.getAllTidsHavingId(queryId, args[1]);
			queryVector.add(query);
			queryIdVector.add(queryId);

			t3 = System.currentTimeMillis();
			// The tids in the scoreList is 1 less than the actual tid e.g.
			// tid=1 willl be having id=0 in IdScore object
			List<IdScore> scoreList = hmm.getSimilarRecords(query, qgramIDF, baseTableTokenWeights);
			SimilarityMatch.logOutput(baseDir + i + "." + hmm.getClass().getName() + ".txt", query, files, scoreList);
			t4 = System.currentTimeMillis();
			queryTime += (t4 - t3);
			// Compute Accuracy
			int[] booleanList = SimilarityMatch.generateBooleanList(actualResult, scoreList);
			double map = AccuracyMeasure.meanAveragePrecision(booleanList, actualResult.size());
			mapArray[i - 1] = map;
			// System.err.println("Query Time: " + (t4-t1));
			System.err.println("query: " + i + "  metric: " + hmm.getClass().getName() + "      MAP: " + map);

			System.err.println("DONE QUERY: " + i);
			i++;
		}

		System.err.println("\nQuery Time: " + queryTime + " ms     Preprocess Time : " + prepTime + " ms ");
	}

}
