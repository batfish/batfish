package org.batfish.common.topology.bridge_domain.function;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.ALL_VLAN_IDS;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IntegerSpace;

/** Filter that accepts state with set VLAN ID in a some space of allowed VLAN IDs. */
public final class FilterByVlanIdImpl implements FilterByVlanId {

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

  static FilterByVlanId of(IntegerSpace allowedVlanIds) {
    return allowedVlanIds.isEmpty()
        ? DENY_ALL
        : allowedVlanIds.contains(ALL_VLAN_IDS)
            ? identity()
            : new FilterByVlanIdImpl(allowedVlanIds);
  }

  private static final FilterByVlanIdImpl DENY_ALL = new FilterByVlanIdImpl(IntegerSpace.EMPTY);

  private final @Nonnull IntegerSpace _allowedVlanIds;

  private FilterByVlanIdImpl(IntegerSpace allowedVlanIds) {
    _allowedVlanIds = allowedVlanIds;
  }
}
