package simfunctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.wcohen.ss.JaroWinkler;

import experiment.IdScore;

public class SoftTfIdf extends TfIdf {

	public static boolean localDebugMode = true;
	public static double jwSimilarityThreshold = 0.8;
	
	public HashMap<String, Double> getTF(String str) {
		tokenizeUsingQgrams = false;
		return getTokenTFMultiple(str);
	}
	
	public void printDebug(String str){
		//if(localDebugMode)
		//	System.out.print(str);
	}
	
	// Returns the maximum similar token based on Jaro-Wrinkler score
	public String getMaximumSimilarToken(String token, Set<String> tupleTokens, JaroWinkler jw) {
		double currentScore = 0, maxScore = 0;
		boolean isFirstToken = true;
		String maxSimilarToken = "";
		printDebug(token+"\n");
		for (String tok : tupleTokens) {
			if(isFirstToken){
				maxSimilarToken = tok;
				isFirstToken = false;
			}
			currentScore = jw.score(jw.prepare(token), jw.prepare(tok));
			printDebug(tok+" "+currentScore+" :: ");
			if (currentScore > maxScore) {
				maxScore = currentScore;
				maxSimilarToken = tok;
			}
		}
		printDebug("\nMaxSimilar  "+maxSimilarToken+" "+maxScore+" :: \n");
		return maxSimilarToken;
	}

	public List<IdScore> getSimilarRecords(String query, HashMap<String, Double> qgramIDF,
			Vector<HashMap<String, Double>> recordTokenWeights) {
		// query = query.replaceAll("'", "''");
		JaroWinkler jw = new JaroWinkler();
		// double score = jw.score(jw.prepare(str1), jw.prepare(str2));

		List<IdScore> scoreList = new ArrayList<IdScore>();
		HashMap<String, Double> queryWeights = getQueryWeights(query, qgramIDF);
		for (int k = 0; k < recordTokenWeights.size(); k++) {
			double score = 0;
			HashMap<String, Double> fileWeights = recordTokenWeights.get(k);
			
			if(fileWeights.containsKey("union")){
				localDebugMode = true;
			}else{
				localDebugMode = false;
			}
			if((k > 320) && (k < 325)){
				localDebugMode = true;
			}else{
				localDebugMode = false;
			}
			printDebug("QUERY: "+query+"   RECORD: "+(k+1) + "=====================================\n");
			for (String qgram : queryWeights.keySet()) {
				// if (fileWeights.containsKey(qgram)) {
				String maxSimilarToken = getMaximumSimilarToken(qgram, fileWeights.keySet(), jw);
				double jaroWinklerSimilarityScore = jw.score(jw.prepare(qgram), jw.prepare(maxSimilarToken));

            // A match is considered only when JWsim-score > 0.8 thats the threshold
            if(jaroWinklerSimilarityScore > jwSimilarityThreshold){
   				score += fileWeights.get(maxSimilarToken) * queryWeights.get(qgram) * jaroWinklerSimilarityScore;
   				printDebug(qgram+" "+queryWeights.get(qgram)+" :: ");
	   			printDebug(maxSimilarToken+" "+fileWeights.get(maxSimilarToken)+" ::  jwSim "+jaroWinklerSimilarityScore+"\n");
            }

				// }
			}
			if(score > 0.5){
				localDebugMode = true;
			}
			printDebug("QUERY: "+query+"   RECORD: "+(k+1) + "=====================================\n");
			printDebug("final Score: "+score+"\n");
			printDebug("===========================================================================\n");
			scoreList.add(new IdScore(k, score));
		}
		// // System.gc();
		Collections.sort(scoreList);
		return scoreList;
	}
}
