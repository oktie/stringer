package simfunctions;

import java.util.HashMap;

public class DistinctTfIdf extends TfIdf {

	// Tokenizer based on conditional occurance; evry token is a word
	public HashMap<String, Double> getTF(String str) {
		return getConditionalTF(str);
	}

}
