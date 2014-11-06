parser grammar JuniperGrammar_policy_options;

@members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "JuniperGrammar_policy_options: " + hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	return errors;
}
}


/* --- Policy-Options Stanza Rules -------------------------------------------------------------------*/
policy_options_stanza returns [JStanza js]
  :
  (POLICY_OPTIONS OPEN_BRACE l=po_stanza_list CLOSE_BRACE) 
  {
    PolicyOptionsStanza pos = new PolicyOptionsStanza();
    for (POStanza x : l) {
      pos.addPOStanza(x);
    }
    js = pos;
  }
  ;

po_stanza_list returns [List<POStanza> pol = new ArrayList<POStanza>()]
  :
  ((x=po_stanza
   |x=inactive_po_stanza
   ){pol.add(x);}
  )+
  ;
  
inactive_po_stanza returns [POStanza pos]
  :
  INACTIVE COLON (x=po_stanza) 
  {
    x.set_stanzaStatus(StanzaStatusType.INACTIVE);
    pos=x;
  }
  ;
   
po_stanza returns [POStanza pos]
  :
  (x=as_path_po_stanza
  |x=community_po_stanza
  |x=policy_statement_po_stanza
  |x=prefix_list_po_stanza
  |x=null_po_stanza
  )
  {pos =x;}
  ;
  
/* --- --- Policy-Options Sub-Stanza Rules -----------------------------------------------------------*/
as_path_po_stanza returns [POStanza pos]
@init{
  String pathstr="";
}
  :
  (AS_PATH (name=VARIABLE) 
    (
      (x=string_in_double_quotes {pathstr = x;})
      |(y=ARI_CHANGETHIS3 {pathstr=y.getText();})
    )
  SEMICOLON) 
  {pos = new PO_AsPathStanza(name.getText(), pathstr);}
  ;
  
community_po_stanza returns [POStanza pos]
@init {
  PO_CommunityStanza cpos;
}
  :
  (COMMUNITY (name=VARIABLE) {cpos = new PO_CommunityStanza(name.getText());} MEMBERS
  ((x=bracketed_list
    {
      for (String s : x) {
        cpos.addCommunityId(s);
      }
    })
  |(y = as_id){cpos.addCommunityId(y);}
  |(y = string_in_double_quotes){cpos.addCommunityId(y);}
  |(z = NO_EXPORT){cpos.addCommunityId(z.getText());}
  |(ASTERISK COLON ASTERISK){cpos.addCommunityId("*:*");}
  |(ASTERISK COLON i=integer){cpos.addCommunityId("*:"+i);}
  )
  SEMICOLON)
  {pos = cpos;}
  ;
  
policy_statement_po_stanza returns [POStanza pos]
@init {
  PO_PolicyStatementStanza pspos;
}
  :
  (POLICY_STATEMENT (name=VARIABLE) {pspos = new PO_PolicyStatementStanza(name.getText());}  OPEN_BRACE)
  (
    ((x=inactive_term_ps_po_stanza
     |x=term_ps_po_stanza
     ){pspos.addTerm(x);}
    )* 
    (a=anon_term_ps_po_stanza {pspos.addTerm(a);})?
  )
  CLOSE_BRACE
  {pos = pspos;}
  ;
  
prefix_list_po_stanza returns [POStanza pos]
@init {
  PO_PrefixListStanza plpos;
}
  :
  (PREFIX_LIST (name=VARIABLE |name=IP_ADDRESS_WITH_MASK) {plpos = new PO_PrefixListStanza(name.getText());}
    (
      (OPEN_BRACE
      (
        ((ipmask=IP_ADDRESS_WITH_MASK SEMICOLON| (ipmask=IPV6_ADDRESS_WITH_MASK {plpos.set_stanzaStatus(StanzaStatusType.IPV6);}) SEMICOLON) {plpos.addAddress(ipmask.getText());})+
        |(APPLY_PATH (path_str = string_in_double_quotes) {plpos.set_applyPathStr(path_str);} SEMICOLON )
      )
      CLOSE_BRACE)
      |((SEMICOLON) {plpos.set_stanzaStatus(StanzaStatusType.IGNORED);})
    )
  )
  {pos = plpos;}
  ;
   
