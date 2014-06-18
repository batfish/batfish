grammar JuniperGrammar;

options {
  superClass = ConfigurationParser;
}
import JuniperGrammar_firewall, JuniperGrammar_interface, JuniperGrammar_routing_options, JuniperGrammar_ospf, JuniperGrammar_policy_options, JuniperGrammar_bgp;


tokens {
  ACCEPT                      = 'accept';
  ACCESS                      = 'access';
  ACCOUNTING                  = 'accounting';
  ADD                         = 'add';
  ADDRESS                     = 'address';
  AGGREGATED_ETHER_OPTIONS    = 'aggregated-ether-options';
  AREA                        = 'area';
  ARP_RESP                    = 'arp-resp';
  AS_PATH                     = 'as-path';
  AUTHENTICATION_KEY          = 'authentication-key';
  AUTHENTICATION_ORDER        = 'authentication-order';
  AUTONOMOUS_SYSTEM           = 'autonomous-system';
  BGP                         = 'bgp';
  BRIDGE                      = 'bridge';
  BRIDGE_DOMAINS              = 'bridge-domains';
  CHASSIS                     = 'chassis';
  CLASS                       = 'class';
  CLUSTER                     = 'cluster';
  COMMUNITY                   = 'community';
  COUNT                       = 'count';
  DAMPING                     = 'damping';
  DELETE                      = 'delete';
  DESCRIPTION                 = 'description';
  DESTINATION_ADDRESS         = 'destination-address';
  DESTINATION_PORT            = 'destination-port';
  DISABLE                     = 'disable';
  DISCARD                     = 'discard';
  DOMAIN                      = 'domain';
  DOMAIN_NAME                 = 'domain-name';
  DOMAIN_SEARCH               = 'domain-search';
  ENABLE                      = 'enable';
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
  FORWARDING_OPTIONS          = 'forwarding-options';
  FROM                        = 'from';
  FTP                         = 'ftp';
  GENERATE                    = 'generate';
  GIGETHER_OPTIONS            = 'gigether-options';
  GROUP                       = 'group';
  HOLD_TIME                   = 'hold-time';
  HOST                        = 'host';
  HOST_NAME                   = 'host-name';
  ICMP                        = 'icmp';
  ICMP_TYPE                   = 'icmp-type';
  IGMP                        = 'igmp';
  IGMP_SNOOPING               = 'igmp-snooping';
  IMPORT                      = 'import';
  INET                        = 'inet';
  INET6                       = 'inet6';
  INPUT                       = 'input';
  INSTALL                     = 'install';
  INTERFACE                   = 'interface';
  INTERFACE_MODE              = 'interface-mode';
  INTERFACES                  = 'interfaces';
  INTERNAL                    = 'internal';
  IP                          = 'ip';
  LICENSE                     = 'license';
  LLDP                        = 'lldp';
  LLDP_MED                    = 'lldp-med';
  LOCAL_ADDRESS               = 'local-address';
  LOCAL_AS                    = 'local-as';
  LOCAL_PREFERENCE            = 'local-preference';
  LOG                         = 'log';
  LOG_UPDOWN                  = 'log-updown';
  LOGIN                       = 'login';
  MAX_CONFIGURATIONS_ON_FLASH = 'max-configurations-on-flash';
  MAX_CONFIGURATION_ROLLBACKS = 'max-configuration-rollbacks';
  METRIC                      = 'metric';
  MEMBERS                     = 'members';
  MLD                         = 'mld';
  MTU                         = 'mtu';
  MULTIHOP                    = 'multihop';
  NAME_SERVER                 = 'name-server';
  NATIVE_VLAN_ID              = 'native-vlan-id';
  NEIGHBOR                    = 'neighbor';
  NETWORK_SUMMARY_EXPORT      = 'network-summary-export';
  NEXT                        = 'next';
  NEXT_HOP                    = 'next-hop';
  NO_REDIRECTS                = 'no-redirects';
  NSSA                        = 'nssa';
  NTP                         = 'ntp';
  ORLONGER                    = 'orlonger';
  OSPF                        = 'ospf';
  OSPF3                       = 'ospf3';
  OUTPUT                      = 'output';
  PEER_AS                     = 'peer-as';
  PIM                         = 'pim';
  POE                         = 'poe';
  POLICY                      = 'policy';
  POLICY_OPTIONS              = 'policy-options';
  POLICY_STATEMENT            = 'policy-statement';
  PORTS                       = 'ports';
  PORT_MODE                   = 'port-mode';
  PREFIX_LENGTH_RANGE         = 'prefix-length-range';
  PREFIX_LIST                 = 'prefix-list';
  PRIMARY                     = 'primary';
  PROTOCOL                    = 'protocol';
  PROTOCOLS                   = 'protocols';
  READVERTISE                 = 'readvertise';
  REJECT                      = 'reject';
  REMOVE_PRIVATE              = 'remove-private';
  RIB                         = 'rib';
  ROOT_AUTHENTICATION         = 'root-authentication';
  ROUTE                       = 'route';
  ROUTE_FILTER                = 'route-filter';
  ROUTER_ADVERTISEMENT        = 'router-advertisement';
  ROUTER_ID                   = 'router-id';
  ROUTING_OPTIONS             = 'routing-options';
  RPF_CHECK                   = 'rpf-check';
  RSTP                        = 'rstp';
  SAMPLE                      = 'sample';
  SAMPLING                    = 'sampling';
  SECURITY                    = 'security';
  SERVICES                    = 'services';
  SET                         = 'set';
  SNMP                        = 'snmp';
  SOURCE_ADDRESS              = 'source-address';
  SOURCE_PORT                 = 'source-port';
  SSH                         = 'ssh';
  STATIC                      = 'static';
  SYSLOG                      = 'syslog';
  SYSTEM                      = 'system';
  TACACS                      = 'tacacs';
  TACPLUS_SERVER              = 'tacplus-server';
  TAG                         = 'tag';
  TCP                         = 'tcp';
  TELNET                      = 'telnet';
  TERM                        = 'term';
  TFTP                        = 'tftp';
  THEN                        = 'then';
  THROUGH                     = 'through';
  TIME_ZONE                   = 'time-zone';
  TRACEOPTIONS                = 'traceoptions';
  TRAPS                       = 'traps';
  TRUNK                       = 'trunk';
  TYPE                        = 'type';
  UDP                         = 'udp';
  UNIT                        = 'unit';
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
	String errorMessage = "JuniperGrammar: " + hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	List<String> allErrors = new ArrayList<String>();
	allErrors.addAll(errors);
	allErrors.addAll(gJuniperGrammar_bgp.getErrors());
	allErrors.addAll(gJuniperGrammar_interface.getErrors());
	allErrors.addAll(gJuniperGrammar_ospf.getErrors());
	allErrors.addAll(gJuniperGrammar_policy_options.getErrors());
	allErrors.addAll(gJuniperGrammar_routing_options.getErrors());
	allErrors.addAll(gJuniperGrammar_firewall.getErrors());
	return allErrors;
}

