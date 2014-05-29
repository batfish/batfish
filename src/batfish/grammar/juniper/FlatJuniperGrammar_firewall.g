parser grammar FlatJuniperGrammar_firewall;

@members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "FlatJuniperGrammar_firewall: " + hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	return errors;
}
}

accept_then_t_ff_stanza returns [ThenTFFStanza ttffs]
  :
  (ACCEPT NEWLINE) 
                  {
                   ttffs = new ThenTFFStanza(ThenTFFType.ACCEPT);
                  }
  ;

count_then_t_ff_stanza
  :
  COUNT ~NEWLINE+ NEWLINE
  ;

destination_address_from_t_ff_stanza returns [FromTFFStanza ftffs]
@init {
DestinationAddressFromTFFStanza dftffs = new DestinationAddressFromTFFStanza();
}
  :
  (DESTINATION_ADDRESS ( (i=IP_ADDRESS_WITH_MASK) 
                                                 {
                                                  dftffs.addAddress(i.getText());
                                                 }) NEWLINE) 
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
      (OPEN_BRACKET ( (p=port) 
                              {
                               dpftffs.addPort(p);
                              })+ CLOSE_BRACKET NEWLINE)
      | (p=port NEWLINE) 
                        {
                         dpftffs.addPort(p);
                        }
    )
  )
  
  {
   ftffs = dpftffs;
  }
  ;

discard_then_t_ff_stanza returns [ThenTFFStanza ttffs]
  :
  (DISCARD NEWLINE) 
                   {
                    ttffs = new ThenTFFStanza(ThenTFFType.DISCARD);
                   }
  ;

filter_f_stanza returns [FlatFilterFStanza ffs]
  :
  (
    FILTER fname=VARIABLE TERM tname=VARIABLE 
                                             {
                                              ffs = new FlatFilterFStanza(fname.getText(), tname.getText());
                                             }
    (
      (FROM ( (f=from_t_ff_stanza) 
                                  {
                                   ffs.processFromStanza(f);
                                  }))
      | (THEN ( (th=then_t_ff_stanza) 
                                     {
                                      ffs.processThenStanza(th);
                                     }))
    )
    
    {
     ffs.processTerm();
    }
  )
  ;

firewall_stanza returns [JStanza js]
  :
  FIREWALL
  (
    (FAMILY INET (l=filter_f_stanza) 
                                    {
                                     FlatFireWallStanza fws = new FlatFireWallStanza();
                                     fws.processStanza(l);
                                     js = fws;
                                    })
    | (FAMILY INET6 ~NEWLINE+ NEWLINE) 
                                      {
                                       js = new NullJStanza();
                                      }
  )
  ;

from_t_ff_stanza returns [FromTFFStanza ftffs]
  :
  (
    x=destination_address_from_t_ff_stanza
    | x=destination_port_from_t_ff_stanza
    | x=protocol_from_t_ff_stanza
    | x=source_address_from_t_ff_stanza
  )
  
  {
   ftffs = x;
  }
  ;

log_then_t_ff_stanza
  :
  LOG NEWLINE
  ;

next_term_then_t_ff_stanza returns [ThenTFFStanza ttffs]
  :
  (NEXT TERM NEWLINE) 
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
      (OPEN_BRACKET ( (p=protocol) 
                                  {
                                   pftffs.addProtocol(p);
                                  })+ CLOSE_BRACKET NEWLINE)
      | (p=protocol NEWLINE) 
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
  SAMPLE NEWLINE
  ;

source_address_from_t_ff_stanza returns [FromTFFStanza ftffs]
@init {
SourceAddressFromTFFStanza sftffs = new SourceAddressFromTFFStanza();
}
  :
  (SOURCE_ADDRESS ( (i=IP_ADDRESS_WITH_MASK) 
                                            {
                                             sftffs.addAddress(i.getText());
                                            }) NEWLINE) 
                                                       {
                                                        ftffs = sftffs;
                                                       }
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
