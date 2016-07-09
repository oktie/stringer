package utility;

import java.util.*;

public class Util
{
	public static boolean localDebugMode = false;
	
	public static double getIDFWeight(String token, HashMap<String, Double> qgramIDF, double meanIDF) {
		if (qgramIDF.containsKey(token)) {
			return qgramIDF.get(token);
		} else {
			return meanIDF;
		}
	}	
	
	public static double getMeanIDF(HashMap<String, Double> qgramIDF){
		double meanIDF=0;
		if(qgramIDF.size() <= 0){
			return 0;
		}
		for(String token: qgramIDF.keySet()){
			meanIDF += qgramIDF.get(token);
		}
		return meanIDF/qgramIDF.size();
	}

	public static double getWeightedSumForTokenSet(Set<String> tokenSet, HashMap<String, Double> qgramIDF, double meanIDF){
		double weightedSum=0;
		for(String token: tokenSet){
			weightedSum += getIDFWeight(token, qgramIDF, meanIDF);
		}
		return weightedSum;
	}
	
	public static void printDebug(String str){
		if(localDebugMode)
			System.out.print(str);
	}

	public static void printlnDebug(String str){
		if(localDebugMode)
			System.out.println(str);
	}
	
	public static void printlnDebug(Set<String> tokenSet){
		if(localDebugMode){
			System.out.print(tokenSet.size()+"   ");
			System.out.print("[ ");
			for(String token: tokenSet)
				System.out.print(token+" , ");
			System.out.println(" ]");
		}
	}
}
