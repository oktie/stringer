/*
 *  ui_dbgen.c:  Curses User Interface for the database generator program.
 *	   The actual database generation routines are in gen.c.  This
 *	   file only contains the UI related routines.
 *	   
 *	   The main() routine is in this file.
 *
 *  Author: Mauricio A. Hernandez (mauricio@cs.columbia.edu)
 *	    Computer Science Department
 *	    Columbia University &
 *	    the University of Illinois at Springfield
 *
 *  Copyright (c) 1996, 1997 by Mauricio A. Hernandez.  All rights reserved. 
 *
 */
static char RCSid[] = "\
$Id: ui_dbgen.c,v 2.0 1997/06/29 02:02:38 mauricio Exp mauricio $\
";

static char copyright[] = "\
Copyright (c) 1996, 1997 by Mauricio A. Hernandez.  All rights reserved.\
";

#include <curses.h>
#include <stdlib.h>
#include <ctype.h>
#include <fcntl.h>
#include "gen_dbgen.h"

#ifdef DEBUG
#define DBG(args) args
#endif
#ifndef DEBUG
#define DBG(args) /*nothing*/
#endif

#define TOTAL_ITEMS 50
#define QUIT	1
#define GENERATE 2
#define APPEND	3

#define MAXCCLS 2
static int ccl = 0;
static char CommandList[MAXCCLS][80] = {
"Commands: (q)uit (g)enerate (a)ppend (s)ave (r)estore (c)hange s(e)ed (o)ther",
"Commands: (u)niform (p)oission (z)ipf (>)next page  (<)prev page      (o)ther"};

static char *Instructions = CommandList[0];

typedef int boolean;

typedef struct {
	int x;
	int y;
} coord;

typedef struct {
	char message[100];
	coord mesg_pos;
	coord input_pos;
	boolean is_float;
	boolean is_char;
	char units;
	union {
	    float  *fval;
	    long   *lval;
	    char   *cval;
	} u;
	short page;
	short subpage;
} VALUES;

typedef struct {
	int	list[10];
} SAFETY;

typedef struct {
	char message[50];
	coord mesg_pos;
	short page;
} TITLES;

/* Global variable definitions */
static int first_line;
static int lline;
GLOBAL global_info;
SSN ssns;
NAMES names;
ADDRESSES addresses;
TYPO_TYPES typos;
RESULTS results;
TITLES1 titles1;

int distribution;
extern double theta;

VALUES params[TOTAL_ITEMS];
SAFETY safe[TOTAL_ITEMS];
TITLES titles[10];
static int parameters = 0;
static int num_of_titles = 0;
static int safe_checks = 0;
static char namesfile[100];
static char titlesfile[100];
static char outfile[100];
static char paramsfile[100];
static short n_page = 1;
static short c_page  = 1;
static short subpage = 1;
char *generator_dir = NULL;

boolean userinterface = FALSE;
int	seed = 0;


boolean
confirm(text)
char *text;
{
	WINDOW *cwin;
	int l = strlen(text);
	char ch;

	/*touchwin(stdscr);*/

	cwin = newwin(4, l+4, LINES/2 - 2, COLS/2 - l/2 - 2);
	box(cwin, '|', '-');
	wmove (cwin, 1, 2);
	wprintw(cwin, "%s", text);
	wmove (cwin, 2, 2);
	wprintw(cwin, "Please confirm (y/n) : ");
	wrefresh(cwin);
	ch = tolower(getch());
	while (ch != 'y' && ch != 'n')
	    ch = tolower(getch());
	waddch(cwin, ch);
	wrefresh(cwin);

	delwin(cwin);
	touchwin(stdscr);
	refresh();

	return ((ch == 'y')? TRUE: FALSE);
}

int
input_number(text, deflt)
char *text;
int  deflt;
{
	WINDOW *cwin;
	int l = strlen(text);
	char ch, buf[20];
	void w_read_string();

	/* This line was moved to the end of the function to properly
         * clean the screen after the window is gone. - Mauricio 6/28/97 */
	/*touchwin(stdscr);*/

	cwin = newwin(4, l+4, LINES/2 - 2, COLS/2 - l/2 - 2);
	box(cwin, '|', '-');
	wmove (cwin, 1, 2);
	wprintw(cwin, "%s", text);
	wmove (cwin, 2, 2);
	wprintw(cwin, "New Value: %d", deflt);
	wmove (cwin, 2, 13);
	wrefresh(cwin);
	w_read_string(cwin, buf, 10, 0);
	delwin(cwin);

	touchwin(stdscr);
	refresh();
	if (atoi(buf) == 0)
	   return deflt;
	else
	   return(atoi(buf));
}

