CC=gcc
CFLAGS=-I.
DEPS = my_assembler.h
OBJ = my_assembler.o

%.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS)

my_assembler: $(OBJ)
	$(CC) -o $@ $^ $(CFLAGS)

.PHONY: clean

clean:
	rm -f *.o *~ core my_assembler
