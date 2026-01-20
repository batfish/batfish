lexer grammar FortiosLexer;

options {
  superClass = 'org.batfish.grammar.fortios.parsing.FortiosBaseLexer';
}

tokens {
  IGNORED_CONFIG_BLOCK,
  QUOTED_TEXT,
  STR_SEPARATOR,
  UNIMPLEMENTED_PLACEHOLDER,
  UNQUOTED_WORD_CHARS
}

// Keyword Tokens

ACCPROFILE: 'accprofile' {
  // ignore config system accprofile
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
ADMIN:
  'admin'
  {
    if (lastTokenType() == REPLACEMSG) {
      pushMode(M_Str);
    } else if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
      // ignore config system admin
      setType(IGNORED_CONFIG_BLOCK);
      pushMode(M_IgnoredConfigBlock);
    }
  }
;
ADMINTIMEOUT: 'admintimeout' -> pushMode(M_Str);
ADMIN_HTTPS_SSL_VERSIONS: 'admin-https-ssl-versions' -> pushMode(M_Str);
ACCEPT: 'accept';
ACCESS_LIST: 'access-list';
ACTION: 'action';
ANTIVIRUS: 'antivirus' {
  // ignore config antivirus
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
ADDRESS: 'address';
ADDRESS6: 'address6' {
  // ignore config firewall address6
  if (lastTokenType() == FIREWALL && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
ADDRGRP: 'addrgrp';
AFTER: 'after' -> pushMode(M_SingleStr);
AGGREGATE: 'aggregate';
ALERTMAIL: 'alertmail' {
  if (lastTokenType() == REPLACEMSG) {
    pushMode(M_Str);
  }
};
ALIAS: 'alias' -> pushMode(M_Str);
ALL: [aA][lL][lL];
ALLOW: 'allow';
ALLOWACCESS: 'allowaccess' -> pushMode(M_Str);
ALLOW_ROUTING: 'allow-routing';
ANY: 'any';
API_USER: 'api-user' {
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    // ignore config system api-user
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
APPLICATION: 'application' {
  // ignore config application
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
APPLICATION_LIST: 'application-list' -> pushMode(M_Str);
AV_PROFILE: 'av-profile' -> pushMode(M_Str);
APPEND: 'append';
AS: 'as' -> pushMode(M_Str);
ASSOCIATED_INTERFACE: 'associated-interface' -> pushMode(M_Str);
AUTH: 'auth' {
  if (lastTokenType() == REPLACEMSG) {
    pushMode(M_Str);
  }
};
AUTO_ASIC_OFFLOAD: 'auto-asic-offload';
AUTOMATION: 'automation' {
  if (lastTokenType() == REPLACEMSG) {
    pushMode(M_Str);
  }
};
AUTO: 'auto';
AUTOMATION_ACTION: 'automation-action' {
  // ignore config system automation-action
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
AUTOMATION_STITCH: 'automation-stitch' {
  // ignore config system automation-stitch
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
AUTOMATION_TRIGGER: 'automation-trigger' {
  // ignore config system automation-trigger
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
BEFORE: 'before' -> pushMode(M_SingleStr);
BGP: 'bgp';
BUFFER: 'buffer' -> pushMode(M_Str);
CASB: 'casb' {
  // ignore config casb
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
CACHE_TTL: 'cache-ttl';
CATEGORY: 'category' {
  // ignore config firewall service category
  if (lastTokenType() == SERVICE && secondToLastTokenType() == FIREWALL) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  } else if (lastTokenType() == SET && secondToLastTokenType() == NEWLINE) {
    pushMode(M_Str);
  }
};
CLEAR: 'clear';
CLONE: 'clone' -> pushMode(M_SingleStr);
COLOR: 'color';
COMMENT: 'comment' -> pushMode(M_Str);
COMMENTS: 'comments' -> pushMode(M_Str);
CONFIG: 'config';
CONSOLE: 'console' {
  // ignore config system console
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
COUNTRY: 'country';
CUSTOM: 'custom';
CUSTOM_LANGUAGE: 'custom-language' {
  // ignore config system custom-language
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};

DEFAULT: 'default';
DEDICATED_TO: 'dedicated-to' -> pushMode(M_Str);
DELETE: 'delete' -> pushMode(M_Str);
DENY: 'deny';
DESCRIPTION: 'description' -> pushMode(M_Str);
DEVICE: 'device' -> pushMode(M_Str);
DEVICE_IDENTIFICATION: 'device-identification' -> pushMode(M_Str);
DNS: 'dns' {
  // ignore config system dns
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
DNSFILTER: 'dnsfilter' {
  // ignore config dnsfilter
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
DNSFILTER_PROFILE: 'dnsfilter-profile' -> pushMode(M_Str);
EMAIL_FILTER: 'emailfilter' {
  // ignore config emailfilter
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
DISABLE: 'disable';
DISTANCE: 'distance';
DLP: 'dlp' {
  // ignore config dlp
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
DOS_POLICY: 'DoS-policy' {
  if (lastTokenType() == FIREWALL && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
DOWN: 'down';
DST: 'dst';
DSTADDR: 'dstaddr' -> pushMode(M_Str);
DSTINTF: 'dstintf' -> pushMode(M_Str);
DYNAMIC: 'dynamic';
EBGP_MULTIPATH: 'ebgp-multipath';
EDIT: 'edit' -> pushMode(M_Str);
EMAC_VLAN: 'emac-vlan';
ENDPOINT_CONTROL: 'endpoint-control' {
  // ignore config endpoint-control
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
EMAIL_SERVER: 'email-server' {
  // ignore config system email-server
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
ENABLE: 'enable';
END: 'end';
END_IP: 'end-ip';
ENDIP: 'endip';
EXACT_MATCH: 'exact-match';
EXCLUDE: 'exclude';
EXCLUDE_MEMBER: 'exclude-member' -> pushMode(M_Str);
FABRIC_OBJECT: 'fabric-object';
FEDERATED_UPGRADE: 'federated-upgrade' {
  // ignore config system federated-upgrade
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
FILE_FILTER: 'file-filter' {
  // ignore config file-filter
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  } else {
    pushMode(M_Str);
  }
};
FILE_FILTER_PROFILE: 'file-filter-profile' -> pushMode(M_Str);
FIREWALL: 'firewall';
FIXED_PORT_RANGE: 'fixed-port-range';
FOLDER: 'folder';
FORTIGUARD: 'fortiguard' {
  // ignore config system fortiguard
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
FORWARD_ERROR_CORRECTION: 'forward-error-correction' -> pushMode(M_Str);
FORTIGUARD_WF: 'fortiguard-wf' {
  if (lastTokenType() == REPLACEMSG) {
    pushMode(M_Str);
  }
};
FQDN: 'fqdn';
FTM_PUSH: 'ftm-push' {
  // ignore config system ftm-push
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
FTP: 'ftp' {
  if (lastTokenType() == REPLACEMSG) {
    pushMode(M_Str);
  }
};
GATEWAY: 'gateway';
GEOGRAPHY: 'geography';
GLOBAL: 'global';
GROUP: 'group';
GUI_AUTO_UPGRADE_SETUP_WARNING: 'gui-auto-upgrade-setup-warning' -> pushMode(M_Str);
HA: 'ha' {
  // ignore config system ha
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
HOSTNAME: 'hostname' -> pushMode(M_Str);
HTTP: 'http' {
  if (lastTokenType() == REPLACEMSG) {
    pushMode(M_Str);
  }
};
IBGP_MULTIPATH: 'ibgp-multipath';
ICAP: 'icap' {
  if (lastTokenType() == REPLACEMSG) {
    pushMode(M_Str);
  } else if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
ICMP: 'ICMP';
ICMP6: 'ICMP6';
ICMPCODE: 'icmpcode';
ICMPTYPE: 'icmptype';
IKE: 'ike' {
  // ignore config system ike
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
INTERFACE: 'interface' -> pushMode(M_Str);
INTERFACE_SUBNET: 'interface-subnet';
INTERNET_SERVICE_ID: 'internet-service-id' -> pushMode(M_Str);
INTERNET_SERVICE_NAME: 'internet-service-name';
INTERNET_SERVICE_DEFINITION: 'internet-service-definition' {
  // ignore config firewall internet-service-definition
  if (lastTokenType() == FIREWALL && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
INTRAZONE: 'intrazone';
IP: 'ip';
IPS: 'ips' {
  // ignore config ips
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
IPS_SENSOR: 'ips-sensor' -> pushMode(M_Str);
IPMASK: 'ipmask';
IP_MANAGED_BY_FORTIIPAM: 'ip-managed-by-fortiipam' -> pushMode(M_Str);
IPRANGE: 'iprange';
IP_UPPER: 'IP';
IPAM: 'ipam' {
  // ignore config system ipam
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
IPPOOL: 'ippool';
IPSEC: 'ipsec';
ISIS: 'isis' {
  if (lastTokenType() == ROUTER && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
LLDP_TRANSMISSION: 'lldp-transmission' -> pushMode(M_Str);
LOCAL_IN_POLICY: 'local-in-policy' {
  // ignore config firewall local-in-policy
  if (lastTokenType() == FIREWALL && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
LOCATION: 'location';
LOG: 'log' {
  // ignore config log
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
LOGTRAFFIC: 'logtraffic';
LOGTRAFFIC_START: 'logtraffic-start';
LOG_SINGLE_CPU_HIGH: 'log-single-cpu-high' -> pushMode(M_Str);
LOOPBACK: 'loopback';
MAC: 'mac';
MAIL: 'mail' {
  if (lastTokenType() == REPLACEMSG) {
    pushMode(M_Str);
  }
};
MATCH_IP_ADDRESS: 'match-ip-address' -> pushMode(M_Str);
MEDIATYPE: 'mediatype' -> pushMode(M_Str);
MEMBER: 'member' -> pushMode(M_Str);
MOVE: 'move' -> pushMode(M_SingleStr);
MODE: 'mode' -> pushMode(M_Str);
MONITOR_BANDWIDTH: 'monitor-bandwidth' -> pushMode(M_Str);
MTU: 'mtu';
MTU_OVERRIDE: 'mtu-override';
MULTICAST: 'multicast' {
  if (lastTokenType() == ROUTER && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
MULTICAST_ADDRESS: 'multicast-address' {
  // ignore config firewall multicast-address
  if (lastTokenType() == FIREWALL && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
NAT: 'nat';
NAC_QUAR: 'nac-quar' {
  if (lastTokenType() == REPLACEMSG) {
    pushMode(M_Str);
  }
};
NAME: 'name' -> pushMode(M_Str);
NEIGHBOR: 'neighbor';
NETWORK: 'network';
NETFLOW: 'netflow' {
  // ignore config system netflow
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
NEXT: 'next';
ONE_TO_ONE: 'one-to-one';
OVERLOAD: 'overload';
NP_QUEUES: 'np-queues' {
  // ignore config np-queues
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
NP6: 'np6' {
  // ignore config system np6
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
NP_ACCELERATION: 'np-acceleration';
NPU: 'npu' {
  // ignore config system npu
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
NTP: 'ntp' {
  // ignore config system ntp
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
OBJECT_TAGGING: 'object-tagging' {
  // ignore config system object-tagging
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
ON_DEMAND_SNIFFER: 'on-demand-sniffer' {
  if (lastTokenType() == FIREWALL && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
OSPF: 'ospf' {
  if (lastTokenType() == ROUTER && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
SETTINGS: 'settings' {
  // ignore config system settings
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
SESSION_TTL: 'session-ttl' {
  // ignore config system session-ttl
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
PERMIT: 'permit';
PHYSICAL: 'physical';
PHYSICAL_SWITCH: 'physical-switch' {
  // ignore config system physical-switch
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
POLICY: 'policy';
POOLNAME: 'poolname' -> pushMode(M_Str);
PORT_BLOCK_ALLOCATION: 'port-block-allocation';
PREFIX: 'prefix';
PRE_LOGIN_BANNER: 'pre-login-banner' -> pushMode(M_Str);
PROTOCOL: 'protocol';
IP_PROTOCOL: 'ip-protocol' {
  // ignore config ip-protocol
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
ETHERNET_TYPE: 'ethernet-type' {
  // ignore config ethernet-type
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
PROTOCOL_NUMBER: 'protocol-number';
PROXY: 'proxy';
PROXY_ADDRESS: 'proxy-address' {
  // ignore config firewall proxy-address
  if (lastTokenType() == FIREWALL && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
PROFILE_PROTOCOL_OPTIONS: 'profile-protocol-options' {
  // ignore config firewall profile-protocol-options
  if (lastTokenType() == FIREWALL && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  } else {
    pushMode(M_Str);
  }
};
REDISTRIBUTE: 'redistribute' -> pushMode(M_Str);
REDISTRIBUTE6: 'redistribute6' {
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  } else {
    pushMode(M_Str);
  }
};
REDUNDANT: 'redundant';
REMOTE_AS: 'remote-as' -> pushMode(M_Str);
RENAME: 'rename' -> pushMode(M_SingleStr);
REPLACEMSG: 'replacemsg';
ROLE: 'role' -> pushMode(M_Str);
REPLACEMSG_IMAGE: 'replacemsg-image'{
  // ignore config system replacemsg-image
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
REPORT: 'report' {
  // ignore config report
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
RIP: 'rip' {
  if (lastTokenType() == ROUTER && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
ROUTE_MAP: 'route-map';
ROUTE_MAP_IN: 'route-map-in' -> pushMode(M_Str);
ROUTE_MAP_OUT: 'route-map-out' -> pushMode(M_Str);
ROUTER: 'router';
ROUTER_ID: 'router-id';
RULE: 'rule';
SCHEDULE: 'schedule' {
  // ignore config firewall schedule
  if (lastTokenType() == FIREWALL && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  } else {
    pushMode(M_Str);
  }
};
SCTP_PORTRANGE: 'sctp-portrange';
SDN: 'sdn';
SDWAN: 'sdwan' {
  // ignore config system sdwan
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
SECONDARY_IP: 'secondary-IP';
SECONDARYIP: 'secondaryip';
SELECT: 'select';
SERVICE:
  'service'
  {
    // After `firewall service`, we expect keywords, not strings
    if (lastTokenType() != FIREWALL) {
      pushMode(M_Str);
    }
  }
;
SESSION_HELPER: 'session-helper' {
  // ignore config system session-helper
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
SHAPER: 'shaper' {
  // ignore config firewall shaper
  if (lastTokenType() == FIREWALL && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
SET: 'set';
SNMP: 'snmp' {
  // ignore config system snmp
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
SNMP_INDEX: 'snmp-index';
SPAM: 'spam' {
  if (lastTokenType() == REPLACEMSG) {
    pushMode(M_Str);
  }
};
SPEED: 'speed';
SPLIT_PORT_MODE: 'split-port-mode' {
  // ignore config split-port-mode
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
SRC_CHECK: 'src-check' -> pushMode(M_Str);
SRCADDR: 'srcaddr' -> pushMode(M_Str);
SRCINTF: 'srcintf' -> pushMode(M_Str);
STANDALONE_CLUSTER: 'standalone-cluster' {
  // ignore config system standalone-cluster
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
SSH: 'ssh' {
  // ignore config firewall ssh
  if (lastTokenType() == FIREWALL && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
SSLVPN: 'sslvpn' {
  if (lastTokenType() == REPLACEMSG) {
    pushMode(M_Str);
  }
};
SSL_SSH_PROFILE: 'ssl-ssh-profile' {
  // ignore config firewall ssl-ssh-profile
  if (lastTokenType() == FIREWALL && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  } else {
    pushMode(M_Str);
  }
};
SSO_ADMIN: 'sso-admin' {
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    // ignore config system sso-admin
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
START_IP: 'start-ip';
STARTIP: 'startip';
STATIC: 'static';
STATUS: 'status';
STORAGE: 'storage' {
  // ignore config system storage
  if (lastTokenType() == SYSTEM && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
SUBNET: 'subnet';
SUB_TYPE: 'sub-type' -> pushMode(M_Str);
SWITCH_CONTROLLER: 'switch-controller' {
  // ignore config switch-controller
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
SYSTEM: 'system';
TAGGING: 'tagging';
TCP_PORTRANGE: 'tcp-portrange';
TCP_UDP_SCTP: 'TCP/UDP/SCTP';
TO: 'to' -> pushMode(M_SingleStr);
TCP_HALFCLOSE_TIMER: 'tcp-halfclose-timer' -> pushMode(M_Str);
TIMEZONE: 'timezone';
TRAFFIC_QUOTA: 'traffic-quota' {
  if (lastTokenType() == REPLACEMSG) {
    pushMode(M_Str);
  }
};
TUNNEL: 'tunnel';
TYPE: 'type';
UDP_PORTRANGE: 'udp-portrange';
UNSELECT: 'unselect';
UDP_IDLE_TIMER: 'udp-idle-timer' -> pushMode(M_Str);
ULL_PORT_MODE: 'ull-port-mode' -> pushMode(M_Str);
UNSET: 'unset';
USER: 'user' {
  // ignore config user
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
UP: 'up';
UPDATE_SOURCE: 'update-source' -> pushMode(M_Str);
UTM: [uU][tT][mM] {
  if (lastTokenType() == REPLACEMSG) {
    pushMode(M_Str);
  }
};
UTM_STATUS: 'utm-status';
UUID: 'uuid' -> pushMode(M_Str);
VDOM: 'vdom' -> pushMode(M_Str);
VIRTUAL_PATCH: 'virtual-patch' {
  // ignore config virtual-patch
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
VIDEO_FILTER: 'video-filter' {
  // ignore config video-filter
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
VISIBILITY: 'visibility';
VLAN: 'vlan';
VLANID: 'vlanid';
VOIP: 'voip' {
  // ignore config voip
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
VPN: 'vpn' {
  // ignore config vpn
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
VRF: 'vrf';
WAF: 'waf' {
  // ignore config waf
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
WANOPT: 'wanopt' {
  // ignore config wanopt
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
WEBPROXY: 'webproxy' {
  if (lastTokenType() == REPLACEMSG) {
    pushMode(M_Str);
  }
};
WEB_PROXY: 'web-proxy' {
  // ignore config web-proxy
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
WEBFILTER: 'webfilter' {
  // ignore config webfilter
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
WEBFILTER_PROFILE: 'webfilter-profile' -> pushMode(M_Str);
WILDCARD: 'wildcard';
WILDCARD_FQDN: 'wildcard-fqdn' {
  // ignore config firewall wildcard-fqdn
  if (lastTokenType() == FIREWALL && secondToLastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
WIRELESS_CONTROLLER: 'wireless-controller' {
  if (lastTokenType() == CONFIG) {
    setType(IGNORED_CONFIG_BLOCK);
    pushMode(M_IgnoredConfigBlock);
  }
};
WL_MESH: 'wl-mesh';
ZONE: 'zone';

// Numeric tokens
TEN_FULL: '10full';
TEN_HALF: '10half';
HUNDRED_FULL: '100full';
HUNDRED_HALF: '100half';
HUNDRED_AUTO: '100auto';
THOUSAND_FULL: '1000full';
THOUSAND_HALF: '1000half';
THOUSAND_AUTO: '1000auto';
TEN_THOUSAND_FULL: '10000full';
TEN_THOUSAND_HALF: '10000half';
TEN_THOUSAND_AUTO: '10000auto';
TWENTY_FIVE_THOUSAND_FULL: '25000full';
TWENTY_FIVE_THOUSAND_AUTO: '25000auto';
TWO_THOUSAND_FIVE_HUNDRED_AUTO: '2500auto';
FORTY_THOUSAND_FULL: '40000full';
FORTY_THOUSAND_AUTO: '40000auto';
FIVE_THOUSAND_AUTO: '5000auto';
FIFTY_THOUSAND_FULL: '50000full';
FIFTY_THOUSAND_AUTO: '50000auto';
HUNDRED_GFULL: '100Gfull';
HUNDRED_GAUTO: '100Gauto';
TWO_HUNDRED_GFULL: '200Gfull';
TWO_HUNDRED_GAUTO: '200Gauto';
FOUR_HUNDRED_G_FULL: '400Gfull';
FOUR_HUNDRED_G_AUTO: '400Gauto';
HUNDRED_GHALF: '100Ghalf';

// Other Tokens

COLON: ':';

HYPHEN: '-';

COMMENT_LINE
:
  F_Whitespace* '#'
  {lastTokenType() == NEWLINE || lastTokenType() == -1}?
  F_NonNewline* (F_Newline | EOF) -> channel(HIDDEN)
;

DOUBLE_QUOTE: '"' -> pushMode(M_DoubleQuote);

SUBNET_MASK
:
  F_SubnetMask
;

IP_ADDRESS
:
  F_IpAddress
;

IP_PREFIX
:
  F_IpPrefix
;

IPV6_ADDRESS
:
  F_Ipv6Address
;

IPV6_PREFIX
:
  F_Ipv6Prefix
;

MAC_ADDRESS_LITERAL
:
  F_MacAddress
;

NEWLINE
:
  F_Newline
;

SINGLE_QUOTE: ['] -> pushMode(M_SingleQuote);

UINT8
:
  F_Uint8
;

UINT16
:
  F_Uint16
;

UINT32
:
  F_Uint32
;

WS
:
  F_Whitespace+ -> channel ( HIDDEN )
;

// Fragments

fragment
F_Digit
:
  [0-9]
;

fragment
F_HexDigit
:
  [0-9A-Fa-f]
;

fragment
F_HexUint32
:
  '0x' F_HexDigit F_HexDigit? F_HexDigit? F_HexDigit? F_HexDigit? F_HexDigit? F_HexDigit? F_HexDigit?
;

fragment
F_IpAddress
:
  F_Uint8 '.' F_Uint8 '.' F_Uint8 '.' F_Uint8
;

fragment
F_IpPrefix
:
  F_IpAddress '/' F_IpPrefixLength
;

fragment
F_IpPrefixLength
:
  F_Digit
  | [12] F_Digit
  | [3] [012]
;

fragment
F_Ipv6Address
:
  '::' F_Ipv6HexWordLE7
  | F_Ipv6HexWord '::' F_Ipv6HexWordLE6
  | F_Ipv6HexWord2 '::' F_Ipv6HexWordLE5
  | F_Ipv6HexWord3 '::' F_Ipv6HexWordLE4
  | F_Ipv6HexWord4 '::' F_Ipv6HexWordLE3
  | F_Ipv6HexWord5 '::' F_Ipv6HexWordLE2
  | F_Ipv6HexWord6 '::' F_Ipv6HexWordLE1
  | F_Ipv6HexWord7 '::'
  | F_Ipv6HexWord8
;

fragment
F_Ipv6HexWord
:
  F_HexDigit F_HexDigit? F_HexDigit? F_HexDigit?
;

fragment
F_Ipv6HexWord2
:
  F_Ipv6HexWord ':' F_Ipv6HexWord
;

fragment
F_Ipv6HexWord3
:
  F_Ipv6HexWord2 ':' F_Ipv6HexWord
;

fragment
F_Ipv6HexWord4
:
  F_Ipv6HexWord3 ':' F_Ipv6HexWord
;

fragment
F_Ipv6HexWord5
:
  F_Ipv6HexWord4 ':' F_Ipv6HexWord
;

fragment
F_Ipv6HexWord6
:
  F_Ipv6HexWord5 ':' F_Ipv6HexWord
;

fragment
F_Ipv6HexWord7
:
  F_Ipv6HexWord6 ':' F_Ipv6HexWord
;

fragment
F_Ipv6HexWord8
:
  F_Ipv6HexWord6 ':' F_Ipv6HexWordFinal2
;

fragment
F_Ipv6HexWordFinal2
:
  F_Ipv6HexWord2
  | F_IpAddress
;

fragment
F_Ipv6HexWordFinal3
:
  F_Ipv6HexWord ':' F_Ipv6HexWordFinal2
;

fragment
F_Ipv6HexWordFinal4
:
  F_Ipv6HexWord ':' F_Ipv6HexWordFinal3
;

fragment
F_Ipv6HexWordFinal5
:
  F_Ipv6HexWord ':' F_Ipv6HexWordFinal4
;

fragment
F_Ipv6HexWordFinal6
:
  F_Ipv6HexWord ':' F_Ipv6HexWordFinal5
;

fragment
F_Ipv6HexWordFinal7
:
  F_Ipv6HexWord ':' F_Ipv6HexWordFinal6
;

fragment
F_Ipv6HexWordLE1
:
  F_Ipv6HexWord?
;

fragment
F_Ipv6HexWordLE2
:
  F_Ipv6HexWordLE1
  | F_Ipv6HexWordFinal2
;

fragment
F_Ipv6HexWordLE3
:
  F_Ipv6HexWordLE2
  | F_Ipv6HexWordFinal3
;

fragment
F_Ipv6HexWordLE4
:
  F_Ipv6HexWordLE3
  | F_Ipv6HexWordFinal4
;

fragment
F_Ipv6HexWordLE5
:
  F_Ipv6HexWordLE4
  | F_Ipv6HexWordFinal5
;

fragment
F_Ipv6HexWordLE6
:
  F_Ipv6HexWordLE5
  | F_Ipv6HexWordFinal6
;

fragment
F_Ipv6HexWordLE7
:
  F_Ipv6HexWordLE6
  | F_Ipv6HexWordFinal7
;

fragment
F_Ipv6Prefix
:
  F_Ipv6Address '/' F_Ipv6PrefixLength
;

fragment
F_Ipv6PrefixLength
:
  F_Digit
  | F_PositiveDigit F_Digit
  | '1' [01] F_Digit
  | '12' [0-8]
;

fragment
F_MacAddress
:
  F_HexDigit F_HexDigit F_HexDigit F_HexDigit '.'
  F_HexDigit F_HexDigit F_HexDigit F_HexDigit '.'
  F_HexDigit F_HexDigit F_HexDigit F_HexDigit
;

// Any number of newlines, allowing whitespace in between
fragment
F_Newline
:
  F_NewlineChar (F_Whitespace* F_NewlineChar+)*
;

// A single newline character [sequence - allowing \r, \r\n, or \n]
fragment
F_NewlineChar
:
  '\r' '\n'?
  | '\n'
;

fragment
F_NonNewline
:
  ~[\n\r]
;

fragment
F_NonWhitespace
:
  ~[ \t\u000C\u00A0\n\r]
;

fragment
F_PositiveDigit
:
  [1-9]
;

fragment
F_StandardCommunity
:
  F_Uint16 ':' F_Uint16
;

fragment
F_SubnetMask
:
  F_SubnetMaskOctet '.0.0.0'
  | '255.' F_SubnetMaskOctet '.0.0'
  | '255.255.' F_SubnetMaskOctet '.0'
  | '255.255.255.' F_SubnetMaskOctet
;

fragment
F_SubnetMaskOctet
:
  '0'
  | '128'
  | '192'
  | '224'
  | '240'
  | '248'
  | '252'
  | '254'
  | '255'
;

fragment
F_Uint8
:
  F_Digit
  | F_PositiveDigit F_Digit
  | '1' F_Digit F_Digit
  | '2' [0-4] F_Digit
  | '25' [0-5]
;

fragment
F_Uint16
:
  F_Digit
  | F_PositiveDigit F_Digit F_Digit? F_Digit?
  | [1-5] F_Digit F_Digit F_Digit F_Digit
  | '6' [0-4] F_Digit F_Digit F_Digit
  | '65' [0-4] F_Digit F_Digit
  | '655' [0-2] F_Digit
  | '6553' [0-5]
;

fragment
F_Uint32
:
// 0-4294967295
  F_Digit
  | F_PositiveDigit F_Digit F_Digit? F_Digit? F_Digit? F_Digit? F_Digit?
  F_Digit? F_Digit?
  | [1-3] F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit
  F_Digit
  | '4' [0-1] F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit
  | '42' [0-8] F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit
  | '429' [0-3] F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit
  | '4294' [0-8] F_Digit F_Digit F_Digit F_Digit F_Digit
  | '42949' [0-5] F_Digit F_Digit F_Digit F_Digit
  | '429496' [0-6] F_Digit F_Digit F_Digit
  | '4294967' [0-1] F_Digit F_Digit
  | '42949672' [0-8] F_Digit
  | '429496729' [0-5]
;

fragment
F_Whitespace
:
  ' '
  | '\t'
  | '\u000C'
  | '\u00A0'
;

fragment
F_LineContinuation: '\\' F_Newline;

fragment
F_UnquotedEscapedChar: '\\' ~[\n];

fragment
F_QuotedEscapedChar: '\\' ["'\\];

fragment
F_WordChar: ~[ \t\u000C\u00A0\r\n#()<>?'"\\];

mode M_DoubleQuote;

M_DoubleQuote_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), popMode;

M_DoubleQuote_QUOTED_TEXT: (F_QuotedEscapedChar | ~'"')+ -> type(QUOTED_TEXT);

mode M_SingleQuote;

M_SingleQuote_SINGLE_QUOTE: ['] -> type(SINGLE_QUOTE), popMode;

M_SingleQuote_QUOTED_TEXT: ~[']+ -> type(QUOTED_TEXT);

mode M_Str;

M_Str_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), pushMode(M_DoubleQuote);

M_Str_SINGLE_QUOTE: ['] -> type(SINGLE_QUOTE), pushMode(M_SingleQuote);

M_Str_LINE_CONTINUATION: F_LineContinuation -> skip;

M_Str_UNQUOTED_WORD_CHARS: (F_WordChar | F_UnquotedEscapedChar)+ -> type(UNQUOTED_WORD_CHARS);

M_Str_WS: F_Whitespace+ -> type(STR_SEPARATOR);

M_Str_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_SingleStr;

M_SingleStr_WS: F_Whitespace+ -> type(STR_SEPARATOR), mode(M_SingleStrValue);

M_SingleStr_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_SingleStrValue;

M_SingleStrValue_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), pushMode(M_DoubleQuote);

M_SingleStrValue_SINGLE_QUOTE: ['] -> type(SINGLE_QUOTE), pushMode(M_SingleQuote);

M_SingleStrValue_LINE_CONTINUATION: F_LineContinuation -> skip;

M_SingleStrValue_UNQUOTED_WORD_CHARS: (F_WordChar | F_UnquotedEscapedChar)+ -> type(UNQUOTED_WORD_CHARS);

M_SingleStrValue_WS: F_Whitespace+ -> skip, popMode;

M_SingleStrValue_NEWLINE: F_Newline -> type(NEWLINE), popMode;


/////////////////////////////////////////////
/// Mode to ignore an entire config block ///
/////////////////////////////////////////////

// This is the entrance, after `config <something we want to ignore> [maybe more]`. Eat rest of line,
// then start ignoring inner lines.
mode M_IgnoredConfigBlock;

M_IgnoredConfigBlock_REST_OF_LINE: F_NonNewline* F_Newline -> more, mode(M_IgnoredConfigBlockInner);

// We are on some line inside an ignored config block. Eat lines, push if we hit an inner stanza.
mode M_IgnoredConfigBlockInner;

M_IgnoredConfigBlockInner_CONFIG: 'config' F_NonNewline* F_Newline -> more, pushMode(M_IgnoredInteriorConfigBlockInner);

M_IgnoredConfigBlockInner_EDIT: 'edit' F_NonNewline* F_Newline -> more, pushMode(M_IgnoredEditBlock);

M_IgnoredConfigBlockInner_SINGLE_LINE: ('set' | 'unset') F_NonNewline* F_Newline -> more;

M_IgnoredConfigBlockInner_END: 'end' -> type(END), popMode;

M_IgnoredConfigBlockInner_WS: F_Whitespace+ -> more;

// This is the entrance to an edit line
mode M_IgnoredEditBlock;

M_IgnoredEditBlock_CONFIG: 'config' F_NonNewline* F_Newline -> more, pushMode(M_IgnoredInteriorConfigBlockInner);

M_IgnoredEditBlock_SINGLE_LINE: ('set' | 'unset') F_NonNewline* F_Newline -> more;

M_IgnoredEditBlock_NEXT: 'next' F_Whitespace* F_Newline -> more, popMode;

M_IgnoredEditBlock_WS: F_Whitespace+ -> more;

// We are on some line inside an ignored config block (not the outermost on).
// Eat lines, push if we hit an inner stanza.
// This is the same as M_IgnoredConfigBlockInner, except that the END token is also skipped.
mode M_IgnoredInteriorConfigBlockInner;

M_IgnoredInteriorConfigBlockInner_CONFIG: 'config' F_NonNewline* F_Newline -> more, pushMode(M_IgnoredInteriorConfigBlockInner);

M_IgnoredInteriorConfigBlockInner_EDIT: 'edit' F_NonNewline* F_Newline -> more, pushMode(M_IgnoredEditBlock);

M_IgnoredInteriorConfigBlockInner_SINGLE_LINE: ('set' | 'unset') F_NonNewline* F_Newline -> more;

M_IgnoredInteriorConfigBlockInner_END: 'end' F_Whitespace* F_Newline -> more, popMode;

M_IgnoredInteriorConfigBlockInner_WS: F_Whitespace+ -> more;
