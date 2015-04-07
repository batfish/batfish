parser grammar QuestionParser;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = QuestionLexer;
}

@header {
package org.batfish.grammar.question;
}

multipath_question
:
   MULTIPATH environment=VARIABLE
;

question
:
   multipath_question 
;