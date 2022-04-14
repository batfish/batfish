package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.popTag;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.function.StateFunction;
import org.batfish.common.topology.bridge_domain.node.NonVlanAwareBridgeDomain;

/**
 * An edge from an {@link org.batfish.common.topology.bridge_domain.node.L2Interface} to a {@link
 * NonVlanAwareBridgeDomain}.
 */
public final class L2ToNonVlanAwareBridgeDomain extends L2ToBridgeDomain {
  public interface Function extends StateFunction {}

  /**
   * Helper for creating and edge from an IOS-XR-style l2transport interface to its bridge domain.
   */
  public static @Nonnull L2ToNonVlanAwareBridgeDomain l2TransportToBridgeDomain(int tagPopCount) {
    return of(popTag(tagPopCount));
  }

  @VisibleForTesting
  public static @Nonnull L2ToNonVlanAwareBridgeDomain of(Function stateFunction) {
    return new L2ToNonVlanAwareBridgeDomain(stateFunction);
  }

  private L2ToNonVlanAwareBridgeDomain(Function stateFunction) {
    super(stateFunction);
  }
}
