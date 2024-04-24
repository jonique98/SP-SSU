#define MAX_INST_TABLE_LENGTH 256
#define MAX_INPUT_LINES 5000
#define MAX_TABLE_LENGTH 5000
#define MAX_OPERAND_PER_INST 3
#define MAX_OBJECT_CODE_STRING 74
#define MAX_OBJECT_CODE_LENGTH 5000
#define MAX_LINES 100

typedef struct _inst {
    char str[10];
    unsigned char op;
    int format;
    int ops;
} inst;


typedef struct _token {
    char *label;
    char *operator;
    char *operand[MAX_OPERAND_PER_INST];
    char *comment;
    // char nixbpe; // 다음 과제에 사용될 변수
} token;


typedef struct _symbol {
    char name[10];
    int addr;
} symbol;

typedef struct _literal {
    char literal[20];
    int addr;
} literal;


int init_inst_table(inst **inst_table, int *inst_table_length,
                    const char *inst_table_dir);
int init_input(char **input, int *input_length, const char *input_dir);
int assem_pass1(const inst **inst_table, int inst_table_length,
                const char **input, int input_length, token **tokens,
                int *tokens_length, symbol **symbol_table,
                int *symbol_table_length, literal **literal_table,
                int *literal_table_length);
int token_parsing(const char *input, token *tok, const inst **inst_table,
                  int inst_table_length);
int search_opcode(const char *str, const inst **inst_table,
                  int inst_table_length);
int make_opcode_output(const char *output_dir, const token **tokens,
                       int tokens_length, const inst **inst_table,
                       int inst_table_length);
int assem_pass2(const token **tokens, int tokens_length,
                const symbol **symbol_table, int symbol_table_length,
                const literal **literal_table, int literal_table_length,
                char object_code[][MAX_OBJECT_CODE_STRING],
                int *object_code_length);
int make_symbol_table_output(const char *symtab_dir,
                             const symbol **symbol_table,
                             int symbol_table_length);
int make_literal_table_output(const char *literal_table_dir,
                              const literal **literal_table,
                              int literal_table_length);
int make_objectcode_output(const char *objectcode_dir,
                           const char object_code[][MAX_OBJECT_CODE_STRING],
                           int object_code_length);
