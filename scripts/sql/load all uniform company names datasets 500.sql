DROP TABLE IF EXISTS `tst`;
CREATE TABLE  `tst` (
  `tid` bigint,
  `id` bigint,
  `clean` varchar(100),
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
LOAD DATA LOCAL INFILE 'C:/Documents and Settings/oktie/dcp-workspace/MemStringer/datasets/cname-datasets-500/CU1.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu500`.`CU1`;
CREATE TABLE  `cnamesu500`.`CU1` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu500`.`CU1`
SELECT tid, id, string
FROM tst;



DROP TABLE IF EXISTS `tst`;
CREATE TABLE  `tst` (
  `tid` bigint,
  `id` bigint,
  `clean` varchar(100),
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
LOAD DATA LOCAL INFILE 'C:/Documents and Settings/oktie/dcp-workspace/MemStringer/datasets/cname-datasets-500/CU2.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu500`.`CU2`;
CREATE TABLE  `cnamesu500`.`CU2` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu500`.`CU2`
SELECT tid, id, string
FROM tst;





DROP TABLE IF EXISTS `tst`;
CREATE TABLE  `tst` (
  `tid` bigint,
  `id` bigint,
  `clean` varchar(100),
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
LOAD DATA LOCAL INFILE 'C:/Documents and Settings/oktie/dcp-workspace/MemStringer/datasets/cname-datasets-500/CU3.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu500`.`CU3`;
CREATE TABLE  `cnamesu500`.`CU3` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu500`.`CU3`
SELECT tid, id, string
FROM tst;




DROP TABLE IF EXISTS `tst`;
CREATE TABLE  `tst` (
  `tid` bigint,
  `id` bigint,
  `clean` varchar(100),
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
LOAD DATA LOCAL INFILE 'C:/Documents and Settings/oktie/dcp-workspace/MemStringer/datasets/cname-datasets-500/CU4.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu500`.`CU4`;
CREATE TABLE  `cnamesu500`.`CU4` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu500`.`CU4`
SELECT tid, id, string
FROM tst;




DROP TABLE IF EXISTS `tst`;
CREATE TABLE  `tst` (
  `tid` bigint,
  `id` bigint,
  `clean` varchar(100),
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
LOAD DATA LOCAL INFILE 'C:/Documents and Settings/oktie/dcp-workspace/MemStringer/datasets/cname-datasets-500/CU5.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu500`.`CU5`;
CREATE TABLE  `cnamesu500`.`CU5` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu500`.`CU5`
SELECT tid, id, string
FROM tst;





DROP TABLE IF EXISTS `tst`;
CREATE TABLE  `tst` (
  `tid` bigint,
  `id` bigint,
  `clean` varchar(100),
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
LOAD DATA LOCAL INFILE 'C:/Documents and Settings/oktie/dcp-workspace/MemStringer/datasets/cname-datasets-500/CU6.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu500`.`CU6`;
CREATE TABLE  `cnamesu500`.`CU6` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu500`.`CU6`
SELECT tid, id, string
FROM tst;




DROP TABLE IF EXISTS `tst`;
CREATE TABLE  `tst` (
  `tid` bigint,
  `id` bigint,
  `clean` varchar(100),
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
LOAD DATA LOCAL INFILE 'C:/Documents and Settings/oktie/dcp-workspace/MemStringer/datasets/cname-datasets-500/CU7.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu500`.`CU7`;
CREATE TABLE  `cnamesu500`.`CU7` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu500`.`CU7`
SELECT tid, id, string
FROM tst;





DROP TABLE IF EXISTS `tst`;
CREATE TABLE  `tst` (
  `tid` bigint,
  `id` bigint,
  `clean` varchar(100),
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
LOAD DATA LOCAL INFILE 'C:/Documents and Settings/oktie/dcp-workspace/MemStringer/datasets/cname-datasets-500/CU8.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu500`.`CU8`;
CREATE TABLE  `cnamesu500`.`CU8` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu500`.`CU8`
SELECT tid, id, string
FROM tst;