parser grammar CiscoGrammar_bgp;

@members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "CiscoGrammar_bgp: " + hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	return errors;
}
}

address_family_rb_stanza returns [RBStanza rbs]
@init {
AFType t = null;
}
  :
  (
    ADDRESS_FAMILY
    (
      (IPV4 
           {
            t = AFType.IPV4;
           })
      | (IPV6 
             {
              t = AFType.IPV6;
             })
    )
    MULTICAST? NEWLINE afsl=af_stanza_list EXIT_ADDRESS_FAMILY NEWLINE closing_comment
  )
  
  {
   AddressFamilyRBStanza afrbs = new AddressFamilyRBStanza(t);
   for (AFStanza afs : afsl) {
   	afrbs.processStanza(afs);
   }
   rbs = afrbs;
  }
  ;

address_family_rb_stanza_list returns [List<RBStanza> rbsl= new ArrayList<RBStanza>()]
  :
  (s=address_family_rb_stanza 
                             {
                              rbsl.add(s);
                             })*
  ;

af_stanza_list returns [List<AFStanza> afsl = new ArrayList<AFStanza>()]
  :
  ( (x=af_stanza) 
                 {
                  afsl.add(x);
                 })+
  ;

af_stanza returns [AFStanza s]
  :
  (
    x=aggregate_address_af_stanza
    | x=default_metric_af_stanza
    | x=neighbor_activate_af_stanza
    | x=neighbor_default_originate_af_stanza
    | x=neighbor_peer_group_assignment_af_stanza
    | x=neighbor_prefix_list_af_stanza
    | x=neighbor_route_map_af_stanza
    | x=neighbor_route_reflector_client_af_stanza
    | x=neighbor_send_community_af_stanza
    | x=network_af_stanza
    | x=null_af_stanza
    | x=redistribute_connected_af_stanza
    | x=redistribute_static_af_stanza
  )
  
  {
   s = x;
  }
  ;

aggregate_address_af_stanza returns [AFStanza afs]
@init {
boolean summaryOnly = false;
}
  :
  (AGGREGATE_ADDRESS network=IP_ADDRESS subnet=IP_ADDRESS (SUMMARY_ONLY 
                                                                       {
                                                                        summaryOnly = true;
                                                                       })? NEWLINE) 
                                                                                   {
                                                                                    afs = new AggregateAddressAFStanza(network.getText(), subnet.getText(),
                                                                                    		summaryOnly);
                                                                                   }
  ;

aggregate_address_rb_stanza returns [RBStanza rbs]
@init {
boolean summaryOnly = false;
}
  :
  (AGGREGATE_ADDRESS network=IP_ADDRESS subnet=IP_ADDRESS (SUMMARY_ONLY 
                                                                       {
                                                                        summaryOnly = true;
                                                                       })? NEWLINE) 
                                                                                   {
                                                                                    rbs = new AggregateAddressRBStanza(network.getText(), subnet.getText(),
                                                                                    		summaryOnly);
                                                                                   }
  ;

auto_summary_af_stanza
  :
  NO? AUTO_SUMMARY NEWLINE
  ;

cluster_id_bgp_rb_stanza returns [RBStanza rbs]
  :
  (
    BGP CLUSTER_ID
    (
      (x=integer) 
                 {
                  rbs = new ClusterIdRBStanza(x);
                 }
      | (ip=IP_ADDRESS) 
                       {
                        rbs = new ClusterIdRBStanza(ip.getText());
                       }
    )
    NEWLINE
  )
  ;

default_metric_af_stanza returns [AFStanza afs]
  :
  (DEFAULT_METRIC metric=integer NEWLINE) 
                                         {
                                          afs = new DefaultMetricAFStanza(metric);
                                         }
  ;

default_metric_rb_stanza returns [RBStanza rbs]
  :
  (DEFAULT_METRIC metric=integer NEWLINE) 
                                         {
                                          rbs = new NullRBStanza();
                                         }
  ;

router_bgp_stanza returns [Stanza s]
  :
  (ROUTER BGP procnum=integer NEWLINE rbsl=rb_stanza_list closing_comment afrbsl=address_family_rb_stanza_list) 
                                                                                                               {
                                                                                                                RouterBGPStanza rs = new RouterBGPStanza(procnum);
                                                                                                                for (RBStanza rbs : rbsl) {
                                                                                                                	rs.processStanza(rbs);
                                                                                                                }
                                                                                                                for (RBStanza rbs : afrbsl) {
                                                                                                                	rs.processStanza(rbs);
                                                                                                                }
                                                                                                                s = rs;
                                                                                                               }
  ;

