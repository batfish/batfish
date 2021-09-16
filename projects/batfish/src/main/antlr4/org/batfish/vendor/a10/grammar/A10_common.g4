parser grammar A10_common;

options {
    tokenVocab = A10Lexer;
}

hostname: str;

str: STR;

null_rest_of_line: ~NEWLINE* NEWLINE;
