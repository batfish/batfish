package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.function.ComposeBaseImpl;
import org.batfish.common.topology.bridge_domain.function.StateFunction;
import org.batfish.common.topology.bridge_domain.node.NonVlanAwareBridgeDomain;

/**
 * An edge from a {@link NonVlanAwareBridgeDomain} to an {@link
 * org.batfish.common.topology.bridge_domain.node.L2Interface}.
 */
public final class NonVlanAwareBridgeDomainToL2 extends Edge {

  public interface Function extends StateFunction {}

  /**
   * Helper for creating an edge from a vlan-aware bridge domain to an IOS-XR-style l2transport
   * interface.
   */
  public static @Nonnull NonVlanAwareBridgeDomainToL2 bridgeDomainToL2Transport() {
    return new NonVlanAwareBridgeDomainToL2(identity());
  }

  @VisibleForTesting
  public static @Nonnull Function compose(Function func1, Function func2) {
    return func1.equals(identity())
        ? func2
        : func2.equals(identity()) ? func1 : new Compose(func1, func2);
  }

  @VisibleForTesting
  public static @Nonnull NonVlanAwareBridgeDomainToL2 of(Function stateFunction) {
    return new NonVlanAwareBridgeDomainToL2(stateFunction);
  }

  private NonVlanAwareBridgeDomainToL2(Function stateFunction) {
    super(stateFunction);
  }

  private static final class Compose extends ComposeBaseImpl<Function> implements Function {

    private Compose(Function func1, Function func2) {
      super(func1, func2);
    }
  }
}
