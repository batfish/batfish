lexer grammar MrvLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

tokens {
   QUOTED_TEXT
}

// Mrv Keywords

ACCESS: 'Access';

ASYNC: 'Async';

AUTHTYPE: 'AuthType';

AUTOHANG: 'AutoHang';

BANNER: 'Banner';

BONDDEVS: 'BondDevs';

BONDMIIMON: 'BondMiimon';

BONDMODE: 'BondMode';

CONFIGVERSION: 'ConfigVersion';

DESPASSWORD: 'DesPassword';

DHCP: 'Dhcp';

DNS1: 'Dns1';

DNS2: 'Dns2';

DSRWAIT: 'DSRWait';

FLOWCONT: 'FlowCont';

GATEWAY1: 'Gateway1';

GUI: 'Gui';

GUIMENUNAME: 'GUIMenuName';

IDLETIMEOUT: 'IdleTimeout';

IFNAME: 'ifName';

INTERFACE: 'Interface';

IPADDRESS: 'IpAddress';

IPBROADCAST: 'IpBroadcast';

IPMASK: 'IpMask';

LX
:
   'LX' F_NonNewline* F_Newline -> channel ( HIDDEN )
;

MAXCONNECTIONS: 'MaxConnections';

MAXSUBS: 'MaxSubs';

MENUNAME: 'MenuName';

NAME: 'Name';

NOTIFFACILITY: 'NotifFacility';

NOTIFPRIORITY: 'NotifPriority';

NOTIFYADDRESSNAME: 'NotifyAddressName';

NOTIFYADDRESSSERVICE: 'NotifyAddressService';

NOTIFYADDRESSSTATE: 'NotifyAddressState';

NOTIFYSERVICENAME: 'NotifyServiceName';

NOTIFYSERVICEPROTOCOL: 'NotifyServiceProtocol';

NOTIFYSERVICERAW: 'NotifyServiceRaw';

NTP: 'Ntp';

NTPADDRESS: 'NtpAddress';

NTPALTADDRESS: 'NtpAltAddress';

NTPSOURCEINTERFACE: 'NtpSourceInterface';

OUTAUTHTYPE: 'OutAuthType';

PROMPT: 'Prompt';

RADPRIMACCTSECRET: 'RadPrimAcctSecret';

RADPRIMSECRET: 'RadPrimSecret';

RADSECACCTSECRET: 'RadSecAcctSecret';

RADSECSECRET: 'RadSecSecret';

REMOTEACCESSLIST: 'RemoteAccessList';

SECURITYV3: 'SecurityV3';

SHAPASSWORD: 'ShaPassword';

SIGNATURE
:
   'Signature' F_NonNewline* F_Newline -> channel ( HIDDEN )
;

SNMP: 'Snmp';

SNMPGETCLIENT: 'SnmpGetClient';

SNMPGETCOMMUNITY: 'SnmpGetCommunity';

SNMPSOURCEINTERFACE: 'SnmpSourceInterface';

SNMPTRAPCLIENT: 'SnmpTrapClient';

SNMPTRAPCOMMUNITY: 'SnmpTrapCommunity';

SPEED: 'Speed';

SSH: 'SSH';

SSHPORTLIST: 'SshPortList';

STAT: 'Stat';

SUBSCRIBER: 'Subscriber';

SUBSTAT: 'SubStat';

SUBTEMPLATE: 'SubTemplate';

SUPERPASSWORD: 'SuperPassword';

SYSTEM: 'System';

SYSTEMNAME: 'SystemName';

T_BOOL: 'BOOL';

T_FACILITY: 'FACILITY';

T_INTEGER: 'INTEGER';

T_IPADDR: 'IPADDR';

T_OCTET: 'OCTET';

T_OCTETSTRING: 'OCTETSTRING';

T_PASSWORD: 'PASSWORD';

T_PRIORITY: 'PRIORITY';

T_SHORT: 'SHORT';

T_SHORTSTRING: 'SHORTSTRING';

T_SPEED: 'SPEED';

T_STRING: 'STRING';

TACPLUSPRIMADDR: 'TacPlusPrimAddr';

TACPLUSPRIMACCTSECRET: 'TacPlusPrimAcctSecret';

TACPLUSPRIMAUTHORSECRET: 'TacPlusPrimAuthorSecret';

TACPLUSPRIMSECRET: 'TacPlusPrimSecret';

TACPLUSSECADDR: 'TacPlusSecAddr';

TACPLUSSECACCTSECRET: 'TacPlusSecAcctSecret';

TACPLUSSECAUTHORSECRET: 'TacPlusSecAuthorSecret';

TACPLUSSECSECRET: 'TacPlusSecSecret';

TACPLUSUSESUB: 'TacPlusUseSub';

TELNET: 'Telnet';

TELNETCLIENT: 'TelnetClient';

TYPE: 'TYPE';

VALUE: 'VALUE';

// Other tokens

DEC
:
   F_Digit+
;

DOUBLE_QUOTE
:
   '"' -> pushMode ( M_QuotedString )
;

LINE_COMMENT
:
   '!' -> pushMode ( M_LineComment ) , channel ( HIDDEN )
;

PERIOD
:
   '.'
;

WS
:
   F_Whitespace+ -> channel ( HIDDEN )
;

fragment
F_Digit
:
   '0' .. '9'
;

fragment
F_Newline
:
   [\r\n]+
;

fragment
F_NonNewline
:
   ~[\r\n]
;

fragment
F_Whitespace
:
   [ \n\t\u000C]
;

mode M_LineComment;

M_LineComment_FILLER
:
   F_NonNewline* F_Newline -> channel ( HIDDEN ) , popMode
;

mode M_QuotedString;

M_QuotedString_QUOTED_TEXT
:
   ~'"'+ -> type ( QUOTED_TEXT )
;

M_QuotedString_DOUBLE_QUOTE
:
   '"' -> type ( DOUBLE_QUOTE ) , popMode
;
