parser grammar Mrv_system;

import Mrv_common;

options {
   tokenVocab = MrvLexer;
}

a_system
:
   SYSTEM PERIOD
   (
      a_system_configversion
      | a_system_dns1
      | a_system_dns2
      | a_system_gateway1
      | a_system_gui
      | a_system_notiffacility
      | a_system_notifpriority
      | a_system_notifyaddressname
      | a_system_notifyaddressservice
      | a_system_notifyaddressstate
      | a_system_notifyservicename
      | a_system_notifyserviceprotocol
      | a_system_notifyserviceraw
      | a_system_ntp
      | a_system_ntpaddress
      | a_system_ntpaltaddress
      | a_system_ntpsourceinterface
      | a_system_radprimacctsecret
      | a_system_radprimsecret
      | a_system_radsecacctsecret
      | a_system_radsecsecret
      | a_system_snmp
      | a_system_snmpgetclient
      | a_system_snmpgetcommunity
      | a_system_snmpsourceinterface
      | a_system_snmptrapclient
      | a_system_snmptrapcommunity
      | a_system_ssh
      | a_system_systemname
      | a_system_tacplusprimaddr
      | a_system_tacplusprimacctsecret
      | a_system_tacplusprimauthorsecret
      | a_system_tacplusprimsecret
      | a_system_tacplussecaddr
      | a_system_tacplussecacctsecret
      | a_system_tacplussecauthorsecret
      | a_system_tacplussecsecret
      | a_system_tacplususesub
      | a_system_telnet
      | a_system_telnetclient
   )
;

a_system_configversion
:
   CONFIGVERSION nidecl
;

a_system_dns1
:
   DNS1 nipdecl
;

a_system_dns2
:
   DNS2 nipdecl
;

a_system_gateway1
:
   GATEWAY1 nipdecl
;

a_system_gui
:
   GUI nbdecl
;

a_system_notiffacility
:
   NOTIFFACILITY nfdecl
;

a_system_notifpriority
:
   NOTIFPRIORITY nprdecl
;

a_system_notifyaddressname
:
   NOTIFYADDRESSNAME nsdecl
;

a_system_notifyaddressservice
:
   NOTIFYADDRESSSERVICE nidecl
;

a_system_notifyaddressstate
:
   NOTIFYADDRESSSTATE nbdecl
;

a_system_notifyservicename
:
   NOTIFYSERVICENAME nsdecl
;

a_system_notifyserviceprotocol
:
   NOTIFYSERVICEPROTOCOL nidecl
;

a_system_notifyserviceraw
:
   NOTIFYSERVICERAW nosdecl
;

a_system_ntp
:
   NTP nbdecl
;

a_system_ntpaddress
:
   NTPADDRESS nipdecl
;

a_system_ntpaltaddress
:
   NTPALTADDRESS nipdecl
;

a_system_ntpsourceinterface
:
   NTPSOURCEINTERFACE nidecl
;

a_system_radprimacctsecret
:
   RADPRIMACCTSECRET npdecl
;

a_system_radprimsecret
:
   RADPRIMSECRET npdecl
;

a_system_radsecacctsecret
:
   RADSECACCTSECRET npdecl
;

a_system_radsecsecret
:
   RADSECSECRET npdecl
;

a_system_snmp
:
   SNMP nbdecl
;

a_system_snmpgetclient
:
   SNMPGETCLIENT nipdecl
;

a_system_snmpgetcommunity
:
   SNMPGETCOMMUNITY nsdecl
;

a_system_snmpsourceinterface
:
   SNMPSOURCEINTERFACE nidecl
;

a_system_snmptrapclient
:
   SNMPTRAPCLIENT nipdecl
;

a_system_snmptrapcommunity
:
   SNMPTRAPCOMMUNITY nsdecl
;

a_system_ssh
:
   SSH nbdecl
;

a_system_systemname
:
   SYSTEMNAME nsdecl
;

a_system_tacplusprimaddr
:
   TACPLUSPRIMADDR nipdecl
;

a_system_tacplusprimacctsecret
:
   TACPLUSPRIMACCTSECRET npdecl
;

a_system_tacplusprimauthorsecret
:
   TACPLUSPRIMAUTHORSECRET npdecl
;

a_system_tacplusprimsecret
:
   TACPLUSPRIMSECRET npdecl
;

a_system_tacplussecaddr
:
   TACPLUSSECADDR nipdecl
;

a_system_tacplussecacctsecret
:
   TACPLUSSECACCTSECRET npdecl
;

a_system_tacplussecauthorsecret
:
   TACPLUSSECAUTHORSECRET npdecl
;

a_system_tacplussecsecret
:
   TACPLUSSECSECRET npdecl
;

a_system_tacplususesub
:
   TACPLUSUSESUB nbdecl
;

a_system_telnet
:
   TELNET nbdecl
;

a_system_telnetclient
:
   TELNETCLIENT nbdecl
;
