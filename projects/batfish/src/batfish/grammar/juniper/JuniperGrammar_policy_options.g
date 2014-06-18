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

accept_then_t_ps_stanza returns [ThenTPSStanza ats]
  :
  (ACCEPT SEMICOLON) 
                    {
                     ats = new AcceptThenTPSStanza();
                    }
  ;

anon_term_ps_po_stanza returns [TermPSPOStanza t]
@init {
t = new TermPSPOStanza("rule0");
}
  :
  (
    FROM
    (
      (OPEN_BRACE fl=from_t_ps_stanza_list CLOSE_BRACE) 
                                                       {
                                                        for (FromTPSStanza x : fl) {
                                                        	t.processFromStanza(x);
                                                        }
                                                       }
      | (f=from_t_ps_stanza) 
                            {
                             t.processFromStanza(f);
                            }
    )
  )?
  (
    THEN
    (
      (OPEN_BRACE thl=then_t_ps_stanza_list CLOSE_BRACE) 
                                                        {
                                                         for (ThenTPSStanza y : thl) {
                                                         	t.processThenStanza(y);
                                                         }
                                                        }
      | (th=then_t_ps_stanza) 
                             {
                              t.processThenStanza(th);
                             }
    )
  )
  ;

as_path_from_t__ps_stanza returns [FromTPSStanza ftpss]
  :
  (AS_PATH name=VARIABLE SEMICOLON) 
                                   {
                                    ftpss = new ASPathFromTPSStanza(name.getText());
                                   }
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
      | (DOUBLE_QUOTE ( ( (c2=~DOUBLE_QUOTE) 
                                            {
                                             line += c2.getText();
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
                                            }))+ DOUBLE_QUOTE)
      |
      (
        (
          c=
          ~(
            OPEN_BRACKET
            | DOUBLE_QUOTE
           )
        )
        
        {
         line += c.getText();
        }
        ( (c=~SEMICOLON) 
                        {
                         line += c.getText();
                        })*
      )
      
      {
       apos.addMember(line);
      }
    )
    SEMICOLON
  )
  
  {
   pos = apos;
  }
  ;

community_from_t_ps_stanza returns [FromTPSStanza ftpss]
  :
  (COMMUNITY name=VARIABLE SEMICOLON) 
                                     {
                                      ftpss = new CommunityFromTPSStanza(name.getText());
                                     }
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
        ( (c=~SEMICOLON) 
                        {
                         line += c.getText();
                        })*
      )
      
      {
       cpos.addMember(line);
      }
    )
    SEMICOLON
  )
  
  {
   pos = cpos;
  }
  ;

family_from_t__ps_stanza
  :
  FAMILY ~SEMICOLON+ SEMICOLON
  ;

community_then_t_ps_stanza returns [ThenTPSStanza ttpss]
  :
  (
    COMMUNITY
    (
      (SET name=VARIABLE) 
                         {
                          ttpss = new CommunitySetThenTPSStanza(name.getText());
                         }
      | (ADD name=VARIABLE) 
                           {
                            ttpss = new CommunityAddThenTPSStanza(name.getText());
                           }
      | (DELETE name=VARIABLE) 
                              {
                               ttpss = new CommunityDeleteThenTPSStanza(name.getText());
                              }
    )
    SEMICOLON
  )
  ;

from_t_ps_stanza returns [FromTPSStanza ftpss]
  :
  (
    x=as_path_from_t__ps_stanza
    | x=community_from_t_ps_stanza
    | x=neighbor_from_t_ps_stanza
    | x=null_from_t_ps_stanza
    | x=protocol_from_t_ps_stanza
    | x=prefix_list_from_t_ps_stanza
    | x=route_filter_from_t_ps_stanza
  )
  
  {
   ftpss = x;
  }
  ;

from_t_ps_stanza_list returns [List<FromTPSStanza> l = new ArrayList<FromTPSStanza>()]
  :
  ( (x=from_t_ps_stanza) 
                        {
                         l.add(x);
                        })+
  ;

local_preference_then_t_ps_stanza returns [ThenTPSStanza lpts]
  :
  (LOCAL_PREFERENCE x=integer SEMICOLON) 
                                        {
                                         lpts = new LocalPreferenceThenTPSStanza(x);
                                        }
  ;

metric_then_t_ps_stanza returns [ThenTPSStanza ts]
  :
  (METRIC x=integer SEMICOLON) 
                              {
                               ts = new MetricThenTPSStanza(x);
                              }
  ;

neighbor_from_t_ps_stanza returns [FromTPSStanza fs]
  :
  (
    NEIGHBOR
    (
      ( (ip=IP_ADDRESS SEMICOLON) 
                                 {
                                  fs = new NeighborFromTPSStanza(ip.getText());
                                 })
      | ( (IPV6_ADDRESS SEMICOLON) 
                                  {
                                   fs = new IPv6FromTPSStanza();
                                  })
    )
  )
  ;

