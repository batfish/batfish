parser grammar JuniperGrammar_interface;

@members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "JuniperGrammar_interface: " + hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	return errors;
}
}

/* --- Interfaces Stanza Rules -----------------------------------------------------------------------*/
interfaces_stanza returns [JStanza js]
@init {
  InterfacesStanza iss = new InterfacesStanza();
}
  :
  INTERFACES OPEN_BRACE 
  (x=interface_stanza {iss.addInterfaceStanza(x);})+
  CLOSE_BRACE
  {js =iss;}
  ;
  
interface_stanza returns [InterfaceStanza is = new InterfaceStanza()]
  :
  (name=VARIABLE {is.set_name(name.getText());})?
  OPEN_BRACE l=if_stanza_list CLOSE_BRACE  
  {
    for (IFStanza ifs : l) {
      is.addIFStanza(ifs);
    }
  }
  ;
  
if_stanza_list returns [List<IFStanza> ifsl = new ArrayList<IFStanza>()]
  :
  (
    (x=if_stanza
    |x=inactive_if_stanza
    ){ifsl.add(x);}
  )+
  ;
  
inactive_if_stanza returns [IFStanza ifs]
  :
  INACTIVE COLON (x=if_stanza) 
  {
    x.set_stanzaStatus(StanzaStatusType.INACTIVE);
    ifs=x;
  }
  ;
     
if_stanza returns [IFStanza ifs]
  :
  (x=disable_if_stanza
  |x=unit_if_stanza
  |x=null_if_stanza
  |x=apply_groups_if_stanza
  )
  { ifs =x; }
  ;
 
 /* --- --- Interfaces Sub-Stanza Rules ---------------------------------------------------------------*/
apply_groups_if_stanza returns [IFStanza ifs] // TODO: FIX!
  :
  ifags = apply_groups_stanza
  {
    IF_NullStanza ifns = new IF_NullStanza("");
    ifs = ifns;
  }
  ;

disable_if_stanza returns [IFStanza ifs]
  :
  (DISABLE SEMICOLON) {ifs = new IF_DisableStanza();}
  ;

unit_if_stanza returns [IFStanza ifs]
@init {
IF_UnitStanza ifus = new IF_UnitStanza();
}
  :
  UNIT 
  (num = integer {ifus.set_num(num);}
  |UNIT_WILDCARD {ifus.set_wildcard(true);}
  )
  OPEN_BRACE 
  (x=u_if_stanza {ifus.addIFUstanza(x);})+
  CLOSE_BRACE 
  {ifs = ifus;}
  ;    

null_if_stanza returns [IFStanza ifs]
  :
  ( s=aggregated_ether_options_if_stanza
  | s=description_if_stanza
  | s=encapsulation_if_stanza
  | s=flexible_vlan_tagging_if_stanza
  | s=gigether_options_if_stanza
  | s=mtu_if_stanza
  | s=traps_if_stanza
  | s=vlan_tagging_if_stanza  
  )
  {ifs=new IF_NullStanza(s);}
  ;
  
/* --- --- --- Interfaces->Null Stanza Rules ---------------------------------------------------------*/ 
aggregated_ether_options_if_stanza returns [String s]
  :
  x=AGGREGATED_ETHER_OPTIONS ignored_substanza {s = x.getText() + " {...}";}
  ;
  
description_if_stanza returns [String s]
  :
  desc=description_common_stanza {s=desc;}
  ;
  
encapsulation_if_stanza returns [String s]
  :
  enc=encapsulation_common_stanza {s=enc;}
  ;
  
flexible_vlan_tagging_if_stanza returns [String s]
  :
  x=FLEXIBLE_VLAN_TAGGING SEMICOLON {s=x.getText();}
  ;
  
gigether_options_if_stanza returns [String s]
  :
  x=GIGETHER_OPTIONS ignored_substanza {s = x.getText() + " {...}";}
  ;
  
