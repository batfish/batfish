parser grammar FlatJuniperGrammar_routing_options;

@members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "FlatJuniperGrammar_routing_options: " + hdr + " "
			+ msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	return errors;
}
}

autonomous_system_ro_stanza returns [ROStanza ros]
  :
  (AUTONOMOUS_SYSTEM num=integer NEWLINE) 
                                         {
                                          ros = new AutonomousSystemROStanza(num);
                                         }
  ;

generate_ro_stanza returns [ROStanza ros]
  :
  (GENERATE ROUTE ip=IP_ADDRESS_WITH_MASK l=rg_stanza) 
                                                      {
                                                       FlatGenerateROStanza gros = new FlatGenerateROStanza(ip.getText());
                                                       gros.processStanza(l);                                                       
                                                       ros = gros;
                                                      }
  ;

metric_rg_stanza
  :
  METRIC ~NEWLINE+ NEWLINE
  ;

null_rg_stanza returns [RGStanza rgs = new NullRGStanza()]
  :
  metric_rg_stanza
  ;

null_ro_stanza returns [ROStanza ros = new NullROStanza()]
  :
  rib_ro_stanza
  ;

policy_rg_stanza returns [RGStanza rgs]
  :
  (POLICY p=VARIABLE NEWLINE) 
                             {
                              rgs = new PolicyRGStanza(p.getText());
                             }
  ;

rg_stanza returns [RGStanza rgs]
  :
  (
    x=null_rg_stanza
    | x=policy_rg_stanza
  )
  
  {
   rgs = x;
  }
  ;

rib_ro_stanza
  :
  RIB VARIABLE ~NEWLINE+ NEWLINE
  ;

ro_stanza returns [ROStanza ros]
  :
  (
    x=autonomous_system_ro_stanza
    | x=generate_ro_stanza
    | x=null_ro_stanza
    | x=router_id_ro_stanza
    | x=static_ro_stanza
  )
  
  {
   ros = x;
  }
  ;

router_id_ro_stanza returns [ROStanza ros]
  :
  (ROUTER_ID id=IP_ADDRESS NEWLINE) 
                                   {
                                    ros = new RouterIDROStanza(id.getText());
                                   }
  ;

routing_options_stanza returns [JStanza js]
  :
  (ROUTING_OPTIONS rosl=ro_stanza) 
                                  {
                                   FlatRoutingOptionsStanza ros = new FlatRoutingOptionsStanza();
                                   ros.processStanza(rosl);
                                   js = ros;
                                  }
  ;

static_ro_stanza returns [ROStanza ros]
@init {
StaticROStanza sros = new StaticROStanza();
String nextHopInt = null;
}
  :
  (
    STATIC
    (
      (
        ROUTE ip=IP_ADDRESS_WITH_MASK
        (
          (
            NEXT_HOP
            (
              nexthopip=IP_ADDESSS
              | (nexthopint=VARIABLE) 
                                     {
                                      nextHopInt = nexthopint.getText();
                                     }
            )
          )
          | (DISCARD) 
                     {
                      nextHopInt = "Null0";
                     }
          | (TAG integer) 
                         {
                          nextHopInt = null;
                         }
        )
      )
      
      {
       String nextHopIp = (nexthopip != null ? nexthopip.getText() : null);
       sros.addStaticRoute(ip.getText(), nextHopIp, nextHopInt);
      }
    )
    NEWLINE
  )
  
  {
   ros = sros;
  }
  ;
