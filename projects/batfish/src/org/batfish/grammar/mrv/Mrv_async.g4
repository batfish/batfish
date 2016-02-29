parser grammar Mrv_async;

import Mrv_common;

options {
   tokenVocab = MrvLexer;
}

async_autohang
:
   ASYNC PERIOD AUTOHANG type_declaration
;
