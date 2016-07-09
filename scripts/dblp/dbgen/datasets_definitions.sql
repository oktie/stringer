DROP TABLE IF EXISTS `test`.`datasets`;


CREATE TABLE  `test`.`datasets` (
  `name` varchar(5) NOT NULL,
  `Rsize` int NOT NULL,
  `Csize` int NOT NULL,
  `Perr` int NOT NULL,
  `Eerr` int NOT NULL,
  `Pins` int NOT NULL,
  `Pdel` int NOT NULL,
  `Prep` int NOT NULL,
  `Pswp` int NOT NULL,
  `Ptkrep` int NOT NULL,
  `dist`   int default 0,
  PRIMARY KEY  (`name`)
);

INSERT INTO `test`.datasets
VALUES ( "H1", 5000, 500, 90, 30, 24, 24, 24, 14, 14, 0);

INSERT INTO datasets
VALUES ( "H2", 5000, 500, 50, 30, 24, 24, 24, 14, 14, 0);

INSERT INTO datasets
VALUES ( "M1", 5000, 500, 30, 30, 24, 24, 24, 14, 14, 0);

INSERT INTO datasets
VALUES ( "M2", 5000, 500, 10, 30, 24, 24, 24, 14, 14, 0);

INSERT INTO datasets
VALUES ( "M3", 5000, 500, 90, 10, 24, 24, 24, 14, 14, 0);

INSERT INTO datasets
VALUES ( "M4", 5000, 500, 50, 10, 24, 24, 24, 14, 14, 0);

INSERT INTO datasets
VALUES ( "L1", 5000, 500, 30, 10, 24, 24, 24, 14, 14, 0);

INSERT INTO datasets
VALUES ( "L2", 5000, 500, 10, 10, 24, 24, 24, 14, 14, 0);