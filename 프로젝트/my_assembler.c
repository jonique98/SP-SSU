#include "my_assembler.h"


int is_in_program_literal_table(char *literal[], char *str) {
    for (int i = 0; i < 10; i++) {
        if (literal[i] && strcmp(literal[i], str) == 0)
            return i;
    }
    return -1;
}


int make_symbol_table_output(const char *symtab_dir,
                             const symbol **symbol_table,
                             int symbol_table_length) {

    FILE *fp;
    int err;

    err = 0;

    fp = fopen(symtab_dir, "w");
    if (fp == NULL) {
        fprintf(stderr, "make_symbol_table_output: 심볼테이블 파일을 열 수 없습니다.\n");
        err = -1;
        return err;
    }

    for (int i = 0; i < symbol_table_length; i++) {
        fprintf(fp, "%s\t%04X\t%s\n", symbol_table[i]->name, symbol_table[i]->addr, symbol_table[i]->label);
    }

    fclose(fp);
    return 0;
}

int make_literal_table_output(const char *literal_table_dir,
                              const literal **literal_table,
                              int literal_table_length) {

    FILE *fp;
    int err;

    err = 0;

    fp = fopen(literal_table_dir, "w");
    if (fp == NULL) {
        fprintf(stderr, "make_literal_table_output: 리터럴테이블 파일을 열 수 없습니다.\n");
        err = -1;
        return err;
    }

    for (int i = 0; i < literal_table_length; i++) {
        fprintf(fp, "%s\t%04X\n", literal_table[i]->literal, literal_table[i]->addr);
    }

    fclose(fp);
    return 0;
}

// int main(int argc, char **argv) {
//     // SIC/XE 머신의 instruction 정보를 저장하는 테이블이다.
//     inst *inst_table[MAX_INST_TABLE_LENGTH];
//     int inst_table_length;

//     // 소스코드를 저장하는 테이블이다. 라인 단위 저장한다.
//     char *input[MAX_INPUT_LINES];
//     int input_length;

//     // 소스코드의 각 라인을 토큰으로 전환하여 저장한다.
//     token *tokens[MAX_INPUT_LINES];
//     int tokens_length;

//     // 소스코드 내의 심볼을 저장하는 테이블이다. 추후 과제에 사용 예정.
//     symbol *symbol_table[MAX_TABLE_LENGTH];
//     int symbol_table_length;

//     // 소스코드 내의 리터럴을 저장하는 테이블이다. 추후 과제에 사용 예정.
//     literal *literal_table[MAX_TABLE_LENGTH];
//     int literal_table_length;

//     // 오브젝트 코드를 저장하는 테이블이다. 추후 과제에 사용 예정.
//     char object_code[MAX_OBJECT_CODE_LENGTH][MAX_OBJECT_CODE_STRING];
//     int object_code_length;

//     int err = 0;

//     if ((err = init_inst_table(inst_table, &inst_table_length, "inst_table.txt")) < 0) {
//         fprintf(stderr, "init_inst_table: 기계어 목록 초기화에 실패했습니다. (error_code: %d)\n", err);
//         return -1;
//     }

//     if ((err = init_input(input, &input_length, "input.txt")) < 0) {
//         fprintf(stderr, "init_input: 소스코드 입력에 실패했습니다. (error_code: %d)\n", err);
//         return -1;
//     }

//     if ((err = assem_pass1((const inst **)inst_table, inst_table_length,
//                            (const char **)input, input_length, tokens,
//                            &tokens_length, symbol_table, &symbol_table_length,
//                            literal_table, &literal_table_length)) < 0) {
//         fprintf(stderr, "assem_pass1: 패스1 과정에서 실패했습니다. (error_code: %d)\n", err);
//         return -1;
//     }

//     if ((err = make_opcode_output("./output_20181259.txt",
//                                   (const token **)tokens,
//                                   tokens_length,
//                                   (const inst **)inst_table,
//                                   inst_table_length)) < 0) {
//         fprintf(stderr, "make_opcode_output: opcode 파일 출력 과정에서 실패했습니다. (error_code: %d)\n", err);
//         return -1;
//     }

//     if ((err = make_symbol_table_output("symtab_20181259", (const symbol **)symbol_table, symbol_table_length)) < 0) {
//         fprintf(stderr, "make_symbol_table_output: 심볼테이블 파일 출력 과정에서 실패했습니다. (error_code: %d)\n", err);
//         return -1;
//     }

//     if ((err = make_literal_table_output("littab_20181259", (const literal **)literal_table, literal_table_length)) < 0) {
//         fprintf(stderr, "make_literal_table_output: 리터럴테이블 파일 출력 과정에서 실패했습니다. (error_code: %d)\n", err);
//         return -1;
//     }


//     return 0;
// }

unsigned int get_register_number(char *reg) {
    if (strcmp(reg, "A") == 0)
        return 0;
    else if (strcmp(reg, "X") == 0)
        return 1;
    else if (strcmp(reg, "L") == 0)
        return 2;
    else if (strcmp(reg, "B") == 0)
        return 3;
    else if (strcmp(reg, "S") == 0)
        return 4;
    else if (strcmp(reg, "T") == 0)
        return 5;
    else if (strcmp(reg, "F") == 0)
        return 6;
    else if (strcmp(reg, "PC") == 0)
        return 8;
    else if (strcmp(reg, "SW") == 0)
        return 9;
    else
        return -1;
}

int get_format_wrapper(char *operator, int table_index, const inst **inst_table) {
    if(operator[0] == '+')
        return 4;
    else
        return get_format(table_index, inst_table);
}

//총 8비트 2진수로 변환해주는 함수 뒤에 6비트를 nixbpe로 사용함
char *convert_binary(char nixbpe) {
    char *binary = (char *)malloc(sizeof(char) * 9);
    int i = 0;
    for (i = 0; i < 8; i++) {
        binary[i] = '0';
    }
    binary[i] = '\0';
    i = 7;
    while (nixbpe > 0) {
        binary[i] = (nixbpe % 2) + '0';
        nixbpe /= 2;
        i--;
    }
    return binary;
}

int find_from_token_operator(char *operator, const token *tokens[], int tokens_length) {
    for (int i = 0; i < tokens_length; i++) {
        if (tokens[i]->operator && strcmp(operator, tokens[i]->operator) == 0) {
            return i;
        }
    }
    return -1;
}

int find_from_token_label(char *label, const token *tokens[], int tokens_length) {
    for (int i = 0; i < tokens_length; i++) {
        if (tokens[i]->label && strcmp(label, tokens[i]->label) == 0) {
            return i;
        }
    }
    return -1;
}

