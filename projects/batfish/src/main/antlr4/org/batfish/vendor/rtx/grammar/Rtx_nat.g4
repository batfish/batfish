parser grammar Rtx_nat;

// `nat descriptor <sub-command> <descriptor-number> ...`
//
// A descriptor is a named bundle of NAT settings; `ip <if> nat descriptor <n>` applies it to an
// interface. The lexer has already narrowed the sub-command to one of these, sending every
// unmodeled one down snd_null (see M_NatDescriptor in RtxLexer.g4).
s_nat
:
  NAT DESCRIPTOR
  (
    snd_type
    | snd_address
    | snd_masquerade_port_range
    | snd_null
  )
;

// `nat descriptor type <n> none|nat|masquerade|nat-masquerade [hairpin=on|off]`
snd_type
:
  TYPE num = UINT type = nat_type HAIRPIN? NEWLINE
;

nat_type
:
  NONE
  | NAT
  | MASQUERADE
  | NAT_MASQUERADE
;

snd_address
:
  ADDRESS
  (
    snda_outer
    | snda_inner
  )
;

// `nat descriptor address outer <n> <address-list>|<mnemonic>`
//
// For a masquerade descriptor only the first outer address is used; for a dynamic NAT descriptor
// the whole list is a pool.
snda_outer
:
  OUTER num = UINT
  (
    items += nat_address_item+
    | mnemonic = nat_outer_mnemonic
  )
  NEWLINE
;

// `nat descriptor address inner <n> <address-list>|auto`
//
// Restricts which inner addresses are translated. `auto` means all of them.
snda_inner
:
  INNER num = UINT
  (
    items += nat_address_item+
    | AUTO
  )
  NEWLINE
;

nat_address_item
:
  low = IPV4_ADDRESS ( DASH high = IPV4_ADDRESS )?
;

// Addresses the router learns at runtime rather than from the configuration.
nat_outer_mnemonic
:
  IPCP
  | PRIMARY
  | SECONDARY
  | MAP_E
  | HB46PP
;

// `nat descriptor masquerade port range <n> <range> [<range> [<range> [<range>]]]`
//
// Ports are taken from the first range until it is exhausted, then the next, and so on.
snd_masquerade_port_range
:
  MASQUERADE_PORT_RANGE num = UINT ranges += nat_port_range+ NEWLINE
;

nat_port_range
:
  low = UINT DASH high = UINT
;

// Sub-commands that do not affect what Batfish models: `log`, `timer`, `masquerade session
// limit`, `backward-compatibility`, and the like. Static NAT entries (`nat descriptor static`)
// also land here; they are not modeled.
snd_null
:
  IGNORED_TEXT* NEWLINE
;
