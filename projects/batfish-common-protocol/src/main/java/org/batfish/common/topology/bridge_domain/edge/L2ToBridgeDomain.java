package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.popTag;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.setVlanId;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.translateVlan;

import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.function.StateFunction;

/**
 * An edge from an {@link org.batfish.common.topology.bridge_domain.node.L2Interface} to a {@link
 * org.batfish.common.topology.bridge_domain.node.BridgeDomain}.
 */
public final class L2ToBridgeDomain extends Edge {
  public interface Function extends StateFunction {}

  /**
   * Helper for creating and edge from a traditional access-mode switchport to a device's vlan-aware
   * bridge domain.
   */
  public static @Nonnull L2ToBridgeDomain accessToBridgeDomain(int vlanId) {
    return of(setVlanId(vlanId));
  }

  /**
   * Helper for creating and edge from an IOS-XR-style l2transport interface to its bridge domain.
   */
  public static @Nonnull L2ToBridgeDomain l2TransportToBridgeDomain(int tagPopCount) {
    return of(popTag(tagPopCount));
  }

  /**
   * Helper for creating an edge from a traditional trunk-mode switchport to a device's vlan-aware
   * bridge domain.
   */
  public static @Nonnull L2ToBridgeDomain trunkToBridgeDomain(Map<Integer, Integer> translations) {
    return of(translateVlan(translations));
  }

  @VisibleForTesting
  public static @Nonnull L2ToBridgeDomain of(Function stateFunction) {
    return new L2ToBridgeDomain(stateFunction);
  }

  private L2ToBridgeDomain(Function stateFunction) {
    super(stateFunction);
  }
}