next_hop_then_t_ps_stanza returns [ThenTPSStanza ts]
  :
  (NEXT_HOP ip=IP_ADDRESS SEMICOLON) 
                                    {
                                     ts = new NextHopThenTPSStanza(ip.getText());
                                    }
  ;

null_from_t_ps_stanza returns [FromTPSStanza ftpss = new NullFromTPSStanza()]
  :
  family_from_t__ps_stanza
  ;

null_po_stanza returns [POStanza pos = new NullPOStanza()]
  :
  x=as_path_po_stanza
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

po_stanza_list returns [List<POStanza> pol = new ArrayList<POStanza>()]
  :
  ( (x=po_stanza) 
                 {
                  pol.add(x);
                 })+
  ;

policy_options_stanza returns [JStanza js]
  :
  (POLICY_OPTIONS OPEN_BRACE l=po_stanza_list CLOSE_BRACE) 
                                                          {
                                                           PolicyOptionsStanza pos = new PolicyOptionsStanza();
                                                           for (POStanza x : l) {
                                                           	pos.processStanza(x);
                                                           }
                                                           js = pos;
                                                          }
  ;

policy_statement_po_stanza returns [POStanza pos]
@init {
PolicyStatementPOStanza pspos;
}
  :
  (
    POLICY_STATEMENT (name=VARIABLE) 
                                    {
                                     pspos = new PolicyStatementPOStanza(name.getText());
                                    }
    OPEN_BRACE ( (x=term_ps_po_stanza) 
                                      {
                                       pspos.addTerm(x);
                                      })* ( (a=anon_term_ps_po_stanza) 
                                                                      {
                                                                       pspos.addTerm(a);
                                                                      })? CLOSE_BRACE
  )
  
  {
   pos = pspos;
  }
  ;

prefix_list_from_t_ps_stanza returns [FromTPSStanza fs]
  :
  (PREFIX_LIST name=VARIABLE SEMICOLON) 
                                       {
                                        fs = new PrefixListFromTPSStanza(name.getText());
                                       }
  ;

prefix_list_po_stanza returns [POStanza pos]
@init {
PrefixListPOStanza plpos;
}
  :
  (
    PREFIX_LIST (name=VARIABLE) 
                               {
                                plpos = new PrefixListPOStanza(name.getText());
                               }
    OPEN_BRACE
    (
      ( (ipmask=IP_ADDRESS_WITH_MASK SEMICOLON) 
                                               {
                                                plpos.addAddress(ipmask.getText());
                                               })+
      | ( (IPV6_ADDRESS_WITH_MASK SEMICOLON) 
                                            {
                                             plpos.setBool(true);
                                            })+
    )
    CLOSE_BRACE
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
    SEMICOLON
  )
  
  {
   fs = ps;
  }
  ;

reject_then_t_ps_stanza returns [ThenTPSStanza tps]
  :
  (REJECT SEMICOLON) 
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
          SEMICOLON
        )
        
        {
         fs = rs;
        }
      )
      | (IPV6_ADDRESS_WITH_MASK ~SEMICOLON+ SEMICOLON) 
                                                      {
                                                       fs = new IPv6FromTPSStanza();
                                                      }
    )
  )
  ;

term_ps_po_stanza returns [TermPSPOStanza t]
  :
  (
    TERM (name=VARIABLE) 
                        {
                         t = new TermPSPOStanza(name.getText());
                        }
    OPEN_BRACE
    (
      FROM
      (
        (OPEN_BRACE fl=from_t_ps_stanza_list CLOSE_BRACE) 
                                                         {
                                                          for (FromTPSStanza x : fl) {
                                                          	t.processFromStanza(x);
                                                          }
                                                         }
        | (f=from_t_ps_stanza) 
                              {
                               t.processFromStanza(f);
                              }
      )
    )?
    (
      THEN
      (
        (OPEN_BRACE thl=then_t_ps_stanza_list CLOSE_BRACE) 
                                                          {
                                                           for (ThenTPSStanza y : thl) {
                                                           	t.processThenStanza(y);
                                                           }
                                                          }
        | (th=then_t_ps_stanza) 
                               {
                                t.processThenStanza(th);
                               }
      )
    )
    CLOSE_BRACE
  )
  ;

then_t_ps_stanza returns [ThenTPSStanza ttpss]
  :
  (
    x=accept_then_t_ps_stanza
    | x=community_then_t_ps_stanza
    | x=local_preference_then_t_ps_stanza
    | x=metric_then_t_ps_stanza
    | x=next_hop_then_t_ps_stanza
    | x=reject_then_t_ps_stanza
  )
  
  {
   ttpss = x;
  }
  ;

then_t_ps_stanza_list returns [List<ThenTPSStanza> l = new ArrayList<ThenTPSStanza>()]
  :
  ( (x=then_t_ps_stanza) 
                        {
                         l.add(x);
                        })+
  ;
