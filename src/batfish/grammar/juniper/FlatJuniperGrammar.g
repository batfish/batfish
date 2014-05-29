grammar FlatJuniperGrammar;

options {
  superClass = ConfigurationParser;
}
import FlatJuniperGrammar_firewall, FlatJuniperGrammar_interface, FlatJuniperGrammar_routing_options, FlatJuniperGrammar_ospf, FlatJuniperGrammar_policy_options, FlatJuniperGrammar_bgp;


tokens {
  ACCEPT                 = 'accept';
  ACCESS                 = 'access';
  ADDRESS                = 'address';
  AREA                   = 'area';
  AS_PATH                = 'as-path';
  AUTHENTICATION_KEY     = 'authentication-key';
  AUTHENTICATION_ORDER   = 'authentication-order';
  AUTONOMOUS_SYSTEM      = 'autonomous-system';
  BGP                    = 'bgp';
  BRIDGE                 = 'bridge';
  BRIDGE_DOMAINS         = 'bridge-domains';
  CHASSIS                = 'chassis';
  CLASS                  = 'class';
  CLUSTER                = 'cluster';
  COMMUNITY              = 'community';
  COUNT                  = 'count';
  DAMPING                = 'damping';
  DESCRIPTION            = 'description';
  DESTINATION_ADDRESS    = 'destination-address';
  DESTINATION_PORT       = 'destination-port';
  DISABLE                = 'disable';
  DISCARD                = 'discard';
  DOMAIN                 = 'domain';
  DOMAIN_NAME            = 'domain-name';
  EXACT                  = 'exact';
  EXPORT                 = 'export';
  EXTERNAL               = 'external';
  FAMILY                 = 'family';
  FILE                   = 'file';
  FILTER                 = 'filter';
  FIREWALL               = 'firewall';
  FORWARDING_OPTIONS     = 'forwarding-options';
  FROM                   = 'from';
  FTP                    = 'ftp';
  GENERATE               = 'generate';
  GROUP                  = 'group';
  HOST                   = 'host';
  HOST_NAME              = 'host-name';
  ICMP                   = 'icmp';
  IGMP                   = 'igmp';
  IMPORT                 = 'import';
  INET                   = 'inet';
  INET6                  = 'inet6';
  INTERFACE              = 'interface';
  INTERFACE_MODE         = 'interface-mode';
  INTERFACES             = 'interfaces';
  INTERNAL               = 'internal';
  IP                     = 'ip';
  LLDP                   = 'lldp';
  LOCAL_ADDRESS          = 'local-address';
  LOCAL_AS               = 'local-as';
  LOCAL_PREFERENCE       = 'local-preference';
  LOG                    = 'log';
  LOG_UPDOWN             = 'log-updown';
  LOGIN                  = 'login';
  METRIC                 = 'metric';
  MEMBERS                = 'members';
  MTU                    = 'mtu';
  MULTIHOP               = 'multihop';
  NAME_SERVER            = 'name-server';
  NEIGHBOR               = 'neighbor';
  NETWORK_SUMMARY_EXPORT = 'network-summary-export';
  NEXT                   = 'next';
  NEXT_HOP               = 'next-hop';
  NO_REDIRECTS           = 'no-redirects';
  NTP                    = 'ntp';
  ORLONGER               = 'orlonger';
  OSPF                   = 'ospf';
  OSPF3                  = 'ospf3';
  PEER_AS                = 'peer-as';
  PIM                    = 'pim';
  POLICY                 = 'policy';
  POLICY_OPTIONS         = 'policy-options';
  POLICY_STATEMENT       = 'policy-statement';
  PORTS                  = 'ports';
  PREFIX_LENGTH_RANGE    = 'prefix-length-range';
  PREFIX_LIST            = 'prefix-list';
  PROTOCOL               = 'protocol';
  PROTOCOLS              = 'protocols';
  REJECT                 = 'reject';
  REMOVE_PRIVATE         = 'remove-private';
  RIB                    = 'rib';
  ROOT_AUTHENTICATION    = 'root-authentication';
  ROUTE                  = 'route';
  ROUTE_FILTER           = 'route-filter';
  ROUTER_ADVERTISEMENT   = 'router-advertisement';
  ROUTER_ID              = 'router-id';
  ROUTING_OPTIONS        = 'routing-options';
  SAMPLE                 = 'sample';
  SET                    = 'set';
  SERVICES               = 'services';
  SNMP                   = 'snmp';
  SOURCE_ADDRESS         = 'source-address';
  SSH                    = 'ssh';
  STATIC                 = 'static';
  SYSLOG                 = 'syslog';
  SYSTEM                 = 'system';
  TACACS                 = 'tacacs';
  TACPLUS_SERVER         = 'tacplus-server';
  TAG                    = 'tag';
  TCP                    = 'tcp';
  TELNET                 = 'telnet';
  TERM                   = 'term';
  TFTP                   = 'tftp';
  THEN                   = 'then';
  THROUGH                = 'through';
  TIME_ZONE              = 'time-zone';
  TRAPS                  = 'traps';
  TRUNK                  = 'trunk';
  TYPE                   = 'type';
  UDP                    = 'udp';
  UNIT                   = 'unit';
  USER                   = 'user';
  VERSION                = 'version';
  VLAN_ID                = 'vlan-id';
  VLAN_ID_LIST           = 'vlan-id-list';
  VSTP                   = 'vstp';
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
package batfish.grammar.juniper;

import batfish.grammar.ConfigurationParser;

import batfish.grammar.juniper.bgp.*;
import batfish.grammar.juniper.interfaces.*;
import batfish.grammar.juniper.ospf.*;
import batfish.grammar.juniper.policy_options.*;
import batfish.grammar.juniper.routing_options.*;
import batfish.grammar.juniper.firewall.*;

import batfish.representation.VendorConfiguration;
import batfish.representation.SwitchportMode;

import batfish.representation.juniper.*;

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
	String errorMessage = "FlatJuniperGrammar: " + hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	List<String> allErrors = new ArrayList<String>();
	allErrors.addAll(errors);
	allErrors.addAll(gFlatJuniperGrammar_bgp.getErrors());
	allErrors.addAll(gFlatJuniperGrammar_interface.getErrors());
	allErrors.addAll(gFlatJuniperGrammar_ospf.getErrors());
	allErrors.addAll(gFlatJuniperGrammar_policy_options.getErrors());
	allErrors.addAll(gFlatJuniperGrammar_routing_options.getErrors());
	allErrors.addAll(gFlatJuniperGrammar_firewall.getErrors());
	return allErrors;
}

