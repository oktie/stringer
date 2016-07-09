#!/bin/sh
# 

#sort -n -t: +1 -2 $1 | cut -d: -f1,3- | awk -f add_ruid.awk
#sort -n -t: +1 -2 -T /hdb/tmp $1 | awk -F : -f add_ruid.awk
sort -n -t: +1 -2 -T $MPTMPDIR $1 | set_ruid
