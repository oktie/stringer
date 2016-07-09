﻿DROP TABLE IF EXISTS test.`all_results`;

CREATE TABLE  test.`all_results` (
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

INSERT INTO test.`all_results`
SELECT * FROM cnames.clustering_results;

INSERT INTO test.`all_results`
SELECT * FROM cnames.ricochet_results;

INSERT INTO test.`all_results`
SELECT * FROM cnames.star_results;

INSERT INTO test.`all_results`
SELECT * FROM cnames.mcl_results;

INSERT INTO test.`all_results`
SELECT * FROM cnames.ccl_results;

INSERT INTO test.`all_results`
SELECT * FROM cnames.artpt_results;

INSERT INTO test.`all_results`
SELECT * FROM cnames.mincut_results;


DELETE FROM test.all_results
WHERE alg="pairscan";

UPDATE test.all_results
SET class = "1-high"
WHERE class = "high";

UPDATE test.all_results
SET class = "2-med"
WHERE class = "med";

UPDATE test.all_results
SET class = "3-low"
WHERE class = "low";