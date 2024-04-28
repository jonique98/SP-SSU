CC=gcc
CFLAGS=-I.
DEPS = my_assembler.h
SRC = my_assembler.c token_parsing.c utils.c assem_pass1.c

%.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS)

my_assembler: $(SRC)
	$(CC) -o $@ $^ $(CFLAGS)

.PHONY: clean

clean:
	rm -f *.o *~ core my_assembler output_20181259.txt output_littab.txt output_symtab.txt output_objectcode.txt
	