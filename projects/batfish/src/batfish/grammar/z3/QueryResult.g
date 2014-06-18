grammar QueryResult;

tokens {
  AND     = 'and';
  EXTRACT = 'extract';
  FALSE   = 'false';
  LET     = 'let';
  NOT     = 'not';
  OR      = 'or';
  SAT     = 'sat';
  TRUE    = 'true';
  UNSAT   = 'unsat';
  VAR     = ':var';
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
}

@parser::members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "QueryResult: " + hdr + " " + msg;
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

atomic_element returns [Element elem]
  :
  (
    t=BIN
    | t=DEC
    | t=EXTRACT
    | t=FALSE
    | t=HEX
    | t=TRUE
    | t=VARIABLE
  )
  
  {
   elem = new AtomicElement(t.getText());
  }
  ;

element returns [Element elem]
  :
  (
    e=atomic_element
    | e=multiline_list
    | e=sameline_list
    | e=var_element
  )
  
  {
   elem = e;
  }
  ;

integer returns [int i]
  :
  (x=DEC) 
         {
          i = Integer.parseInt(x.getText());
         }
  ;

multiline_list returns [Element elem]
@init {
ListElement l = new MultilineList();
}
  :
  (
    LEFT_PAREN (head=multiline_list_head 
                                        {
                                         l.setHeadElement(head);
                                        }) (body_elem=element 
                                                             {
                                                              l.addBodyElement(body_elem);
                                                             })+ RIGHT_PAREN
  )
  
  {
   elem = l;
  }
  ;

multiline_list_head returns [Element elem]
  :
  (
    t=AND
    | t=OR
    | t=NOT
    | t=LET
  )
  
  {
   elem = new AtomicElement(t.getText());
  }
  ;

result returns [Result r]
  :
  ( (SAT e=element) 
                   {
                    r = new Result(e);
                   })
  | UNSAT
  ;

results returns [List<Result> resultList = new ArrayList<Result>()]
  :
  (r=result 
           {
            	resultList.add(r);
           })+
  ;

sameline_list returns [Element elem]
@init {
ListElement l = new SamelineList();
}
  :
  (
    LEFT_PAREN (head=sameline_list_head 
                                       {
                                        l.setHeadElement(head);
                                       }) (body_elem=element 
                                                            {
                                                             l.addBodyElement(body_elem);
                                                            })* RIGHT_PAREN
  )
  
  {
   elem = l;
  }
  ;

sameline_list_head returns [Element elem]
  :
  (
    e=sameline_list
    | e=token_sameline_list_head
  )
  
  {
   elem = e;
  }
  ;

token_sameline_list_head returns [Element elem]
  :
  (
    t=EQUALS
    | t=UNDERSCORE
    | t=VARIABLE
  )
  
  {
   elem = new AtomicElement(t.getText());
  }
  ;

var_element returns [Element elem]
  :
  (LEFT_PAREN (VAR i=integer) RIGHT_PAREN) 
                                          {
                                           elem = new VarElement(i);
                                          }
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
  ('a!') => ('a!' DIGIT+)
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
