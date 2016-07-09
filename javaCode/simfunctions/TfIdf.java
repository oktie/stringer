package simfunctions;

import java.util.HashMap;

public class TfIdf extends Preprocess {

	// Tokenizer based on whitespace; evry token is a word
	public HashMap<String, Double> getTF(String str) {
		return getTokenTFMultiple(str);
	}

}
