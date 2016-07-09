DROP TABLE IF EXISTS test.`allz_results`;

CREATE TABLE  test.`allz_results` (
  `alg` varchar(10) NOT NULL default '',
  `tbl` varchar(10) NOT NULL default '',
  `class` varchar(10) NOT NULL default '',
  `thr1` double NOT NULL default '0',
  `thr2` double NOT NULL default '0',
  `pcpr` double default NULL,
  `cpr` double default NULL,
  `pr` double default NULL,
  `re` double default NULL,
  `f1` double default NULL,
  `ccount` int(11) default NULL,
  PRIMARY KEY  (`alg`,`tbl`,`class`,`thr1`,`thr2`)
);

INSERT INTO test.`allz_results`
SELECT * FROM cnamesz.clustering_results;

INSERT INTO test.`allz_results`
SELECT * FROM cnamesz.ricochet_results;

INSERT INTO test.`allz_results`
SELECT * FROM cnamesz.star_results;

INSERT INTO test.`allz_results`
SELECT * FROM cnamesz.mcl_results;

INSERT INTO test.`allz_results`
SELECT * FROM cnamesz.ccl_results;

INSERT INTO test.`allz_results`
SELECT * FROM cnamesz.artpt_results;

#INSERT INTO test.`allz_results`
#SELECT * FROM cnamesz.mincut_results;
UPDATE test.tmp_results
SET alg="mincut";
INSERT INTO test.allz_results
SELECT * FROM test.tmp_results;



DELETE FROM test.allz_results
WHERE alg="pairscan";

UPDATE test.allz_results
SET class = "1-high"
WHERE class = "high";

UPDATE test.allz_results
SET class = "2-med"
WHERE class = "med";

UPDATE test.allz_results
SET class = "3-low"
WHERE class = "low";