/*
 *  zipfgen_dbgen.c: Random database generator program.  File ui_dbgen.c
 *         contians the user interface routines.
 *
 *  Author: Mauricio A. Hernandez
 *	    Computer Science Department
 *	    Columbia University
 *	    University of Illinois at Springfield
 *
 *  Copyright (c) 1996, 1997 by Mauricio A. Hernandez
 *  All rights reserved.
 */
static char RCSid[] = "\
$Id: zipfgen_dbgen.c,v 2.0 1997/06/29 02:02:38 mauricio Exp mauricio $\
";

static char copyright[] = "\
Copyright (c) 1996, 1997 by Mauricio A. Hernandez.  All rights reserved.\
";

#include <curses.h>
#include <math.h>
#include <string.h>
#include <ctype.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/time.h>
#include "gen_dbgen.h"
#include "keyboard.h"

#ifdef DEBUG
#define DBG(args) args
#endif
#ifndef DEBUG
#define DBG(args) /*nothing*/
#endif

typedef int boolean;
long myrandom();

#define   print_delim()   (fprintf(out, ":"))
#define   print_eol()	  (fprintf(out, "\n"))
#define   gendigit()	  (myrandom(9) + '0')
#define	  genletter()	  (myrandom(26) + 'a')

typedef struct {
	long	allocated;
	long	size;
	char 	**item;
} list_of_names;

list_of_names  NameList;

typedef struct {
	long spos;
	long fpos;
	long order;
} INDEX;

char *zips[43000];
INDEX zindex[100];
static long nzips;
static long nzindex;


typedef struct {
	char ssn[10];
	char fname[30];
	char lname[30];
	char minit[10];
	char street_number[10];
	char street_name[30];
	char apartment[10];
	char suffix[10];
	char city[30];
	char state[10];
	char zipcode[10];
} Person;
Person Record, Duplicate;

long trecs;

static boolean FirstTime = TRUE;

/* Output stream */
FILE *out;

/* Quiet Flag */
boolean be_quiet = FALSE;

char *
freadline(int ifd, char *line)
{
	static char buffer[513];
	static char *bptr;
	static int  nbytes;
	static int  done;
	register char *p = line;

	while (!done) {
	    if (nbytes == 0) {
	        nbytes = read(ifd, buffer, sizeof(buffer) - 1);
	        if (nbytes < 0) {
	    	    fprintf(stderr, "Error reading input fd.\n");
		    exit(-1);
	        }

		if (nbytes < sizeof(buffer) - 1) {
		    done = 1;
	        }

	        buffer[nbytes] = 0;
	        bptr = buffer;
	    }

	    while (*bptr) {
	        nbytes--;
	        if (*bptr == '\n') {
		    bptr++;
		    *p = '\0';
		    return line;
	        }
	        *p++ = *bptr++;
	    }
	}
	
	*buffer = '\0';
	bptr = NULL;
	nbytes = 0;
	done = 0;
	return NULL;
}
	    
	    
/*
 * read_names(filename) :  Read a list of names from file 'filename'.
 *	The names are stored in structure NameList and global variable
 * 	The total number of names read is returned (-1 indicates an error).
 */
int
read_names(filename)
char *filename;
{
	FILE  *fp, *fopen();
	char  buf[100];
	register int i;
	int pid, fds[2];

	pipe(fds);

	if ((pid = fork()) == 0) {
		/* The child will uncompress zipcodes.gz and pass the
		 * result to the parent.
		 */
		close (1);	/* Close the stdout */
		dup(fds[1]);	/* Dup the output side of the pipe */
		close(fds[0]);
		close(fds[1]);

fprintf(stderr, "Child is going to gunzip\n");
		if (execlp ("gunzip", "gunzip", "-c", filename, (char *) 0) < 0) {
			fprintf(stderr, "Can't execute gzip.\n");
			exit(-1);
		}
	}
	if (pid < 0) {
		fprintf(stderr, "Can't fork a child process!\n");
		exit(-1);
	}

	/* This is the parent process.  We must read from the input
	 * side of the pipe */
	if (!be_quiet) fprintf(stderr, "Reading names from database.\n");
	NameList.size = 0;
	NameList.allocated = 1000;

	NameList.item = (char **) malloc (NameList.allocated * sizeof(char *));

	i = 0;
	while (freadline(fds[0], buf)) {
	    if (i == NameList.allocated) {
		NameList.allocated *= 2;
		NameList.item = (char **) realloc (NameList.item, NameList.allocated * sizeof(char *));
	    }

	    NameList.item[i++] = (char *) strdup(buf);
	    if (!be_quiet && (i % 1000) == 0) {
		fprintf(stderr, "*");
		fflush(stderr);
	    }
	}
	if (!be_quiet) fprintf(stderr, "\n");

	NameList.size = i;
	return (NameList.size);
}

