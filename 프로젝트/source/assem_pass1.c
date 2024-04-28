#include "my_assembler.h"

// inst_table에서 str에 해당하는 인덱스를 반환
int search_opcode(const char *str, const inst **inst_table,
                  int inst_table_length) {


    if(*str == '+')
        str++;

    for (int i = 0; i < inst_table_length; i++) {
        if (strcmp(str, inst_table[i]->str) == 0) {
            return i;
        }
    }

    return -1;
}

int get_format(int table_index, const inst **inst_table) {
    return inst_table[table_index]->format;
}

// inst_table에서 table_index에 해당하는 인덱스의 format을 반환
int get_ops(int table_index, const inst **inst_table) {
    return inst_table[table_index]->ops;
}

int is_in_symbol_table(const char *str, symbol **symbol_table,
                       int symbol_table_length) {

    if(str[0] == '@' || str[0] == '#')
        str++;
    for (int i = 0; i < symbol_table_length; i++) {
        if (strcmp(str, symbol_table[i]->name) == 0) {
            return i;
        }
    }
    return -1;
}

int is_in_symbol_table_wrapper(const char *str, const char *label,
                                symbol **symbol_table,
                               int symbol_table_length) {

    if (!str)
        return -1;

    if(str[0] == '@' || str[0] == '#')
        str++;

    for (int i = 0; i < symbol_table_length; i++) {
        if (strcmp(str, symbol_table[i]->name) == 0 && strcmp(label, symbol_table[i]->label) == 0) {
            return i;
        }
    }
    return -1;
}

int is_in_literal_table(const char *str, literal **literal_table,
                        int literal_table_length) {
    for (int i = 0; i < literal_table_length; i++) {
        if (strcmp(str, literal_table[i]->literal) == 0) {
            return i;
        }
    }
    return -1;
}

int init_inst_table(inst **inst_table, int *inst_table_length,
                    const char *inst_table_dir) {
    FILE *fp;
	char line[MAX_LINES];
    int err;

    err = 0;

	fp = fopen(inst_table_dir, "r");
	if (fp == NULL) {
		fprintf(stderr, "init_inst_table: 기계어 목록 파일을 열 수 없습니다.\n");
        err = -1;
        return err;
	}

	int i = 0;
	char **temp;
	while (fgets(line, MAX_LINES, fp) != NULL) {
        if(word_count(line) == 0)
            continue;

        if(word_count(line) != 4) {
            fprintf(stderr, "init_inst_table: 기계어 목록 파일의 형식이 잘못되었습니다.\n");
            err = -1;
            return err;
        }

		temp = split(line, &err);
        if (temp == NULL) {
            fprintf(stderr, "init_inst_table: split 함수에서 에러가 발생했습니다.\n");
            return err;
        }

        inst_table[i] = (inst *)malloc(sizeof(inst));
        if (inst_table[i] == NULL) {
            fprintf(stderr, "init_inst_table: 메모리 할당에 실패했습니다.\n");
            exit(1);
        }
        strcpy(inst_table[i]->str, temp[0]);
        inst_table[i]->op = (unsigned char)strtol(temp[1], NULL, 16);
        inst_table[i]->format = format_change(temp[2], &err);
        inst_table[i]->ops = atoi(temp[3]);
        i++;
	}
    *inst_table_length = i;
    return err;
}

int init_input(char **input, int *input_length, const char *input_dir) {
    FILE *fp;
    int err;

    err = 0;
    fp = fopen(input_dir, "r");
    if (fp == NULL) {
        fprintf(stderr, "init_input: 소스코드 파일을 열 수 없습니다.\n");
        err = -1;
        return err;
    }

    int i = 0;
    char line[MAX_LINES];
    while (fgets(line, MAX_LINES, fp) != NULL) {
        input[i] = (char *)malloc(strlen(line) + 1);
        if (input[i] == NULL) {
            fprintf(stderr, "init_input: 메모리 할당에 실패했습니다.\n");
            exit(1);
        }
        strcpy(input[i], line);
        i++;
    }
    *input_length = i;
    return err;
}

