select * from
(SELECT tid1, tid2, score FROM cnamesf.scores_AB_weightedjaccardbm25 s where tid1!=tid2
UNION
SELECT tid2 as tid1, tid1 as tid2, score FROM cnamesf.scores_AB_weightedjaccardbm25 s where tid1!=tid2) c
order by tid1, tid2
INTO OUTFILE "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/ABscores.txt"
FIELDS TERMINATED BY ' ';

select * from
(SELECT tid1, tid2, score FROM cnamesf.scores_TS_weightedjaccardbm25 s where tid1!=tid2
UNION
SELECT tid2 as tid1, tid1 as tid2, score FROM cnamesf.scores_TS_weightedjaccardbm25 s where tid1!=tid2) c
order by tid1, tid2
INTO OUTFILE "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/TSscores.txt"
FIELDS TERMINATED BY ' ';

select * from
(SELECT tid1, tid2, score FROM cnamesf.scores_EDL_weightedjaccardbm25 s where tid1!=tid2
UNION
SELECT tid2 as tid1, tid1 as tid2, score FROM cnamesf.scores_EDL_weightedjaccardbm25 s where tid1!=tid2) c
order by tid1, tid2
INTO OUTFILE "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/EDLscores.txt"
FIELDS TERMINATED BY ' ';

select * from
(SELECT tid1, tid2, score FROM cnamesf.scores_EDM_weightedjaccardbm25 s where tid1!=tid2
UNION
SELECT tid2 as tid1, tid1 as tid2, score FROM cnamesf.scores_EDM_weightedjaccardbm25 s where tid1!=tid2) c
order by tid1, tid2
INTO OUTFILE "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/EDMscores.txt"
FIELDS TERMINATED BY ' ';


select * from
(SELECT tid1, tid2, score FROM cnamesf.scores_EDH_weightedjaccardbm25 s where tid1!=tid2
UNION
SELECT tid2 as tid1, tid1 as tid2, score FROM cnamesf.scores_EDH_weightedjaccardbm25 s where tid1!=tid2) c
order by tid1, tid2
INTO OUTFILE "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/EDHscores.txt"
FIELDS TERMINATED BY ' ';