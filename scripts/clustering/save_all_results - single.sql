DROP TABLE IF EXISTS test.`allf_results`;

CREATE TABLE  test.`allf_results` (
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

INSERT INTO test.`allf_results`
SELECT * FROM cnamesf.clustering_results;

INSERT INTO test.`allf_results`
SELECT * FROM cnamesf.ricochet_results;

INSERT INTO test.`allf_results`
SELECT * FROM cnamesf.star_results;

INSERT INTO test.`allf_results`
SELECT * FROM cnamesf.mcl_results;

INSERT INTO test.`allf_results`
SELECT * FROM cnamesf.ccl_results;

INSERT INTO test.`allf_results`
SELECT * FROM cnamesf.artpt_results;

INSERT INTO test.`allf_results`
SELECT * FROM cnamesf.mincut_results;

#UPDATE test.tmp_results
#SET alg="mincut";
INSERT INTO test.allf_results
SELECT * FROM test.tmpf_results
WHERE thr1 not in (select thr1 from test.allf_results where alg='mincut');


DELETE FROM test.allf_results
WHERE alg="pairscan";

UPDATE allf_results
SET class="1-TS"
WHERE tbl = "TS";

UPDATE allf_results
SET class="2-AB"
WHERE tbl = "AB";

UPDATE allf_results
SET class="31-EDH"
WHERE tbl = "EDH";

UPDATE allf_results
SET class="32-EDM"
WHERE tbl = "EDM";

UPDATE allf_results
SET class="33-EDL"
WHERE tbl = "EDL";