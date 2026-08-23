package org.batfish.dataplane.traceroute;

/** Terminal disposition of an IPv6 FIB path trace. */
public enum Ipv6TraceDisposition {
  ACCEPTED,
  DENIED_IN,
  DENIED_OUT,
  EXITS_NETWORK,
  LOOP,
  MAX_HOPS,
  NEIGHBOR_UNREACHABLE,
  NO_ROUTE,
  NULL_ROUTED
}
