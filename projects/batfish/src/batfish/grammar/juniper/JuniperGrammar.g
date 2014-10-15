grammar JuniperGrammar;

options {
  superClass = ConfigurationParser;
}
import  JuniperGrammar_firewall, 
        JuniperGrammar_groups,
        JuniperGrammar_interface, 
        JuniperGrammar_policy_options, 
        JuniperGrammar_protocols, 
        JuniperGrammar_ospf, 
        JuniperGrammar_bgp, 
        JuniperGrammar_routing_options,
        JuniperGrammar_system; 


tokens {
  ACCEPT                      = 'accept';
  ACCESS                      = 'access';
  ACCOUNTING                  = 'accounting';
  ACTIVE                      = 'active';
  ADD                         = 'add';
  ADDRESS                     = 'address';
  ADDRESS_MASK                = 'address-mask';
  AGGREGATE                   = 'aggregate';
  AGGREGATED_ETHER_OPTIONS    = 'aggregated-ether-options';
  ALLOW                       = 'allow';
  APPLY_GROUPS                = 'apply-groups';
  APPLY_GROUPS_EXCEPT         = 'apply-groups-except';
  APPLY_PATH                  = 'apply-path';
  AREA                        = 'area';
  ARP_RESP                    = 'arp-resp';
  AS_PATH                     = 'as-path';
  AS_PATH_PREPEND             = 'as-path-prepend';
  AUTHENTICATION_KEY          = 'authentication-key';
  AUTHENTICATION_ORDER        = 'authentication-order';
  AUTONOMOUS_SYSTEM           = 'autonomous-system';
  BFD                         = 'bfd';
  BFD_LIVENESS_DETECTION      = 'bfd-liveness-detection';
  BGP                         = 'bgp';
  BRIDGE                      = 'bridge';
  BRIDGE_DOMAINS              = 'bridge-domains';
  CCC                         = 'ccc';
  CHASSIS                     = 'chassis';
  CLASS                       = 'class';
  CLASS_OF_SERVICE            = 'class-of-service';
  CLUSTER                     = 'cluster';
  COMMUNITY                   = 'community';
  CONNECTIONS                 = 'connections';
  COUNT                       = 'count';
  DAMPING                     = 'damping';
  DATA_REMOVED                = 'Data Removed';
  DEFAULTS                    = 'defaults';
  DELETE                      = 'delete';
  DESCRIPTION                 = 'description';
  DESTINATION_ADDRESS         = 'destination-address';
  DESTINATION_PORT            = 'destination-port';
  DIRECT                      = 'direct';
  DISABLE                     = 'disable';
  DISCARD                     = 'discard';
  DOMAIN                      = 'domain';
  DOMAIN_NAME                 = 'domain-name';
  DOMAIN_SEARCH               = 'domain-search';
  DUMPONPANIC                 = 'dump-on-panic';
  ENABLE                      = 'enable';
  ENCAPSULATION               = 'encapsulation';
  ETHERNET_SWITCHING          = 'ethernet-switching';
  ETHERNET_SWITCHING_OPTIONS  = 'ethernet-switching-options';
  EXACT                       = 'exact';
  EXCEPT                      = 'except';
  EXPORT                      = 'export';
  EXTERNAL                    = 'external';
  FAMILY                      = 'family';
  FILE                        = 'file';
  FILTER                      = 'filter';
  FIREWALL                    = 'firewall';
  FLEXIBLE_VLAN_TAGGING       = 'flexible-vlan-tagging';
  FORWARDING_OPTIONS          = 'forwarding-options';
  FORWARDING_TABLE            = 'forwarding-table';
  FROM                        = 'from';
  FTP                         = 'ftp';
  GENERATE                    = 'generate';
  GIGETHER_OPTIONS            = 'gigether-options';
  GRACEFUL_RESTART            = 'graceful-restart';
  GROUP                       = 'group';
  GROUPS                      = 'groups';
  HOLD_TIME                   = 'hold-time';
  HOST                        = 'host';
  HOST_NAME                   = 'host-name';
  ICMP                        = 'icmp';
  ICMP_TYPE                   = 'icmp-type';
  IGMP                        = 'igmp';
  IGMP_SNOOPING               = 'igmp-snooping';
  IGP                         = 'igp';
  IMPORT                      = 'import';
  IMPORT_RIB                  = 'import-rib';
  INACTIVE                    = 'inactive';
  INET                        = 'inet';
  INET6                       = 'inet6';
  INET_VPN                    = 'inet-vpn';
  INET6_VPN                   = 'inet6-vpn';
  INPUT                       = 'input';
  INPUT_VLAN_MAP              = 'input-vlan-map';
  INSTALL                     = 'install';
  INSTALL_NEXTHOP             = 'install-nexthop';
  INTERFACE                   = 'interface';
  INTERFACE_MODE              = 'interface-mode';
  INTERFACES                  = 'interfaces';
  INTERFACE_ROUTES            = 'interface-routes';
  INTERNAL                    = 'internal';
  IP                          = 'ip';
  ISIS                        = 'isis';
  ISO                         = 'iso';
  L2_CIRCUIT                  = 'l2circuit';
  L2_VPN                      = 'l2vpn';
  LICENSE                     = 'license';
  LDP                         = 'ldp';
  LLDP                        = 'lldp';
  LLDP_MED                    = 'lldp-med';
  LOAD_BALANCE                = 'load-balance';
  LOCAL_ADDRESS               = 'local-address';
  LOCAL_AS                    = 'local-as';
  LOCAL_PREFERENCE            = 'local-preference';
  LOCATION                    = 'location';
  LOG                         = 'log';
  LOG_UPDOWN                  = 'log-updown';
  LOGIN                       = 'login';
  LONGER                      = 'longer';
  MARTIANS                    = 'martians';
  MAX_CONFIGURATIONS_ON_FLASH = 'max-configurations-on-flash';
  MAX_CONFIGURATION_ROLLBACKS = 'max-configuration-rollbacks';
  METRIC                      = 'metric';
  METRIC_OUT                  = 'metric-out';
  MEMBERS                     = 'members';
  MLD                         = 'mld';
  MPLS                        = 'mpls';
  MSDP                        = 'msdp';
  MTU                         = 'mtu';
  MULTICAST                   = 'multicast';
  MULTIHOP                    = 'multihop';
  MULTIPATH                   = 'multipath';
  NAME_SERVER                 = 'name-server';
  NATIVE_VLAN_ID              = 'native-vlan-id';
  NEIGHBOR                    = 'neighbor';
  NETWORK_SUMMARY_EXPORT      = 'network-summary-export';
  NEXT                        = 'next';
  NEXT_HOP                    = 'next-hop';
  NEXT_TABLE                  = 'next-table';
  NO_EXPORT                   = 'no-export';
  NO_INSTALL                  = 'no-install';
  NO_READVERTISE              = 'no-readvertise';
  NO_REDIRECTS                = 'no-redirects';
  NO_RESOLVE                  = 'no-resolve';
  NO_RETAIN                   = 'no-retain';
  NO_NEIGHBOR_LEARN           = 'no-neighbor-learn';
  NSSA                        = 'nssa';
  NTP                         = 'ntp';
  ORLONGER                    = 'orlonger';
  OSPF                        = 'ospf';
  OSPF3                       = 'ospf3';
  OUTPUT                      = 'output';
  PASSIVE                     = 'passive';
  PEER_AS                     = 'peer-as';
  PER_PACKET                  = 'per-packet';
  PIM                         = 'pim';
  POE                         = 'poe';
  POLICY                      = 'policy';
  POLICY_OPTIONS              = 'policy-options';
  POLICY_STATEMENT            = 'policy-statement';
  PORTS                       = 'ports';
  PORT_MODE                   = 'port-mode';
  PREFERENCE                  = 'preference';
  PREFIX_LENGTH_RANGE         = 'prefix-length-range';
  PREFIX_LIST                 = 'prefix-list';
  PREFIX_LIST_FILTER          = 'prefix-list-filter';
  PRIMARY                     = 'primary';
  PROTOCOL                    = 'protocol';
  PROTOCOLS                   = 'protocols';
  RADIUS_OPTIONS              = 'radius-options';
  RADIUS_SERVER               = 'radius-server';
  READVERTISE                 = 'readvertise';
  REJECT                      = 'reject';
  REMOVE_PRIVATE              = 'remove-private';
  REMOVED                     = 'Removed';
  RESOLVE                     = 'resolve';
  RETAIN                      = 'retain';
  RIB                         = 'rib';
  RIB_GROUP                   = 'rib-group';
  RIB_GROUPS                  = 'rib-groups';
  ROOT_AUTHENTICATION         = 'root-authentication';
  ROUTE                       = 'route';
  ROUTE_FILTER                = 'route-filter';
  ROUTER_ADVERTISEMENT        = 'router-advertisement';
  ROUTER_ID                   = 'router-id';
  ROUTING_INSTANCES           = 'routing-instances';
  ROUTING_OPTIONS             = 'routing-options';
  RPF_CHECK                   = 'rpf-check';
  RSTP                        = 'rstp';
  RSVP                        = 'rsvp';
  SAMPLE                      = 'sample';
  SAMPLING                    = 'sampling';
  SECURITY                    = 'security';
  SERVICES                    = 'services';
  SELF                        = 'self';
  SET                         = 'set';
  SNMP                        = 'snmp';
  SOURCE_ADDRESS              = 'source-address';
  SOURCE_ADDRESS_FILTER       = 'source-address-filter';
  SOURCE_PORT                 = 'source-port';
  SSH                         = 'ssh';
  STANZA_REMOVED              = 'Stanza Removed';
  STATIC                      = 'static';
  SUBTRACT                    = 'subtract';
  SYSLOG                      = 'syslog';
  SYSTEM                      = 'system';
  TACACS                      = 'tacacs';
  TACPLUS_SERVER              = 'tacplus-server';
  TAG                         = 'tag';
  TARGET                      = 'target';
  TARGETED_BROADCAST          = 'targeted-broadcast';
  TCP                         = 'tcp';
  TCP_MSS                     = 'tcp-mss';
  TELNET                      = 'telnet';
  TERM                        = 'term';
  TFTP                        = 'tftp';
  THEN                        = 'then';
  THROUGH                     = 'through';
  TIME_ZONE                   = 'time-zone';
  TO                          = 'to';
  TRACEOPTIONS                = 'traceoptions';
  TRAPS                       = 'traps';
  TRUNK                       = 'trunk';
  TYPE                        = 'type';
  UDP                         = 'udp';
  UNIT                        = 'unit';
  UPTO                        = 'upto';
  USER                        = 'user';
  VERSION                     = 'version';
  VIRTUAL_CHASSIS             = 'virtual-chassis';
  VLAN                        = 'vlan';
  VLANS                       = 'vlans';
  VLAN_ID                     = 'vlan-id';
  VLAN_ID_LIST                = 'vlan-id-list';
  VLAN_TAGGING                = 'vlan-tagging';
  VSTP                        = 'vstp';
}

