package org.batfish.datamodel.isp_configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * A specification for connectivity needed to establish a BGP peering session specified by {@link
 * BgpPeerInfo}. It allows users to specify where (physical/aggregate interface) the ISP connects to
 * the snapshot, and optionally, the VLAN tag to use on the ISP side. Based on this information,
 * Batfish will create layer-1 edges (multiple for an aggregate interface). The BGP peering is not
 * created if these edges conflict with existing layer-1 edges.
 *
 * <p>The IP address of this interface comes from the parent {@link BgpPeerInfo} object and
 * encapsulation is configured when VLAN tag is provided.
 *
 * <p>The specification cannot express all types of connectivity between the snapshot and ISP, such
 * as loopback-based peering at the ISP side and multiple BGP sessions at the same ISP interface.
 */
@ParametersAreNonnullByDefault
public class IspAttachment {
  private static final String PROP_HOSTNAME = "hostname";
  private static final String PROP_INTERFACE = "interface";
  private static final String PROP_VLAN_TAG = "vlan";

  @Nullable private final String _hostname;
  @Nonnull private final String _interface;
  @Nullable private final Integer _vlanTag;

  public IspAttachment(@Nullable String hostname, String iface, @Nullable Integer vlanTag) {
    _hostname = hostname;
    _interface = iface;
    _vlanTag = vlanTag;
  }

  @JsonCreator
  private static IspAttachment jsonCreator(
      @JsonProperty(PROP_HOSTNAME) @Nullable String hostname,
      @JsonProperty(PROP_INTERFACE) @Nullable String iface,
      @JsonProperty(PROP_VLAN_TAG) @Nullable Integer vlanTag) {
    checkNotNull(iface, "interface cannot be null for BgpPeerConnectivity");
    return new IspAttachment(hostname, iface, vlanTag);
  }

  public @Nullable String getHostname() {
    return _hostname;
  }

  public @Nonnull String getInterface() {
    return _interface;
  }

  public @Nullable Integer getVlanTag() {
    return _vlanTag;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof IspAttachment)) {
      return false;
    }
    IspAttachment that = (IspAttachment) o;
    return Objects.equals(_hostname, that._hostname)
        && _interface.equals(that._interface)
        && Objects.equals(_vlanTag, that._vlanTag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _interface, _vlanTag);
  }
}
