lexer grammar JuniperGrammarLexer;

options {
   superClass = 'batfish.grammar.BatfishLexer';
}

@header {
package batfish.grammar.juniper;
}

@members {
boolean enableIPV6_ADDRESS = true;
boolean enableIP_ADDRESS = true;
boolean enableDEC = true;

@Override
public String printStateVariables() {
   StringBuilder sb = new StringBuilder();
   sb.append("enableIPV6_ADDRESS: " + enableIPV6_ADDRESS + "\n");
   sb.append("enableIP_ADDRESS: " + enableIP_ADDRESS + "\n");
   sb.append("enableDEC: " + enableDEC + "\n");
   return sb.toString();
}

}

// Juniper Keywords

ACCEPT
:
   'accept'
;

ACCESS
:
   'access'
;

ACCOUNTING
:
   'accounting'
;

ACTIVE
:
   'active'
;

ADD
:
   'add'
;

ADDRESS
:
   'address'
;

ADDRESS_MASK
:
   'address-mask'
;

AGGREGATE
:
   'aggregate'
;

AGGREGATED_ETHER_OPTIONS
:
   'aggregated-ether-options'
;

ALLOW
:
   'allow'
;

APPLY_GROUPS
:
   'apply-groups'
;

APPLY_GROUPS_EXCEPT
:
   'apply-groups-except'
;

APPLY_PATH
:
   'apply-path'
;

AREA
:
   'area'
;

ARP
:
   'arp'
;

ARP_RESP
:
   'arp-resp'
;

AS_PATH
:
   'as-path'
;

AS_PATH_PREPEND
:
   'as-path-prepend'
;

AUTHENTICATION_KEY
:
   'authentication-key'
;

AUTHENTICATION_ORDER
:
   'authentication-order'
;

AUTONOMOUS_SYSTEM
:
   'autonomous-system'
;

BACKUP_ROUTER
:
   'backup-router'
;

BFD
:
   'bfd'
;

BFD_LIVENESS_DETECTION
:
   'bfd-liveness-detection'
;

BGP
:
   'bgp'
;

BRIDGE
:
   'bridge'
;

BRIDGE_DOMAINS
:
   'bridge-domains'
;

CCC
:
   'ccc'
;

CHASSIS
:
   'chassis'
;

CLASS
:
   'class'
;

CLASS_OF_SERVICE
:
   'class-of-service'
;

CLUSTER
:
   'cluster'
;

COMMUNITY
:
   'community'
;

CONNECTIONS
:
   'connections'
;

COUNT
:
   'count'
;

DAMPING
:
   'damping'
;

DATA_REMOVED
:
   'Data Removed'
;

DEFAULTS
:
   'defaults'
;

DELETE
:
   'delete'
;

DESCRIPTION
:
   'description'
;

DESTINATION_ADDRESS
:
   'destination-address'
;

DESTINATION_PORT
:
   'destination-port'
;

DIRECT
:
   'direct'
;

DISABLE
:
   'disable'
;

DISCARD
:
   'discard'
;

DOMAIN
:
   'domain'
;

DOMAIN_NAME
:
   'domain-name'
;

DOMAIN_SEARCH
:
   'domain-search'
;

DUMPONPANIC
:
   'dump-on-panic'
;

ENABLE
:
   'enable'
;

ENCAPSULATION
:
   'encapsulation'
;

ETHERNET_SWITCHING
:
   'ethernet-switching'
;

ETHERNET_SWITCHING_OPTIONS
:
   'ethernet-switching-options'
;

EXACT
:
   'exact'
;

EXCEPT
:
   'except'
;

EXPORT
:
   'export'
;

EXPORT_RIB
:
   'export-rib'
;

EXTERNAL
:
   'external'
;

FAIL_FILTER
:
   'fail-filter'
;

FAMILY
:
   'family'
;

FILE
:
   'file'
;

FILTER
:
   'filter'
;

FIREWALL
:
   'firewall'
;

FLEXIBLE_VLAN_TAGGING
:
   'flexible-vlan-tagging'
;

FORWARDING_OPTIONS
:
   'forwarding-options'
;

FORWARDING_TABLE
:
   'forwarding-table'
;

FRAMING
:
   'framing'
;

FROM
:
   'from'
;

FTP
:
   'ftp'
;

GENERATE
:
   'generate'
;

GIGETHER_OPTIONS
:
   'gigether-options'
;

GRACEFUL_RESTART
:
   'graceful-restart'
;

GROUP
:
   'group'
;

GROUPS
:
   'groups'
;

HOLD_TIME
:
   'hold-time'
;

HOST
:
   'host'
;

HOST_NAME
:
   'host-name'
;

ICMP
:
   'icmp'
;

ICMP_TYPE
:
   'icmp-type'