null_po_stanza returns [POStanza pos]
  :
   s=removed_stanza {pos = new PO_NullStanza(s);}
  ;

/* --- --- --- Policy-Options->Policy-Statement Stanza Rules -----------------------------------------*/  
anon_term_ps_po_stanza returns [POPS_TermStanza tpspos]
  :
  (x=subterm_ps_po_stanza) 
  {
    x.set_name("rule0"); // TODO [P2]: why rule0? Juniper convention 
    tpspos=x;
  } 
  ;
 
inactive_term_ps_po_stanza returns [POPS_TermStanza tpspos]
  :
  INACTIVE COLON (x=term_ps_po_stanza) 
  {
    x.set_stanzaStatus(StanzaStatusType.INACTIVE);
    tpspos=x;
  }
  ;
 
term_ps_po_stanza returns [POPS_TermStanza tpspos]
  :
  (TERM    // TODO [P3]: it seems bad that these have to be spelled out
    (name=ACCEPT
    |name=ALLOW
    |name=BGP
    |name=DIRECT
    |name=DISCARD
    |name=IMPORT
    |name=INPUT
    |name=NEXT
    |name=NO_EXPORT
    |name=REJECT
    |name=VARIABLE
    |name=DEC
    )
    OPEN_BRACE (x=subterm_ps_po_stanza {x.set_name(name.getText());}) CLOSE_BRACE
  )
  {tpspos = x;}
  ; 

/* --- --- --- --- Policy-Options->Policy-Statement->Term Sub-Stanza Rules ---------------------------*/
subterm_ps_po_stanza returns [POPS_TermStanza tpspos]
@init{
  tpspos = new POPS_TermStanza();
}
  :
  ((FROM
    ((OPEN_BRACE fl=from_t_ps_stanza_list CLOSE_BRACE) 
    {
      for (POPST_FromStanza x : fl) {
        tpspos.addFromStanza(x);
      }
    }
    |(f=from_t_ps_stanza | f=inactive_from_t_ps_stanza) {tpspos.addFromStanza(f);}
    )
  )
  |(TO
    ((OPEN_BRACE tol=to_t_ps_stanza_list CLOSE_BRACE) 
    {
      for (POPST_ToStanza y : tol) {
        tpspos.addToStanza(y);
      }
    }
    |(t=to_t_ps_stanza | t=inactive_to_t_ps_stanza) {tpspos.addToStanza(t);}
    )
  )
  |(THEN
    ((OPEN_BRACE thl=then_t_ps_stanza_list CLOSE_BRACE) 
    {
      for (POPST_ThenStanza y : thl) {
        tpspos.addThenStanza(y);
      }
    }
    |(th=then_t_ps_stanza | th=inactive_then_t_ps_stanza) {tpspos.addThenStanza(th);}
    )
  ))+ 
  ;    
  
from_t_ps_stanza_list returns [List<POPST_FromStanza> ftpsposl = new ArrayList<POPST_FromStanza>()]
  :
  ( (x=from_t_ps_stanza | x=inactive_from_t_ps_stanza) {ftpsposl.add(x);})+
  ;
  
inactive_from_t_ps_stanza returns [POPST_FromStanza ftpspos]
  :
  INACTIVE COLON (x=from_t_ps_stanza) 
  {
    x.set_stanzaStatus(StanzaStatusType.INACTIVE);
    ftpspos = x;
  }
  ;
  
from_t_ps_stanza returns [POPST_FromStanza ftpspos]
  :
  (x=as_path_from_t_ps_stanza
  |x=community_from_t_ps_stanza
  |x=family_from_t_ps_stanza
  |x=interface_from_t_ps_stanza
  |x=neighbor_from_t_ps_stanza
  |x=origin_from_t_ps_stanza
  |x=prefix_list_from_t_ps_stanza
  |x=prefix_list_filter_from_t_ps_stanza
  |x=protocol_from_t_ps_stanza
  |x=rib_from_t_ps_stanza
  |x=route_filter_from_t_ps_stanza
  |x=source_address_filter_from_t_ps_stanza
  |x=tag_from_t_ps_stanza
  )
  {ftpspos = x;}
  ;  
  
then_t_ps_stanza_list returns [List<POPST_ThenStanza> ttpsposl = new ArrayList<POPST_ThenStanza>()]
  :
  ( (x=then_t_ps_stanza | x=inactive_then_t_ps_stanza) {ttpsposl.add(x);})+
  ;
  
