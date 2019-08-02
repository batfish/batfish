package org.batfish.datamodel.bgp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Base class for all BGP address family config */
@ParametersAreNonnullByDefault
public abstract class AddressFamily implements Serializable {
  static final String PROP_ADDRESS_FAMILY_CAPABILITIES = "addressFamilyCapabilities";
  static final String PROP_EXPORT_POLICY = "exportPolicy";
  static final String PROP_EXPORT_POLICY_SOURCES = "exportPolicySources";
  static final String PROP_IMPORT_POLICY = "importPolicy";
  static final String PROP_IMPORT_POLICY_SOURCES = "importPolicySources";
  static final String ROUTE_REFLECTOR_CLIENT = "routeReflectorClient";

  @Nonnull protected final AddressFamilyCapabilities _addressFamilyCapabilities;
  // Policies
  @Nullable protected final String _exportPolicy;
  @Nullable protected final String _importPolicy;
  // Policy sources
  @Nonnull protected SortedSet<String> _importPolicySources;
  @Nonnull protected SortedSet<String> _exportPolicySources;
  protected final boolean _routeReflectorClient;

  protected AddressFamily(
      @Nonnull AddressFamilyCapabilities addressFamilyCapabilities,
      @Nullable String exportPolicy,
      SortedSet<String> exportPolicySources,
      @Nullable String importPolicy,
      SortedSet<String> importPolicySources,
      boolean routeReflectorClient) {
    _addressFamilyCapabilities = addressFamilyCapabilities;
    _exportPolicy = exportPolicy;
    _exportPolicySources = exportPolicySources;
    _importPolicy = importPolicy;
    _importPolicySources = importPolicySources;
    _routeReflectorClient = routeReflectorClient;
  }

  @JsonProperty(PROP_ADDRESS_FAMILY_CAPABILITIES)
  @Nonnull
  public AddressFamilyCapabilities getAddressFamilyCapabilities() {
    return _addressFamilyCapabilities;
  }

  /** The policy governing all advertisements sent to this peer */
  @Nullable
  @JsonProperty(PROP_EXPORT_POLICY)
  public String getExportPolicy() {
    return _exportPolicy;
  }

  @Nonnull
  @JsonProperty(PROP_EXPORT_POLICY_SOURCES)
  public SortedSet<String> getExportPolicySources() {
    return _exportPolicySources;
  }

  /** Routing policy governing all advertisements received from this peer */
  @Nullable
  @JsonProperty(PROP_IMPORT_POLICY)
  public String getImportPolicy() {
    return _importPolicy;
  }

  @Nonnull
  @JsonProperty(PROP_IMPORT_POLICY_SOURCES)
  public SortedSet<String> getImportPolicySources() {
    return _importPolicySources;
  }

  /** Whether or not this peer is a route-reflector client of ours */
  @JsonProperty(ROUTE_REFLECTOR_CLIENT)
  public boolean getRouteReflectorClient() {
    return _routeReflectorClient;
  }

  @JsonIgnore
  public void setExportPolicySources(@Nonnull SortedSet<String> exportPolicySources) {
    _exportPolicySources = exportPolicySources;
  }

  @JsonIgnore
  public void setImportPolicySources(@Nonnull SortedSet<String> importPolicySources) {
    _importPolicySources = importPolicySources;
  }

  @Override
  public abstract int hashCode();

  @Override
  public abstract boolean equals(@Nullable Object obj);

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_addressFamilyCapabilities", _addressFamilyCapabilities)
        .add("_exportPolicy", _exportPolicy)
        .add("_importPolicy", _importPolicy)
        .add("_importPolicySources", _importPolicySources)
        .add("_exportPolicySources", _exportPolicySources)
        .add("_routeReflectorClient", _routeReflectorClient)
        .toString();
  }

  @JsonIgnore
  public abstract Type getType();

  /** Builder for an {@link AddressFamily} */
  @ParametersAreNonnullByDefault
  public abstract static class Builder<B extends Builder<B, F>, F extends AddressFamily> {

    @Nullable protected AddressFamilyCapabilities _addressFamilyCapabilities;
    @Nullable protected String _exportPolicy;
    @Nullable protected String _importPolicy;
    @Nonnull protected SortedSet<String> _importPolicySources = ImmutableSortedSet.of();
    @Nonnull protected SortedSet<String> _exportPolicySources = ImmutableSortedSet.of();
    protected boolean _routeReflectorClient;

    @Nonnull
    public B setAddressFamilyCapabilities(
        @Nullable AddressFamilyCapabilities addressFamilyCapabilities) {
      _addressFamilyCapabilities = addressFamilyCapabilities;
      return getThis();
    }

    @Nonnull
    public B setExportPolicy(@Nullable String exportPolicy) {
      _exportPolicy = exportPolicy;
      return getThis();
    }

    @Nonnull
    public B setImportPolicy(@Nullable String importPolicy) {
      _importPolicy = importPolicy;
      return getThis();
    }

    @Nonnull
    public B setImportPolicySources(SortedSet<String> importPolicySources) {
      _importPolicySources = importPolicySources;
      return getThis();
    }

    @Nonnull
    public B setExportPolicySources(SortedSet<String> exportPolicySources) {
      _exportPolicySources = exportPolicySources;
      return getThis();
    }

    @Nonnull
    public B setRouteReflectorClient(boolean routeReflectorClient) {
      _routeReflectorClient = routeReflectorClient;
      return getThis();
    }

    @Nonnull
    public abstract B getThis();

    @Nonnull
    public abstract F build();
  }

  /** BGP address family type */
  public enum Type {
    IPV4_UNICAST,
    EVPN
  }
}
