package org.batfish.dataplane.traceroute;

/** Terminal disposition of an IPv6 FIB path trace. */
public enum Ipv6TraceDisposition {
  ACCEPTED,
  EXITS_NETWORK,
  LOOP,
  MAX_HOPS,
  NEIGHBOR_UNREACHABLE,
  NO_ROUTE
}
