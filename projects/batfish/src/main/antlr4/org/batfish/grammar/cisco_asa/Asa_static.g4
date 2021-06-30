parser grammar Asa_static;

import Asa_common;

options {
   tokenVocab = AsaLexer;
}

s_route
:
   ROUTE
   iface = variable
   destination = IP_ADDRESS mask = IP_ADDRESS
   gateway = IP_ADDRESS? // if missing, iface must be Null0
   (distance = protocol_distance)?
   (
     TUNNELED
     | TRACK track = dec
   )?
   NEWLINE
;