parser grammar SpecifierParser;

options {
  tokenVocab = SpecifierLexer;
}

@members {
  /** Case-insensitive check that the next token's text equals a soft keyword. */
  private boolean nextIs(String kw) {
    return _input.LT(1).getText().equalsIgnoreCase(kw);
  }
}

// Parser for the Batfish flexible specifier language, replacing the parboiled
// grammar in Parser.java / CommonParser.java. Each specifier type has its own
// entry point; the visitor builds the same AstNode tree the parboiled parser
// produced. Rule names mirror the parboiled rule names (lower-cased first
// letter) so the antlr4-c3 autocomplete mapping can key on rule indices.
//
// Set-operator precedence is preserved from the original: intersection ('&')
// binds tighter than union (',') and difference ('\'), so each "spec" rule is
// split into a "<spec>" (union/difference level) over "<intersection>" over
// "<term>".

// ---- Entry points (one per Grammar value; wrap with leading/trailing WS via
// the hidden channel and require EOF). ----

nodeSpecInput : nodeSpec EOF;
interfaceSpecInput : interfaceSpec EOF;
filterSpecInput : filterSpec EOF;
ipSpaceSpecInput : ipSpaceSpec EOF;
locationSpecInput : locationSpec EOF;
routingPolicySpecInput : routingPolicySpec EOF;
ipProtocolSpecInput : ipProtocolSpec EOF;
appSpecInput : appSpec EOF;
oneAppSpecInput : oneAppSpec EOF;
nameSetSpecInput : nameSetSpec EOF;
enumSetSpecInput : enumSetSpec EOF;

// ---- Shared lexical-ish rules ----

// A name: a single unquoted NAME token or a quoted name. In name-heavy grammars
// the lexer already forms the full name (including interior '.', ':', '/'); NUM
// is excluded because names cannot start with a digit.
name : NAME | QUOTED_NAME;

referenceBook : name;

// Anchored close-delimiters, so a caret before ')'/']' classifies as OPERATOR_END (mirroring
// parboiled's CloseParens/CloseBrackets rules) rather than the enclosing container rule.
closeParens : CLOSE_PAREN;
closeBrackets : CLOSE_BRACKET;

// ---- Node ----

nodeSpec : nodeIntersection ( ( COMMA | BACKSLASH ) nodeIntersection )*;

nodeIntersection : nodeTerm ( AMP nodeTerm )*;

nodeTerm
  : nodeRole
  | nodeType
  | nodeNameRegexDeprecated
  | nodeNameRegex
  | nodeName
  | nodeParens
  ;

nodeRole : AT_ROLE nodeRoleAndDimension;

nodeRoleAndDimension : OPEN_PAREN nodeRoleDimensionName nodeRoleAndDimensionTail;

nodeRoleAndDimensionTail : COMMA nodeRoleName closeParens;

nodeRoleDimensionName : name;

nodeRoleName : name;

nodeType : AT_DEVICE_TYPE OPEN_PAREN nodeTypeSpec closeParens;

nodeTypeSpec : NAME;

nodeName : name;

nodeNameRegex : REGEX;

nodeNameRegexDeprecated : deprecatedRegex;

nodeParens : OPEN_PAREN nodeSpec closeParens;

// ---- Interface ----

interfaceSpec : interfaceIntersection ( ( COMMA | BACKSLASH ) interfaceIntersection )*;

interfaceIntersection : interfaceTerm ( AMP interfaceTerm )*;

interfaceTerm
  : interfaceWithNode
  | interfaceWithoutNode
  | interfaceParens
  ;

interfaceWithNode : nodeTerm interfaceWithNodeTail;

interfaceWithNodeTail : OPEN_BRACKET interfaceWithoutNode closeBrackets;

interfaceWithoutNode
  : interfaceWithoutNodeIntersection
    ( ( COMMA | BACKSLASH ) interfaceWithoutNodeIntersection )*
  ;

interfaceWithoutNodeIntersection : interfaceWithoutNodeTerm ( AMP interfaceWithoutNodeTerm )*;

