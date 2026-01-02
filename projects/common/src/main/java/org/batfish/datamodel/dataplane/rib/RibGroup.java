package org.batfish.datamodel.dataplane.rib;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents the configuration for defining a Rib Group -- a collection of named RIBs, with
 * attached import policies.
 */
@ParametersAreNonnullByDefault
public final class RibGroup implements Serializable {

  private static final String PROP_NAME = "name";
  private static final String PROP_EXPORT_RIB = "exportRib";
  private static final String PROP_IMPORT_POLICY = "importPolicies";
  private static final String PROP_RIB_IDS = "ribIds";

  private final @Nonnull String _name;
  private final @Nonnull List<RibId> _importRibs;
  private final @Nonnull String _importPolicy;
  private final @Nullable RibId _exportRib;

  /** Create a new RibGroup */
  public RibGroup(
      String name, List<RibId> importRibs, String importPolicy, @Nullable RibId exportRib) {
    _name = name;
    _exportRib = exportRib;
    _importPolicy = importPolicy;
    _importRibs = importRibs;
  }

  @JsonCreator
  private static RibGroup create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_RIB_IDS) @Nullable List<RibId> ribIds,
      @JsonProperty(PROP_IMPORT_POLICY) @Nullable String importPolicy,
      @JsonProperty(PROP_EXPORT_RIB) @Nullable RibId exportRib) {
    checkArgument(name != null, "RibGroup: missing %s", PROP_NAME);
    checkArgument(importPolicy != null, "RibGroup: missing %s", PROP_IMPORT_POLICY);
    return new RibGroup(name, firstNonNull(ribIds, ImmutableList.of()), importPolicy, exportRib);
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @JsonProperty(PROP_EXPORT_RIB)
  public @Nullable RibId getExportRib() {
    return _exportRib;
  }

  /**
   * Return the import policy to apply when importing routes from protocol RIBs into {@link
   * #_importRibs}
   */
  @JsonProperty(PROP_IMPORT_POLICY)
  public @Nonnull String getImportPolicy() {
    return _importPolicy;
  }

  @JsonProperty(PROP_RIB_IDS)
  public @Nonnull List<RibId> getImportRibs() {
    return _importRibs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RibGroup)) {
      return false;
    }
    RibGroup ribGroup = (RibGroup) o;
    return _name.equals(ribGroup._name)
        && Objects.equals(_exportRib, ribGroup._exportRib)
        && _importPolicy.equals(ribGroup._importPolicy)
        && _importRibs.equals(ribGroup._importRibs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _exportRib, _importPolicy, _importRibs);
  }
}
