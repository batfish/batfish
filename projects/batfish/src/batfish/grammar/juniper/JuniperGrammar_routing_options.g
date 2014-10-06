parser grammar JuniperGrammar_routing_options;

@members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "JuniperGrammar_routing_options: " + hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	return errors;
}
}


/* --- Routing Options Stanza Rules ------------------------------------------------------------------*/
routing_options_stanza returns [JStanza js]
@init {
  RoutingOptionsStanza rs = new RoutingOptionsStanza();
}
  :
  (ROUTING_OPTIONS OPEN_BRACE (x=ro_stanza {rs.AddROStanza(x);})+ CLOSE_BRACE)
  {js = rs;}
  ;

ro_stanza returns [ROStanza ros]
  :
  (x=autonomous_system_ro_stanza
  |x=martians_ro_stanza
  |x=rib_groups_ro_stanza
  |x=rib_ro_stanza
  |x=router_id_ro_stanza
  |x=static_ro_stanza
  |x=null_ro_stanza
  )
  {ros = x;}
  ;

/* --- --- Routing Options Sub-Stanza Rules ----------------------------------------------------------*/
autonomous_system_ro_stanza returns [ROStanza ros]
  :
  (AUTONOMOUS_SYSTEM num=integer SEMICOLON) {ros = new RO_AutonomousSystemStanza(num);}
  ;
  
martians_ro_stanza returns [ROStanza ros]
@init {
  RO_MartiansStanza mros = new RO_MartiansStanza ();
  Martian m;
}
  :
  MARTIANS OPEN_BRACE {m = new Martian();}
  (
    (ip=IP_ADDRESS_WITH_MASK 
    |ip=IPV6_ADDRESS_WITH_MASK {m.set_isIPV6(true);}
    ){m.set_ipWithMask(ip.getText());}
    (ORLONGER) {m.set_fm(new FilterMatch_Null(FilterMatchType.ORLONGER));}
    (ALLOW? {m.set_isAllowed(true);})
    SEMICOLON
    {mros.AddMartian(m);}
  )+
  CLOSE_BRACE
  {ros=mros;}
  ;
  
rib_groups_ro_stanza returns [ROStanza ros]
@init {
   RO_RibGroupsStanza rros = new RO_RibGroupsStanza();
}
  :
  RIB_GROUPS OPEN_BRACE 
  (group_name = VARIABLE OPEN_BRACE IMPORT_RIB l=bracketed_list CLOSE_BRACE {rros.AddGroup(group_name.getText(),l);})+
  CLOSE_BRACE
  {ros = rros;}
  ;
  
rib_ro_stanza returns [ROStanza ros]
  :
  // TODO [Ask Ari]: probably am not supposed to be ignoring this stuff.
  RIB (name=VARIABLE) ignored_substanza {ros = new RO_RibStanza(name.getText());}
  ;
  
router_id_ro_stanza returns [ROStanza ros]
  :
  (ROUTER_ID id=IP_ADDRESS SEMICOLON) {ros = new RO_RouterIDStanza(id.getText());}
  ;
  
static_ro_stanza returns [ROStanza ros]
@init {
  RO_StaticStanza sros = new RO_StaticStanza ();
}
  :
  (STATIC OPEN_BRACE 
  (
  (x=sro_stanza 
  |x=inactive_sro_stanza
  ){sros.AddROSTStanza(x);})+ 
  CLOSE_BRACE)  
  {ros=sros;}                                                          
  ;

null_ro_stanza returns [ROStanza ros]
  :
  (s=aggregate_ro_stanza
  |s=interface_routes_ro_stanza
  |s=forwarding_table_ro_stanza
  |s=multicast_ro_stanza
  )
  {ros = new RO_NullStanza(s);}
  ;  
  
/* --- --- --- Routing Options->Static Stanza Rules --------------------------------------------------*/
inactive_sro_stanza returns [RO_STStanza sros]
  :
  INACTIVE COLON (x=sro_stanza) 
  {
    x.set_stanzaStatus(StanzaStatusType.INACTIVE);
    sros=x;
  }
  ; 
 
sro_stanza returns [RO_STStanza sros]
  :
  (x=defaults_sro_stanza 
  |x=rib_group_sro_stanza
  |x=route_sro_stanza 
  )
  {sros = x;}
  ;

