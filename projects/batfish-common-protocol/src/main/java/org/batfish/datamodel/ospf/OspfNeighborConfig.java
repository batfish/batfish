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

/** Configuration for one end of an OSPF adjacency */
@ParametersAreNonnullByDefault
public final class OspfNeighborConfig implements Serializable {
  private static final String PROP_AREA = "area";
  private static final String PROP_HOSTNAME = "hostname";
  private static final String PROP_INTERFACE = "interface";
  private static final String PROP_PASSIVE = "passive";
  private static final String PROP_VRF = "vrf";

  private static final long serialVersionUID = 1L;

  private final long _area;
  private final String _interfaceName;
  private final boolean _isPassive;
  private final String _hostname;
  private final String _vrfName;

  private OspfNeighborConfig(
      long area, String interfaceName, boolean isPassive, String hostname, String vrfName) {
    _area = area;
    _interfaceName = interfaceName;
    _isPassive = isPassive;
    _hostname = hostname;
    _vrfName = vrfName;
  }

  @JsonCreator
  private static OspfNeighborConfig create(
      @Nullable @JsonProperty(PROP_AREA) Long area,
      @Nullable @JsonProperty(PROP_INTERFACE) String interfaceName,
      @Nullable @JsonProperty(PROP_PASSIVE) Boolean passive,
      @Nullable @JsonProperty(PROP_HOSTNAME) String hostname,
      @Nullable @JsonProperty(PROP_VRF) String vrf) {
    checkArgument(area != null, "OspfNeighborConfig missing %s", PROP_AREA);
    checkArgument(interfaceName != null, "OspfNeighborConfig missing %s", PROP_INTERFACE);
    checkArgument(hostname != null, "OspfNeighborConfig missing %s", PROP_HOSTNAME);
    checkArgument(vrf != null, "OspfNeighborConfig missing %s", PROP_VRF);
    return new OspfNeighborConfig(
        area, interfaceName, firstNonNull(passive, Boolean.FALSE), hostname, vrf);
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
    @Nullable private Boolean _isPassive;
    @Nullable private String _hostname;
    @Nullable private String _vrfName;

    private Builder() {}

    public Builder setArea(Long area) {
      this._area = area;
      return this;
    }

    public Builder setInterfaceName(@Nonnull String interfaceName) {
      this._interfaceName = interfaceName;
      return this;
    }

    public Builder setHostname(@Nonnull String hostname) {
      this._hostname = hostname;
      return this;
    }

    public Builder setVrfName(@Nonnull String vrfName) {
      this._vrfName = vrfName;
      return this;
    }

    public Builder setPassive(Boolean passive) {
      this._isPassive = passive;
      return this;
    }

    public OspfNeighborConfig build() {
      checkArgument(_area != null, "OspfNeighborConfig missing %s", PROP_AREA);
      checkArgument(_interfaceName != null, "OspfNeighborConfig missing %s", PROP_INTERFACE);
      checkArgument(_hostname != null, "OspfNeighborConfig missing %s", PROP_HOSTNAME);
      checkArgument(_vrfName != null, "OspfNeighborConfig missing %s", PROP_VRF);
      return new OspfNeighborConfig(
          _area, _interfaceName, firstNonNull(_isPassive, Boolean.FALSE), _hostname, _vrfName);
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
        && Objects.equals(_hostname, other._hostname)
        && Objects.equals(_vrfName, other._vrfName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_area, _interfaceName, _isPassive, _hostname, _vrfName);
  }
}
