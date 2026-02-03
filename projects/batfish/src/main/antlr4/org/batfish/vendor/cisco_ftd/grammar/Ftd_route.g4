parser grammar Ftd_route;

options {
   tokenVocab = FtdLexer;
}

route_stanza
:
   ROUTE interface_name network = IP_ADDRESS mask = IP_ADDRESS gateway = IP_ADDRESS metric = dec NEWLINE
;
