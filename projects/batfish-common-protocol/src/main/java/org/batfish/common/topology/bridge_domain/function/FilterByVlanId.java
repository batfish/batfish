package org.batfish.common.topology.bridge_domain.function;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.ALL_VLAN_IDS;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.VlanAwareBridgeDomainToL2;
import org.batfish.common.topology.bridge_domain.edge.VlanAwareBridgeDomainToL2Vni;
import org.batfish.common.topology.bridge_domain.edge.VlanAwareBridgeDomainToL3;
import org.batfish.datamodel.IntegerSpace;

/** Filter that accepts state with set VLAN ID in a some space of allowed VLAN IDs. */
public interface FilterByVlanId
    extends VlanAwareBridgeDomainToL2.Function,
        VlanAwareBridgeDomainToL3.Function,
        VlanAwareBridgeDomainToL2Vni.Function {

  static FilterByVlanId of(IntegerSpace allowedVlanIds) {
    return allowedVlanIds.isEmpty()
        ? FilterByVlanIdImpl.DENY_ALL
        : allowedVlanIds.contains(ALL_VLAN_IDS)
            ? identity()
            : new FilterByVlanIdImpl(allowedVlanIds);
  }

  final class FilterByVlanIdImpl implements FilterByVlanId {

    @Override
    public final <T, U> T accept(StateFunctionVisitor<T, U> visitor, U arg) {
      return visitor.visitFilterByVlanId(this, arg);
    }

    /**
     * The space of allowed VLAN IDs in the state for which this filter accepts.
     *
     * <p>Note that this filter never accepts a state without a set VLAN ID.
     */
    public final @Nonnull IntegerSpace getAllowedVlanIds() {
      return _allowedVlanIds;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      } else if (!(o instanceof FilterByVlanIdImpl)) {
        return false;
      }
      FilterByVlanIdImpl that = (FilterByVlanIdImpl) o;
      return _allowedVlanIds.equals(that._allowedVlanIds);
    }

    @Override
    public int hashCode() {
      return _allowedVlanIds.hashCode();
    }

    private static final FilterByVlanId DENY_ALL = new FilterByVlanIdImpl(IntegerSpace.EMPTY);

    private final @Nonnull IntegerSpace _allowedVlanIds;

    private FilterByVlanIdImpl(IntegerSpace allowedVlanIds) {
      _allowedVlanIds = allowedVlanIds;
    }
  }
}
