parser grammar NecIx_route_map;

options {
  tokenVocab = NecIxLexer;
}

s_route_map
:
  ROUTE_MAP name = variable action = ( PERMIT | DENY )? seq = uint? NEWLINE rm_inner*
;

rm_inner
:
  INDENT
  (
    rm_match
    | rm_set
  )
;

rm_match
:
  MATCH
  (
    rmm_prefix_list
    | rmm_unimplemented
  )
;

rmm_prefix_list
:
  IP ADDRESS PREFIX_LIST name = variable NEWLINE
;

// match community / metric / interface / tag / ip next-hop, match ip address access-list (policy
// routing).
rmm_unimplemented
:
  ~( IP | NEWLINE ) null_rest_of_line
  | IP ~ADDRESS null_rest_of_line
  | IP ADDRESS ~( PREFIX_LIST | NEWLINE ) null_rest_of_line
;

rm_set
:
  SET
  (
    rms_as_path_prepend
    | rms_community
    | rms_local_preference
    | rms_metric
    | rms_unimplemented
  )
;

rms_as_path_prepend
:
  AS_PATH PREPEND ( asns += uint )+ NEWLINE
;

rms_community
:
  COMMUNITY ( communities += community )+ ADDITIVE? NEWLINE
;

community
:
  STANDARD_COMMUNITY
  | NO_ADVERTISE
  | NO_EXPORT
  | NO_EXPORT_SUBCONFED
;

rms_local_preference
:
  LOCAL_PREFERENCE value = uint NEWLINE
;

rms_metric
:
  METRIC value = uint NEWLINE
;

// set community none / delete, set origin, set ip next-hop, set metric +N, ...
rms_unimplemented
:
  ~( AS_PATH | COMMUNITY | LOCAL_PREFERENCE | METRIC | NEWLINE ) null_rest_of_line
  | COMMUNITY ~( STANDARD_COMMUNITY | NO_ADVERTISE | NO_EXPORT | NO_EXPORT_SUBCONFED | NEWLINE )
  null_rest_of_line
  | METRIC ~( UINT | NEWLINE ) null_rest_of_line
;
