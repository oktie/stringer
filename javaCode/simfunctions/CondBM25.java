package simfunctions;

import java.util.HashMap;

public class CondBM25 extends BM25 {

	//static double k1 = 1.5, b = 0.675, k3 = 8;

	// Tokenizer based on whitespace; evry token is a word
	public HashMap<String, Double> getTF(String str) {
		return getConditionalTF(str);
	}

	public void convertDFtoIDF(HashMap<String, Double> qgramIDF, int size) {
		//We will convert the DF to conditional IDF
		/*   t_0 = size
		 *   idf( t_n ) =  log(  [ df(t_{n-1}) + df(t_n) + 0.5 ]/ [df(t_n) + 0.5 ])
		 */
		HashMap<String, Double> qgramFrequency = new HashMap<String, Double>();
		qgramFrequency.putAll(qgramIDF);
		
		for (String qgram : qgramIDF.keySet()) {
			if(qgram.endsWith("_1")){
				qgramIDF.put(qgram, Math.log((size - qgramFrequency.get(qgram) + 0.5) / (qgramFrequency.get(qgram) + 0.5)));
			}else{
				int j=2;
				while(! qgram.endsWith("_"+j)){
					j++;
				}
				String[] tokens = qgram.split("_");
				qgramIDF.put(qgram, Math.log(( qgramFrequency.get(tokens[0]+"_"+(j-1)) - qgramFrequency.get(qgram) + 0.5) / (qgramFrequency.get(qgram) + 0.5)));
			}
		}
		qgramFrequency.clear();
	}

}
