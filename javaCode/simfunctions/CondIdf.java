package simfunctions;

import java.util.HashMap;

public class CondIdf extends TfIdf {

	// Tokenizer based on conditional occurance; evry token is a word
	public HashMap<String, Double> getTF(String str) {
		return getConditionalTF(str);
	}

	public void convertDFtoIDF(HashMap<String, Double> qgramIDF, int size) {
		//We will convert the DF to conditional IDF
		/*   t_0 = size
		 *   idf( t_n ) =  log( 1 + df(t_{n-1}) / df(t_n))
		 */
		HashMap<String, Double> qgramFrequency = new HashMap<String, Double>();
		qgramFrequency.putAll(qgramIDF);
		
		for (String qgram : qgramIDF.keySet()) {
			if(qgram.endsWith("_1")){
				qgramIDF.put(qgram, Math.log(1 + size / qgramFrequency.get(qgram)));
			}else{
				int j=2;
				while(! qgram.endsWith("_"+j)){
					j++;
				}
				String[] tokens = qgram.split("_");
				qgramIDF.put(qgram, Math.log(1 + qgramFrequency.get(tokens[0]+"_"+(j-1))/ qgramFrequency.get(qgram)));
			}
		}
	}
}
