parser grammar FlatJuniper_mpls;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

c_interface_switch
:
   INTERFACE_SWITCH name = junos_name
   (
      ci_interface
   )
;

ci_interface
:
   INTERFACE interface_id
;

p_connections
:
   CONNECTIONS
   (
      c_interface_switch
   )
;

p_mpls
:
   MPLS
   (
       mpls_admin_groups
       | mpls_interface
       | mpls_label_switched_path
   )
;

mpls_interface
:
   INTERFACE name = interface_id
   (
      apply
      | mplsi_admin_group
      | mplsi_srlg
   )
;

mplsi_admin_group
:
   ADMIN_GROUP name = junos_name
;

mplsi_srlg
:
   SRLG name = junos_name
;

mpls_admin_groups
:
   // Admin-group values should be in the range 0-31
   ADMIN_GROUPS name = junos_name number = uint8
;

mpls_label_switched_path
:
   LABEL_SWITCHED_PATH name = junos_name
   (
      apply
      | mplslsp_admin_group
      | mplslsp_secondary
   )
;

mplslsp_admin_group
:
   ADMIN_GROUP
   (
      mplslspag_exclude
      | mplslspag_include_all
      | mplslspag_include_any
   )
;

mplslspag_exclude
:
   EXCLUDE name = junos_name
;

mplslspag_include_all
:
   INCLUDE_ALL name = junos_name
;

mplslspag_include_any
:
   INCLUDE_ANY name = junos_name
;

mplslsp_secondary
:
   SECONDARY name = junos_name
   (
      apply
      | mplslsps_admin_group
   )
;

mplslsps_admin_group
:
   ADMIN_GROUP
   (
      mplslspsag_exclude
      | mplslspsag_include_all
      | mplslspsag_include_any
   )
;

mplslspsag_exclude
:
   EXCLUDE name = junos_name
;

mplslspsag_include_all
:
   INCLUDE_ALL name = junos_name
;

mplslspsag_include_any
:
   INCLUDE_ANY name = junos_name
;