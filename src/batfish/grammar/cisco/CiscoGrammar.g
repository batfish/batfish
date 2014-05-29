grammar CiscoGrammar;

options {
  superClass = ConfigurationParser;
}
import CiscoGrammar_bgp, CiscoGrammar_interface, CiscoGrammar_ospf;


tokens {
  AAA                          = 'aaa';
  AAA_SERVER                   = 'aaa-server';
  ABSOLUTE_TIMEOUT             = 'absolute-timeout';
  ACCEPT_DIALIN                = 'accept-dialin';
  ACCESS                       = 'access';
  ACCESS_CLASS                 = 'access-class';
  ACCESS_GROUP                 = 'access-group';
  ACCESS_LIST                  = 'access-list';
  ACCESS_LOG                   = 'access-log';
  ACCOUNTING                   = 'accounting';
  ACCOUNTING_PORT              = 'accounting-port';
  ACCOUNTING_SERVER_GROUP      = 'accounting-server-group';
  ACTION                       = 'action';
  ACTIVATE                     = 'activate';
  ACTIVATION_CHARACTER         = 'activation-character';
  ACTIVE                       = 'active';
  ADD                          = 'add';
  ADDITIVE                     = 'additive';
  ADDRESS                      = 'address';
  ADDRESS_FAMILY               = 'address-family';
  ADDRESS_POOL                 = 'address-pool';
  ADMISSION                    = 'admission';
  AES128_SHA1                  = 'aes128-sha1';
  AES256_SHA1                  = 'aes256-sha1';
  AGGREGATE_ADDRESS            = 'aggregate-address';
  ALERT_GROUP                  = 'alert-group';
  ALIAS                        = 'alias';
  ALLOWED                      = 'allowed';
  ALWAYS                       = 'always';
  ANY                          = 'any';
  ANYCONNECT                   = 'anyconnect';
  ANYCONNECT_ESSENTIALS        = 'anyconnect-essentials';
  AP                           = 'ap';
  ARCHIVE                      = 'archive';
  AREA                         = 'area';
  AS_PATH                      = 'as-path';
  ASA                          = 'ASA';
  ASDM                         = 'asdm';
  ASSOCIATE                    = 'associate';
  ASSOCIATION                  = 'association';
  ASYNC                        = 'async';
  ASYNC_BOOTP                  = 'async-bootp';
  AUDIT                        = 'audit';
  AUTH_PROXY                   = 'auth-proxy';
  AUTHENTICATION               = 'authentication';
  AUTHENTICATION_PORT          = 'authentication-port';
  AUTHENTICATION_SERVER_GROUP  = 'authentication-server-group';
  AUTHORIZATION                = 'authorization';
  AUTHORIZATION_REQUIRED       = 'authorization-required';
  AUTHORIZATION_SERVER_GROUP   = 'authorization-server-group';
  AUTO                         = 'auto';
  AUTOSELECT                   = 'autoselect';
  AUTO_SUMMARY                 = 'auto-summary';
  AUTO_SYNC                    = 'auto-sync';
  BANDWIDTH                    = 'bandwidth';
  BANNER                       = 'banner';
  BFD                          = 'bfd';
  BGP                          = 'bgp';
  BGP_COMMUNITY                = 'bgp-community';
  BIND                         = 'bind';
  BOOT                         = 'boot';
  BOOT_END_MARKER              = 'boot-end-marker';
  BOOT_START_MARKER            = 'boot-start-marker';
  BOOTP                        = 'bootp';
  BOOTPC                       = 'bootpc';
  BOOTPS                       = 'bootps';
  BRIDGE                       = 'bridge';
  CA                           = 'ca';
  CACHE                        = 'cache';
  CALL                         = 'call';
  CALL_HOME                    = 'call-home';
  CALLER_ID                    = 'caller-id';
  CARD                         = 'card';
  CAS_CUSTOM                   = 'cas-custom';
  CCM                          = 'ccm';
  CCM_GROUP                    = 'ccm-group';
  CCM_MANAGER                  = 'ccm-manager';
  CDP                          = 'cdp';
  CEF                          = 'cef';
  CGMP                         = 'cgmp';
  CHANNEL_GROUP                = 'channel-group';
  CHANNEL_PROTOCOL             = 'channel-protocol';
  CIPC                         = 'cipc';
  CLASS                        = 'class';
  CLASSLESS                    = 'classless';
  CLASS_MAP                    = 'class-map';
  CLNS                         = 'clns';
  CLOCK                        = 'clock';
  CLUSTER                      = 'cluster';
  CLUSTER_ID                   = 'cluster-id';
  CMD                          = 'cmd';
  CNS                          = 'cns';
  CODEC                        = 'codec';
  COLLECT                      = 'collect';
  COMM_LIST                    = 'comm-list';
  CONFIG_REGISTER              = 'config-register';
  CONFORM_ACTION               = 'conform-action';
  CONNECTED                    = 'connected';
  CONSOLE                      = 'console';
  CONTACT_EMAIL_ADDR           = 'contact-email-addr';
  CONTROL_PLANE                = 'control-plane';
  CONTROLLER                   = 'controller';
  COST                         = 'cost';
  CPTONE                       = 'cptone';
  CRYPTO                       = 'crypto';
  CRL                          = 'crl';
  CTL_FILE                     = 'ctl-file';
  CTS                          = 'cts';
  DAMPENING                    = 'dampening';
  DBL                          = 'dbl';
  DEAD_INTERVAL                = 'dead-interval';
  DEFAULT                      = 'default';
  DEFAULT_DOMAIN               = 'default-domain';
  DEFAULT_GATEWAY              = 'default-gateway';
  DEFAULT_GROUP_POLICY         = 'default-group-policy';
  DEFAULT_INFORMATION          = 'default-information';
  DEFAULT_METRIC               = 'default-metric';
  DEFAULT_NETWORK              = 'default-network';
  DEFAULT_ORIGINATE            = 'default-originate';
  DEFINITION                   = 'definition';
  DELETE                       = 'delete';
  DENY                         = 'deny';
  DES_SHA1                     = 'des-sha1';
  DESIRABLE                    = 'desirable';
  DESTINATION                  = 'destination';
  DEVICE                       = 'device';
  DEVICE_SENSOR                = 'device-sensor';
  DHCP                         = 'dhcp';
  DHCPD                        = 'dhcpd';
  DIAGNOSTIC                   = 'diagnostic';
  DIAL_PEER                    = 'dial-peer';
  DIALER_LIST                  = 'dialer-list';
  DIRECTED_BROADCAST           = 'directed-broadcast';
  DISABLE                      = 'disable';
  DISTRIBUTE_LIST              = 'distribute-list';
  DNS                          = 'dns';
  DNS_GUARD                    = 'dns-guard';
  DNS_SERVER                   = 'dns-server';
  DOMAIN                       = 'domain';
  DOMAIN_LIST                  = 'domain-list';
  DOMAIN_LOOKUP                = 'domain-lookup';
  DOMAIN_NAME                  = 'domain-name';
  DOT11                        = 'dot11';
  DOT1Q                        = 'dot1q';
  DS0_GROUP                    = 'ds0-group';
  DSP                          = 'dsp';
  DSPFARM                      = 'dspfarm';
  DUPLEX                       = 'duplex';
  DYNAMIC                      = 'dynamic';
  DYNAMIC_ACCESS_POLICY_RECORD = 'dynamic-access-policy-record';
  DYNAMIC_MAP                  = 'dynamic-map';
  EBGP_MULTIHOP                = 'ebgp-multihop';
  ECHO                         = 'echo';
  ECHO_REPLY                   = 'echo-reply';
  ENABLE                       = 'enable';
  ENCAPSULATION                = 'encapsulation';
  ENCR                         = 'encr';
  ENCRYPTION                   = 'encryption';
  END                          = 'end';
  ENROLLMENT                   = 'enrollment';
  ENVIRONMENT                  = 'environment';
  EQ                           = 'eq';
  ERRDISABLE                   = 'errdisable';
  ESP                          = 'esp';
  ESTABLISHED                  = 'established';
  EVALUATE                     = 'evaluate';
  EVENT                        = 'event';
  EXCEED_ACTION                = 'exceed-action';
  EXCEPTION                    = 'exception';
  EXEC                         = 'exec';
  EXEC_TIMEOUT                 = 'exec-timeout';
  EXECUTE                      = 'execute';
  EXIT_ADDRESS_FAMILY          = 'exit-address-family';
  EXPORT                       = 'export';
  EXPORT_PROTOCOL              = 'export-protocol';
  EXPORTER                     = 'exporter';
  EXPANDED                     = 'expanded';
  EXTENDED                     = 'extended';
  FABRIC                       = 'fabric';
  FAILOVER                     = 'failover';
  FAIR_QUEUE                   = 'fair-queue';
  FALL_OVER                    = 'fall-over';
  FALLBACK_DN                  = 'fallback-dn';
  FEATURE                      = 'feature';
  FILE                         = 'file';
  FILE_BROWSING                = 'file-browsing';
  FILE_ENTRY                   = 'file-entry';
  FINGER                       = 'finger';
  FIRMWARE                     = 'firmware';
  FLOW                         = 'flow';
  FLOW_CACHE                   = 'flow-cache';
  FLOW_EXPORT                  = 'flow-export';
  FORWARD_PROTOCOL             = 'forward-protocol';
  FQDN                         = 'fqdn';
  FRAGMENTS                    = 'fragments';
  FRAMING                      = 'framing';
  FTP                          = 'ftp';
  FTP_DATA                     = 'ftp-data';
  FTP_SERVER                   = 'ftp-server';
  FULL_DUPLEX                  = 'full-duplex';
  GATEKEEPER                   = 'gatekeeper';
  GATEWAY                      = 'gateway';
  GE                           = 'ge';
  GRACEFUL_RESTART             = 'graceful-restart';
  GRATUITOUS_ARPS              = 'gratuitous-arps';
  GRE                          = 'gre';
  GROUP                        = 'group';
  GROUP_ALIAS                  = 'group-alias';
  GROUP_OBJECT                 = 'group-object';
  GROUP_POLICY                 = 'group-policy';
  GROUP_RANGE                  = 'group-range';
  GROUP_URL                    = 'group-url';
  GT                           = 'gt';
  HALF_DUPLEX                  = 'half-duplex';
  HASH                         = 'hash';
  HELLO_MULTIPLIER             = 'hello-multiplier';
  HELPER_ADDRESS               = 'helper-address';
  HIDDEN_SHARES                = 'hidden-shares';
  HIDEKEYS                     = 'hidekeys';
  HISTORY                      = 'history';
  HOLD_QUEUE                   = 'hold-queue';
  HOST                         = 'host';
  HOST_ROUTING                 = 'host-routing';
  HOSTNAME                     = 'hostname';
  HTTP                         = 'http';
  HW_MODULE                    = 'hw-module';
  ICMP                         = 'icmp';
  ICMP_ECHO                    = 'icmp-echo';
  ICMP_OBJECT                  = 'icmp-object';
  IDENTITY                     = 'identity';
  IGMP                         = 'igmp';
  IKEV1                        = 'ikev1';
  IN                           = 'in';
  INACTIVITY_TIMER             = 'inactivity-timer';
  INBOUND                      = 'inbound';
  INSPECT                      = 'inspect';
  INTERNAL                     = 'internal';
  INTERNET                     = 'internet';
  IP                           = 'ip';
  IP_ADDRESS_LITERAL           = 'ip-address';
  IPC                          = 'ipc';
  IPSEC                        = 'ipsec';
  IPSEC_UDP                    = 'ipsec-udp';
  IPV4                         = 'ipv4';
  IPV6                         = 'ipv6';
  IPV6_ADDRESS_POOL            = 'ipv6-address-pool';
  IRDP                         = 'irdp';
  ISAKMP                       = 'isakmp';
  ISDN                         = 'isdn';
  ISL                          = 'isl';
  KEEPALIVE                    = 'keepalive';
  KEEPALIVE_ENABLE             = 'keepalive-enable';
  KEEPOUT                      = 'keepout';
  KEYPAIR                      = 'keypair';
  LAPB                         = 'lapb';
  LE                           = 'le';
  L2TP                         = 'l2tp';
  LDAP_BASE_DN                 = 'ldap-base-dn';
  LDAP_LOGIN                   = 'ldap-login';
  LDAP_LOGIN_DN                = 'ldap-login-dn';
  LDAP_NAMING_ATTRIBUTE        = 'ldap-naming-attribute';
  LDAP_SCOPE                   = 'ldap-scope';
  LICENSE                      = 'license';
  LIFETIME                     = 'lifetime';
  LINE                         = 'line';
  LINECODE                     = 'linecode';
  LLDP                         = 'lldp';
  LOAD_INTERVAL                = 'load-interval';
  LOCAL                        = 'local';
  LOCAL_AS                     = 'local-as';
  LOCAL_IP                     = 'local-ip';
  LOCAL_PORT                   = 'local-port';
  LOCAL_PREFERENCE             = 'local-preference';
  LOG                          = 'log';
  LOG_ADJACENCY_CHANGES        = 'log-adjacency-changes';
  LOG_INPUT                    = 'log-input';
  LOG_NEIGHBOR_CHANGES         = 'log-neighbor-changes';
  LOGGING                      = 'logging';
  LOGIN                        = 'login';
  LPD                          = 'lpd';
  LRE                          = 'lre';
  LT                           = 'lt';
  MAC_ADDRESS_TABLE            = 'mac-address-table';
  MACRO                        = 'macro';
  MAIL_SERVER                  = 'mail-server';
  MAIN_CPU                     = 'main-cpu';
  MANAGEMENT_ONLY              = 'management-only';
  MAP                          = 'map';
  MASK                         = 'mask';
  MATCH                        = 'match';
  MAXIMUM                      = 'maximum';
  MAXIMUM_PATHS                = 'maximum-paths';
  MAXIMUM_PREFIX               = 'maximum-prefix';
  MDIX                         = 'mdix';
  MEDIA_TERMINATION            = 'media-termination';
  MEDIA_TYPE                   = 'media-type';
  MEMBER                       = 'member';
  MEMORY_SIZE                  = 'memory-size';
  MESSAGE_LENGTH               = 'message-length';
  METRIC                       = 'metric';
  METRIC_TYPE                  = 'metric-type';
  MFIB                         = 'mfib';
  MGCP                         = 'mgcp';
  MICROCODE                    = 'microcode';
  MINIMAL                      = 'minimal';
  MLD                          = 'mld';
  MLS                          = 'mls';
  MODE                         = 'mode';
  MODEM                        = 'modem';
  MONITOR                      = 'monitor';
  MOP                          = 'mop';
  MOTD                         = 'motd';
  MPLS                         = 'mpls';
  MROUTE                       = 'mroute';
  MROUTE_CACHE                 = 'mroute-cache';
  MSDP                         = 'msdp';
  MTA                          = 'mta';
  MTU                          = 'mtu';
  MULTICAST                    = 'multicast';
  MULTICAST_ROUTING            = 'multicast-routing';
  MULTILINK                    = 'multilink';
  NAME_LOOKUP                  = 'name-lookup';
  NAME_SERVER                  = 'name-server';
  NAMEIF                       = 'nameif';
  NAMES                        = 'names';
  NAT                          = 'nat';
  NAT_CONTROL                  = 'nat-control';
  NATIVE                       = 'native';
  ND                           = 'nd';
  NEGOTIATE                    = 'negotiate';
  NEGOTIATION                  = 'negotiation';
  NEIGHBOR                     = 'neighbor';
  NEQ                          = 'neq';
  NETBIOS_DGM                  = 'netbios-dgm';
  NETBIOS_NS                   = 'netbios-ns';
  NETBIOS_SS                   = 'netbios-ss';
  NETWORK                      = 'network';
  NETWORK_CLOCK_PARTICIPATE    = 'network-clock-participate';
  NETWORK_CLOCK_SELECT         = 'network-clock-select';
  NETWORK_OBJECT               = 'network-object';
  NEXT_HOP                     = 'next-hop';
  NEXT_HOP_SELF                = 'next-hop-self';
  NO                           = 'no';
  NO_ADVERTISE                 = 'no-advertise';
  NO_EXPORT                    = 'no-export';
  NO_SUMMARY                   = 'no-summary';
  NON500_ISAKMP                = 'non500-isakmp';
  NONE                         = 'none';
  NONEGOTIATE                  = 'nonegotiate';
  NOTIFY                       = 'notify';
  NSF                          = 'nsf';
  NSSA                         = 'nssa';
  NTP                          = 'ntp';
  OBJECT                       = 'object';
  OBJECT_GROUP                 = 'object-group';
  ORIGIN                       = 'origin';
  ORIGINATE                    = 'originate';
  OSPF                         = 'ospf';
  OUT                          = 'out';
  PACKET_TOO_BIG               = 'packet-too-big';
  PAGER                        = 'pager';
  PARAMETERS                   = 'parameters';
  PARENT                       = 'parent';
  PARTICIPATE                  = 'participate';
  PASSIVE_INTERFACE            = 'passive-interface';
  PASSWORD_STORAGE             = 'password-storage';
  PEER                         = 'peer';
  PEER_GROUP                   = 'peer-group';
  PERMANENT                    = 'permanent';
  PERMIT                       = 'permit';
  PHONE_PROXY                  = 'phone-proxy';
  PHYSICAL_LAYER               = 'physical-layer';
  PICKUP                       = 'pickup';
  PIM                          = 'pim';
  PIM_AUTO_RP                  = 'pim-auto-rp';
  PKI                          = 'pki';
  PLATFORM                     = 'platform';
  POLICE                       = 'police';
  POLICY                       = 'policy';
  POLICY_MAP                   = 'policy-map';
  POP3                         = 'pop3';
  PORT                         = 'port';
  PORT_OBJECT                  = 'port-object';
  PORT_SECURITY                = 'port-security';
  PORT_UNREACHABLE             = 'port-unreachable';
  POWER                        = 'power';
  PPP                          = 'ppp';
  PREPEND                      = 'prepend';
  PRI_GROUP                    = 'pri-group';
  PRIORITY                     = 'priority';
  PRIORITY_QUEUE               = 'priority-queue';
  PRIVATE_VLAN                 = 'private-vlan';
  PREFIX                       = 'prefix';
  PREFIX_LIST                  = 'prefix-list';
  PRIORITY                     = 'priority';
  PRIORITY_QUEUE               = 'priority-queue';
  PRIVILEGE                    = 'privilege';
  PROCESS                      = 'process';
  PROFILE                      = 'profile';
  PROMPT                       = 'prompt';
  PROTOCOL                     = 'protocol';
  PROTOCOL_OBJECT              = 'protocol-object';
  PROXY_ARP                    = 'proxy-arp';
  QOS                          = 'qos';
  QUEUE_BUFFERS                = 'queue-buffers';
  QUEUE_LIMIT                  = 'queue-limit';
  QUEUE_SET                    = 'queue-set';
  RADIUS                       = 'radius';
  RADIUS_COMMON_PW             = 'radius-common-pw';
  RADIUS_SERVER                = 'radius-server';
  RANGE                        = 'range';
  RC4_SHA1                     = 'rc4-sha1';
  RCMD                         = 'rcmd';
  RCV_QUEUE                    = 'rcv-queue';
  RD                           = 'rd';
  RECORD                       = 'record';
  RECORD_ENTRY                 = 'record-entry';
  REDIRECT                     = 'redirect';
  REDIRECT_FQDN                = 'redirect-fqdn';
  REDIRECTS                    = 'redirects';
  REDISTRIBUTE                 = 'redistribute';
  REDUNDANCY                   = 'redundancy';
  REFLECT                      = 'reflect';
  REMOTE_AS                    = 'remote-as';
  REMOTE_IP                    = 'remote-ip';
  REMOTE_PORT                  = 'remote-port';
  REMOVE_PRIVATE_AS            = 'remove-private-as';
  REMOTE_SPAN                  = 'remote-span';
  REMOVED                      = '<removed>';
  RESOURCE                     = 'resource';
  RESOURCE_POOL                = 'resource-pool';
  REVOCATION_CHECK             = 'revocation-check';
  RING                         = 'ring';
  RIP                          = 'rip';
  ROUTE                        = 'route';
  ROUTE_CACHE                  = 'route-cache';
  ROUTE_MAP                    = 'route-map';
  ROUTE_REFLECTOR_CLIENT       = 'route-reflector-client';
  ROUTER                       = 'router';
  ROUTER_ID                    = 'router-id';
  ROUTING                      = 'routing';
  RSAKEYPAIR                   = 'rsakeypair';
  RTR                          = 'rtr';
  SAME_SECURITY_TRAFFIC        = 'same-security-traffic';
  SAP                          = 'sap';
  SCCP                         = 'sccp';
  SCHEDULER                    = 'scheduler';
  SCHEME                       = 'scheme';
  SCP                          = 'scp';
  SCRIPTING                    = 'scripting';
  SCTP                         = 'sctp';
  SECONDARY                    = 'secondary';
  SECURITY                     = 'security';
  SECURITY_LEVEL               = 'security-level';
  SEND_COMMUNITY               = 'send-community';
  SENDER                       = 'sender';
  SEQ                          = 'seq';
  SERIAL                       = 'serial';
  SERIAL_NUMBER                = 'serial-number';
  SERVER                       = 'server';
  SERVER_TYPE                  = 'server-type';
  SERVICE                      = 'service';
  SERVICE_MODULE               = 'service-module';
  SERVICE_POLICY               = 'service-policy';
  SERVICE_TYPE                 = 'service-type';
  SESSION_LIMIT                = 'session-limit';
  SESSION_TIMEOUT              = 'session-timeout';
  SET                          = 'set';
  SHELL                        = 'shell';
  SHUTDOWN                     = 'shutdown';
  SLA                          = 'sla';
  SMTP                         = 'smtp';
  SMTP_SERVER                  = 'smtp-server';
  SNMP                         = 'snmp';
  SNMP_SERVER                  = 'snmp-server';
  SNMPTRAP                     = 'snmptrap';
  SPE                          = 'spe';
  SOFT_RECONFIGURATION         = 'soft-reconfiguration';
  SOURCE                       = 'source';
  SOURCE_INTERFACE             = 'source-interface';
  SOURCE_IP_ADDRESS            = 'source-ip-address';
  SOURCE_ROUTE                 = 'source-route';
  SPANNING_TREE                = 'spanning-tree';
  SPEED                        = 'speed';
  SPLIT_TUNNEL_NETWORK_LIST    = 'split-tunnel-network-list';
  SPLIT_TUNNEL_POLICY          = 'split-tunnel-policy';
  SRR_QUEUE                    = 'srr-queue';
  SSH                          = 'ssh';
  SSL                          = 'ssl';
  STANDARD                     = 'standard';
  STANDBY                      = 'standby';
  STATIC                       = 'static';
  STCAPP                       = 'stcapp';
  STOPBITS                     = 'stopbits';
  STORM_CONTROL                = 'storm-control';
  STP                          = 'stp';
  SUBJECT_NAME                 = 'subject-name';
  SUBNET                       = 'subnet';
  SUBNETS                      = 'subnets';
  SUBNET_ZERO                  = 'subnet-zero';
  SUBSCRIBE_TO                 = 'subscribe-to';
  SUBSCRIBE_TO_ALERT_GROUP     = 'subscribe-to-alert-group';
  SUMMARY_ONLY                 = 'summary-only';
  SUPPLEMENTARY_SERVICES       = 'supplementary-services';
  SWITCH                       = 'switch';
  SWITCHBACK                   = 'switchback';
  SWITCHPORT                   = 'switchport';
  SYNCHRONIZATION              = 'synchronization';
  SYSLOG                       = 'syslog';
  SYSOPT                       = 'sysopt';
  SYSTEM                       = 'system';
  TABLE_MAP                    = 'table-map';
  TACACS                       = 'tacacs';
  TACACS_PLUS                  = 'tacacs+';
  TAG                          = 'tag';
  TAG_SWITCHING                = 'tag-switching';
  TB_VLAN1                     = 'tb-vlan1';
  TB_VLAN2                     = 'tb-vlan2';
  TCP                          = 'tcp';
  TELNET                       = 'telnet';
  TERMINAL_TYPE                = 'terminal-type';
  TFTP                         = 'tftp';
  TFTP_SERVER                  = 'tftp-server';
  THREAT_DETECTION             = 'threat-detection';
  THREE_DES                    = '3des';
  THREE_DES_SHA1               = '3des-sha1';
  TIME_EXCEEDED                = 'time-exceeded';
  TIMEOUT                      = 'timeout';
  TIMEOUTS                     = 'timeouts';
  TIMER                        = 'timer';
  TIMERS                       = 'timers';
  TIMING                       = 'timing';
  TLS_PROXY                    = 'tls-proxy';
  TRACK                        = 'track';
  TRANSLATE                    = 'translate';
  TRANSPORT                    = 'transport';
  TRUNK                        = 'trunk';
  TRUST                        = 'trust';
  TRUSTPOINT                   = 'trustpoint';
  TRUSTPOOL                    = 'trustpool';
  TTL_EXCEEDED                 = 'ttl-exceeded';
  TUNNEL                       = 'tunnel';
  TUNNEL_GROUP                 = 'tunnel-group';
  TUNNEL_GROUP_LIST            = 'tunnel-group-list';
  TYPE                         = 'type';
  UDLD                         = 'udld';
  UDP                          = 'udp';
  UNABLE                       = 'Unable';
  UNICAST_ROUTING              = 'unicast-routing';
  UNNUMBERED                   = 'unnumbered';
  UNREACHABLE                  = 'unreachable';
  UNREACHABLES                 = 'unreachables';
  UPDATE_SOURCE                = 'update-source';
  UPGRADE                      = 'upgrade';
  USER_IDENTITY                = 'user-identity';
  USERNAME                     = 'username';
  VALIDATION_USAGE             = 'validation-usage';
  VERIFY                       = 'verify';
  VERSION                      = 'version';
  VIOLATE_ACTION               = 'violate-action';
  VIRTUAL_REASSEMBLY           = 'virtual-reassembly';
  VIRTUAL_TEMPLATE             = 'virtual-template';
  VLAN                         = 'vlan';
  VMPS                         = 'vmps';
  VOICE                        = 'voice';
  VOICE_CARD                   = 'voice-card';
  VOICE_PORT                   = 'voice-port';
  VPDN                         = 'vpdn';
  VPDN_GROUP                   = 'vpdn-group';
  VPN                          = 'vpn';
  VPN_FILTER                   = 'vpn-filter';
  VPN_IDLE_TIMEOUT             = 'vpn-idle-timeout';
  VPN_TUNNEL_PROTOCOL          = 'vpn-tunnel-protocol';
  VRF                          = 'vrf';
  VRRP                         = 'vrrp';
  VTP                          = 'vtp';
  WEBVPN                       = 'webvpn';
  WINS_SERVER                  = 'wins-server';
  WITHOUT_CSD                  = 'without-csd';
  WRR_QUEUE                    = 'wrr-queue';
  WSMA                         = 'wsma';
  WWW                          = 'www';
  X25                          = 'x25';
  X29                          = 'x29';
  XLATE                        = 'xlate';
}

