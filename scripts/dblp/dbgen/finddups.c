#include <stdio.h>

typedef int boolean;

#define TRUE 1
#define FALSE 0

void
main(argc, argv)
int argc;
char **argv;
{
	boolean print_pid = FALSE;
	long pid, cid;
	char p[200], c[200];
	char t[200];
	
	pid = -1;
	cid = 0;
	while (gets(t)) {
	    strcpy(c, t);
	    cid = atol(strtok(c, ":"));
	    if (cid != pid) {
	        strcpy(p, t);
		pid = cid;
		print_pid = TRUE;
	        continue;
	    }
	    if (print_pid) {
		printf("%s\n", p);
		print_pid = FALSE;
	    }
	    printf("%s\n", t);
	}
}
