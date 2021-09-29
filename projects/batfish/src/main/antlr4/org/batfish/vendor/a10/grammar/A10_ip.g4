parser grammar A10_ip;

import A10_common;

options {
   tokenVocab = A10Lexer;
}

s_ip: IP si;

si: si_route;

si_route: ROUTE sir_definition;

sir_definition: ip_prefix ip_address (sird_description sird_distance? | sird_distance sird_description?)? NEWLINE;

sird_description: DESCRIPTION route_description;

// 1-255
sird_distance: uint8;

// TODO handle deleting static routes
