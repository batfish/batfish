parser grammar CiscoGrammar_ospf;

@members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "CiscoGrammar_ospf: " + hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	return errors;
}
}

area_ipv6_ro_stanza
  :
  AREA ~NEWLINE* NEWLINE
  ;

area_nssa_ro_stanza returns [ROStanza ros]
  :
  (AREA num=integer NSSA NO_SUMMARY? NEWLINE) 
                                             {
                                              ros = new AreaNssaROStanza(num);
                                             }
  ;

default_information_ipv6_ro_stanza
  :
  DEFAULT_INFORMATION ~NEWLINE* NEWLINE
  ;

default_information_ro_stanza returns [ROStanza ros]
@init {
boolean always = false;
}
  :
  (
    DEFAULT_INFORMATION ORIGINATE
    (
      (METRIC metric=DEC)
      | (METRIC_TYPE type=DEC)
      | (ALWAYS 
               {
                always = true;
               })
      | (ROUTE_MAP map=VARIABLE)
    )*
    NEWLINE
  )
  
  {
   String routeMap = null;
   Integer metricInt = null;
   Integer typeInt = null;
   if (metric != null) {
   	metricInt = Integer.parseInt(metric.getText());
   }
   if (type != null) {
   	typeInt = Integer.parseInt(type.getText());
   }
   if (map != null) {
   	routeMap = map.getText();
   }
   ros = new DefaultInformationOriginateROStanza(always, metricInt, typeInt,
   		routeMap);
  }
  ;

ipv6_ro_stanza returns [IPv6ROStanza ros]
  :
  (
    x=null_ipv6_ro_stanza
    | x=passive_interface_ipv6_ro_stanza
    | x=redistribute_ipv6_ro_stanza
  )
  
  {
   ros = x;
  }
  ;

ipv6_ro_stanza_list returns [List<IPv6ROStanza> rsl = new ArrayList<IPv6ROStanza>()]
  :
  ( (x=ipv6_ro_stanza) 
                      {
                       rsl.add(x);
                      })+
  ;

ipv6_router_ospf_stanza returns [Stanza s]
  :
  (IPV6 ROUTER OSPF procnum=integer NEWLINE rosl=ipv6_ro_stanza_list closing_comment) 
                                                                                     {
                                                                                      IPv6RouterOSPFStanza rs = new IPv6RouterOSPFStanza(procnum);
                                                                                      for (IPv6ROStanza ros : rosl) {
                                                                                      	rs.processStanza(ros);
                                                                                      }
                                                                                      s = rs;
                                                                                     }
  ;

log_adjacency_changes_ipv6_ro_stanza
  :
  LOG_ADJACENCY_CHANGES NEWLINE
  ;

maximum_paths_ro_stanza returns [ROStanza ros = new NullROStanza()]
  :
  MAXIMUM_PATHS ~NEWLINE* NEWLINE
  ;

network_ro_stanza returns [ROStanza ros]
@init {
Integer area = null;
}
  :
  (
    NETWORK ip=IP_ADDRESS sub=IP_ADDRESS AREA
    (
      (area_int=integer 
                       {
                        area = area_int;
                       })
      | (area_ip=IP_ADDRESS 
                           {
                            area = (int) Util.ipToLong(area_ip.getText());
                           })
    )
    NEWLINE
  )
  
  {
   ros = new NetworkROStanza(ip.getText(), sub.getText(), area);
  }
  ;

null_ipv6_ro_stanza returns [IPv6ROStanza ros = new NullIPv6ROStanza()]
  :
  area_ipv6_ro_stanza
  | comment_stanza
  | default_information_ipv6_ro_stanza
  | log_adjacency_changes_ipv6_ro_stanza
  | router_id_ipv6_ro_stanza
  ;

null_ro_stanza returns [ROStanza ros = new NullROStanza()]
  :
  comment_stanza
  | null_standalone_ro_stanza
  ;

null_standalone_ro_stanza
  :
  NO?
  (
    (AREA DEC AUTHENTICATION)
    | BFD
    | DISTRIBUTE_LIST
    | LOG_ADJACENCY_CHANGES
    | NSF
  )
  ~NEWLINE* NEWLINE
  ;

