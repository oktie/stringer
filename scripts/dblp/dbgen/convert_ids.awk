BEGIN {
	pid = cid = -1;
	count = 0;
	
}
{
	if ($1 != pid) {
		cid = count;
		pid = $1;
	}
	printf ("%d\n", cid);
	count++;
}
