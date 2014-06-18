grammar Semantics;

tokens {
  COMMENT_OPENER = '///';
}

@lexer::header {
package batfish.grammar.semantics;
}

@lexer::members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	return errors;
}
}

@parser::header {
package batfish.grammar.semantics;

import java.util.Map;
import java.util.HashMap;
}

@parser::members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	return errors;
}
}

predicate_semantics returns [Map<String, String> semantics = new HashMap<String, String>()]
  :
  (
    ignored_line
    | (x=function_comment 
                         {
                          semantics.put(x.get(0), x.get(1));
                         })
    | (x=predicate_comment 
                          {
                           semantics.put(x.get(0), x.get(1));
                          })
  )*
  ;

ignored_line
  :
  (
    ~(
      COMMENT_OPENER
      | NEWLINE
     )
    ~NEWLINE* NEWLINE
  )
  | NEWLINE
  ;

function_comment returns [List<String> keyValuePair = new ArrayList<String>()]
  :
  (COMMENT_OPENER WS+ LEFT_SQUARE name=VARIABLE a=function_opening_line_remainder b=body_line_list c=closing_line) 
                                                                                                                  {
                                                                                                                   String semantics = "[" + name.getText() + a + b + c;
                                                                                                                   keyValuePair.add(name.getText());
                                                                                                                   keyValuePair.add(semantics);
                                                                                                                  }
  ;

closing_line returns [String s = ""]
  :
  COMMENT_OPENER WS+
  (
    a=
    ~(
      PERIOD
      | WS
      | NEWLINE
     )
    
    {
     s += a.getText();
    }
  )
  (
    a=
    ~(
      PERIOD
      | NEWLINE
     )
    
    {
     s += a.getText();
    }
  )*
  (PERIOD 
         {
          s += ".";
         }) WS* NEWLINE
  ;

body_line_list returns [String s = ""]
  :
  (x=body_line 
              {
               s += x;
              })*
  ;

body_line returns [String s = ""]
  :
  COMMENT_OPENER WS+
  (
    a=
    ~(
      WS
      | PERIOD
      | NEWLINE
     )
    
    {
     s += a.getText();
    }
  )
  (
    a=
    ~(
      PERIOD
      | NEWLINE
     )
    
    {
     s += a.getText();
    }
  )*
  (NEWLINE 
          {
           s += "\n\t";
          })
  ;

predicate_comment returns [List<String> keyValuePair = new ArrayList<String>()]
  :
  (COMMENT_OPENER WS+ LEFT_SQUARE name=VARIABLE a=predicate_opening_line_remainder b=body_line_list c=closing_line) 
                                                                                                                   {
                                                                                                                    String semantics = "[" + name.getText() + a + b + c;
                                                                                                                    keyValuePair.add(name.getText());
                                                                                                                    keyValuePair.add(semantics);
                                                                                                                   }
  ;

predicate_opening_line_remainder returns [String s = ""]
  :
  (a=LEFT_PAREN 
               {
                s += a.getText();
               }) (b=~NEWLINE 
                             {
                              s += b.getText();
                             })* (NEWLINE 
                                         {
                                          s += "\n\t";
                                         })
  ;

function_opening_line_remainder returns [String s = ""]
  :
  (a=LEFT_SQUARE 
                {
                 s += a.getText();
                }) (b=~NEWLINE 
                              {
                               s += b.getText();
                              })* (NEWLINE 
                                          {
                                           s += "\n\t";
                                          })
  ;

//COMMENT_OPENER
//  :
//  '///'
//  ;

WS
  :
  (
    ' '
    | '\t'
    | '\u000C'
  )
  ;

NEWLINE
  :
  '\n'
  ;

LEFT_SQUARE
  :
  '['
  ;

RIGHT_SQUARE
  :
  ']'
  ;

BACKTICK
  :
  '`'
  ;

LEFT_PAREN
  :
  '('
  ;

RIGHT_PAREN
  :
  ')'
  ;

MINUS
  :
  '-'
  ;

LT
  :
  '<'
  ;

GT
  :
  '>'
  ;

EXCLAMATION
  :
  '!'
  ;

COLON
  :
  ':'
  ;

FORWARD_SLASHES
  :
  '/'+
  ;

DOUBLE_QUOTE
  :
  '"'
  ;

UNDERSCORE
  :
  '_'
  ;

PERCENT
  :
  '%'
  ;

PLUS
  :
  '+'
  ;

SINGLE_QUOTE
  :
  '\''
  ;

SEMICOLON
  :
  ';'
  ;

QUESTION_MARK
  :
  '?'
  ;

COMMA
  :
  ','
  ;

PERIOD
  :
  '.'
  ;

EQUALS
  :
  '='
  ;

STAR
  :
  '*'
  ;

LEFT_BRACE
  :
  '{'
  ;

RIGHT_BRACE
  :
  '}'
  ;

AMPERSAND
  :
  '&'
  ;

DEC
  :
  '0'
  | POSITIVE_DIGIT DIGIT*
  ;

HEX
  :
  '0x' HEX_DIGIT+
  ;

VARIABLE
  :
  LETTER
  (
    LETTER
    | DIGIT
    | '-'
    | '_'
  )*
  ;

fragment
DEC_BYTE
  :
  (POSITIVE_DIGIT DIGIT DIGIT)
  | (POSITIVE_DIGIT DIGIT)
  | DIGIT
  ;

fragment
DIGIT
  :
  '0'..'9'
  ;

fragment
HEX_DIGIT
  :
  (
    '0'..'9'
    | 'a'..'f'
    | 'A'..'F'
  )
  ;

fragment
POSITIVE_HEX_DIGIT
  :
  (
    '1'..'9'
    | 'a'..'f'
    | 'A'..'F'
  )
  ;

fragment
POSITIVE_DIGIT
  :
  '1'..'9'
  ;

fragment
LOWER_CASE_LETTER
  :
  'a'..'z'
  ;

fragment
UPPER_CASE_LETTER
  :
  'A'..'Z'
  ;

fragment
LETTER
  :
  LOWER_CASE_LETTER
  | UPPER_CASE_LETTER
  ;