;

IGMP
:
   'igmp'
;

IGMP_SNOOPING
:
   'igmp-snooping'
;

IGP
:
   'igp'
;

IMPORT
:
   'import'
;

IMPORT_RIB
:
   'import-rib'
;

INACTIVE
:
   'inactive'
;

INCLUDE_MP_NEXT_HOP
:
   'include-mp-next-hop'
;

INET
:
   'inet'
;

INET6
:
   'inet6'
;

INET_VPN
:
   'inet-vpn'
;

INET6_VPN
:
   'inet6-vpn'
;

INNER
:
   'inner'
;

INPUT
:
   'input'
;

INPUT_VLAN_MAP
:
   'input-vlan-map'
;

INSTALL
:
   'install'
;

INSTALL_NEXTHOP
:
   'install-nexthop'
;

INSTANCE
:
   'instance'
;

INTERFACE
:
   'interface'
;

INTERFACE_MODE
:
   'interface-mode'
;

INTERFACES
:
   'interfaces'
;

INTERFACE_ROUTES
:
   'interface-routes'
;

INTERNAL
:
   'internal'
;

IP
:
   'ip'
;

ISIS
:
   'isis'
;

ISO
:
   'iso'
;

L2_CIRCUIT
:
   'l2circuit'
;

L2_VPN
:
   'l2vpn'
;

LICENSE
:
   'license'
;

LINK_MODE
:
   'link-mode'
;

LDP
:
   'ldp'
;

LLDP
:
   'lldp'
;

LLDP_MED
:
   'lldp-med'
;

LOAD_BALANCE
:
   'load-balance'
;

LOCAL_ADDRESS
:
   'local-address'
;

LOCAL_AS
:
   'local-as'
;

LOCAL_PREFERENCE
:
   'local-preference'
;

LOCATION
:
   'location'
;

LOG
:
   'log'
;

LOG_UPDOWN
:
   'log-updown'
;

LOGIN
:
   'login'
;

LONGER
:
   'longer'
;

LSP
:
   'lsp'
;

MAC
:
   'mac' -> pushMode(M_MacAddress)
;

MARTIANS
:
   'martians'
;

MAX_CONFIGURATIONS_ON_FLASH
:
   'max-configurations-on-flash'
;

MAX_CONFIGURATION_ROLLBACKS
:
   'max-configuration-rollbacks'
;

METRIC
:
   'metric'
;

METRIC_OUT
:
   'metric-out'
;

MEMBERS
:
   'members'
;

MLD
:
   'mld'
;

MPLS
:
   'mpls'
;

MSDP
:
   'msdp'
;

MTU
:
   'mtu'
;

MULTICAST
:
   'multicast'
;

MULTIHOP
:
   'multihop'
;

MULTIPATH
:
   'multipath'
;

NAME_SERVER
:
   'name-server'
;

NATIVE_VLAN_ID
:
   'native-vlan-id'
;

NEIGHBOR
:
   'neighbor'
;

NETWORK_SUMMARY_EXPORT
:
   'network-summary-export'
;

NEXT
:
   'next'
;

NEXT_HOP
:
   'next-hop'
;

NEXT_TABLE
:
   'next-table'
;

NO_EXPORT
:
   'no-export'
;

NO_INSTALL
:
   'no-install'
;

NO_READVERTISE
:
   'no-readvertise'
;

NO_REDIRECTS
:
   'no-redirects'
;

NO_RESOLVE
:
   'no-resolve'
;

NO_RETAIN
:
   'no-retain'
;

NO_NEIGHBOR_LEARN
:
   'no-neighbor-learn'
;

NSSA
:
   'nssa'
;

NTP
:
   'ntp'
;

ORIGIN
:
   'origin'
;

ORLONGER
:
   'orlonger'
;

OSPF
:
   'ospf'
;

OSPF3
:
   'ospf3'
;

OUTPUT
:
   'output'
;

OUTPUT_VLAN_MAP
:
   'output-vlan-map'
;

OUTER
:
   'outer'
;

PASSIVE
:
   'passive'
;

PEER_AS
:
   'peer-as'
;

PER_PACKET
:
   'per-packet'
;

PIM
:
   'pim'
;

POE
:
   'poe'
;

POLICER
:
   'policer'
;

POLICY
:
   'policy'
;

POLICY_OPTIONS
:
   'policy-options'
;

POLICY_STATEMENT
:
   'policy-statement'
;

PORTS
:
   'ports'
;

PORT_MODE
:
   'port-mode'
;

PREFERENCE
:
   'preference'
;

PREFIX_LENGTH_RANGE
:
   'prefix-length-range'
;

PREFIX_LIST
:
   'prefix-list'
;

PREFIX_LIST_FILTER
:
   'prefix-list-filter'