int
compare_order(a, b)
INDEX *a;
INDEX *b;
{
	return ((a->order > b->order)? TRUE: FALSE);
}

/*
 * read_zipcodes() :  Read the zipcode information in file 'zipcodes'.
 *	The zipcode information is ordered, alphabetically, by states
 *	in the file 'zipcodes'.  This routine also creates an index by
 *	state of the zipcodes in zindex. 
 *
 *		nzips = Total number of zipcodes read.
 *		nzindex = Total number of states in the zipcodes.
 */
int
read_zipcodes()
{
	FILE *zfile, *fopen();
	char cs[20], buf[100], zipfiles[100];
	extern char *generator_dir;
	int pid, fds[2];

	nzips = nzindex = 0;

	pipe(fds);

	if ((pid = fork()) == 0) {
		/* The child will uncompress zipcodes.gz and pass the
		 * result to the parent.
		 */
		close (1);	/* Close the stdout */
		dup(fds[1]);	/* Dup the output side of the pipe */
		close(fds[0]);
		close(fds[1]);

		sprintf(zipfiles, "%s/zipcodes.gz", generator_dir);

fprintf(stderr, "Child is going to gunzip\n");
		if (execlp ("gunzip", "gunzip", "-c", zipfiles, (char *) 0) < 0) {
			fprintf(stderr, "Can't execute gzip.\n");
			exit(-1);
		}
	}
	if (pid < 0) {
		fprintf(stderr, "Can't fork a child process!\n");
		exit(-1);
	}

	/* This is the parent process.  We must read from the input
	 * side of the pipe */
	if (!be_quiet) fprintf(stderr,"Reading zipcodes information:\n");

	strcpy(cs, "");
	while (freadline(fds[0], buf)) {
/*fprintf(stderr, "%s\n", buf);*/
	    if (strcmp(cs, &buf[strlen(buf)-2]) != 0) {
	        strcpy(cs, &buf[strlen(buf)-2]);
		if (!be_quiet) fprintf(stderr, "%s  ", cs);
		if (nzindex > 0)
		    zindex[nzindex-1].fpos = nzips-1;
		zindex[nzindex].spos = nzips;
		zindex[nzindex++].order = myrandom(100);
	    }
	    zips[nzips++] = (char *) strdup(buf);
	}

	close (fds[1]);
	close (fds[0]);

	zindex[nzindex-1].fpos = nzips-1;

	/* Sort the indexes to a random order */
	qsort((char *) zindex, nzindex, sizeof(INDEX), compare_order);

	if (!be_quiet) fprintf(stderr,"\nDone.  Now creating random database.  Please wait.\n");

	return(nzips);
}

/*
 * myrandom(limit) :  Generates a random number in the range [0, limit).
 */
long
myrandom(limit)
long limit;
{
	if (limit < 1) return (0); /* Take care of div by zero */
	if (limit < 2) return (0);
#ifndef __Linux
	return (rand() % limit);
#else
	return (random() % limit);
#endif
}

float
frandom(limit)
long limit;
{
	float r;
	long rdm;

	if (limit < 2) return(0);
#ifndef __Linux
	rdm = rand() % (limit*100);
#else
	rdm = random() % (limit*100);
#endif
	r = (float) rdm/100.0;
	return (r);
}
	
/*
 * zipf distribution
 */
double zipf_c;
double theta;

#define round(x) ((int)(floor((x)+0.5)))
  
void
init_zipf(int D)
{
  int i;
  double denom;
  
  denom = 0.0;
  for (i = 1;  i <= D;  i++)
    denom += (1.0 / pow((double) i, (1.0 - theta)));

  zipf_c = 1.0/denom;
}

double
zipf(int j)
{
    return (zipf_c / pow((double) j, (1.0 - theta)) );
}

