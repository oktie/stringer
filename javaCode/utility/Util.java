/*******************************************************************************
 * Copyright (c) 2006-2007 University of Toronto Database Group
 *     
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
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
