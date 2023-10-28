package org.batfish.datamodel.bgp;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** Configuration settings for EVPN address family */
@ParametersAreNonnullByDefault
public final class EvpnAddressFamily extends AddressFamily {

  private static final String PROP_L2_VNIS = "l2Vnis";
  private static final String PROP_L3_VNIS = "l3Vnis";
  private static final String PROP_PROPAGATE_UNMATCHED = "propagateUnmatched";
  private static final String PROP_NVE_IP = "nveIp";

  private final @Nonnull SortedSet<Layer2VniConfig> _l2VNIs;
  private final @Nonnull SortedSet<Layer3VniConfig> _l3VNIs;
  private final boolean _propagateUnmatched;
  private final @Nullable Ip _nveIp;

  protected EvpnAddressFamily(
      // super fields
      AddressFamilyCapabilities addressFamilyCapabilities,
      @Nullable String exportPolicy,
      SortedSet<String> exportPolicySources,
      @Nullable String importPolicy,
      SortedSet<String> importPolicySources,
      boolean routeReflectorClient,
      // local fields
      Set<Layer2VniConfig> l2Vnis,
      Set<Layer3VniConfig> l3Vnis,
      @Nullable Ip nveIp,
      boolean propagateUnmatched) {
    super(
        addressFamilyCapabilities,
        exportPolicy,
        exportPolicySources,
        importPolicy,
        importPolicySources,
        routeReflectorClient);
    _l2VNIs = ImmutableSortedSet.copyOf(l2Vnis);
    _l3VNIs = ImmutableSortedSet.copyOf(l3Vnis);
    _nveIp = nveIp;
    _propagateUnmatched = propagateUnmatched;
  }

  @JsonCreator
  private static EvpnAddressFamily create(
      // super fields
      @JsonProperty(PROP_ADDRESS_FAMILY_CAPABILITIES) @Nullable
          AddressFamilyCapabilities addressFamilyCapabilities,
      @JsonProperty(PROP_EXPORT_POLICY) @Nullable String exportPolicy,
      @JsonProperty(PROP_EXPORT_POLICY_SOURCES) @Nullable SortedSet<String> exportPolicySources,
      @JsonProperty(PROP_IMPORT_POLICY) @Nullable String importPolicy,
      @JsonProperty(PROP_IMPORT_POLICY_SOURCES) @Nullable SortedSet<String> importPolicySources,
      @JsonProperty(ROUTE_REFLECTOR_CLIENT) @Nullable Boolean routeReflectorClient,
      // local fields
      @JsonProperty(PROP_L2_VNIS) @Nullable Set<Layer2VniConfig> l2Vnis,
      @JsonProperty(PROP_L3_VNIS) @Nullable Set<Layer3VniConfig> l3Vnis,
      @JsonProperty(PROP_NVE_IP) @Nullable Ip nveIp,
      @JsonProperty(PROP_PROPAGATE_UNMATCHED) @Nullable Boolean propagateUnmatched) {
    checkArgument(propagateUnmatched != null, "Missing %s", PROP_PROPAGATE_UNMATCHED);
    return new Builder()
        .setAddressFamilyCapabilities(addressFamilyCapabilities)
        .setExportPolicy(exportPolicy)
        .setExportPolicySources(firstNonNull(exportPolicySources, ImmutableSortedSet.of()))
        .setImportPolicy(importPolicy)
        .setImportPolicySources(firstNonNull(importPolicySources, ImmutableSortedSet.of()))
        .setRouteReflectorClient(firstNonNull(routeReflectorClient, Boolean.FALSE))
        .setL2Vnis(firstNonNull(l2Vnis, ImmutableSortedSet.of()))
        .setL3Vnis(firstNonNull(l3Vnis, ImmutableSortedSet.of()))
        .setNveIp(nveIp)
        .setPropagateUnmatched(propagateUnmatched)
        .build();
  }

  /** L2 VNI associations and config */
  @JsonProperty(PROP_L2_VNIS)
  public @Nonnull SortedSet<Layer2VniConfig> getL2VNIs() {
    return _l2VNIs;
  }