#if 0
int
zipf(int j, int n)
{
  double v, accm;
  int    r, i;

  v = zipf_c / pow ((double) j, (1.0 - theta));
  /*fprintf(stderr, "%f\t%f\t%d\n", v, n*v, round(n*v));*/
  return (round(n * v));
/*  return (n * v);*/

  r = myrandom(10000);
  v = (double)r/10000.0;

  accm = 0.0;
  for (i = 1;  i <= n;  i++) {
    accm += zipf_c / pow ((double) i, (1.0 - theta));
    if (v < accm)
      return (i - 1);
  }
  return (n - 1);
}
#endif


/*
 * poisson:  return an interger random number using a Poisson distribution.
 */
double Poisson[MAX_DUPLICATES];
double fact[MAX_DUPLICATES];

void
init_poisson(int mean)
{
  int i;
  double lambda, f;

  fact[0] = 1.0;
  for (i = 1;  i < MAX_DUPLICATES;  i++) {
    fact[i] = (double)i * fact[i-1];
    DBG(fprintf(stderr, "fact[%d]=%f\n", i, fact[i]));
  }

  lambda = (double) mean;
  for (i = 0;  i < MAX_DUPLICATES;  i++) {
    f = exp(-lambda);
    f = pow(lambda, (double) i);
    Poisson[i] = (exp(-lambda) * pow(lambda, (double) i)) / fact[i];
    DBG(fprintf(stderr, "Poisson[%d] = %f\n", i, Poisson[i]));
  }
}

double
poisson(int i)
{
  return (Poisson[i]);
}

#if 0
int
poisson()
{
  double v, accm;
  int    r, i;

  r = myrandom(10000);
  v = (double)r/10000.0;
  accm = Poisson[0];
  for (i = 1;  i < MAX_DUPLICATES;  i++) {
    if (v < accm)
      return (i-1);
    accm += Poisson[i];
  }

  return (MAX_DUPLICATES - 1);
}
#endif

/*
 * probability(p) : Returns TRUE if a random number [1,100] is < p
 */
boolean
probability(p)
float p;
{
	register float r;
	r = frandom(100);
	return ((r < p)? TRUE:  FALSE);
}

void
generate_ssn(p)
Person *p;
{
	boolean  all_zero;
	register int d;

	/*  Check if we should generate an SSN for this person */
	if (!probability(ssns.gen_prob)) {
	    strcpy(p->ssn, "000000000");
	    return;
	}

	/*  No SSN number starts with 0 or 900 */
	p->ssn[0] = myrandom(7) + '1';
	/* Generate the rest of the numbers at random */
	for (d = 1;  d < ssns.max_digits;  d++)
	    p->ssn[d] = gendigit();

	/* Check for illegal all zero fields */
	all_zero = TRUE;
	for (d = 3; d <= 4;  d++)
	    if (p->ssn[d] != '0') {
		all_zero = FALSE;
		break;
	    }
	if (all_zero) {
	    d = myrandom(2) + 3;
	    p->ssn[d] = myrandom(9) + '1';
	}

	all_zero = TRUE;
	for (d = 5; d < 9; d++)
	    if (p->ssn[d] != '0') {
		all_zero = FALSE;
	        break;
	    }
	if (all_zero) {
	    d = myrandom(4) + 5;
	    p->ssn[d] = myrandom(9) + '1';
	}

	/* Put the EOS and print it out */
	p->ssn[ssns.max_digits] = 0;
}

void
generate_name(p)
register Person *p;
{
	long fn, ln;

	fn = myrandom(NameList.size);
	strcpy(p->fname, NameList.item[fn]);
	p->fname[0] = toupper(p->fname[0]);
	while ((ln = myrandom(NameList.size)) == fn);
	strcpy(p->lname, NameList.item[ln]);
	p->lname[0] = toupper(p->lname[0]);

	if (probability(names.no_middle_name))
	    strcpy(p->minit, "");
	else {
	    p->minit[0] = toupper(genletter());
	    p->minit[1] = 0;
	}
}

