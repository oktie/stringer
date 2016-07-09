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
