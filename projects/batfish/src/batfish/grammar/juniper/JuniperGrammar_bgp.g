parser grammar JuniperGrammar_bgp;

@members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "JuniperGrammar_bgp: " + hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	return errors;
}
}

/* --- --- --- Protocol->BGP Common Stanza Rules -----------------------------------------------------*/
export_common_stanza returns [BGPExportList b = new BGPExportList()]
  :
  (EXPORT 
  (name=VARIABLE {b.AddPolicyName(name.getText());} 
  |x=bracketed_list 
   {
     for (String s : x) {
       b.AddPolicyName(s);
     }
   }
  )
  SEMICOLON)
  ; 

bgp_family_common_stanza returns [BGPFamily bfs = new BGPFamily()]
  :
  FAMILY 
  (ft=BRIDGE 
  |ft=CCC
  |ft=ETHERNET_SWITCHING
  |ft=INET
  |ft=INET_VPN
  |ft=INET6
  |ft=INET6_VPN
  |ft=ISO
  |ft=L2_VPN
  |ft=MPLS
  |ft=VPLS
  )
  ignored_substanza // TODO [Ask Ari]: I'm certain these should not be ignored.
  ;
  
import_common_stanza returns [BGPImportList b = new BGPImportList()]
  :
  (IMPORT 
  (name=VARIABLE {b.AddPolicyName(name.getText());} 
  |x=bracketed_list 
   {
     for (String s : x) {
       b.AddPolicyName(s);
     }
   }
  )
  SEMICOLON)
  ; 
  
local_address_common_stanza returns [BGPLocalAddress las]
  :
  LOCAL_ADDRESS 
  (
  ip=IP_ADDRESS {las = new BGPLocalAddress(ip.getText(),false);}
  |ip=IPV6_ADDRESS {las = new BGPLocalAddress(ip.getText(), true);}
  )
  SEMICOLON
  ; 
  
peer_as_common_stanza returns [BGPPeerAS pas]
  :
  (PEER_AS num=integer SEMICOLON) {pas = new BGPPeerAS(num);}
  ;

/* --- --- --- Protocol->BGP Stanza Rules ------------------------------------------------------------*/
bgp_p_stanza returns [PStanza ps]
 :
  (BGP OPEN_BRACE l=bg_stanza_list CLOSE_BRACE)
  {
    BGPStanza bs = new BGPStanza();
    for (BGStanza bgs : l) {
      bs.AddBGStanza(bgs);
    }
    ps=bs;
  }
  ;
  
bg_stanza_list returns [List<BGStanza> bgsl = new ArrayList<BGStanza>()]
  :
  (
    (x=bg_stanza
    |x=inactive_bg_stanza
    ){bgsl.add(x);}
  )+
  ;
    
/* --- --- --- --- Protocol->BGP Sub-Stanza Rules ----------------------------------------------------*/  
    
bg_stanza returns [BGStanza bgs]
  :
  (x=family_bg_stanza
  |x=group_bg_stanza
  |x=null_bg_stanza
  )
  { bgs =x; }
  ;
  
inactive_bg_stanza returns [BGStanza bgs]
  :
  INACTIVE COLON (x=bg_stanza) 
  {
    x.set_stanzaStatus(StanzaStatusType.INACTIVE);
    bgs=x;
  }
  ;
  
/* --- --- --- --- --- Protocol->BGP->Family Stanza Rules --------------------------------------------*/
family_bg_stanza returns [BGStanza bgs] 
  :
  x=bgp_family_common_stanza {bgs = new BG_FamilyStanza(x);} 
  ;

/* --- --- --- --- --- Protocol->BGP->Group Stanza Rules ---------------------------------------------*/
group_bg_stanza returns [BGStanza bgs]
  :
  (GROUP name=VARIABLE OPEN_BRACE l=gbg_stanza_list CLOSE_BRACE)
  {
    BG_GroupStanza gbgs = new BG_GroupStanza(name.getText());
    for (BG_GRStanza x : l) {
      gbgs.AddBGGRStanza(x);
    }
    bgs = gbgs;
  }
  ;
  
gbg_stanza_list returns [List<BG_GRStanza> gbgl = new ArrayList<BG_GRStanza>()]
  :(
    (x=gbg_stanza
    |x=inactive_gbg_stanza
    ){gbgl.add(x);}
  )+
  ;
  
 gbg_stanza returns [BG_GRStanza gbgs]
  :
  (x=export_gbg_stanza
  |x=family_gbg_stanza
  |x=import_gbg_stanza
  |x=local_address_gbg_stanza
  |x=local_as_gbg_stanza
  |x=neighbor_gbg_stanza
  |x=peer_as_gbg_stanza
  |x=type_gbg_stanza
  |x=null_gbg_stanza
  )
  { gbgs =x; }
  ;
  
 inactive_gbg_stanza returns [BG_GRStanza bgs]
  :
  INACTIVE COLON (x=gbg_stanza) 
  {
    x.set_stanzaStatus(StanzaStatusType.INACTIVE);
    bgs=x;
  }
  ;
  
