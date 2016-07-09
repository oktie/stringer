#include <stdio.h>

typedef int boolean;

#define TRUE 1
#define FALSE 0

main()
{
	long pid, cid, count;
	char t[200];
	
	pid = -1;
	cid = 0;
	count = 0;
	while (gets(t)) {
	    cid = atol(strtok(t, ":"));
	    if (cid != pid) {
		pid = cid;
	        continue;
	    }
	    count++;
	}

	printf("%ld\n", count);
}
