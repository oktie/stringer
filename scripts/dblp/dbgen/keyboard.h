/*
 *  External global definitions in keyboard.c
 */
#define KEYBOARD_COLS 10
#define KEYBOARD_ROWS 3

extern char keyboard[][KEYBOARD_COLS];
extern char rows[][4];
extern char cols[][4];
extern char inrow[], incolumn[];

extern void key_substitutions();

#define in_column(x) (incolumn[(x)])
#define in_row(x)    (inrow[(x)])
