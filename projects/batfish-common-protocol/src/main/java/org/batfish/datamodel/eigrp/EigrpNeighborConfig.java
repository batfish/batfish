package org.batfish.datamodel.eigrp;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** Configuration for one end of an EIGRP adjacency */
@ParametersAreNonnullByDefault
public final class EigrpNeighborConfig implements Serializable {
  private static final String PROP_ASN = "asn";
  private static final String PROP_EXPORT_POLICY = "exportPolicy";
  private static final String PROP_HOSTNAME = "hostname";
  private static final String PROP_INTERFACE = "interface";
  private static final String PROP_IP = "ip";
  private static final String PROP_PASSIVE = "passive";
  private static final String PROP_VRF = "vrf";

  private final long _asn;
  @Nonnull private final String _exportPolicy;
  @Nonnull private final String _interfaceName;
  @Nonnull private final Ip _ip;
  private final boolean _isPassive;
  @Nonnull private final String _hostname;
  @Nonnull private final String _vrfName;

  private EigrpNeighborConfig(
      long asn,
      String exportPolicy,
      String interfaceName,
      boolean isPassive,
      String hostname,
      String vrfName,
      Ip ip) {
    _asn = asn;
    _exportPolicy = exportPolicy;
    _interfaceName = interfaceName;
    _ip = ip;
    _isPassive = isPassive;
    _hostname = hostname;
    _vrfName = vrfName;
  }

  @JsonCreator
  private static EigrpNeighborConfig create(
      @Nullable @JsonProperty(PROP_ASN) Long asn,
      @Nullable @JsonProperty(PROP_EXPORT_POLICY) String exportPolicy,
      @Nullable @JsonProperty(PROP_HOSTNAME) String hostname,
      @Nullable @JsonProperty(PROP_INTERFACE) String interfaceName,
      @Nullable @JsonProperty(PROP_IP) Ip ip,
      @Nullable @JsonProperty(PROP_PASSIVE) Boolean passive,
      @Nullable @JsonProperty(PROP_VRF) String vrf) {
    checkArgument(asn != null, "EigrpNeighborConfig missing %s", PROP_ASN);
    checkArgument(exportPolicy != null, "EigrpNeighborConfig missing %s", PROP_EXPORT_POLICY);
    checkArgument(hostname != null, "EigrpNeighborConfig missing %s", PROP_HOSTNAME);
    checkArgument(interfaceName != null, "EigrpNeighborConfig missing %s", PROP_INTERFACE);
    checkArgument(ip != null, "EigrpNeighborConfig missing %s", PROP_IP);
    checkArgument(vrf != null, "EigrpNeighborConfig missing %s", PROP_VRF);
    return new EigrpNeighborConfig(
        asn, exportPolicy, interfaceName, firstNonNull(passive, Boolean.FALSE), hostname, vrf, ip);
  }

  @JsonProperty(PROP_ASN)
  public long getAsn() {
    return _asn;
  }

  @Nonnull
  @JsonProperty(PROP_EXPORT_POLICY)
  public String getExportPolicy() {
    return _exportPolicy;
  }

  @Nonnull
  @JsonProperty(PROP_INTERFACE)
  public String getInterfaceName() {
    return _interfaceName;
  }

  @Nonnull
  @JsonProperty(PROP_IP)
  public Ip getIp() {
    return _ip;
  }

  @Nonnull
  @JsonProperty(PROP_HOSTNAME)
  public String getHostname() {
    return _hostname;
  }

  @Nonnull
  @JsonProperty(PROP_VRF)
  public String getVrfName() {
    return _vrfName;
  }

  @JsonProperty(PROP_PASSIVE)
  public boolean isPassive() {
    return _isPassive;
  }

  @JsonIgnore
  public EigrpNeighborConfigId getId() {
    return new EigrpNeighborConfigId(_asn, _hostname, _interfaceName, _vrfName);
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Builder for {@link EigrpNeighborConfig} */
  public static final class Builder {
    @Nullable private Long _asn;
    @Nullable private String _exportPolicy;
    @Nullable private String _hostname;
    @Nullable private String _interfaceName;
    @Nullable private Ip _ip;
    private boolean _isPassive;
    @Nullable private String _vrfName;

    private Builder() {}

    public Builder setAsn(Long asn) {
      _asn = asn;
      return this;
    }

    public Builder setExportPolicy(String exportPolicy) {
      _exportPolicy = exportPolicy;
      return this;
    }

    public Builder setInterfaceName(String interfaceName) {
      _interfaceName = interfaceName;
      return this;
    }

    public Builder setIp(Ip ip) {
      _ip = ip;
      return this;
    }

    public Builder setHostname(String hostname) {
      _hostname = hostname;
      return this;
    }

    public Builder setVrfName(String vrfName) {
      _vrfName = vrfName;
      return this;
    }

    public Builder setPassive(boolean passive) {
      _isPassive = passive;
      return this;
    }

    public EigrpNeighborConfig build() {
      checkArgument(_asn != null, "EigrpNeighborConfig missing %s", PROP_ASN);
      checkArgument(_exportPolicy != null, "EigrpNeighborConfig missing %s", PROP_EXPORT_POLICY);
      checkArgument(_interfaceName != null, "EigrpNeighborConfig missing %s", PROP_INTERFACE);
      checkArgument(_ip != null, "EigrpNeighborConfig missing %s", PROP_IP);
      checkArgument(_hostname != null, "EigrpNeighborConfig missing %s", PROP_HOSTNAME);
      checkArgument(_vrfName != null, "EigrpNeighborConfig missing %s", PROP_VRF);
      return new EigrpNeighborConfig(
          _asn,
          _exportPolicy,
          _interfaceName,
          firstNonNull(_isPassive, Boolean.FALSE),
          _hostname,
          _vrfName,
          _ip);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EigrpNeighborConfig)) {
      return false;
    }
    EigrpNeighborConfig other = (EigrpNeighborConfig) o;
    return _asn == other._asn
        && Objects.equals(_exportPolicy, other._exportPolicy)
        && Objects.equals(_hostname, other._hostname)
        && Objects.equals(_interfaceName, other._interfaceName)
        && Objects.equals(_ip, other._ip)
        && _isPassive == other._isPassive
        && Objects.equals(_vrfName, other._vrfName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_asn, _interfaceName, _ip, _isPassive, _hostname, _vrfName, _exportPolicy);
  }
}
