lexer grammar MrvLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

@header {
package org.batfish.grammar.mrv;
}

tokens {
   QUOTED_TEXT
}

// Mrv Keywords

ACCESS
:
   'Access'
;

ASYNC
:
   'Async'
;

AUTHTYPE
:
   'AuthType'
;

AUTOHANG
:
   'AutoHang'
;

BANNER
:
   'Banner'
;

BONDDEVS
:
   'BondDevs'
;

BONDMIIMON
:
   'BondMiimon'
;

BONDMODE
:
   'BondMode'
;

BOOL
:
   'BOOL'
;

CONFIGVERSION
:
   'ConfigVersion'
;

DESPASSWORD
:
   'DesPassword'
;

DHCP
:
   'Dhcp'
;

DNS1
:
   'Dns1'
;

DNS2
:
   'Dns2'
;

DSRWAIT
:
   'DSRWait'
;

FACILITY
:
   'FACILITY'
;

GATEWAY1
:
   'Gateway1'
;

GUI
:
   'Gui'
;

GUIMENUNAME
:
   'GUIMenuName'
;

IDLETIMEOUT
:
   'IdleTimeout'
;

IFNAME
:
   'ifName'
;

INTEGER
:
   'INTEGER'
;

INTERFACE
:
   'Interface'
;

IPADDR
:
   'IPADDR'
;

IPADDRESS
:
   'IpAddress'
;

IPBROADCAST
:
   'IpBroadcast'
;

IPMASK
:
   'IpMask'
;

LX
:
   'LX' F_NonNewline* F_Newline -> channel ( HIDDEN )
;

MAXSUBS
:
   'MaxSubs'
;

MENUNAME
:
   'MenuName'
;

NAME
:
   'Name'
;

NOTIFFACILITY
:
   'NotifFacility'
;

NOTIFPRIORITY
:
   'NotifPriority'
;

NOTIFYADDRESSNAME
:
   'NotifyAddressName'
;

NOTIFYADDRESSSERVICE
:
   'NotifyAddressService'
;

NOTIFYSERVICENAME
:
   'NotifyServiceName'
;

NOTIFYSERVICEPROTOCOL
:
   'NotifyServiceProtocol'
;

NOTIFYSERVICERAW
:
   'NotifyServiceRaw'
;

NTP
:
   'Ntp'
;

NTPADDRESS
:
   'NtpAddress'
;

NTPALTADDRESS
:
   'NtpAltAddress'
;

NTPSOURCEINTERFACE
:
   'NtpSourceInterface'
;

OCTET
:
   'OCTET'
;

OCTETSTRING
:
   'OCTETSTRING'
;

OUTAUTHTYPE
:
   'OutAuthType'
;

PASSWORD
:
   'PASSWORD'
;

PRIORITY
:
   'PRIORITY'
;

PROMPT
:
   'Prompt'
;

REMOTEACCESSLIST
:
   'RemoteAccessList'
;

SECURITYV3
:
   'SecurityV3'
;

SHAPASSWORD
:
   'ShaPassword'
;

SHORT
:
   'SHORT'
;

SHORTSTRING
:
   'SHORTSTRING'
;

SIGNATURE
:
   'Signature' F_NonNewline* F_Newline -> channel ( HIDDEN )
;

SNMP
:
   'Snmp'
;

SNMPGETCLIENT
:
   'SnmpGetClient'
;

SNMPGETCOMMUNITY
:
   'SnmpGetCommunity'
;

SNMPSOURCEINTERFACE
:
   'SnmpSourceInterface'
;

SNMPTRAPCLIENT
:
   'SnmpTrapClient'
;

SNMPTRAPCOMMUNITY
:
   'SnmpTrapCommunity'
;

SSH
:
   'SSH'
;

SSHPORTLIST
:
   'SshPortList'
;

STAT
:
   'Stat'
;

STRING
:
   'STRING'
;

SUBSCRIBER
:
   'Subscriber'
;

SUBSTAT
:
   'SubStat'
;

SUBTEMPLATE
:
   'SubTemplate'
;

SUPERPASSWORD
:
   'SuperPassword'
;

SYSTEM
:
   'System'
;

SYSTEMNAME
:
   'SystemName'
;

TACPLUSPRIMADDR
:
   'TacPlusPrimAddr'
;

TACPLUSPRIMSECRET
:
   'TacPlusPrimSecret'
;

TACPLUSSECADDR
:
   'TacPlusSecAddr'
;

TACPLUSSECSECRET
:
   'TacPlusSecSecret'
;

TACPLUSUSESUB
:
   'TacPlusUseSub'
;

TELNET
:
   'Telnet'
;

TELNETCLIENT
:
   'TelnetClient'
;

TYPE
:
   'TYPE'
;

VALUE
:
   'VALUE'
;

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
