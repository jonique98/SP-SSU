#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include "my_assembler.h"


// 문자열의 개수를 반환하는 함수
int strings_len(char **strings) {
    int i = 0;
    while (strings[i]) {
        i++;
    }
    return i;
}

// 단어를 split할 때 사용하는 함수
int word_count(char *str) {
    int count = 0;
    int i = 0;

    while (str[i]) {
        while (str[i] == ' ' || str[i] == '\t' || str[i] == '\n' || str[i] == '\r' || str[i] == '\f') {
            i++;
        }
        if (str[i]) {
            count++;
            while (str[i] && str[i] != ' ' && str[i] != '\t' && str[i] != '\n' && str[i] != '\r' && str[i] != '\f') {
                i++;
            }
        }
    }

    return count;
}

// operand의 개수를 세는 함수
int word_count_operands(char *str) {
    int count = 0;
    int i = 0;

    while (str[i]) {
        while (str[i] == ',') {
            i++;
        }
        if (str[i]) {
            count++;
            while (str[i] && str[i] != ',') {
                i++;
            }
        }
    }

    return count;
}

//operand가 2개 이상인 경우를 처리하기 위한 함수
char **operand_split(char *str) {
    if (str == NULL) {
        return NULL;
    }
    char **result = NULL;
    int size = word_count_operands(str) + 1;

    result = (char **)malloc(sizeof(char *) * size);
    if (result == NULL) {
        fprintf(stderr, "operand_split: 메모리 할당에 실패했습니다.\n");
        exit(1);
    }

    int i = 0;
    int j = 0;
    while (str[i]) {
        while (str[i] == ',') {
            i++;
        }
        if (str[i]) {
            int start = i;
            while (str[i] && str[i] != ',') {
                i++;
            }

            result[j] = (char *)malloc(i - start + 1);
            if (result[j] == NULL) {
                for (int k = 0; k < j; ++k) {
                    free(result[k]);
                }
                free(result);
                return NULL;
            }
            strncpy(result[j], str + start, i - start);
            result[j][i - start] = '\0';
            j++;
        }
    }

    result[j] = NULL;
    return result;
}

// 문자열을 받아서 포맷을 반환하는 함수 1=1 2=2 3/4=3
int format_change(char *str, int *err) {
    if (strcmp(str, "1") == 0) {
        return 1;
    } else if (strcmp(str, "2") == 0) {
        return 2;
    } else if (strcmp(str, "3/4") == 0) {
        return 3;
    } else {
        fprintf(stderr, "format_change: 포맷이 잘못되었습니다.\n");
        *err = -1;
        return -1;
    }
}

// 문자열을 공백 문자를 기준으로 나누는 함수
char **split(char *str, int *err) {
    char **result = NULL;
    int count = word_count(str);

    result = (char **)malloc(sizeof(char *) * (count + 1));
    if (result == NULL) {
        fprintf(stderr, "split: 메모리 할당에 실패했습니다.\n");
        exit(1);
    }

    int i = 0;
    int j = 0;
    while (str[i]) {
        while (str[i] == ' ' || str[i] == '\t' || str[i] == '\n' || str[i] == '\r' || str[i] == '\f') {
            i++; 
        }
        if (str[i]) {
            int start = i;
            while (str[i] && str[i] != ' ' && str[i] != '\t' && str[i] != '\n' && str[i] != '\r' && str[i] != '\f') {
                i++;
            }

            result[j] = (char *)malloc(i - start + 1);
            if (result[j] == NULL) {

                for (int k = 0; k < j; ++k) {
                    free(result[k]);
                }
                free(result);
                return NULL;
            }
            strncpy(result[j], str + start, i - start);
            result[j][i - start] = '\0';
            j++;
        }
    }

    result[j] = NULL;
    return result;
}

// 공백 문자인지 확인하는 함수
int is_white_space(char c) {
    return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f';
}

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

