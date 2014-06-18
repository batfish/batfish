parser grammar FlatJuniperGrammar_ospf;

@members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "FlatJuniperGrammar_ospf: " + hdr + " " + msg;
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

area_op_stanza returns [OPStanza ops]
@init {
FlatAreaOPStanza aops = new FlatAreaOPStanza();
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
    al=aop_stanza
  )
  
  {
   aops.processStanza(al);
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
    NEWLINE
  )
  
  {
   ops = eops;
  }
  ;

import_op_stanza
  :
  IMPORT ~NEWLINE+ NEWLINE
  ;

interface_aop_stanza returns [AOPStanza aops]
  :
  (INTERFACE iname=VARIABLE ~NEWLINE* NEWLINE) 
                                              {
                                               aops = new InterfaceAOPStanza(iname.getText());
                                              }
  ;

network_summary_export_aop_stanza
  :
  NETWORK_SUMMARY_EXPORT ~NEWLINE+ NEWLINE
  ;

null_aop_stanza returns [AOPStanza aops = new NullAOPStanza()]
  :
  network_summary_export_aop_stanza
  ;

null_op_stanza returns [OPStanza ops = new NullOPStanza()]
  :
  import_op_stanza
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

ospf_p_stanza returns [PStanza ps]
  :
  (OSPF opl=op_stanza) 
                      {
                       FlatOSPFPStanza ops = new FlatOSPFPStanza();
                       ops.processStanza(opl);
                       ps = ops;
                      }
  ;

reference_bandwidth_op_stanza returns [OPStanza ops]
  :
  (REFERENCE_BANDWIDTH rb=double_num NEWLINE) 
                                             {
                                              ops = new ReferenceBandwidthOPStanza(rb);
                                             }
  ;
