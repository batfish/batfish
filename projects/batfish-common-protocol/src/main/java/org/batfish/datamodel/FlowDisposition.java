package org.batfish.datamodel;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Arrays;
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
  NULL_ROUTED,

  // TODO: remove this disposition;
  // for now, it's used by CounterExample
  NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK,
  ;

  /** Return whether this FlowDisposition represents a successful delivery. */
  public boolean isSuccessful() {
    return SUCCESS_DISPOSITIONS.contains(this);
  }

  /** All dispositions exclude the old NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK disposition */
  public static final Set<FlowDisposition> ALL_DISPOSITIONS =
      Arrays.stream(FlowDisposition.values())
          .filter(d -> d != NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK)
          .collect(ImmutableSet.toImmutableSet());

  public static final Set<FlowDisposition> ALL_NONLOOP_DISPOSITIONS =
      Arrays.stream(FlowDisposition.values())
          .filter(d -> d != NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK && d != LOOP)
          .collect(ImmutableSet.toImmutableSet());

  public static final Set<FlowDisposition> SUCCESS_DISPOSITIONS =
      ImmutableSet.of(ACCEPTED, DELIVERED_TO_SUBNET, EXITS_NETWORK);
  public static final Set<FlowDisposition> FAILURE_DISPOSITIONS =
      ImmutableSet.copyOf(Sets.difference(ALL_DISPOSITIONS, SUCCESS_DISPOSITIONS));
}
