parser grammar PaloAlto_tag;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_tag
:
    TAG name = variable
    (
        st_color
        | st_comments
    )*
;

st_comments
:
    COMMENTS comments = variable
;

st_color
:
    COLOR null_rest_of_line
;