@lexer::header {
package batfish.grammar.juniper;
}

@lexer::members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = hdr + " " + msg;
	errors.add(errorMessage);
}

@Override
public List<String> getErrors() {
	return errors;
}
}

@parser::header {
// TODO[P0]: check
package batfish.grammar.juniper;

import batfish.grammar.ConfigurationParser;

import batfish.grammar.juniper.system.*;

import batfish.grammar.juniper.policy_options.*;
import batfish.grammar.juniper.protocols.*;
import batfish.grammar.juniper.bgp.*;
import batfish.grammar.juniper.ospf.*;

import batfish.grammar.juniper.firewall.*;

import batfish.grammar.juniper.groups.*;
import batfish.grammar.juniper.interfaces.*;
import batfish.grammar.juniper.routing_options.*;

import batfish.representation.VendorConfiguration;
import batfish.representation.SwitchportMode;

import batfish.representation.juniper.*;
import static batfish.representation.juniper.FamilyOps.*;
import static batfish.representation.juniper.ProtocolOps.*;

import batfish.util.SubRange;
}

@parser::members {
@Override
public VendorConfiguration parse_configuration() throws RecognitionException {
	return juniper_configuration().getConfiguration();
}

private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "JuniperGrammar: " + hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
// TODO[P0]: check
	List<String> allErrors = new ArrayList<String>();
	allErrors.addAll(errors);
	allErrors.addAll(gJuniperGrammar_bgp.getErrors());
   allErrors.addAll(gJuniperGrammar_firewall.getErrors());
   allErrors.addAll(gJuniperGrammar_groups.getErrors());
	allErrors.addAll(gJuniperGrammar_interface.getErrors());
	allErrors.addAll(gJuniperGrammar_ospf.getErrors());
	allErrors.addAll(gJuniperGrammar_policy_options.getErrors());
   allErrors.addAll(gJuniperGrammar_protocols.getErrors());
	allErrors.addAll(gJuniperGrammar_routing_options.getErrors());
   allErrors.addAll(gJuniperGrammar_system.getErrors());
	return allErrors;
}