passive_interface_ipv6_ro_stanza returns [IPv6ROStanza ros = new NullIPv6ROStanza()]
  :
  NO? PASSIVE_INTERFACE ~NEWLINE* NEWLINE
  ;

passive_interface_default_ro_stanza returns [ROStanza ros = new PassiveInterfaceDefaultROStanza()]
  :
  PASSIVE_INTERFACE DEFAULT NEWLINE
  ;

passive_interface_ro_stanza returns [ROStanza ros]
@init {
boolean passive = true;
}
  :
  ( (NO 
       {
        passive = false;
       })? PASSIVE_INTERFACE i=VARIABLE NEWLINE) 
                                                {
                                                 ros = new PassiveInterfaceStanza(i.getText(), passive);
                                                }
  ;

redistribute_bgp_ro_stanza returns [ROStanza ros = new NullROStanza()]
  :
  REDISTRIBUTE BGP DEC (METRIC DEC)? (METRIC_TYPE DEC)? SUBNETS? NEWLINE
  ;

redistribute_ipv6_ro_stanza returns [IPv6ROStanza ros = new NullIPv6ROStanza()]
  :
  REDISTRIBUTE ~NEWLINE* NEWLINE
  ;

redistribute_connected_ro_stanza returns [ROStanza ros]
@init {
boolean subnets = false;
}
  :
  (
    REDISTRIBUTE CONNECTED (METRIC metric=DEC)? (SUBNETS 
                                                        {
                                                         subnets = true;
                                                        })? NEWLINE
  )
  
  {
   Integer metricInt = null;
   if (metric != null) {
   	metricInt = Integer.parseInt(metric.getText());
   }
   ros = new RedistributeConnectedROStanza(metricInt, subnets);
  }
  ;

redistribute_static_ro_stanza returns [ROStanza ros]
@init {
int cost = 20; //default cost
boolean subnets = false;
String mapText = null;
}
  :
  (
    REDISTRIBUTE STATIC (METRIC costToken=DEC 
                                             {
                                              cost = Integer.parseInt(costToken.getText());
                                             })? (SUBNETS 
                                                         {
                                                          subnets = true;
                                                         })? (ROUTE_MAP map=VARIABLE 
                                                                                    {
                                                                                     mapText = map.getText();
                                                                                    })? NEWLINE
  )
  
  {
   ros = new RedistributeStaticROStanza(cost, mapText, subnets);
  }
  ;

ro_stanza returns [ROStanza ros]
  :
  (
    x=area_nssa_ro_stanza
    | x=default_information_ro_stanza
    | x=maximum_paths_ro_stanza
    | x=network_ro_stanza
    | x=null_ro_stanza
    | x=passive_interface_default_ro_stanza
    | x=passive_interface_ro_stanza
    | x=redistribute_bgp_ro_stanza
    | x=redistribute_connected_ro_stanza
    | x=redistribute_static_ro_stanza
    | x=router_id_ro_stanza
  )
  
  {
   ros = x;
  }
  ;

ro_stanza_list returns [List<ROStanza> rsl = new ArrayList<ROStanza>()]
  :
  ( (x=ro_stanza) 
                 {
                  rsl.add(x);
                 })+
  ;

router_id_ipv6_ro_stanza
  :
  ROUTER_ID ~NEWLINE* NEWLINE
  ;

router_id_ro_stanza returns [ROStanza ros]
  :
  (ROUTER_ID ip=IP_ADDRESS NEWLINE) 
                                   {
                                    ros = new RouterIdROStanza(ip.getText());
                                   }
  ;

router_ospf_stanza returns [Stanza s]
  :
  (ROUTER OSPF procnum=integer NEWLINE rosl=ro_stanza_list closing_comment) 
                                                                           {
                                                                            RouterOSPFStanza rs = new RouterOSPFStanza(procnum);
                                                                            for (ROStanza ros : rosl) {
                                                                            	rs.processStanza(ros);
                                                                            }
                                                                            s = rs;
                                                                           }
  ;
