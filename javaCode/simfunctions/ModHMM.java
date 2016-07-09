package simfunctions;

import java.util.HashMap;

public class ModHMM extends HMM {

	// Tokenizer based on whitespace; every token is a word
	public HashMap<String, Double> getTF(String str) {
		return gettokenTFSingle(str);
	}

}
