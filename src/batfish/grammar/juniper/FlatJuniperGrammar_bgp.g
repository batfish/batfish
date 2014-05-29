parser grammar FlatJuniperGrammar_bgp;

@members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "FlatJuniperGrammar_bgp: " + hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	return errors;
}
}

authentication_key_ngb_stanza
  :
  AUTHENTICATION_KEY ~NEWLINE+ NEWLINE
  ;

bgp_p_stanza returns [PStanza ps]
  :
  (bl=bp_stanza) 
                {
                 FlatBGPPStanza bps = new FlatBGPPStanza();
                 bps.processStanza(bl);
                 ps = bps;
                }
  ;

bp_stanza returns [BPStanza bps]
  :
  (
    BGP
    (
      x=group_bp_stanza
      | x=null_bp_stanza
    )
  )
  
  {
   bps = x;
  }
  ;

cluster_gb_stanza returns [GBStanza gbs]
  :
  (CLUSTER ip=IP_ADDRESS NEWLINE) 
                                 {
                                  gbs = new ClusterGBStanza(ip.getText());
                                 }
  ;

damping_bp_stanza
  :
  DAMPING NEWLINE
  ;

description_ngb_stanza
  :
  DESCRIPTION ~NEWLINE+ NEWLINE
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
    NEWLINE
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
    NEWLINE
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
  ~NEWLINE* NEWLINE
  ;

gb_stanza returns [GBStanza gbs]
  :
  (
    x=cluster_gb_stanza
    | x=family_gb_stanza
    | x=local_as_gb_stanza
    | x=neighbor_gb_stanza
    | x=null_gb_stanza
    | x=type_gb_stanza
  )
  
  {
   gbs = x;
  }
  ;

group_bp_stanza returns [BPStanza bps]
  :
  (GROUP name=VARIABLE l=gb_stanza) 
                                   {
                                    FlatGroupBPStanza gbps = new FlatGroupBPStanza(name.getText());
                                    gbps.processStanza(l);
                                    bps = gbps;
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
    NEWLINE
  )
  
  {
   ngbs = ingbs;
  }
  ;

local_address_gb_stanza
  :
  LOCAL_ADDRESS ~NEWLINE+ NEWLINE
  ;

local_address_ngb_stanza
  :
  LOCAL_ADDRESS ~NEWLINE+ NEWLINE
  ;

local_as_gb_stanza returns [GBStanza gbs]
  :
  (LOCAL_AS num=integer NEWLINE) 
                                {
                                 gbs = new LocalASGBStanza(num);
                                }
  ;

local_as_ngb_stanza returns [NGBStanza ngbs]
  :
  (LOCAL_AS num=integer NEWLINE) 
                                {
                                 ngbs = new LocalASNGBStanza(num);
                                }
  ;

log_updown_bp_stanza
  :
  LOG_UPDOWN NEWLINE
  ;

multihop_ngb_stanza
  :
  MULTIHOP ~NEWLINE* NEWLINE
  ;

neighbor_gb_stanza returns [GBStanza gbs]
  :
  (
    NEIGHBOR
    (
      ip=IP_ADDRESS
      | ip=IPV6_ADDRESS
    )
    l=ngb_stanza
  )
  
  {
   FlatNeighborGBStanza ngbs = new FlatNeighborGBStanza(ip.getText());
   ngbs.processStanza(l);
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

null_bp_stanza returns [BPStanza bps=new NullBPStanza()]
  :
  log_updown_bp_stanza
  | damping_bp_stanza
  ;

null_gb_stanza returns [GBStanza gbs = new NullGBStanza()]
  :
  export_gb_stanza
  | local_address_gb_stanza
  ;

null_ngb_stanza returns [NGBStanza ngbs = new NullNGBStanza()]
  :
  authentication_key_ngb_stanza
  | description_ngb_stanza
  | local_address_ngb_stanza
  | multihop_ngb_stanza
  | remove_private_ngb_stanza
  | NEWLINE
  ;

peer_as_ngb_stanza returns [NGBStanza ngbs]
  :
  (PEER_AS num=integer NEWLINE) 
                               {
                                ngbs = new PeerASNGBStanza(num);
                               }
  ;

remove_private_ngb_stanza
  :
  REMOVE_PRIVATE NEWLINE
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
  NEWLINE
  ;
