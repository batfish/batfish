parser grammar A10_common;

options {
    tokenVocab = A10Lexer;
}

hostname: WORD;

null_rest_of_line: ~NEWLINE* NEWLINE;