int find_from_token_operand(char *operand, const token *tokens[], int tokens_length) {
    for (int i = 0; i < tokens_length; i++) {
        if (tokens[i]->operand[0] && strcmp(operand, tokens[i]->operand[0]) == 0) {
            return i;
        }
    }
    return -1;
}

int calculate_pc(int loc, char *operator) {
    if (strcmp(operator, "RESW") == 0)
        return loc + 3 * atoi(operator);
    else if (strcmp(operator, "RESB") == 0)
        return loc + atoi(operator);
    else if (strcmp(operator, "BYTE") == 0)
        return loc + 1;
    else if (strcmp(operator, "WORD") == 0)
        return loc + 3;
    else if (operator[0] == '+')
        return loc + 4;
    else
        return loc + 3;
}

int is_in_extref(char *symbol, extref *extref_table, int extref_table_length) {
    for (int i = 0; i < extref_table_length; i++) {
        if (strcmp(symbol, extref_table[i].symbol) == 0)
            return i;
    }
    return -1;
}

// 앞의 c 혹은 x를 제외하고 ' 사이에 있 제외한 리터럴을 찾아서 리터럴만 반환해주는 함수
char *get_literal(char *operand) {
    char *literal = (char *)malloc(sizeof(char) * 20);

    int j = 0;
    int i = 3;
    while (operand[i] != '\'') {
        literal[j] = operand[i];
        i++;
        j++;
    }
    return literal;
}


