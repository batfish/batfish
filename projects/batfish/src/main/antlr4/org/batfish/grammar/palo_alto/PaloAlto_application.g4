parser grammar PaloAlto_application;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_application
:
    APPLICATION s_application_definition?
;

s_application_definition
:
    name = variable
    (
        sapp_description
        | sapp_ignored
    )?
;

sapp_description
:
    DESCRIPTION description = value
;

sapp_ignored
:
    (
    sapp_category
    | sapp_default_port
    | sapp_risk
    | sapp_subcategory
    | sapp_technology
    | sapp_tcp_timeout
    | sapp_udp_timeout
    | sapp_tcp_half_closed_timeout
    | sapp_tcp_time_wait_timeout
)
;

sapp_category
:
    CATEGORY null_rest_of_line
;

sapp_default_port
:
    DEFAULT PORT null_rest_of_line
;

sapp_risk
:
// 1-5
  RISK uint8
;

sapp_subcategory
:
  SUBCATEGORY null_rest_of_line
;

sapp_technology
:
  TECHNOLOGY null_rest_of_line
;

s_application_group
:
    APPLICATION_GROUP sappg_definition?
;

sappg_definition
:
    name = variable
    (
       sappg_members
    )?
;

sappg_members
:
    MEMBERS variable_list?
;
sapp_tcp_half_closed_timeout : TCP_HALF_CLOSED_TIMEOUT uint32;
sapp_tcp_time_wait_timeout : TCP_TIME_WAIT_TIMEOUT uint32;
sapp_tcp_timeout : TCP_TIMEOUT uint32;
sapp_udp_timeout : UDP_TIMEOUT uint32;