void
printerror(text)
char *text;
{
	WINDOW *win;
	int l = strlen(text);

	touchwin(stdscr);
	win = newwin(4, l+4, LINES/2-2, COLS/2-l/2-2);
	box(win, '|', '-');
	wmove (win, 1, 2);
	wprintw(win, "%s", text);
	wmove (win, 2, 2);
	wprintw(win, "Press <Return> to continue.");
	wrefresh(win);
	getch();
	refresh();

}

float
tofloat(s)
char *s;
{
	register int i, l = strlen(s);
	register float r = 0;
	float p = 10;

	for (i = 0;  i < l && s[i] != '.';  i++) {
	    if (isdigit(s[i]))
		r = r*10 + s[i]-'0';
	    else 
		return(0);
	}

	for (i = i+1; i < l; i++) {
	    if (isdigit(s[i]))
		r = r + (s[i]-'0')/p;
	    else 
		return(0);
	    p *= 10;
	}

	return(r);
}

void
record_safety(i1, i2, i3, i4)
int i1, i2, i3, i4;
{
	safe[safe_checks].list[0] = i1;	
	safe[safe_checks].list[1] = i2;	
	safe[safe_checks].list[2] = i3;	
	safe[safe_checks].list[3] = i4;	
	safe[safe_checks].list[4] = -1;	

	safe_checks++;
}

boolean
load_parameters()
{
	int fd;
	char text[100], dummy[255];
	char filename[255];

	if (userinterface) {
	    sprintf(text, "Loading parameters from %s.", paramsfile);
	    if (!confirm(text))
		return(FALSE);
	}
	
	if (generator_dir)
	    sprintf(filename, "%s/dbgen_params/%s", generator_dir, paramsfile);
	else
	    sprintf(filename, "dbgen_params/%s", paramsfile);

	if ((fd = open(filename, O_RDONLY)) < 0) {
	    sprintf(text, "Cannot open %s for reading.", paramsfile);
	    if (userinterface)
		printerror(text);
	    else
		fprintf(stderr, "%s\n", text);
	    return(FALSE);
	}

	read(fd, (char *) &global_info, sizeof(GLOBAL));
	read(fd, (char *) &ssns, sizeof(SSN));
	read(fd, (char *) &names, sizeof(NAMES));
	read(fd, (char *) &titles1, sizeof(TITLES1));
	read(fd, (char *) &addresses, sizeof(ADDRESSES));
	read(fd, (char *) &typos, sizeof(TYPO_TYPES));
	read(fd, outfile, 100);
        /* This used to be the namesfile, but it is no longer considered
         * a loadable parameter.
         */
        /*read(fd, namesfile, 100);*/
        read(fd, dummy, 100);
	read(fd, paramsfile, 100);

	read(fd, &distribution, sizeof(distribution));
	read(fd, &theta, sizeof(theta));
	read(fd, &seed, sizeof(seed));

	close(fd);
	return(TRUE);
}

int
save_parameters()
{
	int fd;
	char text[100];
	char filename[255];

	if (userinterface) {
	    sprintf(text, "Saving parameters to %s.", paramsfile);
	    if (!confirm(text))
		return(-1);
	}

	if (generator_dir)
	    sprintf(filename, "%s/dbgen_params/%s", generator_dir, paramsfile);
	else
	    sprintf(filename, "dbgen_params/%s", paramsfile);

	if ((fd = open(filename, O_TRUNC | O_CREAT | O_WRONLY, 0644)) < 0) {
	    sprintf(text, "Cannot open %s for writting.", paramsfile);
	    if (userinterface) 
		printerror(text);
	    else
	        fprintf(stderr, "%s\n", text);
	    return(-1);
	}

	write(fd, (char *) &global_info, sizeof(GLOBAL));
	write(fd, (char *) &ssns, sizeof(SSN));
	write(fd, (char *) &names, sizeof(NAMES));
	write(fd, (char *) &titles1, sizeof(TITLES1));
	write(fd, (char *) &addresses, sizeof(ADDRESSES));
	write(fd, (char *) &typos, sizeof(TYPO_TYPES));
	write(fd, outfile, 100);
	write(fd, namesfile, 100);
	write(fd, paramsfile, 100);
	write(fd, (char *) &distribution, sizeof(distribution));
	write(fd, (char *) &theta, sizeof(theta));
	write(fd, (char *) &seed, sizeof(seed));

	close(fd);
	return(1);
}