mtu_if_stanza returns [String s] 
  :
  mstr = mtu_common_stanza {s=mstr;}
  ;

traps_if_stanza returns [String s]
  :
  x=TRAPS SEMICOLON {s = x.getText();}
  ;
  
vlan_tagging_if_stanza returns [String s]
  :
  x=VLAN_TAGGING SEMICOLON {s = x.getText();}
  ;

/* --- --- --- Interfaces->Unit Stanza Rules ---------------------------------------------------------*/ 
u_if_stanza returns [IF_UStanza ifus]
  :
  (x=apply_groups_u_if_stanza
  |x=family_u_if_stanza
  |x=vlanid_u_if_stanza
  |x=null_u_if_stanza
  )
  {ifus=x;}
  ;
 
/* --- --- --- --- Interfaces->Unit Sub-Stanza Rules -------------------------------------------------*/
apply_groups_u_if_stanza returns [IF_UStanza ifus] // TODO: FIX!
  :
  ifuags = apply_groups_stanza
  {
    IFU_NullStanza ifuns = new IFU_NullStanza("");
    ifus = ifuns;
  }
  ;
  
family_u_if_stanza returns [IF_UStanza ifus]
@init {
   IFU_FamilyStanza ifufs;
}
  :
  (FAMILY
     (ft=BRIDGE 
     |ft=CCC
     |ft=INET
     |ft=INET_VPN
     |ft=INET6
     |ft=INET6_VPN
     |ft=ISO
     |ft=L2_VPN
     |ft=ETHERNET_SWITCHING
     |ft=MPLS
     )
     {ifufs = new IFU_FamilyStanza(FamilyTypeFromString(ft.getText()));}
  )
  (SEMICOLON
  |(OPEN_BRACE x=fam_u_if_stanza_list 
  {
     for (IFU_FamStanza i : x) {
        ifufs.addIFU_FamStanza(i);
     }
  } 
  CLOSE_BRACE) 
  )
  {ifus=ifufs;}
  ;
  
vlanid_u_if_stanza returns [IF_UStanza ifus]
  :  
  VLAN_ID x=integer SEMICOLON {ifus = new IFU_VlanIdStanza(x);}
  ;
  
null_u_if_stanza returns [IF_UStanza ifus]
  :
  (s=description_u_if_stanza
  |s=encapsulation_u_if_stanza
  |s=input_vlan_map_u_if_stanza
  )
  {ifus = new IFU_NullStanza(s);}
  ;

/* --- --- --- --- --- Interfaces->Unit->Null Stanza Rules -------------------------------------------*/  
description_u_if_stanza returns [String s]
  :
  desc=description_common_stanza {s=desc;}
  ;
  
encapsulation_u_if_stanza returns [String s]
  :
  enc=encapsulation_common_stanza {s=enc;}
  ;
   
input_vlan_map_u_if_stanza returns [String s] // TODO [Ask Ari]: probably not supposed to ignore this
  :
  i=INPUT_VLAN_MAP ignored_substanza {s = i.getText() + "{...}";}
  ;
  
/* --- --- --- --- Interfaces->Unit->Family Stanza Rules ---------------------------------------------*/   
fam_u_if_stanza_list returns [List<IFU_FamStanza> ifufsl = new ArrayList<IFU_FamStanza>()]
  :
  (
  (x=fam_u_if_stanza
  |x=inactive_fam_u_if_stanza
  ){ifufsl.add(x);})+
  ;
  
inactive_fam_u_if_stanza returns [IFU_FamStanza ifufs]
  :
  INACTIVE COLON (x=fam_u_if_stanza) 
  {
    x.set_stanzaStatus(StanzaStatusType.INACTIVE);
    ifufs=x;
  }
  ;
  
fam_u_if_stanza returns [IFU_FamStanza ifufs]
  :
  (x=address_fam_u_if_stanza 
  |x=filter_fam_u_if_stanza
  |x=native_vlan_id_fam_u_if_stanza
  |x=vlan_members_fam_u_if_stanza
  |x=null_fam_u_if_stanza
  )
  {ifufs = x;}
  ;  
  
