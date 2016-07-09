#!/usr/bin/gawk -f
#
# divide.awk : divide a file into horizontal partitions.  Each 
#	horizontal partition will include 'increments' records.
#	Remainder records are placed in the last partition.
#
# Usage:
#
# 	gawk -f divide.awk -v increments=25000 study.db
#
#	This will divide 'study.db' into horizontal partitions, each of
#	25,000 records. 
#
#	Each partition will be stored in a file named 'PartXX.db', where
#	XX represents the partition number.  The first partition will 
#	be in 'Part01.db'.
#
# Bugs:
#	User must remember to remove the Partition files before using
#	this program again over the same directory.
#
# Written June 1997 by Mauricio A. Hernandez
#

BEGIN {
	filePrefix = "Part";
	if (increments == 0) {
		increments = 200;
	}
	nRecs = 0;
	partNumber = 0;

	outputFile = "";
}
{
	if (nRecs % increments == 0) {
		if (length(outputFile) > 0) {
			close (outputFile);
		}

		partNumber++;
		outputFile = sprintf("%s%02d.db", filePrefix, partNumber);
		print > outputFile;
	} else {
		print >> outputFile;
	}

	nRecs++;

}
END {
	close (outputFile);
}
