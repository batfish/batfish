parser grammar MrvParser;

import
Mrv_async, Mrv_common, Mrv_interface, Mrv_system;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = MrvLexer;
}

@header {
package org.batfish.grammar.mrv;
}

assignment
:
   interface_assignment
;

mrv_configuration
:
   assignment+
;