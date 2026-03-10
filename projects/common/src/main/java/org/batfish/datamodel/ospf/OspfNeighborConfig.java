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
  private final @Nonnull String _interfaceName;
  private final @Nonnull Ip _ip;
  private final boolean _isPassive;
  private final @Nonnull String _hostname;
  private final @Nonnull String _vrfName;

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
      @JsonProperty(PROP_AREA) @Nullable Long area,
      @JsonProperty(PROP_INTERFACE) @Nullable String interfaceName,
      @JsonProperty(PROP_IP) @Nullable Ip ip,
      @JsonProperty(PROP_PASSIVE) @Nullable Boolean passive,
      @JsonProperty(PROP_HOSTNAME) @Nullable String hostname,
      @JsonProperty(PROP_VRF) @Nullable String vrf) {
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

  @JsonProperty(PROP_INTERFACE)
  public @Nonnull String getInterfaceName() {
    return _interfaceName;
  }

  @JsonProperty(PROP_IP)
  public @Nonnull Ip getIp() {
    return _ip;
  }

  @JsonProperty(PROP_HOSTNAME)
  public @Nonnull String getHostname() {
    return _hostname;
  }

  @JsonProperty(PROP_VRF)
  public @Nonnull String getVrfName() {
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
    private @Nullable Long _area;
    private @Nullable String _interfaceName;
    private @Nullable Ip _ip;
    private @Nullable Boolean _isPassive;
    private @Nullable String _hostname;
    private @Nullable String _vrfName;

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