inactive_then_t_ps_stanza returns [POPST_ThenStanza ttpspos]
  :
  INACTIVE COLON (x=then_t_ps_stanza) 
  {
    x.set_stanzaStatus(StanzaStatusType.INACTIVE);
    ttpspos = x;
  }
  ;
  
  then_t_ps_stanza returns [POPST_ThenStanza ttpspos]
  :
  (x=accept_then_t_ps_stanza
  |x=as_path_prepend_then_t_ps_stanza
  |x=community_then_t_ps_stanza
  |x=install_next_hop_then_t_ps_stanza
  |x=local_preference_then_t_ps_stanza
  |x=metric_then_t_ps_stanza
  |x=next_hop_then_t_ps_stanza
  |x=next_policy_then_t_ps_stanza
  |x=next_term_then_t_ps_stanza
  |x=reject_then_t_ps_stanza
  |x=null_then_t_ps_stanza
  )
  {ttpspos = x;}
  ; 
  
 to_t_ps_stanza_list returns [List<POPST_ToStanza> ttpsposl = new ArrayList<POPST_ToStanza>()]
  :
  ( (x=to_t_ps_stanza | x=inactive_to_t_ps_stanza) {ttpsposl.add(x);})+
  ;
  
inactive_to_t_ps_stanza returns [POPST_ToStanza ttpspos]
  :
  INACTIVE COLON (x=to_t_ps_stanza) 
  {
    x.set_stanzaStatus(StanzaStatusType.INACTIVE);
    ttpspos = x;
  }
  ;
  
  to_t_ps_stanza returns [POPST_ToStanza ttpspos]
  :
  (x=rib_to_t_ps_stanza
  |x=instance_to_t_ps_stanza
  )
  {ttpspos = x;}
  ; 
 
/* --- --- --- --- --- Policy-Options->Policy-Statement->Term->From Stanza Rules ---------------------*/
as_path_from_t_ps_stanza returns [POPST_FromStanza ftpspos]
@init {
  POPSTFr_AsPathStanza aftpspos = new POPSTFr_AsPathStanza();
}
  :
  (
  (AS_PATH)
  (
    (OPEN_BRACKET (name=VARIABLE {aftpspos.addName(name.getText());})+ CLOSE_BRACKET)
    |(name=VARIABLE) {aftpspos.addName(name.getText());}
  )
  (SEMICOLON)
  )
  {ftpspos = aftpspos;}
  ;

community_from_t_ps_stanza returns [POPST_FromStanza ftpspos]
@init {
POPSTFr_CommunityStanza cftpspos = new POPSTFr_CommunityStanza();
}
  :
  (
  (COMMUNITY)
  (
    (OPEN_BRACKET (name=VARIABLE {cftpspos.addListName(name.getText());})+ CLOSE_BRACKET)
    |(name=VARIABLE) {cftpspos.addListName(name.getText());}
  )
  (SEMICOLON)
  )
  {ftpspos = cftpspos;}
  ;
  
family_from_t_ps_stanza returns [POPST_FromStanza ftpspos]
  :
  (FAMILY 
  (fam=BRIDGE 
  |fam=CCC
  |fam=ETHERNET_SWITCHING
  |fam=INET
  |fam=INET_VPN
  |fam=INET6
  |fam=INET6_VPN
  |fam=ISO
  |fam=L2_VPN
  |fam=MPLS
  |fam=VPLS
  )
  SEMICOLON) 
  {ftpspos = new POPSTFr_FamilyStanza(FamilyTypeFromString(fam.getText()));}
  ;
  
interface_from_t_ps_stanza returns [POPST_FromStanza ttpspos]
@init {
POPSTFr_InterfaceStanza ittpspos = new POPSTFr_InterfaceStanza();
}
  :
  INTERFACE 
  (x=bracketed_list) 
  {
    for (String s:x) {
      ittpspos.addInterface(s);
    }
  }
  SEMICOLON
  {ttpspos=ittpspos;}
  ;
  