void
generate_address(p)
register Person *p;
{
	float r, t;
	boolean using_PO_box = FALSE;
	char zipcitystate[100];
	long s, v, ra;

	if (probability(addresses.use_PO_box)) {
	    strcpy(p->street_number, "");
	    strcpy(p->apartment, "");
	    strcpy(p->suffix, "");
	    sprintf(p->street_name,"PO Box %d", myrandom(5000)+1);
	    using_PO_box = TRUE;
	} else {
	    sprintf(p->street_number, "%d", myrandom(1000)+1);
	    sprintf(p->apartment, "%c%c%c", gendigit(), genletter(), gendigit());
	    ra = myrandom(NameList.size);
	    strcpy(p->street_name, NameList.item[ra]);
	    p->street_name[0] = toupper(p->street_name[0]);
	    r = frandom(100);
	    if (r < (t = addresses.street_name.suffix.none))
		strcpy(p->suffix, "");
	    else
	    if (r < (t = t + addresses.street_name.suffix.street))
		strcpy(p->suffix, (myrandom(100)<50)? "Street": "St");
	    else
	    if (r < (t = t + addresses.street_name.suffix.road))
		strcpy(p->suffix, (myrandom(100)<50)? "Road": "Rd");
	    else
	    if (r < (t = t + addresses.street_name.suffix.avenue))
		strcpy(p->suffix, (myrandom(100)<50)? "Avenue": "Ave");
	    else
	    if (r < (t = t + addresses.street_name.suffix.lane))
		strcpy(p->suffix, (myrandom(100)<50)? "Lane": "Ln");
	    else
	    if (r < (t = t + addresses.street_name.suffix.pkwy))
		strcpy(p->suffix, (myrandom(100)<40)? "Parkway": "Pkwy");
	    else
		strcpy(p->suffix,"");
	}

	s = myrandom((addresses.state_city_and_zip.num_of_states > nzindex)?
		     nzindex: addresses.state_city_and_zip.num_of_states);
	v = zindex[s].fpos - zindex[s].spos;
	strcpy(zipcitystate, zips[zindex[s].spos + myrandom(v)]);

	strcpy(p->zipcode, strtok(zipcitystate, ":"));
	strcpy(p->city, strtok(NULL, ":"));
	strcpy(p->state, strtok(NULL, ":"));
}

char newletter(oc)
char oc;
{
	char nc;
	long r;
	float t;
	long p;

	if (!isalpha(oc))
	    return(genletter());

	oc = tolower(oc);
	r = frandom(100);
	if (r < (t = typos.same_row)) {
	    p = myrandom(strlen(rows[oc-'a']));
	    return (rows[oc-'a'][p]);
	} else
	if (r < (t += typos.same_column)) {
	    p = myrandom(strlen(cols[oc-'a']));
	    return (cols[oc-'a'][p]);
	} else
	if (r < (t += typos.homologous)) {
	    p = KEYBOARD_COLS-in_column(oc);
	    if (isalpha(keyboard[in_row(oc)][p]))
		return (keyboard[in_row(oc)][p]);
	}
	/* Else, generate a random letter */
	while ((nc = genletter()) == oc);
	return(nc);
}

int
select_a_letter(word, len)
register char *word;
int len;
{
	int alphabet[26];
	float p, t, letter_value[26], cons_val,  vowel_val;
	int n_cons = 0, n_vowels = 0;
	register int j;
	int k, l, r;
	char ch;

	
	if (typos.first_letter < 100.0/len && probability(typos.first_letter))
	    /* Change the first letter of the word. */
	    return(0);

	for (j = 0;  j < 26;  j++) 
	    alphabet[j] = 0;

	for (j = 0;  j < len;  j++) {
	    if (isalpha(word[j])) {
		ch = tolower(word[j]);
		if (alphabet[ch - 'a'] > 0) {
		    alphabet[ch - 'a']++;
		    continue;
		}
	        alphabet[ch - 'a']++;
		if (ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' ||
		    ch == 'u')
		    n_vowels++;
		else
		    n_cons++;
	    }
	}

	if (n_vowels > 0  &&  n_cons > 0) {
	    cons_val = typos.consonants/n_cons;
	    vowel_val = (100.0 - typos.consonants)/n_vowels;
	} else 
	    if (n_cons > 0)
		cons_val = 100.0/n_cons;
	    else
		vowel_val = 100.0/n_vowels;

	for (j = 0;  j < 26;  j++)
	    if (alphabet[j])
		if (j == 0 || j == 'e'-'a' || j == 'i'-'a' ||
		    j == 'o'-'a' || j == 'u'-'a')
		    letter_value[j] = vowel_val;
		else
		    letter_value[j] = cons_val;
	    else
		letter_value[j] = 0.0;

	p = frandom(100);
	t = 0;
	for (j = 0;  j < 26;  j++)
	    if (alphabet[j]) {
		t += letter_value[j];
		if (t >= p)
		    break;
	    }
	if (alphabet[j] > 1)
	    k = myrandom(alphabet[j]) + 1;
	else
	    k = 1;
	for (l = 0;  l < k;  l++) {
	    for (r = 0;  r < len;  r++) 
		if (tolower(word[r]) == 'a'+j)
		    break;
	}
	return(r);
}

