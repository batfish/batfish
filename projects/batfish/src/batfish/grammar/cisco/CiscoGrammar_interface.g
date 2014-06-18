parser grammar CiscoGrammar_interface;

@members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "CiscoGrammar_interface: " + hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	return errors;
}
}

if_stanza returns [IFStanza is]
  :
  (
    x=null_if_stanza
    | x=ip_if_stanza
    | x=switchport_if_stanza
    | x=shutdown_if_stanza
  )
  
  {
   is = x;
  }
  ;

if_stanza_list returns [List<IFStanza> isl = new ArrayList<IFStanza>()]
  :
  ( (x=if_stanza) 
                 {
                  isl.add(x);
                 })*
  ;

interface_stanza returns [Stanza s]
  :
  INTERFACE iname=interface_name NEWLINE ifsl=if_stanza_list closing_comment 
                                                                            {
                                                                             InterfaceStanza is = new InterfaceStanza(iname);
                                                                             for (IFStanza ifs : ifsl) {
                                                                             	is.processStanza(ifs);
                                                                             }
                                                                             s = is;
                                                                            }
  ;

ip_access_group_if_stanza returns [IFStanza is]
  :
  IP ACCESS_GROUP
  (
    name=DEC
    | name=VARIABLE
  )
  (
    IN 
      {
       is = new IpAccessGroupInIFStanza(name.getText());
      }
    | OUT 
         {
          is = new IpAccessGroupOutIFStanza(name.getText());
         }
  )
  NEWLINE
  ;

ip_address_if_stanza returns [IFStanza ifs]
  :
  (IP ADDRESS ip=IP_ADDRESS subnet=IP_ADDRESS (STANDBY IP_ADDRESS)? NEWLINE) 
                                                                            {
                                                                             ifs = new IPAddressIFStanza(ip.getText(), subnet.getText());
                                                                            }
  ;

ip_address_secondary_if_stanza returns [IFStanza ifs]
  :
  (IP ADDRESS ip=IP_ADDRESS subnet=IP_ADDRESS SECONDARY NEWLINE) 
                                                                {
                                                                 ifs = new IPAddressSecondaryIFStanza(ip.getText(), subnet.getText());
                                                                }
  ;

ip_if_stanza returns [IFStanza is]
  :
  (
    x=ip_access_group_if_stanza
    | x=ip_address_if_stanza
    | x=ip_address_secondary_if_stanza
    | x=no_ip_address_if_stanza
    | x=ip_ospf_cost_if_stanza
    | x=ip_ospf_dead_interval_if_stanza
  )
  
  {
   is = x;
  }
  ;

ip_ospf_cost_if_stanza returns [IFStanza s]
  :
  (IP OSPF COST cost=integer NEWLINE) 
                                     {
                                      s = new IpOspfCostIFStanza(cost);
                                     }
  ;

ip_ospf_dead_interval_if_stanza returns [IFStanza is]
  :
  ( (IP OSPF DEAD_INTERVAL seconds=integer NEWLINE) 
                                                   {
                                                    is = new IPOSPFDeadIntervalIFStanza(seconds, 0);
                                                   })
  | ( (IP OSPF DEAD_INTERVAL MINIMAL HELLO_MULTIPLIER mult=integer NEWLINE) 
                                                                           {
                                                                            is = new IPOSPFDeadIntervalIFStanza(1, mult);
                                                                           })
  ;

no_ip_address_if_stanza returns [IFStanza ifs = new NoIPAddressIFStanza()]
  :
  NO IP ADDRESS NEWLINE
  ;

null_if_stanza returns [IFStanza ifs = new NullIFStanza()]
  :
  comment_stanza
  | (NO? SWITCHPORT NEWLINE)
  | null_standalone_if_stanza
  ;

