parser grammar PaloAlto_template_stack;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

sts_description
:
    DESCRIPTION description = value
;

sts_devices
:
    DEVICES variable_list?
;

sts_templates
:
    TEMPLATES variable_list?
;
