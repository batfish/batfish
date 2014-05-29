grammar GNS3Topology;

options {
  superClass = TopologyParser;
}

tokens {
  LEFT_SQUARE        = '[';
  DOUBLE_LEFT_SQUARE = '[[';
  ROUTER             = 'ROUTER';
  JUNOS              = 'JUNOS';
}

@lexer::header {
package batfish.grammar.topology;
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
package batfish.grammar.topology;

import java.util.Map;
import java.util.HashMap;

import batfish.grammar.TopologyParser;
import batfish.representation.Topology;
import batfish.representation.Edge;
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

topology returns [Topology t]
@init {
String currentRouter = null;
List<Edge> edges = new ArrayList<Edge>();
}
  :
  (
    (r=router_line 
                  {
                   currentRouter = r;
                  })
    | (el=edge_line 
                   {
                    edges.add(new Edge(currentRouter, el.get(0), el.get(1), el.get(2)));
                   })
    | (ignored_line)
  )*
  
  {
   t = new Topology(edges);
  }
  ;

router_line returns [String routerName]
  :
  DOUBLE_LEFT_SQUARE
  (
    ROUTER
    | JUNOS
  )
  (name=VARIABLE 
                {
                 routerName = name.getText();
                }) RIGHT_SQUARE RIGHT_SQUARE NEWLINE
  ;

edge_line returns [List<String> edgeRemainder = new ArrayList<String>()]
  :
  (int1=interface_name EQUALS host2=VARIABLE int2=interface_name NEWLINE) 
                                                                         {
                                                                          edgeRemainder.add(int1);
                                                                          edgeRemainder.add(host2.getText());
                                                                          edgeRemainder.add(int2);
                                                                         }
  ;

interface_name returns [String s = ""]
  :
  ( {input.LT(1).getText().length() > 1 && input.LT(1).getText().charAt(0) == 'f'
		&& input.LT(1).getText().contains("/")}?=> (x=VARIABLE) 
                                                         {
                                                          s += "FastEthernet" + x.getText().substring(1);
                                                         })
  | ( {input.LT(1).getText().length() > 1 && input.LT(1).getText().charAt(0) == 'e'}?=> (x=VARIABLE) 
                                                                                                    {
                                                                                                     s += "em" + x.getText().substring(1) + ".0";
                                                                                                    })
  ;

ignored_line
  :
  (
    (
      ~(
        DOUBLE_LEFT_SQUARE
        | VARIABLE
        | NEWLINE
       )
      |
      (
        DOUBLE_LEFT_SQUARE
        ~(
          ROUTER
          | JUNOS
         )
      )
      | ( {input.LT(1).getText().length() <= 1 || input.LT(1).getText().charAt(0) != 'f'
		|| !input.LT(1).getText().contains("/")}?=> VARIABLE)
    )
    ~NEWLINE* NEWLINE
  )
  ;

WS
  :
  (
    ' '
    | '\t'
    | '\u000C'
  )
  
  {
   $channel = HIDDEN;
  }
  ;

NEWLINE
  :
  '\n'
  ;

SHARP
  :
  '#'
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

BACK_SLASH
  :
  '\\'
  ;

FORWARD_SLASH
  :
  '/'
  ;

DOUBLE_QUOTE
  :
  '"'
  ;

IGNORED_NEWLINE
  :
  '\r' 
      {
       $channel = HIDDEN;
      }
  ;

UNDERSCORE
  :
  '_'
  ;

PLUS
  :
  '+'
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
    | FORWARD_SLASH
    | PERIOD
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
