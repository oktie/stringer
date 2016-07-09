package simfunctions;

import java.util.HashMap;

public class Idf extends TfIdf {

	// Tokenizer based on whitespace; evry token is a word
	public HashMap<String, Double> getTF(String str) {
		return gettokenTFSingle(str);
	}

}
