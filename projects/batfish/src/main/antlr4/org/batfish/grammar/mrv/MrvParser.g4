parser grammar MrvParser;

import
Mrv_async, Mrv_common, Mrv_interface, Mrv_subscriber, Mrv_subtemplate, Mrv_system;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = MrvLexer;
}

@header {
package org.batfish.grammar.mrv;
}

assignment
:
   a_async
   | a_interface
   | a_system
   | a_subscriber
   | a_subtemplate
;

mrv_configuration
:
   assignment+
;