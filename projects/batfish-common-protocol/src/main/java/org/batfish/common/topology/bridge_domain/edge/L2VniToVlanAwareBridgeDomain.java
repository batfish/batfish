package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.setVlanId;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.function.StateFunction;
import org.batfish.common.topology.bridge_domain.node.NonVlanAwareBridgeDomain;

/**
 * An edge from an {@link org.batfish.common.topology.bridge_domain.node.L2Vni} to a {@link
 * NonVlanAwareBridgeDomain}.
 */
public final class L2VniToVlanAwareBridgeDomain extends L2VniToBridgeDomain {

  public interface Function extends StateFunction {}

  /** Helper for creating an edge from a layer-2 VNI to a device's vlan-aware bridge domain. */
  public static @Nonnull L2VniToVlanAwareBridgeDomain l2VniToVlanAwareBridgeDomain(int vlan) {
    return of(setVlanId(vlan));
  }

  @VisibleForTesting
  public static @Nonnull L2VniToVlanAwareBridgeDomain of(Function stateFunction) {
    return new L2VniToVlanAwareBridgeDomain(stateFunction);
  }

  private L2VniToVlanAwareBridgeDomain(Function stateFunction) {
    super(stateFunction);
  }
}