int make_opcode_output(const char *output_dir, const token **tokens,
                       int tokens_length, const inst **inst_table,
                       int inst_table_length) {
    FILE *fp;
    int err;

    err = 0;

    fp = fopen(output_dir, "w");
    if (fp == NULL) {
        fprintf(stderr, "make_opcode_output: 출력 파일을 열 수 없습니다.\n");
        err = -1;
        return err;
    }

    for (int i = 0; i < tokens_length; i++) {
    if(tokens[i]->label == NULL && tokens[i]->operator == NULL && tokens[i]->operand[0] == NULL && tokens[i]->comment ) {
        fprintf(fp, "%-10s\t", tokens[i]->comment);
        fprintf(fp, "\n");
        continue;
    }

    // 레이블 출력: 최대 10자리, 왼쪽 정렬
    fprintf(fp, "%-10s\t", tokens[i]->label ? tokens[i]->label : "");

    // 연산자 출력: 최대 8자리, 왼쪽 정렬
    fprintf(fp, "%-8s\t", tokens[i]->operator ? tokens[i]->operator : "");

    // 오퍼랜드 출력
    if (tokens[i]->operand[0]) {
        fprintf(fp, "%s", tokens[i]->operand[0]);
        for (int j = 1; j < 3 && tokens[i]->operand[j]; j++) {
            fprintf(fp, ",%s", tokens[i]->operand[j]);
        }
    }

    int space_size = 0;
    if(tokens[i]->operand[0])
        space_size = strlen(tokens[i]->operand[0]);
    if(tokens[i]->operand[1])
        space_size += strlen(tokens[i]->operand[1]) + 1;

    for(int j = 0; j < 15 - space_size; j++) {
        fprintf(fp, " ");
    }

    // opcode 출력
    if (tokens[i]->operator) {
        int s_op = search_opcode(tokens[i]->operator, inst_table, inst_table_length);
        if (s_op != -1) {
            fprintf(fp, "%02X", inst_table[s_op]->op);
        }
    }
    
    fprintf(fp, "\n");
}
    return err;
}

void init_nixbpe(token *tokens, const inst **inst_table, int inst_table_length) {

if(tokens->operator && search_opcode(tokens->operator, inst_table, inst_table_length) >= 0){

    //RSUB
    if(strcmp(tokens->operator, "RSUB") == 0) {
        tokens->nixbpe |= 1 <<5;
        tokens->nixbpe |= 1 <<4;
        return;
    }

        if(tokens->operand[0]) {
            if(tokens->operand[0][0] == '@') {
                tokens->nixbpe |= 1 << 5;
            } else if(tokens->operand[0][0] == '#') {
                tokens->nixbpe |= 1 << 4;
            } else {
                tokens->nixbpe |= 1 << 5;
                tokens->nixbpe |= 1 << 4;
            }
        }

        if(tokens->operator && tokens->operator[0] == '+') {
            tokens->nixbpe |= 1 << 0;
        }
        else if (tokens->operand[0][0] != '#' && get_format(search_opcode(tokens->operator, inst_table, inst_table_length), inst_table) == 3) {
            tokens->nixbpe |= 1 << 1;
        }

        if(tokens->operand[1] && tokens->operand[1][0] == 'X') {
            tokens->nixbpe |= 1 << 3;
        }
    }
}

