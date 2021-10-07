parser grammar A10_ip_nat;

import A10_common;

options {
   tokenVocab = A10Lexer;
}

si_nat: NAT sin;

sin: sin_pool;

sin_pool: POOL nat_pool_name start = ip_address end = ip_address NETMASK ip_netmask sinp_properties* NEWLINE;

sinp_properties
:
   (
      sinpp_gateway
      | sinpp_ha_group_id
      | sinpp_ip_rr
      | sinpp_port_overload
      | sinpp_scaleout_device_id
      | sinpp_vrid
   )
;

sinpp_gateway: GATEWAY gateway = ip_address;

sinpp_ha_group_id: HA_GROUP_ID ha_group_id;

sinpp_ip_rr: IP_RR;

sinpp_port_overload: PORT_OVERLOAD;

sinpp_scaleout_device_id: SCALEOUT_DEVICE_ID scaleout_device_id;

sinpp_vrid: VRID non_default_vrid;
