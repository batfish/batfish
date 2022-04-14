package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.function.ComposeBaseImpl;
import org.batfish.common.topology.bridge_domain.function.StateFunction;
import org.batfish.common.topology.bridge_domain.node.NonVlanAwareBridgeDomain;

/**
 * An edge from a {@link NonVlanAwareBridgeDomain} to an {@link
 * org.batfish.common.topology.bridge_domain.node.L2Vni}.
 */
public final class NonVlanAwareBridgeDomainToL2Vni extends Edge {
  public interface Function extends StateFunction {}

  /** Helper for creating an edge from a non-vlan-aware bridge domain to an attached layer-2 VNI. */
  public static @Nonnull NonVlanAwareBridgeDomainToL2Vni nonVlanAwareBridgeDomainToL2Vni() {
    return NON_VLAN_AWARE_BRIDGE_DOMAIN_TO_L2_VNI;
  }

  @VisibleForTesting
  public static @Nonnull Function compose(Function func1, Function func2) {
    return func1.equals(identity())
        ? func2
        : func2.equals(identity()) ? func1 : new Compose(func1, func2);
  }

  @VisibleForTesting
  public static @Nonnull NonVlanAwareBridgeDomainToL2Vni of(Function stateFunction) {
    return new NonVlanAwareBridgeDomainToL2Vni(stateFunction);
  }

  private static final NonVlanAwareBridgeDomainToL2Vni NON_VLAN_AWARE_BRIDGE_DOMAIN_TO_L2_VNI =
      of(identity());

  private static final class Compose extends ComposeBaseImpl<Function> implements Function {

    private Compose(Function func1, Function func2) {
      super(func1, func2);
    }
  }

  private NonVlanAwareBridgeDomainToL2Vni(Function stateFunction) {
    super(stateFunction);
  }
}