/* --- --- --- --- --- Interfaces->Unit->Family Sub Stanza Rules -------------------------------------*/   
address_fam_u_if_stanza returns [IFU_FamStanza ifufs]  
@init {
  IFUF_AddressStanza ifufas = new IFUF_AddressStanza();
}
  :
  ADDRESS {ifufas = new IFUF_AddressStanza();}
  (x=IP_ADDRESS_WITH_MASK {ifufas.set_address(x.getText());}
  |x=IPV6_ADDRESS_WITH_MASK 
  {
    ifufas.set_stanzaStatus(StanzaStatusType.IPV6);
    ifufas.addIgnoredStatement("Address " + x.getText()); 
  }
  )
  (SEMICOLON
  |ignored_substanza
  )
  {ifufs = ifufas;}
  ;
  
filter_fam_u_if_stanza  returns [IFU_FamStanza ifufs]
@init {
  IFUF_FilterStanza ifuffs = new IFUF_FilterStanza();
}
  :
  FILTER OPEN_BRACE
  ((INACTIVE {ifuffs.set_inputInactive(true);})? INPUT infltr=VARIABLE SEMICOLON {ifuffs.set_inStr(infltr.getText());})?
  ((INACTIVE {ifuffs.set_outputInactive(true);})? OUTPUT outfltr=VARIABLE SEMICOLON {ifuffs.set_outStr(outfltr.getText());})?
  CLOSE_BRACE 
  {ifufs = ifuffs;}
  ;
  
 native_vlan_id_fam_u_if_stanza returns [IFU_FamStanza ifufs]  
  :
  (NATIVE_VLAN_ID id=integer SEMICOLON) {ifufs = new IFUF_NativeVlanIdStanza(id);}
  ; 
  
 vlan_members_fam_u_if_stanza returns [IFU_FamStanza ifufs]  
  :
  VLAN OPEN_BRACE MEMBERS
  s = bracketed_list 
  CLOSE_BRACE
  {ifufs = new IFUF_VlanMembersStanza(s);}
  ; 
  
 null_fam_u_if_stanza returns [IFU_FamStanza ifufs]  
  :
  (s=mtu_fam_u_if_stanza
  |s=port_mode_fam_u_if_stanza
  |s=no_redirects_fam_u_if_stanza
  |s=no_neighbor_learn_fam_u_if_stanza
  |s=primary_fam_u_if_stanza
  |s=rpf_check_fam_u_if_stanza
  |s=targeted_broadcast_fam_u_if_stanza
  ){ifufs=new IFUF_NullStanza(s);}
  ; 
  
/* --- --- --- --- --- --- Interfaces->Unit->Family->Null Stanza Rules -------------------------------*/     
mtu_fam_u_if_stanza returns [String s]
  :
  mstr = mtu_common_stanza  {s=mstr;}
  ;
  
port_mode_fam_u_if_stanza returns [String s] // TODO [P0]: should not be ignored
  :
  x=PORT_MODE mode=VARIABLE {s = x.getText() + " " + mode.getText();}
  ;

no_redirects_fam_u_if_stanza returns [String s]  
  :
  x=NO_REDIRECTS SEMICOLON {s = x.getText();}
  ;
  
no_neighbor_learn_fam_u_if_stanza returns [String s]
  : 
  x=NO_NEIGHBOR_LEARN SEMICOLON {s = x.getText();}
  ;
  
primary_fam_u_if_stanza returns [String s] 
  :
  x=PRIMARY SEMICOLON {s = x.getText();}
  ;

rpf_check_fam_u_if_stanza returns [String s]
  :
  x=RPF_CHECK ignored_substanza {s = x.getText();}
  ;
  
targeted_broadcast_fam_u_if_stanza returns [String s]
  : 
  x=TARGETED_BROADCAST SEMICOLON {s = x.getText();}
  ;