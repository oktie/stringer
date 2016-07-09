DROP TABLE IF EXISTS `tst`;
CREATE TABLE  `tst` (
  `tid` bigint,
  `id` bigint,
  `clean` varchar(100),
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/CU1.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`CU1`;
CREATE TABLE  `cnamesu`.`CU1` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`CU1`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/CU2.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`CU2`;
CREATE TABLE  `cnamesu`.`CU2` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`CU2`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/CU3.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`CU3`;
CREATE TABLE  `cnamesu`.`CU3` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`CU3`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/CU4.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`CU4`;
CREATE TABLE  `cnamesu`.`CU4` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`CU4`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/CU5.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`CU5`;
CREATE TABLE  `cnamesu`.`CU5` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`CU5`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/CU6.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`CU6`;
CREATE TABLE  `cnamesu`.`CU6` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`CU6`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/CU7.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`CU7`;
CREATE TABLE  `cnamesu`.`CU7` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`CU7`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/CU8.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`CU8`;
CREATE TABLE  `cnamesu`.`CU8` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`CU8`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/CZ1.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesz`.`CZ1`;
CREATE TABLE  `cnamesz`.`CZ1` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesz`.`CZ1`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/CZ2.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesz`.`CZ2`;
CREATE TABLE  `cnamesz`.`CZ2` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesz`.`CZ2`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/CZ3.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesz`.`CZ3`;
CREATE TABLE  `cnamesz`.`CZ3` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesz`.`CZ3`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/CZ4.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesz`.`CZ4`;
CREATE TABLE  `cnamesz`.`CZ4` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesz`.`CZ4`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/CZ5.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesz`.`CZ5`;
CREATE TABLE  `cnamesz`.`CZ5` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesz`.`CZ5`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/CZ6.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesz`.`CZ6`;
CREATE TABLE  `cnamesz`.`CZ6` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesz`.`CZ6`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/CZ7.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesz`.`CZ7`;
CREATE TABLE  `cnamesz`.`CZ7` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesz`.`CZ7`
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
LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/cname-datasets/CZ8.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesz`.`CZ8`;
CREATE TABLE  `cnamesz`.`CZ8` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesz`.`CZ8`
SELECT tid, id, string
FROM tst;