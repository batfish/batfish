package org.batfish.datamodel.eigrp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Vrf;

/** Represents an EIGRP process on a router */
public class EigrpProcess implements Comparable<EigrpProcess>, Serializable {

  private static final String PROP_ASN = "asn";
  private static final String PROP_EXPORT_POLICY = "export-policy";
  private static final String PROP_MODE = "eigrp-mode";
  private static final String PROP_ROUTER_ID = "router-id";
  private static final long serialVersionUID = 1L;
  private final Long _asn;
  private final String _exportPolicy;
  private final EigrpProcessMode _mode;
  private final Ip _routerId;

  @JsonCreator
  public EigrpProcess(
      @JsonProperty(PROP_ASN) Long asn,
      @Nullable @JsonProperty(PROP_EXPORT_POLICY) String exportPolicy,
      @JsonProperty(PROP_MODE) EigrpProcessMode mode,
      @JsonProperty(PROP_ROUTER_ID) Ip routerId) {
    _asn = asn;
    _exportPolicy = exportPolicy;
    _mode = mode;
    _routerId = routerId;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public int compareTo(@Nonnull EigrpProcess rhs) {
    return Comparator.comparing(EigrpProcess::getAsn)
        .thenComparing(proc -> Optional.ofNullable(proc.getExportPolicy()).orElse(""))
        .thenComparing(EigrpProcess::getMode)
        .thenComparing(EigrpProcess::getRouterId)
        .compare(this, rhs);
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
        && Objects.equals(_exportPolicy, rhs._exportPolicy)
        && Objects.equals(_mode, rhs._mode)
        && Objects.equals(_routerId, rhs._routerId);
  }

  /** @return The AS number for this process */
  @JsonProperty(PROP_ASN)
  public Long getAsn() {
    return _asn;
  }

  /**
   * @return The routing policy applied to routes in the main RIB to determine which ones are
   *     exported into EIGRP and how
   */
  @JsonProperty(PROP_EXPORT_POLICY)
  @Nullable
  public String getExportPolicy() {
    return _exportPolicy;
  }

  /** @return The router-id of this EIGRP process */
  @JsonProperty(PROP_ROUTER_ID)
  public Ip getRouterId() {
    return _routerId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_asn, _exportPolicy, _mode.ordinal(), _routerId);
  }

  /** @return The EIGRP mode for this process */
  @JsonProperty(PROP_MODE)
  public EigrpProcessMode getMode() {
    return _mode;
  }

  public static class Builder {

    private Long _asn;

    @Nullable private String _exportPolicy;

    private EigrpProcessMode _mode;

    private Ip _routerId;

    private Vrf _vrf;

    private Builder() {}

    @Nullable
    public EigrpProcess build() {
      if (_asn == null || _mode == null || _routerId == null) {
        return null;
      }
      EigrpProcess proc = new EigrpProcess(_asn, _exportPolicy, _mode, _routerId);
      if (_vrf != null) {
        _vrf.getEigrpProcesses().put(_asn, proc);
      }
      return proc;
    }

    public Builder setAsNumber(Long asn) {
      _asn = asn;
      return this;
    }

    public Builder setExportPolicy(@Nullable String exportPolicy) {
      _exportPolicy = exportPolicy;
      return this;
    }

    public Builder setRouterId(Ip routerId) {
      _routerId = routerId;
      return this;
    }

    public Builder setMode(EigrpProcessMode mode) {
      _mode = mode;
      return this;
    }

    public Builder setVrf(Vrf vrf) {
      _vrf = vrf;
      return this;
    }
  }
}
