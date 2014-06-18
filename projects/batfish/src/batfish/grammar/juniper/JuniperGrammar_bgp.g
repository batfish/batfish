parser grammar JuniperGrammar_bgp;

@members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "JuniperGrammar_bgp: " + hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	return errors;
}
}

authentication_key_ngb_stanza
  :
  AUTHENTICATION_KEY ~SEMICOLON+ SEMICOLON
  ;

bgp_p_stanza returns [PStanza ps]
  :
  (BGP OPEN_BRACE bl=bp_stanza_list CLOSE_BRACE) 
                                                {
                                                 BGPPStanza bps = new BGPPStanza();
                                                 for (BPStanza x : bl) {
                                                 	bps.processStanza(x);
                                                 }
                                                 ps = bps;
                                                }
  ;

bp_stanza returns [BPStanza bps]
  :
  (
    x=group_bp_stanza
    | x=null_bp_stanza
  )
  
  {
   bps = x;
  }
  ;

bp_stanza_list returns [List<BPStanza> blist=new ArrayList<BPStanza>()]
  :
  ( (x=bp_stanza) 
                 {
                  blist.add(x);
                 })+
  ;

cluster_gb_stanza returns [GBStanza gbs]
  :
  (CLUSTER ip=IP_ADDRESS SEMICOLON) 
                                   {
                                    gbs = new ClusterGBStanza(ip.getText());
                                   }
  ;

damping_bp_stanza
  :
  DAMPING SEMICOLON
  ;

description_ngb_stanza
  :
  DESCRIPTION ~SEMICOLON+ SEMICOLON
  ;

export_gb_stanza returns [GBStanza gbs]
@init {
ExportGBStanza egbs = new ExportGBStanza();
}
  :
  (
    EXPORT
    (
      (name=VARIABLE) 
                     {
                      egbs.addPS(name.getText());
                     }
      | (OPEN_BRACKET ( (name=VARIABLE) 
                                       {
                                        egbs.addPS(name.getText());
                                       })+ CLOSE_BRACKET)
    )
    SEMICOLON
  )
  
  {
   gbs = egbs;
  }
  ;

export_ngb_stanza returns [NGBStanza ngbs]
@init {
ExportNGBStanza engbs = new ExportNGBStanza();
}
  :
  (
    EXPORT
    (
      (name=VARIABLE) 
                     {
                      engbs.addPS(name.getText());
                     }
      | (OPEN_BRACKET ( (name=VARIABLE) 
                                       {
                                        engbs.addPS(name.getText());
                                       })+ CLOSE_BRACKET)
    )
    SEMICOLON
  )
  
  {
   ngbs = engbs;
  }
  ;

family_gb_stanza returns [GBStanza gbs]
  :
  FAMILY
  (
    (INET) 
          {
           gbs = new FamilyGBStanza(true);
          }
    | (INET6) 
             {
              gbs = new FamilyGBStanza(false);
             }
  )
  OPEN_BRACE substanza+ CLOSE_BRACE
  ;

family_ngb_stanza
  :
  FAMILY
  (
    (INET)
    | (INET6)
  )
  OPEN_BRACE substanza+ CLOSE_BRACE
  ;

gb_stanza returns [GBStanza gbs]
  :
  (
    x=cluster_gb_stanza
    | x=export_gb_stanza
    | x=family_gb_stanza
    | x=import_gb_stanza
    | x=local_as_gb_stanza
    | x=neighbor_gb_stanza
    | x=null_gb_stanza
    | x=peer_as_gb_stanza
    | x=type_gb_stanza
  )
  
  {
   gbs = x;
  }
  ;

gb_stanza_list returns [List<GBStanza> glist = new ArrayList<GBStanza>()]
  :
  ( (x=gb_stanza) 
                 {
                  glist.add(x);
                 })+
  ;

group_bp_stanza returns [BPStanza bps]
  :
  (GROUP name=VARIABLE OPEN_BRACE l=gb_stanza_list CLOSE_BRACE) 
                                                               {
                                                                GroupBPStanza gbps = new GroupBPStanza(name.getText());
                                                                for (GBStanza x : l) {
                                                                	gbps.processStanza(x);
                                                                }
                                                                bps = gbps;
                                                               }
  ;

hold_time_ngb_stanza
  :
  HOLD_TIME ~SEMICOLON* SEMICOLON
  ;

