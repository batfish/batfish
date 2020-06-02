parser grammar PaloAlto_device_group;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

sdg_description
:
    DESCRIPTION description = value
;

sdg_devices
:
    DEVICES device = variable
;
