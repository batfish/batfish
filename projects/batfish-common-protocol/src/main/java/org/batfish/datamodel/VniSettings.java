package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Settings for a specific VXLAN segment */
@ParametersAreNonnullByDefault
public final class VniSettings implements Serializable {

  public static final class Builder {

    @Nonnull private Set<Ip> _bumTransportIps = ImmutableSet.of();
    @Nullable private BumTransportMethod _bumTransportMethod;
    @Nullable private Ip _sourceAddress;
    @Nullable private Integer _udpPort;
    @Nullable private Integer _vlan;
    @Nullable private Integer _vni;

    private Builder() {}

    public @Nonnull VniSettings build() {
      checkArgument(_vni != null, "VNI must not be null.");
      checkArgument(_bumTransportMethod != null, "BumTransportMethod must not be null.");
      checkArgument(
          _bumTransportMethod != BumTransportMethod.MULTICAST_GROUP || _bumTransportIps.size() <= 1,
          "Cannot specify more than one multicast group.");
      return new VniSettings(
          _bumTransportIps,
          _bumTransportMethod,
          _sourceAddress,
          firstNonNull(_udpPort, DEFAULT_UDP_PORT),
          _vlan,
          _vni);
    }

    public @Nonnull Builder setBumTransportIps(Collection<Ip> bumTransportIps) {
      _bumTransportIps = ImmutableSortedSet.copyOf(bumTransportIps);
      return this;
    }

    public @Nonnull Builder setBumTransportMethod(BumTransportMethod bumTransportMethod) {
      _bumTransportMethod = bumTransportMethod;
      return this;
    }

    public @Nonnull Builder setSourceAddress(@Nullable Ip sourceAddress) {
      _sourceAddress = sourceAddress;
      return this;
    }

    public @Nonnull Builder setUdpPort(Integer udpPort) {
      _udpPort = udpPort;
      return this;
    }

    public @Nonnull Builder setVlan(@Nullable Integer vlan) {
      _vlan = vlan;
      return this;
    }

    public @Nonnull Builder setVni(int vni) {
      _vni = vni;
      return this;
    }
  }

  public static final Integer DEFAULT_UDP_PORT = 4789;

  @Nonnull private final Set<Ip> _bumTransportIps;
  @Nonnull private final BumTransportMethod _bumTransportMethod;
  @Nullable private final Ip _sourceAddress;
  @Nonnull private final Integer _udpPort;
  @Nullable private final Integer _vlan;
  private final int _vni;

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private VniSettings(
      Set<Ip> bumTransportIps,
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
        ? Iterables.getOnlyElement(_bumTransportIps)
        : null;
  }

  public @Nonnull Set<Ip> getBumTransportIps() {
    return _bumTransportIps;
  }

  public @Nonnull BumTransportMethod getBumTransportMethod() {
    return _bumTransportMethod;
  }

  @Nullable
  public Ip getSourceAddress() {
    return _sourceAddress;
  }

  public @Nonnull Integer getUdpPort() {
    return _udpPort;
  }

  @Nullable
  public Integer getVlan() {
    return _vlan;
  }

  public int getVni() {
    return _vni;
  }

  @Nonnull
  public Builder toBuilder() {
    return builder()
        .setBumTransportMethod(_bumTransportMethod)
        .setSourceAddress(_sourceAddress)
        .setVni(_vni)
        .setVlan(_vlan)
        .setUdpPort(_udpPort)
        .setBumTransportIps(_bumTransportIps);
  }

  /** Return a new {@link VniSettings} with a flood list that includes a given {@code ip} */
  @Nonnull
  public VniSettings addToFloodList(Ip ip) {
    checkArgument(
        _bumTransportMethod == BumTransportMethod.UNICAST_FLOOD_GROUP,
        "Cannot add new IPs if the transport method is not %s",
        BumTransportMethod.UNICAST_FLOOD_GROUP);
    return toBuilder()
        .setBumTransportIps(
            ImmutableSortedSet.<Ip>naturalOrder().addAll(_bumTransportIps).add(ip).build())
        .build();
  }
}
