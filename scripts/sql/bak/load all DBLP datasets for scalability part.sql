DROP TABLE IF EXISTS `tst`;
CREATE TABLE  `tst` (
  `tid` bigint,
  `id` bigint,
  `clean` varchar(100),
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);

LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/titles-datasets/5K.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`5K`;
CREATE TABLE  `cnamesu`.`5K` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`5K`
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

LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/titles-datasets/10K.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`10K`;
CREATE TABLE  `cnamesu`.`10K` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`10K`
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

LOAD DATA LOCAL INFILE '//manhattan.db.toronto.edu/oktie/data/titles-datasets/20K.txt'
INTO TABLE tst
FIELDS TERMINATED BY ':'
LINES TERMINATED BY '\n'
(tid, id, clean, string);
DROP TABLE IF EXISTS `cnamesu`.`20K`;
CREATE TABLE  `cnamesu`.`20K` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  PRIMARY KEY  (`tid`)
);
INSERT INTO `cnamesu`.`20K`
SELECT tid, id, string
FROM tst;
