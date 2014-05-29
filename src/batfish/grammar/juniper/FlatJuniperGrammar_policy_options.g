parser grammar FlatJuniperGrammar_policy_options;

@members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "FlatJuniperGrammar_policy_options: " + hdr + " "
			+ msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	return errors;
}
}

accept_then_t_ps_stanza returns [ThenTPSStanza ats]
  :
  (ACCEPT NEWLINE) 
                  {
                   ats = new AcceptThenTPSStanza();
                  }
  ;

anon_term_ps_po_stanza returns [FlatTermPSPOStanza t]
@init {
t = new FlatTermPSPOStanza("rule0");
}
  :
  (
    (FROM ( (f=from_t_ps_stanza) 
                                {
                                 t.processFromStanza(f);
                                }))
    | (THEN ( (th=then_t_ps_stanza) 
                                   {
                                    t.processThenStanza(th);
                                   }))
  )
  ;

as_path_from_t__ps_stanza
  :
  AS_PATH ~NEWLINE+ NEWLINE
  ;

as_path_po_stanza returns [POStanza pos]
@init {
String line = "";
ASPathPOStanza apos;
}
  :
  (
    AS_PATH (name=VARIABLE) 
                           {
                            apos = new ASPathPOStanza(name.getText());
                           }
    (
      (OPEN_BRACKET ( ( (c1=~CLOSE_BRACKET) 
                                           {
                                            line += c1.getText();
                                            /*System.out.println(input.LT(-1).getText());
                                            System.out.println(input.LT(-1).getTokenIndex());
                                            System.out.println(input.LT(1).getText());
                                            System.out.println(input.LT(1).getType());
                                            System.out.println(input.LT(1).getChannel());
                                            System.out.println(input.LT(1).getTokenIndex());*/
                                            List<Token> tokens = ((CommonTokenStream) input).getTokens(input.LT(-1)
                                            		.getTokenIndex(), input.index());
                                            boolean ws = false;
                                            for (Token t : tokens) {
                                            	if (t.getChannel() == Token.HIDDEN_CHANNEL) {
                                            		//System.out.println(token.getType());
                                            		ws = true;
                                            	}
                                            }
                                            if (ws) {
                                            	apos.addMember(line);
                                            	line = "";
                                            }
                                           }))+ CLOSE_BRACKET)
      |
      (
        (c=~OPEN_BRACKET) 
                         {
                          line += c.getText();
                         }
        ( (c=~NEWLINE) 
                      {
                       line += c.getText();
                      })*
      )
      
      {
       apos.addMember(line);
      }
    )
    NEWLINE
  )
  
  {
   pos = apos;
  }
  ;

community_from_t_ps_stanza
  :
  COMMUNITY ~NEWLINE+ NEWLINE
  ;

community_po_stanza returns [POStanza pos]
@init {
String line = "";
CommunityPOStanza cpos;
}
  :
  (
    COMMUNITY (name=VARIABLE) 
                             {
                              cpos = new CommunityPOStanza(name.getText());
                             }
    MEMBERS
    (
      (OPEN_BRACKET ( ( (c1=~CLOSE_BRACKET) 
                                           {
                                            line += c1.getText();
                                            /*System.out.println(input.LT(-1).getText());
                                            System.out.println(input.LT(-1).getTokenIndex());
                                            System.out.println(input.LT(1).getText());
                                            System.out.println(input.LT(1).getType());
                                            System.out.println(input.LT(1).getChannel());
                                            System.out.println(input.LT(1).getTokenIndex());*/
                                            List<Token> tokens = ((CommonTokenStream) input).getTokens(input.LT(-1)
                                            		.getTokenIndex(), input.index());
                                            boolean ws = false;
                                            for (Token t : tokens) {
                                            	if (t.getChannel() == Token.HIDDEN_CHANNEL) {
                                            		//System.out.println(token.getType());
                                            		ws = true;
                                            	}
                                            }
                                            if (ws) {
                                            	cpos.addMember(line);
                                            	line = "";
                                            }
                                           }))+ CLOSE_BRACKET)
      |
      (
        (c=~OPEN_BRACKET) 
                         {
                          line += c.getText();
                         }
        ( (c=~NEWLINE) 
                      {
                       line += c.getText();
                      })*
      )
      
      {
       cpos.addMember(line);
      }
    )
    NEWLINE
  )
  
  {
   pos = cpos;
  }
  ;

