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
 /**
 * 
 */
package evaluation;

/**
 * 
 * Defines all the accuracy measures for quality evaluation
 * 
 */
public class AccuracyMeasure {

	/**
	 *  Calculates precision at the recall levels 0.1, 0.2, ... ,.09 , 1 and
	 *  returns all these precision values in a ordered array where first element 
	 *  is the precision at the greatest recall-level less than than 0.1; second 
	 *  element is the precision at the greatest recall-level >= 0.1 and < 0.2 and so on .
	 * 
	 * @param orderedList 
	 * @param expectedRecords 
	 * @return double[] 
	 * 
	 */
	public static double[] elevenPointPreciosn(int[] orderedList, int expectedRecords){
		double[] precisionArray = {0,0,0,0,0,0,0,0,0,0,0};
		double[] allOnes = {1,1,1,1,1,1,1,1,1,1,1};
		if(expectedRecords <= 0){
			return allOnes;
		}else if(orderedList == null){
			return precisionArray;
		}else if(orderedList.length == 0){
			return precisionArray;
		}else{
			double currentPrecisionValue = 0.0;
			int lastUpdatedIndex = -1;
			int indexToUpdate = 0;
			int relevantRecordsTillNow = 0;
			int recordsTillNow = 0;
			for(int i: orderedList){
				recordsTillNow++;
				if(i==1){
					relevantRecordsTillNow++;
					indexToUpdate = (int) (relevantRecordsTillNow*10.0)/(expectedRecords);
					currentPrecisionValue = (relevantRecordsTillNow*1.0)/recordsTillNow;
					for(int k=lastUpdatedIndex+1; k< indexToUpdate; k++){
						precisionArray[k] = currentPrecisionValue;
					}
					precisionArray[indexToUpdate] = currentPrecisionValue;
					lastUpdatedIndex = indexToUpdate;
				}
			}
			return precisionArray;
		}
	}
	
	/**
	 * 
	 *  Calculates precision at the recall levels 0.1, 0.2, ... ,.09 , 1 and
	 *  returns all these precision values in a ordered array where first element 
	 *  is the precision at recall-level equal or greater than 0.1 and so on.
	 *  It just finds the expected number of relevant records (expectedRecords) 
	 *  and calls the function elevenPointPreciosn(orderedList, expectedRecords).
	 *  
	 * @param orderedList  
	 * @return double[] 
	 * 
	 */
	public static double[] elevenPointPreciosn(int[] orderedList){
		double[] precisionArray = {1,1,1,1,1,1,1,1,1,1,1};
		if(orderedList == null){
			return precisionArray;
		}else if(orderedList.length == 0){
			return precisionArray;
		}else{
			int expectedRecords = 0;
			for(int i: orderedList){
				if(i==1){
					expectedRecords++;
				}
			}
			return elevenPointPreciosn(orderedList, expectedRecords);
		}
	}

	/**
	 *  To find the precision at the positionK. If the argument positionK is 0, it returns 0.
	 *  if the list is empty, it returns 0. positionK = 1 means the first element of the orderedList.
	 * 
	 * @param orderedList
	 * @param positionK
	 * @return
	 */
	public static double precisionAtTopK(int[] orderedList, int positionK){
		double precision=0.0;
		int relevantRecords = 0;
		if((orderedList != null) && (orderedList.length > 0) && (positionK > 0)){
			int finalPosition = Math.min(positionK, orderedList.length);
			for(int i=0;i < finalPosition; i++){
				if(orderedList[i] == 1){
					relevantRecords++;
				}
			}
			return (relevantRecords*1.0)/positionK;
		}else{
			return precision;
		}
	}
	/**
	 * 
	 *  Calculates precision at each point a relevant record is found and then 
	 *  takes an average over all the precision values and return the average.
	 *  expectedRecord argument is just required for efficiency so that it can 
	 *  stop iterating over the array once it has found expectedRecords relevant
	 *  records.
	 *  
	 * @param orderedList 
	 * @param expectedRecords 
	 * @return double 
	 * 
	 */	
	public static double meanAveragePrecision(int[] orderedList, int expectedRecords){
		if(orderedList == null){
			return 0.0;
		}		
		double sumPrecision = 0.0;
		int relevantRecords = 0;
		int records=0;
		for(int i:orderedList){
			records++;
			if(i==1){
				relevantRecords++;
				sumPrecision += ( relevantRecords*1.0 )/records;
				if(relevantRecords >= expectedRecords){
					break;
				}
			}
		}
		if(relevantRecords > 0){
			return sumPrecision/expectedRecords;
		}else{
			return 0.0;
		}
	}

