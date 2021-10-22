parser grammar A10Parser;

import
  A10_common,
  A10_floating_ip,
  A10_ha,
  A10_health_monitor,
  A10_interface,
  A10_ip_nat,
  A10_ip_route,
  A10_lacp_trunk,
  A10_rba,
  A10_router_bgp,
  A10_slb_server,
  A10_slb_service_group,
  A10_slb_virtual_server,
  A10_trunk,
  A10_vlan,
  A10_vrrp_a;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = A10Lexer;
}

a10_configuration: NEWLINE? statement+ EOF;

statement
:
   s_floating_ip
   | s_health_monitor
   | s_hostname
   | s_interface
   | s_ha
   | s_ip
   | s_lacp_trunk
   | s_no
   | s_rba
   | s_router
   | s_slb
   | s_trunk
   | s_vlan
   | s_vrrp_a
;

s_ip: IP si;

si: si_nat | si_route;

s_no: NO (sn_ha | sn_ip);

sn_ip: IP sni;

sni: sni_route;

s_hostname: HOSTNAME hostname NEWLINE;

s_router: ROUTER sr_bgp;

s_slb: SLB ss;

ss: ss_server | ss_service_group | ss_virtual_server;
