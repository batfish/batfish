grammar LogicBloxFactGrammar;

@lexer::header {
package batfish.grammar.logicblox;
}

@lexer::members {
boolean enableIPV6_ADDRESS = true;
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
package batfish.grammar.logicblox;

import java.util.Collections;
}

@parser::members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "LogicBloxFactGrammar: " + hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	List<String> allErrors = new ArrayList<String>();
	allErrors.addAll(errors);
	return allErrors;
}

public int nextIntVal() {
	return Integer.valueOf(input.LT(1).getText());
}

public int nextTokenType() {
	return input.LT(1).getType();
}
}

application returns [Term t]
  :
  (v=VARIABLE OPEN_BRACKET l=argument_list? CLOSE_BRACKET) 
                                                          {
                                                           String s = "[";
                                                           if (l != null) {
                                                           	s += l;
                                                           }
                                                           s += "]";
                                                           t = new Term(v.getText(), s);
                                                          }
  ;

application_term returns [Term t]
  :
  (app=application EQUALS arg=argument) 
                                       {
                                        t = new Term(app.getPredicateName(), app.getRemainder() + " = " + arg);
                                       }
  ;

argument returns [String s]
  :
  (
    (
      x=VARIABLE
      | x=INTEGER
      | x=FLOAT
      | x=STRING_LITERAL
    )
    
    {
     s = x.getText();
    }
  )
  | (a=application 
                  {
                   s = a.getPredicateName() + a.getRemainder();
                  })
  ;

argument_list returns [String s]
  :
  (a=argument 
             {
              s = a;
             }) (COMMA b=argument 
                                 {
                                  s += ", " + b;
                                 })*
  ;

constructor_insertion returns [Insertion i]
  :
  (PLUS e=entity_term COMMA PLUS a=application_term (LEFT_ARROW tl=term_list)? PERIOD) 
                                                                                      {
                                                                                       i = new ConstructorInsertion(e, a, tl);
                                                                                      }
  ;

entity_term returns [Term t]
  :
  (pred=VARIABLE OPEN_PAREN var=VARIABLE CLOSE_PAREN) 
                                                     {
                                                      t = new Term(pred.getText(), "(" + var.getText() + ")");
                                                     }
  ;

fact_block returns [List<Insertion> insertions = new ArrayList<Insertion>()]
  :
  (i=insertion 
              {
               insertions.add(i);
              })*
  ;

function_insertion returns [Insertion i]
  :
  (PLUS a=application_term (LEFT_ARROW tl=term_list)? PERIOD) 
                                                             {
                                                              i = new PredicateInsertion(a, tl);
                                                             }
  ;

insertion returns [Insertion i]
  :
  (
    x=constructor_insertion
    | x=function_insertion
    | x=predicate_insertion
    | x=refmode_insertion
  )
  
  {
   i = x;
  }
  ;

operator returns [String s]
  :
  (x=EQUALS) 
            {
             s = x.getText();
            }
  ;

operator_term returns [Term s]
  :
  (sa=simple_argument o=operator a=argument) 
                                            {
                                             s = new Term(null, sa + o + a);
                                            }
  ;

positive_term returns [Term t] //throws out + prefix
  :
  (
    PLUS
    (
      x=predicate_term
      | x=application_term
    )
  )
  
  {
   t = x;
  }
  ;

predicate_insertion returns [Insertion i]
  :
  (PLUS p=predicate_term (LEFT_ARROW tl=term_list)? PERIOD) 
                                                           {
                                                            i = new PredicateInsertion(p, tl);
                                                           }
  ;

predicate_term returns [Term t]
@init {
String remainder = "";
String predicateName;
}
  :
  (
    (v=VARIABLE 
               {
                predicateName = v.getText();
                remainder = "(";
               }) OPEN_PAREN
    (
      (
        x=argument_list
        | x=refmode_argument
      )
      
      {
       remainder += x;
      }
    )?
    CLOSE_PAREN 
               {
                remainder += ")";
               }
  )
  
  {
   t = new Term(predicateName, remainder);
  }
  ;

refmode_argument returns [String s]
  :
  (v=VARIABLE COLON a=argument) 
                               {
                                s = v.getText() + ":" + a;
                               }
  ;

refmode_insertion returns [Insertion i]
  :
  (PLUS e=entity_term COMMA PLUS r=refmode_term (LEFT_ARROW tl=term_list)? PERIOD) 
                                                                                  {
                                                                                   i = new RefmodeInsertion(e, r, tl);
                                                                                  }
  ;

refmode_term returns [Term t]
  :
  (v=VARIABLE OPEN_PAREN r=refmode_argument CLOSE_PAREN) 
                                                        {
                                                         t = new Term(v.getText(), "(" + r + ")");
                                                        }
  ;

simple_argument returns [String s]
  :
  (
    x=VARIABLE
    | x=INTEGER
    | x=FLOAT
    | x=STRING_LITERAL
  )
  
  {
   s = x.getText();
  }
  ;

term returns [Term t]
  :
  (
    x=predicate_term
    | x=application_term
    | x=operator_term
    | x=positive_term
  )
  
  {
   t = x;
  }
  ;

term_list returns [List<Term> terms = new ArrayList<Term>()]
  :
  (t1=term 
          {
           terms.add(t1);
          }) (COMMA (t2=term 
                            {
                             terms.add(t2);
                            }))*
  ;

CLOSE_BRACKET
  :
  ']'
  ;

CLOSE_PAREN
  :
  ')'
  ;

COLON
  :
  ':'
  ;

COMMA
  :
  ','
  ;

INTEGER
  :
  DIGIT+
  ;

FLOAT
  :
  DIGIT+
  (
    ('.' DIGIT) => '.' DIGIT+ EXP?
    | ('.\n') => 
                {
                 $type = INTEGER;
                } // change the token here
    | ('. ') => 
               {
                $type = INTEGER;
               } // change the token here
    | EXP
  )
  ;

fragment
EXP
  :
  (
    'e'
    | 'E'
  )
  DIGIT+
  ;

EQUALS
  :
  '='
  ;

LEFT_ARROW
  :
  '<-'
  ;

OPEN_BRACKET
  :
  '['
  ;

OPEN_PAREN
  :
  '('
  ;

PERIOD
  :
  '.'
  ;

PLUS
  :
  '+'
  ;

STRING_LITERAL
  :
  DOUBLE_QUOTE ~DOUBLE_QUOTE* DOUBLE_QUOTE
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

WS
  :
  (
    ' '
    | '\t'
    | '\n'
    | '\u000C'
    | ('//' ~'\n'*)
  )
  
  {
   $channel = HIDDEN;
  }
  ;

fragment
DIGIT
  :
  '0'..'9'
  ;

fragment
DOUBLE_QUOTE
  :
  '"'
  ;

fragment
E
  :
  'e'
  | 'E'
  ;

fragment
LETTER
  :
  LOWER_CASE_LETTER
  | UPPER_CASE_LETTER
  ;

fragment
LINE_COMMENT_OPENER
  :
  '//'
  ;

fragment
LOWER_CASE_LETTER
  :
  'a'..'z'
  ;

fragment
NEWLINE_CHAR
  :
  '\n'
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