	/**
	 * 
	 *  Calculates precision at each point a relevant record is found and then 
	 *  takes an average over all the precision vlaues and return the average.
	 *  It just calls the function 
	 *  meanAveragePrecision(orderedList, orderedList.length).
	 *  
	 *  
	 * @param orderedList 
	 * @param expectedRecords 
	 * @return double 
	 * @see meanAveragePrecision(int[] orderedList)
	 * 
	 */		
	/* fix this to find exppectedRecords
	 * public static double meanAveragePrecision(int[] orderedList){
		if(orderedList == null){
			return 0.0;
		}
		return meanAveragePrecision(orderedList, orderedList.length);
	}
	*/
	/**
	 *  Finds the position (k) of the first occurrence of a relevant record 
	 *  and return (1/k) i.e. the reciprocal rank. if no relevant record 
	 *  occur then it returns 0.
	 * 
	 * @param orderedList
	 * @return double
	 * 
	 */
	public static double reciprocalRank(int[] orderedList){
		if(orderedList == null){
			return 0.0;
		}
		double rank = 0.0;
		int position =0;
		boolean relevantRecordFound=false;
		for(int i: orderedList){
			position++;
			if(i==1){
				relevantRecordFound=true;
				break;
			}
		}
		if(relevantRecordFound){
			rank = 1.0/position;
		}
		return rank;
	}
	
	/**
	 * 
	 * To print the integer array.
	 * 
	 * @param list
	 * 
	 */
	public static void printList(int[] list){
		System.out.print("[");
		if(list != null){
			for(int i: list){
				System.out.print(i+", ");
			}
		}
		System.out.println("]");
	}
	
	/**
	 * 
	 * To print the double array.
	 * 
	 * @param list
	 * 
	 */
	public static void printList(double[] list){
		System.out.print("[");
		if(list != null){
			for(double i: list){
				System.out.print(i+", ");
			}
		}
		System.out.println("]");
	}
	
	/*
	 *  Just to check the implementation of the above functions.
	*/ 
	public static void main(String[] args){
		int myList1[] = {1,0,1,0,1,1,1,1,1,0,1,1,1};
		int[] myList2 = {0,0,1,0};
		double rank=0.0, map=0.0, precitionAtTopK=0.0;
		double[] elevenPtPrecision = null;
		System.out.print("Ordered List:  ");
		AccuracyMeasure.printList(myList1);
		rank = AccuracyMeasure.reciprocalRank(myList1);
		System.out.println("Reciprocal Rank: "+rank);
		//map = AccuracyMeasure.meanAveragePrecision(myList1);
		System.out.println("MAP: "+map);
		elevenPtPrecision = AccuracyMeasure.elevenPointPreciosn(myList1);
		AccuracyMeasure.printList(elevenPtPrecision);
		for(int j=0; j< myList1.length; j++){
			precitionAtTopK = AccuracyMeasure.precisionAtTopK(myList1, j+1);
			System.out.println("Precision at Top "+(j+1)+" : "+precitionAtTopK);
		}
		
		System.out.println();
		
		System.out.print("Ordered List:  ");
		AccuracyMeasure.printList(myList2);
		rank = AccuracyMeasure.reciprocalRank(myList2);
		System.out.println("Reciprocal Rank: "+rank);
		//map = AccuracyMeasure.meanAveragePrecision(myList2);
		System.out.println("MAP: "+map);
		elevenPtPrecision = AccuracyMeasure.elevenPointPreciosn(myList2);
		AccuracyMeasure.printList(elevenPtPrecision);
	}
	
}
