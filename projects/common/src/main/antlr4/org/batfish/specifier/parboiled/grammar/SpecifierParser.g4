parser grammar SpecifierParser;

options {
  tokenVocab = SpecifierLexer;
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

nodeRoleAndDimensionTail : COMMA nodeRoleName CLOSE_PAREN;

nodeRoleDimensionName : name;

nodeRoleName : name;

nodeType : AT_DEVICE_TYPE OPEN_PAREN nodeTypeSpec CLOSE_PAREN;

nodeTypeSpec : NAME;

nodeName : name;

nodeNameRegex : REGEX;

nodeNameRegexDeprecated : deprecatedRegex;

nodeParens : OPEN_PAREN nodeSpec CLOSE_PAREN;

// ---- Interface ----

interfaceSpec : interfaceIntersection ( ( COMMA | BACKSLASH ) interfaceIntersection )*;

interfaceIntersection : interfaceTerm ( AMP interfaceTerm )*;

interfaceTerm
  : interfaceWithNode
  | interfaceWithoutNode
  | interfaceParens
  ;

interfaceWithNode : nodeTerm interfaceWithNodeTail;

interfaceWithNodeTail : OPEN_BRACKET interfaceWithoutNode CLOSE_BRACKET;

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

interfaceConnectedTo : AT_CONNECTED_TO OPEN_PAREN ipSpaceSpec CLOSE_PAREN;

interfaceInterfaceGroup : AT_INTERFACE_GROUP interfaceGroupAndReferenceBook;

interfaceGroupAndReferenceBook : OPEN_PAREN referenceBook interfaceGroupAndReferenceBookTail;

interfaceGroupAndReferenceBookTail : COMMA interfaceGroup CLOSE_PAREN;

interfaceGroup : name;

interfaceType : AT_INTERFACE_TYPE OPEN_PAREN interfaceTypeSpec CLOSE_PAREN;

interfaceTypeSpec : NAME;

interfaceVrf : AT_VRF OPEN_PAREN vrfName CLOSE_PAREN;

vrfName : name;

interfaceZone : AT_ZONE OPEN_PAREN zoneName CLOSE_PAREN;

zoneName : name;

interfaceName : name;

interfaceNameRegex : REGEX;

interfaceNameRegexDeprecated : deprecatedRegex;

interfaceParens : OPEN_PAREN interfaceSpec CLOSE_PAREN;

interfaceWithoutNodeParens : OPEN_PAREN interfaceWithoutNode CLOSE_PAREN;

// ---- Filter ----

filterSpec : filterIntersection ( ( COMMA | BACKSLASH ) filterIntersection )*;

filterIntersection : filterTerm ( AMP filterTerm )*;

filterTerm
  : filterWithNode
  | filterWithoutNode
  | filterParens
  ;

filterWithNode : nodeTerm filterWithNodeTail;

filterWithNodeTail : OPEN_BRACKET filterWithoutNode CLOSE_BRACKET;

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

filterInterfaceIn : AT_IN OPEN_PAREN interfaceSpec CLOSE_PAREN;

filterInterfaceOut : AT_OUT OPEN_PAREN interfaceSpec CLOSE_PAREN;

filterName : name;

filterNameRegex : REGEX;

filterNameRegexDeprecated : deprecatedRegex;

filterParens : OPEN_PAREN filterSpec CLOSE_PAREN;

filterWithoutNodeParens : OPEN_PAREN filterWithoutNode CLOSE_PAREN;

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

ipSpaceParens : OPEN_PAREN ipSpaceSpec CLOSE_PAREN;

ipSpaceAddressGroup : AT_ADDRESS_GROUP addressGroupAndReferenceBook;

addressGroupAndReferenceBook : OPEN_PAREN referenceBook addressGroupAndReferenceBookTail;

addressGroupAndReferenceBookTail : COMMA addressGroup CLOSE_PAREN;

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

// 'internet' is a soft keyword: it lexes as NAME and is matched by text in the
// visitor. Optionally followed by an interface-with-node tail.
locationInternet : name interfaceWithNodeTail?;

locationEnter : AT_ENTER OPEN_PAREN locationInterface CLOSE_PAREN;

locationInterface
  : interfaceWithNode
  | nodeTerm
  | interfaceFunc
  ;

locationParens : OPEN_PAREN locationSpec CLOSE_PAREN;

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

routingPolicyParens : OPEN_PAREN routingPolicySpec CLOSE_PAREN;

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
  : appName
  | appNameRegex
  | appIcmpTerm
  | appTcpTerm
  | appUdpTerm
  ;

appName : NAME;

appNameRegex : REGEX;

appIcmpTerm : NAME appIcmpType?;

appIcmpType : SLASH NUM appIcmpTypeCode?;

appIcmpTypeCode : SLASH NUM;

appTcpTerm : NAME appPortSpec?;

appUdpTerm : NAME appPortSpec?;

appPortSpec : SLASH appPortTerm ( COMMA appPortTerm )*;

appPortTerm
  : appPortRange
  | appPort
  ;

appPort : NUM;

appPortRange : NUM DASH NUM;

// ---- Single application ----

oneAppSpec
  : appName
  | oneAppIcmp
  | oneAppTcp
  | oneAppUdp
  ;

oneAppIcmp : NAME SLASH NUM oneAppIcmpType;

oneAppIcmpType : SLASH NUM;

oneAppTcp : NAME SLASH appPort;

oneAppUdp : NAME SLASH appPort;

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
