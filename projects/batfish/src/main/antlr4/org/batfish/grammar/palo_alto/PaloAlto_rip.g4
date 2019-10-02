parser grammar PaloAlto_rip;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

snvrp_rip
:
    RIP
;