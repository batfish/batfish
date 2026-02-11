parser grammar Fortios_router;

options {
  tokenVocab = FortiosLexer;
}

c_router: ROUTER (
    cr_access_list
    | cr_bgp
    | cr_route_map
    | cr_static
    | IGNORED_CONFIG_BLOCK
);
