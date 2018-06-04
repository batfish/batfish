parser grammar PaloAlto_deviceconfig;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

null_rest_of_line
:
    ~NEWLINE* NEWLINE
;

s_deviceconfig
:
   DEVICECONFIG
   (
      sdc_system
   )
;

sdc_system
:
   SYSTEM
   (
      sdcs_hostname
   )
;

sdcs_hostname
:
   HOSTNAME name = VARIABLE
;
