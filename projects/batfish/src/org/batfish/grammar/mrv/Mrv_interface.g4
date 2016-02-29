parser grammar Mrv_interface;

import Mrv_common;

options {
   tokenVocab = MrvLexer;
}

interface_assignment
:
   interface_ifname
;

interface_ifname
:
   INTERFACE PERIOD IFNAME type_declaration
;