public int nextIntVal() {
  return Integer.valueOf(input.LT(1).getText());
}
}

/* JStanza Rules -------------------------------------------------------------------------------------*/

juniper_configuration returns [JuniperConfiguration jc = new JuniperConfiguration()]
  :
  (x=j_stanza_list EOF)
  {
    for (JStanza js : x) {
      js.postProcessStanza();
      // TODO [P0] : apply groups here
      // TODO [P0] : prefix-list->apply-paths here
    }
  }
  ;

j_stanza_list returns [List<JStanza> jslist = new ArrayList<JStanza>()]
  :
  (x=j_stanza {jslist.add(x);})+
  ;

j_stanza returns [JStanza js]
  :
  (x = apply_groups_stanza 
  |x=firewall_stanza 
  |x=protocols_stanza
  |x=routing_options_stanza
    
  |x=groups_stanza 
  |x=interfaces_stanza 
  |x=policy_options_stanza
  |x=system_stanza
  
  |x=null_stanza
  ) 
  {js = x;}
  ;
  
/* --- JStanza Sub-Stanza  Rules ---------------------------------------------------------------------*/

null_stanza returns [JStanza js]
  :
  (s=chassis_stanza
  |s=class_of_service_stanza
  |s=forwarding_options_stanza
  |s=routing_instances_stanza
  |s=services_stanza
  |s=version_stanza
  |s=removed_top_level_stanza
  )
  {js = new NullJStanza(s);}
  ;

