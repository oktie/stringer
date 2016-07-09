#include <stdio.h>

main()
{
    unsigned long *clusterID;
    unsigned long nClusters = 1000;
    unsigned long ruid = 0;
    unsigned long clusterIdVal, recordIdVal;
    char line[255];
    register char *ptr, *ptr2;

    clusterID = (unsigned long *) malloc (nClusters * sizeof(unsigned long));

    while (gets(line)) {
	ptr = line;
	while (*ptr && *ptr != ':') ptr++;
	*ptr = '\0';
	clusterIdVal = atol(line);
	ptr2 = ++ptr;
	while (*ptr2 && *ptr2 != ':') ptr2++;
	*ptr2 = '\0';
	recordIdVal = atol(ptr);
	ptr2++;

	if (clusterIdVal == recordIdVal) {
	    printf("%d:%d:", ruid, ruid);
	    if (clusterIdVal > nClusters - 1) {
	    	nClusters = clusterIdVal + 1;
	    	clusterID = (unsigned long *) realloc (clusterID, nClusters * sizeof(unsigned long));
	    }
	    clusterID[clusterIdVal] = ruid;
	} else 
	    printf("%d:%d:", clusterID[clusterIdVal], ruid);
	
	printf("%s\n", ptr2);

	ruid++;
    }
}


	
