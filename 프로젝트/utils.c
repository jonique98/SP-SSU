#include "my_assembler.h"

// 문자열의 개수를 반환하는 함수
int strings_len(char **strings) {
    int i = 0;
    while (strings[i]) {
        i++;
    }
    return i;
}

int word_count_equ(char *str) {
    int count = 0;
    int i = 0;

    while (str[i]) {
        while (str[i] == '+' || str[i] == '-' || str[i] == '*' || str[i] == '/') {
            i++;
        }
        if (str[i]) {
            count++;
            while (str[i] && str[i] != '+' && str[i] != '-' && str[i] != '*' && str[i] != '/') {
                i++;
            }
        }
    }

    return count;
}

int return_operator_equ(char *str) {
    if (str == NULL) {
        return -1;
    }
    int i = 0;
    while (str[i]) {
        if(str[i] == '+')
            return 1;
        else if (str[i] == '-')
            return 2;
        else if (str[i] == '*')
            return 3;
        else if (str[i] == '/')
            return 4;
        i++;
    }
    return -1;
}

//사칙연산 기호를 기준으로 문자열을 나누는 함수 (없으면 그냥 문자열 반환)
char **equ_split(char *str){
    if (str == NULL) {
        return NULL;
    }
    char **result = NULL;
    int size = word_count_equ(str) + 1;

    result = (char **)malloc(sizeof(char *) * size);
    if (result == NULL) {
        fprintf(stderr, "equ_split: 메모리 할당에 실패했습니다.\n");
        exit(1);
    }

    int i = 0;
    int j = 0;
    while (str[i]) {
        while (str[i] == '+' || str[i] == '-' || str[i] == '*' || str[i] == '/') {
            i++;
        }
        if (str[i]) {
            int start = i;
            while (str[i] && str[i] != '+' && str[i] != '-' && str[i] != '*' && str[i] != '/') {
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

// comment가 있는 경우를 처리하기 위해 문장 길이를 계산하는 함수
int calc_totallen(char **temp){
    int len = strings_len(temp);
    int totalLength = 0;

    for (int i = 0; i < len; i++)
        totalLength += strlen(temp[i]) + 1;
    return totalLength;
}

int starts_with(const char *str, const char c) {
    if (!str) {
        return 0;
    }

    return str[0] == c;
}

// optable에 있는 명령어인지 확인
int is_operator(const char *str, const inst **inst_table, int inst_table_length) {

    if(str[0] == '+') {
        str++;
    }


    for (int i = 0; i < inst_table_length; i++) {
        if (strcmp(str, inst_table[i]->str) == 0) {
            return 1;
        }
    }
    return 0;
}