// inst_table에서 table_index에 해당하는 인덱스의 format을 반환
int get_ops(int table_index, const inst **inst_table) {
    return inst_table[table_index]->ops;
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

// comment가 있는 경우를 처리하기 위해 문장 길이를 계산하는 함수
int calc_totallen(char **temp){
    int len = strings_len(temp);
    int totalLength = 0;

    for (int i = 0; i < len; i++)
        totalLength += strlen(temp[i]) + 1;
    return totalLength;
}

int token_parsing(const char *input, token *token, const inst **inst_table, int inst_table_length) {

    token->label = NULL;
    token->operator = NULL;
    token->operand[0] = NULL;
    token->comment = NULL;

    int err ;
    char **temp = split((char *)input, &err);

    if(temp == NULL) {
        fprintf(stderr, "token_parsing: split 함수에서 에러가 발생했습니다.\n");
        return err;
    }

    err = -1;
    //라인의 길이
    int len = strings_len(temp);
    if(len == 0) {
        token->label = NULL;
        token->operator = NULL;
        token->operand[0] = NULL;
        token->comment = NULL;
        return 0;
    }

    // 라인의 첫 글자가 '.'이면 주석
    if (input[0] == '.') {
        token->label = NULL;
        token->operator = NULL;
        token->operand[0] = NULL;
        token->comment = (char *)malloc(calc_totallen(temp));
        if (token->comment == NULL) {
            fprintf(stderr, "token_parsing: 메모리 할당에 실패했습니다.\n");
            return -1;
        }
        
        for(int i = 0; i < len; i++) {
            strcat(token->comment, temp[i]);
            if(i != len - 1)
                strcat(token->comment, " ");
        }
    }
    else if (is_white_space(input[0])) {
    // 라인의 첫 글자가 공백이면 레이블이 없는 경우
            token->label = NULL;
            token->operator = temp[0];
            int opcode_index = search_opcode(temp[0], inst_table, inst_table_length);
            if (opcode_index == -1) {
                // EXTDEF, EXTREF, LTORG
                if(strcmp(token->operator, "EXTDEF") == 0 || strcmp(token->operator, "EXTREF") == 0) {
                    temp++;
                    if(!temp)
                        return err;
                    len--;
                    char **operands = operand_split(temp[0]);
                    if(operands == NULL) {
                        fprintf(stderr, "token_parsing: operand_split 함수에서 에러가 발생했습니다.\n");
                        return -1;
                    }
                    for(int i = 0; i < strings_len(operands); i++) {
                        token->operand[i] = operands[i];
                    }
                }
                else if (strcmp(token->operator, "LTORG")== 0 || strcmp(token->operator, "CSECT") == 0 || strcmp(token->operator, "START") == 0 || strcmp(token->operator, "END") == 0) {
                    token->operand[0] = NULL;
                    temp++;
                    if(!temp)
                        return 1;
                    len--;
                    if(temp[0]){
                        token->comment = (char *)malloc(calc_totallen(temp));
                        for(int i = 0; i < len; i++) {
                            strcat(token->comment, temp[i]);
                            if(i != len - 1)
                                strcat(token->comment, " ");
                        }
                    }
                    return 1;
                }
                else {
                    temp++;
                    if(!temp)
                        return 1;
                    len--;
                    fprintf(stderr, "warning: optable에 없는 명령어 %s가 들어왔습니다.\n", token->operator);
                    token->operand[0] = temp[0];
                    return 1;
                }
                temp++;
                if(!temp)
                    return 1;
                len--;
                token->comment = (char *)malloc(calc_totallen(temp));
                for(int i = 0; i < len; i++) {
                    strcat(token->comment, temp[i]);
                    if(i != len - 1)
                        strcat(token->comment, " ");
                }
                return 1;
            }
            temp++;
            if(!temp)
                return 1;
            len--;

            if (get_ops(opcode_index, inst_table) == 0) {
            // operator가 필요없는 명령어
                token->operand[0] = NULL;
                token->comment = (char *)malloc(calc_totallen(temp));
                for(int i = 0; i < len; i++) {
                    strcat(token->comment, temp[i]);
                    if(i != len - 1)
                        strcat(token->comment, " ");
                }
            }
            else if (get_ops(opcode_index, inst_table) == 1) {
            // operands가 하나 있는 경우
                if(!temp[0]){
                    fprintf(stderr, "token_parsing: operand가 없는 input %s 있습니다.\n", token->operator);
                    return -1;
                }
                token->operand[0] = temp[0];
                temp++;
                if(!temp)
                    return err;
                len--;
                token->comment = (char *)malloc(calc_totallen(temp));
                for(int i = 0; i < len; i++) {
                    strcat(token->comment, temp[i]);
                    if(i != len - 1)
                        strcat(token->comment, " ");
                }
            }
            else if (get_ops(opcode_index, inst_table) == 2) {
            // operands가 두개 있는 경우
                char **operands = operand_split(temp[0]);
                if(operands == NULL || operands[2] != NULL) {
                    fprintf(stderr, "token_parsing: operand_split 함수에서 에러가 발생했습니다.\n");
                    return -1;
                }
                token->operand[0] = operands[0];
                token->operand[1] = operands[1];
                token->operand[2] = NULL;
                temp++;
                if(!temp)
                    return err;
                len--;
                token->comment = (char *)malloc(calc_totallen(temp));
                for(int i = 0; i < len; i++) {
                    strcat(token->comment, temp[i]);
                    if(i != len - 1)
                        strcat(token->comment, " ");
                }
            }
    }
    else {
    // 라인의 첫 글자가 존재 즉 레이블이 있는 경우
        if(len == 1){
            fprintf(stderr, "token_parsing: 레이블만 존재하는 input이 있습니다.\n");
            return err;
        }
        token->label = temp[0];

        if(strcmp(temp[1], "START") == 0 || strcmp(temp[1], "END") == 0) {
            token->operator = temp[1];
            token->operand[0] = NULL;
            temp += 2;
            if(!temp)
                return err;
            len -= 2;
        }
        else if(strcmp(temp[1], "BYTE") == 0 || strcmp(temp[1], "WORD") == 0 || strcmp(temp[1], "RESB") == 0 || strcmp(temp[1], "RESW") == 0 || strcmp(temp[1], "EQU") == 0) {
            token->operator = temp[1];
            if(!temp[2]){
                fprintf(stderr, "token_parsing: operand가 없는 BYTE, WORD, RESB, RESW가 있습니다.\n");
                return err;
            }
            token->operand[0] = temp[2];
            temp += 3;
            if(!temp)
                return err;
            len -= 3;
        }
        else if(strcmp(temp[1], "EXTDEF") == 0 || strcmp(temp[1], "EXTREF") == 0) {
            token->operator = temp[1];
            if(!temp[2]){
                fprintf(stderr, "token_parsing: operand가 없는 EXTDEF, EXTREF가 있습니다.\n");
                return err;
            }
            char **operands = operand_split(temp[2]);
            if(operands == NULL) {
                fprintf(stderr, "token_parsing: operand_split 함수에서 에러가 발생했습니다.\n");
                return -1;
            }
            int i = 0;
            while (operands[i]) {
                token->operand[i] = operands[i];
                i++;
            }
            token->operand[i] = NULL;
            temp += 3;
            if(!temp)
                return err;
            len -= 3;
        }
        else if (strcmp(temp[1], "LTORG") == 0 || strcmp(temp[1], "CSECT") == 0) {
            token->operator = temp[1];
            token->operand[0] = NULL;
            temp += 2;
            if(!temp)
                return err;
            len -= 2;
        }
        else {
            token->operator = temp[1];
            int opcode_index = search_opcode(temp[1], inst_table, inst_table_length);
            temp += 2;
            if(!temp)
                return err;
            len -= 2;

            if (opcode_index == -1) {
            // opcode 테이블에 없는 경우
                fprintf(stderr, "warning: optable에 없는 명령어 %s가 들어왔습니다.\n", token->operator);
                char **operands = operand_split(temp[0]);
                if(operands == NULL) {
                    return 1;
                }
                int i = 0;
                while (operands[i]) {
                    token->operand[i] = operands[i];
                    i++;
                }
                token->operand[i] = NULL;
                temp++;
                if(!temp)
                    return err;
                len--;
                token->comment = (char *)malloc(calc_totallen(temp));
                for(int i = 0; i < len; i++) {
                    strcat(token->comment, temp[i]);
                    if(i != len - 1)
                        strcat(token->comment, " ");
                }
            }
            else if (get_ops(opcode_index, inst_table) == 0) {
            // opcode 테이블에 있고 operands가 없는 경우
                token->operand[0] = NULL;
                token->comment = (char *)malloc(calc_totallen(temp));
                for(int i = 0; i < len; i++) {
                    strcat(token->comment, temp[i]);
                    if(i != len - 1)
                        strcat(token->comment, " ");
                }
            }
            else if (get_ops(opcode_index, inst_table) == 1) {
            // opcode 테이블에 있고 operands가 하나 있는 경우
                if(!temp[0]){
                    fprintf(stderr, "token_parsing: operand가 없는 input이 있습니다.\n");
                    return err;
                }
                token->operand[0] = temp[0];
                temp++;
                if(!temp)
                    return err;
                len--;
                token->comment = (char *)malloc(calc_totallen(temp));
                for(int i = 0; i < len; i++) {
                    strcat(token->comment, temp[i]);
                    if(i != len - 1)
                        strcat(token->comment, " ");
                }
            }
            else if (get_ops(opcode_index, inst_table) == 2) {
            // opcode 테이블에 있고 operands가 두개 있는 경우
                char **operands = operand_split(temp[0]);
                if(operands == NULL) {
                    fprintf(stderr, "token_parsing: operand_split 함수에서 에러가 발생했습니다.\n");
                    return -1;
                }
                if(operands[2] != NULL) {
                    fprintf(stderr, "token_parsing: operand가 2개 이상인 operator에 operands가 없습니다.\n");
                    return err;
                }
                int i = 0;
                while (operands[i]) {
                    token->operand[i] = operands[i];
                    i++;
                }
                token->operand[i] = NULL;
                temp++;
                if(!temp)
                    return err;
                len--;
                token->comment = (char *)malloc(calc_totallen(temp));
                for(int i = 0; i < len; i++) {
                    strcat(token->comment, temp[i]);
                    if(i != len - 1)
                        strcat(token->comment, " ");
                }
            }
        }
    }

    return 0;
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
        (*tokens_length)++;
    }

    return 0;
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

int main(int argc, char **argv) {
    // SIC/XE 머신의 instruction 정보를 저장하는 테이블이다.
    inst *inst_table[MAX_INST_TABLE_LENGTH];
    int inst_table_length;

    // 소스코드를 저장하는 테이블이다. 라인 단위 저장한다.
    char *input[MAX_INPUT_LINES];
    int input_length;

    // 소스코드의 각 라인을 토큰으로 전환하여 저장한다.
    token *tokens[MAX_INPUT_LINES];
    int tokens_length;

    // 소스코드 내의 심볼을 저장하는 테이블이다. 추후 과제에 사용 예정.
    symbol *symbol_table[MAX_TABLE_LENGTH];
    int symbol_table_length;

    // 소스코드 내의 리터럴을 저장하는 테이블이다. 추후 과제에 사용 예정.
    literal *literal_table[MAX_TABLE_LENGTH];
    int literal_table_length;

    // 오브젝트 코드를 저장하는 테이블이다. 추후 과제에 사용 예정.
    char object_code[MAX_OBJECT_CODE_LENGTH][MAX_OBJECT_CODE_STRING];
    int object_code_length;

    int err = 0;

    if ((err = init_inst_table(inst_table, &inst_table_length, "inst_table.txt")) < 0) {
        fprintf(stderr, "init_inst_table: 기계어 목록 초기화에 실패했습니다. (error_code: %d)\n", err);
        return -1;
    }

    if ((err = init_input(input, &input_length, "input.txt")) < 0) {
        fprintf(stderr, "init_input: 소스코드 입력에 실패했습니다. (error_code: %d)\n", err);
        return -1;
    }

    if ((err = assem_pass1((const inst **)inst_table, inst_table_length,
                           (const char **)input, input_length, tokens,
                           &tokens_length, symbol_table, &symbol_table_length,
                           literal_table, &literal_table_length)) < 0) {
        fprintf(stderr, "assem_pass1: 패스1 과정에서 실패했습니다. (error_code: %d)\n", err);
        return -1;
    }

    if ((err = make_opcode_output("./output_20181259.txt",
                                  (const token **)tokens,
                                  tokens_length,
                                  (const inst **)inst_table,
                                  inst_table_length)) < 0) {
        fprintf(stderr, "make_opcode_output: opcode 파일 출력 과정에서 실패했습니다. (error_code: %d)\n", err);
        return -1;
    }

    // 추후 프로젝트에서 사용되는 부분
    /*
    if ((err = make_symbol_table_output("symtab_00000000", (const symbol **)symbol_table, symbol_table_length)) < 0) {
        fprintf(stderr, "make_symbol_table_output: 심볼테이블 파일 출력 과정에서 실패했습니다. (error_code: %d)\n", err);
        return -1;
    }

    if ((err = make_literal_table_output("littab_00000000", (const literal **)literal_table, literal_table_length)) < 0) {
        fprintf(stderr, "make_literal_table_output: 리터럴테이블 파일 출력 과정에서 실패했습니다. (error_code: %d)\n", err);
        return -1;
    }

    if ((err = assem_pass2((const token **)tokens, tokens_length,
                           (const symbol **)symbol_table, symbol_table_length,
                           (const literal **)literal_table, literal_table_length,
                           object_code, &object_code_length)) < 0) {
        fprintf(stderr, "assem_pass2: 패스2 과정에서 실패했습니다. (error_code: %d)\n", err);
        return -1;
    }

    if ((err = make_objectcode_output("output_00000000",
                                      (const char(*)[74])object_code,
                                      object_code_length)) < 0) {
        fprintf(stderr, "make_objectcode_output: 오브젝트코드 파일 출력 과정에서 실패했습니다. (error_code: %d)\n", err);
        return -1;
    }
    */

    return 0;
}
