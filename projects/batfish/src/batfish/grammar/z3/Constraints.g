grammar Constraints;

tokens {
  AND        = 'and';
  BITVEC     = 'BitVec';
  DEFINE_FUN = 'define-fun';
  EXTRACT    = 'extract';
  FALSE      = 'false';
  LET        = 'let';
  MODEL      = 'model';
  NOT        = 'not';
  OR         = 'or';
  SAT        = 'sat';
  TRUE       = 'true';
  UNSAT      = 'unsat';
  VAR        = ':var';
}

@lexer::header {
package batfish.grammar.z3;
}

@lexer::members {
boolean inComment = false;

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
package batfish.grammar.z3;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
}

@parser::members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "Constraints: " + hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	return errors;
}

public int nextIntVal() {
	return Integer.valueOf(input.LT(1).getText());
}

public int nextTokenType() {
	return input.LT(1).getType();
}
}

integer returns [int i]
  :
  (d=DEC 
        {
         i = Integer.parseInt(d.getText());
        })
  | (h=HEX 
          {
           String hexString = "0" + h.getText().substring(1);
           i = Integer.parseInt(hexString);
          })
  | (b=BIN 
          {
           String binString = "0" + b.getText().substring(1);
           i = Integer.parseInt(binString);
          })
  ;

constraint returns [Constraint c]
  :
  (LEFT_PAREN DEFINE_FUN key=VARIABLE LEFT_PAREN RIGHT_PAREN LEFT_PAREN UNDERSCORE BITVEC DEC RIGHT_PAREN value=long_integer RIGHT_PAREN) 
                                                                                                                                         {
                                                                                                                                          c = new Constraint(key.getText(), value);
                                                                                                                                         }
  ;

constraints returns [Map<String, Long> constraintMap]
  :
  (SAT LEFT_PAREN MODEL (c=constraint 
                                     {
                                      if (constraintMap == null) {
                                      	constraintMap = new HashMap<String, Long>();
                                      }
                                      constraintMap.put(c.getKey(), c.getValue());
                                     })* RIGHT_PAREN)
  | UNSAT
  ;

long_integer returns [long l]
  :
  (d=DEC 
        {
         String decString = d.getText();
         l = Long.parseLong(decString);
        })
  | (h=HEX 
          {
           String hexString = h.getText().substring(2);
           l = Long.parseLong(hexString, 16);
          })
  | (b=BIN 
          {
           String binString = b.getText().substring(2);
           l = Long.parseLong(binString, 2);
          })
  ;

COMMENT_LINE
  :
  {!inComment}?=>
  (
    (';' ~NEWLINE_CHAR) => ('!' ~NEWLINE_CHAR+ NEWLINE_CHAR)
  )
  
  {
   $channel = HIDDEN;
  }
  ;

BIN
  :
  ('#b') => ('#b' DIGIT+)
  ;

DEC
  :
  DIGIT+
  ;

EQUALS
  :
  '='
  ;

HEX
  :
  ('#x') => ('#x' HEX_DIGIT+)
  ;

LEFT_PAREN
  :
  '('
  ;

RIGHT_PAREN
  :
  ')'
  ;

UNDERSCORE
  :
  '_'
  ;

VARIABLE
  :
  LETTER
  (
    LETTER
    | DIGIT
    | '_'
  )*
  ;

WS
  :
  (
    ' '
    | '\t'
    | '\u000C'
    | ( {!inComment}?=> '\n')
  )
  
  {
   $channel = HIDDEN;
  }
  ;

fragment
NEWLINE_CHAR
  :
  '\n'
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
HEX_WORD
  :
  HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
  ;

fragment
LETTER
  :
  LOWER_CASE_LETTER
  | UPPER_CASE_LETTER
  ;

fragment
LOWER_CASE_LETTER
  :
  'a'..'z'
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
UPPER_CASE_LETTER
  :
  'A'..'Z'
  ;
