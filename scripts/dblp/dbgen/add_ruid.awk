BEGIN {
	ruid = 0;
}
{
	if ($1 == $2) {
 	   printf("%d:%d:", ruid, ruid);
	   clstid[$1] = ruid;
	} else
	   printf("%d:%d:", clstid[$1], ruid);

	printf("%s:%s:%s:%s:%s:%s:%s:%s:%s:%s\n", $3, $4, $5, $6, $7, $8, $9, $10, $11, $12);
	ruid++;
}
