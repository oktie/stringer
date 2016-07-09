/*  keyboard.c : keyboard configuration routine.
 *
 *  Author:  Mauricio A. Hernandez
 *	     Department of Computer Science, Columbia University.
 *
 *  Copyright (c) 1993,1994,1995,1996 by Mauricio A. Hernandez
 *  All rights reserved.
 */
#include <stdio.h>
#include <ctype.h>

#ifdef DEBUG
#define DBG(x)	x
#endif
#ifndef DEBUG
#define DBG(x)  /* x */
#endif

#define KEYBOARD_COLS 10
#define KEYBOARD_ROWS 3

char keyboard[KEYBOARD_ROWS][KEYBOARD_COLS] = {
'q','w','e','r','t','y','u','i','o','p',
'a','s','d','f','g','h','j','k','l',';',
'z','x','c','v','b','n','m',',','.','/'
};

char rows[26][4], cols[26][4];

int inrow[26];
int incolumn[26];

void
key_substitutions()
{
	register int c, r, l;
	char k;

	for (c = 0;  c < KEYBOARD_COLS;  c++)
	    for (r = 0;  r < KEYBOARD_ROWS;  r++)
		if (isalpha(k = keyboard[r][c])) 
		    inrow[k-'a'] = r, incolumn[k-'a'] = c;

	for (c = 0;  c < KEYBOARD_COLS;  c++) {
	    for (r = 0;  r < KEYBOARD_ROWS;  r++) {
		l = 0;
		k = keyboard[r][c];
		if (!isalpha(k))
		    continue;
		if (c > 0)
		    if (isalpha(keyboard[r][c-1]))
			rows[k-'a'][l++] = keyboard[r][c-1];
		if (c < 9)
		    if (isalpha(keyboard[r][c+1]))
			rows[k-'a'][l++] = keyboard[r][c+1];
		rows[k-'a'][l] = '\0';
		if (l == 0) 
		    sprintf(rows[k-'a'], "%c", k);
DBG(fprintf(stderr, "rows[%c] ='%s'\n", k, rows[k-'a']));
	    }
	    for (r = 0;  r < KEYBOARD_ROWS;  r++) {
		l = 0;
		k = keyboard[r][c];
		if (!isalpha(k))
		    continue;
		if (r > 0)
		    if (isalpha(keyboard[r-1][c]))
			cols[k-'a'][l++] = keyboard[r-1][c];
		if (r < 2)
		    if (isalpha(keyboard[r+1][c]))
			cols[k-'a'][l++] = keyboard[r+1][c];
		cols[k-'a'][l] = '\0';
		if (l == 0) 
		    sprintf(cols[k-'a'], "%c", k);
DBG(fprintf(stderr, "cols[%c] ='%s'\n", k, cols[k-'a']));
	    }
	}
}
