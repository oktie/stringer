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


public class Config 
{
	public String cfgFileName;
	public String dbName;
	public String host ;
	public String user ;
	public String passwd;
	public String port ;
	public String url ;
	public boolean useFile;
	public String fileName;
	public String dbServer;
	public String outputFile;
    public String preprocessingColumn;
	public boolean debugMode;
	public int dbOption;
	
	public static String storeResultDirectory = "c:/Users/admin/dcp-workspace/log/";
    public static String queryFileName = "c:/Users/admin/dcp-workspace/data/_fixedQueries_";
    public static String expResultTablePrefix = "_ExpRes_"; 
	public static String queryClass = "dcp.clean";
    
	public static boolean tokenizeUsingQgrams = true;
    public int numHeaders;
    

	public String bm25WeightTable = "$Aux$_BM253_0_cu1_string_BMBaseWeights";
	public String tfidfWeightTable = "$Aux$_TfIdf3_0_cu1_string_Weights";
	// Class constructor 
	public Config() {
		outputFile = "geneRatios.txt";
		
		host = "localhost";
		user = "root";
		passwd = "";
		port = "3306";
		url = "";
		useFile = false;
	    fileName = "tmpFile";
	    dbServer = "mysql";
	    numHeaders = 7;
	    dbName = "midas";
	    preprocessingColumn = "name";
	    
	    //dbName = "probtest";
	    //preprocessingColumn = "title";
	}
	
	public String returnURL(){
		if(dbOption == 1)
			url = "jdbc:postgres://"+ host + ":"+ port +"/"+ dbName;
		else
			url = "jdbc:mysql://"+host+":"+port+"/"+dbName ;
		return url;
	}
	
	public void setDebugMode(boolean val){
		debugMode = val;
	}

	public void setDbName(String name){
		dbName = name;
	}
	
}
