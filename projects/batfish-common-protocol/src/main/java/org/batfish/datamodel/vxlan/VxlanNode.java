package org.batfish.datamodel.vxlan;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** A VXLAN endpoint. */
@ParametersAreNonnullByDefault
public final class VxlanNode {

  public static final class Builder {

    private String _hostname;
    private Ip _sourceAddress;
    private Integer _vlan;
    private String _vrf;

    private Builder() {}

    public @Nonnull VxlanNode build() {
      checkArgument(_hostname != null, "Missing %s", "hostname");
      checkArgument(_sourceAddress != null, "Missing %s", "sourceAddress");
      checkArgument(_vlan != null, "Missing %s", "vlan");
      checkArgument(_vrf != null, "Missing %s", "vrf");
      return new VxlanNode(_hostname, _sourceAddress, _vlan, _vrf);
    }

    public @Nonnull Builder setHostname(String hostname) {
      _hostname = hostname;
      return this;
    }

    public @Nonnull Builder setSourceAddress(Ip sourceAddress) {
      _sourceAddress = sourceAddress;
      return this;
    }

    public @Nonnull Builder setVlan(int vlan) {
      _vlan = vlan;
      return this;
    }

    public @Nonnull Builder setVrf(String vrf) {
      _vrf = vrf;
      return this;
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private final String _hostname;
  private final Ip _sourceAddress;
  private final int _vlan;
  private final String _vrf;

  public VxlanNode(String hostname, Ip sourceAddress, int vlan, String vrf) {
    _hostname = hostname;
    _sourceAddress = sourceAddress;
    _vlan = vlan;
    _vrf = vrf;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VxlanNode)) {
      return false;
    }
    VxlanNode rhs = (VxlanNode) obj;
    return _hostname.equals(rhs._hostname)
        && _sourceAddress.equals(rhs._sourceAddress)
        && _vlan == rhs._vlan
        && _vrf.equals(rhs._vrf);
  }

  /** Hostname of the endpoint. */
  public @Nonnull String getHostname() {
    return _hostname;
  }

  /** Source IP address of the VXLAN connection initiated by this node. */
  public @Nonnull Ip getSourceAddress() {
    return _sourceAddress;
  }

  /** VLAN associated with the VNI on the edges in which this {@link VxlanNode} is incident. */
  public int getVlan() {
    return _vlan;
  }

  /** VRF associated with the VXLAN connection */
  public @Nonnull String getVrf() {
    return _vrf;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _sourceAddress, _vlan, _vrf);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add("hostname", _hostname)
        .add("sourceAddress", _sourceAddress)
        .add("vlan", _vlan)
        .add("vrf", _vrf)
        .toString();
  }
}