neighbor_from_t_ps_stanza returns [POPST_FromStanza ftpspos]
@init {
  POPSTFr_NeighborStanza nftpspos = new POPSTFr_NeighborStanza();
}
  :
  (NEIGHBOR
    (
      (ip=IP_ADDRESS) 
      |(ip = IPV6_ADDRESS){nftpspos.set_stanzaStatus(StanzaStatusType.IPV6);} 
    ) {nftpspos.set_ip(ip.getText());}
    SEMICOLON
  ) {ftpspos=nftpspos;}
  ; 
  
origin_from_t_ps_stanza returns [POPST_FromStanza ftpspos]
  :
  (x=ORIGIN y=VARIABLE SEMICOLON) {ftpspos = new POPSTFr_OriginStanza (y.getText());}
  ;
  
prefix_list_filter_from_t_ps_stanza returns [POPST_FromStanza ftpspos] 
@init {
  POPSTFr_PrefixListFilterStanza plfftpspos;
}
  :
  (PREFIX_LIST_FILTER name=VARIABLE) {plfftpspos = new POPSTFr_PrefixListFilterStanza(name.getText());}
  (match_type = match_type_filter_from_t_ps_stanza {plfftpspos.set_fms(match_type);})?
  (SEMICOLON
  |(prefix_action = action_filter_from_t_ps_stanza {plfftpspos.addFas(prefix_action);})
  |(OPEN_BRACE (prefix_action = action_filter_from_t_ps_stanza {plfftpspos.addFas(prefix_action);})* CLOSE_BRACE)
  )
  {ftpspos=plfftpspos;}
  ;

prefix_list_from_t_ps_stanza returns [POPST_FromStanza ftpspos] 
  :
  (PREFIX_LIST name=VARIABLE SEMICOLON) {ftpspos = new POPSTFr_PrefixListStanza(name.getText());}
  ;

protocol_from_t_ps_stanza returns [POPST_FromStanza ftpspos]
@init {
POPSTFr_ProtocolStanza pftpspos = new POPSTFr_ProtocolStanza();
}
  :
  (PROTOCOL
    ((p=AGGREGATE {pftpspos.addProtocol(ProtocolType.AGGREGATE);}
    |p=BGP {pftpspos.addProtocol(ProtocolType.BGP);}
    |p=DIRECT {pftpspos.addProtocol(ProtocolType.DIRECT);}
    |p=ISIS {pftpspos.addProtocol(ProtocolType.ISIS);}
    |p=MSDP {pftpspos.addProtocol(ProtocolType.MSDP);}
    |p=OSPF {pftpspos.addProtocol(ProtocolType.OSPF);}
    |p=STATIC {pftpspos.addProtocol(ProtocolType.STATIC);}
    )
    |(l=bracketed_list { for (String s : l) pftpspos.addProtocol(ProtocolTypeFromString(s));}))
  SEMICOLON)
  {ftpspos = pftpspos;}
  ;
  
rib_from_t_ps_stanza returns [POPST_FromStanza ftpspos]
  :
  (s=rib_common_stanza) {ftpspos = new POPSTFr_RibStanza (s);}
  ;
  
route_filter_from_t_ps_stanza returns [POPST_FromStanza ftpspos]
@init{
  POPSTFr_RouteFilterStanza rfftpspos = new POPSTFr_RouteFilterStanza();
}
  :
  (
  ROUTE_FILTER 
  (
    (ip=IP_ADDRESS_WITH_MASK)
    |(ip=IPV6_ADDRESS_WITH_MASK){rfftpspos.set_stanzaStatus(StanzaStatusType.IPV6);}
  ){rfftpspos.set_prefix(ip.getText());}
  (match_type = match_type_filter_from_t_ps_stanza {rfftpspos.set_fms(match_type);})?
  (SEMICOLON
  |(prefix_action = action_filter_from_t_ps_stanza {rfftpspos.addFas(prefix_action);})
  |(OPEN_BRACE (prefix_action = action_filter_from_t_ps_stanza {rfftpspos.addFas(prefix_action);})* CLOSE_BRACE)
  )
  )
  {ftpspos = rfftpspos;}
  ;
  