/* --- --- --- --- --- Protocol->BGP->Null Stanza Rules ----------------------------------------------*/
null_bg_stanza returns [BGStanza bgs]
  :
  (s=log_updown_bg_stanza
  |s=traceoptions_bg_stanza
  )
  {bgs = new BG_NullStanza(s);}
  ;
  
/* --- --- --- --- --- --- Protocol->BGP->Group Sub-Stanza Rules -------------------------------------*/
export_gbg_stanza returns [BG_GRStanza gbgs]
  :
  x=export_common_stanza {gbgs = new BGGR_ExportStanza(x);}
  ; 
  
family_gbg_stanza returns [BG_GRStanza gbgs] 
  :
  x=bgp_family_common_stanza {gbgs = new BGGR_FamilyStanza(x);} 
  ;
  
import_gbg_stanza returns [BG_GRStanza gbgs]
  :
  x=import_common_stanza {gbgs = new BGGR_ImportStanza(x);}
  ; 

local_address_gbg_stanza returns [BG_GRStanza gbgs]
  :
  x = local_address_common_stanza {gbgs = new BGGR_LocalAddressStanza(x);}
  ; 
  
local_as_gbg_stanza returns [BG_GRStanza gbgs]
  :
  (LOCAL_AS num=integer SEMICOLON) {gbgs = new BGGR_LocalAsStanza(num);}
  ;
  
neighbor_gbg_stanza returns [BG_GRStanza gbgs]
@init {
  BGGR_NeighborStanza ngbgs = new BGGR_NeighborStanza();
}
  :
  (NEIGHBOR
  (ip=IP_ADDRESS
  |ip=IPV6_ADDRESS {ngbgs.set_stanzaStatus(StanzaStatusType.IPV6);}
  ){ngbgs.set_neighborIP(ip.getText());}
  OPEN_BRACE ((x=ngbg_stanza | x=inactive_ngbg_stanza) {ngbgs.addBGGRNStanza(x);})+ CLOSE_BRACE
  )
  {gbgs = ngbgs;}
  ;
  
peer_as_gbg_stanza returns [BG_GRStanza gbgs]
  :
  (x=peer_as_common_stanza) {gbgs = new BGGR_PeerAsStanza(x);}
  ;

type_gbg_stanza returns [BG_GRStanza gbgs]
  :
  TYPE
  (
  INTERNAL {gbgs = new BGGR_TypeStanza(false);}
  |EXTERNAL {gbgs = new BGGR_TypeStanza(true);}
  )
  SEMICOLON
  ; 

null_gbg_stanza returns [BG_GRStanza gbgs]
  :
  (s=bfd_liveness_detection_gbg_stanza
  |s=log_updown_gbg_stanza
  |s=metric_out_gbg_stanza
  |s=multihop_gbg_stanza
  |s=multipath_gbg_stanza
  |s=remove_private_gbg_stanza
  )
  {gbgs = new BGGR_NullStanza(s);}
  ;
  
/* --- --- --- --- --- --- Protocol->BGP->Null Sub-Stanza Rules --------------------------------------*/
log_updown_bg_stanza returns [String s]
  :
  x=log_updown_common_stanza {s=x;}
  ;
  
traceoptions_bg_stanza returns [String s]
  :
  x=TRACEOPTIONS ignored_substanza {s=x.getText() + "{..}";}
  ;
  
  
/* --- --- --- --- --- --- --- Protocol->BGP->Group->Neighbor Stanza Rules ---------------------------*/
inactive_ngbg_stanza returns [BGGR_NStanza ngbgs]
  :
  INACTIVE COLON x=ngbg_stanza  
  {
    x.set_stanzaStatus(StanzaStatusType.INACTIVE);
    ngbgs=x;
  }
  ;

ngbg_stanza returns [BGGR_NStanza ngbgs]
  :
  (x=export_ngbg_stanza
  |x=family_ngbg_stanza
  |x=import_ngbg_stanza
  |x=local_address_ngbg_stanza
  |x=peer_as_ngbg_stanza
  
  | x=null_ngbg_stanza
  )
  
  {ngbgs = x;}
  ;
  
/* --- --- --- --- --- --- --- Protocol->BGP->Group->Null Stanza Rules -------------------------------*/
bfd_liveness_detection_gbg_stanza  returns [String s]
  :
  x = bfd_liveness_detection_common_stanza {s=x;}
  ;

