package org.batfish.datamodel;

public enum FlowDisposition {
  ACCEPTED,
  DENIED_IN,
  DENIED_OUT,
  LOOP,
  NEIGHBOR_UNREACHABLE,
  // TODO: remove this disposition;
  // for now, it's used by CounterExample
  NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK,
  DELIVERED_TO_SUBNET,
  EXITS_NETWORK,
  INSUFFICIENT_INFO,
  NO_ROUTE,
  NULL_ROUTED
}
