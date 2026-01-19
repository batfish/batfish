parser grammar Fortios_firewall;

options {
  tokenVocab = FortiosLexer;
}

c_firewall: FIREWALL (
  cf_address
  | cf_addrgrp
  | cf_internet_service_name
  | cf_policy
  | cf_service
  | cf_ippool
  | IGNORED_CONFIG_BLOCK
);
