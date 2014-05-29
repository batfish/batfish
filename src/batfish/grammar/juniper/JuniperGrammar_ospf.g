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

aop_stanza returns [AOPStanza aops]
  :
  (
    x=interface_aop_stanza
    | x=null_aop_stanza
  )
  
  {
   aops = x;
  }
  ;

aop_stanza_list returns [List<AOPStanza> l=new ArrayList<AOPStanza>()]
  :
  ( (x=aop_stanza) 
                  {
                   l.add(x);
                  })+
  ;

area_op_stanza returns [OPStanza ops]
@init {
AreaOPStanza aops = new AreaOPStanza();
}
  :
  (
    AREA
    (
      (i=integer) 
                 {
                  aops.setIDInt(i);
                 }
      | (ip=IP_ADDRESS) 
                       {
                        aops.setIDIP(ip.getText());
                       }
    )
    OPEN_BRACE al=aop_stanza_list CLOSE_BRACE
  )
  
  {
   for (AOPStanza x : al) {
   	aops.processStanza(x);
   }
   ops = aops;
  }
  ;

export_op_stanza returns [OPStanza ops]
@init {
ExportOPStanza eops = new ExportOPStanza();
}
  :
  (
    EXPORT
    (
      (name=VARIABLE) 
                     {
                      eops.addPS(name.getText());
                     }
      | (OPEN_BRACKET ( (name=VARIABLE) 
                                       {
                                        eops.addPS(name.getText());
                                       })+ CLOSE_BRACKET)
    )
    SEMICOLON
  )
  
  {
   ops = eops;
  }
  ;

import_op_stanza
  :
  IMPORT ~SEMICOLON SEMICOLON
  ;

interface_aop_stanza returns [AOPStanza aops]
  :
  (
    INTERFACE iname=VARIABLE
    (
      (OPEN_BRACE substanza+ CLOSE_BRACE)
      | SEMICOLON
    )
  )
  
  {
   aops = new InterfaceAOPStanza(iname.getText());
  }
  ;

network_summary_export_aop_stanza
  :
  NETWORK_SUMMARY_EXPORT ~SEMICOLON+ SEMICOLON
  ;

nssa_aop_stanza
  :
  NSSA SEMICOLON
  ;

null_aop_stanza returns [AOPStanza aops = new NullAOPStanza()]
  :
  network_summary_export_aop_stanza
  | nssa_aop_stanza
  ;

null_op_stanza returns [OPStanza ops = new NullOPStanza()]
  :
  import_op_stanza
  | traceoptions_op_stanza
  ;

op_stanza returns [OPStanza ops]
  :
  (
    x=area_op_stanza
    | x=export_op_stanza
    | x=null_op_stanza
    | x=reference_bandwidth_op_stanza
  )
  
  {
   ops = x;
  }
  ;

op_stanza_list returns [List<OPStanza> opl= new ArrayList<OPStanza>()]
  :
  ( (x=op_stanza) 
                 {
                  opl.add(x);
                 })+
  ;

ospf_p_stanza returns [PStanza ps]
  :
  (OSPF OPEN_BRACE opl=op_stanza_list CLOSE_BRACE) 
                                                  {
                                                   OSPFPStanza ops = new OSPFPStanza();
                                                   for (OPStanza x : opl) {
                                                   	ops.processStanza(x);
                                                   }
                                                   ps = ops;
                                                  }
  ;

reference_bandwidth_op_stanza returns [OPStanza ops]
  :
  (REFERENCE_BANDWIDTH rb=double_num SEMICOLON) 
                                               {
                                                ops = new ReferenceBandwidthOPStanza(rb);
                                               }
  ;

traceoptions_op_stanza
  :
  TRACEOPTIONS OPEN_BRACE substanza+ CLOSE_BRACE
  ;
