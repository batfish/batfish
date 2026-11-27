parser grammar Fortios_vdom;

options {
  tokenVocab = FortiosLexer;
}

c_vdom: VDOM newline cv_entries;

// Backups may use END for the edit, with no separate END after the last edit.
cv_end: END NEWLINE cv_entries?;

cv_entries
:
    cv_edit (cv_next | cv_end)
    | END NEWLINE
;

cv_edit: EDIT name = str newline cve_config*;

cv_next: NEXT newline cv_entries;

cve_config: CONFIG config_body;
