parser grammar Fortios_firewall;

options {
  tokenVocab = FortiosLexer;
}

c_firewall: FIREWALL (cf_service | cf_policy);