void
typo(word, errorp)
char *word;
single_err_struct *errorp;
{
	register int c, j;
	register int l = strlen(word);
	float t, p;
	char temp;

	if (l < 2) return;	/* Do not consider 1 letter words. */
	
	p = frandom(100);
	if (p < (t = errorp->insertion)) {
	    c = myrandom(l+1);
	    for (j = l;  j >= c;  j--)
		word[j+1] = word[j];
	    word[c] = newletter(word[c]);
	} else 
	if (p < (t += errorp->deletion)) {
	    c = select_a_letter(word, l);
	    for (j = c;  j < l;  j++)
		word[j] = word[j+1];
	} else
	if (p < (t += errorp->replacement)) {
	    c = select_a_letter(word, l);
	    word[c] = newletter(word[c]);
	} else
	if (p < (t += errorp->swapping)) {
	    c = myrandom(l-1);
	    temp = word[c];
	    word[c] = word[c+1];
	    word[c+1] = temp;
	}
}

void
corrupt_ssns(p)
register Person *p;
{
	register int l = strlen(p->ssn);
	int j, n, c;
	char nd;

	if (strcmp(p->ssn, "000000000") == 0 && probability(30)) {
	    generate_ssn(p);
	    return;
	} else
	    if (strcmp(p->ssn, "000000000") == 0)
		return;

	n = (probability(80))? 1: 2;
	for (j = 0;  j<n;  j++) {
	    c = myrandom(l);
	    while ((nd = gendigit()) == p->ssn[c]);
	    p->ssn[c] = nd;
	}
}
   

/*
 * corrupt_record(p, d) :  generate a corrupted copy of the record in p and
 *	return it in d.
 */
