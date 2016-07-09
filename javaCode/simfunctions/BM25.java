package simfunctions;

import java.util.HashMap;

public class BM25 extends Preprocess {

	static double k1 = 1.5, b = 0.675, k3 = 8;

	// Tokenizer based on whitespace; evry token is a word
	public HashMap<String, Double> getTF(String str) {
	//	System.out.println("BM25-getTF::  ["+str+"] "+str.length());
		HashMap<String, Double> tokenTF = getTokenTFMultiple(str);
	//	System.out.println("BM25-getTF:: "+tokenTF+"\n"+ tokenTF.size() + " sum_tf:"+ getTotalTokens(tokenTF));
		return tokenTF;
	}

	public void convertDFtoIDF(HashMap<String, Double> qgramIDF, int size) {
		for (String qgram : qgramIDF.keySet()) {
			qgramIDF.put(qgram, Math.log((size - qgramIDF.get(qgram) + 0.5) / (qgramIDF.get(qgram) + 0.5)));
		}
	}

	public HashMap<String, Double> getWeights(HashMap<String, Double> tokenTF, HashMap<String, Double> qgramIDF,
			double averageLength) {
		double bm25Weight, recordLength = 0, normFactor;
		recordLength = getTotalTokens(tokenTF);
		normFactor = k1 * ((1 - b) + b * recordLength / averageLength);
		for (String qgram : tokenTF.keySet()) {
			if (qgramIDF.containsKey(qgram)) {
				bm25Weight = tokenTF.get(qgram);
				bm25Weight = (1 + k1) * bm25Weight / (normFactor + bm25Weight);
				tokenTF.put(qgram, bm25Weight * qgramIDF.get(qgram));
			}
		}
		return tokenTF;
	}

	public HashMap<String, Double> getQueryWeights(String str, HashMap<String, Double> qgramIDF) {
		HashMap<String, Double> tokenTF = getTF(str);
		double bm25Weight;
		for (String qgram : tokenTF.keySet()) {
			bm25Weight = tokenTF.get(qgram);
			bm25Weight = (1 + k3) * bm25Weight / (k3 + bm25Weight);
			tokenTF.put(qgram, bm25Weight);
		}
		return tokenTF;
	}
}
