parser grammar Ftd_failover;

options {
   tokenVocab = FtdLexer;
}

failover_stanza
:
   FAILOVER
   (
      failover_enable_null
      | failover_lan
      | failover_link_null
      | failover_polltime_null
      | failover_interface_ip_null
      | null_rest_of_line
   )
;

failover_enable_null
:
   NEWLINE
;

failover_lan
:
   LAN
   (
      UNIT (PRIMARY | SECONDARY) NEWLINE
      | INTERFACE iface_name = ~NEWLINE+ NEWLINE
   )
;

failover_link_null
:
   LINK iface_name = ~NEWLINE+ NEWLINE
;

failover_polltime_null
:
   POLLTIME
   (
      UNIT msec_value = dec
      | UNIT MSEC msec_value = dec
      | UNIT msec_value = dec HOLDTIME holdtime_value = dec
      | UNIT MSEC msec_value = dec HOLDTIME holdtime_value = dec
      | UNIT msec_value = dec HOLDTIME MSEC holdtime_value = dec
      | INTERFACE msec_value = dec
      | INTERFACE MSEC msec_value = dec
      | INTERFACE msec_value = dec HOLDTIME holdtime_value = dec
      | INTERFACE MSEC msec_value = dec HOLDTIME holdtime_value = dec
   )
   NEWLINE
;

failover_interface_ip_null
:
   INTERFACE IP iface_name = ~(IP_ADDRESS | NEWLINE)+ ip = IP_ADDRESS mask = IP_ADDRESS STANDBY standby_ip = IP_ADDRESS NEWLINE
;
