package org.batfish.datamodel.vxlan;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.batfish.common.topology.bridge_domain.node.VlanAwareBridgeDomain.DEFAULT_VLAN_AWARE_BRIDGE_DOMAIN_NAME;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
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

    @Nonnull private Set<Ip> _bumTransportIps = ImmutableSet.of();
    @Nullable private BumTransportMethod _bumTransportMethod;
    @Nullable private Ip _sourceAddress;
    @Nullable private Integer _udpPort;
    @Nullable private Integer _vlan;
    @Nullable private String _nonVlanAwareBridgeDomain;
    @Nullable private String _vlanAwareBridgeDomain;
    @Nullable private Integer _vni;
    @Nullable private String _srcVrf;

    private Builder() {}

    public @Nonnull Layer2Vni build() {
      checkArgument(_vni != null, "VNI must not be null.");
      checkArgument(
          _vlan != null || _nonVlanAwareBridgeDomain != null,
          "Must set VLAN or non-vlan-aware bridge domain.");
      checkArgument(_bumTransportMethod != null, "BumTransportMethod must not be null.");
      checkArgument(
          _bumTransportMethod != BumTransportMethod.MULTICAST_GROUP || _bumTransportIps.size() <= 1,
          "Cannot specify more than one multicast group.");
      checkArgument(_srcVrf != null, "Source VRF for VNI cannot be null");
      String vlanAwareBridgeDomain =
          _vlan != null
              ? firstNonNull(_vlanAwareBridgeDomain, DEFAULT_VLAN_AWARE_BRIDGE_DOMAIN_NAME)
              : null;
      return new Layer2Vni(
          _bumTransportIps,
          _bumTransportMethod,
          _sourceAddress,
          firstNonNull(_udpPort, DEFAULT_UDP_PORT),
          _vlan,
          vlanAwareBridgeDomain,
          _nonVlanAwareBridgeDomain,
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

    public @Nonnull Builder setNonVlanAwareBridgeDomain(@Nullable String nonVlanAwareBridgeDomain) {
      if (nonVlanAwareBridgeDomain != null) {
        _vlan = null;
      }
      _nonVlanAwareBridgeDomain = nonVlanAwareBridgeDomain;
      return this;
    }

    public @Nonnull Builder setVlan(@Nullable Integer vlan) {
      if (vlan != null) {
        _nonVlanAwareBridgeDomain = null;
      }
      _vlan = vlan;
      return this;
    }

    public @Nonnull Builder setVlanAwareBridgeDomain(@Nullable String vlanAwareBridgeDomain) {
      if (vlanAwareBridgeDomain != null) {
        _nonVlanAwareBridgeDomain = null;
      }
      _vlanAwareBridgeDomain = vlanAwareBridgeDomain;
      return this;
    }

    public @Nonnull Builder setVni(int vni) {
      _vni = vni;
      return this;
    }

    @Nonnull
    public Builder setSrcVrf(String srcVrf) {
      _srcVrf = srcVrf;
      return this;
    }
  }

  @Nonnull private final Set<Ip> _bumTransportIps;
  @Nonnull private final BumTransportMethod _bumTransportMethod;
  @Nullable private final Ip _sourceAddress;
  private final int _udpPort;
  private final @Nullable Integer _vlan;
  private final @Nullable String _vlanAwareBridgeDomain;
  private final @Nullable String _nonVlanAwareBridgeDomain;
  private final int _vni;
  @Nonnull private final String _srcVrf;

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @Nonnull
  public static Builder testBuilder() {
    return builder().setSrcVrf(Configuration.DEFAULT_VRF_NAME);
  }

  private Layer2Vni(
      Set<Ip> bumTransportIps,
      BumTransportMethod bumTransportMethod,
      @Nullable Ip sourceAddress,
      int udpPort,
      @Nullable Integer vlan,
      @Nullable String vlanAwareBridgeDomain,
      @Nullable String nonVlanAwareBridgeDomain,
      int vni,
      String srcVrf) {
    _bumTransportIps = bumTransportIps;
    _bumTransportMethod = bumTransportMethod;
    _sourceAddress = sourceAddress;
    _udpPort = udpPort;
    _vlan = vlan;
    _vlanAwareBridgeDomain = vlanAwareBridgeDomain;
    _nonVlanAwareBridgeDomain = nonVlanAwareBridgeDomain;
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
        && Objects.equals(_vlanAwareBridgeDomain, rhs._vlanAwareBridgeDomain)
        && Objects.equals(_nonVlanAwareBridgeDomain, rhs._nonVlanAwareBridgeDomain)
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
        _vlanAwareBridgeDomain,
        _nonVlanAwareBridgeDomain,
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
  @Nullable
  public Ip getSourceAddress() {
    return _sourceAddress;
  }

  @Override
  public int getUdpPort() {
    return _udpPort;
  }

  /**
   * The VLAN to bridge to in the vlan-aware bridge.
   *
   * <p>Returns {@link Optional#empty()} if this VNI is non-vlan-aware.
   */
  public @Nonnull Optional<Integer> getVlan() {
    return Optional.ofNullable(_vlan);
  }

  /**
   * The vlan-aware bridge of which this VNI is a member.
   *
   * <p>Returns {@link Optional#empty()} if this VNI is non-vlan-aware.
   */
  public @Nonnull Optional<String> getVlanAwareBridgeDomain() {
    return Optional.ofNullable(_vlanAwareBridgeDomain);
  }

  /**
   * The non-vlan-aware bridge of which this VNI is a member.
   *
   * <p>Returns {@link Optional#empty()} if this VNI is vlan-aware.
   */
  public @Nonnull Optional<String> getNonVlanAwareBridgeDomain() {
    return Optional.ofNullable(_nonVlanAwareBridgeDomain);
  }

  @Override
  public int getVni() {
    return _vni;
  }

  @Nonnull
  @Override
  public String getSrcVrf() {
    return _srcVrf;
  }

  @Nonnull
  public Builder toBuilder() {
    return builder()
        .setBumTransportMethod(_bumTransportMethod)
        .setSourceAddress(_sourceAddress)
        .setVni(_vni)
        .setVlan(_vlan)
        .setVlanAwareBridgeDomain(_vlanAwareBridgeDomain)
        .setNonVlanAwareBridgeDomain(_nonVlanAwareBridgeDomain)
        .setUdpPort(_udpPort)
        .setBumTransportIps(_bumTransportIps)
        .setSrcVrf(_srcVrf);
  }

  /** Return a new {@link Layer2Vni} with a flood list that includes a given {@code ip} */
  @Nonnull
  public Layer2Vni addToFloodList(Ip ip) {
    checkArgument(
        _bumTransportMethod == BumTransportMethod.UNICAST_FLOOD_GROUP,
        "Cannot add new IPs if the transport method is not %s",
        BumTransportMethod.UNICAST_FLOOD_GROUP);
    return toBuilder()
        .setBumTransportIps(ImmutableSet.<Ip>builder().addAll(_bumTransportIps).add(ip).build())
        .build();
  }
}