import_gb_stanza returns [GBStanza gbs]
@init {
ImportGBStanza ingbs = new ImportGBStanza();
}
  :
  (
    IMPORT
    (
      (name=VARIABLE) 
                     {
                      ingbs.addPS(name.getText());
                     }
      | (OPEN_BRACKET ( (name=VARIABLE) 
                                       {
                                        ingbs.addPS(name.getText());
                                       })+ CLOSE_BRACKET)
    )
    SEMICOLON
  )
  
  {
   gbs = ingbs;
  }
  ;

import_ngb_stanza returns [NGBStanza ngbs]
@init {
ImportNGBStanza ingbs = new ImportNGBStanza();
}
  :
  (
    IMPORT
    (
      (name=VARIABLE) 
                     {
                      ingbs.addPS(name.getText());
                     }
      | (OPEN_BRACKET ( (name=VARIABLE) 
                                       {
                                        ingbs.addPS(name.getText());
                                       })+ CLOSE_BRACKET)
    )
    SEMICOLON
  )
  
  {
   ngbs = ingbs;
  }
  ;

local_address_gb_stanza
  :
  LOCAL_ADDRESS ~SEMICOLON+ SEMICOLON
  ;

local_address_ngb_stanza
  :
  LOCAL_ADDRESS ~SEMICOLON+ SEMICOLON
  ;

local_as_gb_stanza returns [GBStanza gbs]
  :
  (LOCAL_AS num=integer SEMICOLON) 
                                  {
                                   gbs = new LocalASGBStanza(num);
                                  }
  ;

local_as_ngb_stanza returns [NGBStanza ngbs]
  :
  (LOCAL_AS num=integer SEMICOLON) 
                                  {
                                   ngbs = new LocalASNGBStanza(num);
                                  }
  ;

log_updown_bp_stanza
  :
  LOG_UPDOWN SEMICOLON
  ;

multihop_ngb_stanza
  :
  MULTIHOP OPEN_BRACE ~CLOSE_BRACE+ CLOSE_BRACE
  ;

neighbor_gb_stanza returns [GBStanza gbs]
  :
  (
    NEIGHBOR
    (
      ip=IP_ADDRESS
      | ip=IPV6_ADDRESS
    )
    OPEN_BRACE l=ngb_stanza_list CLOSE_BRACE
  )
  
  {
   NeighborGBStanza ngbs = new NeighborGBStanza(ip.getText());
   for (NGBStanza x : l) {
   	ngbs.processStanza(x);
   }
   gbs = ngbs;
  }
  ;

ngb_stanza returns [NGBStanza ngbs]
  :
  (
    x=export_ngb_stanza
    | x=import_ngb_stanza
    | x=local_as_ngb_stanza
    | x=null_ngb_stanza
    | x=peer_as_ngb_stanza
  )
  
  {
   ngbs = x;
  }
  ;

ngb_stanza_list returns [List<NGBStanza> l= new ArrayList<NGBStanza>()]
  :
  ( (x=ngb_stanza) 
                  {
                   l.add(x);
                  })+
  ;

null_bp_stanza returns [BPStanza bps=new NullBPStanza()]
  :
  log_updown_bp_stanza
  | damping_bp_stanza
  ;

null_gb_stanza returns [GBStanza gbs = new NullGBStanza()]
  :
  description_ngb_stanza
  | local_address_gb_stanza
  ;

null_ngb_stanza returns [NGBStanza ngbs = new NullNGBStanza()]
  :
  authentication_key_ngb_stanza
  | description_ngb_stanza
  | family_ngb_stanza
  | hold_time_ngb_stanza
  | local_address_ngb_stanza
  | multihop_ngb_stanza
  | remove_private_ngb_stanza
  ;

peer_as_gb_stanza returns [GBStanza gbs]
  :
  (PEER_AS num=integer SEMICOLON) 
                                 {
                                  gbs = new PeerASGBStanza(num);
                                 }
  ;

peer_as_ngb_stanza returns [NGBStanza ngbs]
  :
  (PEER_AS num=integer SEMICOLON) 
                                 {
                                  ngbs = new PeerASNGBStanza(num);
                                 }
  ;

remove_private_ngb_stanza
  :
  REMOVE_PRIVATE SEMICOLON
  ;

type_gb_stanza returns [GBStanza gbs]
  :
  TYPE
  (
    (INTERNAL) 
              {
               gbs = new TypeGBStanza(false);
              }
    | (EXTERNAL) 
                {
                 gbs = new TypeGBStanza(true);
                }
  )
  SEMICOLON
  ;
