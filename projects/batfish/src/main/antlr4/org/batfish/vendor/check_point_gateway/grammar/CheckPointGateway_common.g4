parser grammar CheckPointGateway_common;

options {
    tokenVocab = CheckPointGatewayLexer;
}

word: WORD;

null_rest_of_line: ~NEWLINE* NEWLINE;
