parser grammar Fortios_bgp;

options {
  tokenVocab = FortiosLexer;
}

cr_bgp: BGP newline crb*;

crb: crb_set | crb_config;

crb_set: SET crb_set_singletons;

crb_set_singletons
:
    crb_set_as
    | crb_set_ebgp_multipath
    | crb_set_ibgp_multipath
    | crb_set_router_id
;

crb_set_as: AS bgp_as newline;

crb_set_ebgp_multipath: EBGP_MULTIPATH enable_or_disable newline;

crb_set_ibgp_multipath: IBGP_MULTIPATH enable_or_disable newline;

crb_set_router_id: ROUTER_ID router_id = ip_address newline;

crb_config
:
    CONFIG (
        crbc_neighbor
        | crbc_network
        | crbc_redistribute
        | IGNORED_CONFIG_BLOCK
    ) END NEWLINE
;

crbc_neighbor: NEIGHBOR newline crbcn_edit*;

crbcn_edit: EDIT bgp_neighbor_id newline crbcne* NEXT newline;

crbcne
:
    SET (
        crbcne_set_remote_as
        | crbcne_set_route_map_in
        | crbcne_set_route_map_out
        | crbcne_set_update_source
    )
;

crbcne_set_remote_as: REMOTE_AS bgp_remote_as newline;

crbcne_set_route_map_in: ROUTE_MAP_IN route_map_name newline;

crbcne_set_route_map_out: ROUTE_MAP_OUT route_map_name newline;

crbcne_set_update_source: UPDATE_SOURCE interface_name newline;

crbc_network: NETWORK newline crbcnet_edit*;

crbcnet_edit: EDIT bgp_network_id newline crbcnete* NEXT newline;

crbcnete
:
    crbcnete_set_prefix
    | UNSET unimplemented
;

crbcnete_set_prefix: SET PREFIX network = ip_address_with_mask_or_prefix newline;


crbc_redistribute: REDISTRIBUTE bgp_redist_protocol newline crbcr*;

crbcr
:
    SET crbcr_set_status
;

crbcr_set_status: STATUS enable_or_disable newline;

// 0-4294967295
bgp_as: str;

// 1-4294967295
bgp_remote_as: str;

// An IP (but not using ip_address rule because EDIT pushes str mode)
bgp_neighbor_id: str;

// 1-4294967295
bgp_network_id: str;

bgp_redist_protocol: str;
