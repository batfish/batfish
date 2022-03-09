package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.filterByOuterTag;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.function.StateFunction;
import org.batfish.datamodel.IntegerSpace;

/**
 * An edge from a {@link org.batfish.common.topology.bridge_domain.node.PhysicalInterface} to a
 * {@link org.batfish.common.topology.bridge_domain.node.NonBridgedL3Interface}.
 */
public final class PhysicalToNonBridgedL3 extends Edge {
  public interface Function extends StateFunction {}

  /** Helper for creating an edge from a physical interface to a non-bridged layer-3 interface. */
  public static @Nonnull PhysicalToNonBridgedL3 physicalToNonBridgedL3(
      @Nullable Integer allowedTag) {
    return of(
        allowedTag != null
            ? filterByOuterTag(IntegerSpace.of(allowedTag), false)
            : filterByOuterTag(IntegerSpace.EMPTY, true));
  }

  @VisibleForTesting
  public static @Nonnull PhysicalToNonBridgedL3 of(Function stateFunction) {
    return new PhysicalToNonBridgedL3(stateFunction);
  }

  private PhysicalToNonBridgedL3(Function stateFunction) {
    super(stateFunction);
  }
}
