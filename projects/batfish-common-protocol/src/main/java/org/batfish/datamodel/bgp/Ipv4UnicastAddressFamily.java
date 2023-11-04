package org.batfish.datamodel.bgp;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Configuration for an IPv4 address family */
@ParametersAreNonnullByDefault
public final class Ipv4UnicastAddressFamily extends AddressFamily {

  private Ipv4UnicastAddressFamily(
      AddressFamilyCapabilities addressFamilyCapabilities,
      @Nullable String exportPolicy,
      SortedSet<String> exportPolicySources,
      @Nullable String importPolicy,
      SortedSet<String> importPolicySources,
      boolean routeReflectorClient) {
    super(
        addressFamilyCapabilities,
        exportPolicy,
        exportPolicySources,
        importPolicy,
        importPolicySources,
        routeReflectorClient);
  }

  @JsonCreator
  private static Ipv4UnicastAddressFamily jsonCreator(
      // super fields
      @JsonProperty(PROP_ADDRESS_FAMILY_CAPABILITIES) @Nullable
          AddressFamilyCapabilities addressFamilyCapabilities,
      @JsonProperty(PROP_EXPORT_POLICY) @Nullable String exportPolicy,
      @JsonProperty(PROP_EXPORT_POLICY_SOURCES) @Nullable SortedSet<String> exportPolicySources,
      @JsonProperty(PROP_IMPORT_POLICY) @Nullable String importPolicy,
      @JsonProperty(PROP_IMPORT_POLICY_SOURCES) @Nullable SortedSet<String> importPolicySources,
      @JsonProperty(ROUTE_REFLECTOR_CLIENT) @Nullable Boolean routeReflectorClient) {
    checkArgument(
        addressFamilyCapabilities != null, "Missing %s", PROP_ADDRESS_FAMILY_CAPABILITIES);
    return new Ipv4UnicastAddressFamily(
        addressFamilyCapabilities,
        exportPolicy,
        firstNonNull(exportPolicySources, ImmutableSortedSet.of()),
        importPolicy,
        firstNonNull(importPolicySources, ImmutableSortedSet.of()),
        firstNonNull(routeReflectorClient, Boolean.FALSE));
  }

  @Override
  public Type getType() {
    return Type.IPV4_UNICAST;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Ipv4UnicastAddressFamily)) {
      return false;
    }
    Ipv4UnicastAddressFamily that = (Ipv4UnicastAddressFamily) o;
    return _addressFamilyCapabilities.equals(that._addressFamilyCapabilities)
        && Objects.equals(_exportPolicy, that._exportPolicy)
        && Objects.equals(_importPolicy, that._importPolicy)
        && _exportPolicySources.equals(that._exportPolicySources)
        && _importPolicySources.equals(that._importPolicySources)
        && _routeReflectorClient == that._routeReflectorClient;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _addressFamilyCapabilities,
        _exportPolicy,
        _exportPolicySources,
        _importPolicy,
        _importPolicySources,
        _routeReflectorClient);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder
      extends AddressFamily.Builder<Builder, Ipv4UnicastAddressFamily> {

    private Builder() {
      _addressFamilyCapabilities = AddressFamilyCapabilities.builder().build();
    }

    @Override
    public @Nonnull Builder getThis() {
      return this;
    }

    @Override
    public @Nonnull Ipv4UnicastAddressFamily build() {
      checkArgument(
          _addressFamilyCapabilities != null, "Missing %s", PROP_ADDRESS_FAMILY_CAPABILITIES);
      return new Ipv4UnicastAddressFamily(
          _addressFamilyCapabilities,
          _exportPolicy,
          _exportPolicySources,
          _importPolicy,
          _importPolicySources,
          _routeReflectorClient);
    }
  }
}
