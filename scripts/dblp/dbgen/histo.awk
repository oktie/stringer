BEGIN {
	total = 0;
	for (i = 0;  i < 20;  i++) {
	    h[i] = 0;
	}
}
{
	h[$1]++;
	total++;
}
END {
	for (i = 0;  i < 20;  i++) {
	    printf("%d\t%d\t%f\n", i, h[i], h[i]/total);
	}
}
