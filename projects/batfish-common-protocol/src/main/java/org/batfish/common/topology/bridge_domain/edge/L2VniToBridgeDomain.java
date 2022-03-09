package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.setVlanId;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.function.StateFunction;

/**
 * An edge from an {@link org.batfish.common.topology.bridge_domain.node.L2Vni} to a {@link
 * org.batfish.common.topology.bridge_domain.node.BridgeDomain}.
 */
public final class L2VniToBridgeDomain extends Edge {

  public interface Function extends StateFunction {}

  /** Helper for creating an edge from a layer-2 VNI to a device's vlan-aware bridge domain. */
  public static @Nonnull L2VniToBridgeDomain l2VniToVlanAwareBridgeDomain(int vlan) {
    return of(setVlanId(vlan));
  }

  /** Helper for creating an edge from a layer-2 VNI to some non-vlan-aware bridge domain. */
  public static @Nonnull L2VniToBridgeDomain l2VniToNonVlanAwareBridgeDomain() {
    return L2VNI_TO_NON_VLAN_AWARE_BRIDGE_DOMAIN;
  }

  @VisibleForTesting
  public static @Nonnull L2VniToBridgeDomain of(Function stateFunction) {
    return new L2VniToBridgeDomain(stateFunction);
  }

  private static final L2VniToBridgeDomain L2VNI_TO_NON_VLAN_AWARE_BRIDGE_DOMAIN = of(identity());

  private L2VniToBridgeDomain(Function stateFunction) {
    super(stateFunction);
  }
}
