parser grammar Rtx_interface;

ip_interface
:
  iface = INTERFACE_NAME
  (
    ipi_address
    | ipi_nat
    | ipi_secure_filter
    | ipi_vrrp
  )
;

ipi_address
:
  ADDRESS addr = IPV4_PREFIX NEWLINE
;

// `ip <if> nat descriptor <n>... [reverse <n>...]`
//
// Packets crossing the interface are translated by each listed descriptor in order. Descriptors
// after `reverse` translate the opposite direction's addresses and ports.
ipi_nat
:
  NAT DESCRIPTOR nums += UINT+ ( REVERSE reverse += UINT+ )? NEWLINE
;

// `ip <if> secure filter <in|out> <num>... [dynamic <num>...]`
//
// The listed filters are evaluated in order, first match wins, and packets matching none of them
// are discarded. That trailing implicit deny is part of the command's semantics.
ipi_secure_filter
:
  SECURE FILTER direction = secure_filter_direction nums += UINT*
  ( DYNAMIC IGNORED_TEXT* )? NEWLINE
;

secure_filter_direction
:
  IN
  | OUT
;

ipi_vrrp
:
  VRRP
  (
    ipiv_group
    | ipiv_shutdown_null
  )
;

ipiv_group
:
  vrid = UINT vip = IPV4_ADDRESS params += vrrp_param* NEWLINE
;

vrrp_param
:
  VRRP_ADVERTISE_INTERVAL
  | VRRP_AUTH
  | VRRP_DOWN_INTERVAL
  | VRRP_PREEMPT
  | VRRP_PRIORITY
;

// `ip <if> vrrp shutdown trigger ...` makes the router leave the VRRP group when a tracked
// interface goes down. Batfish's static analysis treats every interface as up, so tracking has no
// effect on the result.
ipiv_shutdown_null
:
  SHUTDOWN IGNORED_TEXT* NEWLINE
;