community_then_t_ps_stanza
  :
  COMMUNITY ~NEWLINE+ NEWLINE
  ;

from_t_ps_stanza returns [FromTPSStanza ftpss]
  :
  (
    x=neighbor_from_t_ps_stanza
    | x=null_from_t_ps_stanza
    | x=protocol_from_t_ps_stanza
    | x=prefix_list_from_t_ps_stanza
    | x=route_filter_from_t_ps_stanza
  )
  
  {
   ftpss = x;
  }
  ;

local_preference_then_t_ps_stanza returns [ThenTPSStanza lpts]
  :
  (LOCAL_PREFERENCE x=integer NEWLINE) 
                                      {
                                       lpts = new LocalPreferenceThenTPSStanza(x);
                                      }
  ;

metric_then_t_ps_stanza returns [ThenTPSStanza ts]
  :
  (METRIC x=integer NEWLINE) 
                            {
                             ts = new MetricThenTPSStanza(x);
                            }
  ;

neighbor_from_t_ps_stanza returns [FromTPSStanza fs]
  :
  (NEIGHBOR ip=IP_ADDRESS NEWLINE) 
                                  {
                                   fs = new NeighborFromTPSStanza(ip.getText());
                                  }
  ;

next_hop_then_t_ps_stanza returns [ThenTPSStanza ts]
  :
  (NEXT_HOP ip=IP_ADDRESS NEWLINE) 
                                  {
                                   ts = new NextHopThenTPSStanza(ip.getText());
                                  }
  ;

null_from_t_ps_stanza returns [FromTPSStanza ftpss = new NullFromTPSStanza()]
  :
  as_path_from_t__ps_stanza
  | community_from_t_ps_stanza
  ;

null_po_stanza returns [POStanza pos = new NullPOStanza()]
  :
  x=as_path_po_stanza
  ;

null_then_t_ps_stanza returns [ThenTPSStanza ttpss = new NullThenTPSStanza()]
  :
  community_then_t_ps_stanza
  ;

po_stanza returns [POStanza pos]
  :
  (
    x=community_po_stanza
    | x=null_po_stanza
    | x=policy_statement_po_stanza
    | x=prefix_list_po_stanza
  )
  
  {
   pos = x;
  }
  ;

policy_options_stanza returns [JStanza js]
  :
  (POLICY_OPTIONS l=po_stanza) 
                              {
                               FlatPolicyOptionsStanza pos = new FlatPolicyOptionsStanza();
                               pos.processStanza(l);
                               js = pos;
                              }
  ;

policy_statement_po_stanza returns [POStanza pos]
@init {
FlatPolicyStatementPOStanza pspos;
}
  :
  (
    POLICY_STATEMENT (name=VARIABLE) 
                                    {
                                     pspos = new FlatPolicyStatementPOStanza(name.getText());
                                    }
    (
      ( (x=term_ps_po_stanza) 
                             {
                              pspos.addTerm(x);
                             })
      | ( (a=anon_term_ps_po_stanza) 
                                    {
                                     pspos.addTerm(a);
                                    })
    )
  )
  
  {
   pos = pspos;
  }
  ;

prefix_list_from_t_ps_stanza returns [FromTPSStanza fs]
  :
  (PREFIX_LIST name=VARIABLE NEWLINE) 
                                     {
                                      fs = new PrefixListFromTPSStanza(name.getText());
                                     }
  ;

