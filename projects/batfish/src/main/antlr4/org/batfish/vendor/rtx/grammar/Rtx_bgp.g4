parser grammar Rtx_bgp;

s_bgp
:
  BGP
  (
    sb_autonomous_system
    | sb_export
    | sb_import
    | sb_neighbor
    | sb_use
  )
;

sb_use
:
  USE ( ON | OFF ) NEWLINE
;

sb_autonomous_system
:
  AUTONOMOUS_SYSTEM as = UINT NEWLINE
;

sb_neighbor
:
  NEIGHBOR
  (
    sbn_peer
    | sbn_null
  )
;

// `bgp neighbor <id> <remote-as> <remote-address>`
sbn_peer
:
  id = UINT remote_as = UINT remote_ip = IPV4_ADDRESS NEWLINE
;

// `bgp neighbor pre-shared-key <id> text <key>`
sbn_null
:
  PRE_SHARED_KEY IGNORED_TEXT* NEWLINE
;

// RTX calls routes selected from the local routing table for BGP advertisement "imported" routes.
sb_import
:
  IMPORT
  (
    sbi_filter
    | sbi_apply_bgp
    | sbi_apply_static
  )
;

sbi_filter
:
  FILTER number = UINT reject = REJECT? match_type = bgp_filter_match_type
  prefixes += IPV4_PREFIX+ NEWLINE
;

sbi_apply_bgp
:
  remote_as = UINT BGP from_as = UINT FILTER filters += UINT+ NEWLINE
;

sbi_apply_static
:
  remote_as = UINT STATIC FILTER filters += UINT+ NEWLINE
;

// RTX calls routes received from a BGP peer and installed into the local routing table "exported"
// routes.
sb_export
:
  EXPORT
  (
    sbe_filter
    | sbe_apply
  )
;

sbe_filter
:
  FILTER number = UINT reject = REJECT? match_type = bgp_filter_match_type
  prefixes += IPV4_PREFIX+ NEWLINE
;

sbe_apply
:
  remote_as = UINT FILTER filters += UINT+ NEWLINE
;

bgp_filter_match_type
:
  INCLUDE
  | REFINES
  | EQUAL
;
