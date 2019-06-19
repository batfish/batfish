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
  private static final String PROP_METRIC = "metric";
  private static final String PROP_PASSIVE = "passive";
  private static final long serialVersionUID = 1L;
  private final long _asn;
  private final boolean _enabled;
  private @Nonnull final EigrpMetric _metric;
  private final boolean _passive;

  private EigrpInterfaceSettings(
      long asn, boolean enabled, @Nonnull EigrpMetric metric, boolean passive) {
    _asn = asn;
    _enabled = enabled;
    _metric = metric;
    _passive = passive;
  }

  @JsonCreator
  private static EigrpInterfaceSettings create(
      @JsonProperty(PROP_ASN) Long asn,
      @JsonProperty(PROP_ENABLED) boolean enabled,
      @JsonProperty(PROP_METRIC) EigrpMetric metric,
      @JsonProperty(PROP_PASSIVE) boolean passive) {
    return new EigrpInterfaceSettings(asn, enabled, metric, passive);
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
    return Objects.hash(_asn, _enabled, _metric, _passive);
  }

  public static class Builder {

    @Nullable private Long _asn;

    private boolean _enabled;

    @Nullable private EigrpMetric _metric;

    private boolean _passive;

    private Builder() {}

    @Nullable
    public EigrpInterfaceSettings build() {
      if (_asn == null || _metric == null) {
        return null;
      }
      return new EigrpInterfaceSettings(_asn, _enabled, _metric, _passive);
    }

    public Builder setAsn(@Nullable Long asn) {
      _asn = asn;
      return this;
    }

    public Builder setEnabled(boolean enabled) {
      _enabled = enabled;
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
