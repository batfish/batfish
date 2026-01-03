package org.batfish.datamodel.eigrp;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Settings for a {@link org.batfish.datamodel.Interface} with an {@link EigrpProcess}. */
@ParametersAreNonnullByDefault
public class EigrpInterfaceSettings implements Serializable {
  private static final String PROP_ASN = "asn";
  private static final String PROP_ENABLED = "enabled";
  private static final String PROP_EXPORT_POLICY = "exportPolicy";
  private static final String PROP_IMPORT_POLICY = "importPolicy";
  private static final String PROP_METRIC = "metric";
  private static final String PROP_PASSIVE = "passive";

  private final long _asn;
  private final boolean _enabled;
  private final @Nullable String _exportPolicy;
  private final @Nullable String _importPolicy;
  private final @Nonnull EigrpMetric _metric;
  private final boolean _passive;

  private EigrpInterfaceSettings(
      long asn,
      boolean enabled,
      @Nullable String exportPolicy,
      @Nullable String importPolicy,
      @Nonnull EigrpMetric metric,
      boolean passive) {
    _asn = asn;
    _enabled = enabled;
    _exportPolicy = exportPolicy;
    _importPolicy = importPolicy;
    _metric = metric;
    _passive = passive;
  }

  @JsonCreator
  private static EigrpInterfaceSettings create(
      @JsonProperty(PROP_ASN) @Nullable Long asn,
      @JsonProperty(PROP_ENABLED) boolean enabled,
      @JsonProperty(PROP_EXPORT_POLICY) @Nullable String exportPolicy,
      @JsonProperty(PROP_IMPORT_POLICY) @Nullable String importPolicy,
      @JsonProperty(PROP_METRIC) @Nullable EigrpMetric metric,
      @JsonProperty(PROP_PASSIVE) boolean passive) {
    checkArgument(asn != null, "Missing %s", PROP_ASN);
    checkArgument(metric != null, "Missing %s", PROP_METRIC);
    return new EigrpInterfaceSettings(asn, enabled, exportPolicy, importPolicy, metric, passive);
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EigrpInterfaceSettings)) {
      return false;
    }
    EigrpInterfaceSettings rhs = (EigrpInterfaceSettings) obj;
    return Objects.equals(_asn, rhs._asn)
        && (_enabled == rhs._enabled)
        && Objects.equals(_exportPolicy, rhs._exportPolicy)
        && Objects.equals(_importPolicy, rhs._importPolicy)
        && _metric.equals(rhs._metric)
        && _passive == rhs._passive;
  }

  /**
   * @return The AS number for this interface
   */
  @JsonProperty(PROP_ASN)
  public long getAsn() {
    return _asn;
  }

  /**
   * @return Whether EIGRP is enabled on this interface
   */
  @JsonProperty(PROP_ENABLED)
  public boolean getEnabled() {
    return _enabled;
  }

  /**
   * @return Name of the export policy for this interface if there is any
   */
  @JsonProperty(PROP_EXPORT_POLICY)
  public @Nullable String getExportPolicy() {
    return _exportPolicy;
  }

  /**
   * @return Name of the import policy for this interface if there is any
   */
  @JsonProperty(PROP_IMPORT_POLICY)
  public @Nullable String getImportPolicy() {
    return _importPolicy;
  }

  /**
   * @return The interface metric
   */
  @JsonProperty(PROP_METRIC)
  public @Nonnull EigrpMetric getMetric() {
    return _metric;
  }

  /**
   * @return Whether interface is passive
   */
  @JsonProperty(PROP_PASSIVE)
  public boolean getPassive() {
    return _passive;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_asn, _enabled, _exportPolicy, _importPolicy, _metric, _passive);
  }

  public static class Builder {

    private @Nullable Long _asn;
    private boolean _enabled;
    private @Nullable String _exportPolicy;
    private @Nullable String _importPolicy;
    private @Nullable EigrpMetric _metric;
    private boolean _passive;

    private Builder() {}

    public @Nonnull EigrpInterfaceSettings build() {
      checkArgument(_asn != null, "Missing %s", PROP_ASN);
      checkArgument(_metric != null, "Missing %s", PROP_METRIC);
      return new EigrpInterfaceSettings(
          _asn, _enabled, _exportPolicy, _importPolicy, _metric, _passive);
    }

    public Builder setAsn(@Nullable Long asn) {
      _asn = asn;
      return this;
    }

    public Builder setEnabled(boolean enabled) {
      _enabled = enabled;
      return this;
    }

    public Builder setExportPolicy(@Nullable String exportPolicy) {
      _exportPolicy = exportPolicy;
      return this;
    }

    public Builder setImportPolicy(@Nullable String importPolicy) {
      _importPolicy = importPolicy;
      return this;
    }

    public Builder setMetric(@Nonnull EigrpMetric metric) {
      _metric = metric;
      return this;
    }

    public Builder setPassive(boolean passive) {
      _passive = passive;
      return this;
    }
  }
}
