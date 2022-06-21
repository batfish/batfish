package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.function.StateFunction;
import org.batfish.common.topology.bridge_domain.node.L3BridgedInterface;
import org.batfish.common.topology.bridge_domain.node.NonVlanAwareBridgeDomain;

/** An edge from a {@link L3BridgedInterface} to a {@link NonVlanAwareBridgeDomain}. */
public final class L3ToNonVlanAwareBridgeDomain extends L3ToBridgeDomain {

  public interface Function extends StateFunction {}

  /**
   * Helper for creating an edge from an IOS-XR BVI (bridged virtual interface) to its bridge
   * domain.
   */
  public static @Nonnull L3ToNonVlanAwareBridgeDomain bviToBridgeDomain() {
    return of(identity());
  }

  @VisibleForTesting
  public static @Nonnull L3ToNonVlanAwareBridgeDomain of(Function stateFunction) {
    return new L3ToNonVlanAwareBridgeDomain(stateFunction);
  }

  private L3ToNonVlanAwareBridgeDomain(Function stateFunction) {
    super(stateFunction);
  }
}
