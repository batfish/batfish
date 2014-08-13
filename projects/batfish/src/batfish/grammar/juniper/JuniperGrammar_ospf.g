parser grammar JuniperGrammar_ospf;

@members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "JuniperGrammar_ospf: " + hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	return errors;
}
}

/* --- --- --- Protocol->OSPF Stanza Rules -----------------------------------------------------------*/
ospf_p_stanza returns [PStanza ps]
@init {
  OSPFStanza os = new OSPFStanza();
}
  :
  (OSPF OPEN_BRACE (x=op_stanza {os.addOPStanza(x);})+ CLOSE_BRACE) 
  ;
    
/* --- --- --- --- Protocol->OSPF Sub-Stanza Rules ---------------------------------------------------*/    
op_stanza returns [OPStanza ops]
  :
  (x=area_op_stanza
  |x=export_op_stanza
  |x=reference_bandwidth_op_stanza
  |x=null_op_stanza
  )
  { ops =x; }
  ;

/* --- --- --- --- --- Protocol->OSPF->Area Stanza Rules ---------------------------------------------*/
area_op_stanza returns [OPStanza ops]
@init {
OP_AreaStanza aops = new OP_AreaStanza();
}
  :
  (AREA
  (i=integer {aops.set_areaId(i);}
  //|ip=IP_ADDRESS {aops.set_areaIp(-1);}// TODO: Juniper docs say this can be IP but representation is set for int
  )
  OPEN_BRACE (x=aop_stanza {aops.addOPARStanza(x);})+ CLOSE_BRACE
  )
  {ops = aops;}
  ;
  
/* --- --- --- --- --- Protocol->OSPF->Export Stanza Rules -------------------------------------------*/
export_op_stanza returns [OPStanza ops]
@init {
OP_ExportStanza eops = new OP_ExportStanza();
}
  :
  (EXPORT
  (
    (name=VARIABLE) {eops.addPolicy(name.getText());}
   |(x=bracketed_list)
    {
      for (String s: x) {
        eops.addPolicy(s);
      }
    }
  )
  SEMICOLON
  )
  {ops = eops;}
  ;

/* --- --- --- --- --- Protocol->OSPF->Reference Bandwidth Stanza Rules ------------------------------*/
reference_bandwidth_op_stanza returns [OPStanza ops]
  :
  (REFERENCE_BANDWIDTH rb=double_num SEMICOLON)  {ops = new OP_ReferenceBandwidthStanza(rb);}  
  ;

/* --- --- --- --- --- Protocol->OSPF->Null Stanza Rules ---------------------------------------------*/
null_op_stanza returns [OPStanza ops]
  :
  (s=import_op_stanza
  |s=traceoptions_op_stanza)
  {ops = new OP_NullStanza(s);}
  ;
  
/* --- --- --- --- --- --- Protocol->OSPF->Area Sub-Stanza Rules -------------------------------------*/
aop_stanza returns [OP_ARStanza aops]
  :
  (x=interface_aop_stanza
  |x=null_aop_stanza
  )
  {aops =x;}
  ;
  
/* --- --- --- --- --- --- Protocol->OSPF->Null Sub-Stanza Rules -------------------------------------*/
import_op_stanza returns [String s]
  :
  x=IMPORT ignored_substanza {s = x.getText() + "{...}";}
  ;
  
traceoptions_op_stanza returns [String s]
  :
  x=TRACEOPTIONS ignored_substanza {s = x.getText() + "{...}";}
  ;
  
/* --- --- --- --- --- --- --- Protocol->OSPF->Area->Interface Stanza Rules --------------------------*/
interface_aop_stanza returns [OP_ARStanza aops]
  :
  (INTERFACE iname=VARIABLE ignored_substanza SEMICOLON) {aops = new OPAR_InterfaceStanza(iname.getText());}
  // TODO [Ask Ari]: shoule we really be ignoring this?
  ;
  
/* --- --- --- --- --- --- --- Protocol->OSPF->Area->Null Stanza Rules -------------------------------*/
null_aop_stanza returns [OP_ARStanza aops]
  :
  (s=network_summary_export_aop_stanza
  |s=nssa_aop_stanza
  )
  {aops = new OPAR_NullStanza(s);}
  ;

/* --- --- --- --- --- --- --- --- Protocol->OSPF->Area->Null Sub-Stanza Rules -----------------------*/
network_summary_export_aop_stanza returns [String s]
  :
  x=NETWORK_SUMMARY_EXPORT ignored_substanza SEMICOLON {s = x.getText() + "{...}";}
  ;

nssa_aop_stanza returns [String s]
  :
  x=NSSA SEMICOLON {s = x.getText();}
  ;
  