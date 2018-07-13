package org.batfish.datamodel.eigrp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;

public class EigrpInterfaceSettings implements Serializable {

  private static final String PROP_ASN = "asn";
  private static final String PROP_ENABLED = "enabled";
  private static final long serialVersionUID = 1L;
  private Long _asn;
  private boolean _enabled;

  private EigrpInterfaceSettings(Builder builder) {
    _asn = builder._asn;
    _enabled = builder._enabled;
  }

  @JsonCreator
  private EigrpInterfaceSettings(
      @JsonProperty(PROP_ASN) Long asn, @JsonProperty(PROP_ENABLED) boolean enabled) {
    _asn = asn;
    _enabled = enabled;
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
    return Objects.equals(_asn, rhs._asn) && (_enabled == rhs._enabled);
  }

  @JsonProperty(PROP_ASN)
  public Long getAsNumber() {
    return _asn;
  }

  public void setAsNumber(Long asn) {
    _asn = asn;
  }

  @JsonProperty(PROP_ENABLED)
  public boolean getEnabled() {
    return _enabled;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_asn, _enabled);
  }

  public static class Builder {
    private Long _asn;

    private boolean _enabled;

    private Builder() {}

    public EigrpInterfaceSettings build() {
      return new EigrpInterfaceSettings(this);
    }

    public Builder setAsn(Long asn) {
      _asn = asn;
      return this;
    }

    public Builder setEnabled(boolean enabled) {
      _enabled = enabled;
      return this;
    }
  }
}