router_id_bgp_rb_stanza returns [RBStanza rbs]
  :
  (BGP ROUTER_ID routerid=IP_ADDRESS NEWLINE) 
                                             {
                                              rbs = new RouterIdRBStanza(routerid.getText());
                                             }
  ;

neighbor_activate_af_stanza returns [AFStanza afs]
  :
  NEIGHBOR
  (
    x=IP_ADDRESS 
                {
                 afs = new NeighborActivateAFStanza(x.getText());
                }
    | VARIABLE
  )
  ACTIVATE NEWLINE
  ;

neighbor_ebgp_multihop_rb_stanza returns [RBStanza rbs = new NullRBStanza()]
  :
  NEIGHBOR
  (
    IP_ADDRESS
    | VARIABLE
  )
  EBGP_MULTIHOP hop=integer NEWLINE
  ;

neighbor_default_originate_af_stanza returns [AFStanza afs]
  :
  (
    NEIGHBOR
    (
      name=IP_ADDRESS
      | name=VARIABLE
    )
    DEFAULT_ORIGINATE (ROUTE_MAP map=VARIABLE)? NEWLINE
  )
  
  {
   String mapName = (map == null) ? null : map.getText();
   return new NeighborDefaultOriginateAFStanza(name.getText(), mapName);
  }
  ;

neighbor_ip_route_reflector_client_af_stanza returns [AFStanza afs = new NullAFStanza()]
  :
  NEIGHBOR IP_ADDRESS ROUTE_REFLECTOR_CLIENT NEWLINE
  ;

neighbor_next_hop_self_rb_stanza returns [RBStanza rbs = new NullRBStanza()]
  :
  NEIGHBOR IP_ADDRESS NEXT_HOP_SELF NEWLINE
  ;

neighbor_peer_group_assignment_af_stanza returns [AFStanza afs]
  :
  (NEIGHBOR (address=IP_ADDRESS) PEER_GROUP name=VARIABLE NEWLINE) 
                                                                  {
                                                                   afs = new NeighborPeerGroupAssignmentAFStanza(name.getText(), address.getText());
                                                                  }
  ;

neighbor_peer_group_assignment_rb_stanza returns [RBStanza rbs]
  :
  (NEIGHBOR (address=IP_ADDRESS) PEER_GROUP name=VARIABLE NEWLINE) 
                                                                  {
                                                                   rbs = new NeighborPeerGroupAssignmentRBStanza(name.getText(), address.getText());
                                                                  }
  ;

neighbor_peer_group_creation_rb_stanza returns [RBStanza rbs]
  :
  (NEIGHBOR name=VARIABLE PEER_GROUP NEWLINE) 
                                             {
                                              rbs = new NeighborPeerGroupCreationRBStanza(name.getText());
                                             }
  ;

neighbor_pg_prefix_list_rb_stanza returns [RBStanza rbs]
  :
  NEIGHBOR
  (
    neighbor=IP_ADDRESS
    | neighbor=VARIABLE
  )
  PREFIX_LIST list_name=VARIABLE
  (
    (IN 
       {
        rbs = new NeighborPeerGroupPrefixListInRBStanza(neighbor.getText(),
        		list_name.getText());
       })
    | (OUT 
          {
           rbs = new NullRBStanza();
          })
  )
  NEWLINE
  ;

neighbor_pg_remote_as_rb_stanza returns [RBStanza rbs]
@init {
boolean ip = false;
}
  :
  (
    NEIGHBOR
    (
      (pg=IP_ADDRESS 
                    {
                     ip = true;
                    })
      | pg=VARIABLE
    )
    REMOTE_AS as=integer NEWLINE
  )
  
  {
   rbs = new NeighborPeerGroupASAssignmentRBStanza(pg.getText(), as, ip);
  }
  ;

neighbor_pg_route_map_rb_stanza returns [RBStanza rbs]
@init {
boolean in = false;
}
  :
  (
    NEIGHBOR
    (
      pg=IP_ADDRESS
      | pg=VARIABLE
    )
    ROUTE_MAP name=VARIABLE
    (
      (IN 
         {
          in = true;
         })
      | (OUT 
            {
             in = false;
            })
    )
    NEWLINE
  )
  
  {
   if (in) {
   	rbs = new NeighborPeerGroupRouteMapInStanza(pg.getText(), name.getText());
   } else {
   	rbs = new NeighborPeerGroupRouteMapOutStanza(pg.getText(), name.getText());
   }
  }
  ;

