parser grammar Cisco_sla;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

ip_sla_null
:
   NO?
   (
      FREQUENCY
      | HISTORY
      | HOPS_OF_STATISTICS_KEPT
      | ICMP_ECHO
      | OWNER
      | PATH_ECHO
      | PATHS_OF_STATISTICS_KEPT
      | REQUEST_DATA_SIZE
      | SAMPLES_OF_HISTORY_KEPT
      | TAG
      | THRESHOLD
      | TIMEOUT
      | TOS
      | UDP_JITTER
      | VRF
   ) null_rest_of_line
;


s_ip_sla
:
   NO? IP SLA null_rest_of_line
   (
      ip_sla_null
   )*
;
