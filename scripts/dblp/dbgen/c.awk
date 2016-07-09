BEGIN {
	for (i = 0;  i < 100;  i++) {
		count[i] = 0;
	}
	prev = 0;
	c = 0;
}
{
	if (prev == $1) {
		c++;
	} else {
		count[c]++;
		c = 1;
		prev = $1;
	}
}	
END {
	count[c]++;
	for (i = 0;  i < 100;  i++) {
		printf ("%d\t%d\n", i, count[i]);
	}
}