public int nextIntVal() {
	return Integer.valueOf(input.LT(1).getText());
}
}

juniper_configuration returns [FlatJuniperConfiguration fjc = new FlatJuniperConfiguration()]
  :
  (x=j_stanza_list EOF) 
                       {
                        for (JStanza js : x) {
                        	fjc.processStanza(js);
                        }
                       }
  ;

j_stanza_list returns [List<JStanza> jslist = new ArrayList<JStanza>()]
  :
  (x=j_stanza 
             {
              jslist.add(x);
             })+
  ;

j_stanza returns [JStanza js]
  :
  (
    SET
    (
      x=firewall_stanza
      | x=interfaces_stanza
      | x=null_stanza
      | x=policy_options_stanza
      | x=protocols_stanza
      | x=routing_options_stanza
      | x=system_stanza
    )
  )
  
  {
   js = x;
  }
  ;

authentication_order_sys_stanza
  :
  AUTHENTICATION_ORDER ~NEWLINE* NEWLINE
  ;

bridge_domains_stanza
  :
  BRIDGE_DOMAINS ~NEWLINE* NEWLINE
  ;

chassis_stanza
  :
  CHASSIS ~NEWLINE* NEWLINE
  ;

domain_name_sys_stanza
  :
  DOMAIN_NAME ~NEWLINE* NEWLINE
  ;

double_num returns [double i]
  :
  (
    x=DEC
    | x=HEX
  )
  
  {
   i = Double.parseDouble(x.getText());
  }
  ;

forwarding_options_stanza
  :
  FORWARDING_OPTIONS ~NEWLINE+ NEWLINE
  ;

