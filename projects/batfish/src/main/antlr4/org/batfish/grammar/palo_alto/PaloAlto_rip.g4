parser grammar PaloAlto_rip;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

vrp_rip
:
    RIP
;