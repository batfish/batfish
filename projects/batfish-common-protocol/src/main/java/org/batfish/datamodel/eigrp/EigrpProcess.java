package org.batfish.datamodel.eigrp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.io.Serializable;
import java.util.Objects;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Vrf;

@JsonSchemaDescription("An EIGRP routing process")
public class EigrpProcess implements Serializable {

  private static final String PROP_ASN = "asn";
  private static final String PROP_MODE = "eigrp-mode";
  private static final String PROP_ROUTER_ID = "router-id";
  private static final long serialVersionUID = 1L;
  private final Long _asn;
  private final EigrpProcessMode _mode;
  private final Ip _routerId;

  public EigrpProcess(Long asn, EigrpProcessMode mode, Ip routerId) {
    _asn = asn;
    _mode = mode;
    _routerId = routerId;
  }

  public static Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static EigrpProcess create(
      @JsonProperty(PROP_ASN) Long asn,
      @JsonProperty(PROP_MODE) EigrpProcessMode mode,
      @JsonProperty(PROP_ROUTER_ID) Ip routerId) {
    return new EigrpProcess(asn, mode, routerId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EigrpProcess)) {
      return false;
    }
    EigrpProcess rhs = (EigrpProcess) obj;
    return Objects.equals(_asn, rhs._asn)
        && Objects.equals(_mode, rhs._mode)
        && Objects.equals(_routerId, rhs._routerId);
  }

  @JsonProperty(PROP_ROUTER_ID)
  @JsonPropertyDescription("The router-id of this EIGRP process")
  public Ip getRouterId() {
    return _routerId;
  }

  @JsonProperty(PROP_ASN)
  @JsonPropertyDescription("The AS number for this process")
  public Long getAsn() {
    return _asn;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_asn, _routerId);
  }

  @JsonProperty(PROP_MODE)
  @JsonPropertyDescription("The EIGRP mode for this process")
  public EigrpProcessMode getMode() {
    return _mode;
  }

  public static class Builder {

    private Long _asn;

    private EigrpProcessMode _mode;

    private Ip _routerId;

    private Vrf _vrf;

    private Builder() {}

    public EigrpProcess build() {
      EigrpProcess proc = new EigrpProcess(_asn, _mode, _routerId);
      if (_vrf != null) {
        _vrf.setEigrpProcess(proc);
      }
      return proc;
    }

    public Builder setAsNumber(Long asn) {
      _asn = asn;
      return this;
    }

    public Builder setRouterId(Ip routerId) {
      _routerId = routerId;
      return this;
    }

    public Builder setVrf(Vrf vrf) {
      _vrf = vrf;
      return this;
    }

    public Builder setMode(EigrpProcessMode mode) {
      _mode = mode;
      return this;
    }
  }
}
