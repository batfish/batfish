parser grammar PaloAlto_policy_rule;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}


bgp_med
:
// 0 - 4294967295
  uint32
;

bgp_policy_rule
:
    RULES name = variable
    (
        pr_action
        | pr_enable
        | pr_match
        | pr_used_by
    )?
;

pr_action
:
    ACTION
    (
        pra_allow
        | pra_deny
    )
;

pr_enable
:
    ENABLE yn = yes_or_no
;

pr_match
:
    MATCH
    (
        prm_address_prefix
        | prm_from_peer
    )
;

pr_used_by
:
    USED_BY name = variable
;

pra_allow
:
    ALLOW praa_update?
;

pra_deny
:
    DENY
;

praa_update
:
    UPDATE
    (
        praau_as_path
        | praau_community
        | praau_extended_community
        | praau_med
        | praau_origin
    )?
;

praau_as_path
:
    AS_PATH name = variable
;

praau_community
:
    COMMUNITY name = variable
;

praau_extended_community
:
    EXTENDED_COMMUNITY name = variable
;

praau_med
:
    MED val = bgp_med
;

praau_origin
:
    ORIGIN (EGP | IGP | INCOMPLETE)
;

prm_address_prefix
:
    ADDRESS_PREFIX ip_prefix EXACT yn = yes_or_no
;

prm_from_peer
:
    FROM_PEER variable_list
;