int init_main_program(int *assem_index,
                        object_code *obj_code,
                        const token *tokens[], int tokens_length,
                        const inst *inst_table[], int inst_table_length,
                        const symbol *symbol_table[], int symbol_table_length,
                        const literal *literal_table[], int literal_table_length) {

    int start_index = find_from_token_operator("START", tokens, tokens_length);
    if (start_index == -1) {
        fprintf(stderr, "init_main_program: START가 없습니다.\n");
        return -1;
    }
    char *start_symbol = tokens[start_index]->label;
    int start_addr = symbol_table[is_in_symbol_table(start_symbol, (symbol **)symbol_table, symbol_table_length)]->addr;

    obj_code->main_program.header_record.header_symbol = start_symbol;
    obj_code->main_program.header_record.start_address = start_addr;

    int end_index = find_from_token_operator("END", tokens, tokens_length);
    if (end_index == -1) {
        fprintf(stderr, "init_main_program: END가 없습니다.\n");
        return -1;
    }

    obj_code_data *main = &(obj_code->main_program);


    int text_index = 0;
    int text_length = 0;

    int text_table_index = 0;

    int text_loc = start_addr;
    int pc = start_addr;

    int modification_index = 0;
    int literal_length = 0;

    while (text_index < tokens_length) {

        main->text[text_table_index].loc = text_loc;

         if(tokens[text_index]->operator == NULL) {
            text_index++;
            continue;
        }

        unsigned int assembled_code = 0;

        if (strcmp(tokens[text_index]->operator, "END") == 0){
            //남은 리터럴 처리 밑에 참고해서
            for (int i = 0; i < literal_length; i++) {
                int leteral_len = strlen(main->literals[i]);
                int temp_assem_code = 0;

                //리터럴을 한글자씩 16진수로 변환해서 assembled_code에 저장
                // 예를들어 EOF는 454F46이 된다.
                for (int j = 0; j < leteral_len; j++) {
                    temp_assem_code = (temp_assem_code << 8) + main->literals[i][j];
                }
                main->text[text_table_index].text = temp_assem_code;
                main->text[text_table_index].loc = text_loc;
                main->text[text_table_index].operator = "LITERAL";
                text_table_index++;
                text_loc += leteral_len;
            }
            break;


        }
        if (strcmp(tokens[text_index]->operator, "CSECT") == 0)
            break;

        if (strcmp(tokens[text_index]->operator, "START") == 0 || strcmp(tokens[text_index]->operator, "EQU") == 0){
            text_index++;
            continue;
        }

        if (strcmp(tokens[text_index]->operator, "EXTREF") == 0) {
            int extref_index = 0;
            while(extref_index < 3 && tokens[text_index]->operand[extref_index]) {
                main->extref_table[extref_index].symbol = tokens[text_index]->operand[extref_index];
                main->extref_table[extref_index].addr = 0;
                extref_index++;
            }
            text_index++;
            continue;
        }
        if (strcmp(tokens[text_index]->operator, "EXTDEF") == 0) {
            int extdef_index = 0;
            int operand_index = 0;

            while (operand_index < 3 && tokens[text_index]->operand[operand_index]) {
                main->extdef[extdef_index].symbol = tokens[text_index]->operand[operand_index];
                int index = is_in_symbol_table_wrapper(tokens[text_index]->operand[extdef_index], main->header_record.header_symbol, (symbol **)symbol_table, symbol_table_length);
                main->extdef[extdef_index].addr = symbol_table[index]->addr;
                extdef_index++;
                operand_index++;
            }
            text_index++;
            main->extdef_length = extdef_index;
            continue ;
        }

         if (tokens[text_index]->operand[0] && tokens[text_index]->operand[0][0] == '='){
            if(is_in_program_literal_table(main->literals, tokens[text_index]->operand[0]) == -1){
                main->literals[literal_length] = get_literal(tokens[text_index]->operand[0]);
                literal_length++;
            }
        }

          if (strcmp(tokens[text_index]->operator, "LTORG") == 0) {
            for (int i = 0; i < literal_length; i++) {
                int leteral_len = strlen(main->literals[i]);
                int temp_assem_code = 0;

                //리터럴을 한글자씩 16진수로 변환해서 assembled_code에 저장
                // 예를들어 EOF는 454F46이 된다.
                for (int j = 0; j < leteral_len; j++) {
                    temp_assem_code = (temp_assem_code << 8) + main->literals[i][j];
                }
                main->text[text_table_index].text = temp_assem_code;
                main->text[text_table_index].loc = text_loc;
                main->text[text_table_index].operator = tokens[text_index]->operator;
                text_table_index++;
                text_loc += strlen(main->literals[i]);
            }
            literal_length = 0;
            text_index++;
            continue;
        }

        pc = calculate_pc(text_loc, tokens[text_index]->operator);

        if (strcmp(tokens[text_index]->operator, "RESW") == 0){
            text_loc += 3 * atoi(tokens[text_index]->operand[0]);
        }
        else if (strcmp(tokens[text_index]->operator, "RESB") == 0){
            text_loc += atoi(tokens[text_index]->operand[0]);
        }
        else if (strcmp(tokens[text_index]->operator, "BYTE") == 0){
            if (tokens[text_index]->operand[0][0] == 'C') {
                int i = 2;
                while(tokens[text_index]->operand[0][i] != '\'') {
                    assembled_code = (assembled_code << 8) + tokens[text_index]->operand[0][i];
                    i++;
                }
            }
            else if (tokens[text_index]->operand[0][0] == 'X') {
                int i = 2;
                while(tokens[text_index]->operand[0][i] != '\'') {
                    int temp = 0;
                    if(tokens[text_index]->operand[0][i] >= '0' && tokens[text_index]->operand[0][i] <= '9')
                        temp = tokens[text_index]->operand[0][i] - '0';
                    else if(tokens[text_index]->operand[0][i] >= 'A' && tokens[text_index]->operand[0][i] <= 'F')
                        temp = tokens[text_index]->operand[0][i] - 'A' + 10;
                    assembled_code = (assembled_code << 4) + temp;
                    i++;
                }
            }
            else {
                fprintf(stderr, "init_main_program: BYTE 타입이 잘못되었습니다.\n");
                return -1;
            }
            text_loc += 1;
        }
        else if (strcmp(tokens[text_index]->operator, "WORD") == 0){

            char **temp_operand = equ_split(tokens[text_index]->operand[0]);
            if(is_in_extref(temp_operand[0], main->extref_table, 3) != -1) {


                if(strings_len(temp_operand) == 2) {
                    int operator_equ = return_operator_equ(tokens[text_index]->operand[0]);

                   for(int i = 0; i < 2; i++){
                    char *temp = temp_operand[i];
                    if(i == 2){
                        if(operator_equ == 1){
                            char *temp2 = (char *)malloc(sizeof(char) * 10);
                            temp2[0] = '+';
                            strcat(temp2, temp_operand[i]);
                            temp = temp2;
                        }
                        else if(operator_equ == 2){
                            char *temp2 = (char *)malloc(sizeof(char) * 10);
                            temp2[0] = '-';
                            strcat(temp2, temp_operand[i]);
                            temp = temp2;
                        }
                        else if(operator_equ == 3){
                            char *temp2 = (char *)malloc(sizeof(char) * 10);
                            temp2[0] = '*';
                            strcat(temp2, temp_operand[i]);
                            temp = temp2;
                        }
                        else if(operator_equ == 4){
                            char *temp2 = (char *)malloc(sizeof(char) * 10);
                            temp2[0] = '/';
                            strcat(temp2, temp_operand[i]);
                            temp = temp2;
                        }
                    }
                    else{
                          char *temp2 = (char *)malloc(sizeof(char) * 10);
                            temp2[0] = '+';
                            strcat(temp2, temp_operand[i]);
                            temp = temp2;

                    }
                        main->modification_table[modification_index].addr = text_loc;
                        main->modification_table[modification_index].modification_length = 6;
                        main->modification_table[modification_index].symbol = temp;
                        modification_index++;
                   } 
                }
                else {             

                    //+기호 붙이기
                    char *temp = tokens[text_index]->operand[0];
                    char *temp2 = (char *)malloc(sizeof(char) * 10);
                    temp2[0] = '+';
                    strcat(temp2, temp);
                    temp = temp2;
    
                //modification record
                    main->modification_table[modification_index].addr = text_loc;
                    main->modification_table[modification_index].modification_length = 6;
                    main->modification_table[modification_index].symbol = temp;
                    modification_index++;
                }

                assembled_code = 0;
            }
            else {
                int word_symbol = is_in_symbol_table_wrapper(tokens[text_index]->operand[0], main->header_record.header_symbol, (symbol **)symbol_table, symbol_table_length);
                if (word_symbol != -1)
                    assembled_code = symbol_table[word_symbol]->addr;
                else
                    assembled_code = atoi(tokens[text_index]->operand[0]);
            }
            text_loc += 3;
        }
        else {
            int inst_index = search_opcode(tokens[text_index]->operator, inst_table, inst_table_length);
            if (inst_index == -1) {
                fprintf(stderr, "init_main_program: 명령어가 잘못되었습니다.\n");
                return -1;
            }
            assembled_code = inst_table[inst_index]->op;
            if (get_format_wrapper(tokens[text_index]->operator, inst_index, inst_table) == 2) {
                assembled_code = assembled_code << 8;
                if (tokens[text_index]->operand[0]) {
                    int reg1 = get_register_number(tokens[text_index]->operand[0]);
                    assembled_code += reg1 << 4;
                }
                if (tokens[text_index]->operand[1]) {
                    int reg2 = get_register_number(tokens[text_index]->operand[1]);
                    assembled_code += reg2;
                }
                text_loc += 2;
            }
            else if (get_format_wrapper(tokens[text_index]->operator, inst_index, inst_table) == 3) {
                assembled_code = assembled_code << 16;
                assembled_code |= tokens[text_index]->nixbpe << 12;
                if (tokens[text_index]->operand[0]) {

                    char *operand  = tokens[text_index]->operand[0];
                    if (tokens[text_index]->operand[0][0] == '#')
                        assembled_code += atoi(operand+1);
                    else if (is_in_symbol_table_wrapper(operand, main->header_record.header_symbol,(symbol **)symbol_table, symbol_table_length) != -1) {
                        int operand_symbol = is_in_symbol_table_wrapper(tokens[text_index]->operand[0], main->header_record.header_symbol, (symbol **)symbol_table, symbol_table_length);
                        assembled_code += (symbol_table[operand_symbol]->addr - pc) & 0xFFF;
                    }
                    else if (is_in_literal_table(operand, (literal **)literal_table, literal_table_length) != -1) {
                        int operand_literal = is_in_literal_table(tokens[text_index]->operand[0], (literal **)literal_table, literal_table_length);
                        assembled_code += (literal_table[operand_literal]->addr - pc);
                    }
                    else {
                        assembled_code += atoi(tokens[text_index]->operand[0]);
                    }
                }
                text_loc += 3;
            }
            else if (get_format_wrapper(tokens[text_index]->operator, inst_index, inst_table) == 4) {
                assembled_code = assembled_code << 24;
                assembled_code |= tokens[text_index]->nixbpe << 20;
                if (tokens[text_index]->operand[0]) {

                    if (is_in_extref(tokens[text_index]->operand[0], main->extref_table, 3) != -1) {

                        char *temp = tokens[text_index]->operand[0];
                        char *temp2 = (char *)malloc(sizeof(char) * 10);
                        temp2[0] = '+';
                        strcat(temp2, temp);
                        temp = temp2;

                        //modification record
                        main->modification_table[modification_index].addr = text_loc + 1;
                        main->modification_table[modification_index].modification_length = 5;
                        main->modification_table[modification_index].symbol = temp;
                        modification_index++;

                        assembled_code += 0;
                    }
                    else{
                        int operand_symbol = is_in_symbol_table_wrapper(tokens[text_index]->operand[0],main->header_record.header_symbol, (symbol **)symbol_table, symbol_table_length);
                        if (operand_symbol != -1) {
                            assembled_code |= symbol_table[operand_symbol]->addr & 0xFFFFF;
                        }
                        else {
                            assembled_code += atoi(tokens[text_index]->operand[0]);
                        }
                    }
                }
                text_loc += 4;
            }
        }
        main->text[text_table_index].text = assembled_code;
        main->text[text_table_index].operator = tokens[text_index]->operator;
        text_table_index++;
        // print operator and nixbpe as binary
        // printf("%s %s\n", tokens[text_index]->operator, convert_binary(tokens[text_index]->nixbpe));
        text_index++;
    }

    *assem_index = text_index;
    main->text_length = text_table_index;
    main->modification_table_length = modification_index;
    main->header_record.program_length = text_loc - start_addr;


    return 0;

}