log_updown_gbg_stanza returns [String s]
  :
  x=log_updown_common_stanza  {s=x;}
  ;
  
metric_out_gbg_stanza returns [String s]
  :
  x=metric_out_common_stanza  {s=x;}
  ;  

multihop_gbg_stanza returns [String s]
  :
  x=multihop_common_stanza  {s=x;}
  ;

multipath_gbg_stanza returns [String s]
  :
  x=MULTIPATH SEMICOLON {s=x.getText();}
  ;

remove_private_gbg_stanza returns [String s]
  :
  x=remove_private_common_stanza {s=x;}
  ;
  
/* --- --- --- --- --- --- --- --- Protocol->BGP->Group->Neighbor Sub-Stanza Rules ------------------*/
export_ngbg_stanza returns [BGGR_NStanza ngbgs]
  :
  x=export_common_stanza {ngbgs = new BGGRN_ExportStanza(x);}
  ; 

family_ngbg_stanza returns [BGGR_NStanza gbgs] 
  :
  x=bgp_family_common_stanza {gbgs = new BGGRN_FamilyStanza(x);}
  ;
  
import_ngbg_stanza returns [BGGR_NStanza ngbgs]
  :
  x=import_common_stanza {ngbgs = new BGGRN_ImportStanza(x);}
  ;  

local_address_ngbg_stanza returns [BGGR_NStanza ngbgs]
  :
  x = local_address_common_stanza {ngbgs = new BGGRN_LocalAddressStanza(x);}
  ; 
  
peer_as_ngbg_stanza returns [BGGR_NStanza ngbgs]
  :
  (x=peer_as_common_stanza) {ngbgs = new BGGRN_PeerAsStanza(x);}
  ;
  
null_ngbg_stanza returns [BGGR_NStanza ngbgs]
  :  
  (s=bfd_liveness_detection_ngbg_stanza
  |s=cluster_ngbg_stanza 
  |s=description_ngbg_stanza
  |s=graceful_restart_ngbg_stanza
  |s=include_mp_next_hop_ngbg_stanza
  |s=hold_time_ngbg_stanza
  |s=local_preference_ngbg_stanza
  |s=metric_out_ngbg_stanza
  |s=multihop_ngbg_stanza
  |s=multipath_ngbg_stanza
  |s=passive_ngbg_stanza
  |s=remove_private_ngbg_stanza
  |s=tcp_mss_ngbg_stanza
  )
  {ngbgs = new BGGRN_NullStanza(s);}
  ;
  
/* --- --- --- --- --- --- --- --- Protocol->BGP->Group->Neighbor->Null Stanza Rules ------------------*/
bfd_liveness_detection_ngbg_stanza returns [String s]
  :
  x = bfd_liveness_detection_common_stanza  {s=x;}
  ;
  
cluster_ngbg_stanza returns [String s]
  :
  x=CLUSTER (ip=IP_ADDRESS) SEMICOLON {s = x.getText();} // TODO [Ask Ari]: Make sure this is ok to ignore
  ;

description_ngbg_stanza returns [String s]
  :
  x = description_common_stanza  {s=x;}
  ;
  
graceful_restart_ngbg_stanza returns [String s]
  :
  x=GRACEFUL_RESTART SEMICOLON {s = x.getText();}
  ;
  
include_mp_next_hop_ngbg_stanza returns [String s]
  :
  x=INCLUDE_MP_NEXT_HOP SEMICOLON {s = x.getText();}
  ;
  
hold_time_ngbg_stanza returns [String s]
  :
  x=HOLD_TIME i=integer SEMICOLON {s = x.getText() + " " + i;}
  ;  
  
local_preference_ngbg_stanza returns [String s]
  :
  x=LOCAL_PREFERENCE y=DEC SEMICOLON {s = x.getText() + " " + y.getText();}
  ;  
  
metric_out_ngbg_stanza returns [String s]
  :
  x=metric_out_common_stanza  {s=x;}
  ;    
  
multipath_ngbg_stanza returns [String s]
  :
  x=MULTIPATH SEMICOLON {s = x.getText();}
  ;

multihop_ngbg_stanza returns [String s]
  :
  x=multihop_common_stanza  {s=x;}
  ;

passive_ngbg_stanza returns [String s]
  :
  x=PASSIVE SEMICOLON {s = x.getText();}
  ;  

remove_private_ngbg_stanza returns [String s]
  :
  x=remove_private_common_stanza {s = x;}
  ;  

tcp_mss_ngbg_stanza returns [String s]
  :
  x=TCP_MSS y=DEC SEMICOLON {s = x.getText() + " " + y.getText();}
  ;  
  
  
/* --- --- --- --- --- Protocol->BGP->Group Sub-Stanza Rules -------------------------------------------------------*/ 