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

autonomous_system_ro_stanza returns [ROStanza ros]
  :
  (AUTONOMOUS_SYSTEM num=integer SEMICOLON) 
                                           {
                                            ros = new AutonomousSystemROStanza(num);
                                           }
  ;

generate_ro_stanza returns [ROStanza ros]
@init {
GenerateROStanza gros = new GenerateROStanza();
}
  :
  (GENERATE OPEN_BRACE ( (l=route_g_ro_stanza) 
                                              {
                                               gros.processRoute(l);
                                              })+ CLOSE_BRACE) 
                                                              {
                                                               ros = gros;
                                                              }
  ;

metric_rg_stanza
  :
  METRIC (~SEMICOLON)+ SEMICOLON
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
  (POLICY p=VARIABLE SEMICOLON) 
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

rg_stanza_list returns [List<RGStanza> rgsl = new ArrayList<RGStanza>()]
  :
  ( (x=rg_stanza) 
                 {
                  rgsl.add(x);
                 })+
  ;

rib_ro_stanza
  :
  RIB VARIABLE OPEN_BRACE substanza+ CLOSE_BRACE
  ;

ro_stanza returns [ROStanza ros]
  :
  (
    x=autonomous_system_ro_stanza
    | x=null_ro_stanza
    | x=router_id_ro_stanza
    | x=static_ro_stanza
    | x=generate_ro_stanza
  )
  
  {
   ros = x;
  }
  ;

ro_stanza_list returns [List<ROStanza> rol = new ArrayList<ROStanza>()]
  :
  ( (x=ro_stanza) 
                 {
                  rol.add(x);
                 })+
  ;

route_g_ro_stanza returns [RouteGROStanza rgros]
  :
  (
    ROUTE ip=IP_ADDRESS_WITH_MASK
    (
      ( (OPEN_BRACE l=rg_stanza_list CLOSE_BRACE) 
                                                 {
                                                  rgros = new RouteGROStanza(ip.getText());
                                                  for (RGStanza rgs : l) {
                                                  	rgros.processStanza(rgs);
                                                  }
                                                 })
      | ( (l2=rg_stanza) 
                        {
                         rgros = new RouteGROStanza(ip.getText());
                         rgros.processStanza(l2);
                        })
    )
  )
  ;

router_id_ro_stanza returns [ROStanza ros]
  :
  (ROUTER_ID id=IP_ADDRESS SEMICOLON) 
                                     {
                                      ros = new RouterIDROStanza(id.getText());
                                     }
  ;

routing_options_stanza returns [JStanza js]
  :
  (ROUTING_OPTIONS OPEN_BRACE rosl=ro_stanza_list CLOSE_BRACE) 
                                                              {
                                                               RoutingOptionsStanza ros = new RoutingOptionsStanza();
                                                               for (ROStanza x : rosl) {
                                                               	ros.processStanza(x);
                                                               }
                                                               js = ros;
                                                              }
  ;

static_ro_stanza returns [ROStanza ros]
@init {
StaticROStanza sros = new StaticROStanza();
String nextHopInt = null;
List<String> nextHopIps = new ArrayList<String>();
}
  :
  (
    STATIC OPEN_BRACE
    (
      (
        ROUTE ip=IP_ADDRESS_WITH_MASK
        (
          (
            OPEN_BRACE
            (
              (
                NEXT_HOP
                (
                  (nexthopip=IP_ADDRESS) 
                                        {
                                         nextHopIps.add(nexthopip.getText());
                                        }
                  | (nexthopint=VARIABLE) 
                                         {
                                          nextHopInt = nexthopint.getText();
                                         }
                  | (OPEN_BRACKET ( (nexthopip2=IP_ADDRESS) 
                                                           {
                                                            nextHopIps.add(nexthopip2.getText());
                                                           })+ CLOSE_BRACKET)
                )
              )
              | (DISCARD) 
                         {
                          nextHopInt = "Null0";
                         }
            )
            SEMICOLON
            (
              (
                INSTALL SEMICOLON
                | READVERTISE SEMICOLON
              )*
              (TAG integer SEMICOLON)?
            )
            CLOSE_BRACE
          )
          |
          (
            (
              (
                NEXT_HOP
                (
                  (nexthopip=IP_ADDRESS) 
                                        {
                                         nextHopIps.add(nexthopip.getText());
                                        }
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
            )
            SEMICOLON
          )
        )
      )
      
      {
       if (nextHopIps.isEmpty()) {
       	sros.addStaticRoute(ip.getText(), null, nextHopInt);
       } else {
       	for (String nip : nextHopIps) {
       		sros.addStaticRoute(ip.getText(), nip, nextHopInt);
       	}
       }
      }
    )+
    CLOSE_BRACE
  )
  
  {
   ros = sros;
  }
  ;
