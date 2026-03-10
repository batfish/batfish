package org.batfish.datamodel.vxlan;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;

/** Configuration for an L3 VXLAN VNI */
public final class Layer3Vni implements Vni {
  private final @Nonnull Set<Ip> _learnedNexthopVtepIps;
  private final @Nullable Ip _sourceAddress;
  private final int _udpPort;
  private final int _vni;
  private final @Nonnull String _srcVrf;

  private Layer3Vni(
      Set<Ip> learnedNexthopVtepIps,
      @Nullable Ip sourceAddress,
      int udpPort,
      int vni,
      String srcVrf) {
    _learnedNexthopVtepIps = learnedNexthopVtepIps;
    _sourceAddress = sourceAddress;
    _udpPort = udpPort;
    _vni = vni;
    _srcVrf = srcVrf;
  }

  /**
   * The set of all VTEP IPs of all {@link org.batfish.datamodel.route.nh.NextHopVtep} routes for
   * this VNI in the VRF corresponding to this Layer3 VNI.
   */
  public @Nonnull Set<Ip> getLearnedNexthopVtepIps() {
    return _learnedNexthopVtepIps;
  }

  @Override
  public @Nullable Ip getSourceAddress() {
    return _sourceAddress;
  }

  @Override
  public int getUdpPort() {
    return _udpPort;
  }

  @Override
  public int getVni() {
    return _vni;
  }

  @Override
  public @Nonnull String getSrcVrf() {
    return _srcVrf;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Layer3Vni)) {
      return false;
    }
    Layer3Vni layer3Vni = (Layer3Vni) o;
    return _vni == layer3Vni._vni
        && _learnedNexthopVtepIps.equals(layer3Vni._learnedNexthopVtepIps)
        && Objects.equals(_sourceAddress, layer3Vni._sourceAddress)
        && _udpPort == layer3Vni._udpPort
        && _srcVrf.equals(layer3Vni._srcVrf);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_learnedNexthopVtepIps, _sourceAddress, _udpPort, _vni, _srcVrf);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("learnedNexthopVtepIps", _learnedNexthopVtepIps)
        .add("sourceAddress", _sourceAddress)
        .add("udpPort", _udpPort)
        .add("vni", _vni)
        .add("srcVrf", _srcVrf)
        .toString();
  }

  public static Builder builder() {
    return new Builder();
  }

  public @Nonnull Builder toBuilder() {
    return builder()
        .setLearnedNexthopVtepIps(_learnedNexthopVtepIps)
        .setSourceAddress(_sourceAddress)
        .setVni(_vni)
        .setSrcVrf(_srcVrf)
        .setUdpPort(_udpPort);
  }

  public static Builder testBuilder() {
    return builder().setSrcVrf(Configuration.DEFAULT_VRF_NAME);
  }

  /** Builder for {@link Layer3Vni} */
  public static final class Builder {
    private @Nonnull Set<Ip> _learnedNexthopVtepIps = ImmutableSet.of();
    private @Nullable Ip _sourceAddress;
    private @Nullable Integer _udpPort = Vni.DEFAULT_UDP_PORT;
    private @Nullable Integer _vni;
    private @Nullable String _srcVrf;

    private Builder() {}

    public @Nonnull Builder setLearnedNexthopVtepIps(Set<Ip> learnedNexthopVtepIps) {
      _learnedNexthopVtepIps = learnedNexthopVtepIps;
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

    public @Nonnull Builder setVni(int vni) {
      _vni = vni;
      return this;
    }

    public @Nonnull Builder setSrcVrf(String srcVrf) {
      _srcVrf = srcVrf;
      return this;
    }

    public @Nonnull Layer3Vni build() {
      checkArgument(_vni != null, "VNI must not be null.");
      checkArgument(_srcVrf != null, "Source VRF for VNI cannot be null");
      return new Layer3Vni(
          ImmutableSet.copyOf(_learnedNexthopVtepIps),
          _sourceAddress,
          firstNonNull(_udpPort, DEFAULT_UDP_PORT),
          _vni,
          _srcVrf);
    }
  }
}
