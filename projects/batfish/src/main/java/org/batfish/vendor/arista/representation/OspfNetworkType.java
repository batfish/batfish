package org.batfish.vendor.arista.representation;

/** Enumerates the types of OSPF networks that can be associated with an interface. */
public enum OspfNetworkType {
  BROADCAST,
  POINT_TO_POINT,
  NON_BROADCAST,
  POINT_TO_MULTIPOINT,
  POINT_TO_MULTIPOINT_NON_BROADCAST
}
