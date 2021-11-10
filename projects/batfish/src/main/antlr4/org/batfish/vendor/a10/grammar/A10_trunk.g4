parser grammar A10_trunk;

import A10_common;

options {
    tokenVocab = A10Lexer;
}

// ACOS 2.X style trunk definition
s_trunk: TRUNK trunk_number NEWLINE st_definition*;

// TODO determine other allowed syntax here
// presumably overlaps some with `interface trunk` syntax
st_definition: std_ethernet | std_name;

std_name: NAME name = interface_name_str NEWLINE;

std_ethernet: (trunk_ethernet_interface | trunk_ethernet_interface_range)+ NEWLINE;

trunk_ethernet_interface: ETHERNET num = ethernet_number;

trunk_ethernet_interface_range: ETHERNET num = ethernet_number TO to = ethernet_number;
