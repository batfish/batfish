package org.batfish.datamodel.eigrp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Settings for a {@link org.batfish.datamodel.Interface} with an {@link EigrpProcess}. */
public class EigrpInterfaceSettings implements Serializable {
  private static final String PROP_ASN = "asn";
  private static final String PROP_ENABLED = "enabled";
  private static final String PROP_EXPORT_POLICY = "exportPolicy";
  private static final String PROP_METRIC = "metric";
  private static final String PROP_PASSIVE = "passive";

  private final long _asn;
  private final boolean _enabled;
  @Nullable private final String _exportPolicy;
  private @Nonnull final EigrpMetric _metric;
  private final boolean _passive;

  private EigrpInterfaceSettings(
      long asn,
      boolean enabled,
      @Nullable String exportPolicy,
      @Nonnull EigrpMetric metric,
      boolean passive) {
    _asn = asn;
    _enabled = enabled;
    _exportPolicy = exportPolicy;
    _metric = metric;
    _passive = passive;
  }

  @JsonCreator
  private static EigrpInterfaceSettings create(
      @JsonProperty(PROP_ASN) Long asn,
      @JsonProperty(PROP_ENABLED) boolean enabled,
      @Nullable @JsonProperty(PROP_EXPORT_POLICY) String exportPolicy,
      @JsonProperty(PROP_METRIC) EigrpMetric metric,
      @JsonProperty(PROP_PASSIVE) boolean passive) {
    return new EigrpInterfaceSettings(asn, enabled, exportPolicy, metric, passive);
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
        && Objects.equals(_metric, rhs._metric)
        && _passive == rhs._passive;
  }

  /** @return The AS number for this interface */
  @JsonProperty(PROP_ASN)
  public long getAsn() {
    return _asn;
  }

  /** @return Whether EIGRP is enabled on this interface */
  @JsonProperty(PROP_ENABLED)
  public boolean getEnabled() {
    return _enabled;
  }

  /** @return Name of the export policy for this interface if there is any */
  @Nullable
  public String getExportPolicy() {
    return _exportPolicy;
  }

  /** @return The interface metric */
  @JsonProperty(PROP_METRIC)
  @Nonnull
  public EigrpMetric getMetric() {
    return _metric;
  }

  /** @return Whether interface is passive */
  @JsonProperty(PROP_PASSIVE)
  public boolean getPassive() {
    return _passive;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_asn, _enabled, _exportPolicy, _metric, _passive);
  }

  public static class Builder {

    @Nullable private Long _asn;

    private boolean _enabled;

    @Nullable private String _exportPolicy;

    @Nullable private EigrpMetric _metric;

    private boolean _passive;

    private Builder() {}

    @Nullable
    public EigrpInterfaceSettings build() {
      if (_asn == null || _metric == null) {
        return null;
      }
      return new EigrpInterfaceSettings(_asn, _enabled, _exportPolicy, _metric, _passive);
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

    public Builder setMetric(@Nullable EigrpMetric metric) {
      _metric = metric;
      return this;
    }

    public Builder setPassive(boolean passive) {
      _passive = passive;
      return this;
    }
  }
}
