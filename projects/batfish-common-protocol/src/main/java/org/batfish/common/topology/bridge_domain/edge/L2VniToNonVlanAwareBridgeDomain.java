package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.function.StateFunction;
import org.batfish.common.topology.bridge_domain.node.NonVlanAwareBridgeDomain;

/**
 * An edge from an {@link org.batfish.common.topology.bridge_domain.node.L2Vni} to a {@link
 * NonVlanAwareBridgeDomain}.
 */
public final class L2VniToNonVlanAwareBridgeDomain extends L2VniToBridgeDomain {

  public interface Function extends StateFunction {}

  /** Helper for creating an edge from a layer-2 VNI to some non-vlan-aware bridge domain. */
  public static @Nonnull L2VniToNonVlanAwareBridgeDomain instance() {
    return INSTANCE;
  }

  @VisibleForTesting
  public static @Nonnull L2VniToNonVlanAwareBridgeDomain of(Function stateFunction) {
    return new L2VniToNonVlanAwareBridgeDomain(stateFunction);
  }

  private static final L2VniToNonVlanAwareBridgeDomain INSTANCE = of(identity());

  private L2VniToNonVlanAwareBridgeDomain(Function stateFunction) {
    super(stateFunction);
  }
}
