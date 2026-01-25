parser grammar Ftd_tunnel_group;

options {
   tokenVocab = FtdLexer;
}

tunnel_group_stanza
:
   TUNNEL_GROUP (name_parts += ~(NEWLINE | TYPE | IPSEC_ATTRIBUTES))+
   (
      TYPE (IPSEC_L2L | REMOTE_ACCESS) NEWLINE
      | tunnel_group_ipsec_attributes
      | null_rest_of_line
   )
;

tunnel_group_ipsec_attributes
:
   IPSEC_ATTRIBUTES NEWLINE
   (
      tg_ikev2_attr
      | stanza_unrecognized_line
   )*
;

tg_ikev2_attr
:
   IKEV2
   (
      (REMOTE_AUTHENTICATION | LOCAL_AUTHENTICATION) PRE_SHARED_KEY (key_parts += ~NEWLINE)+
      | null_rest_of_line
   ) NEWLINE
;
