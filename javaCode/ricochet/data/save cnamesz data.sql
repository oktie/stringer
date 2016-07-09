select * from
(SELECT tid1, tid2, score FROM cnamesz.scores_zl1_weightedjaccardbm25 s where tid1!=tid2
UNION
SELECT tid2 as tid1, tid1 as tid2, score FROM cnamesz.scores_zl1_weightedjaccardbm25 s where tid1!=tid2) c
order by tid1, tid2
INTO OUTFILE "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/ZL1scores.txt"
FIELDS TERMINATED BY ' ';

select * from
(SELECT tid1, tid2, score FROM cnamesz.scores_zl2_weightedjaccardbm25 s where tid1!=tid2
UNION
SELECT tid2 as tid1, tid1 as tid2, score FROM cnamesz.scores_zl2_weightedjaccardbm25 s where tid1!=tid2) c
order by tid1, tid2
INTO OUTFILE "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/ZL2scores.txt"
FIELDS TERMINATED BY ' ';

select * from
(SELECT tid1, tid2, score FROM cnamesz.scores_zm1_weightedjaccardbm25 s where tid1!=tid2
UNION
SELECT tid2 as tid1, tid1 as tid2, score FROM cnamesz.scores_zm1_weightedjaccardbm25 s where tid1!=tid2) c
order by tid1, tid2
INTO OUTFILE "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/ZM1scores.txt"
FIELDS TERMINATED BY ' ';

select * from
(SELECT tid1, tid2, score FROM cnamesz.scores_zm2_weightedjaccardbm25 s where tid1!=tid2
UNION
SELECT tid2 as tid1, tid1 as tid2, score FROM cnamesz.scores_zm2_weightedjaccardbm25 s where tid1!=tid2) c
order by tid1, tid2
INTO OUTFILE "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/ZM2scores.txt"
FIELDS TERMINATED BY ' ';

select * from
(SELECT tid1, tid2, score FROM cnamesz.scores_zm3_weightedjaccardbm25 s where tid1!=tid2
UNION
SELECT tid2 as tid1, tid1 as tid2, score FROM cnamesz.scores_zm3_weightedjaccardbm25 s where tid1!=tid2) c
order by tid1, tid2
INTO OUTFILE "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/ZM3scores.txt"
FIELDS TERMINATED BY ' ';

select * from
(SELECT tid1, tid2, score FROM cnamesz.scores_zm4_weightedjaccardbm25 s where tid1!=tid2
UNION
SELECT tid2 as tid1, tid1 as tid2, score FROM cnamesz.scores_zm4_weightedjaccardbm25 s where tid1!=tid2) c
order by tid1, tid2
INTO OUTFILE "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/ZM4scores.txt"
FIELDS TERMINATED BY ' ';


select * from
(SELECT tid1, tid2, score FROM cnamesz.scores_zh1_weightedjaccardbm25 s where tid1!=tid2
UNION
SELECT tid2 as tid1, tid1 as tid2, score FROM cnamesz.scores_zh1_weightedjaccardbm25 s where tid1!=tid2) c
order by tid1, tid2
INTO OUTFILE "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/ZH1scores.txt"
FIELDS TERMINATED BY ' ';

select * from
(SELECT tid1, tid2, score FROM cnamesz.scores_zh2_weightedjaccardbm25 s where tid1!=tid2
UNION
SELECT tid2 as tid1, tid1 as tid2, score FROM cnamesz.scores_zh2_weightedjaccardbm25 s where tid1!=tid2) c
order by tid1, tid2
INTO OUTFILE "C:/Users/admin/dcp-workspace/MemStringer/javaCode/ricochet/data/ZH2scores.txt"
FIELDS TERMINATED BY ' ';