/* --- Null Stanza Rules -------------------------------------------------------------------*/

chassis_stanza returns [String s]
  :
  x=CHASSIS ignored_substanza {s = x.getText() + "{...}";}
  ;

class_of_service_stanza returns [String s]
  :
  x=CLASS_OF_SERVICE ignored_substanza {s = x.getText() + "{...}";}
  ;

forwarding_options_stanza returns [String s]
  :
  x=FORWARDING_OPTIONS ignored_substanza {s = x.getText() + "{...}";}
  ;
  
routing_instances_stanza returns [String s] // TODO [Ask Ari]: probably don't ignore 
  :
  x=ROUTING_INSTANCES ignored_substanza {s = x.getText() + "{...}";}
  ;

services_stanza returns [String s]
  :
  x=SERVICES ignored_substanza {s = x.getText() + "{...}";}
  ;
  
version_stanza returns [String s]
  :
  x=VERSION y=VERSION_TOKEN SEMICOLON {s = x.getText() + " " + y.getText();}
  ;
  
  
/* Basic Rules ---------------------------------------------------------------------------------------*/
as_id returns [String s]
@init {
   s="";
}
  :
  (x=AS_NUM) {s=x.getText();}
  ;
  
bracketed_list returns [ArrayList<String> sl]
@init {
  String line = "";
  sl = new ArrayList<String>();
}
  :
  OPEN_BRACKET         
  ((c1=~CLOSE_BRACKET) 
  {
    line += c1.getText(); 
    // grab rest of member on the line
    List<Token> tokens = ((CommonTokenStream) input).getTokens(input.LT(-1).getTokenIndex(), input.index());

    // hunt for white space to find the end of this item
    boolean hidden_channel = false;
    for (Token t : tokens) {
      if (t.getChannel() == Token.HIDDEN_CHANNEL) {
        hidden_channel = true;
      }
    }
                                            
    // if white space found, line now holds next item
    if (hidden_channel) {
      sl.add(line);
      line = "";
    }
  }
  )+  
  CLOSE_BRACKET
  ;