source_address_filter_from_t_ps_stanza returns [POPST_FromStanza ftpspos]
@init{
  POPSTFr_SourceAddressFilterStanza sfftpspos = new POPSTFr_SourceAddressFilterStanza();
}
  :
  (
  SOURCE_ADDRESS_FILTER 
  (
    (ip=IP_ADDRESS_WITH_MASK)
    |(ip=IPV6_ADDRESS_WITH_MASK){sfftpspos.set_stanzaStatus(StanzaStatusType.IPV6);}
  ){sfftpspos.set_prefix(ip.getText());}
  (match_type = match_type_filter_from_t_ps_stanza {sfftpspos.set_fms(match_type);})?
  (SEMICOLON
  |(prefix_action = action_filter_from_t_ps_stanza {sfftpspos.addFas(prefix_action);})
  |(OPEN_BRACE (prefix_action = action_filter_from_t_ps_stanza {sfftpspos.addFas(prefix_action);})* CLOSE_BRACE)
  )
  )
  {ftpspos = sfftpspos;}
  ;
  
tag_from_t_ps_stanza returns [POPST_FromStanza ftpspos]
  :
  (x=TAG i=integer SEMICOLON) {ftpspos = new POPSTFr_TagStanza (i);}
  ;
  
/* --- --- --- --- --- Policy-Options->Policy-Statement->Term->Then Stanza Rules ---------------------*/ 
accept_then_t_ps_stanza returns [POPST_ThenStanza ttpspos]
  :
  (ACCEPT SEMICOLON) {ttpspos = new POPSTTh_AcceptStanza();}
  ;
  
as_path_prepend_then_t_ps_stanza returns [POPST_ThenStanza ttpspos]
  :
  (AS_PATH_PREPEND 
  (asnum = integer {ttpspos = new POPSTTh_AsPathPrependStanza(asnum);}
  |s = string_in_double_quotes {ttpspos = new POPSTTh_AsPathPrependStanza(s);}
  ) 
  SEMICOLON) 
  ;

community_then_t_ps_stanza returns [POPST_ThenStanza ttpspos]
@init {
POPSTTh_CommunityStanza cttpspos = new POPSTTh_CommunityStanza();
}
  :
  (COMMUNITY
    (
      (SET {cttpspos.set_commType(POPSTTh_CommunityType.COMM_SET);}
      |ADD {cttpspos.set_commType(POPSTTh_CommunityType.COMM_ADD);}
      |DELETE {cttpspos.set_commType(POPSTTh_CommunityType.COMM_DELETE);}
      )
      (name=VARIABLE {cttpspos.addCommName(name.getText());}
      |l=bracketed_list {cttpspos.set_commNames(l);}
      )
    )
    SEMICOLON
  )
  {ttpspos=cttpspos;}
  ;
  
install_next_hop_then_t_ps_stanza returns [POPST_ThenStanza ttpspos]
@init {
  String hopname = "";
}
  :
  (INSTALL_NEXTHOP LSP 
  
  (
   (name = VARIABLE {hopname=name.getText();}) 
   |(i=integer '-' name=VARIABLE {hopname=i+ "-" + name.getText();}) 
  )
  SEMICOLON) {ttpspos = new POPSTTh_InstallNextHopStanza(hopname);}
  ;

local_preference_then_t_ps_stanza returns [POPST_ThenStanza ttpspos]
@init {
  POPSTTh_LocalPreferenceStanza lpttpspos = new POPSTTh_LocalPreferenceStanza();
}
  :
  (
    LOCAL_PREFERENCE  
    (
    (ADD) {lpttpspos.set_lpType(POPSTTh_LocalPreferenceType.LP_ADD);}
    |(SUBTRACT){lpttpspos.set_lpType(POPSTTh_LocalPreferenceType.LP_SUBTRACT);}
    )?
    x=integer SEMICOLON
  ) 
  {
     lpttpspos.set_localPref(x);
     ttpspos = lpttpspos;
  }
  ;
  
metric_then_t_ps_stanza returns [POPST_ThenStanza ttpspos]
  :
  METRIC
  (
    (OPEN_BRACE IGP x=integer SEMICOLON CLOSE_BRACE) {ttpspos = new POPSTTh_MetricStanza(x);}
    |(x=integer SEMICOLON) {ttpspos = new POPSTTh_MetricStanza(x);}
  )
  ;

