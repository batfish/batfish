package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.pushTag;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.function.StateFunction;
import org.batfish.common.topology.bridge_domain.node.L1Interface;
import org.batfish.common.topology.bridge_domain.node.L3NonBridgedInterface;

/** An edge from a {@link L3NonBridgedInterface} to a {@link L1Interface}. */
public final class L3ToL1 extends Edge {
  public interface Function extends StateFunction {}

  /** Helper for creating an edge from a non-bridged layer-3 interface to its layer-1 interface. */
  public static @Nonnull L3ToL1 l3NonBridgedToL1(@Nullable Integer tagToPush) {
    return of(tagToPush == null ? identity() : pushTag(tagToPush));
  }

  @VisibleForTesting
  public static @Nonnull L3ToL1 of(Function stateFunction) {
    return new L3ToL1(stateFunction);
  }

  private L3ToL1(Function stateFunction) {
    super(stateFunction);
  }
}