void
corrupt_record(p, d)
register Person *p, *d;
{
	register long j, r, l;
	boolean has_error = FALSE;
	single_err_struct generic;
	char *newzip;

	generic.prob = 80.0;
	generic.insertion = 30.0;
	generic.replacement = 30.0;
	generic.swapping = 20.0;
	generic.deletion = 20.0;

	/* Begin with the SSN */
	strcpy(d->ssn, p->ssn);
	if (probability(ssns.errorp)) {
	    has_error = TRUE;
	    corrupt_ssns(d->ssn);
	}

	/* Names */
	strcpy(d->fname, p->fname);
	strcpy(d->minit, p->minit);
	strcpy(d->lname, p->lname);
	if (probability(names.errorp)) {
	    has_error = TRUE;
	    /* Swap fname with lname */
	    if (probability(names.swap_names)) {
		strcpy(d->fname, p->lname);
		strcpy(d->lname, p->fname);
	    }
 
	    /* Change lname */
	    if (probability(names.change_lname)) {
		strcpy(d->lname, NameList.item[myrandom(NameList.size)]);
	    }

	    /* generate a typo */
	    if (probability(names.single_error.prob)) {
		/* single typing error */
		r = myrandom(100);
		if (r < 47)
		    typo(d->fname, &names.single_error);
		else
		if (r < 94)
		    typo(d->lname, &names.single_error);
		else
		    if (myrandom(27)==1)
			d->minit[0] = '\0';
		    else
			d->minit[0] = genletter();
	    } else {
		/* Generate a double, tripe, etc. typographical error */
		l = myrandom(2)+2;
	        for (j = 2;  j <= l;  j++) {
		    r = myrandom(100);
		    if (r < 47)
			typo(d->fname, &names.single_error);
		    else
		    if (r < 94)
			typo(d->lname, &names.single_error);
		    else
			if (myrandom(27)==1)
			    d->minit[0] = '\0';
		    	else
			    d->minit[0] = genletter();
		}
	    }

	    /* Repace the first name by the initial */
	    if (probability(names.fname_for_init)) {
		sprintf(d->fname, "%c", p->fname[0]);
	    }
	}

	/* Address */
	if (probability(addresses.changep)) {
	    generate_address(d);
	    if (probability(60.0)) {
		/* 60% of the times, remain in the same city */
	    	strcpy(d->city, p->city);
	    	strcpy(d->state, p->state);
	    	strcpy(d->zipcode, p->zipcode);
	    }
	} else {
	    strcpy(d->street_number, p->street_number);
	    strcpy(d->street_name, p->street_name);
	    strcpy(d->suffix, p->suffix);
	    strcpy(d->apartment, p->apartment);
	    strcpy(d->city, p->city);
	    strcpy(d->state, p->state);
	    strcpy(d->zipcode, p->zipcode);
	}

	if (strlen(d->street_number) > 0) {
	    if (probability(addresses.street_number.errorp)) {
		has_error = TRUE; 
		r = myrandom(strlen(d->street_number));
		/* Shouldn't I use an special typo routine here */
		d->street_number[r] = gendigit();
	    }
	}

	if (probability(addresses.street_name.errorp)) {
	    has_error = TRUE;
	    if (probability(addresses.street_name.single_error.prob)) {
		typo(d->street_name, &addresses.street_name.single_error);
	    } else {
#if 0
		l = myrandom(2)+2;
		for (j = 2;  j <= l;  j++) {
		     typo(d->street_name, &addresses.street_name.single_error);
		}
#endif
	    }
	    /* Change the terminator */
	    if (myrandom(100) < 20) {
		if (strcmp(d->suffix, "Street") == 0)
		    if (myrandom(100) < 90)
		        strcpy(d->suffix, "St");
		    else
			strcpy(d->suffix, "Avenue");
		else
		if (strcmp(d->suffix, "Avenue") == 0)
		    if (myrandom(100) < 90)
		        strcpy(d->suffix, "Av");
		    else
			strcpy(d->suffix, "Road");
		else
		if (strcmp(d->suffix, "Lane") == 0)
		    strcpy(d->suffix, "Ln");
		else
		if (strcmp(d->suffix, "Road") == 0)
		    strcpy(d->suffix, "Rd");
		else
		if (strcmp(d->suffix, "Parkway") == 0)
		    strcpy(d->suffix, "Pkwy");
	    }
	}

	if (probability(addresses.state_city_and_zip.errorp)) {
	    has_error = TRUE;
	    if (probability(addresses.state_city_and_zip.single_err_prob))
		typo(d->city, &generic);
	    else {
#if 0
		l = myrandom(2)+2;
		for (j = 2;  j <= l;  j++) 
		     typo(d->city, &generic);
#endif
	    }
	
	    if (probability(addresses.state_city_and_zip.change_zip_code)) {
		l = myrandom(strlen(d->zipcode));
		d->zipcode[l] = gendigit();
	    }

	    if (probability(addresses.state_city_and_zip.change_state)) {
		l = myrandom(nzindex);
		newzip = zips[zindex[l].spos];
		strcpy(d->state, &newzip[strlen(newzip)-2]);
	    }
	}
}


static long maxNRecords;

void
print_record(clusterId, p, fixed, originalRecord)
int clusterId;
register Person *p;
int fixed;
boolean originalRecord;
{
	int sortPos;

	if (originalRecord)
	  sortPos = clusterId;
	else
	  sortPos = clusterId + myrandom(maxNRecords) + 1;
	
        if (fixed)
	  fprintf(out, "%-8d:%-8d:%9s:%-15s:%-2s:%-15s:%-4s:%-15s",
		  clusterId, sortPos, p->ssn, p->fname, p->minit, p->lname,
		  p->street_number, p->street_name);
	else
	  fprintf(out, "%d:%d:%s:%s:%s:%s:%s:%s",
		  clusterId, sortPos, p->ssn, p->fname, p->minit, p->lname,
		  p->street_number, p->street_name);
	trecs++;
	if (fixed)
	  fprintf(out, "%-7s", p->suffix);
 	else
	  if (strlen(p->suffix) > 0)
	    fprintf(out, " %s", p->suffix);
	if (fixed)
	  fprintf(out, ":%-4s:%-16s:%-2s:%-5s\n",
		  p->apartment, p->city,
		  p->state, p->zipcode);
	else
	  fprintf(out, ":%s:%s:%s:%s\n",
		  p->apartment, p->city,
		  p->state, p->zipcode);
}