int assem_pass1(const inst **inst_table, int inst_table_length,
                const char **input, int input_length, token **tokens,
                int *tokens_length, symbol **symbol_table,
                int *symbol_table_length, literal **literal_table,
                int *literal_table_length) {


    *tokens_length = 0;
    int err = 0;
    // 한줄씩 토큰을 만들어 tokens에 저장
    for(int i = 0; i < input_length; i++) {
        tokens[i] = (token *)malloc(sizeof(token));
        err = token_parsing(input[i], tokens[i], inst_table, inst_table_length);
        if(err < 0) {
            fprintf(stderr, "assem_pass1: 토큰 파싱에 실패했습니다.\n");
            return err;
        }
        init_nixbpe(tokens[i], inst_table, inst_table_length);
        (*tokens_length)++;
    }

    int loc = 0;

    //token을 기반으로 symbol_table과 literal_table을 만듦
    int len = *tokens_length;
    int symbol_len = 0;

    int literal_len = 0;
    int literal_cur = 0;
    char *label = 0;
    for(int i = 0; i < len; i++) {

         if(tokens[i]->operator) {
                if(strcmp(tokens[i]->operator, "START") == 0) {
                    loc = strtol(tokens[i]->operand[0], NULL, 16);
                    label = tokens[i]->label;
                }
                else if (strcmp(tokens[i]->operator, "CSECT") == 0) {
                     for(int j = literal_cur; j < literal_len; j++) {

                    literal_table[j]->addr = loc;

                    if(literal_table[j]->literal[1] == 'C') {
                        loc += strlen(literal_table[j]->literal) - 4;
                    } else {
                        loc += (strlen(literal_table[j]->literal) - 4) / 2;
                    }

                }
                    label = tokens[i]->label;
                    loc = 0;
                }

                else if (strcmp(tokens[i]->operator, "END") == 0) {
                    for(int j = literal_cur; j < literal_len; j++) {

                    literal_table[j]->addr = loc;

                    if(literal_table[j]->literal[1] == 'C') {
                        loc += strlen(literal_table[j]->literal) - 4;
                    } else {
                        loc += (strlen(literal_table[j]->literal) - 4) / 2;
                    }
                }
                    break;
                }
        }


        if(tokens[i]->label) {
            symbol_table[symbol_len] = (symbol *)malloc(sizeof(symbol));
            if (symbol_table[symbol_len] == NULL) {
                fprintf(stderr, "assem_pass1: 메모리 할당에 실패했습니다.\n");
                exit(1);
            }
            strcpy(symbol_table[symbol_len]->name, tokens[i]->label);

            if (tokens[i]->operator && strcmp(tokens[i]->operator, "EQU") == 0) {
                if(strcmp(tokens[i]->operand[0], "*") == 0) {
                    symbol_table[symbol_len]->addr = loc;
                }
                else {
                    if(return_operator_equ(tokens[i]->operand[0]) == -1) {
                        int symbol_index = is_in_symbol_table(tokens[i]->operand[0], symbol_table, symbol_len);
                        if(symbol_index == -1) {
                            fprintf(stderr, "assem_pass1: %d번째 토큰의 EQU 연산자의 피연산자가 심볼테이블에 없습니다.\n", i);
                            return -1;
                        }
                        symbol_table[symbol_len]->addr = symbol_table[symbol_index]->addr;
                    } else{
                        char **temp = equ_split(tokens[i]->operand[0]);

                        if(strings_len(temp) > 2) {
                            fprintf(stderr, "assem_pass1: %d번째 토큰의 EQU 연산자의 피연산자가 3개 이상입니다 구현하지 않았습니다.\n", i);
                            return -1;
                        }


                        int operator_num = return_operator_equ(tokens[i]->operand[0]);
                        int symbol_index1 = is_in_symbol_table(temp[0], symbol_table, symbol_len);
                        int symbol_index2 = is_in_symbol_table(temp[1], symbol_table, symbol_len);

                        // printf("%d %d\n", symbol_index1, symbol_index2);
                        // printf("%X %X\n", symbol_table[symbol_index1]->addr, symbol_table[symbol_index2]->addr);

                        if(symbol_index1 == -1 || symbol_index2 == -1) {
                            fprintf(stderr, "assem_pass1: %d번째 토큰의 EQU 연산자의 피연산자가 심볼테이블에 없습니다.\n", i);
                            return -1;
                        }

                        if(operator_num == 1) {
                            symbol_table[symbol_len]->addr = symbol_table[symbol_index1]->addr + symbol_table[symbol_index2]->addr;
                        } else if(operator_num == 2) {
                            symbol_table[symbol_len]->addr = symbol_table[symbol_index1]->addr - symbol_table[symbol_index2]->addr;
                        } else if(operator_num == 3) {
                            symbol_table[symbol_len]->addr = symbol_table[symbol_index1]->addr * symbol_table[symbol_index2]->addr;
                        } else if(operator_num == 4) {
                            symbol_table[symbol_len]->addr = symbol_table[symbol_index1]->addr / symbol_table[symbol_index2]->addr;
                        }
                    }
                }
            }
            else {
                symbol_table[symbol_len]->addr = loc;
            }
            symbol_table[symbol_len]->label = label;
            symbol_len++;
        }


        if (tokens[i]->operator) {
            if(tokens[i]->operand[0] && starts_with(tokens[i]->operand[0], '=')) {
                if(is_in_literal_table(tokens[i]->operand[0], literal_table, literal_len) == -1) {
                    literal_table[literal_len] = (literal *)malloc(sizeof(literal));
                    if (literal_table[literal_len] == NULL) {
                        fprintf(stderr, "assem_pass1: 메모리 할당에 실패했습니다.\n");
                        exit(1);
                    }
                    strcpy(literal_table[literal_len]->literal, tokens[i]->operand[0]);
                    literal_len++;
                }
            }

            if(search_opcode(tokens[i]->operator, inst_table, inst_table_length) >= 0) {
                if(tokens[i]->operator[0] == '+') {
                    loc += 4;
                } else {
                    loc += get_format(search_opcode(tokens[i]->operator, inst_table, inst_table_length), inst_table);
                }
            }
            else if (strcmp(tokens[i]->operator, "WORD") == 0) {
                loc += 3;
            }
            else if (strcmp(tokens[i]->operator, "RESW") == 0) {
                loc += 3 * atoi(tokens[i]->operand[0]);
            }
            else if (strcmp(tokens[i]->operator, "RESB") == 0) {
                loc += atoi(tokens[i]->operand[0]);
            }
            else if (strcmp(tokens[i]->operator, "LTORG") == 0) {
                for(int j = literal_cur; j < literal_len; j++) {

                    literal_table[j]->addr = loc;

                    if(literal_table[j]->literal[1] == 'C') {
                        loc += strlen(literal_table[j]->literal) - 4;
                    } else {
                        loc += (strlen(literal_table[j]->literal) - 4) / 2;
                    }
                }
                literal_cur = literal_len;
            }
            else if (strcmp(tokens[i]->operator, "BYTE") == 0) {
                loc += 1;
            }
        }
    
    }
    *symbol_table_length = symbol_len;
    *literal_table_length = literal_len;

    return 0;
}
