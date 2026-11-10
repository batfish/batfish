parser grammar Rtx_tunnel;

s_tunnel
:
  TUNNEL
  (
    st_disable
    | st_enable
    | st_encapsulation_null
    | st_endpoint_null
    | st_select
  )
;

// `tunnel select <n>` switches the tunnel that subsequent `tunnel`/`ip tunnel` commands configure.
// This is the only stateful construct in the RTX configuration language.
st_select
:
  SELECT num = UINT NEWLINE
;

st_enable
:
  ENABLE num = UINT NEWLINE
;

st_disable
:
  DISABLE num = UINT NEWLINE
;

// The encapsulation type does not change how Batfish models the interface.
st_encapsulation_null
:
  ENCAPSULATION IGNORED_TEXT* NEWLINE
;

// `tunnel endpoint address [<local>] <remote>`. The J57 tunnels have IPv6 endpoints, which Batfish
// cannot model, so the endpoints are discarded and only the interface is created.
st_endpoint_null
:
  ENDPOINT ADDRESS IGNORED_TEXT* NEWLINE
;