void
load_default_values()
{
	global_info.num_of_records = 100;
	global_info.num_of_clusters = 50;
	global_info.num_of_duplicates = 0.0;
	global_info.max_dup_per_record = 4;
	distribution = UNIFORM;

	ssns.max_digits = 9;
	ssns.gen_prob = 70.0;
	ssns.errorp = 25.0;

	names.errorp = 25.0;
	names.single_error.prob = 80.0;
	names.single_error.insertion	= 40.0;
	names.single_error.deletion	= 20.0;
	names.single_error.replacement	= 20.0;
	names.single_error.swapping	= 20.0;
	names.swap_names	=  5.0;
	names.no_middle_name	= 20.0;
	names.fname_for_init	=  5.0;
	names.change_lname	=  5.0;

	titles1.errorp = 50.0;
	titles1.single_error.prob = 40.0;
	titles1.single_error.insertion	= 40.0;
	titles1.single_error.deletion	= 20.0;
	titles1.single_error.replacement = 20.0;
	titles1.single_error.swapping	= 20.0;
	titles1.charswap	=  5.0;
	titles1.tokenswap	= 20.0;
	
	addresses.use_PO_box = 10.0;
	addresses.changep = 10.0;
	addresses.street_number.errorp = 25.0;
	addresses.street_number.max_digits = 4;

	addresses.street_name.errorp = 25.0;
	addresses.street_name.single_error.prob = 80.0;
	addresses.street_name.single_error.insertion   = 40.0;
	addresses.street_name.single_error.deletion    = 20.0;
	addresses.street_name.single_error.replacement = 20.0;
	addresses.street_name.single_error.swapping    = 20.0;

	addresses.street_name.suffix.none   = 10.0;
	addresses.street_name.suffix.street = 25.0;
	addresses.street_name.suffix.road   = 20.0;
	addresses.street_name.suffix.avenue = 25.0;
	addresses.street_name.suffix.lane   = 10.0;
	addresses.street_name.suffix.pkwy   = 10.0;

	addresses.state_city_and_zip.errorp	     = 25.0;
	addresses.state_city_and_zip.single_err_prob = 80.0;
	addresses.state_city_and_zip.change_zip_code = 10.0;
	addresses.state_city_and_zip.change_state    = 10.0;
	addresses.state_city_and_zip.num_of_states   = 50;

	typos.same_row = 40.0;
	typos.same_column = 30.0;
	typos.homologous = 3.0;
	typos.first_letter = 8.0;
	typos.consonants = 80.0;
	strcpy(typos.consonant_freq, "rstnlchdpgmfbywvzxqkj");

	strcpy(outfile, "output.db");
	sprintf(namesfile, "%s/mnames.gz", generator_dir);
	sprintf(titlesfile, "%s/mtitles.gz", generator_dir);
	strcpy(paramsfile, "params.gen");
}

void
set_params(n, mesg, line, ind, units, is_char_field)
int n;
char *mesg;
int *line;
int ind;
char units;
boolean is_char_field;
{
	strcpy(params[n].message, mesg);
	if (*line >= lline-1) {
	    *line = first_line;
	    if (subpage < 0)
		n_page++;
	    subpage = -subpage;
	}

	params[n].page = n_page;
	params[n].subpage = subpage;
	params[n].mesg_pos.y = *line;
	params[n].mesg_pos.x = ind*2 + ((subpage >0)? 2 : 42);
	params[n].input_pos.y = *line;
	if (is_char_field)
	    if (strlen(mesg) < 20)
		params[n].input_pos.x = (subpage > 0)? 20: 55;
	    else
		params[n].input_pos.x = strlen(mesg)+1;
	else
	    params[n].input_pos.x = (subpage > 0)? 30: 70;
	params[n].units = units;
	(*line)++;
}

void 
set_title(mesg, line, ind)
char *mesg;
int *line;
int ind;
{
	if (*line >= lline-1) {
	    *line = first_line;
	    if (subpage < 0)
		n_page++;
	    subpage = -subpage;
	}

	strcpy(titles[num_of_titles].message, mesg);
	titles[num_of_titles].mesg_pos.x = ind*2 + ((subpage > 0)? 1: 41);
	titles[num_of_titles].mesg_pos.y = *line;
	titles[num_of_titles].page = n_page;
	num_of_titles++;
	(*line)++;
}