int init_subroutine(int *assem_index,
                        object_code *obj_code,
                        const token *tokens[], int tokens_length,
                        const inst *inst_table[], int inst_table_length,
                        const symbol *symbol_table[], int symbol_table_length,
                        const literal *literal_table[], int literal_table_length,
                        int subroutine_count) {

    char *csect_symbol = tokens[*assem_index]->label;
    int csect_addr = symbol_table[is_in_symbol_table(csect_symbol,(symbol **)symbol_table, symbol_table_length)]->addr;

    obj_code->subroutine[subroutine_count].header_record.header_symbol = csect_symbol;
    obj_code->subroutine[subroutine_count].header_record.start_address = csect_addr;

    int end_index = find_from_token_operator("END", tokens, tokens_length);
    if (end_index == -1) {
        fprintf(stderr, "init_subroutine: END가 없습니다.\n");
        return -1;
    }
    char *end_symbol = tokens[end_index]->operand[0];

    obj_code_data *subroutine = &(obj_code->subroutine[subroutine_count]);


    *assem_index = *assem_index + 1;
    int text_index = *assem_index;
    int text_length = 0;

    int text_table_index = 0;

    int text_loc = csect_addr;
    int pc = csect_addr;

    int modification_index = 0;
    int literal_length = 0;

    while (text_index < tokens_length) {

        subroutine->text[text_table_index].loc = text_loc;

        if(tokens[text_index]->operator == NULL) {
            text_index++;
            continue;
        }

        unsigned int assembled_code = 0;
        
        if(strcmp(tokens[text_index]->operator, "END") == 0)
        {
            //남은 리터럴 할당
            for (int i = 0; i < literal_length; i++) {
                //만약 앞 글자가 C라면 리터럴을 한글자씩 16진수로 변환해서 assembled_code에 저장
                char *temp_literal = get_literal(subroutine->literals[i]);
                // 예를들어 EOF는 454F46이 된다.
                if(subroutine->literals[i][1] == 'C') {
                    int literal_len = strlen(temp_literal);
                    int temp_assem_code = 0;
                    for (int j = 0; j < literal_len; j++) {
                        temp_assem_code = (temp_assem_code << 8) + temp_literal[j];
                    }
                    subroutine->text[text_table_index].text = temp_assem_code;
                    subroutine->text[text_table_index].loc = text_loc;
                    subroutine->text[text_table_index].operator = "LITERAL";
                    text_table_index++;
                    text_loc += literal_len;
                }
                else if (subroutine->literals[i][1] == 'X') {
                    int literal_len = strlen(temp_literal);
                    int temp_assem_code = 0;
                    for (int j = 0; j < literal_len; j++) {
                        int temp = 0;
                        if(temp_literal[j] >= '0' && temp_literal[j] <= '9')
                            temp = temp_literal[j] - '0';
                        else if(temp_literal[j] >= 'A' && temp_literal[j] <= 'F')
                            temp = temp_literal[j] - 'A' + 10;
                        temp_assem_code = (temp_assem_code << 4) + temp;
                    }
                    subroutine->text[text_table_index].text = temp_assem_code;
                    subroutine->text[text_table_index].loc = text_loc;
                    subroutine->text[text_table_index].operator = "LITERAL";
                    text_table_index++;
                    text_loc += literal_len / 2;
                }
            }
            break;
        }
        if (strcmp(tokens[text_index]->operator, "CSECT") == 0)
            break;

        if (strcmp(tokens[text_index]->operator, "START") == 0 || strcmp(tokens[text_index]->operator, "EQU") == 0){
            text_index++;
            continue;
        }

        if (strcmp(tokens[text_index]->operator, "EXTREF") == 0) { 
            int extref_index = 0;
            while(extref_index < 3 && tokens[text_index]->operand[extref_index]) {
                subroutine->extref_table[extref_index].symbol = tokens[text_index]->operand[extref_index];
                subroutine->extref_table[extref_index].addr = 0;
                extref_index++;
            }
            text_index++;
            continue;
        }
        if (strcmp(tokens[text_index]->operator, "EXTDEF") == 0) {
            int extdef_index = 0;
            int operand_index = 0;
            while(operand_index < 3 && tokens[text_index]->operand[operand_index]) {
                subroutine->extdef[extdef_index].symbol = tokens[text_index]->operand[operand_index];
                int index = is_in_symbol_table_wrapper(tokens[text_index]->operand[extdef_index], subroutine->header_record.header_symbol, (symbol **)symbol_table, symbol_table_length);
                subroutine->extdef[extdef_index].addr = symbol_table[index]->addr;
                extdef_index++;
                operand_index++;
            }
            text_index++;
            subroutine->extdef_length = extdef_index;
            continue ;
        }

        if (tokens[text_index]->operand[0] && tokens[text_index]->operand[0][0] == '='){
            if(is_in_program_literal_table(subroutine->literals, tokens[text_index]->operand[0]) == -1){
                subroutine->literals[literal_length] = tokens[text_index]->operand[0];
                literal_length++;
            }
        }

        

        if (strcmp(tokens[text_index]->operator, "LTORG") == 0) {
             for (int i = 0; i < literal_length; i++) {
                //만약 앞 글자가 C라면 리터럴을 한글자씩 16진수로 변환해서 assembled_code에 저장
                char *temp_literal = get_literal(subroutine->literals[i]);
                // 예를들어 EOF는 454F46이 된다.
                if(subroutine->literals[i][1] == 'C') {
                    int literal_len = strlen(temp_literal);
                    int temp_assem_code = 0;
                    for (int j = 0; j < literal_len; j++) {
                        temp_assem_code = (temp_assem_code << 8) + temp_literal[j];
                    }
                    subroutine->text[text_table_index].text = temp_assem_code;
                    subroutine->text[text_table_index].loc = text_loc;
                    subroutine->text[text_table_index].operator = "LITERAL";
                    text_table_index++;
                    text_loc += literal_len;
                }
                else if (subroutine->literals[i][1] == 'X') {
                    int literal_len = strlen(temp_literal);
                    int temp_assem_code = 0;
                    for (int j = 0; j < literal_len; j++) {
                        int temp = 0;
                        if(temp_literal[j] >= '0' && temp_literal[j] <= '9')
                            temp = temp_literal[j] - '0';
                        else if(temp_literal[j] >= 'A' && temp_literal[j] <= 'F')
                            temp = temp_literal[j] - 'A' + 10;
                        temp_assem_code = (temp_assem_code << 4) + temp;
                    }
                    subroutine->text[text_table_index].text = temp_assem_code;
                    subroutine->text[text_table_index].loc = text_loc;
                    subroutine->text[text_table_index].operator = "LITERAL";
                    text_table_index++;
                    text_loc += literal_len / 2;
                }
            }
            continue;
        }

        pc = calculate_pc(text_loc, tokens[text_index]->operator);

        if (strcmp(tokens[text_index]->operator, "RESW") == 0){
            text_loc += 3 * atoi(tokens[text_index]->operand[0]);
        }
        else if (strcmp(tokens[text_index]->operator, "RESB") == 0){
            text_loc += atoi(tokens[text_index]->operand[0]);
        }
        else if (strcmp(tokens[text_index]->operator, "BYTE") == 0){
            if (tokens[text_index]->operand[0][0] == 'C') {
                int i = 2;
                while(tokens[text_index]->operand[0][i] != '\'') {
                    assembled_code = (assembled_code << 8) + tokens[text_index]->operand[0][i];
                    i++;
                }
            }
            //x 일떄는 그대로 저장 예를들어 x'F1'이면 16진수로 출력했을 때 그대로 F1로 나와야함
            else if (tokens[text_index]->operand[0][0] == 'X') {
                int i = 2;
                while(tokens[text_index]->operand[0][i] != '\'') {
                    int temp = 0;
                    if(tokens[text_index]->operand[0][i] >= '0' && tokens[text_index]->operand[0][i] <= '9')
                        temp = tokens[text_index]->operand[0][i] - '0';
                    else if(tokens[text_index]->operand[0][i] >= 'A' && tokens[text_index]->operand[0][i] <= 'F')
                        temp = tokens[text_index]->operand[0][i] - 'A' + 10;
                    assembled_code = (assembled_code << 4) + temp;
                    i++;
                }
            }
            else {
                fprintf(stderr, "init_subroutine: BYTE 타입이 잘못되었습니다.\n");
                return -1;
            }
            text_loc += 1;
        }
        else if (strcmp(tokens[text_index]->operator, "WORD") == 0){
            char **temp_operand = equ_split(tokens[text_index]->operand[0]);
            if(is_in_extref(temp_operand[0], subroutine->extref_table, 3) != -1) {

                if(strings_len(temp_operand) == 2) {
                    int operator_equ = return_operator_equ(tokens[text_index]->operand[0]);
                   for(int i = 0; i < 2; i++){
                        char *temp = temp_operand[i];
                        if(i == 1) {
                            if (operator_equ == 1) {
                                //연산자를 심볼 앞에 더해줘야함 예를 들어 +buffer
                                char *temp2 = (char *)malloc(sizeof(char) * 10);
                                temp2[0] = '+';
                                strcat(temp2, temp);
                                temp = temp2;
                            }
                            else if (operator_equ == 2) {
                                //연산자를 심볼 뒤에 더해줘야함 예를 들어 -buffer
                                char *temp2 = (char *)malloc(sizeof(char) * 10);
                                temp2[0] = '-';
                                strcat(temp2, temp);
                                temp = temp2;
                            }
                            else if (operator_equ == 3) {
                                //연산자를 심볼 앞에 더해줘야함 예를 들어 *buffer
                                char *temp2 = (char *)malloc(sizeof(char) * 10);
                                temp2[0] = '*';
                                strcat(temp2, temp);
                                temp = temp2;
                            }
                            else if (operator_equ == 4) {
                                //연산자를 심볼 앞에 더해줘야함 예를 들어 /buffer
                                char *temp2 = (char *)malloc(sizeof(char) * 10);
                                temp2[0] = '/';
                                strcat(temp2, temp);
                                temp = temp2;
                            }
                        }
                        else{
                              char *temp2 = (char *)malloc(sizeof(char) * 10);
                                temp2[0] = '+';
                                strcat(temp2, temp);
                                temp = temp2;
                        }
                        subroutine->modification_table[modification_index].addr = text_loc;
                        subroutine->modification_table[modification_index].modification_length = 6;
                        subroutine->modification_table[modification_index].symbol = temp;
                        modification_index++;
                   } 
                }
                else {             
                        //+기호 붙이기
                    char *temp = tokens[text_index]->operand[0];
                    char *temp2 = (char *)malloc(sizeof(char) * 10);
                    temp2[0] = '+';
                    strcat(temp2, temp);
                    temp = temp2;
    
                //modification record
                    subroutine->modification_table[modification_index].addr = text_loc;
                    subroutine->modification_table[modification_index].modification_length = 6;
                    subroutine->modification_table[modification_index].symbol = temp;
                    modification_index++;
                }

                assembled_code = 0;

            }
            else {
                int word_symbol = is_in_symbol_table_wrapper(tokens[text_index]->operand[0], subroutine->header_record.header_symbol, (symbol **)symbol_table, symbol_table_length);
                if (word_symbol != -1)
                    assembled_code = symbol_table[word_symbol]->addr;
                else
                    assembled_code = atoi(tokens[text_index]->operand[0]);
            }

            text_loc += 3;

        }
        else {
            int inst_index = search_opcode(tokens[text_index]->operator, inst_table, inst_table_length);
            if (inst_index == -1) {
                fprintf(stderr, "init_subroutine: 명령어가 잘못되었습니다.\n");
                return -1;
            }
            assembled_code = inst_table[inst_index]->op;
            if (get_format_wrapper(tokens[text_index]->operator, inst_index, inst_table) == 2) {
                assembled_code = assembled_code << 8;
                if (tokens[text_index]->operand[0]) {
                    int reg1 = get_register_number(tokens[text_index]->operand[0]);
                    assembled_code += reg1 << 4;
                }
                if (tokens[text_index]->operand[1]) {
                    int reg2 = get_register_number(tokens[text_index]->operand[1]);
                    assembled_code += reg2;
                }
                text_loc += 2;
            }
            else if (get_format_wrapper(tokens[text_index]->operator, inst_index, inst_table) == 3) {
                assembled_code = assembled_code << 16;
                assembled_code |= tokens[text_index]->nixbpe << 12;
                if (tokens[text_index]->operand[0]) {

                    char *operand  = tokens[text_index]->operand[0];
                    if (tokens[text_index]->operand[0][0] == '#')
                        assembled_code += atoi(operand+1);
                    else if (is_in_symbol_table_wrapper(operand, subroutine->header_record.header_symbol, (symbol **)symbol_table, symbol_table_length) != -1) {
                        int operand_symbol = is_in_symbol_table_wrapper(tokens[text_index]->operand[0], subroutine->header_record.header_symbol,(symbol **)symbol_table, symbol_table_length);
                        assembled_code += (symbol_table[operand_symbol]->addr - pc) & 0xFFF;
                    }
                    else if (is_in_literal_table(operand, (literal **)literal_table, literal_table_length) != -1) {
                        int operand_literal = is_in_literal_table(tokens[text_index]->operand[0], (literal **)literal_table, literal_table_length);
                        assembled_code += (literal_table[operand_literal]->addr - pc);
                    }
                    else {
                        assembled_code += atoi(tokens[text_index]->operand[0]);
                    }
                }
                text_loc += 3;
            }
            else if (get_format_wrapper(tokens[text_index]->operator, inst_index, inst_table) == 4) {
                assembled_code = assembled_code << 24;

                assembled_code |= tokens[text_index]->nixbpe << 20;

                if (tokens[text_index]->operand[0]) {

                    if (is_in_extref(tokens[text_index]->operand[0], subroutine->extref_table, 3) != -1) {

                        char *temp = tokens[text_index]->operand[0];
                        char *temp2 = (char *)malloc(sizeof(char) * 10);
                        temp2[0] = '+';
                        strcat(temp2, temp);
                        temp = temp2;

                        //modification record
                        subroutine->modification_table[modification_index].addr = text_loc + 1;
                        subroutine->modification_table[modification_index].modification_length = 5;
                        subroutine->modification_table[modification_index].symbol = temp;
                        modification_index++;

                        assembled_code += 0;
                    }
                    else{
                        int operand_symbol = is_in_symbol_table_wrapper(tokens[text_index]->operand[0], subroutine->header_record.header_symbol,(symbol **)symbol_table, symbol_table_length);
                        if (operand_symbol != -1) {
                            assembled_code |= symbol_table[operand_symbol]->addr & 0xFFFFF;
                        }
                        else {
                            assembled_code += atoi(tokens[text_index]->operand[0]);
                        }
                    }
                }

                text_loc += 4;

            }
        }
        subroutine->text[text_table_index].text = assembled_code;
        subroutine->text[text_table_index].operator = tokens[text_index]->operator;
        text_table_index++;

        // print operator and nixbpe as binary
        // printf("%s %s\n", tokens[text_index]->operator, convert_binary(tokens[text_index]->nixbpe));
        text_index++;
    }

    *assem_index = text_index;
    subroutine->text_length = text_table_index;
    subroutine->modification_table_length = modification_index;
    subroutine->header_record.program_length = text_loc - csect_addr;

    return 0;

}


