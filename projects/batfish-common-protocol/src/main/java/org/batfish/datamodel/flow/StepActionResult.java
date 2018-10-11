package org.batfish.datamodel.flow;

public enum StepActionResult {
  ACCEPTED,
  DENIED_IN,
  DENIED_OUT,
  LOOP,
  NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK,
  NO_ROUTE,
  NULL_ROUTED,
  SENT_OUT,
  SENT_IN
}
