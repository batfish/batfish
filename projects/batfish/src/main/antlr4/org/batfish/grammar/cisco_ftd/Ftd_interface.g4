parser grammar Ftd_interface;

options {
   tokenVocab = FtdLexer;
}

interface_stanza
:
   INTERFACE name = interface_name NEWLINE
   interface_stanza_tail*
;

interface_name
:
   (
      GIGABIT_ETHERNET dec FORWARD_SLASH dec (FORWARD_SLASH dec)?
      | ETHERNET dec FORWARD_SLASH dec (FORWARD_SLASH dec)?
      | PORT_CHANNEL dec PERIOD dec
      | PORT_CHANNEL dec
      | MANAGEMENT dec FORWARD_SLASH dec (FORWARD_SLASH dec)?
      // Handle NAME tokens for interface names with hyphens that include numbers
      // e.g., "Port-channel1" matches as NAME because hyphen is a special char
      // NAME PERIOD dec must come BEFORE NAME so subinterfaces are matched
      | NAME PERIOD dec
      | NAME
      | WORD
   )
;

interface_stanza_tail
:
   (
      if_description
      | if_ip_address
      | if_mac_address_null
      | if_nameif
      | if_no_nameif_null
      | if_security_level
      | if_no_security_level_null
      | if_no_ip_address_null
      | if_shutdown
      | if_no_shutdown
      | if_vlan
      | if_vrf
      | if_cts
      | if_management_only
      // Use interface_unrecognized_line instead of unrecognized_line to avoid
      // consuming lines that start new stanzas (like INTERFACE)
      | interface_unrecognized_line
   )
;

// Like unrecognized_line but doesn't match lines starting with stanza keywords
interface_unrecognized_line
:
   ~(NEWLINE | INTERFACE | ACCESS_LIST | ACCESS_GROUP | HOSTNAME | OBJECT | OBJECT_GROUP | ROUTE | NAT | FAILOVER | CRYPTO | ROUTER)
   ~NEWLINE* NEWLINE
;

if_description
:
   DESCRIPTION description_line
;

if_ip_address
:
   NO? IP ADDRESS
   (
      ip = IP_ADDRESS mask = IP_ADDRESS
      (
         STANDBY standby_ip = IP_ADDRESS
      )?
   )
   NEWLINE
;

if_mac_address_null
:
   MAC_ADDRESS mac = ~NEWLINE+
   (
      STANDBY standby_mac = ~NEWLINE+
   )?
   NEWLINE
;

if_nameif
:
   NAMEIF (name_parts += ~NEWLINE)+ NEWLINE
;

if_no_nameif_null
:
   NO NAMEIF NEWLINE
;

if_security_level
:
   SECURITY_LEVEL level = dec NEWLINE
;

if_no_security_level_null
:
   NO SECURITY_LEVEL NEWLINE
;

if_no_ip_address_null
:
   NO IP ADDRESS NEWLINE
;

if_shutdown
:
   SHUTDOWN NEWLINE
;

if_no_shutdown
:
   NO SHUTDOWN NEWLINE
;

if_vlan
:
   VLAN vlan_id = dec NEWLINE
;

if_cts
:
   CTS MANUAL NEWLINE
   if_cts_tail*
;

if_cts_tail
:
   (
      PROPAGATE SGT PRESERVE_UNTAG NEWLINE
      | POLICY STATIC SGT DISABLED TRUSTED NEWLINE
      | interface_unrecognized_line
   )
;

if_management_only
:
   MANAGEMENT_ONLY NEWLINE
;

if_vrf
:
   VRF name = vrf_name NEWLINE
;

vrf_name
:
   NAME
   | WORD
;
