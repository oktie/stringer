package simfunctions;

import java.util.HashMap;


public class DistinctHMM extends HMM {

	public HashMap<String, Double> getTF(String str) {
		return getConditionalTF(str);
	}

}
