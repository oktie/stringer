package simfunctions;

import java.util.HashMap;

public class DistinctBM25 extends BM25 {

	//static double k1 = 1.5, b = 0.675, k3 = 8;

	// Tokenizer based on whitespace; evry token is a word
	public HashMap<String, Double> getTF(String str) {
		return getConditionalTF(str);
	}

}
