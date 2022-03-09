package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.pushTag;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.function.StateFunction;

/**
 * An edge from a {@link org.batfish.common.topology.bridge_domain.node.NonBridgedL3Interface} to a
 * {@link org.batfish.common.topology.bridge_domain.node.PhysicalInterface}.
 */
public final class NonBridgedL3ToPhysical extends Edge {
  public interface Function extends StateFunction {}

  /** Helper for creating an edge from a non-bridged layer-3 interface to its physical interface. */
  public static @Nonnull NonBridgedL3ToPhysical nonBridgedLayer3ToPhysical(
      @Nullable Integer tagToPush) {
    return of(tagToPush == null ? identity() : pushTag(tagToPush));
  }

  @VisibleForTesting
  public static @Nonnull NonBridgedL3ToPhysical of(Function stateFunction) {
    return new NonBridgedL3ToPhysical(stateFunction);
  }

  private NonBridgedL3ToPhysical(Function stateFunction) {
    super(stateFunction);
  }
}