neighbor_pg_route_reflector_client_af_stanza returns [AFStanza afs]
  :
  (NEIGHBOR pg=VARIABLE ROUTE_REFLECTOR_CLIENT NEWLINE) 
                                                       {
                                                        afs = new PeerGroupRouteReflectorClientAFStanza(pg.getText());
                                                       }
  ;

neighbor_prefix_list_af_stanza returns [AFStanza afs]
  :
  NEIGHBOR
  (
    neighbor=IP_ADDRESS
    | neighbor=VARIABLE
  )
  PREFIX_LIST list_name=VARIABLE
  (
    (IN 
       {
        afs = new NeighborPrefixListInAFStanza(neighbor.getText(), list_name.getText());
       })
    | (OUT 
          {
           afs = new NullAFStanza();
          })
  )
  NEWLINE
  ;

neighbor_remove_private_as_af_stanza
  :
  NEIGHBOR
  (
    IP_ADDRESS
    | VARIABLE
  )
  REMOVE_PRIVATE_AS NEWLINE
  ;

neighbor_route_map_af_stanza returns [AFStanza afs]
@init {
boolean in = false;
}
  :
  (
    NEIGHBOR
    (
      pg=IP_ADDRESS
      | pg=VARIABLE
    )
    ROUTE_MAP name=VARIABLE
    (
      (IN 
         {
          in = true;
         })
      | (OUT 
            {
             in = false;
            })
    )
    NEWLINE
  )
  
  {
   if (in) {
   	afs = new NeighborRouteMapInStanza(pg.getText(), name.getText());
   } else {
   	afs = new NeighborRouteMapOutStanza(pg.getText(), name.getText());
   }
  }
  ;

neighbor_route_reflector_client_af_stanza returns [AFStanza afs]
  :
  (
    x=neighbor_pg_route_reflector_client_af_stanza
    | x=neighbor_ip_route_reflector_client_af_stanza
  )
  
  {
   afs = x;
  }
  ;

neighbor_send_community_af_stanza returns [AFStanza afs]
  :
  (
    NEIGHBOR
    (
      x=IP_ADDRESS
      | x=VARIABLE
    )
    SEND_COMMUNITY NEWLINE
  )
  
  {
   if (x != null) {
   	afs = new NeighborSendCommunityAFStanza(x.getText());
   } else {
   	afs = new NullAFStanza();
   }
  }
  ;

neighbor_send_community_rb_stanza returns [RBStanza rbs]
  :
  (
    NEIGHBOR
    (
      x=IP_ADDRESS
      | x=VARIABLE
    )
    SEND_COMMUNITY NEWLINE
  )
  
  {
   if (x != null) {
   	rbs = new NeighborSendCommunityRBStanza(x.getText());
   } else {
   	rbs = new NullRBStanza();
   }
  }
  ;

neighbor_shutdown_rb_stanza returns [RBStanza rbs]
  :
  (
    NEIGHBOR
    (
      x=IP_ADDRESS
      | x=VARIABLE
    )
    SHUTDOWN NEWLINE
  )
  
  {
   rbs = new NeighborShutdownRBStanza(x.getText());
  }
  ;

neighbor_update_source_rb_stanza returns [RBStanza rbs]
  :
  (
    NEIGHBOR
    (
      x=IP_ADDRESS
      | x=VARIABLE
    )
    UPDATE_SOURCE source=VARIABLE NEWLINE
  )
  
  {
   rbs = new NeighborUpdateSourceRBStanza(x.getText(), source.getText());
  }
  ;

network_af_stanza returns [AFStanza afs]
@init {
String maskString = "";
}
  :
  NETWORK
  (
    ( (ip=IP_ADDRESS (MASK mask=IP_ADDRESS)?) 
                                             {
                                              if (mask != null) {
                                              	maskString = mask.getText();
                                              }
                                              afs = new NetworkAFStanza(ip.getText(), maskString);
                                             })
    | ( (ip=IPV6_ADDRESS (FORWARD_SLASH DEC)?) 
                                              {
                                               afs = new NullAFStanza();
                                              })
  )
  NEWLINE
  ;

network_rb_stanza returns [RBStanza rbs]
@init {
String maskString = "";
}
  :
  NETWORK
  (
    ( (ip=IP_ADDRESS (MASK mask=IP_ADDRESS)?) 
                                             {
                                              if (mask != null) {
                                              	maskString = mask.getText();
                                              }
                                              rbs = new NetworkRBStanza(ip.getText(), maskString);
                                             })
    | ( (ip=IPV6_ADDRESS (FORWARD_SLASH DEC)?) 
                                              {
                                               rbs = new NullRBStanza();
                                              })
  )
  NEWLINE
  ;

