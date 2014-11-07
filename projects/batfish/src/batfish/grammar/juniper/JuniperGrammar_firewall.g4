parser grammar JuniperGrammar_firewall;

@members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "JuniperGrammar_firewall: " + hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	return errors;
}
}

accept_then_t_ff_stanza returns [ThenTFFStanza ttffs]
  :
  (ACCEPT SEMICOLON) 
                    {
                     ttffs = new ThenTFFStanza(ThenTFFType.ACCEPT);
                    }
  ;

count_then_t_ff_stanza
  :
  COUNT ~SEMICOLON SEMICOLON
  ;

destination_address_from_t_ff_stanza returns [FromTFFStanza ftffs]
@init {
DestinationAddressFromTFFStanza dftffs = new DestinationAddressFromTFFStanza();
}
  :
  (DESTINATION_ADDRESS OPEN_BRACE ( (i=IP_ADDRESS_WITH_MASK SEMICOLON) 
                                                                      {
                                                                       dftffs.addAddress(i.getText());
                                                                      })+ CLOSE_BRACE) 
                                                                                      {
                                                                                       ftffs = dftffs;
                                                                                      }
  ;

destination_port_from_t_ff_stanza returns [FromTFFStanza ftffs]
@init {
DestinationPortFromTFFStanza dpftffs = new DestinationPortFromTFFStanza();
}
  :
  (
    DESTINATION_PORT
    (
    
      (x=bracketed_list SEMICOLON) // TODO: Semicolon?
        {
          for (String s: x) {
            dpftffs.addPort(Integer.parseInt(s));
          }
        }
                              
      | (p=port SEMICOLON) {dpftffs.addPort(p);}
    )
  )
  
  {
   ftffs = dpftffs;
  }
  ;

discard_then_t_ff_stanza returns [ThenTFFStanza ttffs]
  :
  (DISCARD SEMICOLON) 
                     {
                      ttffs = new ThenTFFStanza(ThenTFFType.DISCARD);
                     }
  ;

filter_f_stanza returns [FilterFStanza ffs]
  :
  (
    FILTER (name=VARIABLE) 
                          {
                           ffs = new FilterFStanza(name.getText());
                          }
    OPEN_BRACE ( (t=term_f_f_stanza) 
                                    {
                                     ffs.processTerm(t);
                                    })+ CLOSE_BRACE
  )
  ;

filter_f_stanza_list returns [List<FilterFStanza> fsl = new ArrayList<FilterFStanza>()]
  :
  ( (x=filter_f_stanza) 
                       {
                        fsl.add(x);
                       })+
  ;

firewall_stanza returns [JStanza js]
  :
  FIREWALL OPEN_BRACE
  (
    (
      FAMILY INET OPEN_BRACE (l=filter_f_stanza_list) 
                                                     {
                                                      FireWallStanza fws = new FireWallStanza();
                                                      for (FilterFStanza fs : l) {
                                                      	fws.processStanza(fs);
                                                      }
                                                      js = fws;
                                                     }
      CLOSE_BRACE (FAMILY INET6 OPEN_BRACE substanza+ CLOSE_BRACE)?
    )
    | ( (nl=filter_f_stanza_list) 
                                 {
                                  FireWallStanza fws = new FireWallStanza();
                                  for (FilterFStanza fs : nl) {
                                  	fws.processStanza(fs);
                                  }
                                  js = fws;
                                 })
  )
  CLOSE_BRACE
  ;

from_t_ff_stanza returns [FromTFFStanza ftffs]
  :
  (
    x=destination_address_from_t_ff_stanza
    | x=destination_port_from_t_ff_stanza
    | x=icmp_type_from_t_ff_stanza
    | x=protocol_from_t_ff_stanza
    | x=source_address_from_t_ff_stanza
    | x=source_port_from_t_ff_stanza
  )
  
  {
   ftffs = x;
  }
  ;

from_t_ff_stanza_list returns [List<FromTFFStanza> l = new ArrayList<FromTFFStanza>()]
  :
  ( (x=from_t_ff_stanza) 
                        {
                         l.add(x);
                        })+
  ;

icmp_type_from_t_ff_stanza returns [FromTFFStanza ftffs]
@init {
ProtocolFromTFFStanza pftffs = new ProtocolFromTFFStanza();
}
  :  
  (ICMP_TYPE x=bracketed_list CLOSE_BRACKET SEMICOLON) 
                                                                  {
                                                                   pftffs.addProtocol(1);
                                                                   ftffs = pftffs;
                                                                  }
  ;

log_then_t_ff_stanza
  :
  LOG SEMICOLON
  ;

next_term_then_t_ff_stanza returns [ThenTFFStanza ttffs]
  :
  (NEXT TERM SEMICOLON) 
                       {
                        ttffs = new ThenTFFStanza(ThenTFFType.NEXT_TERM);
                       }
  ;