char *
my_strtok(char *buffer, register char term)
{
        static char *tokbuf;
        char *ans;

        if (buffer)
            tokbuf = buffer;

        ans = tokbuf;
        while (*tokbuf)
            if (*tokbuf++ == term) {
                *(tokbuf-1) = '\0';
                break;
            }

        return((*ans)? ans: NULL);
}

int
read_record(FILE *infp, Person *Record)
{
  int rid;
  char *ptr, oneline[255];

  if (fgets (oneline, sizeof(oneline), infp)) {
    rid = atoi(my_strtok(oneline, ':'));
    my_strtok(NULL, ':');
    ptr = my_strtok(NULL, ':');
    strcpy (Record->ssn, (ptr)? ptr: "");
    ptr = my_strtok(NULL, ':');
    strcpy (Record->fname, (ptr)? ptr: "");
    ptr = my_strtok(NULL, ':');
    strcpy (Record->minit, (ptr)? ptr: "");
    ptr = my_strtok(NULL, ':');
    strcpy (Record->lname, (ptr)? ptr: "");
    ptr = my_strtok(NULL, ':');
    strcpy (Record->street_number, (ptr)? ptr: "");
    ptr = my_strtok(NULL, ':');
    strcpy (Record->street_name, (ptr)? ptr: "");
    ptr = my_strtok(NULL, ':');
    strcpy (Record->apartment, (ptr)? ptr: "");
    strcpy (Record->suffix, "");
    ptr = my_strtok(NULL, ':');
    strcpy (Record->city, (ptr)? ptr: "");
    ptr = my_strtok(NULL, ':');
    strcpy (Record->state, (ptr)? ptr: "");
    ptr = my_strtok(NULL, '\n');
    strcpy (Record->zipcode, (ptr)? ptr: "");
   
    return rid;
  }

  return -1;
}


/*
 *  generate() - Generate a random database of names given a model.
 *
 */
