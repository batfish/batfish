parser grammar JuniperGrammar_protocols;

@members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "JuniperGrammar_protocols: " + hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	return errors;
}
}

/* --- Protocol Stanza Rules -------------------------------------------------------------------------*/

protocols_stanza returns [JStanza js]
@init {
  ProtocolsStanza ps = new ProtocolsStanza();
}
  :
  (PROTOCOLS OPEN_BRACE (x=p_stanza {ps.addPStanza(x);})+ CLOSE_BRACE)
  {js = ps;}
  ;
  
/* --- --- Protocol Sub-Stanza Rules -----------------------------------------------------------------*/
p_stanza returns [PStanza ps]
  :
  (x=bgp_p_stanza
  |x=ospf_p_stanza
  |x=null_p_stanza
  )
  { ps =x; }
  ;

/* --- --- --- Protocol->Null Stanza Rules -------------------------------------------------------------*/

null_p_stanza returns [PStanza ps]
  :
  (s=bfd_p_stanza
  |s=connections_p_stanza
  |s=igmp_p_stanza
  |s=igmp_snooping_p_stanza
  |s=isis_p_stanza
  |s=l2_circuit_p_stanza
  |s=ldp_p_stanza
  |s=lldp_p_stanza
  |s=lldp_med_p_stanza
  |s=mld_p_stanza
  |s=mpls_p_stanza
  |s=msdp_p_stanza
  |s=ospf3_p_stanza
  |s=pim_p_stanza
  |s=router_advertisement_p_stanza
  |s=rstp_p_stanza
  |s=rsvp_p_stanza
  |s=vstp_p_stanza)
  {ps = new P_NullStanza(s);}
  ;
  
bfd_p_stanza returns [String s]
  :
  x=BFD ignored_substanza {s = x.getText() + "{...}";}
  ;
  
connections_p_stanza returns [String s]
  :
  x=CONNECTIONS ignored_substanza {s = x.getText() + "{...}";}
  ;
  
igmp_p_stanza returns [String s]
  :
  x=IGMP ignored_substanza {s = x.getText() + "{...}";}
  ;
  
igmp_snooping_p_stanza returns [String s]
  :
  x=IGMP_SNOOPING ignored_substanza {s = x.getText() + "{...}";}
  ;
  
isis_p_stanza returns [String s]
  :
  x=ISIS ignored_substanza {s = x.getText() + "{...}";}
  ;
  
l2_circuit_p_stanza returns [String s]
  :
  x=L2_CIRCUIT ignored_substanza {s = x.getText() + "{...}";}
  ;

ldp_p_stanza returns [String s]
  :
  x=LDP ignored_substanza {s = x.getText() + "{...}";}
  ;

lldp_p_stanza returns [String s]
  :
  x=LLDP ignored_substanza {s = x.getText() + "{...}";}
  ;

lldp_med_p_stanza returns [String s]
  :
  x=LLDP_MED ignored_substanza {s = x.getText() + "{...}";}
  ;

mld_p_stanza returns [String s]
  :
  x=MLD ignored_substanza {s = x.getText() + "{...}";}
  ;

mpls_p_stanza returns [String s]
  :
  x=MPLS ignored_substanza {s = x.getText() + "{...}";}
  ;

msdp_p_stanza returns [String s]
  :
  x=MSDP ignored_substanza {s = x.getText() + "{...}";}
  ;
  
ospf3_p_stanza returns [String s]
  :
  x=OSPF3 ignored_substanza {s = x.getText() + "{...}";}
  ;

pim_p_stanza returns [String s]
  :
  x=PIM ignored_substanza {s = x.getText() + "{...}";}
  ;

router_advertisement_p_stanza returns [String s]
  :
  x=ROUTER_ADVERTISEMENT ignored_substanza {s = x.getText() + "{...}";}
  ;
  
rstp_p_stanza returns [String s]
  :
  x=RSTP ignored_substanza {s = x.getText() + "{...}";}
  ;
  
rsvp_p_stanza returns [String s]
  :
  x=RSVP ignored_substanza {s = x.getText() + "{...}";}
  ;
  
vstp_p_stanza returns [String s]
  :
  x=VSTP ignored_substanza {s = x.getText() + "{...}";}
  ;