This folder contains

OCR.java which is the program for Ordered Concurrent Rippling clustering algorithm
CR.java which is the program for Concurrent Rippling clustering algorithm
BSR.java which is the program for Balanced Sequential Rippling clustering algorithm
SR.java which is the program for Sequential Rippling clustering algorithm
google1.txt and categoryGoogle1.txt which are the sample data file (see description below)

In each of this java file:

input is specified in the String "myargument" , each in the form of: A B C D 
(for example: myargument = "1019 google1.txt 15 categoryGoogle1.txt";)

where A = number of objects to cluster e.g. 1010
      B = name of file containing the similarities (weights of edges) between each pair of objects (directed edge) 
	  hence, if object 1 has an undirected edge to object 2, this file must contain both these lines:
		fil:1 fil:2 sim_between_1_and_2 
		fil:2 fil:1 sim_between_2_and_1
	  "fil:x" is just the notation used to specify an object; where x is the (integer) index of the object
      C = number of clusters to expect in the correct clustering (this is for measuring of precision, recall)
      D = name of file containing the correct clustering
      	  each line of this file contains the information about a cluster: 
          i.e. the cluster label and the objects who are member of the cluster
          e.g. babel fil:1 fil:13 fil:20 fil:55
          (in this case babel is the label of the cluster and objects with index 1, 13, 20, 55 are its members)

the program will produce the average precision, recall, and F1 value for the input you specify
