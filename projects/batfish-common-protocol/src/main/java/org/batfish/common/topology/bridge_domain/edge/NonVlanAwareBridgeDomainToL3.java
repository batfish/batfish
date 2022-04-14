package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.filterByOuterTag;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.function.StateFunction;
import org.batfish.common.topology.bridge_domain.node.L3BridgedInterface;
import org.batfish.common.topology.bridge_domain.node.NonVlanAwareBridgeDomain;
import org.batfish.datamodel.IntegerSpace;

/** An edge from a {@link NonVlanAwareBridgeDomain} to a {@link L3BridgedInterface}. */
public final class NonVlanAwareBridgeDomainToL3 extends Edge {

  public interface Function extends StateFunction {}

  /**
   * Helper for creating an edge from an IOS-XR named bridge domain to its BVI (bridged virtual
   * interface).
   */
  public static @Nonnull NonVlanAwareBridgeDomainToL3 bridgeDomainToBvi() {
    return of(filterByOuterTag(IntegerSpace.EMPTY, true));
  }

  @VisibleForTesting
  public static @Nonnull NonVlanAwareBridgeDomainToL3 of(Function stateFunction) {
    return new NonVlanAwareBridgeDomainToL3(stateFunction);
  }

  private NonVlanAwareBridgeDomainToL3(Function stateFunction) {
    super(stateFunction);
  }
}