;

PRIMARY
:
   'primary'
;

PROTOCOL
:
   'protocol'
;

PROTOCOLS
:
   'protocols'
;

RADIUS_OPTIONS
:
   'radius-options'
;

RADIUS_SERVER
:
   'radius-server'
;

READVERTISE
:
   'readvertise'
;

REFERENCE_BANDWIDTH
:
   'reference-bandwidth'
;

REJECT
:
   'reject'
;

REMOVE_PRIVATE
:
   'remove-private'
;

REMOVED
:
   'Removed'
;

RESOLVE
:
   'resolve'
;

RETAIN
:
   'retain'
;

RIB
:
   'rib'
;

RIB_GROUP
:
   'rib-group'
;

RIB_GROUPS
:
   'rib-groups'
;

ROOT_AUTHENTICATION
:
   'root-authentication'
;

ROUTE
:
   'route'
;

ROUTE_FILTER
:
   'route-filter'
;

ROUTER_ADVERTISEMENT
:
   'router-advertisement'
;

ROUTER_ID
:
   'router-id'
;

ROUTING_INSTANCES
:
   'routing-instances'
;

ROUTING_OPTIONS
:
   'routing-options'
;

RPF_CHECK
:
   'rpf-check'
;

RSTP
:
   'rstp'
;

RSVP
:
   'rsvp'
;

SAMPLE
:
   'sample'
;

SAMPLING
:
   'sampling'
;

SECURITY
:
   'security'
;

SERVICES
:
   'services'
;

SELF
:
   'self'
;

SET
:
   'set'
;

SNMP
:
   'snmp'
;

SOURCE_ADDRESS
:
   'source-address'
;

SOURCE_ADDRESS_FILTER
:
   'source-address-filter'
;

SOURCE_PORT
:
   'source-port'
;

SSH
:
   'ssh'
;

STANZA_REMOVED
:
   'Stanza Removed'
;

STATIC
:
   'static'
;

SUBTRACT
:
   'subtract'
;

SYSLOG
:
   'syslog'
;

SYSTEM
:
   'system'
;

TACACS
:
   'tacacs'
;

TACPLUS_SERVER
:
   'tacplus-server'
;

TAG
:
   'tag'
;

TARGET
:
   'target'
;

TARGETED_BROADCAST
:
   'targeted-broadcast'
;

TCP
:
   'tcp'
;

TCP_MSS
:
   'tcp-mss'
;

TELNET
:
   'telnet'
;

TERM
:
   'term'
;

TFTP
:
   'tftp'
;

THEN
:
   'then'
;

THROUGH
:
   'through'
;

TIME_ZONE
:
   'time-zone'
;

TO
:
   'to'
;

TRACEOPTIONS
:
   'traceoptions'
;

TRAPS
:
   'traps'
;

TRUNK
:
   'trunk'
;

TUNNEL
:
   'tunnel'
;

TYPE
:
   'type'
;

UDP
:
   'udp'
;

UNIT
:
   'unit'
;

UPTO
:
   'upto'
;

URPF_LOGGING
:
   'urpf-logging'
;

USER
:
   'user'
;

VERSION
:
   'version' -> pushMode(M_Version)
;

VIRTUAL_CHASSIS
:
   'virtual-chassis'
;

VLAN
:
   'vlan'
;

VLANS
:
   'vlans'
;

VLAN_ID
:
   'vlan-id'
;

VLAN_ID_LIST
:
   'vlan-id-list'
;

VLAN_TAGS
:
   'vlan-tags'
;

VLAN_TAGGING
:
   'vlan-tagging'
;

VPLS
:
   'vpls'
;

VSTP
:
   'vstp'
;

// End of Juniper keywords

VARIABLE
:
   (
      (
         (
            F_Variable_RequiredVarChar
            {!enableIPV6_ADDRESS}?

            F_Variable_VarChar*
         )
         |
         (
            F_Variable_RequiredVarChar_Ipv6
            {enableIPV6_ADDRESS}?

            F_Variable_VarChar_Ipv6*
         )
      )
      |
      (
         (
            F_Variable_VarChar
            {!enableIPV6_ADDRESS}?

            F_Variable_VarChar* F_Variable_RequiredVarChar F_Variable_VarChar*
         )
         |
         (
            F_Variable_VarChar_Ipv6
            {enableIPV6_ADDRESS}?

            F_Variable_VarChar_Ipv6* F_Variable_RequiredVarChar_Ipv6
            F_Variable_VarChar_Ipv6*
         )
      )
   )
;

ARI_CHANGETHIS1
:
   'ss '
   (
      ~';'
   )* ';'
;

ARI_CHANGETHIS2
:
   '802.3ad '
   (
      ~';'
   )* ';'
;

