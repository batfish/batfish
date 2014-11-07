parser grammar JuniperGrammar_system;

@members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
  String hdr = getErrorHeader(e);
  String msg = getErrorMessage(e, tokenNames);
  String errorMessage = "JuniperGrammar_system: " + hdr + " " + msg;
  errors.add(errorMessage);
}

public List<String> getErrors() {
  return errors;
}
}

/* --- System Stanza Rules ---------------------------------------------------------------------------*/
system_stanza returns [JStanza js]
@init {
  SystemStanza ss = new SystemStanza();
}
  :
  SYSTEM OPEN_BRACE 
  (x=sys_stanza {ss.AddSysStanza(x);})+
  CLOSE_BRACE
  {js = ss;}
  ;
     
sys_stanza returns [SysStanza ss]
  :
  (x=host_name_sys_stanza
  |x=null_sys_stanza
  )
  { ss =x; }
  ;
  
/* --- --- System Sub-Stanza Rules -------------------------------------------------------------------*/
host_name_sys_stanza returns [SysStanza ss]
  :
  (HOST_NAME name=VARIABLE SEMICOLON) {ss = new Sys_HostNameStanza(name.getText());}
  ; 
  
null_sys_stanza returns [SysStanza ss]
  :
  (s=accounting_sys_stanza
  |s=arp_sys_stanza
  |s=authentication_order_sys_stanza
  |s=backup_router_sys_stanza
  |s=domain_name_sys_stanza
  |s=domain_search_sys_stanza
  |s=dump_on_panic_sys_stanza
  |s=license_sys_stanza
  |s=login_sys_stanza
  |s=location_sys_stanza
  |s=max_configurations_on_flash_sys_stanza
  |s=max_configuration_rollbacks_sys_stanza
  |s=name_server_sys_stanza
  |s=ntp_sys_stanza
  |s=ports_sys_stanza
  |s=radius_options_sys_stanza
  |s=radius_server_sys_stanza
  |s=removed_stanza
  |s=root_authentication_sys_stanza
  |s=services_sys_stanza
  |s=syslog_sys_stanza
  |s=tacplus_server_sys_stanza
  |s=time_zone_sys_stanza
  )
  {ss = new Sys_NullStanza(s);}
  ;

/* --- --- --- System->Null Stanza Rules -------------------------------------------------------------*/
accounting_sys_stanza returns [String s]
  :
  x=ACCOUNTING ignored_substanza {s=x.getText() + "{...}";}
  ;  
  
arp_sys_stanza returns [String s]
  :
  x=ARP ignored_substanza {s=x.getText() + "{...}";}
  ; 
  
authentication_order_sys_stanza returns [String s]
  :
  x=AUTHENTICATION_ORDER bracketed_list SEMICOLON {s=x.getText() + "{...}";}
  ;
  
backup_router_sys_stanza returns [String s]
  :
  x=BACKUP_ROUTER i=IP_ADDRESS SEMICOLON {s=x.getText() + " " + i.getText();}
  ;

domain_name_sys_stanza returns [String s]
  :
  x=DOMAIN_NAME VARIABLE SEMICOLON {s=x.getText() + "{...}";}
  ;

domain_search_sys_stanza returns [String s]
  :
  x=DOMAIN_SEARCH VARIABLE SEMICOLON {s=x.getText() + "{...}";}
  ;
  
dump_on_panic_sys_stanza returns [String s]
  :
  x=DUMPONPANIC SEMICOLON {s=x.getText();}
  ;
  
license_sys_stanza returns [String s]
  :
  x=LICENSE ignored_substanza {s=x.getText() + "{...}";}
  ;
  
location_sys_stanza returns [String s]
  :
  x=LOCATION VARIABLE+ SEMICOLON {s=x.getText() + "{...}";}
  ;

login_sys_stanza returns [String s]
  :
  x=LOGIN ignored_substanza {s=x.getText() + "{...}";}
  ;

max_configurations_on_flash_sys_stanza returns [String s]
  :
  x=MAX_CONFIGURATIONS_ON_FLASH VARIABLE+ SEMICOLON {s=x.getText() + "{...}";}
  ;

max_configuration_rollbacks_sys_stanza returns [String s]
  :
  x=MAX_CONFIGURATION_ROLLBACKS VARIABLE+ SEMICOLON {s=x.getText() + "{...}";}
  ;

name_server_sys_stanza returns [String s]
  :
  x=NAME_SERVER ignored_substanza {s=x.getText() + "{...}";}
  ;

ntp_sys_stanza returns [String s]
  :
  x=NTP ignored_substanza {s=x.getText() + "{...}";}
  ;

radius_options_sys_stanza returns [String s]
  :
  x=RADIUS_OPTIONS ignored_substanza {s=x.getText() + "{...}";}
  ;

radius_server_sys_stanza returns [String s]
  :
  x=RADIUS_SERVER ignored_substanza {s=x.getText() + "{...}";}
  ;

ports_sys_stanza returns [String s]
  :
  x=PORTS ignored_substanza {s=x.getText() + "{...}";}
  ;

root_authentication_sys_stanza returns [String s]
  :
  x=ROOT_AUTHENTICATION ignored_substanza {s=x.getText() + "{...}";}
  ;

services_sys_stanza returns [String s]
  :
  x=SERVICES ignored_substanza {s=x.getText() + "{...}";}
  ;

syslog_sys_stanza returns [String s]
  :
  x=SYSLOG ignored_substanza {s=x.getText() + "{...}";}
  ;

tacplus_server_sys_stanza returns [String s]
  :
  x=TACPLUS_SERVER ignored_substanza {s=x.getText() + "{...}";}
  ;

time_zone_sys_stanza returns [String s]
  :
  x=TIME_ZONE VARIABLE SEMICOLON {s=x.getText();}
  ;
  