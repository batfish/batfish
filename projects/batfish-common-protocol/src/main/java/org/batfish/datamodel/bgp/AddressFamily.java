package org.batfish.datamodel.bgp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Base class for all BGP address family config */
@ParametersAreNonnullByDefault
public abstract class AddressFamily implements Serializable {
  static final String PROP_EXPORT_POLICY = "exportPolicy";
  static final String PROP_EXPORT_POLICY_SOURCES = "exportPolicySources";
  static final String PROP_IMPORT_POLICY = "importPolicy";
  static final String PROP_IMPORT_POLICY_SOURCES = "importPolicySources";

  @Nonnull protected final AddressFamilySettings _addressFamilySettings;
  // Policies
  @Nullable protected final String _exportPolicy;
  @Nullable protected final String _importPolicy;
  // Policy sources
  @Nonnull protected SortedSet<String> _importPolicySources;
  @Nonnull protected SortedSet<String> _exportPolicySources;

  protected AddressFamily(
      @Nonnull AddressFamilySettings addressFamilySettings,
      @Nullable String exportPolicy,
      SortedSet<String> exportPolicySources,
      @Nullable String importPolicy,
      SortedSet<String> importPolicySources) {
    _addressFamilySettings = addressFamilySettings;
    _exportPolicy = exportPolicy;
    _exportPolicySources = exportPolicySources;
    _importPolicy = importPolicy;
    _importPolicySources = importPolicySources;
  }

  @Nonnull
  public AddressFamilySettings getAddressFamilySettings() {
    return _addressFamilySettings;
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

  @JsonIgnore
  public abstract Type getType();

  /** Builder for an {@link AddressFamily} */
  @ParametersAreNonnullByDefault
  public abstract static class Builder<B extends Builder<B, F>, F extends AddressFamily> {

    @Nullable protected AddressFamilySettings _addressFamilySettings;
    @Nullable protected String _exportPolicy;
    @Nullable protected String _importPolicy;
    @Nonnull protected SortedSet<String> _importPolicySources = ImmutableSortedSet.of();
    @Nonnull protected SortedSet<String> _exportPolicySources = ImmutableSortedSet.of();

    public B setAddressFamilySettings(@Nullable AddressFamilySettings addressFamilySettings) {
      _addressFamilySettings = addressFamilySettings;
      return getThis();
    }

    public B setExportPolicy(@Nullable String exportPolicy) {
      _exportPolicy = exportPolicy;
      return getThis();
    }

    public B setImportPolicy(@Nullable String importPolicy) {
      _importPolicy = importPolicy;
      return getThis();
    }

    public B setImportPolicySources(SortedSet<String> importPolicySources) {
      _importPolicySources = importPolicySources;
      return getThis();
    }

    public B setExportPolicySources(SortedSet<String> exportPolicySources) {
      _exportPolicySources = exportPolicySources;
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