int assem_pass2(const token *tokens[], int tokens_length,
                const inst *inst_table[], int inst_table_length,
                const symbol *symbol_table[], int symbol_table_length,
                const literal *literal_table[], int literal_table_length,
                object_code *obj_code) 
{

    int assem_index = 0;
    int subroutine_count = 0;

    while(assem_index < tokens_length) {
        while(assem_index < tokens_length &&
            (strcmp(tokens[assem_index]->operator, "START") != 0 && 
            strcmp(tokens[assem_index]->operator, "CSECT") != 0))
            assem_index++;
        
        if(assem_index >= tokens_length)
            break;
    
        if (strcmp(tokens[assem_index]->operator, "START") == 0){
            init_main_program(&assem_index, obj_code, tokens, tokens_length, inst_table, inst_table_length, symbol_table, symbol_table_length, literal_table, literal_table_length);
        }
        else if (strcmp(tokens[assem_index]->operator, "CSECT") == 0) {
            init_subroutine(&assem_index, obj_code, tokens, tokens_length, inst_table, inst_table_length, symbol_table, symbol_table_length, literal_table, literal_table_length, subroutine_count);
            subroutine_count++;
        }
    }

    obj_code->subroutine_length = subroutine_count;

    return 0;
}

int calculate_text_length(const obj_code_data *data, int cur_len) {
    int len = cur_len;
    int start_addr = data->text[cur_len].loc;
    int temp_addr = 0;

    if(strcmp(data->text[len].operator, "LTORG") == 0){
        while( len < data->text_length && strcmp(data->text[len].operator, "LTORG") == 0)
            len++;
        return len;
    }

    while (data->text[len].text == 0)
            {
                len++;
                if(len == data->text_length)
                    break;
            }
    if(len == data->text_length)
        return len;

    while(len < data->text_length) {
        if (
            strcmp(data->text[len].operator, "CSECT") == 0 ||
            strcmp(data->text[len].operator, "LTORG") == 0 ||
            (strcmp(data->text[len].operator, "WORD") != 0 && data->text[len].text == 0) )
            break;
        temp_addr = data->text[len].loc;
        if(temp_addr - start_addr >= 29){
            break;
        }
        len++;
    }
    return len;
}