public int nextIntVal() {
	return Integer.valueOf(input.LT(1).getText());
}
}

juniper_configuration returns [JuniperConfiguration jc = new JuniperConfiguration()]
  :
  (x=j_stanza_list EOF) 
                       {
                        for (JStanza js : x) {
                        	jc.processStanza(js);
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
    x=firewall_stanza
    | x=interfaces_stanza
    | x=null_stanza
    | x=policy_options_stanza
    | x=protocols_stanza
    | x=routing_options_stanza
    | x=system_stanza
  )
  
  {
   js = x;
  }
  ;

accounting_sys_stanza
  :
  ACCOUNTING OPEN_BRACE substanza+ CLOSE_BRACE
  ;

authentication_order_sys_stanza
  :
  AUTHENTICATION_ORDER ~SEMICOLON* SEMICOLON
  ;

bridge_domains_stanza
  :
  BRIDGE_DOMAINS OPEN_BRACE substanza+ CLOSE_BRACE
  ;

chassis_stanza
  :
  CHASSIS OPEN_BRACE substanza+ CLOSE_BRACE
  ;

domain_name_sys_stanza
  :
  DOMAIN_NAME ~SEMICOLON* SEMICOLON
  ;

domain_search_sys_stanza
  :
  DOMAIN_SEARCH ~SEMICOLON* SEMICOLON
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

ethernet_switching_options_stanza
  :
  ETHERNET_SWITCHING_OPTIONS OPEN_BRACE substanza+ CLOSE_BRACE
  ;

forwarding_options_stanza
  :
  FORWARDING_OPTIONS OPEN_BRACE substanza+ CLOSE_BRACE
  ;

host_name_sys_stanza returns [SysStanza ss]
  :
  (HOST_NAME name=VARIABLE SEMICOLON) 
                                     {
                                      ss = new HostNameSysStanza(name.getText());
                                     }
  ;

igmp_snooping_p_stanza
  :
  IGMP_SNOOPING OPEN_BRACE substanza+ CLOSE_BRACE
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

license_sys_stanza
  :
  LICENSE OPEN_BRACE substanza+ CLOSE_BRACE
  ;

lldp_p_stanza
  :
  (LLDP OPEN_BRACE substanza+ CLOSE_BRACE)
  ;

lldp_med_p_stanza
  :
  (LLDP_MED OPEN_BRACE substanza+ CLOSE_BRACE)
  ;

login_sys_stanza
  :
  LOGIN OPEN_BRACE substanza+ CLOSE_BRACE
  ;

max_configurations_on_flash_sys_stanza
  :
  MAX_CONFIGURATIONS_ON_FLASH ~SEMICOLON* SEMICOLON
  ;

max_configuration_rollbacks_sys_stanza
  :
  MAX_CONFIGURATION_ROLLBACKS ~SEMICOLON* SEMICOLON
  ;

mld_p_stanza
  :
  MLD OPEN_BRACE substanza+ CLOSE_BRACE
  ;

name_server_sys_stanza
  :
  NAME_SERVER OPEN_BRACE ~CLOSE_BRACE* CLOSE_BRACE
  ;

not_brace
  :
  ~(
    OPEN_BRACE
    | CLOSE_BRACE
   )
  ;

ntp_sys_stanza
  :
  NTP OPEN_BRACE substanza+ CLOSE_BRACE
  ;

null_stanza returns [JStanza js = new NullJStanza()]
  :
  (
    bridge_domains_stanza
    | chassis_stanza
    | ethernet_switching_options_stanza
    | forwarding_options_stanza
    | poe_stanza
    | security_stanza
    | services_stanza
    | snmp_stanza
    | version_stanza
    | virtual_chassis_stanza
    | vlans_stanza
  )
  ;

null_p_stanza returns [PStanza ps = new NullPStanza()]
  :
  igmp_snooping_p_stanza
  | lldp_p_stanza
  | lldp_med_p_stanza
  | mld_p_stanza
  | ospf3_p_stanza
  | pim_p_stanza
  | router_advertisement_p_stanza
  | rstp_p_stanza
  | vstp_p_stanza
  ;

null_sys_stanza returns [SysStanza ss= new NullSysStanza()]
  :
  accounting_sys_stanza
  | authentication_order_sys_stanza
  | domain_name_sys_stanza
  | domain_search_sys_stanza
  | login_sys_stanza
  | license_sys_stanza
  | max_configurations_on_flash_sys_stanza
  | max_configuration_rollbacks_sys_stanza
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
  OSPF3 OPEN_BRACE substanza+ CLOSE_BRACE
  ;

p_stanza returns [PStanza ps]
  :
  (
    x=bgp_p_stanza
    | x=null_p_stanza
    | x=ospf_p_stanza
  )
  
  {
   ps = x;
  }
  ;

p_stanza_list returns [List<PStanza> ps = new ArrayList<PStanza>()]
  :
  ( (x=p_stanza) 
                {
                 ps.add(x);
                })+
  ;

pim_p_stanza
  :
  PIM OPEN_BRACE substanza+ CLOSE_BRACE
  ;

poe_stanza
  :
  POE OPEN_BRACE substanza+ CLOSE_BRACE
  ;

ports_sys_stanza
  :
  PORTS OPEN_BRACE ~CLOSE_BRACE* CLOSE_BRACE
  ;

protocols_stanza returns [JStanza js]
  :
  (PROTOCOLS OPEN_BRACE pl=p_stanza_list CLOSE_BRACE) 
                                                     {
                                                      ProtocolsStanza ps = new ProtocolsStanza();
                                                      for (PStanza x : pl) {
                                                      	ps.processStanza(x);
                                                      }
                                                      js = ps;
                                                     }
  ;

root_authentication_sys_stanza
  :
  ROOT_AUTHENTICATION OPEN_BRACE ~CLOSE_BRACE* CLOSE_BRACE
  ;

router_advertisement_p_stanza
  :
  ROUTER_ADVERTISEMENT OPEN_BRACE substanza+ CLOSE_BRACE
  ;

rstp_p_stanza
  :
  RSTP OPEN_BRACE substanza+ CLOSE_BRACE
  ;

security_stanza
  :
  SECURITY OPEN_BRACE substanza+ CLOSE_BRACE
  ;

services_stanza
  :
  SERVICES OPEN_BRACE ~CLOSE_BRACE* CLOSE_BRACE
  ;

services_sys_stanza
  :
  SERVICES OPEN_BRACE substanza+ CLOSE_BRACE
  ;

snmp_stanza
  :
  SNMP OPEN_BRACE substanza+ CLOSE_BRACE
  ;

substanza
  :
  (
    (not_brace)
    | (OPEN_BRACE substanza+ CLOSE_BRACE)
    | (OPEN_BRACE CLOSE_BRACE)
  )
  ;

sys_stanza returns [SysStanza ss]
  :
  (
    x=host_name_sys_stanza
    | x=null_sys_stanza
  )
  
  {
   ss = x;
  }
  ;

sys_stanza_list returns [List<SysStanza> ssl = new ArrayList<SysStanza>()]
  :
  ( (x=sys_stanza) 
                  {
                   ssl.add(x);
                  })+
  ;

syslog_sys_stanza
  :
  SYSLOG OPEN_BRACE substanza+ CLOSE_BRACE
  ;

system_stanza returns [JStanza js]
  :
  (SYSTEM OPEN_BRACE l=sys_stanza_list CLOSE_BRACE) 
                                                   {
                                                    SystemStanza ss = new SystemStanza();
                                                    
                                                    for (SysStanza s : l) {
                                                    	ss.processStanza(s);
                                                    }
                                                    js = ss;
                                                   }
  ;

tacplus_server_sys_stanza
  :
  TACPLUS_SERVER OPEN_BRACE substanza+ CLOSE_BRACE
  ;

time_zone_sys_stanza
  :
  TIME_ZONE ~SEMICOLON* SEMICOLON
  ;

version_stanza
  :
  VERSION ~SEMICOLON* SEMICOLON
  ;

virtual_chassis_stanza
  :
  VIRTUAL_CHASSIS OPEN_BRACE substanza+ CLOSE_BRACE
  ;

vlan_stanza
  :
  VLAN OPEN_BRACE substanza+ CLOSE_BRACE
  ;

vlans_stanza
  :
  VLANS OPEN_BRACE substanza+ CLOSE_BRACE
  ;

vstp_p_stanza
  :
  VSTP OPEN_BRACE substanza+ CLOSE_BRACE
  ;

AMPERSAND
  :
  '&'
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
  (
    DIGIT DIGIT* '.' DIGIT+ LETTER+
    | DIGIT DIGIT* '.' '*'
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
