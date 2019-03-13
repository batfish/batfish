lexer grammar F5BigipImishLexer;

options {
  superClass = 'org.batfish.grammar.BatfishLexer';
}

@members {
// Java code to end up in F5BigipImishLexer.java goes here

private int lastTokenType = -1;

@Override
public void emit(Token token) {
    super.emit(token);
    if (token.getChannel() != HIDDEN) {
       lastTokenType = token.getType();
    }
}

}

// Keywords

ACCESS_LIST
:
  'access-list'
;

ADDRESS
:
  'address'
;

ANY
:
  'any'
;

BFD
:
  'bfd'
;

BGP
:
  'bgp'
;

CAPABILITY
:
  'capability'
;

COMMUNITY
:
  'community'
;

CON
:
  'con'
;

DENY
:
  'deny'
;

DESCRIPTION
:
  'description'
;

EBGP
:
  'ebgp'
;

END
:
  'end'
;

FALL_OVER
:
  'fall-over'
;

GRACEFUL_RESTART
:
  'graceful-restart'
;

INTERFACE
:
  'interface'
;

IP
:
  'ip'
;

KERNEL
:
  'kernel'
;

LINE
:
  'line'
;

LOGIN
:
  'login'
;

MATCH
:
  'match'
;

MAX_PATHS
:
  'max-paths'
;

MAXIMUM_PREFIX
:
  'maximum-prefix'
;

NEIGHBOR
:
  'neighbor'
;

NO
:
  'no'
;

PEER_GROUP
:
  'peer-group'
;

PERMIT
:
  'permit'
;

REDISTRIBUTE
:
  'redistribute'
;

REMOTE_AS
:
  'remote-as'
;

ROUTE_MAP
:
  'route-map'
;

ROUTER
:
  'router'
;

SERVICE
:
  'service'
;

SET
:
  'set'
;

UPDATE_SOURCE
:
  'update-source'
;

VTY
:
  'vty'
;

// Complex tokens

COMMENT_LINE
:
  (
    F_Whitespace
  )* '!'
  {lastTokenType == NEWLINE || lastTokenType == -1}?

  F_NonNewlineChar* F_Newline+ -> channel ( HIDDEN )
;

COMMENT_TAIL
:
  '!' F_NonNewlineChar* -> channel ( HIDDEN )
;

NEWLINE
:
  F_Newline+
;

WORD
:
  F_WordChar+
;

WS
:
  F_Whitespace+ -> channel ( HIDDEN ) // parser never sees tokens on hidden channel

;

// Fragments

fragment
F_Newline
:
  [\r\n] // carriage return or line feed

;

fragment
F_NonNewlineChar
:
  ~[\r\n] // carriage return or line feed

;

fragment
F_Whitespace
:
  [ \t\u000C] // tab or space or unicode 0x000C

;

fragment
F_WordChar
:
  ~[ \t\n\r{}[\]]
;
