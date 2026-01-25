parser grammar Ftd_failover;

options {
   tokenVocab = FtdLexer;
}

failover_stanza
:
   FAILOVER
   (
      failover_enable
      | failover_lan
      | failover_link
      | failover_polltime
      | failover_interface_ip
      | null_rest_of_line
   )
;

failover_enable
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

failover_link
:
   LINK iface_name = ~NEWLINE+ NEWLINE
;

failover_polltime
:
   POLLTIME
   (
      UNIT MSEC? msec_value = dec (HOLDTIME MSEC? holdtime_value = dec)?
      | INTERFACE MSEC? msec_value = dec (HOLDTIME holdtime_value = dec)?
   )
   NEWLINE
;

failover_interface_ip
:
   INTERFACE IP iface_name = ~(IP_ADDRESS | NEWLINE)+ ip = IP_ADDRESS mask = IP_ADDRESS STANDBY standby_ip = IP_ADDRESS NEWLINE
;
