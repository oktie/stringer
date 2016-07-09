﻿select * from
(SELECT tid1, tid2, score FROM dblp.scores_dblpl1_weightedjaccardbm25 s where tid1!=tid2
UNION
SELECT tid2 as tid1, tid1 as tid2, score FROM dblp.scores_dblpl1_weightedjaccardbm25 s where tid1!=tid2) c
order by tid1, tid2
INTO OUTFILE "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/DBLPL1scores.txt"
FIELDS TERMINATED BY ' ';

select * from
(SELECT tid1, tid2, score FROM dblp.scores_dblpl2_weightedjaccardbm25 s where tid1!=tid2
UNION
SELECT tid2 as tid1, tid1 as tid2, score FROM dblp.scores_dblpl2_weightedjaccardbm25 s where tid1!=tid2) c
order by tid1, tid2
INTO OUTFILE "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/DBLPL2scores.txt"
FIELDS TERMINATED BY ' ';

select * from
(SELECT tid1, tid2, score FROM dblp.scores_dblpm1_weightedjaccardbm25 s where tid1!=tid2
UNION
SELECT tid2 as tid1, tid1 as tid2, score FROM dblp.scores_dblpm1_weightedjaccardbm25 s where tid1!=tid2) c
order by tid1, tid2
INTO OUTFILE "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/DBLPM1scores.txt"
FIELDS TERMINATED BY ' ';

select * from
(SELECT tid1, tid2, score FROM dblp.scores_dblpm2_weightedjaccardbm25 s where tid1!=tid2
UNION
SELECT tid2 as tid1, tid1 as tid2, score FROM dblp.scores_dblpm2_weightedjaccardbm25 s where tid1!=tid2) c
order by tid1, tid2
INTO OUTFILE "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/DBLPM2scores.txt"
FIELDS TERMINATED BY ' ';

select * from
(SELECT tid1, tid2, score FROM dblp.scores_dblpm3_weightedjaccardbm25 s where tid1!=tid2
UNION
SELECT tid2 as tid1, tid1 as tid2, score FROM dblp.scores_dblpm3_weightedjaccardbm25 s where tid1!=tid2) c
order by tid1, tid2
INTO OUTFILE "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/DBLPM3scores.txt"
FIELDS TERMINATED BY ' ';

select * from
(SELECT tid1, tid2, score FROM dblp.scores_dblpm4_weightedjaccardbm25 s where tid1!=tid2
UNION
SELECT tid2 as tid1, tid1 as tid2, score FROM dblp.scores_dblpm4_weightedjaccardbm25 s where tid1!=tid2) c
order by tid1, tid2
INTO OUTFILE "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/DBLPM4scores.txt"
FIELDS TERMINATED BY ' ';

select * from
(SELECT tid1, tid2, score FROM dblp.scores_dblph1_weightedjaccardbm25 s where tid1!=tid2
UNION
SELECT tid2 as tid1, tid1 as tid2, score FROM dblp.scores_dblph1_weightedjaccardbm25 s where tid1!=tid2) c
order by tid1, tid2
INTO OUTFILE "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/DBLPH1scores.txt"
FIELDS TERMINATED BY ' ';

select * from
(SELECT tid1, tid2, score FROM dblp.scores_dblph2_weightedjaccardbm25 s where tid1!=tid2
UNION
SELECT tid2 as tid1, tid1 as tid2, score FROM dblp.scores_dblph2_weightedjaccardbm25 s where tid1!=tid2) c
order by tid1, tid2
INTO OUTFILE "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/DBLPH2scores.txt"
FIELDS TERMINATED BY ' ';