ARI_CHANGETHIS3
:
   '.*3265.*'
;

ARI_CHANGETHIS4
:
   'ae4.202'
;

AMPERSAND
:
   '&'
;

//TODO: fix this awful rule

AS_NUM
:
   TARGET COLON DEC COLON DEC
   | DEC COLON ASTERISK
   | DEC COLON DEC
   | DEC COLON DEC COLON DEC
   | DEC COLON DEC 'L' COLON DEC
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
   | F_PositiveDigit F_Digit*
;

DOLLAR
:
   '$'
;

DOUBLE_QUOTED_STRING
:
   '"' ~'"'* '"'
;

FLOAT
:
   F_PositiveDigit* F_Digit '.'
   (
      '0'
      | F_Digit* F_PositiveDigit
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
   '0x' F_HexDigit+
;

INFO_REMOVED
:
   (
      'Authentication ' DATA_REMOVED
   ) // TODO: why doesnt this work with variable
   {
                    skip();
                   }

;

IP_ADDRESS
:
   F_DecByte '.'
   {enableIP_ADDRESS}?

   F_DecByte '.' F_DecByte '.' F_DecByte
;

IP_ADDRESS_WITH_MASK
:
   F_DecByte '.'
   {enableIP_ADDRESS}?

   F_DecByte '.' F_DecByte '.' F_DecByte '/' F_Digit F_Digit?
;

IPV6_ADDRESS
:
   (
      (
         ':'
         {enableIPV6_ADDRESS}?

         ':'
         (
            (
               F_HexDigit+ ':'
            )* F_HexDigit+
         )?
      )
      |
      (
         F_HexDigit+
         {enableIPV6_ADDRESS}?

         ':' ':'?
      )+
   )
   (
      F_HexDigit+
   )?
;

IPV6_ADDRESS_WITH_MASK
:
   (
      (
         ':'
         {enableIPV6_ADDRESS}?

         ':'
         (
            (
               F_HexDigit+ ':'
            )* F_HexDigit+
         )?
      )
      |
      (
         F_HexDigit+
         {enableIPV6_ADDRESS}?

         ':' ':'?
      )+
      (
         F_HexDigit+
      )?
   ) '/' F_DecByte
;

ISO_ADDRESS
:
   F_Digit F_Digit '.' F_Digit F_Digit F_Digit F_Digit '.' F_Digit F_Digit
   F_Digit F_Digit '.' F_Digit F_Digit F_Digit F_Digit '.' F_Digit F_Digit
   F_Digit F_Digit '.' F_Digit F_Digit
;

LESS_THAN
:
   '<'
;

LINE_COMMENT
:
   '#' F_NonNewlineChar* F_NewlineChar+ -> channel(HIDDEN)
;

MULTILINE_COMMENT
:
   '/*' .*? '*/' -> channel(HIDDEN)
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

WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

fragment
F_NewlineChar
:
   [\r\n]
;

fragment
F_NonNewlineChar
:
   ~[\r\n]
;

fragment
F_DecByte
: // TODO: This is a hack.

   (
      F_PositiveDigit F_Digit F_Digit
   )
   |
   (
      F_PositiveDigit F_Digit
   )
   | F_Digit
;

fragment
F_Digit
:
   '0' .. '9'
;

fragment
F_HexDigit
:
   (
      '0' .. '9'
      | 'a' .. 'f'
      | 'A' .. 'F'
   )
;

fragment
F_Letter
:
   (
      'A' .. 'Z'
   )
   |
   (
      'a' .. 'z'
   )
;

fragment
F_PositiveDigit
:
   '1' .. '9'
;

fragment
F_Variable_RequiredVarChar
:
   ~( '0' .. '9' | [ \t\n\r/.,-;{}<>] )
;

fragment
F_Variable_RequiredVarChar_Ipv6
:
   ~( '0' .. '9' | [ \t\n\r/.,-:;{}<>] )
;

fragment
F_Variable_VarChar
:
   ~[ \t\n\r;{}<>]
;

fragment
F_Variable_VarChar_Ipv6
:
   ~[ \t\n\r:;{}<>]
;

fragment
F_WhitespaceChar
:
   [ \t\u000C\r\n]
;

mode M_MacAddress;

MAC_ADDRESS
:
   F_HexDigit F_HexDigit ':' F_HexDigit F_HexDigit ':' F_HexDigit F_HexDigit
   ':' F_HexDigit F_HexDigit ':' F_HexDigit F_HexDigit ':' F_HexDigit
   F_HexDigit
;

mode M_Version;

M_Version_SEMICOLON
:
   ';' -> type(SEMICOLON), popMode
;

M_Version_VERSION_STRING
:
   ~[ \t\u000C\r\n;]+
;

M_Version_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;