double_num returns [double i]
  :
  (
    x=DEC
    | x=HEX
  )
  {i = Double.parseDouble(x.getText());}
  ;
    
integer returns [int i]
  :
  (
    x=DEC
    | x=HEX
  )
  {i = Integer.parseInt(x.getText());}
  ;
 
 string_in_double_quotes returns [String s]
  :
  (x = DOUBLE_QUOTED_STRING)
  {
    s = x.getText();
    s = s.substring(1,s.length()-1);
  }
  ;
  
string_up_to_semicolon returns [String s]
@init{
   String linesofar = "";
}
  :
  (
  (x = VARIABLE 
  |x = IPV6_ADDRESS
  |x = COLON
  )
  {linesofar += x.getText();}
  )+
  SEMICOLON
  {
    s = linesofar;
  }
  ;
 
/* Rules for Ignoring---------------------------------------------------------------------------------*/ 

not_brace 
  :
  ~(
    OPEN_BRACE
    | CLOSE_BRACE
   )
  ;

ignored_substanza
  : 
  OPEN_BRACE substanza* CLOSE_BRACE
  ;

substanza
  :
  (
    (not_brace)
    | (OPEN_BRACE substanza+ CLOSE_BRACE)
    | (OPEN_BRACE CLOSE_BRACE)
  )
  ;

removed_stanza returns [String s]
  :
  (
    (name=VARIABLE){s=name.getText();}
    (DATA_REMOVED {s+="Data Removed";}
    |STANZA_REMOVED {s+="Stanza Removed";}
    )
  )
  
  ;

removed_top_level_stanza returns [String s]
  :
  (x=VARIABLE)
  (DATA_REMOVED
  |STANZA_REMOVED
  )
  (CLOSE_BRACE)?
  {s=x.getText() + " removed stanza";}
  ;
  
/*Common/Shared Stanzas ------------------------------------------------------------------------------*/
bfd_liveness_detection_common_stanza returns [String s]
  :
  x=BFD_LIVENESS_DETECTION ignored_substanza {s=x.getText();}
  ;  

description_common_stanza returns [String s]
  :
  y=DESCRIPTION 
  (x=string_in_double_quotes
  |x=string_up_to_semicolon
  )
  SEMICOLON 
  {s=y.getText() + " " + x;}
  ;  
  
encapsulation_common_stanza returns [String s]
  :
  y=ENCAPSULATION x=VARIABLE SEMICOLON {s = y.getText() + " " + x.getText();}
  ;
  
log_updown_common_stanza returns [String s]
  :
  x=LOG_UPDOWN SEMICOLON {s = x.getText();}
  ;
  
metric_out_common_stanza returns [String s]
  :
  x = METRIC_OUT (y=VARIABLE | y=IGP) SEMICOLON {s = x.getText() + " " + y.getText();}
  ; 
  
mtu_common_stanza returns [String s] 
  :
  x=MTU i=integer SEMICOLON {s = x.getText() + " " +Integer.toString(i);}
  ;
  
multihop_common_stanza returns [String s]
  :
  x=MULTIHOP ignored_substanza {s = x.getText() + "{...}";}
  ;

