package org.batfish.representation.cisco_xr;

/** Enumerates the types of OSPF networks that can be associated with an interface. */
public enum OspfNetworkType {
  BROADCAST,
  POINT_TO_POINT,
  NON_BROADCAST,
  POINT_TO_MULTIPOINT,
  POINT_TO_MULTIPOINT_NON_BROADCAST
}