int calculate_print_section(const obj_code_data *data, int index) {
    while( data->text[index].operator != NULL &&
            (strcmp(data->text->operator, "END") == 0 ||
                strcmp(data->text->operator, "RESB") == 0 ||
            strcmp(data->text->operator, "RESW") == 0 ||
            strcmp(data->text->operator, "EQU") == 0)) {
        if(index == data->text_length)
            break;
        index++;
    }
    return index;
}

int make_objectcode_output(const char *filename, const object_code *obj_code) {
    FILE *fp = fopen(filename, "w");
    if (fp == NULL) {
        fprintf(stderr, "make_object_code_output: 파일을 열 수 없습니다.\n");
        return -1;
    }

    fprintf(fp, "H%-6s%06X%06X\n", obj_code->main_program.header_record.header_symbol, obj_code->main_program.header_record.start_address, obj_code->main_program.header_record.program_length);
    //extdef 출력 맨 처음에만 알파벳 D 출력하고 심볼 주소값 출력 줄바꿈 없이 출력

    if(obj_code->main_program.extdef_length != 0){
        fprintf(fp, "D");
        for(int i = 0; i < obj_code->main_program.extdef_length; i++) {
            fprintf(fp, "%-6s%06X", obj_code->main_program.extdef[i].symbol, obj_code->main_program.extdef[i].addr);
        }
        fprintf(fp, "\n");
    }
    
    //extref 출력 맨 처음에만 알파벳 R 출력하고 심볼 주소값 출력 줄바꿈 없이 출력
    if(obj_code->main_program.extref_table[0].symbol != NULL){
        fprintf(fp, "R");
        for(int i = 0; i < 3; i++) {
            if(obj_code->main_program.extref_table[i].symbol != NULL)
                fprintf(fp, "%-6s", obj_code->main_program.extref_table[i].symbol);
        }
        fprintf(fp, "\n");
    }


    //맨 앞에 T를 쓰고 해당 텍스트의 시작 주소와 줄바꿈 전까지의 길이 출력함 길이는 최대 16진수 1D까지 넘어가면 줄바꿈 해야함
    // 그러므로 미리 길이를 체크해야함
    int prev_text_index = 0;
    int cur_text_index = 0;

    int test = 0;
    while(prev_text_index < obj_code->main_program.text_length) {
        cur_text_index = calculate_text_length(&(obj_code->main_program), prev_text_index);

        if(cur_text_index == prev_text_index)
            break;


        if(strcmp(obj_code->main_program.text[prev_text_index].operator, "RESW") == 0
            || strcmp(obj_code->main_program.text[prev_text_index].operator, "RESB") == 0
            || strcmp(obj_code->main_program.text[prev_text_index].operator, "EQU") == 0){
            prev_text_index = calculate_print_section(&(obj_code->main_program), cur_text_index);
            continue;

        }


        unsigned int text_start_addr = obj_code->main_program.text[prev_text_index].loc;
        unsigned int text_len = obj_code->main_program.text[cur_text_index].loc - text_start_addr;

        //T와 함계 시작주소와 텍스트 길이 16진수로 출력
        fprintf(fp, "T%06X%02X", text_start_addr, text_len);


        for(int i = prev_text_index; i < cur_text_index; i++) {
            //if ltorg 면서 16진수 FF보다 작다면 2자리로 출력
            if(strcmp(obj_code->main_program.text[i].operator, "LITERAL") == 0 && obj_code->main_program.text[i].text < 0xFF)
                fprintf(fp, "00%02X", obj_code->main_program.text[i].text);
            else if (strcmp(obj_code->main_program.text[i].operator, "WORD") == 0)
                fprintf(fp, "%06X", obj_code->main_program.text[i].text);
            else
                fprintf(fp, "%04X", obj_code->main_program.text[i].text);
        }
        fprintf(fp, "\n");

        prev_text_index = calculate_print_section(&(obj_code->main_program), cur_text_index);
        test++;
        if(test > 4)    
            break;

    }

    //M 출력
    for(int i = 0; i < obj_code->main_program.modification_table_length; i++) {
        fprintf(fp, "M%06X%02X%s\n", obj_code->main_program.modification_table[i].addr, obj_code->main_program.modification_table[i].modification_length, obj_code->main_program.modification_table[i].symbol);
    }

    //end 출력

    //token 에서 end를 찾아서 그 인덱스를 찾아서 그 인덱스의 텍스트 주소를 출력
    fprintf(fp, "E%06X\n", obj_code->end_addr);

    //가독성 위한 개행
    fprintf(fp, "\n");


    //같은 방식으로 서브루틴 출력

    for(int i = 0; i < obj_code->subroutine_length; i++) {
        fprintf(fp, "H%-6s%06X%06X\n", obj_code->subroutine[i].header_record.header_symbol, obj_code->subroutine[i].header_record.start_address, obj_code->subroutine[i].header_record.program_length);
        //extdef 출력 맨 처음에만 알파벳 D 출력하고 심볼 주소값 출력 줄바꿈 없이 출력
        if(obj_code->subroutine[i].extdef_length != 0){
            fprintf(fp, "D");
            for(int j = 0; j < obj_code->subroutine[i].extdef_length; j++) {
                fprintf(fp, "%-6s%06X", obj_code->subroutine[i].extdef[j].symbol, obj_code->subroutine[i].extdef[j].addr);
            }
            fprintf(fp, "\n");
        }
        
        //extref 출력 맨 처음에만 알파벳 R 출력하고 심볼 주소값 출력 줄바꿈 없이 출력
        if(obj_code->subroutine[i].extref_table[0].symbol != NULL){
            fprintf(fp, "R");
            for(int j = 0; j < 3; j++) {
                if(obj_code->subroutine[i].extref_table[j].symbol != NULL)
                    fprintf(fp, "%-6s", obj_code->subroutine[i].extref_table[j].symbol);
            }
            fprintf(fp, "\n");
        }


        //맨 앞에 T를 쓰고 해당 텍스트의 시작 주소와 줄바꿈 전까지의 길이 출력함 길이는 최대 16진수 1D까지 넘어가면 줄바꿈 해야함
        // 그러므로 미리 길이를 체크해야함
        int prev_text_index = 0;
        int cur_text_index = 0;

        while(prev_text_index < obj_code->subroutine[i].text_length) {
            cur_text_index = calculate_text_length(&(obj_code->subroutine[i]), prev_text_index);
            if(cur_text_index == prev_text_index)
                break;
        
        
            unsigned int text_start_addr = obj_code->subroutine[i].text[prev_text_index].loc;
            unsigned int text_len = obj_code->subroutine[i].text[cur_text_index].loc - text_start_addr;

            //T와 함계 시작주소와 텍스트 길이 16진수로 출력
            fprintf(fp, "T%06X%02X", text_start_addr, text_len);

            for(int j = prev_text_index; j < cur_text_index; j++) {
                //if ltorg 면서 16진수 FF보다 작다면 2자리로 출력
                if(strcmp(obj_code->subroutine[i].text[j].operator, "LITERAL") == 0 && obj_code->subroutine[i].text[j].text < 0xFF)
                    fprintf(fp, "00%02X", obj_code->subroutine[i].text[j].text);
                else if (strcmp(obj_code->subroutine[i].text[j].operator, "WORD") == 0)
                    fprintf(fp, "%06X", obj_code->subroutine[i].text[j].text);
                else
                    fprintf(fp, "%04X", obj_code->subroutine[i].text[j].text);
            }

            fprintf(fp, "\n");

            prev_text_index = calculate_print_section(&(obj_code->subroutine[i]), cur_text_index);
        }



        //M 출력
        for(int j = 0; j < obj_code->subroutine[i].modification_table_length; j++) {
            fprintf(fp, "M%06X%02X%s\n", obj_code->subroutine[i].modification_table[j].addr, obj_code->subroutine[i].modification_table[j].modification_length, obj_code->subroutine[i].modification_table[j].symbol);
        }

        //E 출력과 가독성 위한 개행
        fprintf(fp, "E\n\n");


    }



return 0;
}