null_standalone_if_stanza
  :
  NO?
  (
    ARP
    | ASYNC
    | AUTO
    | BANDWIDTH
    | CDP
    | CHANNEL
    | CHANNEL_GROUP
    | CHANNEL_PROTOCOL
    | CLNS
    | CLOCK
    | CRYPTO
    | DESCRIPTION
    | DUPLEX
    | ENCAPSULATION
    | FAIR_QUEUE
    | FULL_DUPLEX
    | GROUP_RANGE
    | HALF_DUPLEX
    | HOLD_QUEUE
    |
    (
      IP
      (
        ACCOUNTING
        | ARP
        | CGMP
        | DHCP
        | (DIRECTED_BROADCAST)
        | FLOW
        | HELPER_ADDRESS
        | IGMP
        | IRDP
        | MROUTE_CACHE
        | MTU
        | MULTICAST
        |
        (
          OSPF
          (
            AUTHENTICATION
            | NETWORK
            | PRIORITY
          )
        )
        | NAT
        | PIM
        | POLICY
        | PROXY_ARP
        | REDIRECTS
        | RIP
        | ROUTE_CACHE
        | TCP
        | UNNUMBERED
        | UNREACHABLES
        | VERIFY
        | VIRTUAL_REASSEMBLY
        | VRF
      )
    )
    | IPV6
    | ISDN
    | KEEPALIVE
    | LAPB
    | LLDP
    | LOAD_INTERVAL
    | LOGGING
    | LRE
    | MAC_ADDRESS
    | MACRO
    | MANAGEMENT_ONLY
    | MDIX
    | MEDIA_TYPE
    | MEMBER
    | MLS
    | MOP
    | MPLS
    | MTU
    | NAMEIF
    | NEGOTIATION
    | PEER
    | PHYSICAL_LAYER
    | POWER
    | PPP
    | PRIORITY_QUEUE
    | QOS
    | QUEUE_SET
    | RCV_QUEUE
    | ROUTE_CACHE
    | SECURITY_LEVEL
    | SERIAL
    | SERVICE_MODULE
    | SERVICE_POLICY
    | SPANNING_TREE
    | SPEED
    | SNMP
    | SRR_QUEUE
    | STANDBY
    | STORM_CONTROL
    |
    (
      SWITCHPORT
      (
        EMPTY
        | (MODE PRIVATE_VLAN)
        | NONEGOTIATE
        | PORT_SECURITY
        | VOICE
        | VLAN
      )
    )
    | TAG_SWITCHING
    | TRUST
    | TUNNEL
    | UDLD
    | VRF
    | VRRP
    | WRR_QUEUE
    | X25
  )
  ~NEWLINE* NEWLINE
  ;

shutdown_if_stanza returns [IFStanza is = new ShutdownIFStanza()]
  :
  SHUTDOWN NEWLINE
  ;

switchport_access_if_stanza returns [IFStanza is]
  :
  (SWITCHPORT ACCESS VLAN i=integer NEWLINE) 
                                            {
                                             is = new SwitchportAccessIFStanza(i);
                                            }
  ;

switchport_if_stanza returns [IFStanza is]
  :
  (
    x=switchport_access_if_stanza
    | x=switchport_trunk_if_stanza
    | x=switchport_mode_access_stanza
    | x=switchport_mode_dynamic_auto_stanza
    | x=switchport_mode_dynamic_desirable_stanza
    | x=switchport_mode_trunk_stanza
  )
  
  {
   is = x;
  }
  ;

switchport_mode_access_stanza returns [IFStanza is]
  :
  (SWITCHPORT MODE ACCESS NEWLINE) 
                                  {
                                   is = new SwitchportModeIFStanza(SwitchportMode.ACCESS);
                                  }
  ;

switchport_mode_dynamic_auto_stanza returns [IFStanza is]
  :
  (SWITCHPORT MODE DYNAMIC AUTO NEWLINE) 
                                        {
                                         is = new SwitchportModeIFStanza(SwitchportMode.DYNAMIC_AUTO);
                                        }
  ;

switchport_mode_dynamic_desirable_stanza returns [IFStanza is]
  :
  (SWITCHPORT MODE DYNAMIC DESIRABLE NEWLINE) 
                                             {
                                              is = new SwitchportModeIFStanza(SwitchportMode.DYNAMIC_DESIRABLE);
                                             }
  ;

switchport_mode_trunk_stanza returns [IFStanza is]
  :
  (SWITCHPORT MODE TRUNK NEWLINE) 
                                 {
                                  is = new SwitchportModeIFStanza(SwitchportMode.TRUNK);
                                 }
  ;

switchport_trunk_allowed_if_stanza returns [IFStanza is]
  :
  (SWITCHPORT TRUNK ALLOWED VLAN ADD? r=range NEWLINE) 
                                                      {
                                                       is = new SwitchportTrunkAllowedIFStanza(r);
                                                      }
  ;

switchport_trunk_encapsulation_if_stanza returns [IFStanza is]
@init {
SwitchportEncapsulationType et = null;
}
  :
  (
    SWITCHPORT TRUNK ENCAPSULATION
    (
      (DOT1Q 
            {
             et = SwitchportEncapsulationType.DOT1Q;
            })
      | (ISL 
            {
             et = SwitchportEncapsulationType.ISL;
            })
      | (NEGOTIATE 
                  {
                   et = SwitchportEncapsulationType.NEGOTIATE;
                  })
    )
    NEWLINE
  )
  
  {
   is = new SwitchportTrunkEncapsulationIFStanza(et);
  }
  ;

switchport_trunk_if_stanza returns [IFStanza is]
  :
  (
    x=switchport_trunk_native_if_stanza
    | x=switchport_trunk_encapsulation_if_stanza
    | x=switchport_trunk_allowed_if_stanza
  )
  
  {
   is = x;
  }
  ;

switchport_trunk_native_if_stanza returns [IFStanza is]
  :
  (SWITCHPORT TRUNK NATIVE VLAN i=integer NEWLINE) 
                                                  {
                                                   is = new SwitchportTrunkNativeIFStanza(i);
                                                  }
  ;
