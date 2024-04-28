#include "my_assembler.h"

int token_parsing(const char *input, token *token, const inst **inst_table, int inst_table_length) {

    token->label = NULL;
    token->operator = NULL;
    token->operand[0] = NULL;
    token->comment = NULL;
    token->nixbpe = 0;

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
                if (strcmp(token->operator, "START") == 0 || strcmp(token->operator, "END") == 0) {
                    token->operand[0] = temp[1];
                    temp += 2;
                    if(!temp)
                        return err;
                    len -= 2;

                    token->comment = (char *)malloc(calc_totallen(temp));
                    for(int i = 0; i < len; i++) {
                        strcat(token->comment, temp[i]);
                        if(i != len - 1)
                            strcat(token->comment, " ");
                    }
                    return 1;
                }

                if (strcmp(token->operator, "BYTE") == 0 || strcmp(token->operator, "WORD") == 0 || strcmp(token->operator, "RESB") == 0 || strcmp(token->operator, "RESW") == 0 || strcmp(token->operator, "EQU") == 0) {
                    token->operand[0] = temp[1];
                    if(!temp[2]){
                        fprintf(stderr, "token_parsing: operand가 없는 BYTE, WORD, RESB, RESW가 있습니다.\n");
                        return err;
                    }
                    temp += 2;
                    if(!temp)
                        return err;
                    len -= 2;

                    token->comment = (char *)malloc(calc_totallen(temp));
                    for(int i = 0; i < len; i++) {
                        strcat(token->comment, temp[i]);
                        if(i != len - 1)
                            strcat(token->comment, " ");
                    }
                    return 1;
                }
                // EXTDEF, EXTREF, LTORG
                else if(strcmp(token->operator, "EXTDEF") == 0 || strcmp(token->operator, "EXTREF") == 0) {
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
                else if (strcmp(token->operator, "LTORG") == 0 || strcmp(token->operator, "CSECT") == 0) {
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
                if(!temp[0]){
                    fprintf(stderr, "token_parsing: operand가 없는 input %s 있습니다.\n", token->operator);
                    return -1;
                }

                char **operands = operand_split(temp[0]);
                    if(operands == NULL) {
                        fprintf(stderr, "token_parsing: operand_split 함수에서 에러가 발생했습니다.\n");
                        return -1;
                    }
                    for(int i = 0; i < strings_len(operands); i++) {
                        token->operand[i] = operands[i];
                }
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
            if(!temp[2]){
                fprintf(stderr, "token_parsing: operand가 없는 START, END가 있습니다.\n");
                return err;
            }
            token->operand[0] = temp[2];
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
                char **operands = operand_split(temp[0]);
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