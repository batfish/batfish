package org.batfish.datamodel;

import javax.annotation.Nonnull;

/** Reason why an interface is inactive. */
public enum InactiveReason {
  ADMIN_DOWN("Administratively down"),
  AGGREGATE_NEIGHBOR_DOWN("Neighbor interface is down"),
  BLACKLISTED("Considered down for maintenance"),
  AUTOSTATE_FAILURE("Autostate failure"),
  BIND_DOWN("Tunnel bind interface is down"),
  IGNORE_MGMT("Ignored management interface"),
  INCOMPLETE("Incomplete configuration"),
  INVALID("Invalid configuration"),
  LACP_FAILURE("LACP failure"),
  LINE_DOWN("Line down"),
  NO_ACTIVE_MEMBERS("No operational member interfaces"),
  NO_MEMBERS("No configured member interfaces"),
  PARENT_DOWN("Parent interface is down"),
  PHYSICAL_NEIGHBOR_DOWN("Neighbor interface is down"),
  VRF_DOWN("Interface's VRF is administratively down"),
  ;

  public @Nonnull String description() {
    return _description;
  }

  InactiveReason(String description) {
    _description = description;
  }

  private final @Nonnull String _description;
}