null_then_t_ff_stanza returns [ThenTFFStanza t = new ThenTFFStanza(ThenTFFType.NULL)]
  :
  count_then_t_ff_stanza
  | log_then_t_ff_stanza
  | sample_then_t_ff_stanza
  ;

port returns [int i]
  :
  (d=DEC 
        {
         i = Integer.parseInt(d.getText());
        })
  | (BGP 
        {
         i = 179;
        })
  | (DOMAIN 
           {
            i = 53;
           })
  | (FTP 
        {
         i = 20;
        })
  | (NTP 
        {
         i = 123;
        })
  | (SNMP 
         {
          i = 161;
         })
  | (SSH 
        {
         i = 22;
        })
  | (TACACS 
           {
            i = 49;
           })
  | (TELNET 
           {
            i = 23;
           })
  | (TFTP 
         {
          i = 69;
         })
  ;

protocol returns [int i]
  :
  (d=DEC 
        {
         i = Integer.parseInt(d.getText());
        })
  | (ICMP 
         {
          i = 1;
         })
  | (IGMP 
         {
          i = 2;
         })
  | (IP 
       {
        i = 0;
       })
  | (OSPF 
         {
          i = 89;
         })
  | (PIM 
        {
         i = 103;
        })
  | (TCP 
        {
         i = 6;
        })
  | (UDP 
        {
         i = 17;
        })
  ;

protocol_from_t_ff_stanza returns [FromTFFStanza ftffs]
@init {
ProtocolFromTFFStanza pftffs = new ProtocolFromTFFStanza();
}
  :
  (
    PROTOCOL
    (
        (x=bracketed_list SEMICOLON) // TODO: Semicolon?
        {
          for (String s: x) {
            pftffs.addProtocol(Integer.parseInt(s));
          }
        }
      | (p=protocol SEMICOLON) 
                              {
                               pftffs.addProtocol(p);
                              }
    )
  )
  
  {
   ftffs = pftffs;
  }
  ;

sample_then_t_ff_stanza
  :
  SAMPLE SEMICOLON
  ;

source_address_from_t_ff_stanza returns [FromTFFStanza ftffs]
@init {
SourceAddressFromTFFStanza sftffs = new SourceAddressFromTFFStanza();
}
  :
  (SOURCE_ADDRESS OPEN_BRACE ( (i=IP_ADDRESS_WITH_MASK 
                                                      {
                                                       sftffs.addAddress(i.getText());
                                                      }
        ( (EXCEPT) 
                  {
                   sftffs.addExceptAddress(i.getText());
                  })? SEMICOLON))+ CLOSE_BRACE) 
                                               {
                                                ftffs = sftffs;
                                               }
  ;

source_port_from_t_ff_stanza returns [FromTFFStanza ftffs]
@init {
SourcePortFromTFFStanza spftffs = new SourcePortFromTFFStanza();
}
  :
  (
    SOURCE_PORT
    (
      (x=bracketed_list SEMICOLON) // TODO: Semicolon?
        {
          for (String s: x) {
            spftffs.addPort(Integer.parseInt(s));
          }
        }
      | (p=port SEMICOLON) 
                          {
                           spftffs.addPort(p);
                          }
    )
  )
  
  {
   ftffs = spftffs;
  }
  ;

term_f_f_stanza returns [TermFFStanza t]
  :
  (
    TERM (name=VARIABLE) 
                        {
                         t = new TermFFStanza(name.getText());
                        }
    OPEN_BRACE
    (
      FROM
      (
        (OPEN_BRACE fl=from_t_ff_stanza_list CLOSE_BRACE) 
                                                         {
                                                          for (FromTFFStanza x : fl) {
                                                          	t.processFromStanza(x);
                                                          }
                                                         }
        | (f=from_t_ff_stanza) 
                              {
                               t.processFromStanza(f);
                              }
      )
    )?
    (
      THEN
      (
        (OPEN_BRACE thl=then_t_ff_stanza_list CLOSE_BRACE) 
                                                          {
                                                           for (ThenTFFStanza y : thl) {
                                                           	t.processThenStanza(y);
                                                           }
                                                          }
        | (th=then_t_ff_stanza) 
                               {
                                t.processThenStanza(th);
                               }
      )
    )
    CLOSE_BRACE
  )
  ;

then_t_ff_stanza returns [ThenTFFStanza ttffs]
  :
  (
    x=accept_then_t_ff_stanza
    | x=discard_then_t_ff_stanza
    | x=next_term_then_t_ff_stanza
    | x=null_then_t_ff_stanza
  )
  
  {
   ttffs = x;
  }
  ;

then_t_ff_stanza_list returns [List<ThenTFFStanza> l = new ArrayList<ThenTFFStanza>()]
  :
  ( (x=then_t_ff_stanza) 
                        {
                         l.add(x);
                        })+
  ;
