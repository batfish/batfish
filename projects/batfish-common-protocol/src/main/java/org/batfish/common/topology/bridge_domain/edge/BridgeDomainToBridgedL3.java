package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.filterByOuterTag;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.filterByVlanId;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.function.StateFunction;
import org.batfish.datamodel.IntegerSpace;

/**
 * An edge from a {@link org.batfish.common.topology.bridge_domain.node.BridgeDomain} to a {@link
 * org.batfish.common.topology.bridge_domain.node.BridgedL3Interface}.
 */
public final class BridgeDomainToBridgedL3 extends Edge {

  public interface Function extends StateFunction {}

  /**
   * Helper for creating an edge from an IOS-XR named bridge domain to its BVI (bridged virtual
   * interface).
   */
  public static @Nonnull BridgeDomainToBridgedL3 bridgeDomainToBvi() {
    return of(filterByOuterTag(IntegerSpace.EMPTY, true));
  }

  /** Helper for creating an edge from a vlan-aware bridge to an IRB/Vlan interface. */
  public static @Nonnull BridgeDomainToBridgedL3 bridgeDomainToIrb(int vlanId) {
    return of(filterByVlanId(IntegerSpace.of(vlanId)));
  }

  @VisibleForTesting
  public static @Nonnull BridgeDomainToBridgedL3 of(Function stateFunction) {
    return new BridgeDomainToBridgedL3(stateFunction);
  }

  private BridgeDomainToBridgedL3(Function stateFunction) {
    super(stateFunction);
  }
}