int
set_params_array()
{
	register int c = 0;
	int cline;

	cline = first_line;

	set_title("Database Size", &cline, 0);
	set_params(c, "Number of Records", &cline, 1, 0, FALSE);
	params[c].is_float = FALSE;
	params[c].is_char = FALSE;
	params[c++].u.lval = &global_info.num_of_records;

	set_params(c, "Number of Clusters", &cline, 1, 0, FALSE);
	params[c].is_float = FALSE;
	params[c].is_char = FALSE;
	params[c++].u.lval = &global_info.num_of_clusters;

/*
        if (distribution != POISSON)
	  set_params(c, "Max. Duplicates/Record", &cline, 1, 0, FALSE);
        else
	  set_params(c, "Mean Duplicates/Record", &cline, 1, 0, FALSE);
	params[c].is_float = FALSE;
	params[c].is_char = FALSE;
	params[c++].u.lval = &global_info.max_dup_per_record;
*/


	/* SSN stuff */
	cline++;
	cline++;
	set_title("Social Security Numbers", &cline, 0);

	set_params(c, "Geneate SSN", &cline, 1, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &ssns.gen_prob;

	set_params(c, "Error Probability", &cline, 1, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &ssns.errorp;


	/* Names stuff */
	cline++;
	cline++;
	set_title("Person Names", &cline, 0);
	set_params(c, "Typo. Error Prob.", &cline, 1, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &names.errorp;
	set_params(c, "Single Error Prob.", &cline, 2, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &names.single_error.prob;
	set_params(c, "Insertions", &cline, 3, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &names.single_error.insertion;
	set_params(c, "Deletions", &cline, 3, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &names.single_error.deletion;
	set_params(c, "Replacements", &cline, 3, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &names.single_error.replacement;
	set_params(c, "Swappings", &cline, 3, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &names.single_error.swapping;
	record_safety(c-4, c-3, c-2, c-1);	

  /* TODO */


	/* Titles stuff */
	cline++;
	cline++;
	set_title("TITLES", &cline, 0);
	set_params(c, "Prob. of a record to be erroneous", &cline, 1, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &titles1.errorp;
	set_params(c, "Extent of error in a record", &cline, 1, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &titles1.single_error.prob;
	set_params(c, "Insertions", &cline, 3, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &titles1.single_error.insertion;
	set_params(c, "Deletions", &cline, 3, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &titles1.single_error.deletion;
	set_params(c, "Replacements", &cline, 3, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &titles1.single_error.replacement;
	set_params(c, "Swappings Character", &cline, 3, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &titles1.single_error.swapping;
	set_params(c, "Swappings Tokens", &cline, 3, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &titles1.tokenswap;
	//record_safety(c-4, c-3, c-2, c-1);	


	/* Addresses Stuff */
	cline++;
	set_title("Addresses", &cline, 0);
	set_params(c, "Use P.O. Box", &cline, 1, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &addresses.use_PO_box;
	set_params(c, "Change Address", &cline, 1, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &addresses.changep;

	set_title("Street Number", &cline, 1);
/*	set_params(c, "Maximum digits", &cline, 2, 0, FALSE);
	params[c].is_float = FALSE;
	params[c].is_char = FALSE;
	params[c++].u.lval = &addresses.street_number.max_digits;
*/
	set_params(c, "Typo. Error Prob.", &cline, 2, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &addresses.street_number.errorp;

	set_title("Street Names", &cline, 1);
	set_params(c, "Typo. Error Prob.", &cline, 2, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &addresses.street_name.errorp;
	set_params(c, "Single Error Prob.", &cline, 3, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &addresses.street_name.single_error.prob;
	set_params(c, "Insertions", &cline, 4, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &addresses.street_name.single_error.insertion;
	set_params(c, "Deletions", &cline, 4, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &addresses.street_name.single_error.deletion;
	set_params(c, "Replacements", &cline, 4, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &addresses.street_name.single_error.replacement;
	set_params(c, "Swappings", &cline, 4, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &addresses.street_name.single_error.swapping;
	record_safety(c-4, c-3, c-2, c-1);
/*
	set_title("Prefixes", &cline, 2);
	set_params(c, "None", &cline, 3, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &addresses.street_name.suffix.none;
	set_params(c, "Street", &cline, 3, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &addresses.street_name.suffix.street;
	set_params(c, "Road", &cline, 3, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &addresses.street_name.suffix.road;
	set_params(c, "Avenue", &cline, 3, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &addresses.street_name.suffix.avenue;
	set_params(c, "Lane", &cline, 3, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &addresses.street_name.suffix.lane;
	set_params(c, "Parkway", &cline, 3, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &addresses.street_name.suffix.pkwy;
*/
	cline++;
	cline++;
	set_title("City, State, ZIP", &cline, 1);
	set_params(c, "Typo. Error Prob.", &cline, 2, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &addresses.state_city_and_zip.errorp;
	set_params(c, "Single Error Prob.", &cline, 3, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &addresses.state_city_and_zip.single_err_prob;
	set_params(c, "Change Zip Code", &cline, 2, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &addresses.state_city_and_zip.change_zip_code;
	set_params(c, "Change State", &cline, 2, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &addresses.state_city_and_zip.change_state;
	set_params(c, "Number of States", &cline, 2, 0, FALSE);
	params[c].is_float = FALSE;
	params[c].is_char = FALSE;
	params[c++].u.lval = &addresses.state_city_and_zip.num_of_states;

	cline++;
	set_title("Types of Typos", &cline, 0);
	set_params(c, "Same row in keyboard", &cline, 1, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &typos.same_row;
	set_params(c, "Same column in keyboard", &cline, 1, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &typos.same_column;
	set_params(c, "Homologous", &cline, 1, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &typos.homologous;
	set_params(c, "Change First Letter", &cline, 1, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &typos.first_letter;
	set_params(c, "Consonant Change", &cline, 1, '%', FALSE);
	params[c].is_float = TRUE;
	params[c].is_char = FALSE;
	params[c++].u.fval = &typos.consonants;
	set_params(c, "Cons. Order", &cline, 1, 0, TRUE);
	params[c].is_float = FALSE;
	params[c].is_char = TRUE;
	params[c++].u.cval = typos.consonant_freq;

	cline++;
	set_title("Files", &cline, 0);
	set_params(c, "Output File", &cline, 0, 0, TRUE);
	params[c].is_float = FALSE;
	params[c].is_char = TRUE;
	params[c++].u.cval = outfile;
	set_params(c, "Names DB", &cline, 0, 0, TRUE);
	params[c].is_float = FALSE;
	params[c].is_char = TRUE;
	params[c++].u.cval = namesfile;
	set_params(c, "Parameter File", &cline, 0, 0, TRUE);
	params[c].is_float = FALSE;
	params[c].is_char = TRUE;
	params[c++].u.cval = paramsfile;

	return (c);
}

int
reset_params_array()
{
	first_line = 3;
	lline = LINES-1;
	parameters = 0;
	num_of_titles = 0;
	safe_checks = 0;
	n_page = 1;
	c_page  = 1;
	subpage = 1;

	parameters = set_params_array();

	return(parameters);
}

void
show_titles()
{
	register int t;

	for (t = 0;  t < num_of_titles;  t++) {
	    if (titles[t].page != c_page)
		continue;
	    move(titles[t].mesg_pos.y, titles[t].mesg_pos.x);
#ifdef __hp9000s800
	    attron(A_UNDERLINE);
#else
	    standout();	
#endif
	    printw("%s",titles[t].message);
#ifdef __hp9000s800
	    attroff(A_UNDERLINE);
#else
	    standend();
#endif
	};
	standend();
}

void
show_parameter_titles()
{
	register int p;

	for (p = 0; p < parameters; p++) {
	    if (params[p].page != c_page)
		continue;
	    move(params[p].mesg_pos.y, params[p].mesg_pos.x);
	    printw("%s", params[p].message);
	}
}

void
show_parameter_values()
{
	register int p;

	for (p = 0; p < parameters; p++) {
	    if (params[p].page != c_page)
		continue;
	    move(params[p].input_pos.y, params[p].input_pos.x);
	    if (params[p].is_float) 
	        printw("%7.2f%c", *params[p].u.fval, params[p].units);
	    else
		if (params[p].is_char)
		    printw("%-45s", params[p].u.cval);
		else
		    printw("%7d", *params[p].u.lval);
	}
}

show_message(message)
{
	move(lline,0);
	clrtoeol();
	printw("%s", message);
	move(lline,COLS-2);
}

void
center(line, mesg)
int line;
char *mesg;
{
	move(line, COLS/2-strlen(mesg)/2);
	printw("%s", mesg);
}

show_page_marks()
{
	char page_mesg[20];

	sprintf(page_mesg, "Page %d of %d", c_page, n_page);
	center(lline-1, page_mesg);
	if (c_page < n_page) {
	    move(lline-1, COLS-5);
	    printw("More>");
	}
	if (c_page > 1) {
	    move(lline-1, 0);
	    printw("<More");
	}
}

void
show_results(filename)
char *filename;
{
	WINDOW *cwin;

	if (strlen(filename) > 20)
	    filename = &filename[strlen(filename) - 20];

	/*refresh();
	touchwin(stdscr);*/

	cwin = newwin(7, 50, LINES/2 - 2, COLS/2 - 25);
	box(cwin, '|', '-');
	wmove (cwin, 1, 2);
	wprintw(cwin, "Random DB generated to %s.", filename);
	wmove (cwin, 2, 3);
	wprintw(cwin, "%d records generated.", results.trecs);
	/*	wmove (cwin, 3, 4);
	wprintw(cwin, "%d original records, of which", results.orecs);
	*/
	wmove (cwin, 4, 3);
	/*
	wprintw(cwin, "%d where selected for duplication, creating", results.drecs);
	wmove (cwin, 5, 4);
	wprintw(cwin, "%d duplicate records.", results.tdrecs);
	wmove (cwin, 6, 3);
	*/
	wprintw(cwin, "Random seed : %d", results.seed);
	wmove(cwin, 5, 8);
	wprintw(cwin, "Press any <Return> to continue...");
	wrefresh(cwin);
	/*refresh();*/
	noecho();
	getch();
	/*wrefresh(cwin);*/
	delwin(cwin);

	/* This line was added to properly refresh */
	touchwin(stdscr);
	refresh();
}

void
highlight(p)
register int p;
{
	move(params[p].input_pos.y, params[p].input_pos.x-1);
	standout();
	if (params[p].is_float) 
	    printw(">%7.2f", *params[p].u.fval);
	else
	    if (params[p].is_char)
		printw(">%-45s", params[p].u.cval);
	    else
	        printw(">%7d", *params[p].u.lval);
	move(params[p].input_pos.y, params[p].input_pos.x-1);
	standend();
	refresh();
}

void
nohighlight(p)
register int p;
{
	move(params[p].input_pos.y, params[p].input_pos.x-1);
	if (params[p].is_float) 
	    printw(" %7.2f", *params[p].u.fval);
	else
	    if (params[p].is_char)
		printw(" %-45s", params[p].u.cval);
	    else
	        printw(" %7d", *params[p].u.lval);
	move(params[p].input_pos.y, params[p].input_pos.x-1);
	refresh();
}
 
void
erase_param(p)
register int p;
{
	move(params[p].input_pos.y, params[p].input_pos.x-1);
	if (params[p].is_char)
	    printw("                                             ");
	else
  	    printw("        ");
	move(params[p].input_pos.y, params[p].input_pos.x-1);
	refresh();
}

void
read_string(buf, s, ach)
char *buf;
int s;
char ach;
{
	char ch = 0;
	int  p = 0;
	int  x,y;

	getyx(stdscr, y, x);
	noecho();
#ifdef __hp9000s800
	nl();
#endif

	while (TRUE) {
	    if (ach > 0) {
	    	ch = ach;
		ach = 0;
	    } else
	    	ch = getch();

	    if (ch == '\n') {
		break;
	    }

	    if (ch == erasechar()) {
		if (p>0) {
		    buf[p--] = 0;
		    move(y,--x);
		    printw("  ");
		    move(y,x);
		    refresh();
		}
		continue;
	    }

            switch (ch) {
              case 9:
	      case 27:
		break;
              default:
		if (p<s) {
		    buf[p++] = ch;
		    printw("%c",ch);
		    refresh();
		    x++;
		    buf[p] = 0;
		}
            }
        }
        buf[p] = 0;
        move(y+1, 1);
}

void
w_read_string(w, buf, s, ach)
WINDOW *w;
char *buf;
int s;
char ach;
{
	char ch = 0;
	int  p = 0;
	int  x,y;

	getyx(w, y, x);
	noecho();
	nl();

	while (TRUE) {
	    if (ach > 0) {
	    	ch = ach;
		ach = 0;
	    } else
	    	ch = getch();

	    if (ch == '\n') {
		break;
	    }

	    if (ch == erasechar()) {
		if (p>0) {
		    buf[p--] = 0;
		    wmove(w, y, --x);
		    wprintw(w, "  ");
		    wmove(w, y, x);
		    wrefresh(w);
		}
		continue;
	    }

            switch (ch) {
              case 9:
	      case 27:
		break;
              default:
		if (p<s) {
		    buf[p++] = ch;
		    wprintw(w, "%c",ch);
		    wrefresh(w);
		    x++;
		    buf[p] = 0;
		}
            }
        }
        buf[p] = 0;
        wmove(w, y+1, 1);
}

static int cparm = 0;

int
edit_parameters()
{
	boolean STOP = FALSE;
	int retval = QUIT;
	char ch, nval[100];
	int i_theta, newcparm;
	register int j;

	cbreak();
	while (!STOP) {
	    highlight(cparm);
	    noecho();
	    ch = getch();
	    if (isdigit(ch) || ch=='.' || ch=='-' || ch=='/' || ch=='c') {
		standout();
		erase_param(cparm);
	        if (params[cparm].is_char)
		    read_string(nval, 45, 0);
		else
		    if (ch == '/')
			read_string(nval, 7, 0);
		    else
			read_string(nval, 7, ch);
		if (params[cparm].is_float)
		    *params[cparm].u.fval = tofloat(nval);
		else
		    if (params[cparm].is_char)
			strcpy(params[cparm].u.cval, nval);
		    else 
		        *params[cparm].u.lval = atoi(nval);
		standend();
		continue;
	    }

	    switch (ch) {
	      case 'j': 
		down:
		    nohighlight(cparm);
		    if (cparm == parameters - 1 || params[cparm+1].page != c_page) {
			for (j = 0;  j < parameters;  j++) 
			    if (params[j].page == c_page) {
				cparm = j;
				break;
			    }
		    } else
			cparm++;
		    break;
	      case 'k':
		up:
		    nohighlight(cparm);
		    if (cparm == 0 || params[cparm-1].page != c_page) {
			for (j = parameters-1; j >= 0;  j--)
			    if (params[j].page == c_page) {
				cparm = j;
				break;
			    }
		    } else
			cparm--;
		    break;
	      case 'l':
	      case 'h':
		right_left:
		    nohighlight(cparm);
		    newcparm = -1;
		    for (j = 0;  j < parameters;  j++) {
			if (params[j].subpage == params[cparm].subpage ||
			    params[j].page != params[cparm].page)
			    continue;
			if (params[j].input_pos.y >= params[cparm].input_pos.y) {
			    newcparm = j;
			    break;
			}
		    }
		    if (newcparm >= 0)
			cparm = newcparm;
		    break;
	      case 27:
		    getch();
		    ch = getch();
		    if (ch == 66) goto down;
		    if (ch == 65) goto up;
		    if (ch == 68 || ch == 67) goto right_left;
		    break;
	      case '>':
		    if (c_page < n_page) {
		        nohighlight(cparm);
			c_page++;
			for (j = 0;  j < parameters;  j++)
			    if (params[j].page == c_page) {
				cparm = j;
				break;
			    }
			redrawscreen();
		    }
		    break;
	      case '<':
		    if (c_page > 1) {
		        nohighlight(cparm);
			c_page--;
			for (j = 0;  j < parameters;  j++)
			    if (params[j].page == c_page) {
				cparm = j;
				break;
			    }
			redrawscreen();
		    }
		    break;
	      case 'q':
		    nohighlight(cparm);
		    return(QUIT);
		    break;
	      case 'g':
		    nohighlight(cparm);
		    return(GENERATE);
	      case 'a':
		    nohighlight(cparm);
		    return(APPEND);
	      case 's':
		    show_message("Saving parameters...");
		    refresh();
		    save_parameters();
		    show_message(Instructions);
		    refresh();
		    break;
	      case 'r':
		    show_message("Loading parameters...");
		    refresh();
		    load_parameters();
		    show_parameter_values();
		    show_message(Instructions);
		    refresh();
		    break;
	      case 'e':
		    show_message("Setting the Random seed...");
		    refresh();
		    seed = input_number("Please enter the Seed", seed);
		    show_parameter_values();
		    show_message(Instructions);
		    refresh();
		    break;
	      case 'u':
		    distribution = UNIFORM;
		    parameters = reset_params_array();
		    redrawscreen();
		    break;
	      case 'p':
		    distribution = POISSON;
		    parameters = reset_params_array();
		    redrawscreen();
		    break;
	      case 'z':
		    distribution = ZIPF;
		    i_theta = input_number("Please enter Theta(*100)", (int)(theta*100.0));
		    theta = (double) i_theta / 100.0;
		    redrawscreen();
/*fprintf(stderr, "i_theta = %d, theta = %f\n", i_theta, theta);*/
		    break;
	      case 'o':
		    Instructions = CommandList[(++ccl) % MAXCCLS];
		    redrawscreen();
		    break;
	    }
	}
	return(retval);
}

boolean
make_safety_checks()
{
	WINDOW *errwin;
	int j, i, s;
	float t;
	char err[100];

	for (s = 0;  s < safe_checks;  s++) {
	    t = 0;
	    i = 0;
	    while (safe[s].list[i] >= 0) 
		t += *params[safe[s].list[i++]].u.fval;
	    if (abs(t - 100.0) < 0.01)
		continue;
	    /* Sound the alarm */
	    touchwin(stdscr);
	    errwin = newwin(i+5, 40, LINES/2-(i+3)/2, COLS/2-20);
	    box(errwin, '|', '-');
	    wmove(errwin, 1, 2);
	    wprintw(errwin, "The values in the following fields");
	    wmove(errwin, 2, 2);
	    wprintw(errwin, "do not add to 100%%.  Please modify.");
	    for (j = 0;  j < i ;  j++) {
		wmove(errwin, 3+j, 7);
		wprintw(errwin, "%-17s%7.2f%%", params[safe[s].list[j]].message, *params[safe[s].list[j]].u.fval);
	    }	
	    wmove(errwin, 3+j, 4);
	    wprintw(errwin, "Press any key to continue...");
	    wrefresh(errwin);
	    noecho();
	    wgetch(errwin); 
	    delwin(errwin);
	    refresh();
	    return(FALSE);	
	}
	return(TRUE);
}

void
set_main_screen()
{
	register int j;

	clear();
#ifdef __hp9000s800
	attron(A_BOLD);
#endif
	center(0, "Random Database Generator - v2.0");
	center(1, "Computer Science - University of Illinois, Springfield");
#ifdef __hp9000s800
	attroff(A_BOLD);
#endif
	move(2,0);
	for (j = 0; j < COLS - 7;  j++)
	    printw(".");
	printw("%7s", (distribution == UNIFORM)? "Uniform": \
		(distribution == POISSON)? "Poisson": "...Zipf");
	move(lline-1, 0);
	for (j = 0; j < COLS;  j++)
	    printw(".");
}


redrawscreen()
{
	clear();
	set_main_screen();
	show_titles();
	show_parameter_titles();
	show_parameter_values();
	show_message(Instructions);
	show_page_marks();
	refresh();
}


/* Main Program of the User Interface */

main(argc, argv)
int argc;
char **argv;
{
	int c, errflg, first_uid = 0;
	char *optstring = "ac:fin:o:p:s:qz:I:U:e:u:v:w:x:d:r:h";
	boolean oflg, iflg, mflg, nflg, dflg, quiet_flag;
	boolean fixed_size, append_flag;
	char *pname = argv[0];
	char *inputfilename = NULL;
	extern char *optarg;
	extern int optind, opterr;

#if 0
for (c = 0;  c < argc;  c++)
  fprintf(stdout, "%d:%s ", c, argv[c]);
fprintf(stdout, "\n");
#endif

	generator_dir = (char *) getenv("GENERATOR_DIR");
	strcpy(outfile, "output.db");
	strcpy(paramsfile, "default.gen");
	if (!load_parameters());
	    load_default_values();

	opterr = errflg = 0;
	oflg = iflg = mflg = nflg = dflg = quiet_flag = \
	  append_flag = fixed_size = FALSE;
	while ((c = getopt(argc, argv, optstring)) != -1) {
	    switch (c) {
	    case 'h':
		printf("HELP MENU:\n");
		exit(0);	
	    case 'I': 
		inputfilename = optarg;
		break;
	    case 'U':
		first_uid = atoi(optarg);
		break;
	    case 'a':
	        append_flag = TRUE;
		break;
	    case 'q':
		quiet_flag = TRUE;
		break;
	    case 'c':
		dflg = TRUE;
		global_info.num_of_clusters = atol(optarg);
		break;
	    case 'f':
		fixed_size = TRUE;
		break;
	    case 'i':
		iflg = TRUE;
	   	userinterface = FALSE;
		break;
	    case 'o':
		oflg = TRUE;
		strcpy(outfile, optarg);
		break;
	    case 'n':
		nflg = TRUE;
		global_info.num_of_records = atol(optarg);
		break;
	    case 's':
		seed = atoi(optarg);
		break;
	    case 'e':
		titles1.errorp = atof(optarg);
		break;
	    case 'v':
		titles1.single_error.swapping = atof( optarg);
		break;
	    case 'w':
		titles1.tokenswap = atof(optarg);
		break;
	    case 'u':
		titles1.single_error.prob = atof(optarg);
		break;
	    case 'x':
		titles1.single_error.insertion = atof(optarg);
		break;
	    case 'd':
		titles1.single_error.deletion = atof(optarg);
		break;
	    case 'r':
		titles1.single_error.replacement = atof(optarg);
		break;
	    case 'z':
		theta = atof(optarg);
		distribution = ZIPF;
		break;
	    case 'p':
		if (strcmp(optarg, "oisson") == 0) {
		  distribution = POISSON;
		  break;
		}
		if (mflg || nflg || dflg || oflg) {
		    fprintf(stderr,"%s: Loading parameter file will overwrite definitions given by\n", pname);
		    fprintf(stderr,"options m, n, d, or o.  Use -p before any of these options.\n");
		    exit(-1);
		}
		strcpy(paramsfile, optarg);
		load_parameters();
		break;
	    case '?':
		fprintf(stderr, "Usage: %s [-q] [-s <randomseed>] [-p <parameters file>]\n", pname);
		fprintf(stderr, "\t\t[-n <number of records>] [-c <number of clusters>]\n");
		fprintf(stderr, "\t\t[-o <output filename> | -i]\n");
		exit(-1);
	    }	
	}
	
	if (!iflg) {
	    int comm; 

	    userinterface = TRUE;
	    initscr();
	    first_line = 3;
	    lline = LINES-1;
	    parameters = set_params_array();
	
	    redrawscreen();

	    while ((comm = edit_parameters()) != QUIT) {
		if (make_safety_checks()) {
		    clear();
	            refresh();
	            endwin();

#ifdef __hp9000s800
		    system("clear");
#endif
		    generate(seed, namesfile, titlesfile,  outfile, inputfilename, first_uid, (comm == APPEND), quiet_flag, fixed_size);
		
		    initscr();
		    /*c_page = 1;
		    subpage = 1;*/
		    redrawscreen();
		    show_results(outfile);
		}
	    }

	    clear();
	    refresh();
	    endwin();
#ifdef __hp9000s800
	    system("tput reset");
#endif
	} else {
	  generate(seed, namesfile, titlesfile, outfile, inputfilename, first_uid, append_flag, quiet_flag, fixed_size);
	}

	exit(0);
}