  /** L3 VNI associations and config */
  @JsonProperty(PROP_L3_VNIS)
  public @Nonnull SortedSet<Layer3VniConfig> getL3VNIs() {
    return _l3VNIs;
  }

  /**
   * IP of the network virtualization edge. Should be used as NHIP for originated EVPN routes when
   * sending to neighbors.
   *
   * <p>See: https://datatracker.ietf.org/doc/html/rfc8365#section-5.1.3
   */
  @JsonProperty(PROP_NVE_IP)
  public @Nullable Ip getNveIp() {
    return _nveIp;
  }

  /**
   * Whether or not to re-advertise (propagate) EVPN routes that do not match any VNIs. If true,
   * such routes will be advertised to neighbors. If false, routes that do not match any VNI will be
   * dropped.
   */
  @JsonProperty(PROP_PROPAGATE_UNMATCHED)
  public boolean getPropagateUnmatched() {
    return _propagateUnmatched;
  }

  @Override
  public Type getType() {
    return Type.EVPN;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EvpnAddressFamily)) {
      return false;
    }
    EvpnAddressFamily that = (EvpnAddressFamily) o;
    return _addressFamilyCapabilities.equals(that._addressFamilyCapabilities)
        && Objects.equals(_exportPolicy, that._exportPolicy)
        && Objects.equals(_importPolicy, that._importPolicy)
        && _exportPolicySources.equals(that._exportPolicySources)
        && _importPolicySources.equals(that._importPolicySources)
        && _routeReflectorClient == that._routeReflectorClient
        // local fields
        && _l2VNIs.equals(that._l2VNIs)
        && _l3VNIs.equals(that._l3VNIs)
        && _propagateUnmatched == that._propagateUnmatched;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _addressFamilyCapabilities,
        _exportPolicy,
        _exportPolicySources,
        _importPolicy,
        _importPolicySources,
        _routeReflectorClient,
        _l2VNIs,
        _l3VNIs,
        _propagateUnmatched);
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  public static final class Builder extends AddressFamily.Builder<Builder, EvpnAddressFamily> {
    private @Nonnull SortedSet<Layer2VniConfig> _l2Vnis;
    private @Nonnull SortedSet<Layer3VniConfig> _l3Vnis;
    private @Nullable Boolean _propagateUnmatched;
    private @Nullable Ip _nveIp;

    private Builder() {
      _l2Vnis = ImmutableSortedSet.of();
      _l3Vnis = ImmutableSortedSet.of();
      _addressFamilyCapabilities = AddressFamilyCapabilities.builder().build();
    }

    public @Nonnull Builder setL2Vnis(Collection<Layer2VniConfig> l2Vnis) {
      _l2Vnis = ImmutableSortedSet.copyOf(l2Vnis);
      return getThis();
    }

    public @Nonnull Builder setL3Vnis(Collection<Layer3VniConfig> l3Vnis) {
      _l3Vnis = ImmutableSortedSet.copyOf(l3Vnis);
      return getThis();
    }

    public @Nonnull Builder setPropagateUnmatched(boolean propagateUnmatched) {
      _propagateUnmatched = propagateUnmatched;
      return getThis();
    }

    public @Nonnull Builder setNveIp(@Nullable Ip nveIp) {
      _nveIp = nveIp;
      return getThis();
    }

    @Override
    public @Nonnull Builder getThis() {
      return this;
    }

    @Override
    public @Nonnull EvpnAddressFamily build() {
      checkArgument(
          _addressFamilyCapabilities != null, "Missing %s", PROP_ADDRESS_FAMILY_CAPABILITIES);
      checkArgument(_propagateUnmatched != null, "Missing %s", PROP_PROPAGATE_UNMATCHED);
      return new EvpnAddressFamily(
          _addressFamilyCapabilities,
          _exportPolicy,
          _exportPolicySources,
          _importPolicy,
          _importPolicySources,
          _routeReflectorClient,
          _l2Vnis,
          _l3Vnis,
          _nveIp,
          _propagateUnmatched);
    }
  }
}
