package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.filterByVlanId;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.function.StateFunction;
import org.batfish.datamodel.IntegerSpace;

/**
 * An edge from a {@link org.batfish.common.topology.bridge_domain.node.VlanAwareBridgeDomain} to an
 * {@link org.batfish.common.topology.bridge_domain.node.L3BridgedInterface}.
 */
public final class VlanAwareBridgeDomainToL3 extends Edge {

  public interface Function extends StateFunction {}

  /** Helper for creating an edge from a vlan-aware bridge to an IRB/Vlan interface. */
  public static @Nonnull VlanAwareBridgeDomainToL3 bridgeDomainToIrb(int vlanId) {
    return of(filterByVlanId(IntegerSpace.of(vlanId)));
  }

  @VisibleForTesting
  public static @Nonnull VlanAwareBridgeDomainToL3 of(Function stateFunction) {
    return new VlanAwareBridgeDomainToL3(stateFunction);
  }

  private VlanAwareBridgeDomainToL3(Function stateFunction) {
    super(stateFunction);
  }
}
