package org.batfish.datamodel.packet_policy;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Check if the outgoing interface for a packet (after regular dest-based FIB lookup) matches one of
 * the given interfaces.
 *
 * <p><em>This does not route the packet/flow</em>
 */
public class FibLookupOutgoingInterfaceIsOneOf implements BoolExpr {

  private final @Nonnull VrfExpr _vrf;
  private final @Nonnull Set<String> _interfaceNames;

  private static final String PROP_VRF = "vrf";
  private static final String PROP_INTERFACES = "interfaces";

  public FibLookupOutgoingInterfaceIsOneOf(VrfExpr vrf, Iterable<String> interfaceNames) {
    _vrf = vrf;
    _interfaceNames = ImmutableSet.copyOf(interfaceNames);
  }

  /** VRF for which to do the FIB lookup in */
  @JsonProperty(PROP_VRF)
  public @Nonnull VrfExpr getVrf() {
    return _vrf;
  }

  @JsonIgnore
  public @Nonnull Set<String> getInterfaceNames() {
    return _interfaceNames;
  }

  @JsonProperty(PROP_INTERFACES)
  private @Nonnull SortedSet<String> getInterfaceNamesJson() {
    return ImmutableSortedSet.copyOf(_interfaceNames);
  }

  @Override
  public <T> T accept(BoolExprVisitor<T> tBoolExprVisitor) {
    return tBoolExprVisitor.visitFibLookupOutgoingInterfaceIsOneOf(this);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FibLookupOutgoingInterfaceIsOneOf)) {
      return false;
    }
    FibLookupOutgoingInterfaceIsOneOf that = (FibLookupOutgoingInterfaceIsOneOf) o;
    return _vrf.equals(that._vrf) && _interfaceNames.equals(that._interfaceNames);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_vrf, _interfaceNames);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(FibLookupOutgoingInterfaceIsOneOf.class)
        .add(PROP_VRF, _vrf)
        .add(PROP_INTERFACES, _interfaceNames)
        .toString();
  }

  @JsonCreator
  private static FibLookupOutgoingInterfaceIsOneOf create(
      @JsonProperty(PROP_VRF) @Nullable VrfExpr vrf,
      @JsonProperty(PROP_INTERFACES) @Nullable Set<String> interfaces) {
    checkArgument(vrf != null, "Missing %s", PROP_VRF);
    return new FibLookupOutgoingInterfaceIsOneOf(vrf, firstNonNull(interfaces, ImmutableSet.of()));
  }
}