int main(int argc, char **argv) {
    /** SIC/XE 머신의 instruction 정보를 저장하는 테이블 */
    inst *inst_table[MAX_INST_TABLE_LENGTH];
    int inst_table_length;

    /** SIC/XE 소스코드를 저장하는 테이블 */
    char *input[MAX_INPUT_LINES];
    int input_length;

    /** 소스코드의 각 라인을 토큰 전환하여 저장하는 테이블 */
    token *tokens[MAX_INPUT_LINES];
    int tokens_length;

    /** 소스코드 내의 심볼을 저장하는 테이블 */
    symbol *symbol_table[MAX_TABLE_LENGTH];
    int symbol_table_length;

    /** 소스코드 내의 리터럴을 저장하는 테이블 */
    literal *literal_table[MAX_TABLE_LENGTH];
    int literal_table_length;

    /** 오브젝트 코드를 저장하는 변수 */
    object_code *obj_code = (object_code *)malloc(sizeof(object_code));

    int err = 0;

    if ((err = init_inst_table(inst_table, &inst_table_length,
                               "inst_table.txt")) < 0) {
        fprintf(stderr,
                "init_inst_table: 기계어 목록 초기화에 실패했습니다. "
                "(error_code: %d)\n",
                err);
        return -1;
    }

    if ((err = init_input(input, &input_length, "input.txt")) < 0) {
        fprintf(stderr,
                "init_input: 소스코드 입력에 실패했습니다. (error_code: %d)\n",
                err);
        return -1;
    }

    if ((err = assem_pass1((const inst **)inst_table, inst_table_length,
                           (const char **)input, input_length, tokens,
                           &tokens_length, symbol_table, &symbol_table_length,
                           literal_table, &literal_table_length)) < 0) {
        fprintf(stderr,
                "assem_pass1: 패스1 과정에서 실패했습니다. (error_code: %d)\n",
                err);

        return -1;
    }

    // for(int i = 0; i < tokens_length; i++) {
    //     printf("%s %s %s %s %s\n", tokens[i]->label, tokens[i]->operator, tokens[i]->operand[0], tokens[i]->operand[1], convert_binary(tokens[i]->nixbpe));
    // }

    /** 프로젝트1에서는 불필요함 */
    /*
    if ((err = make_opcode_output("output_opcode.txt", (const token **)tokens,
                                  tokens_length, (const inst **)inst_table,
                                  inst_table_length)) < 0) {
        fprintf(stderr,
                "make_opcode_output: opcode 파일 출력 과정에서 실패했습니다. "
                "(error_code: %d)\n",
                err);
        return -1;
    }
    */

    if ((err = make_symbol_table_output("output_symtab.txt",
                                        (const symbol **)symbol_table,
                                        symbol_table_length)) < 0) {
        fprintf(stderr,
                "make_symbol_table_output: 심볼테이블 파일 출력 과정에서 "
                "실패했습니다. (error_code: %d)\n",
                err);
        return -1;
    }

    if ((err = make_literal_table_output("output_littab.txt",
                                         (const literal **)literal_table,
                                         literal_table_length)) < 0) {
        fprintf(stderr,
                "make_literal_table_output: 리터럴테이블 파일 출력 과정에서 "
                "실패했습니다. (error_code: %d)\n",
                err);
        return -1;
    }



    if ((err = assem_pass2((const token **)tokens, tokens_length,
                           (const inst **)inst_table, inst_table_length,
                           (const symbol **)symbol_table, symbol_table_length,
                           (const literal **)literal_table,
                           literal_table_length, obj_code)) < 0) {
        fprintf(stderr,
                "assem_pass2: 패스2 과정에서 실패했습니다. (error_code: %d)\n",
                err);
        return -1;
    }

    // 반복문으로 메인 프로그램 로케이션, 심볼, 텍스트 출력
    // for(int i = 0; i < obj_code->main_program.text_length; i++) {
        // printf("%s, %06X %08X\n", obj_code->main_program.text[i].operator, obj_code->main_program.text[i].loc, obj_code->main_program.text[i].text);
    // }


    // 두번째 서브루틴 프로그램 로케이션, 심볼, 텍스트 출력
    // for(int i = 0; i < obj_code->subroutine[1].text_length; i++) {
    //     printf("%s, %06X %08X\n", obj_code->subroutine[1].text[i].operator, obj_code->subroutine[1].text[i].loc, obj_code->subroutine[1].text[i].text);
    // }




    // // 심볼테이블 라벨과 함께 출력
    // for(int i = 0; i < symbol_table_length; i++) {
    //     printf("%s %06X\n", symbol_table[i]->label, symbol_table[i]->addr);
    // }

    // for(int i = 0; i < obj_code->main_program.text_length; i++) {
    //     if(obj_code->main_program.text[i] != 0)
    //         printf("%08X\n", obj_code->main_program.text[i]);
    // }

    // for(int i = 0; i < obj_code->subroutine_length; i++) {
    //     for(int j = 0; j < obj_code->subroutine[i].text_length; j++) {
    //         if(obj_code->subroutine[i].text[j] != 0)
    //             printf("%08X\n", obj_code->subroutine[i].text[j]);
    //     }
    // }

        // for(int j = 0; j < obj_code->subroutine[0].text_length; j++) {
            // if(obj_code->subroutine[0].text[j] != 0)
                // printf("%08X\n", obj_code->subroutine[0].text[j]);
        // }
    

    //subroutine의 모디피케이션 출력
    // for(int i = 0; i < obj_code->subroutine[0].modification_table_length; i++) {
    //     printf("M%06X%02X%s", obj_code->subroutine[0].modification_table[i].addr, obj_code->subroutine[0].modification_table[i].modification_length, obj_code->subroutine[0].modification_table[i].symbol);
    // }

    // modificatin 출력
    // for(int i = 0; i < obj_code->main_program.modification_table_length; i++) {
    //     printf("M%06X%02X%s", obj_code->main_program.modification_table[i].addr, obj_code->main_program.modification_table[i].modification_length, obj_code->main_program.modification_table[i].symbol);
    // }

    //program length 출력
    // printf("D%06X\n", obj_code->main_program.header_record.program_length);

    if ((err = make_objectcode_output("output_objectcode.txt",
                                      (const object_code *)obj_code)) < 0) {
        fprintf(stderr,
                "make_objectcode_output: 오브젝트코드 파일 출력 과정에서 "
                "실패했습니다. (error_code: %d)\n",
                err);
        return -1;
    }

    return 0;
}