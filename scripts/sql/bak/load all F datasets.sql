DROP TABLE IF EXISTS `tst`;
CREATE TABLE  `tst` (
  `tid` bigint,
  `id` bigint,
  `clean` varchar(100),
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-f-datasets/F1.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`F1`;
CREATE TABLE  `cnamesu`.`F1` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`F1`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-f-datasets/F2.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`F2`;
CREATE TABLE  `cnamesu`.`F2` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`F2`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-f-datasets/F3.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`F3`;
CREATE TABLE  `cnamesu`.`F3` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`F3`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-f-datasets/F4.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`F4`;
CREATE TABLE  `cnamesu`.`F4` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`F4`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-f-datasets/F5.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`F5`;
CREATE TABLE  `cnamesu`.`F5` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`F5`
SELECT tid, id, string
FROM tst;