int
generate(seed, namesfilename, outfilename, inputfilename, first_uid, append_flag, qflag, fixed)
int seed;
char *namesfilename;
char *outfilename;
char *inputfilename;
int  first_uid;
int  append_flag;
int  qflag;
int  fixed;
{
  FILE *nfile, *fopen(), *infp;
  register long i, j;
  long maxd, m, nnames, tdup, dups, tdups;

  /* Added for version 2.0 */
  long nRecords;
  long nClusters;
  long nRecsPerCluster;
  long clstId = 0;
  /* --------------------- */
  
  long t, v, ruid;
  struct stat sbuf;
  char *argv[30], command[200];
  int wc, topipe[2], pid, oldstdout, argc = 0, addincr_flag = 0;
  boolean pipeoutput = FALSE;
  
  static  int last;
  
  be_quiet = qflag;
  
  if (inputfilename) addincr_flag++;
  
  if (outfilename[0] == '|') {
    /* Pipe the output trought */
    for (j = 1;  j < strlen(outfilename);  j++)
      if (outfilename[j] != ' ')
	break;
    if (j == strlen(outfilename)) {
      fprintf(stderr, "Error:  Illegal output name.\n");
      return(-1);
    }
    strcpy(command, &outfilename[j]);
    argv[argc++] = (char *) strtok(command, " ");
    while (argv[argc++] = (char *) strtok(NULL, " "));
    argv[argc] = (char *) 0;
    
    pipe(topipe);
    if ((pid = fork()) == 0) {
      /* This is the forked child. */
      close(0);	/* Close the stdin */
      dup(topipe[0]);	/* Dup the input side of the pipe to stdin. */
      close(topipe[0]);
      close(topipe[1]);
      execv(argv[0], argv);
    } else 
      if (pid < 0) {
	fprintf(stderr,"Error:  Cannot fork the end-pipe process.\n");
	return(-1);
      }
    /* This is the parent section */
    oldstdout = dup(1); /* Dup the stdout */
    close(1);		/* Close the stdout */
    dup(topipe[1]);	/* Dup the output side of the pipe to stdout.*/
    close(topipe[0]);
    close(topipe[1]);
    
    pipeoutput = TRUE;
  }
  
  if (FirstTime) {
    if (stat(namesfilename, &sbuf) < 0) {
      fprintf(stderr, "Error: Names DB '%s' not found.\n", namesfilename);
      return(-1);
    }
    if ((nnames = read_names(namesfilename)) < 0) {
      fprintf(stderr, "Error while reading Names DB '%s'.\n", namesfilename);
      return(-1);
    }
    if (!be_quiet)
      fprintf(stderr, "%d words read from %s.\n", nnames, namesfilename);
    
    /* Preare keyboard information */
    key_substitutions();
  }
  
  /* Initialize the Random generator to seed.  If seed = 0, use the
   * number of seconds since 00:00:00 GMT,  Jan.  1,  1970          */
#ifndef __Linux
  srand((seed == 0)? (seed = (int) (time(NULL) % 32767)): seed);
#else
  srandom((seed == 0)? (seed = (int) (time(NULL) % 32767)): seed);
#endif
  
  if (FirstTime) 
    /* Prepare the Zipcodes data */
    read_zipcodes();
  FirstTime = FALSE;
  
  trecs = first_uid;
  last = 0;
  ruid = first_uid;
  
  if (append_flag) {
    FILE *oldfp;
    char lastline[255];
    
    if ((oldfp = fopen(outfilename, "r")) == NULL) {
      fprintf(stderr, "Error: Cannot open '%s' for reading.\n", outfilename);
      return(-1);
    }
    while (fgets(lastline, sizeof lastline,  oldfp))
      trecs++;
    fclose(oldfp);
    last = atoi(lastline);
  }
  
  if (pipeoutput || strcmp(outfilename, "stdout") == 0)
    out = stdout;
  else
    if ((out = fopen(outfilename, (append_flag)? "a": "w")) == NULL) {
      fprintf(stderr, "Error: Cannot open '%s' for writting.\n", outfilename);
      return(-1);
    }

  if (addincr_flag) {
    if ((infp = fopen(inputfilename, "r")) == NULL) {
      fprintf(stderr, "Error: Cannot open '%s' for reading.\n", inputfilename);
      return(-1);
    }
  }
  
  maxNRecords = nRecords = global_info.num_of_records;
  /* m = global_info.max_dup_per_record; */
  nClusters = global_info.num_of_clusters;


  nRecsPerCluster = nRecords/nClusters;
  
  tdup = tdups = 0;
  if (distribution == POISSON)
    /* nRecsPerCluster is the mean */
    init_poisson(nRecsPerCluster);
  else
    if (distribution == ZIPF)
      init_zipf(nClusters);
  
  DBG(fprintf(out, "maxd = %d\n", maxd));
 

  /* Main Loop */
  for (i = 0;  i < nClusters;  i++) {
    generate_ssn(&Record);
    generate_name(&Record);
    generate_address(&Record);
    print_record(clstId, &Record, fixed, TRUE);

    /* Now corrupt the current record */
    dups = 0;
    if (distribution == UNIFORM) {
      dups = myrandom(nRecsPerCluster*2 - 1);
    } else
      if (distribution == POISSON) {
	dups = poisson(i+1) * nRecords;
      } else {
	dups = zipf(i+1) * (nRecords);
      }

   /* fprintf(stderr, "%d\t%d\n", i, dups);*/
    for (j = 0;  j < dups;  j++) {
      corrupt_record(&Record, &Duplicate);
      print_record(clstId, &Duplicate, fixed, FALSE);
      DBG(fprintf(out, "i=%d\n", i));
    }

    clstId += dups + 1;

    if (!be_quiet && (i%1000) == 0) {
      fprintf(stderr, ".");
      fflush(stderr);
    }
  }
  
  if (!be_quiet && i >= 500)
    fprintf(stderr, "\n");
  
  if (strcmp(outfilename, "stdout") != 0)
    fclose(out);
  
  if (qflag) {
    fprintf(stderr, "------------------------------------------------------\n");
    fprintf(stderr, " %d records generated.\n", trecs);
    fprintf(stderr, "    %d original records, of which\n", nRecords);
    fprintf(stderr, "    %d where selected for duplication, creating  \n", tdup);
    fprintf(stderr, "    %d duplicate records.\n", tdups);
    fprintf(stderr, " Random seed : %d\n", seed);
    fprintf(stderr, "------------------------------------------------------\n");
  }
  results.trecs = trecs;
  results.orecs = nRecords;
  results.drecs = tdup;
  results.tdrecs = tdups;
  results.seed = seed;
  
  if (pipeoutput) {
    printf("%c", 0);
    close(1);
    /* Wait for death of the child */
    wait(&wc);
    dup(oldstdout);
    close(oldstdout);
  }
}