next_hop_then_t_ps_stanza returns [POPST_ThenStanza ttpspos]
@init {
  POPSTTh_NextHopStanza hpttpspos = new POPSTTh_NextHopStanza();
}
  :
  (NEXT_HOP)
  (
    (ip=IP_ADDRESS){hpttpspos.set_hopName(ip.getText());}
    |(ip=IPV6_ADDRESS){hpttpspos.set_stanzaStatus(StanzaStatusType.IPV6);}
    |(DISCARD) {hpttpspos.set_hopType(PolicyStatement_HopType.NEXTHOP_DISCARD);}
    |(SELF) {hpttpspos.set_hopType(PolicyStatement_HopType.NEXTHOP_SELF);}
  )
  (SEMICOLON)
  {ttpspos=hpttpspos;}
  ;
  
next_policy_then_t_ps_stanza returns [POPST_ThenStanza ttpspos]
  :
  NEXT POLICY SEMICOLON {ttpspos = new POPSTTh_NextPolicyStanza();}
  ;
  
next_term_then_t_ps_stanza returns [POPST_ThenStanza ttpspos]
  :
  NEXT TERM SEMICOLON {ttpspos = new POPSTTh_NextTermStanza();}
  ;
  
reject_then_t_ps_stanza returns [POPST_ThenStanza ttpspos]
  :
  (REJECT SEMICOLON) {ttpspos = new POPSTTh_RejectStanza();}
  ; 
  
null_then_t_ps_stanza returns [POPST_ThenStanza ttpspos]
  :
  (s=load_balance_then_t_ps_stanza) {ttpspos = new POPSTTh_NullStanza(s);}
  
  ;

/* --- --- --- --- --- Policy-Options->Policy-Statement->Term->To Stanza Rules -----------------------*/ 
rib_to_t_ps_stanza returns [POPST_ToStanza ttpspos]
  :
  (s=rib_common_stanza) {ttpspos = new POPSTTo_RibStanza (s);}
  ;
  
instance_to_t_ps_stanza returns [POPST_ToStanza ttpspos]
  :
  (x=INSTANCE y=VARIABLE SEMICOLON) {ttpspos = new POPSTTo_InstanceStanza (y.getText());}
  ;
  

/* --- --- --- --- --- --- Policy-Options->Policy-Statement->Term->From->Filter Stanza Rules ---------*/
match_type_filter_from_t_ps_stanza returns [POPSTTh_FilterMatchStanza fms]
@init {
FilterMatch fm = null;
fms = new POPSTTh_FilterMatchStanza();
}
  :
    (
    (ADDRESS_MASK
      (ip = IP_ADDRESS 
      |ip = IPV6_ADDRESS {fms.set_stanzaStatus(StanzaStatusType.IPV6);}
      ){fm = new FilterMatch_AddressMask(ip.getText());})
    |(EXACT) {fm = new FilterMatch_Null(FilterMatchType.EXACT);}
    |(LONGER) {fm = new FilterMatch_Null(FilterMatchType.LONGER);}
    |(ORLONGER) {fm = new FilterMatch_Null(FilterMatchType.ORLONGER);}
    |(PREFIX_LENGTH_RANGE FORWARD_SLASH r1=integer DASH FORWARD_SLASH r2=integer) {fm = new FilterMatch_PrefixLengthRange(r1,r2);}
    |(THROUGH 
      (ip = IP_ADDRESS_WITH_MASK 
      |ip = IPV6_ADDRESS_WITH_MASK){fms.set_stanzaStatus(StanzaStatusType.IPV6);}
     ){
        String[] split_prefix = ip.getText().split("/");
        fm = new FilterMatch_Through(split_prefix[0], Integer.parseInt(split_prefix[1]));
      }
   |(UPTO FORWARD_SLASH r=integer) {fm = new FilterMatch_UpTo(r);}
   ) 
   {fms.set_filterMatch(fm);}
  ;
  
action_filter_from_t_ps_stanza returns [POPST_ThenStanza tpspos]
  :
    (x=then_t_ps_stanza){tpspos=x;}
  ;
  
/* --- --- --- --- --- --- Policy-Options->Policy-Statement->Term->Then->Null Stanza Rules -----------*/
load_balance_then_t_ps_stanza returns [String s]
  :
  (x=LOAD_BALANCE y=PER_PACKET SEMICOLON) {s = x.getText() + " " + y.getText();}
  ;
  