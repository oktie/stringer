package simfunctions;

import java.util.HashMap;

public class ModBM25 extends BM25 {

	// Tokenizer based on whitespace; every token is a word
	public HashMap<String, Double> getTF(String str) {
		return gettokenTFSingle(str);
	}

}
