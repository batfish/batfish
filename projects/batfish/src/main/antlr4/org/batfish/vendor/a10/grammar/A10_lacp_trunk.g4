parser grammar A10_lacp_trunk;

import A10_common;

options {
    tokenVocab = A10Lexer;
}

// ACOS 2.X style lacp trunk definition
s_lacp_trunk: LACP_TRUNK trunk_number NEWLINE slt_definition*;

// TODO determine other allowed syntax here
// presumably overlaps some with `interface trunk` syntax
slt_definition: sltd_ports_threshold;

sltd_ports_threshold: PORTS_THRESHOLD ports_threshold NEWLINE;
