package org.batfish.datamodel.flow;

/** Types of actions which can be taken at the end of a {@link Step} */
public enum StepAction {
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
