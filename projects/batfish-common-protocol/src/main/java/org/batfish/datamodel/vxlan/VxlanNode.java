package org.batfish.datamodel.vxlan;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Comparator.comparing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

@ParametersAreNonnullByDefault
public final class VxlanNode implements Comparable<VxlanNode> {

  public static final class Builder {

    private String _hostname;
    private Ip _sourceAddress;
    private Integer _vlan;
    private String _vrf;

    private Builder() {}

    public @Nonnull VxlanNode build() {
      return create(_hostname, _sourceAddress, _vlan, _vrf);
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

  private static final Comparator<VxlanNode> COMPARATOR =
      comparing(VxlanNode::getHostname)
          .thenComparing(VxlanNode::getVrf)
          .thenComparing(VxlanNode::getSourceAddress)
          .thenComparing(VxlanNode::getVlan);
  private static final String PROP_HOSTNAME = "hostname";
  private static final String PROP_SOURCE_ADDRESS = "sourceAddress";
  private static final String PROP_VLAN = "vlan";
  private static final String PROP_VRF = "vrf";

  public static @Nonnull Builder builder() {
    return new Builder();
  }
  @JsonCreator
  private static @Nonnull VxlanNode create(
      @JsonProperty(PROP_HOSTNAME) @Nullable String hostname,
      @JsonProperty(PROP_SOURCE_ADDRESS) @Nullable Ip sourceAddress,
      @JsonProperty(PROP_VLAN) @Nullable Integer vlan,
      @JsonProperty(PROP_VRF) @Nullable String vrf) {
    checkArgument(hostname != null, "Missing %s", PROP_HOSTNAME);
    checkArgument(sourceAddress != null, "Missing %s", PROP_SOURCE_ADDRESS);
    checkArgument(vlan != null, "Missing %s", PROP_VLAN);
    checkArgument(vrf != null, "Missing %s", PROP_VRF);
    return new VxlanNode(hostname, sourceAddress, vlan, vrf);
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
  public int compareTo(VxlanNode o) {
    return COMPARATOR.compare(this, o);
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

  @JsonProperty(PROP_HOSTNAME)
  public @Nonnull String getHostname() {
    return _hostname;
  }

  @JsonProperty(PROP_SOURCE_ADDRESS)
  public @Nonnull Ip getSourceAddress() {
    return _sourceAddress;
  }

  @JsonProperty(PROP_VLAN)
  public int getVlan() {
    return _vlan;
  }

  @JsonProperty(PROP_VRF)
  public @Nonnull String getVrf() {
    return _vrf;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _sourceAddress, _vlan, _vrf);
  }
}
