package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.setVlanId;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.function.StateFunction;

/**
 * An edge from a {@link org.batfish.common.topology.bridge_domain.node.BridgedL3Interface} to a
 * {@link org.batfish.common.topology.bridge_domain.node.BridgeDomain}.
 */
public final class BridgedL3ToBridgeDomain extends Edge {

  public interface Function extends StateFunction {}

  /**
   * Helper for creating an edge from an IOS-XR BVI (bridged virtual interface) to its bridge
   * domain.
   */
  public static @Nonnull BridgedL3ToBridgeDomain bviToBridgeDomain() {
    return of(identity());
  }

  /**
   * Helper for creating an edge from an IRB/Vlan interface to the a device's vlan-aware bridge
   * domain.
   */
  public static @Nonnull BridgedL3ToBridgeDomain irbToBridgeDomain(int vlanId) {
    return of(setVlanId(vlanId));
  }

  @VisibleForTesting
  public static @Nonnull BridgedL3ToBridgeDomain of(Function stateFunction) {
    return new BridgedL3ToBridgeDomain(stateFunction);
  }

  private BridgedL3ToBridgeDomain(Function stateFunction) {
    super(stateFunction);
  }
}
