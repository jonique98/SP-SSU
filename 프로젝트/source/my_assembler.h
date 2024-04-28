#ifndef __MY_ASSEMBLER_H__
#define __MY_ASSEMBLER_H__


#define MAX_INST_TABLE_LENGTH 256
#define MAX_INPUT_LINES 5000
#define MAX_TABLE_LENGTH 5000
#define MAX_OPERAND_PER_INST 3
#define MAX_OBJECT_CODE_STRING 74
#define MAX_OBJECT_CODE_LENGTH 5000
#define MAX_LINES 100
#define MAX_CONTROL_SECTION_NUM 10
#define MAX_EXTDEF 10
#define MAX_EXTREF 10
#define MAX_MODIFICATION_RECORD 10
#define MAX_SUBROUTINE_NUM 3
#define MAX_TEXT_LENGTH 100

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>

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
    char nixbpe;
} token;


typedef struct _symbol {
    char name[10];
    int addr;
    char *label;
} symbol;

typedef struct _literal {
    char literal[20];
    int addr;
} literal;


typedef struct _modification_record {
    int addr;
    int modification_length;
    char *symbol;
} modification_record;

typedef struct _header {
    char *header_symbol;
    int start_address;
    int program_length;
} header;

typedef struct _extref {
    char *symbol;
    int addr;
} extref;

typedef struct _extdef {
    char *symbol;
    int addr;
} extdef;

typedef struct _text_table {
    unsigned int text;
    unsigned int loc;
    char *operator;
} text_table;

typedef struct _obj_code_data {
    header header_record;
    extref extref_table[MAX_EXTREF];
    extdef extdef_table[MAX_EXTDEF];
    int extdef_length;
    text_table text[MAX_TEXT_LENGTH];
    int text_length;
    char *literals[MAX_TABLE_LENGTH];
    modification_record modification_table[MAX_MODIFICATION_RECORD];
    int modification_table_length;
} obj_code_data;

/**
 * @brief 오브젝트 코드 전체에 대한 정보를 담는 구조체
 *
 * @details
 * 오브젝트 코드 전체에 대한 정보를 담는 구조체이다. Header Record, Define
 * Record, Modification Record 등에 대한 정보를 모두 포함하고 있어야 한다. 이
 * 구조체 변수 하나만으로 object code를 충분히 작성할 수 있도록 구조체를 직접
 * 정의해야 한다.
 */
typedef struct _object_code {
    obj_code_data main_program;
    int end_addr;
    obj_code_data subroutine[MAX_SUBROUTINE_NUM];
    int subroutine_length;
} object_code;




int init_inst_table(inst *inst_table[], int *inst_table_length,
                    const char *inst_table_dir);
int init_input(char *input[], int *input_length, const char *input_dir);
int assem_pass1(const inst *inst_table[], int inst_table_length,
                const char *input[], int input_length, token *tokens[],
                int *tokens_length, symbol *symbol_table[],
                int *symbol_table_length, literal *literal_table[],
                int *literal_table_length);
int token_parsing(const char *input, token *tok, const inst *inst_table[],
                  int inst_table_length);
int search_opcode(const char *str, const inst *inst_table[],
                  int inst_table_length);
int make_opcode_output(const char *output_dir, const token *tokens[],
                       int tokens_length, const inst *inst_table[],
                       int inst_table_length);
int assem_pass2(const token *tokens[], int tokens_length,
                const inst *inst_table[], int inst_table_length,
                const symbol *symbol_table[], int symbol_table_length,
                const literal *literal_table[], int literal_table_length,
                object_code *obj_code);
int make_symbol_table_output(const char *symbol_table_dir,
                             const symbol *symbol_table[],
                             int symbol_table_length);
int make_literal_table_output(const char *literal_table_dir,
                              const literal *literal_table[],
                              int literal_table_length);
int make_objectcode_output(const char *objectcode_dir,
                           const object_code *obj_code);

int strings_len(char **strings);
int word_count(char *str);
int word_count_operands(char *str);
char **operand_split(char *str);
int format_change(char *str, int *err);
char **split(char *str, int *err);
int is_white_space(char c);
int calc_totallen(char **temp);
int get_ops(int table_index, const inst **inst_table);
int starts_with(const char *str, const char c);
int is_operator(const char *str, const inst **inst_table, int inst_table_length);
int return_operator_equ(char *str);
char **equ_split(char *str);
int is_in_symbol_table(const char *str, symbol **symbol_table,
                       int symbol_table_length);
int is_in_literal_table(const char *str, literal **literal_table,
                        int literal_table_length);
int get_format(int table_index, const inst **inst_table);
int is_in_symbol_table_wrapper(const char *str, const char *label,
                                symbol **symbol_table,
                               int symbol_table_length) ;


#endif