remove_private_common_stanza returns [String s]
  :
  x=REMOVE_PRIVATE SEMICOLON {s=x.getText();}
  ;
  
rib_common_stanza returns [String s]
  :
  RIB x=VARIABLE SEMICOLON {s=x.getText();}
  ;

/*Lexing Rules ---------------------------------------------------------------------------------------*/

AMPERSAND
  :
  '&'
  ;
  
AS_NUM
  :
  (DEC COLON DEC)
  |(DEC COLON ASTERISK)
  |(DEC COLON DEC COLON DEC)
  |(TARGET COLON DEC COLON DEC)
  ;

ASTERISK
  :
  '*'
  ;

CARAT
  :
  '^'
  ;

CLOSE_BRACE
  :
  '}'
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

DASH
  :
  '-'
;

DEC
  :
  '0'
  | POSITIVE_DIGIT DIGIT*
  ;

DOLLAR
  :
  '$'
  ;
  
DOUBLE_QUOTED_STRING
  :
  DOUBLE_QUOTE ~DOUBLE_QUOTE* DOUBLE_QUOTE
  ;
  
FLOAT
  :
  POSITIVE_DIGIT* DIGIT '.'
  (
    '0'
    | DIGIT* POSITIVE_DIGIT
  )
  ;

FORWARD_SLASH
  :
  '/'
  ;

GREATER_THAN
  :
  '>'
  ;

HEX
  :
  '0x' HEX_DIGIT+
  ;
  
INFO_REMOVED

  :
  ('Authentication ' DATA_REMOVED) // TODO: why doesnt this work with variable
                   {
                    skip();
                   }
  ;


IP_ADDRESS
  :
  DEC_BYTE '.' DEC_BYTE '.' DEC_BYTE '.' DEC_BYTE
  ;

IP_ADDRESS_WITH_MASK
  :
  DEC_BYTE '.' DEC_BYTE '.' DEC_BYTE '.' DEC_BYTE '/' DEC_BYTE
  ;

IPV6_ADDRESS
  :
  (COLON COLON ( (HEX_DIGIT+ COLON)* HEX_DIGIT+)?)
  | (HEX_DIGIT+ COLON COLON?)+ (HEX_DIGIT+)?
  ;

IPV6_ADDRESS_WITH_MASK
  :
  (
    (COLON COLON ( (HEX_DIGIT+ COLON)* HEX_DIGIT+)?)
    | (HEX_DIGIT+ COLON COLON?)+ (HEX_DIGIT+)?
  )
  '/' DEC_BYTE
  ;

LESS_THAN
  :
  '<'
  ;

LINE_COMMENT
  :
  ('##' ~'\n'* '\n') 
                   {
                    $channel = HIDDEN;
                   }
  ;


MULTILINE_COMMENT
  :
  OPEN_COMMENT (options {greedy=false;}: .)* CLOSE_COMMENT 
                                                          {
                                                           $channel = HIDDEN;
                                                          }
  ;

NEWLINE
  :
  '\n' 
      {
       $channel = HIDDEN;
      }
  ;

OPEN_BRACE
  :
  '{'
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

SEMICOLON
  :
  ';'
  ;

SINGLE_QUOTE
  :
  '\''
  ;
    
UNDERSCORE
  :
  '_'
  ;
  
UNIT_WILDCARD
   :
   '<*>'
   ;  

VARIABLE
  :
  LETTER
  (
    LETTER
    | DIGIT
    | '-'
    | '_'
    | '.'
    | '/'
  )*
  ;

VERSION_TOKEN
  :
  DIGIT+ '.' (DIGIT | LETTER)+ '-' (DIGIT | LETTER)+
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
CLOSE_COMMENT
  :
  '*/'
  ;

fragment
DEC_BYTE
  :// TODO: This is a hack.
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
DOUBLE_QUOTE
  :
  '"'
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
OPEN_COMMENT
  :
  '/*'
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