/* --- --- --- Routing Options->Null Stanza Rules ----------------------------------------------------*/
aggregate_ro_stanza returns [String s] // TODO [Ask Ari]: Should this really get ignored?
  :
  x=AGGREGATE ignored_substanza {s = x.getText() + "{...}";}
  ;

interface_routes_ro_stanza returns [String s] // TODO [Ask Ari]: Should this really get ignored?
  :
  x=INTERFACE_ROUTES ignored_substanza {s = x.getText() + "{...}";}
  ;

forwarding_table_ro_stanza returns [String s] // TODO [Ask Ari]: Should this really get ignored?
  :
  x=FORWARDING_TABLE ignored_substanza {s = x.getText() + "{...}";}
  ;

multicast_ro_stanza returns [String s] // TODO [Ask Ari]: Should this really get ignored?
  :
  x=MULTICAST ignored_substanza {s = x.getText() + "{...}";}
  ;

/* --- --- --- --- Routing Options->Static Stanza Rules ----------------------------------------------*/
defaults_sro_stanza returns [RO_STStanza sros ] 
@init {
ROST_DefaultsStanza dsros = new ROST_DefaultsStanza ();
}
// TODO [Ask Ari]: I'm sure we care about what's in here
  :
  DEFAULTS OPEN_BRACE (x=static_opts_sro_stanza {dsros.AddStaticOption(x);})+ CLOSE_BRACE 
  {sros = dsros;}
  ; 

 rib_group_sro_stanza returns [RO_STStanza sros] 
  :
  RIB_GROUP group_name=VARIABLE SEMICOLON {sros = new ROST_RibGroupStanza(group_name.getText()); }
  ; 
  
route_sro_stanza returns [RO_STStanza sros] 
@init {
ROST_RouteStanza rsros = new ROST_RouteStanza ();
}
  :
  ROUTE  
  (ip=IP_ADDRESS_WITH_MASK
  |ip=IPV6_ADDRESS_WITH_MASK {rsros.set_stanzaStatus(StanzaStatusType.IPV6);}
  ) {rsros.set_ip(ip.getText());}
  
  (OPEN_BRACE (x=static_opts_sro_stanza {rsros.AddStaticOption(x);})+ CLOSE_BRACE 
  |(x=static_opts_sro_stanza {rsros.AddStaticOption(x);}) SEMICOLON
  )
  
  {sros = rsros;}
  ;

/* --- --- --- --- --- Routing Options->Static->Defaults Stanza Rules --------------------------------*/
static_opts_sro_stanza returns [StaticOptions so]
  :
  (ACTIVE {so = new StaticOptions_Active(true);}
  |PASSIVE {so = new StaticOptions_Active(false);}
  |DISCARD {so = new StaticOptions_Discard(true);}
  |AS_PATH x=VARIABLE {so = new StaticOptions_AsPath(x.getText());}
  |c=community_static_opts_sro_stanza {so = new StaticOptions_Communities(c);}
  |INSTALL {so = new StaticOptions_Install(true);}
  |NEXT_HOP i=IP_ADDRESS {so = new StaticOptions_NextHop(i.getText());} 
  |NEXT_TABLE v=VARIABLE {so = new StaticOptions_NextTable(v.getText());} 
  |NO_INSTALL {so = new StaticOptions_Install(false);}
  |METRIC n=integer {so = new StaticOptions_Metric(n);}
  |PREFERENCE n=integer {so = new StaticOptions_Preference(n);}
  |READVERTISE {so = new StaticOptions_Readvertise(true);}
  |NO_READVERTISE {so = new StaticOptions_Readvertise(false);}
  |RESOLVE {so = new StaticOptions_Resolve(true);}
  |NO_RESOLVE {so = new StaticOptions_Resolve(false);}
  |RETAIN {so = new StaticOptions_Retain(true);}
  |NO_RETAIN {so = new StaticOptions_Retain(false);}
  |TAG v=VARIABLE {so = new StaticOptions_Tag(v.getText());}
  )
  SEMICOLON
  ;
  
 /* --- --- --- --- --- Routing Options->Static->Defaults Stanza Rules --------------------------------*/
community_static_opts_sro_stanza returns [ArrayList<String> l]
   :
   COMMUNITY 
   (b=bracketed_list {l=b;}
   |s=as_id {l = new ArrayList<String>(); l.add(s);}
   )
   ;
 