no_neighbor_activate_af_stanza
  :
  NO NEIGHBOR
  (
    IP_ADDRESS
    | VARIABLE
  )
  ACTIVATE NEWLINE
  ;

null_af_stanza returns [AFStanza s = new NullAFStanza()]
  :
  comment_stanza
  | neighbor_remove_private_as_af_stanza
  | no_neighbor_activate_af_stanza
  | null_standalone_af_stanza
  ;

null_rb_stanza returns [RBStanza rbs = new NullRBStanza()]
  :
  comment_stanza
  | null_standalone_rb_stanza
  ;

null_standalone_af_stanza
  :
  NO?
  (
    (AGGREGATE_ADDRESS IPV6_ADDRESS)
    | AUTO_SUMMARY
    | BGP
    | MAXIMUM_PATHS
    |
    (
      NEIGHBOR
      (
        (
          (
            IP_ADDRESS
            | VARIABLE
          )
          (
            MAXIMUM_PREFIX
            | NEXT_HOP_SELF
            | SOFT_RECONFIGURATION
          )
        )
        | IPV6_ADDRESS
      )
    )
    | SYNCHRONIZATION
  )
  ~NEWLINE* NEWLINE
  ;

null_standalone_rb_stanza
  :
  NO?
  (
    AUTO_SUMMARY
    |
    (
      BGP
      (
        DAMPENING
        | GRACEFUL_RESTART
        | LOG_NEIGHBOR_CHANGES
      )
    )
    | MAXIMUM_PATHS
    |
    (
      NEIGHBOR
      (
        (
          (
            IP_ADDRESS
            | VARIABLE
          )
          (
            DESCRIPTION
            | FALL_OVER
            | PASSWORD
            | REMOVE_PRIVATE_AS
            | SOFT_RECONFIGURATION
            | TIMERS
            | TRANSPORT
          )
        )
        | IPV6_ADDRESS
      )
    )
    | SYNCHRONIZATION
  )
  ~NEWLINE* NEWLINE
  ;

rb_stanza returns [RBStanza rbs]
  :
  (
    x=address_family_rb_stanza
    | x=aggregate_address_rb_stanza
    | x=cluster_id_bgp_rb_stanza
    | x=default_metric_rb_stanza
    | x=neighbor_ebgp_multihop_rb_stanza
    | x=neighbor_next_hop_self_rb_stanza
    | x=neighbor_peer_group_creation_rb_stanza
    | x=neighbor_peer_group_assignment_rb_stanza
    | x=neighbor_pg_prefix_list_rb_stanza
    | x=neighbor_pg_remote_as_rb_stanza
    | x=neighbor_pg_route_map_rb_stanza
    | x=neighbor_send_community_rb_stanza
    | x=neighbor_shutdown_rb_stanza
    | x=neighbor_update_source_rb_stanza
    | x=network_rb_stanza
    | x=null_rb_stanza
    | x=redistribute_connected_rb_stanza
    | x=redistribute_ospf_rb_stanza
    | x=redistribute_static_rb_stanza
    | x=router_id_bgp_rb_stanza
  )
  
  {
   rbs = x;
  }
  ;

rb_stanza_list returns [List<RBStanza> rsl = new ArrayList<RBStanza>()]
  :
  ( (x=rb_stanza) 
                 {
                  rsl.add(x);
                 })+
  ;

redistribute_connected_af_stanza returns [AFStanza afs = new NullAFStanza()]
  :
  REDISTRIBUTE CONNECTED ~NEWLINE* NEWLINE
  ;

redistribute_connected_rb_stanza returns [RBStanza rbs = new NullRBStanza()]
  :
  REDISTRIBUTE CONNECTED ~NEWLINE* NEWLINE
  ;

redistribute_ospf_rb_stanza returns [RBStanza rbs = new NullRBStanza()]
  :
  REDISTRIBUTE OSPF ~NEWLINE* NEWLINE
  ;

redistribute_static_af_stanza returns [AFStanza afs]
@init {
String mapName = null;
}
  :
  (REDISTRIBUTE STATIC (ROUTE_MAP map=VARIABLE 
                                              {
                                               mapName = map.getText();
                                              }) NEWLINE) 
                                                         {
                                                          afs = new RedistributeStaticAFStanza(mapName);
                                                         }
  ;

redistribute_static_rb_stanza returns [RBStanza rbs = new NullRBStanza()]
  :
  REDISTRIBUTE STATIC ~NEWLINE* NEWLINE
  ;
