package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.setVlanId;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.function.StateFunction;
import org.batfish.common.topology.bridge_domain.node.L3BridgedInterface;
import org.batfish.common.topology.bridge_domain.node.NonVlanAwareBridgeDomain;

/** An edge from a {@link L3BridgedInterface} to a {@link NonVlanAwareBridgeDomain}. */
public final class L3ToVlanAwareBridgeDomain extends L3ToBridgeDomain {

  public interface Function extends StateFunction {}

  /** Helper for creating an edge from an IRB/Vlan interface to a vlan-aware bridge domain. */
  public static @Nonnull L3ToVlanAwareBridgeDomain irbToBridgeDomain(int vlanId) {
    return of(setVlanId(vlanId));
  }

  @VisibleForTesting
  public static @Nonnull L3ToVlanAwareBridgeDomain of(Function stateFunction) {
    return new L3ToVlanAwareBridgeDomain(stateFunction);
  }

  private L3ToVlanAwareBridgeDomain(Function stateFunction) {
    super(stateFunction);
  }
}
