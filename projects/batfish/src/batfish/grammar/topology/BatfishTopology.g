grammar BatfishTopology;

options {
  superClass = TopologyParser;
}

tokens {
  HEADER = 'CONFIGPARSER_TOPOLOGY';
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
List<Edge> edges = new ArrayList<Edge>();
}
  :
  (
    HEADER NEWLINE
    (
      (el=edge_line 
                   {
                    edges.add(el);
                   })
      | NEWLINE
    )*
  )
  
  {
   t = new Topology(edges);
  }
  ;

edge_line returns [Edge e]
  :
  (node1=VARIABLE COLON int1=VARIABLE COMMA node2=VARIABLE COLON int2=VARIABLE NEWLINE) 
                                                                                       {
                                                                                        e = new Edge(node1.getText(), int1.getText(), node2.getText(), int2.getText());
                                                                                       }
  ;

COLON
  :
  ':'
  ;

COMMA
  :
  ','
  ;

FORWARD_SLASH
  :
  '/'
  ;

IGNORED_NEWLINE
  :
  '\r' 
      {
       $channel = HIDDEN;
      }
  ;

LINE_COMMENT
  :
  '#' ~NEWLINE_CHAR* 
                                 {
                                  $channel = HIDDEN;
                                 }
  ;

MINUS
  :
  '-'
  ;

NEWLINE
  :
  NEWLINE_CHAR
  ;

PERIOD
  :
  '.'
  ;

UNDERSCORE
  :
  '_'
  ;

VARIABLE
  :
  LETTER
  (
    DIGIT
    | FORWARD_SLASH
    | LETTER
    | MINUS
    | PERIOD
    | UNDERSCORE
  )*
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

fragment
DIGIT
  :
  '0'..'9'
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
