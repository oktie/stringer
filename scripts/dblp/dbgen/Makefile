CC=gcc
CFLAGS=-g
#ARCH=/export/home/oktie/workspace/dbgen
ARCH=.
all: $(ARCH) $(ARCH)/dbgen

install:
	cp $(ARCH)/dbgen ../../bin/.

clean:
	rm -rf $(ARCH)/*.o $(ARCH)/dbgen

$(ARCH):
	mkdir $(ARCH)

$(ARCH)/dbgen: $(ARCH)/ui_dbgen.o $(ARCH)/zipfgen_dbgen.o $(ARCH)/keyboard.o
	$(CC) $(ARCH)/zipfgen_dbgen.o $(ARCH)/ui_dbgen.o $(ARCH)/keyboard.o -lcurses -ltermcap -lm -o $@

.c.o:
	$(CC) $(CFLAGS) -c $<

$(ARCH)/keyboard.o: keyboard.c
	$(CC) $(CFLAGS) -c keyboard.c -o $@

$(ARCH)/ui_dbgen.o: ui_dbgen.c gen_dbgen.h
	$(CC) $(CFLAGS) -c ui_dbgen.c -o $@

$(ARCH)/zipfgen_dbgen.o: zipfgen_dbgen.c gen_dbgen.h keyboard.h
	$(CC) $(CFLAGS) -c zipfgen_dbgen.c -o $@
