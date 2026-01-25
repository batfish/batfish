parser grammar Ftd_nat;

options {
   tokenVocab = FtdLexer;
}

nat_stanza
:
   NAT LEFT_PAREN src_ifc = ~COMMA+ COMMA dst_ifc = ~RIGHT_PAREN+ RIGHT_PAREN nat_position? nat_rule NEWLINE
;

nat_position
:
   BEFORE_AUTO
   | AFTER_AUTO
;

nat_rule
:
   (
      nat_source nat_destination? nat_service?
      | nat_destination nat_source? nat_service?
   )
;

nat_source
:
   SOURCE
   (
      STATIC real = nat_address mapped = nat_address
      | DYNAMIC real = nat_address mapped = nat_address
   )
;

nat_destination
:
   DESTINATION STATIC real = nat_address mapped = nat_address
;

nat_service
:
   SERVICE protocol real_port = nat_service_port mapped_port = nat_service_port
   | SERVICE real_service = nat_service_object_name mapped_service = nat_service_object_name
;

nat_address
:
   ip = IP_ADDRESS
   | nat_object_name
;

nat_object_name
:
   ~(
      DESTINATION
      | SERVICE
      | NEWLINE
   )
;

nat_service_object_name
:
   ~(
      SERVICE
      | NEWLINE
   )
;

nat_service_port
:
   port_specifier
   | port_value
;
