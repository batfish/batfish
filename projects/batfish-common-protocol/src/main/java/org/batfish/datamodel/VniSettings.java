package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Settings for a specific VXLAN segment */
@ParametersAreNonnullByDefault
public final class VniSettings implements Serializable {

  public static final class Builder {

    private SortedSet<Ip> _bumTransportIps;
    private BumTransportMethod _bumTransportMethod;
    private Ip _sourceAddress;
    private Integer _udpPort;
    private Integer _vlan;
    private Integer _vni;

    private Builder() {}

    public @Nonnull VniSettings build() {
      return create(_bumTransportIps, _bumTransportMethod, _sourceAddress, _udpPort, _vlan, _vni);
    }

    public @Nonnull Builder setBumTransportIps(SortedSet<Ip> bumTransportIps) {
      _bumTransportIps = ImmutableSortedSet.copyOf(bumTransportIps);
      return this;
    }

    public @Nonnull Builder setBumTransportMethod(BumTransportMethod bumTransportMethod) {
      _bumTransportMethod = bumTransportMethod;
      return this;
    }

    public @Nonnull Builder setSourceAddress(Ip sourceAddress) {
      _sourceAddress = sourceAddress;
      return this;
    }

    public @Nonnull Builder setUdpPort(Integer udpPort) {
      _udpPort = udpPort;
      return this;
    }

    public @Nonnull Builder setVlan(Integer vlan) {
      _vlan = vlan;
      return this;
    }

    public @Nonnull Builder setVni(int vni) {
      _vni = vni;
      return this;
    }
  }

  public static final Integer DEFAULT_UDP_PORT = 4789;
  private static final String PROP_BUM_TRANSPORT_IPS = "bumTransportIps";
  private static final String PROP_BUM_TRANSPORT_METHOD = "bumTransportMethod";
  private static final String PROP_SOURCE_ADDRESS = "sourceAddress";
  private static final String PROP_UDP_PORT = "udpPort";
  private static final String PROP_VLAN = "vlan";
  private static final String PROP_VNI = "vni";
  private static final long serialVersionUID = 1L;

  private final SortedSet<Ip> _bumTransportIps;
  private final BumTransportMethod _bumTransportMethod;
  private final Ip _sourceAddress;
  private final Integer _udpPort;
  private final Integer _vlan;
  private final int _vni;

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private VniSettings(
      SortedSet<Ip> bumTransportIps,
      BumTransportMethod bumTransportMethod,
      @Nullable Ip sourceAddress,
      Integer udpPort,
      @Nullable Integer vlan,
      int vni) {
    _bumTransportIps = bumTransportIps;
    _bumTransportMethod = bumTransportMethod;
    _sourceAddress = sourceAddress;
    _udpPort = udpPort;
    _vlan = vlan;
    _vni = vni;
  }

  @JsonCreator
  private static @Nonnull VniSettings create(
      @Nullable @JsonProperty(PROP_BUM_TRANSPORT_IPS) SortedSet<Ip> bumTransportIps,
      @Nullable @JsonProperty(PROP_BUM_TRANSPORT_METHOD) BumTransportMethod bumTransportMethod,
      @Nullable @JsonProperty(PROP_SOURCE_ADDRESS) Ip sourceAddress,
      @Nullable @JsonProperty(PROP_UDP_PORT) Integer udpPort,
      @Nullable @JsonProperty(PROP_VLAN) Integer vlan,
      @Nullable @JsonProperty(PROP_VNI) Integer vni) {
    SortedSet<Ip> deserializedBumTransportIps =
        ImmutableSortedSet.copyOf(firstNonNull(bumTransportIps, ImmutableSortedSet.of()));
    checkArgument(vni != null, "VNI must not be null.");
    checkArgument(bumTransportMethod != null, "BumTransportMethod must not be null.");
    checkArgument(
        bumTransportMethod != BumTransportMethod.MULTICAST_GROUP
            || deserializedBumTransportIps.size() <= 1,
        "Cannot specify more than one multicast group.");
    return new VniSettings(
        deserializedBumTransportIps,
        bumTransportMethod,
        sourceAddress,
        firstNonNull(udpPort, DEFAULT_UDP_PORT),
        vlan,
        vni);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VniSettings)) {
      return false;
    }
    VniSettings rhs = (VniSettings) obj;
    return Objects.equals(_bumTransportMethod, rhs._bumTransportMethod)
        && Objects.equals(_bumTransportIps, rhs._bumTransportIps)
        && Objects.equals(_sourceAddress, rhs._sourceAddress)
        && Objects.equals(_udpPort, rhs._udpPort)
        && Objects.equals(_vlan, rhs._vlan)
        && _vni == rhs._vni;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _bumTransportMethod, _bumTransportIps, _sourceAddress, _udpPort, _vlan, _vni);
  }

  @JsonIgnore
  public @Nullable Ip getMulticastGroup() {
    return _bumTransportMethod == BumTransportMethod.MULTICAST_GROUP
        ? _bumTransportIps.first()
        : null;
  }

  @JsonProperty(PROP_BUM_TRANSPORT_IPS)
  public @Nonnull SortedSet<Ip> getBumTransportIps() {
    return _bumTransportIps;
  }

  @JsonProperty(PROP_BUM_TRANSPORT_METHOD)
  public @Nonnull BumTransportMethod getBumTransportMethod() {
    return _bumTransportMethod;
  }

  @JsonProperty(PROP_SOURCE_ADDRESS)
  public Ip getSourceAddress() {
    return _sourceAddress;
  }

  @JsonProperty(PROP_UDP_PORT)
  public @Nonnull Integer getUdpPort() {
    return _udpPort;
  }

  @JsonProperty(PROP_VLAN)
  public Integer getVlan() {
    return _vlan;
  }

  @JsonProperty(PROP_VNI)
  public int getVni() {
    return _vni;
  }
}