@lexer::header {
package batfish.grammar.cisco;
}

@lexer::members {
boolean inComment = false;
boolean inMultilineComment = false;
boolean enableIPV6_ADDRESS = true;
boolean enableIP_ADDRESS = true;

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
package batfish.grammar.cisco;

import java.util.Collections;

import batfish.grammar.ConfigurationParser;
import batfish.grammar.cisco.bgp.*;
import batfish.grammar.cisco.interfaces.*;
import batfish.grammar.cisco.ospf.*;
import batfish.grammar.cisco.routemap.*;

import batfish.representation.LineAction;
import batfish.representation.VendorConfiguration;
import batfish.representation.SwitchportEncapsulationType;
import batfish.representation.SwitchportMode;
import batfish.representation.cisco.*;

import batfish.util.SubRange;
import batfish.util.Util;
}

@parser::members {
private List<String> errors = new ArrayList<String>();

@Override
public VendorConfiguration parse_configuration() throws RecognitionException {
	return cisco_configuration().getConfiguration();
}

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "CiscoGrammar: " + hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	List<String> allErrors = new ArrayList<String>();
	allErrors.addAll(gCiscoGrammar_bgp.getErrors());
	allErrors.addAll(gCiscoGrammar_interface.getErrors());
	allErrors.addAll(gCiscoGrammar_ospf.getErrors());
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

access_list_action returns [LineAction ala]
  :
  (PERMIT 
         {
          ala = LineAction.ACCEPT;
         })
  | (DENY 
         {
          ala = LineAction.REJECT;
         })
  ;

access_list_remark_stanza
  :
  ACCESS_LIST integer REMARK ~NEWLINE* NEWLINE
  ;

access_list_stanza returns [Stanza s]
  :
  (
    x=standard_access_list_stanza
    | x=extended_access_list_stanza
  )
  
  {
   s = x;
  }
  ;

address_family_vrf_stanza
  :
  ADDRESS_FAMILY ~NEWLINE* NEWLINE null_block_substanza* EXIT_ADDRESS_FAMILY NEWLINE closing_comment
  ;

banner_stanza
  :
  BANNER MOTD ESCAPE_C ~ESCAPE_C* ESCAPE_C NEWLINE
  ;

certificate_stanza
  :
  CERTIFICATE ~NEWLINE* NEWLINE
  (
    (
      DEC
      | REMOVED
      | VARIABLE
    )
    ~NEWLINE* NEWLINE
  )+
  QUIT NEWLINE
  ;

cisco_configuration returns [CiscoConfiguration cc = new CiscoConfiguration()]
  :
  (x=stanza_list COLON? END) 
                            {
                             for (Stanza stanza : x) {
                             	cc.processStanza(stanza);
                             }
                            }
  ;

closing_comment
  :
  COMMENT_CLOSING_LINE
  ;

comment_stanza
  :
  COMMENT_LINE
  ;

community returns [long l]
  :
  (
    (DEC COLON DEC) => (part1=DEC COLON part2=DEC) 
                                                  {
                                                   long part1l = Long.parseLong(part1.getText());
                                                   long part2l = Long.parseLong(part2.getText());
                                                   l = (part1l << 16) + part2l;
                                                  }
  )
  | (num=DEC 
            {
             l = Long.parseLong(num.getText());
            })
  | (INTERNET 
             {
              l = 0;//TODO: change
             })
  | (LOCAL_AS 
             {
              l = 1;//TODO: change
             })
  | (NO_ADVERTISE 
                 {
                  l = 2;//TODO: change
                 })
  | (NO_EXPORT 
              {
               l = 3;//TODO: change
              })
  ;

extended_access_list_stanza returns [Stanza s]
  :
  (
    ACCESS_LIST {100 <= nextIntVal() && nextIntVal() <= 199}? num=DEC ala=access_list_action prot=protocol srcipr=extended_access_list_ip_range (alps_src=port_specifier)? dstipr=extended_access_list_ip_range (alps_dst=port_specifier)?
    (
      ECHO_REPLY
      | ECHO
      | ESTABLISHED
      | FRAGMENTS
      | LOG
      | LOG_INPUT
      | PACKET_TOO_BIG
      | PORT_UNREACHABLE
      | TTL_EXCEEDED
    )?
    NEWLINE
  )
  
  {
   s = new ExtendedAccessListStanza(ala, num.getText(), prot, srcipr.get(0),
   		srcipr.get(1), dstipr.get(0), dstipr.get(1), alps_src, alps_dst);
  }
  ;

extended_access_list_ip_range returns [List<String> alipr = new ArrayList<String>()]
  :
  (
    ( (ip=IP_ADDRESS wildcard=IP_ADDRESS) 
                                         {
                                          alipr.add(ip.getText());
                                          alipr.add(wildcard.getText());
                                         })
    | (ANY 
          {
           alipr.add("0.0.0.0");
           alipr.add("255.255.255.255");
          })
    | (HOST ip=IP_ADDRESS 
                         {
                          alipr.add(ip.getText());
                          alipr.add("0.0.0.0");
                         })
  )
  ;

hostname returns [String s = new String()]
  :
  x=VARIABLE 
            {
             s = x.getText();
            }
  ;

hostname_stanza returns [Stanza s]
  :
  (HOSTNAME x=hostname NEWLINE) 
                               {
                                s = new HostnameStanza(x);
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

interface_name returns [String iname]
  :
  name=VARIABLE 
               {
                iname = name.getText();
               }
  (FORWARD_SLASH x=DEC 
                      {
                       iname += "/" + x.getText();
                      })?
  ;

ip_access_list_extended_stanza returns [Stanza s]
@init {
IPAccessListExtendedStanza ipals = null;
}
  :
  IP ACCESS_LIST EXTENDED
  (
    ( (name=VARIABLE) 
                     {
                      ipals = new IPAccessListExtendedStanza(name.getText());
                     })
    | ( ( {100 <= nextIntVal() && nextIntVal() <= 199}? num=integer) 
                                                                    {
                                                                     ipals = new IPAccessListExtendedStanza(Integer.toString(num));
                                                                    })
  )
  NEWLINE ( ( (isl=item_ip_access_list_extended_stanza_list) 
                                                            {
                                                             for (ItemIPAccessListExtendedStanza is : isl) {
                                                             	if ((is.getAccessListLine()) != null) {
                                                             		ipals.addLine(is.getAccessListLine());
                                                             	}
                                                             }
                                                             s = ipals;
                                                            }))?
  ;

ip_access_list_standard_stanza returns [Stanza s]
@init {
IPAccessListStandardStanza ipals = null;
}
  :
  IP ACCESS_LIST STANDARD
  (
    ( (name=VARIABLE) 
                     {
                      ipals = new IPAccessListStandardStanza(name.getText());
                     })
    | ( ( {1 <= nextIntVal() && nextIntVal() <= 99}? num=integer) 
                                                                 {
                                                                  ipals = new IPAccessListStandardStanza(Integer.toString(num));
                                                                 })
  )
  NEWLINE
  (
    ( (isl=item_ip_access_list_standard_stanza_list) 
                                                    {
                                                     for (ItemIPAccessListStandardStanza is : isl) {
                                                     	if ((is.getAccessListLine()) != null) {
                                                     		ipals.addLine(is.getAccessListLine());
                                                     	}
                                                     }
                                                     s = ipals;
                                                    })
    | closing_comment
  )
  ;

ip_as_path_access_list_stanza returns [Stanza s]
@init {
String regex = "";
}
  :
  (IP AS_PATH ACCESS_LIST name=DEC action=access_list_action (remainder=~NEWLINE 
                                                                                {
                                                                                 regex += remainder.getText();
                                                                                })* NEWLINE) 
                                                                                            {
                                                                                             s = new IPAsPathAccessListStanza(name.getText(), action, regex);
                                                                                            }
  ;

ip_community_list_expanded_stanza returns [Stanza s]
@init {
String line = "";
}
  :
  {nextTokenType() == EXPANDED
		|| (nextTokenType() == DEC && 100 <= nextIntVal() && nextIntVal() <= 500)}?
  (
    (
      (EXPANDED name=VARIABLE)
      | ( {100 <= nextIntVal() && nextIntVal() <= 500}? name=DEC)
    )
    ala=access_list_action (remainder=~NEWLINE 
                                              {
                                               line += remainder.getText();
                                              })+ NEWLINE
  )
  
  {
   s = new CommunityListExpandedStanza(name.getText(), ala, line);
  }
  ;

ip_community_list_standard_stanza returns [Stanza s]
@init {
List<Long> communities = new ArrayList<Long>();
}
  :
  {nextTokenType() == STANDARD
		|| (nextTokenType() == DEC && 1 <= nextIntVal() && nextIntVal() <= 99)}?
  (
    (
      (STANDARD name=VARIABLE)
      | ( {1 <= nextIntVal() && nextIntVal() <= 99}? name=DEC)
    )
    ala=access_list_action (c=community 
                                       {
                                        communities.add(c);
                                       })+ NEWLINE
  )
  
  {
   s = new CommunityListStandardStanza(name.getText(), ala, communities);
  }
  ;

ip_community_list_stanza returns [Stanza s]
  :
  IP COMMUNITY_LIST
  (
    x=ip_community_list_expanded_stanza
    | x=ip_community_list_standard_stanza
  )
  
  {
   s = x;
  }
  ;

ip_default_gateway_stanza returns [Stanza s] //TODO: implement
  :
  (IP DEFAULT_GATEWAY i=IP_ADDRESS) 
                                   {
                                    s = new NullStanza();
                                   }
  ;

ip_prefix_list_line_stanza returns [Stanza s]
@init {
int min_prefix_length = 0;
int max_prefix_length = 32;
}
  :
  (
    IP PREFIX_LIST name=VARIABLE (SEQ DEC)? action=access_list_action prefix=IP_ADDRESS FORWARD_SLASH (prefix_length=integer 
                                                                                                                            {
                                                                                                                             min_prefix_length = prefix_length;
                                                                                                                            })
    (
      ( (GE minpl=integer) 
                          {
                           min_prefix_length = minpl;
                          })
      | ( (LE maxpl=integer) 
                            {
                             max_prefix_length = maxpl;
                            })
    )?
  )
  
  {
   s = new PrefixListLineStanza(name.getText(), action, prefix.getText(),
   		prefix_length, min_prefix_length, max_prefix_length);
  }
  ;

ip_route_stanza returns [Stanza s]
@init {
int distance = 1;
}
  :
  (
    IP ROUTE prefix=IP_ADDRESS mask=IP_ADDRESS
    (
      (
        (IP_ADDRESS) => (nexthopip=IP_ADDRESS)
      )
      | nexthopint=interface_name
    )
    (d=integer 
              {
               distance = d;
              })? (TAG DEC)? PERMANENT? (TRACK DEC)? NEWLINE
  )
  
  {
   String nextHopIp = (nexthopip != null ? nexthopip.getText() : null);
   String nextHopInt = (nexthopint != null ? nexthopint : null);
   s = new IPRouteStanza(prefix.getText(), mask.getText(), nextHopIp, nextHopInt,
   		distance);
  }
  ;

item_ip_access_list_extended_stanza returns [ItemIPAccessListExtendedStanza iips]
  :
  (
    (
      ala=access_list_action (prot=protocol) srcipr=extended_access_list_ip_range (alps_src=port_specifier)? dstipr=extended_access_list_ip_range (alps_dst=port_specifier)?
      (
        ECHO_REPLY
        | ECHO
        | ESTABLISHED
        | FRAGMENTS
        | LOG
        | PACKET_TOO_BIG
        | PORT_UNREACHABLE
        | REDIRECT
        | TIME_EXCEEDED
        | TTL_EXCEEDED
        | UNREACHABLE
      )?
      NEWLINE
    )
    
    {
     iips = new ItemIPAccessListExtendedStanza(ala, prot, srcipr.get(0),
     		srcipr.get(1), dstipr.get(0), dstipr.get(1), alps_src, alps_dst);
    }
  )
  |
  (
    (
      (access_list_action protocol extended_access_list_ip_range port_specifier? extended_access_list_ip_range port_specifier? REFLECT)
      | DYNAMIC
      | EVALUATE
      | REMARK
    )
    ~NEWLINE* NEWLINE
  )
  ;

item_ip_access_list_extended_stanza_list returns [List<ItemIPAccessListExtendedStanza> l = new ArrayList<ItemIPAccessListExtendedStanza>()]
  :
  (x=item_ip_access_list_extended_stanza 
                                        {
                                         if (x != null) {
                                         	l.add(x);
                                         }
                                        })+
  ;

item_ip_access_list_standard_ip_range returns [List<String> alipr = new ArrayList<String>()]
  :
  (
    ( (ip=IP_ADDRESS (wildcard=IP_ADDRESS)?) 
                                            {
                                             String wildcardText = (wildcard == null ? "0.0.0.0" : wildcard.getText());
                                             alipr.add(ip.getText());
                                             alipr.add(wildcardText);
                                            })
    | (ANY 
          {
           alipr.add("0.0.0.0");
           alipr.add("255.255.255.255");
          })
    | (HOST ip=IP_ADDRESS 
                         {
                          alipr.add(ip.getText());
                          alipr.add("0.0.0.0");
                         })
  )
  ;

item_ip_access_list_standard_stanza returns [ItemIPAccessListStandardStanza iips]
  :
  ( (ala=access_list_action ipr=item_ip_access_list_standard_ip_range NEWLINE) 
                                                                              {
                                                                               iips = new ItemIPAccessListStandardStanza(ala, ipr.get(0), ipr.get(1));
                                                                              })
  | (REMARK ~NEWLINE* NEWLINE)
  ;

item_ip_access_list_standard_stanza_list returns [List<ItemIPAccessListStandardStanza> l = new ArrayList<ItemIPAccessListStandardStanza>()]
  :
  (x=item_ip_access_list_standard_stanza 
                                        {
                                         l.add(x);
                                        })+
  ;

macro_stanza
  :
  MACRO ~COMMENT_CLOSING_LINE* closing_comment
  ;

match_as_path_access_list_rm_stanza returns [RMStanza rms]
@init {
List<String> nameList = new ArrayList<String>();
}
  :
  (MATCH AS_PATH (name=DEC 
                          {
                           nameList.add(name.getText());
                          })+ NEWLINE) 
                                      {
                                       RouteMapMatchLine line = new RouteMapMatchAsPathAccessListLine(nameList);
                                       rms = new MatchRMStanza(line);
                                      }
  ;

match_community_list_rm_stanza returns [RMStanza rms]
@init {
List<String> nameList = new ArrayList<String>();
}
  :
  (
    MATCH COMMUNITY
    (
      (
        name=VARIABLE
        | name=DEC
      )
      
      {
       nameList.add(name.getText());
      }
    )+
    NEWLINE
  )
  
  {
   RouteMapMatchLine line = new RouteMapMatchCommunityListLine(nameList);
   rms = new MatchRMStanza(line);
  }
  ;

match_ip_access_list_rm_stanza returns [RMStanza rms]
@init {
List<String> nameList = new ArrayList<String>();
}
  :
  (
    MATCH IP ADDRESS
    (
      (
        name=VARIABLE
        | name=DEC
      )
      
      {
       nameList.add(name.getText());
      }
    )+
    NEWLINE
  )
  
  {
   RouteMapMatchLine line = new RouteMapMatchIpAccessListLine(nameList);
   rms = new MatchRMStanza(line);
  }
  ;

match_ip_prefix_list_rm_stanza returns [RMStanza rms]
@init {
List<String> nameList = new ArrayList<String>();
}
  :
  (
    MATCH IP ADDRESS PREFIX_LIST
    (
      (
        name=VARIABLE
        | name=DEC
      )
      
      {
       nameList.add(name.getText());
      }
    )+
    NEWLINE
  )
  
  {
   RouteMapMatchLine line = new RouteMapMatchIpPrefixListLine(nameList);
   rms = new MatchRMStanza(line);
  }
  ;

match_ipv6_rm_stanza returns [RMStanza rms = new IgnoreRMStanza()]
  :
  MATCH IPV6 ~NEWLINE* NEWLINE
  ;

match_rm_stanza returns [RMStanza rms]
  :
  (
    x=match_as_path_access_list_rm_stanza
    | x=match_community_list_rm_stanza
    | x=match_ip_access_list_rm_stanza
    | x=match_ip_prefix_list_rm_stanza
    | x=match_ipv6_rm_stanza
    | x=match_tag_rm_stanza
  )
  
  {
   rms = x;
  }
  ;

match_tag_rm_stanza returns [RMStanza rms = new NullRMStanza()]
  :
  MATCH TAG ~NEWLINE* NEWLINE
  ;

null_block_stanza
  :
  (
    ARCHIVE
    | CONTROL_PLANE
    | CONTROLLER
    | DIAL_PEER
    |
    (
      FLOW
      (
        EXPORTER
        | MONITOR
        | RECORD
      )
    )
    | GATEWAY
    | GROUP_POLICY
    | IPC
    | (IPV6 ACCESS_LIST)
    | LINE
    | POLICY_MAP
    | REDUNDANCY
    | (SCCP CCM GROUP)
    |
    (
      STCAPP
      (
        FEATURE
        | SUPPLEMENTARY_SERVICES
      )
    )
    | (VLAN DEC)
    | VOICE
    | VOICE_PORT
    | VPDN_GROUP
  )
  ~NEWLINE* NEWLINE null_block_substanza* (closing_comment)
  ;

null_block_substanza
  :
  comment_stanza
  |
  (
    NO?
    (
      ABSOLUTE_TIMEOUT
      | ACCEPT_DIALIN
      | ACCESS_CLASS
      | ACCOUNTING_SERVER_GROUP
      | ACTIVATION_CHARACTER
      | ADDRESS_POOL
      | ANYCONNECT
      | ASSOCIATE
      | ASSOCIATION
      | AUTHENTICATION
      | AUTHENTICATION_SERVER_GROUP
      | AUTHORIZATION
      | AUTHORIZATION_REQUIRED
      | AUTHORIZATION_SERVER_GROUP
      | AUTO_SYNC
      | AUTOSELECT
      | BANDWIDTH
      | BANNER
      | BIND
      | BRIDGE
      | CABLELENGTH
      | CACHE
      | CALL
      | CALLER_ID
      | CAS_CUSTOM
      | CERTIFICATE
      | CLASS
      | CLOCK
      | COLLECT
      | CONFORM_ACTION
      | CPTONE
      | CRL
      | CRYPTO
      | DBL
      | DEFAULT_DOMAIN
      | DEFAULT_GROUP_POLICY
      | DENY
      | DESCRIPTION
      | DESTINATION
      | DIAGNOSTIC
      | DNS_SERVER
      | DS0_GROUP
      | ENROLLMENT
      | EXCEED_ACTION
      | EXEC
      | EXEC_TIMEOUT
      | EXPORT_PROTOCOL
      | EXPORTER
      | FABRIC
      | FALLBACK_DN
      | FILE_BROWSING
      | FILE_ENTRY
      | FQDN
      | FRAMING
      | GROUP_ALIAS
      | GROUP_POLICY
      | GROUP_URL
      | HIDDEN_SHARES
      | HIDEKEYS
      | HISTORY
      | INSPECT
      | IP
      | IPSEC_UDP
      | IPV6
      | IPV6_ADDRESS_POOL
      | ISAKMP
      | KEEPALIVE_ENABLE
      | KEYPAIR
      | L2TP
      | LINE
      | LINECODE
      | LLDP
      | LOCAL_IP
      | LOCAL_PORT
      | LOCATION
      | LOG
      | LOGGING
      | LOGIN
      | MAIN_CPU
      | MATCH
      | MESSAGE_LENGTH
      | MODE
      | MODEM
      | MTU
      | NAME
      | NOTIFY
      | PARAMETERS
      | PARENT
      | PASSWORD_STORAGE
      | PERMIT
      | PICKUP
      | POLICE
      | POLICY_MAP
      | PORT
      | PREFIX
      | PRI_GROUP
      | PRIORITY
      | PRIVILEGE
      | PROTOCOL
      | QUEUE_BUFFERS
      | QUEUE_LIMIT
      | RD
      | RECORD
      | RECORD_ENTRY
      | REMARK
      | REMOTE_IP
      | REMOTE_PORT
      | REMOTE_SPAN
      | REMOVED
      | RING
      | SCHEME
      | SERVICE
      | SERVICE_POLICY
      | SERVICE_TYPE
      | SESSION_LIMIT
      | SESSION_TIMEOUT
      | SET
      | SHUTDOWN
      | SOURCE
      | SPANNING_TREE
      | SPEED
      | SPLIT_TUNNEL_NETWORK_LIST
      | SPLIT_TUNNEL_POLICY
      | STOPBITS
      | STP
      | SUBJECT_NAME
      | SWITCHBACK
      | TB_VLAN1
      | TB_VLAN2
      | TERMINAL_TYPE
      | TIMEOUTS
      | TIMER
      | TIMING
      | TRANSPORT
      | TUNNEL_GROUP
      | VIOLATE_ACTION
      | VIRTUAL_TEMPLATE
      | VPN_FILTER
      | VPN_IDLE_TIMEOUT
      | VPN_TUNNEL_PROTOCOL
      | WEBVPN
      | WINS_SERVER
      | WITHOUT_CSD
    )
    ~NEWLINE* NEWLINE
  )
  ;

null_rm_stanza returns [RMStanza rms = new NullRMStanza()]
  :
  NO? (DESCRIPTION) ~NEWLINE* NEWLINE
  ;

null_standalone_stanza
  :
  (NO)?
  (
    AAA
    | AAA_SERVER
    | ACCESS_GROUP
    |
    (
      ACCESS_LIST
      (
        (DEC REMARK)
        | VARIABLE
      )
    )
    | ACCOUNTING_PORT
    | ACTION
    | ACTIVE
    | ADDRESS
    | ALERT_GROUP
    | ALIAS
    | ANYCONNECT
    | ANYCONNECT_ESSENTIALS
    | AP
    | ARP
    | ASA
    | ASDM
    | ASSOCIATE
    | ASYNC_BOOTP
    | AUTHENTICATION
    | AUTHENTICATION_PORT
    | AUTO
    | BOOT
    | BOOT_END_MARKER
    | BOOT_START_MARKER
    | CALL
    | CALL_HOME
    | CARD
    | CCM_MANAGER
    | CDP
    | CIPC
    | CLASS_MAP
    | CLOCK
    | CLUSTER
    | CNS
    | CODEC
    | CONFIG_REGISTER
    | CONSOLE
    | CONTACT_EMAIL_ADDR
    | CRL
    | CRYPTO
    | CTL_FILE
    | CTS
    | DEFAULT
    | DESCRIPTION
    | DESTINATION
    | DEVICE_SENSOR
    | DHCPD
    | DIAGNOSTIC
    | DIALER_LIST
    | DISABLE
    | DNS
    | DNS_GUARD
    | DOMAIN_NAME
    | DOT11
    | DSP
    | DSPFARM
    | DYNAMIC_ACCESS_POLICY_RECORD
    | ENABLE
    | ENCR
    | ENCRYPTION
    | ENROLLMENT
    | ENVIRONMENT
    | ERRDISABLE
    | EVENT
    | EXCEPTION
    | FABRIC
    | FAILOVER
    | FILE
    | FIREWALL
    | FIRMWARE
    | FQDN
    | FTP
    | FTP_SERVER
    | GATEKEEPER
    | GROUP
    | GROUP_OBJECT
    | HASH
    | HOST
    | HTTP
    | HW_MODULE
    | ICMP
    | ICMP_ECHO
    | ICMP_OBJECT
    | IDENTITY
    | INACTIVITY_TIMER
    |
    (
      IP
      (
        ADDRESS_POOL
        | ADMISSION
        | ALIAS
        | ARP
        | AUDIT
        | AUTH_PROXY
        | BOOTP
        | BGP_COMMUNITY
        | CEF
        | CLASSLESS
        | DEFAULT_NETWORK
        | DEVICE
        | DHCP
        | DOMAIN
        | DOMAIN_LIST
        | DOMAIN_LOOKUP
        | DOMAIN_NAME
        | FINGER
        | FLOW_CACHE
        | FLOW_EXPORT
        | FORWARD_PROTOCOL
        | FTP
        | GRATUITOUS_ARPS
        | HOST_ROUTING
        | HTTP
        | IGMP
        | LOCAL
        | MFIB
        | MROUTE
        | MSDP
        | MULTICAST
        | MULTICAST_ROUTING
        | NAME_SERVER
        | NAT
        | (OSPF NAME_LOOKUP)
        | (PREFIX_LIST VARIABLE (SEQ DEC)? DESCRIPTION)
        | PIM
        | RADIUS
        | RCMD
        | (ROUTE VRF)
        | ROUTING //might want to use this eventually
        | SAP
        | SCP
        | SLA
        | SOURCE_ROUTE
        | SSH
        | SUBNET_ZERO
        | TACACS
        | TCP
        | TELNET
        | TFTP
        | VERIFY
        | VRF
      )
    )
    | IP_ADDRESS_LITERAL
    |
    (
      IPV6
      (
        CEF
        | HOST
        | LOCAL
        | MFIB
        | MLD
        | MULTICAST
        | MULTICAST_ROUTING
        | ND
        | (OSPF NAME_LOOKUP)
        | PIM
        | PREFIX_LIST
        | ROUTE
        | UNICAST_ROUTING
      )
    )
    | ISDN
    | KEEPOUT
    | KEYPAIR
    | LDAP_BASE_DN
    | LDAP_LOGIN
    | LDAP_LOGIN_DN
    | LDAP_NAMING_ATTRIBUTE
    | LDAP_SCOPE
    | LICENSE
    | LIFETIME
    | LLDP
    | LOGGING
    | MAC
    | MAC_ADDRESS_TABLE
    | MAIL_SERVER
    | MATCH
    | MAXIMUM
    | MEDIA_TERMINATION
    | MEMORY_SIZE
    | MGCP
    | MICROCODE
    | MLS
    | MODE
    | MODEM
    | MONITOR
    | MPLS
    | MTA
    | MTU
    | MULTILINK
    | NAME_SERVER
    | NAME
    | NAMES
    | NAT
    | NAT_CONTROL
    | NETWORK_OBJECT
    | NETWORK_CLOCK_PARTICIPATE
    | NETWORK_CLOCK_SELECT
    | NTP
    | OBJECT
    | OBJECT_GROUP
    | PAGER
    | PARTICIPATE
    | PERCENT
    | PHONE_PROXY
    | PLATFORM
    | PORT_OBJECT
    | POWER
    | PRIORITY
    | PRIORITY_QUEUE
    | PRIVILEGE
    | PROCESS
    | PROFILE
    | PROMPT
    | PROTOCOL_OBJECT
    | QOS
    | RADIUS_COMMON_PW
    | RADIUS_SERVER
    | RD
    | RECORD_ENTRY
    | REDIRECT_FQDN
    | RESOURCE
    | RESOURCE_POOL
    | REVOCATION_CHECK
    | ROUTE
    | RSAKEYPAIR
    | RTR
    | SAME_SECURITY_TRAFFIC
    |
    (
      SCCP
      (
        (CCM IP_ADDRESS)
        | LOCAL
      )
    )
    | SCHEDULER
    | SCRIPTING
    | SECURITY
    | SENDER
    | SERIAL_NUMBER
    | SERVER
    | SERVER_TYPE
    | SERVICE
    | SERVICE_POLICY
    | SET
    | SHELL
    | SHUTDOWN
    | SMTP_SERVER
    | SNMP
    | SNMP_SERVER
    | SOURCE
    | SOURCE_INTERFACE
    | SOURCE_IP_ADDRESS
    | SPANNING_TREE
    | SPE
    | SSH
    | SSL
    | STATIC
    | (STCAPP (CCM_GROUP))
    | SUBJECT_NAME
    | SUBNET
    | SUBSCRIBE_TO
    | SUBSCRIBE_TO_ALERT_GROUP
    | SWITCH
    | SYSOPT
    | SYSTEM
    | TABLE_MAP
    | TAG_SWITCHING
    | TELNET
    | TFTP_SERVER
    | THREAT_DETECTION
    | TIMEOUT
    | TLS_PROXY
    | TRACK
    | TRANSLATE
    | TRANSPORT
    | TUNNEL_GROUP_LIST
    | TYPE
    | UDLD
    | UNABLE
    | UPGRADE
    | USER_IDENTITY
    | USERNAME
    | VALIDATION_USAGE
    | VERSION
    |
    (
      VLAN
      (
        ACCESS_LOG
        | DOT1Q
        | INTERNAL
      )
    )
    | VMPS
    | VPDN
    | VPN
    | VTP
    | VOICE_CARD
    | WEBVPN
    | WSMA
    | X25
    | X29
    | XLATE
  )
  ~NEWLINE* NEWLINE
  ;

null_stanza returns [Stanza s = new NullStanza()]
  :
  banner_stanza
  | certificate_stanza
  | closing_comment
  | comment_stanza
  | macro_stanza
  | null_block_stanza
  | null_standalone_stanza
  |
  (
    (
      | SCCP
      | STCAPP
    )
    NEWLINE
  )
  | vrf_stanza
  ;

port_specifier returns [List < SubRange > range = new ArrayList<SubRange>()]
  :
  (EQ (x=port 
             {
              range.add(new SubRange(x, x));
             })+)
  | ( (GT x=port) 
                 {
                  range.add(new SubRange(x + 1, 65535));
                 })
  | ( (NEQ x=port) 
                  {
                   range.add(new SubRange(0, x - 1));
                   range.add(new SubRange(x + 1, 65535));
                  })
  | ( (LT x=port) 
                 {
                  range.add(new SubRange(0, x - 1));
                 })
  | ( (RANGE x=port y=port) 
                           {
                            range.add(new SubRange(x, y));
                           })
  ;

port returns [int i]
  :
  (d=DEC 
        {
         i = Integer.parseInt(d.getText());
        })
  | (BOOTPC 
           {
            i = 68;
           })
  | (BOOTPS 
           {
            i = 67;
           })
  | (BGP 
        {
         i = 179;
        })
  | (CMD 
        {
         i = 514;
        })
  | (DOMAIN 
           {
            i = 53;
           })
  | (FTP 
        {
         i = 21;
        })
  | (FTP_DATA 
             {
              i = 20;
             })
  | (ISAKMP 
           {
            i = 500;
           })
  | (LPD 
        {
         i = 515;
        })
  | (NETBIOS_DGM 
                {
                 i = 138;
                })
  | (NETBIOS_NS 
               {
                i = 137;
               })
  | (NETBIOS_SS 
               {
                i = 139;
               })
  | (NON500_ISAKMP 
                  {
                   i = 4500;
                  })
  | (NTP 
        {
         i = 123;
        })
  | (PIM_AUTO_RP 
                {
                 i = 496;
                })
  | (POP3 
         {
          i = 110;
         })
  | (SMTP 
         {
          i = 25;
         })
  | (SNMP 
         {
          i = 161;
         })
  | (SNMPTRAP 
             {
              i = 162;
             })
  | (SYSLOG 
           {
            i = 514;
           })
  | (TACACS 
           {
            i = 49;
           })
  | (TELNET 
           {
            i = 23;
           })
  | (TFTP 
         {
          i = 69;
         })
  | (WWW 
        {
         i = 80;
        })
  ;

protocol returns [int i]
  :
  (d=DEC 
        {
         i = Integer.parseInt(d.getText());
        })
  | (ESP 
        {
         i = 50;
        })
  | (GRE 
        {
         i = 47;
        })
  | (ICMP 
         {
          i = 1;
         })
  | (IGMP 
         {
          i = 2;
         })
  | (IP 
       {
        i = 0;
       })
  | (OSPF 
         {
          i = 89;
         })
  | (PIM 
        {
         i = 103;
        })
  | (SCTP 
         {
          i = 132;
         })
  | (TCP 
        {
         i = 6;
        })
  | (UDP 
        {
         i = 17;
        })
  ;

range returns [List < SubRange > lsr = new ArrayList<SubRange>()]
  :
  (
    (x=subrange 
               {
                lsr.add(x);
               }) ( (COMMA y=subrange) 
                                      {
                                       lsr.add(y);
                                      })*
  )
  | NONE
  ;

rm_stanza returns [RMStanza rms]
  :
  (
    x=match_rm_stanza
    | x=null_rm_stanza
    | x=set_rm_stanza
  )
  
  {
   rms = x;
  }
  ;

rm_stanza_list returns [List<RMStanza> rmsl = new ArrayList<RMStanza>()]
  :
  ( (x=rm_stanza) 
                 {
                  rmsl.add(x);
                 })+
  ;

route_map_stanza returns [Stanza s]
@init {
RouteMapStanza rms = null;
}
  :
  (
    ROUTE_MAP name=VARIABLE rmt=access_list_action (num=integer) 
                                                                {
                                                                 rms = new RouteMapStanza(rmt, name.getText(), num);
                                                                }
    NEWLINE ( (rml=rm_stanza_list) 
                                  {
                                   for (RMStanza r : rml) {
                                   	rms.processStanza(r);
                                   }
                                  })? closing_comment
  )
  
  {
   s = rms;
  }
  ;

set_as_path_rm_stanza returns [RMStanza rms]
@init {
List<Integer> asList = new ArrayList<Integer>();
}
  :
  (SET AS_PATH (as=integer 
                          {
                           asList.add(as);
                          })* NEWLINE) 
                                      {
                                       //RouteMapSetLine line = new RouteMapSetAsPathLine(asList);
                                       //rms = new SetRMStanza(line);
                                       rms = new NullRMStanza();
                                      }
  ;

set_as_path_prepend_rm_stanza returns [RMStanza rms]
@init {
List<Integer> asList = new ArrayList<Integer>();
}
  :
  (SET AS_PATH PREPEND (as=integer 
                                  {
                                   asList.add(as);
                                  })* NEWLINE) 
                                              {
                                               //RouteMapSetLine line = new RouteMapSetAsPathPrependLine(asList);
                                               //rms = new SetRMStanza(line);
                                               rms = new NullRMStanza();
                                              }
  ;

set_community_additive_rm_stanza returns [RMStanza rms]
@init {
List<Long> communities = new ArrayList<Long>();
}
  :
  (SET COMMUNITY ( (comm=community) 
                                   {
                                    communities.add(comm);
                                   })+ ADDITIVE NEWLINE) 
                                                        {
                                                         RouteMapSetAdditiveCommunityLine line = new RouteMapSetAdditiveCommunityLine(
                                                         		communities);
                                                         rms = new SetRMStanza(line);
                                                        }
  ;

set_comm_list_delete_rm_stanza returns [RMStanza rms]
  :
  (
    SET COMM_LIST
    (
      name=DEC
      | name=VARIABLE
    )
    DELETE NEWLINE
  )
  
  {
   RouteMapSetDeleteCommunityLine line = new RouteMapSetDeleteCommunityLine(
   		name.getText());
   rms = new SetRMStanza(line);
  }
  ;

set_community_rm_stanza returns [RMStanza rms]
@init {
List<Long> communities = new ArrayList<Long>();
}
  :
  (SET COMMUNITY ( (comm=community) 
                                   {
                                    communities.add(comm);
                                   })+ NEWLINE) 
                                               {
                                                RouteMapSetCommunityLine line = new RouteMapSetCommunityLine(communities);
                                                rms = new SetRMStanza(line);
                                               }
  ;

set_ipv6_rm_stanza returns [RMStanza rms = new IgnoreRMStanza()]
  :
  SET IPV6 ~NEWLINE* NEWLINE
  ;

set_local_preference_rm_stanza returns [RMStanza rms]
  :
  (SET LOCAL_PREFERENCE pref=integer NEWLINE) 
                                             {
                                              RouteMapSetLocalPreferenceLine line = new RouteMapSetLocalPreferenceLine(pref);
                                              rms = new SetRMStanza(line);
                                             }
  ;

set_metric_rm_stanza returns [RMStanza rms]
  :
  (SET METRIC met=integer NEWLINE) 
                                  {
                                   RouteMapSetMetricLine line = new RouteMapSetMetricLine(met);
                                   rms = new SetRMStanza(line);
                                  }
  ;

set_next_hop_rm_stanza returns [RMStanza rms]
@init {
List<String> nextHops = new ArrayList<String>();
}
  :
  (SET IP NEXT_HOP (nexthop=IP_ADDRESS 
                                      {
                                       nextHops.add(nexthop.getText());
                                      })+ NEWLINE) 
                                                  {
                                                   RouteMapSetNextHopLine line = new RouteMapSetNextHopLine(nextHops);
                                                   rms = new SetRMStanza(line);
                                                  }
  ;

set_origin_rm_stanza returns [RMStanza rms = new NullRMStanza()]
  :
  SET ORIGIN ~NEWLINE* NEWLINE
  ;

set_rm_stanza returns [RMStanza rms]
  :
  (
    x=set_as_path_rm_stanza
    | x=set_as_path_prepend_rm_stanza
    | x=set_comm_list_delete_rm_stanza
    | x=set_community_rm_stanza
    | x=set_community_additive_rm_stanza
    | x=set_ipv6_rm_stanza
    | x=set_local_preference_rm_stanza
    | x=set_metric_rm_stanza
    | x=set_next_hop_rm_stanza
    | x=set_origin_rm_stanza
  )
  
  {
   rms = x;
  }
  ;

standard_access_list_stanza returns [Stanza s]
  :
  (ACCESS_LIST {1 <= nextIntVal() && nextIntVal() <= 99}? num=DEC ala=access_list_action ipr=standard_access_list_ip_range LOG? NEWLINE) 
                                                                                                                                        {
                                                                                                                                         s = new StandardAccessListStanza(ala, num.getText(), ipr.get(0), ipr.get(1));
                                                                                                                                        }
  ;

standard_access_list_ip_range returns [List<String> alipr = new ArrayList<String>()]
  :
  (
    ( (ip=IP_ADDRESS (wildcard=IP_ADDRESS)?) 
                                            {
                                             String wildcardText = (wildcard == null ? "0.0.0.0" : wildcard.getText());
                                             alipr.add(ip.getText());
                                             alipr.add(wildcardText);
                                            })
    | (ANY 
          {
           alipr.add("0.0.0.0");
           alipr.add("255.255.255.255");
          })
  )
  ;

stanza returns [Stanza s]
  :
  (
    x=access_list_stanza
    | x=hostname_stanza
    | x=interface_stanza
    | x=ip_access_list_extended_stanza
    | x=ip_access_list_standard_stanza
    | x=ip_as_path_access_list_stanza
    | x=ip_community_list_stanza
    | x=ip_default_gateway_stanza
    | x=ip_prefix_list_line_stanza
    | x=ip_route_stanza
    | x=ipv6_router_ospf_stanza
    | x=null_stanza
    | x=route_map_stanza
    | x=router_bgp_stanza
    | x=router_ospf_stanza
  )
  
  {
   s = x;
  }
  ;

stanza_list returns [List<Stanza> l = new ArrayList<Stanza>()]
  :
  (x=stanza 
           {
            l.add(x);
           })+
  ;

subrange returns [SubRange s]
  :
  ( (x=integer DASH y=integer) 
                              {
                               s = new SubRange(x, y);
                              })
  | (x=integer 
              {
               s = new SubRange(x, x);
              })
  ;

vrf_stanza
  :
  VRF ~NEWLINE* NEWLINE null_block_substanza* closing_comment address_family_vrf_stanza*
  ;

AMPERSAND
  :
  '&'
  ;

ARP
  :
  ('arp') => 'arp' 
                  {
                   enableIPV6_ADDRESS = false;
                  }
  ;

ASTERISK
  :
  '*'
  ;

AT
  :
  '@'
  ;

BACKSLASH
  :
  '\\'
  ;

BRACE_LEFT
  :
  '{'
  ;

BRACE_RIGHT
  :
  '}'
  ;

BRACKET_LEFT
  :
  '['
  ;

BRACKET_RIGHT
  :
  ']'
  ;

CABLELENGTH
  :
  ('cablelength') => 'cablelength' 
                                  {
                                   inComment = true;
                                  }
  ;

CARAT
  :
  '^'
  ;

CERTIFICATE
  :
  ('certificate') => 'certificate' 
                                  {
                                   inComment = true;
                                   inMultilineComment = true;
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

COMMANDER_ADDRESS
  :
  ('commander-address') => 'commander-address' 
                                              {
                                               enableIPV6_ADDRESS = false;
                                              }
  ;

COMMUNITY_LIST
  :
  ('community-list') => 'community-list' 
                                        {
                                         enableIPV6_ADDRESS = false;
                                        }
  ;

COMMUNITY
  :
  ('community') => 'community' 
                              {
                               enableIPV6_ADDRESS = false;
                              }
  ;

COMMENT_LINE
  :
  {!inComment}?=>
  (
    ('!' ~NEWLINE_CHAR) => ('!' ~NEWLINE_CHAR+ NEWLINE_CHAR)
  )
  ;

COMMENT_CLOSING_LINE
  :
  {!inComment}?=> ('!' NEWLINE_CHAR)
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
  DIGIT+
  ;

DESCRIPTION
  :
  ('description') => 'description' 
                                  {
                                   inComment = true;
                                  }
  ;

DOUBLE_QUOTE
  :
  '"'
  ;

ENGINEID
  :
  ('engineid') => 'engineid' 
                            {
                             inComment = true;
                            }
  ;

EQUALS
  :
  '='
  ;

ESCAPE_C
  :
  (
    ('^C') => '^C'
  )
  
  {
   inMultilineComment = !inMultilineComment;
   inComment = !inComment;
  }
  ;

EXCLAMATION_MARK
  :
  {inComment}?=> '!'
  ;

FIREWALL
  :
  ('firewall') => 'firewall' 
                            {
                             enableIPV6_ADDRESS = false;
                            }
  ;

FLOAT
  :
  {!inComment}?=> (POSITIVE_DIGIT* DIGIT '.' DIGIT+)
  ;

FORWARD_SLASH
  :
  '/'
  ;

HEX
  :
  '0x' HEX_DIGIT+
  ;

//HEX_STRING
//  :
//  {inComment || !enableIPV6_ADDRESS}?=> HEX_DIGIT+
//  ;

INTERFACE
  :
  ('interface') => 'interface' 
                              {
                               enableIPV6_ADDRESS = false;
                              }
  ;

SHA1_HASH
  :
  'sha1' ' ' DIGIT+ ' ' HEX_DIGIT+
  ;

DES_HASH
  :
  'des' ' ' DIGIT+ ' ' HEX_DIGIT+
  ;

IP_ADDRESS
  :
  {!inComment && enableIP_ADDRESS}?=> (DEC_BYTE '.' DEC_BYTE '.' DEC_BYTE '.' DEC_BYTE)
  ;

IPV6_ADDRESS
  :
  {!inComment && enableIPV6_ADDRESS}?=>
  (
    (COLON COLON ( (HEX_DIGIT+ COLON)* HEX_DIGIT+)?)
    | (HEX_DIGIT+ COLON COLON?)+ (HEX_DIGIT+)?
  )
  ;

LOCATION
  :
  ('location') => 'location' 
                            {
                             inComment = true;
                            }
  ;

MAC
  :
  ('mac') => 'mac' 
                  {
                   inComment = true;
                  }
  ;

MAC_ADDRESS
  :
  ('mac-address') => 'mac-address' 
                                  {
                                   inComment = true;
                                  }
  ;

NAME
  :
  ('name') => 'name' 
                    {
                     inComment = true;
                    }
  ;

NEWLINE
  :
  NEWLINE_CHAR 
              {
               if (!inMultilineComment) {
               	inComment = false;
               	enableIPV6_ADDRESS = true;
               	enableIP_ADDRESS = true;
               }
              }
  ;

OUI
  :
  ('oui') => 'oui' 
                  {
                   inComment = true;
                  }
  ;

PAREN_LEFT
  :
  '('
  ;

PAREN_RIGHT
  :
  ')'
  ;

PASSWORD
  :
  ('password') => 'password' 
                            {
                             inComment = true;
                            }
  ;

PERCENT
  :
  '%'
  ;

PERIOD
  :
  '.'
  ;

PLUS
  :
  '+'
  ;

POUND
  :
  '#'
  ;

REMARK
  :
  ('remark') => 'remark' 
                        {
                         inComment = true;
                        }
  ;

QUIT
  :
  ('quit') => 'quit' 
                    {
                     inMultilineComment = false;
                     inComment = false;
                    }
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
    | '&'
    | '+'
    | '['
    | ']'
    | ( {!enableIPV6_ADDRESS}?=> ':')
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
NEWLINE_CHAR
  :
  '\n'
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
HEX_WORD
  :
  HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
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
