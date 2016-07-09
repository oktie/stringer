/*
 *  gen_dbgen.h  :  Main definitions for the database generator.
 *	      This files serves both zipfgen.c and ui.c
 *
 *	Author:  Mauricio Hernandez
 *
 *  Copyright (c) 1996, 1997 by Mauricio A. Hernandez
 *  All rights reserved.
 *
 * $Id: gen_dbgen.h,v 2.0 1997/06/29 02:09:56 mauricio Exp mauricio $
 */
#define UNIFORM 0
#define POISSON 1
#define ZIPF	2
#define MAX_DUPLICATES 50

typedef struct {
	long 	num_of_records;
	long	num_of_clusters;
	float	num_of_duplicates;
	long	max_dup_per_record;
} GLOBAL;

typedef struct {
	int	max_digits;
	float 	gen_prob;
	float	errorp;
} SSN;

typedef struct {
	float prob;
	float insertion;
	float deletion;
	float replacement;
	float swapping;
} single_err_struct;

typedef struct {
	float errorp;
	single_err_struct  single_error;
	float charswap;
	float tokenswap;
} TITLES1;

typedef struct {
	float errorp;
	single_err_struct  single_error;
	float swap_names;
	float no_middle_name;
	float fname_for_init;
	float change_lname;
} NAMES;

typedef struct {
	float use_PO_box;
	float changep;
	struct {
	    int max_digits;
	    float errorp;
	} street_number;
	struct {
	    float errorp;
	    single_err_struct  single_error;
	    struct {
		float none;
		float street;
		float road;
		float avenue;
		float lane;
		float pkwy;
	    } suffix;
	} street_name;
	struct {
	    float errorp;
	    float single_err_prob;
	    float change_zip_code;
	    float change_state;
	    long  num_of_states;
	} state_city_and_zip;
} ADDRESSES;

typedef struct {
	float same_row;
	float same_column;
	float homologous;
	float first_letter;
	float consonants;
	char  consonant_freq[100];
} TYPO_TYPES;

typedef struct {
	long	trecs;
	long	orecs;
	long	drecs;
	long	tdrecs;
	int	seed;
} RESULTS;

/* Global variable definitions */
extern GLOBAL global_info;
extern SSN ssns;
extern NAMES names;
extern ADDRESSES addresses;
extern TITLES1 titles1;
extern TYPO_TYPES typos;
extern RESULTS results;
extern int distribution;

extern int generate();
