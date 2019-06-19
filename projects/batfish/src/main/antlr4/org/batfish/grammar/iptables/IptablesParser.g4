parser grammar IptablesParser;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = IptablesLexer;
}

iptables_configuration
:
   command+
   |
   (
      declaration_table declaration_chain_policy* command* COMMIT NEWLINE
   )+
;

action
:
   OPTION_JUMP
   (
      built_in_target
      | chain
   ) //target_options?

   | OPTION_GOTO chain
;

chain
:
   FORWARD
   | INPUT
   | OUTPUT
   | PREROUTING
   | POSTROUTING
   | custom_chain = VARIABLE
;

declaration_chain_policy
:
   COLON chain built_in_target ~NEWLINE+ NEWLINE
;

declaration_table
:
   ASTERISK table NEWLINE
;

table
:
   TABLE_FILTER
   | TABLE_MANGLE
   | TABLE_NAT
   | TABLE_RAW
   | TABLE_SECURITY
   | custom_table = VARIABLE
;

command
:
   IPTABLES?
   (
      FLAG_TABLE table
   )? command_tail NEWLINE
;

command_append
:
   FLAG_APPEND chain rule_spec
;

command_check
:
   FLAG_CHECK chain rule_spec
;

command_delete
:
   FLAG_DELETE chain
   (
      rule_spec
      | rulenum = DEC
   )
;

command_delete_chain
:
   FLAG_DELETE_CHAIN chain?
;

command_flush
:
   FLAG_FLUSH
   (
      chain
      (
         rulenum = DEC
      )?
   )?
   (
      other_options
   )?
;

command_help
:
   FLAG_HELP
;

command_insert
:
   FLAG_INSERT chain
   (
      rulenum = DEC
   )? rule_spec
;

command_list
:
   FLAG_LIST
   (
      chain
      (
         rulenum = DEC
      )?
   )?
   (
      other_options
   )?
;

command_list_rules
:
   FLAG_LIST_RULES
   (
      chain
      (
         rulenum = DEC
      )?
   )?
;

command_new_chain
:
   FLAG_NEW_CHAIN chain
;

command_policy
:
   FLAG_POLICY chain built_in_target
;

command_rename_chain
:
   FLAG_RENAME_CHAIN oldchain = chain newchain = chain
;

command_replace
:
   FLAG_REPLACE chain rulenum = DEC rule_spec
;

command_zero
:
   FLAG_ZERO
   (
      chain
      (
         rulenum = DEC
      )?
   )?
   (
      other_options
   )?
;

command_tail
:
   command_append
   | command_check
   | command_delete
   | command_delete_chain
   | command_flush
   | command_help
   | command_insert
   | command_list
   | command_list_rules
   | command_new_chain
   | command_policy
   | command_rename_chain
   | command_replace
   | command_zero
;

endpoint
:
   IP_ADDRESS
   | IP_PREFIX
   | IPV6_ADDRESS
   | IPV6_PREFIX
   | name = VARIABLE
;

match
:
   OPTION_IPV4
   | OPTION_IPV6
   | NOT?
   (
      OPTION_DESTINATION endpoint
      | OPTION_DESTINATION_PORT port = DEC
      | OPTION_IN_INTERFACE interface_name = VARIABLE
      | OPTION_OUT_INTERFACE interface_name = VARIABLE
      | OPTION_PROTOCOL protocol
      | OPTION_SOURCE endpoint
      | OPTION_SOURCE_PORT port = DEC
   )
   | OPTION_MATCH match_module
;

//this is where rich module-based matching goes; not supported yet

match_module
:
   match_module_tcp
;

match_module_tcp
:
   TCP
;

//this is where options for flush, list, zero commands go
//fill this in later

other_options
:
   OPTION_VERBOSE
;

protocol
:
   TCP
   | UDP
   | UDPLITE
   | ICMP
   | ICMPV6
   | ESP
   | AH
   | SCTP
   | MH
   | ALL
   | protocolnum = DEC
;

rule_spec
:
   (
      match_list += match
   )* action
;

built_in_target
:
   ACCEPT
   | DROP
   | RETURN
;

//not sure what target options are valid yet; fill in later

target_options
:
   OPTION_VERBOSE
;

