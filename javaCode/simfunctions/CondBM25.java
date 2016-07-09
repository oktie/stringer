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
