package org.batfish.vendor.arista.representation.eos;

/** Types of routes that can be redistributed into BGP */
public enum AristaRedistributeType {
  /** ARP-generated host routes */
  ATTACHED_HOST,
  /** Directly connected routes */
  CONNECTED,
  /** Dynamic policy routes */
  DYNAMIC,
  /** IS-IS routes */
  ISIS,
  /** IS-IS level-1 routes */
  ISIS_L1,
  /** IS-IS level-2 routes */
  ISIS_L2,
  /** IS-IS level-1 and level-2 routes */
  ISIS_L1_L2,
  /** All OSPF routes */
  OSPF,
  /** OSPF routes that are internal to the AS */
  OSPF_INTERNAL,
  /** Routes external to the AS, but imported from OSPF */
  OSPF_EXTERNAL,
  /** All OSPF NSSA external routes */
  OSPF_NSSA_EXTERNAL,
  /** Type 1 OSPF NSSA external routes */
  OSPF_NSSA_EXTERNAL_TYPE_1,
  /** Type 2 OSPF NSSA external routes */
  OSPF_NSSA_EXTERNAL_TYPE_2,
  /** All OSPF v3 routes */
  OSPF3,
  /** OSPF v3 routes that are internal to the AS */
  OSPF3_INTERNAL,
  /** Routes external to the AS, but imported from OSPF v3 */
  OSPF3_EXTERNAL,
  /** Type 1 OSPF NSSA external routes */
  OSPF3_NSSA_EXTERNAL_TYPE_1,
  /** Type 2 OSPF NSSA external routes */
  OSPF3_NSSA_EXTERNAL_TYPE_2,
  /** RIP routes */
  RIP,
  /** Static routes */
  STATIC
}
