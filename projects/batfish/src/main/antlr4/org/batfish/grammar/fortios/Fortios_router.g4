parser grammar Fortios_router;

options {
  tokenVocab = FortiosLexer;
}

c_router: ROUTER (
    cr_access_list
    | cr_bgp
    | cr_static
);
