DROP TABLE IF EXISTS `PE`;
CREATE TABLE  `PE` (
  `tid` bigint,
  `id` bigint,
  `string` varchar(100),
  `errorp` bigint,
  PRIMARY KEY  (`tid`)
);


INSERT INTO PE
SELECT tid, id, `string`, 5
FROM cnamesu.P5;

INSERT INTO PE
SELECT tid+500, id, `string`, 10
FROM cnamesu.P10;

INSERT INTO PE
SELECT tid+1000, id, `string`, 15
FROM cnamesu.P15;

INSERT INTO PE
SELECT tid+1500, id, `string`, 20
FROM cnamesu.P20;

INSERT INTO PE
SELECT tid+2000, id, `string`, 25
FROM cnamesu.P25;

INSERT INTO PE
SELECT tid+2500, id, `string`, 30
FROM cnamesu.P30;

INSERT INTO PE
SELECT tid+3000, id, `string`, 35
FROM cnamesu.P35;