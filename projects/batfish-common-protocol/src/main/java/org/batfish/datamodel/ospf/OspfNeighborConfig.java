package org.batfish.datamodel.ospf;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** Configuration for one end of an OSPF adjacency */
@ParametersAreNonnullByDefault
public final class OspfNeighborConfig implements Serializable {
  private static final String PROP_AREA = "area";
  private static final String PROP_HOSTNAME = "hostname";
  private static final String PROP_INTERFACE = "interface";
  private static final String PROP_IP = "ip";
  private static final String PROP_PASSIVE = "passive";
  private static final String PROP_VRF = "vrf";

  private final long _area;
  @Nonnull private final String _interfaceName;
  @Nonnull private final Ip _ip;
  private final boolean _isPassive;
  @Nonnull private final String _hostname;
  @Nonnull private final String _vrfName;

  private OspfNeighborConfig(
      long area, String interfaceName, boolean isPassive, String hostname, String vrfName, Ip ip) {
    _area = area;
    _interfaceName = interfaceName;
    _ip = ip;
    _isPassive = isPassive;
    _hostname = hostname;
    _vrfName = vrfName;
  }

  @JsonCreator
  private static OspfNeighborConfig create(
      @Nullable @JsonProperty(PROP_AREA) Long area,
      @Nullable @JsonProperty(PROP_INTERFACE) String interfaceName,
      @Nullable @JsonProperty(PROP_IP) Ip ip,
      @Nullable @JsonProperty(PROP_PASSIVE) Boolean passive,
      @Nullable @JsonProperty(PROP_HOSTNAME) String hostname,
      @Nullable @JsonProperty(PROP_VRF) String vrf) {
    checkArgument(area != null, "OspfNeighborConfig missing %s", PROP_AREA);
    checkArgument(interfaceName != null, "OspfNeighborConfig missing %s", PROP_INTERFACE);
    checkArgument(ip != null, "OspfNeighborConfig missing %s", PROP_IP);
    checkArgument(hostname != null, "OspfNeighborConfig missing %s", PROP_HOSTNAME);
    checkArgument(vrf != null, "OspfNeighborConfig missing %s", PROP_VRF);
    return new OspfNeighborConfig(
        area, interfaceName, firstNonNull(passive, Boolean.FALSE), hostname, vrf, ip);
  }

  @JsonProperty(PROP_AREA)
  public long getArea() {
    return _area;
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

  public static Builder builder() {
    return new Builder();
  }

  /** Builder for {@link OspfNeighborConfig} */
  public static final class Builder {
    @Nullable private Long _area;
    @Nullable private String _interfaceName;
    @Nullable private Ip _ip;
    @Nullable private Boolean _isPassive;
    @Nullable private String _hostname;
    @Nullable private String _vrfName;

    private Builder() {}

    public Builder setArea(Long area) {
      _area = area;
      return this;
    }

    public Builder setInterfaceName(@Nonnull String interfaceName) {
      _interfaceName = interfaceName;
      return this;
    }

    public Builder setIp(@Nonnull Ip ip) {
      _ip = ip;
      return this;
    }

    public Builder setHostname(@Nonnull String hostname) {
      _hostname = hostname;
      return this;
    }

    public Builder setVrfName(@Nonnull String vrfName) {
      _vrfName = vrfName;
      return this;
    }

    public Builder setPassive(Boolean passive) {
      _isPassive = passive;
      return this;
    }

    public OspfNeighborConfig build() {
      checkArgument(_area != null, "OspfNeighborConfig missing %s", PROP_AREA);
      checkArgument(_interfaceName != null, "OspfNeighborConfig missing %s", PROP_INTERFACE);
      checkArgument(_ip != null, "OspfNeighborConfig missing %s", PROP_IP);
      checkArgument(_hostname != null, "OspfNeighborConfig missing %s", PROP_HOSTNAME);
      checkArgument(_vrfName != null, "OspfNeighborConfig missing %s", PROP_VRF);
      return new OspfNeighborConfig(
          _area, _interfaceName, firstNonNull(_isPassive, Boolean.FALSE), _hostname, _vrfName, _ip);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OspfNeighborConfig)) {
      return false;
    }
    OspfNeighborConfig other = (OspfNeighborConfig) o;
    return _area == other._area
        && _isPassive == other._isPassive
        && Objects.equals(_interfaceName, other._interfaceName)
        && Objects.equals(_ip, other._ip)
        && Objects.equals(_hostname, other._hostname)
        && Objects.equals(_vrfName, other._vrfName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_area, _interfaceName, _ip, _isPassive, _hostname, _vrfName);
  }
}
