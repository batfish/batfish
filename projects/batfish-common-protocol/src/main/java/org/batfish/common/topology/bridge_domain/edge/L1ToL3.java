package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.filterByOuterTag;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.function.StateFunction;
import org.batfish.common.topology.bridge_domain.node.L1Interface;
import org.batfish.common.topology.bridge_domain.node.L3NonBridgedInterface;
import org.batfish.datamodel.IntegerSpace;

/** An edge from a {@link L1Interface} to a {@link L3NonBridgedInterface}. */
public final class L1ToL3 extends Edge {
  public interface Function extends StateFunction {}

  /** Helper for creating an edge from an l1 interface to a non-bridged layer-3 interface. */
  public static @Nonnull L1ToL3 l1ToL3NonBridged(@Nullable Integer allowedTag) {
    return of(
        allowedTag != null
            ? filterByOuterTag(IntegerSpace.of(allowedTag), false)
            : filterByOuterTag(IntegerSpace.EMPTY, true));
  }

  @VisibleForTesting
  public static @Nonnull L1ToL3 of(Function stateFunction) {
    return new L1ToL3(stateFunction);
  }

  private L1ToL3(Function stateFunction) {
    super(stateFunction);
  }
}
