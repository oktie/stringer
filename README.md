# STRINGER: Duplicate Detection System for String Data

This is the original code base from the STRINGER project at University of Toronto's Database Group: http://dblab.cs.toronto.edu/project/stringer/

The project has now been retired and is no longer actively maintained. The code base was developed in 2006-2007 without an intent to be made an open-source collaborative project, and as a result, is far from well documented and tested. Nevertheless, we believe making the code base publicly available would still be of value to the community as many of the components still do not have an open-source alternative implementation.

The goal of the project was to implement and experimentally evaluate the effectiveness of various string similarity measures and clustering algorithms for duplicate detection (also referred to as: entity resolution, record linkage, fuzzy string matching, record matching) in relational databases. There is also code for assigning probability scores to duplicate records in each cluster to create a probabilistic database out of dirty data.

The code is made available under the MIT license. We have tried our best to remove any code that is not developed as a part of the STRINGER project (or copied from other projects) but given that many years have passed since the code's active development, we strongly recommend that you carefully check all the code for other copyright notices or proprietary algorithms or code before relying on it for commercial purposes.

The following articles describe the details of some the algorithms implemented in this code, along with the results of extensive experimental evaluation of the algorithms:

* Oktie Hassanzadeh, Fei Chiang, Renée J. Miller, Hyun Chul Lee: Framework for Evaluating Clustering Algorithms in Duplicate Detection. PVLDB 2(1): 1282-1293 (2009). http://www.vldb.org/pvldb/2/vldb09-1025.pdf
* Oktie Hassanzadeh, Renée J. Miller: Creating probabilistic databases from duplicated data. VLDB J. 18(5): 1141-1166 (2009)
* Oktie Hassanzadeh, Mohammad Sadoghi, Renée J. Miller:Accuracy of Approximate String Joins Using Grams. QDB 2007: 11-18
* Oktie Hassanzadeh: Benchmarking Declarative Approximate Selection Predicates. M.Sc. Thesis, University of Toronto. http://arxiv.org/abs/0907.2471
* Derry Tanti Wijaya, Stéphane Bressan: Ricochet: A Family of Unconstrained Algorithms for Graph Clustering. DASFAA 2009: 153-167