prefix_list_po_stanza returns [POStanza pos]
@init {
FlatPrefixListPOStanza plpos;
}
  :
  (
    PREFIX_LIST (name=VARIABLE) 
                               {
                                plpos = new FlatPrefixListPOStanza(name.getText());
                               }
    (
      ( (ipmask=IP_ADDRESS_WITH_MASK NEWLINE) 
                                             {
                                              plpos.addAddress(ipmask.getText());
                                             })
      | ( (IPV6_ADDRESS_WITH_MASK NEWLINE) 
                                          {
                                           plpos.setBool(true);
                                          })
    )
  )
  
  {
   pos = plpos;
  }
  ;

protocol_from_t_ps_stanza returns [FromTPSStanza fs]
@init {
ProtocolFromTPSStanza ps = new ProtocolFromTPSStanza();
}
  :
  (
    PROTOCOL
    (
      (
        p=VARIABLE
        | p=BGP
        | p=OSPF
        | p=STATIC
      )
      
      {
       ps.addProtocol(p.getText());
      }
      |
      (
        OPEN_BRACKET
        (
          (
            p1=VARIABLE
            | p1=BGP
            | p1=OSPF
            | p1=STATIC
          )
          
          {
           ps.addProtocol(p1.getText());
          }
        )+
        CLOSE_BRACKET
      )
    )
    NEWLINE
  )
  
  {
   fs = ps;
  }
  ;

reject_then_t_ps_stanza returns [ThenTPSStanza tps]
  :
  (REJECT NEWLINE) 
                  {
                   tps = new RejectThenTPSStanza();
                  }
  ;

route_filter_from_t_ps_stanza returns [FromTPSStanza fs]
@init {
RouteFilterFromTPSStanza rs;
}
  :
  (
    ROUTE_FILTER
    (
      (
        (
          (ipmask=IP_ADDRESS_WITH_MASK) 
                                       {
                                        String[] tmp = ipmask.getText().split("/");
                                        int mask = Integer.parseInt(tmp[1]);
                                        rs = new RouteFilterFromTPSStanza(tmp[0], mask);
                                       }
          (
            (EXACT) 
                   {
                    rs.addRange(new SubRange(mask, mask));
                   }
            | (ORLONGER) 
                        {
                         rs.addRange(new SubRange(mask, 32));
                        }
            | (THROUGH ipmask2=IP_ADDRESS_WITH_MASK) 
                                                    {
                                                     String[] tmp2 = ipmask2.getText().split("/");
                                                     int mask2 = Integer.parseInt(tmp2[1]);
                                                     rs.addSecondIP(tmp2[0], mask2);
                                                    }
            | (PREFIX_LENGTH_RANGE FORWARD_SLASH r1=integer DASH FORWARD_SLASH r2=integer) 
                                                                                          {
                                                                                           rs.addRange(new SubRange(r1, r2));
                                                                                          }
          )
          NEWLINE
        )
        
        {
         fs = rs;
        }
      )
      | (IPV6_ADDRESS_WITH_MASK ~NEWLINE+ NEWLINE) 
                                                  {
                                                   fs = new IPv6FromTPSStanza();
                                                  }
    )
  )
  ;

term_ps_po_stanza returns [FlatTermPSPOStanza t]
  :
  (
    TERM
    (
      (name=VARIABLE)
      | (name=DEC)
    )
    
    {
     t = new FlatTermPSPOStanza(name.getText());
    }
    (
      (FROM ( (f=from_t_ps_stanza) 
                                  {
                                   t.processFromStanza(f);
                                  }))
      | (THEN ( (th=then_t_ps_stanza) 
                                     {
                                      t.processThenStanza(th);
                                     }))
    )
  )
  ;

then_t_ps_stanza returns [ThenTPSStanza ttpss]
  :
  (
    x=accept_then_t_ps_stanza
    | x=local_preference_then_t_ps_stanza
    | x=metric_then_t_ps_stanza
    | x=next_hop_then_t_ps_stanza
    | x=null_then_t_ps_stanza
    | x=reject_then_t_ps_stanza
  )
  
  {
   ttpss = x;
  }
  ;
