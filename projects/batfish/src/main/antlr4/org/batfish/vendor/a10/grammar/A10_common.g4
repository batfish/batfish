parser grammar A10_common;

options {
    tokenVocab = A10Lexer;
}

hostname: str_content;

str_content: WORD;

null_rest_of_line: ~NEWLINE* NEWLINE;
