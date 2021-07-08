parser grammar CheckPointGateway_common;

options {
    tokenVocab = CheckPointGatewayLexer;
}

hostname: word;

word: STR_SEPARATOR WORD;

null_rest_of_line: ~NEWLINE* NEWLINE;
