parser grammar Arista_interface;

options {
   tokenVocab = AristaLexer;
}

if_ip_virtual_router
:
   VIRTUAL_ROUTER ADDRESS (address = IP_ADDRESS | prefix = IP_PREFIX) NEWLINE
;
