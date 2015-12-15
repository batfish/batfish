parser grammar QuestionParametersParser;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = QuestionParametersLexer;
}

@header {
package org.batfish.grammar.question;
}

@members {
   private java.util.Map<String, VariableType> _typeBindings = new java.util.HashMap<String, VariableType>(); 
   
}

binding
:
   VARIABLE EQUALS
   (
      bool
      | integer
      | REGEX
      | IP_ADDRESS
      | IP_PREFIX
      | STRING_LITERAL
      | string_set
   )
;

bool
:
   FALSE
   | TRUE
;

integer
:
   MINUS? DEC
;

parameters
:
   binding*
;

regex
:
   REGEX
;

string_set
:
   SET OPEN_ANGLE_BRACKET STRING CLOSE_ANGLE_BRACKET OPEN_BRACE
   (
      str_elem += STRING_LITERAL
      (
         COMMA str_elem += STRING_LITERAL
      )*
   )? CLOSE_BRACE
;