parser grammar PaloAlto_application_filter;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_application_filter
:
    APPLICATION_FILTER name = variable
    (
        saf_category
        | saf_evasive
        | saf_excessive_bandwidth_use
        | saf_has_known_vulnerabilities
        | saf_pervasive
        | saf_prone_to_misuse
        | saf_risk
        | saf_subcategory
        | saf_technology
        | saf_transfers_files
        | saf_tunnels_other_apps
        | saf_used_by_malware
    )
;

saf_category
:
  CATEGORY null_rest_of_line
;

saf_evasive
:
  EVASIVE YES
;

saf_excessive_bandwidth_use
:
  EXCESSIVE_BANDWIDTH_USE YES
;

saf_has_known_vulnerabilities
:
  HAS_KNOWN_VULNERABILITIES YES
;

saf_pervasive
:
  PERVASIVE YES
;

saf_prone_to_misuse
:
  PRONE_TO_MISUSE YES
;

saf_risk
:
// 1-5
  RISK uint8
;

saf_subcategory
:
  SUBCATEGORY null_rest_of_line
;

saf_technology
:
  TECHNOLOGY null_rest_of_line
;

saf_transfers_files
:
  TRANSFERS_FILES YES
;

saf_tunnels_other_apps
:
  TUNNELS_OTHER_APPS YES
;
saf_used_by_malware
:
  USED_BY_MALWARE YES
;