host_name_sys_stanza returns [SysStanza ss]
  :
  (HOST_NAME name=VARIABLE NEWLINE) 
                                   {
                                    ss = new HostNameSysStanza(name.getText());
                                   }
  ;

integer returns [int i]
  :
  (
    x=DEC
    | x=HEX
  )
  
  {
   i = Integer.parseInt(x.getText());
  }
  ;

lldp_p_stanza
  :
  (LLDP ~NEWLINE* NEWLINE)
  ;

login_sys_server
  :
  LOGIN ~NEWLINE* NEWLINE
  ;

name_server_sys_stanza
  :
  NAME_SERVER ~NEWLINE* NEWLINE
  ;

ntp_sys_stanza
  :
  NTP ~NEWLINE* NEWLINE
  ;

null_stanza returns [JStanza js = new NullJStanza()]
  :
  (
    bridge_domains_stanza
    | chassis_stanza
    | forwarding_options_stanza
    | services_stanza
    | snmp_stanza
    | version_stanza
  )
  ;

null_p_stanza returns [PStanza ps = new NullPStanza()]
  :
  lldp_p_stanza
  | ospf3_p_stanza
  | router_advertisement_p_stanza
  | vstp_p_stanza
  ;

null_sys_stanza returns [SysStanza ss= new NullSysStanza()]
  :
  authentication_order_sys_stanza
  | domain_name_sys_stanza
  | login_sys_server
  | name_server_sys_stanza
  | ntp_sys_stanza
  | ports_sys_stanza
  | root_authentication_sys_stanza
  | services_sys_stanza
  | syslog_sys_stanza
  | tacplus_server_sys_stanza
  | time_zone_sys_stanza
  ;

ospf3_p_stanza
  :
  OSPF3 ~NEWLINE* NEWLINE
  ;

p_stanza returns [PStanza ps]
  :
  (
    PROTOCOLS
    (
      x=bgp_p_stanza
      | x=null_p_stanza
      | x=ospf_p_stanza
    )
  )
  
  {
   ps = x;
  }
  ;

ports_sys_stanza
  :
  PORTS ~NEWLINE* NEWLINE
  ;

protocols_stanza returns [JStanza js]
  :
  (pl=p_stanza) 
               {
                FlatProtocolsStanza ps = new FlatProtocolsStanza();
                ps.processStanza(pl);
                js = ps;
               }
  ;

root_authentication_sys_stanza
  :
  ROOT_AUTHENTICATION ~NEWLINE* NEWLINE
  ;

router_advertisement_p_stanza
  :
  ROUTER_ADVERTISEMENT ~NEWLINE* NEWLINE
  ;

services_stanza
  :
  SERVICES ~NEWLINE* NEWLINE
  ;

services_sys_stanza
  :
  SERVICES ~NEWLINE* NEWLINE
  ;

snmp_stanza
  :
  SNMP ~NEWLINE* NEWLINE
  ;

sys_stanza returns [SysStanza ss]
  :
  (
    SYSTEM
    (
      x=host_name_sys_stanza
      | x=null_sys_stanza
    )
  )
  
  {
   ss = x;
  }
  ;

syslog_sys_stanza
  :
  SYSLOG ~NEWLINE* NEWLINE
  ;

system_stanza returns [JStanza js]
  :
  (l=sys_stanza) 
                {
                 SystemStanza ss = new SystemStanza();
                 ss.processStanza(l);
                 js = ss;
                }
  ;

tacplus_server_sys_stanza
  :
  TACPLUS_SERVER ~NEWLINE* NEWLINE
  ;

time_zone_sys_stanza
  :
  TIME_ZONE ~NEWLINE* NEWLINE
  ;

version_stanza
  :
  VERSION ~NEWLINE* NEWLINE
  ;

vstp_p_stanza
  :
  VSTP ~NEWLINE* NEWLINE
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

DOLLAR
  :
  '$'
  ;

DEC
  :
  '0'
  | POSITIVE_DIGIT DIGIT*
  ;

DOUBLE_QUOTE
  :
  '"'
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
  ('#' ~'\n'* '\n') 
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