interfaceWithoutNodeTerm
  : interfaceFunc
  | interfaceNameRegexDeprecated
  | interfaceNameRegex
  | interfaceName
  | interfaceWithoutNodeParens
  ;

interfaceFunc
  : interfaceConnectedTo
  | interfaceInterfaceGroup
  | interfaceType
  | interfaceVrf
  | interfaceZone
  ;

interfaceConnectedTo : AT_CONNECTED_TO OPEN_PAREN ipSpaceSpec closeParens;

interfaceInterfaceGroup : AT_INTERFACE_GROUP interfaceGroupAndReferenceBook;

interfaceGroupAndReferenceBook : OPEN_PAREN referenceBook interfaceGroupAndReferenceBookTail;

interfaceGroupAndReferenceBookTail : COMMA interfaceGroup closeParens;

interfaceGroup : name;

interfaceType : AT_INTERFACE_TYPE OPEN_PAREN interfaceTypeSpec closeParens;

interfaceTypeSpec : NAME;

interfaceVrf : AT_VRF OPEN_PAREN vrfName closeParens;

vrfName : name;

interfaceZone : AT_ZONE OPEN_PAREN zoneName closeParens;

zoneName : name;

interfaceName : name;

interfaceNameRegex : REGEX;

interfaceNameRegexDeprecated : deprecatedRegex;

interfaceParens : OPEN_PAREN interfaceSpec closeParens;

interfaceWithoutNodeParens : OPEN_PAREN interfaceWithoutNode closeParens;

// ---- Filter ----

filterSpec : filterIntersection ( ( COMMA | BACKSLASH ) filterIntersection )*;

filterIntersection : filterTerm ( AMP filterTerm )*;

filterTerm
  : filterWithNode
  | filterWithoutNode
  | filterParens
  ;

filterWithNode : nodeTerm filterWithNodeTail;

filterWithNodeTail : OPEN_BRACKET filterWithoutNode closeBrackets;

filterWithoutNode
  : filterWithoutNodeIntersection ( ( COMMA | BACKSLASH ) filterWithoutNodeIntersection )*
  ;

filterWithoutNodeIntersection : filterWithoutNodeTerm ( AMP filterWithoutNodeTerm )*;

filterWithoutNodeTerm
  : filterInterfaceIn
  | filterInterfaceOut
  | filterNameRegexDeprecated
  | filterNameRegex
  | filterName
  | filterWithoutNodeParens
  ;

filterInterfaceIn : AT_IN OPEN_PAREN interfaceSpec closeParens;

filterInterfaceOut : AT_OUT OPEN_PAREN interfaceSpec closeParens;

filterName : name;

filterNameRegex : REGEX;

filterNameRegexDeprecated : deprecatedRegex;

filterParens : OPEN_PAREN filterSpec closeParens;

filterWithoutNodeParens : OPEN_PAREN filterWithoutNode closeParens;

// ---- IpSpace ----

ipSpaceSpec : ipSpaceIntersection ( ( COMMA | BACKSLASH ) ipSpaceIntersection )*;

ipSpaceIntersection : ipSpaceTerm ( AMP ipSpaceTerm )*;

ipSpaceTerm
  : ipPrefix
  | ipWildcard
  | ipRange
  | ipAddress
  | ipSpaceAddressGroup
  | ipSpaceLocation
  | ipSpaceParens
  ;

ipSpaceParens : OPEN_PAREN ipSpaceSpec closeParens;

ipSpaceAddressGroup : AT_ADDRESS_GROUP addressGroupAndReferenceBook;

addressGroupAndReferenceBook : OPEN_PAREN referenceBook addressGroupAndReferenceBookTail;

addressGroupAndReferenceBookTail : COMMA addressGroup closeParens;

addressGroup : name;

ipSpaceLocation : locationSpec;

ipAddress : IP_ADDRESS;

ipAddressMask : IP_ADDRESS;

ipPrefix : IP_PREFIX;

ipRange : ipAddress DASH ipAddress;

ipWildcard : ipAddress COLON ipAddressMask;

// ---- Location ----

locationSpec : locationIntersection ( ( COMMA | BACKSLASH ) locationIntersection )*;

