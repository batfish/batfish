package org.batfish.datamodel;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;

public enum FlowDisposition {
  ACCEPTED,
  DENIED_IN,
  DENIED_OUT,
  LOOP,
  NEIGHBOR_UNREACHABLE,
  DELIVERED_TO_SUBNET,
  EXITS_NETWORK,
  INSUFFICIENT_INFO,
  NO_ROUTE,
  NULL_ROUTED;

  /** Return whether this FlowDisposition represents a successful delivery. */
  public boolean isSuccessful() {
    return SUCCESS_DISPOSITIONS.contains(this);
  }

  public static final Set<FlowDisposition> ALL_DISPOSITIONS =
      ImmutableSet.copyOf(FlowDisposition.values());

  public static final Set<FlowDisposition> SUCCESS_DISPOSITIONS =
      ImmutableSet.of(ACCEPTED, DELIVERED_TO_SUBNET, EXITS_NETWORK);
  public static final Set<FlowDisposition> FAILURE_DISPOSITIONS =
      ImmutableSet.copyOf(Sets.difference(ALL_DISPOSITIONS, SUCCESS_DISPOSITIONS));
}
