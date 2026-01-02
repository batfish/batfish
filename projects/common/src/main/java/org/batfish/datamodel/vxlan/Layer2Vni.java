package org.batfish.datamodel.vxlan;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;

/** Settings for a VXLAN segment identified by a L2 VNI */
public final class Layer2Vni implements Vni {

  /** Builder for {@link Layer2Vni} */
  public static final class Builder {

    private @Nonnull Set<Ip> _bumTransportIps = ImmutableSet.of();
    private @Nullable BumTransportMethod _bumTransportMethod;
    private @Nullable Ip _sourceAddress;
    private @Nullable Integer _udpPort;
    private @Nullable Integer _vlan;
    private @Nullable Integer _vni;
    private @Nullable String _srcVrf;

    private Builder() {}

    public @Nonnull Layer2Vni build() {
      checkArgument(_vni != null, "VNI must not be null.");
      checkArgument(_vlan != null, "VLAN must not be null.");
      checkArgument(_bumTransportMethod != null, "BumTransportMethod must not be null.");
      checkArgument(
          _bumTransportMethod != BumTransportMethod.MULTICAST_GROUP || _bumTransportIps.size() <= 1,
          "Cannot specify more than one multicast group.");
      checkArgument(_srcVrf != null, "Source VRF for VNI cannot be null");
      return new Layer2Vni(
          _bumTransportIps,
          _bumTransportMethod,
          _sourceAddress,
          firstNonNull(_udpPort, DEFAULT_UDP_PORT),
          _vlan,
          _vni,
          _srcVrf);
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

    public @Nonnull Builder setVlan(Integer vlan) {
      _vlan = vlan;
      return this;
    }

    public @Nonnull Builder setVni(int vni) {
      _vni = vni;
      return this;
    }

    public @Nonnull Builder setSrcVrf(String srcVrf) {
      _srcVrf = srcVrf;
      return this;
    }
  }

  private final @Nonnull Set<Ip> _bumTransportIps;
  private final @Nonnull BumTransportMethod _bumTransportMethod;
  private final @Nullable Ip _sourceAddress;
  private final int _udpPort;
  private final int _vlan;
  private final int _vni;
  private final @Nonnull String _srcVrf;

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  public static @Nonnull Builder testBuilder() {
    return builder().setSrcVrf(Configuration.DEFAULT_VRF_NAME);
  }

  private Layer2Vni(
      Set<Ip> bumTransportIps,
      BumTransportMethod bumTransportMethod,
      @Nullable Ip sourceAddress,
      int udpPort,
      int vlan,
      int vni,
      String srcVrf) {
    _bumTransportIps = bumTransportIps;
    _bumTransportMethod = bumTransportMethod;
    _sourceAddress = sourceAddress;
    _udpPort = udpPort;
    _vlan = vlan;
    _vni = vni;
    _srcVrf = srcVrf;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Layer2Vni)) {
      return false;
    }
    Layer2Vni rhs = (Layer2Vni) obj;
    return Objects.equals(_bumTransportMethod, rhs._bumTransportMethod)
        && Objects.equals(_bumTransportIps, rhs._bumTransportIps)
        && Objects.equals(_sourceAddress, rhs._sourceAddress)
        && _udpPort == rhs._udpPort
        && Objects.equals(_vlan, rhs._vlan)
        && _vni == rhs._vni
        && _srcVrf.equals(rhs._srcVrf);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _bumTransportMethod.ordinal(),
        _bumTransportIps,
        _sourceAddress,
        _udpPort,
        _vlan,
        _vni,
        _srcVrf);
  }

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

  @Override
  public @Nullable Ip getSourceAddress() {
    return _sourceAddress;
  }

  @Override
  public int getUdpPort() {
    return _udpPort;
  }

  public int getVlan() {
    return _vlan;
  }

  @Override
  public int getVni() {
    return _vni;
  }

  @Override
  public @Nonnull String getSrcVrf() {
    return _srcVrf;
  }

  public @Nonnull Builder toBuilder() {
    return builder()
        .setBumTransportMethod(_bumTransportMethod)
        .setSourceAddress(_sourceAddress)
        .setVni(_vni)
        .setVlan(_vlan)
        .setUdpPort(_udpPort)
        .setBumTransportIps(_bumTransportIps)
        .setSrcVrf(_srcVrf);
  }

  /** Return a new {@link Layer2Vni} with a flood list that includes a given {@code ip} */
  public @Nonnull Layer2Vni addToFloodList(Ip ip) {
    checkArgument(
        _bumTransportMethod == BumTransportMethod.UNICAST_FLOOD_GROUP,
        "Cannot add new IPs if the transport method is not %s",
        BumTransportMethod.UNICAST_FLOOD_GROUP);
    return toBuilder()
        .setBumTransportIps(ImmutableSet.<Ip>builder().addAll(_bumTransportIps).add(ip).build())
        .build();
  }
}
