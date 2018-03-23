package org.batfish.datamodel;

public enum ForwardingAction {
  ACCEPT,
  DEBUG,
  DROP,
  DROP_ACL,
  DROP_ACL_IN,
  DROP_ACL_OUT,
  DROP_NO_ROUTE,
  DROP_NULL_ROUTE,
  FORWARD,
  NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK
}
