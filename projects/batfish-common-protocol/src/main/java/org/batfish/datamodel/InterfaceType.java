package org.batfish.datamodel;

/** Types of device interfaces (see {@link org.batfish.datamodel.Interface}) */
public enum InterfaceType {
  /** Logical interface that aggregates multiple (physical) interfaces */
  AGGREGATED,
  /** Generic Logical interface, (e.g., units on Juniper devices) */
  LOGICAL,
  /** Logical loopback interface */
  LOOPBACK,
  /** Special null interface */
  NULL,
  /** Physical port */
  PHYSICAL,
  /** Logical redundant ethernet interface (in Juniper parlance) */
  REDUNDANT,
  /** A logical tunnel interface (e.g., GRE, IP-in-IP encapsulation) */
  TUNNEL,
  /** Uknknown interface type */
  UNKNOWN, // for use as sentinel value
  /** Logical VLAN/irb interface */
  VLAN,
  /** Logical VPN interface, (i.e., IPSec tunnel) */
  VPN,
}
