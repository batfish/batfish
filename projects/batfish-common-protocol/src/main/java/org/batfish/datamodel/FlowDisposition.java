package org.batfish.datamodel;

public enum FlowDisposition {
  ACCEPTED,
  DENIED_IN,
  DENIED_OUT,
  LOOP,
  NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK,
  NO_ROUTE,
  NULL_ROUTED
}