locationIntersection : locationTerm ( AMP locationTerm )*;

locationTerm
  : locationInternet
  | locationEnter
  | locationInterface
  | locationParens
  ;

// 'internet' is a soft keyword: it lexes as NAME. Guard with a predicate so a
// non-internet name falls through to locationInterface. Optionally followed by
// an interface-with-node tail.
locationInternet : {nextIs("internet")}? NAME interfaceWithNodeTail?;

locationEnter : AT_ENTER OPEN_PAREN locationInterface closeParens;

locationInterface
  : interfaceWithNode
  | nodeTerm
  | interfaceFunc
  ;

locationParens : OPEN_PAREN locationSpec closeParens;

// ---- Routing policy ----

routingPolicySpec : routingPolicyIntersection ( ( COMMA | BACKSLASH ) routingPolicyIntersection )*;

routingPolicyIntersection : routingPolicyTerm ( AMP routingPolicyTerm )*;

routingPolicyTerm
  : routingPolicyNameRegexDeprecated
  | routingPolicyNameRegex
  | routingPolicyName
  | routingPolicyParens
  ;

routingPolicyName : name;

routingPolicyNameRegex : REGEX;

routingPolicyNameRegexDeprecated : deprecatedRegex;

routingPolicyParens : OPEN_PAREN routingPolicySpec closeParens;

// ---- IP protocol ----

ipProtocolSpec : ipProtocolTerm ( COMMA ipProtocolTerm )*;

ipProtocolTerm
  : ipProtocol
  | ipProtocolNot
  ;

ipProtocol
  : ipProtocolName
  | ipProtocolNumber
  ;

ipProtocolNot : BANG ipProtocol;

ipProtocolName : NAME;

ipProtocolNumber : NUM;

// ---- Application (set) ----

appSpec : appTerm ( COMMA appTerm )*;

appTerm
  : appIcmpTerm
  | appTcpTerm
  | appUdpTerm
  | appName
  | appNameRegex
  ;

// In application specifiers icmp/tcp/udp lex as dedicated keyword tokens (appKeywords), so appName
// only matches other NAMEs and the keyword terms match their tokens.
appName : NAME;

appNameRegex : REGEX;

appIcmpTerm : ICMP appIcmpType?;

appIcmpType : SLASH NUM appIcmpTypeCode?;

appIcmpTypeCode : SLASH NUM;

appTcpTerm : TCP appPortSpec?;

appUdpTerm : UDP appPortSpec?;

appPortSpec : SLASH appPortTerm ( COMMA appPortTerm )*;

appPortTerm
  : appPortRange
  | appPort
  ;

appPort : NUM;

appPortRange : NUM DASH NUM;

// ---- Single application ----

oneAppSpec
  : oneAppIcmp
  | oneAppTcp
  | oneAppUdp
  | appName
  ;

oneAppIcmp : ICMP SLASH NUM oneAppIcmpType;

oneAppIcmpType : SLASH NUM;

oneAppTcp : TCP SLASH appPort;

oneAppUdp : UDP SLASH appPort;

// ---- Name set ----

nameSetSpec : nameSetTerm ( COMMA nameSetTerm )*;

nameSetTerm
  : nameSetRegexDeprecated
  | nameSetRegex
  | nameSetName
  ;

nameSetName : name;

nameSetRegex : REGEX;

nameSetRegexDeprecated : deprecatedRegex;

// ---- Enum set ----

enumSetSpec : enumSetTerm ( COMMA enumSetTerm )*;

enumSetTerm
  : enumSetBase
  | enumSetNotTerm
  ;

enumSetBase
  : enumSetRegexDeprecated
  | enumSetRegex
  | enumSetValue
  ;

enumSetValue : NAME;

enumSetRegex : REGEX;

enumSetRegexDeprecated : deprecatedRegex;

enumSetNotTerm : BANG enumSetBase;

// ---- Deprecated bare regex ----

// A deprecated regex is a name-shaped token containing '*', lexed as
// DEPRECATED_REGEX.
deprecatedRegex : DEPRECATED_REGEX;
