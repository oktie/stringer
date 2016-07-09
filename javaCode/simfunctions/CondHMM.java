package simfunctions;

import java.util.HashMap;
import java.util.Vector;


public class CondHMM extends HMM {

	public HashMap<String, Double> getTF(String str) {
		return getConditionalTF(str);
	}

	public void convertDFtoIDF(HashMap<String, Double> qgramIDF, int size, Vector<HashMap<String, Double>> recordTokenWeights) {
		//We will convert the DF to conditional IDF
		/*   t_0 = size
		 *   idf( t_n ) =  log( 1 + df(t_{n-1}) / df(t_n))
		 */
		double numTokens = 0;
		for (String qgram : qgramIDF.keySet()) {
			numTokens += qgramIDF.get(qgram);
		}
		HashMap<String, Double> qgramFrequency = new HashMap<String, Double>();
		qgramFrequency.putAll(qgramIDF);
		
		for (String qgram : qgramIDF.keySet()) {
			if(qgram.endsWith("_1")){
				qgramIDF.put(qgram, qgramFrequency.get(qgram)/numTokens);
			}else{
				int j=2;
				while(! qgram.endsWith("_"+j)){
					j++;
				}
				String[] tokens = qgram.split("_");
				String baseToken = tokens[0]+"_"+(j-1);
				double countTokenWithBaseToken = 0;
				for(int k=0; k < recordTokenWeights.size(); k++){
					HashMap<String, Double> tokenTF = recordTokenWeights.get(k);
					if(tokenTF.containsKey(baseToken)){
						countTokenWithBaseToken += getTotalTokens(tokenTF);
					}
				}
				qgramIDF.put(qgram, qgramFrequency.get(qgram) / countTokenWithBaseToken);
			}
		}
	}
	
	
	
	
}
