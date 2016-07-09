DROP TABLE IF EXISTS `tst`;
CREATE TABLE  `tst` (
  `tid` bigint,
  `id` bigint,
  `clean` varchar(100),
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/P5.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`P5`;
CREATE TABLE  `cnamesu`.`P5` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`P5`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/P10.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`P10`;
CREATE TABLE  `cnamesu`.`P10` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`P10`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/P15.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`P15`;
CREATE TABLE  `cnamesu`.`P15` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`P15`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/P20.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`P20`;
CREATE TABLE  `cnamesu`.`P20` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`P20`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/P25.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`P25`;
CREATE TABLE  `cnamesu`.`P25` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`P25`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/P30.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`P30`;
CREATE TABLE  `cnamesu`.`P30` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`P30`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/P35.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`P35`;
CREATE TABLE  `cnamesu`.`P35` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`P35`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/P40.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`P40`;
CREATE TABLE  `cnamesu`.`P40` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`P40`
SELECT tid, id, string
FROM tst;
