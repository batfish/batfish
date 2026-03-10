package org.batfish.datamodel.isp_configuration;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/**
 * BGP peer information used to create sessions to modeled ISPs. This specification identifies a BGP
 * peer in the snapshot via 1) the hostname, and 2) peer address, and 3) vrf name (optional, needed
 * if not default). VRF names are matched in a case-insensitive manner.
 *
 * <p>The specification also allows for optionally describing new connectivity needed to establish
 * the session, via {@link IspAttachment}. The BGP peering is not modeled if this new connectivity
 * conflicts with other information (e.g., L1 links conflict with existing L1).
 *
 * <p>If attachment is not provided, no interfaces are created on the ISP node to support this
 * peering. The assumption is that other mechanisms have enabled the connectivity as needed.
 */
@ParametersAreNonnullByDefault
public class BgpPeerInfo {
  private static final String PROP_HOSTNAME = "hostname";
  private static final String PROP_OVERRIDE_LOCAL_ADDRESS = "overrideLocalAddress";
  private static final String PROP_PEER_ADDRESS = "peerAddress";
  private static final String PROP_VRF = "vrf";
  private static final String PROP_ISP_ATTACHMENT = "ispAttachment";

  private final @Nonnull String _hostname;
  private final @Nullable Ip _overrideLocalAddress;
  private final @Nonnull Ip _peerAddress;
  private final @Nullable String _vrf;
  private final @Nullable IspAttachment _ispAttachment;

  public BgpPeerInfo(
      String hostname,
      @Nullable Ip overrideLocalAddress,
      Ip peerAddress,
      @Nullable String vrf,
      @Nullable IspAttachment ispAttachment) {
    _hostname = hostname.toLowerCase();
    _overrideLocalAddress = overrideLocalAddress;
    _peerAddress = peerAddress;
    _vrf = vrf;
    _ispAttachment = ispAttachment;
  }

  @JsonCreator
  private static BgpPeerInfo jsonCreator(
      @JsonProperty(PROP_HOSTNAME) @Nullable String hostname,
      @JsonProperty(PROP_OVERRIDE_LOCAL_ADDRESS) @Nullable Ip localAddress,
      @JsonProperty(PROP_PEER_ADDRESS) @Nullable Ip peerAddress,
      @JsonProperty(PROP_VRF) @Nullable String vrf,
      @JsonProperty(PROP_ISP_ATTACHMENT) @Nullable IspAttachment bgpPeerConnectivity) {
    checkArgument(hostname != null, "Missing %s", PROP_HOSTNAME);
    checkArgument(peerAddress != null, "Missing %s", PROP_PEER_ADDRESS);
    return new BgpPeerInfo(hostname, localAddress, peerAddress, vrf, bgpPeerConnectivity);
  }

  /**
   * The hostname of the device on which this peering is configured. Required in order to find the
   * configuration of the peering with the ISP.
   */
  @JsonProperty(PROP_HOSTNAME)
  public @Nonnull String getHostname() {
    return _hostname;
  }

  /**
   * The local IP on the device on which this peering is configured, used to populate the peer
   * address (aka, this device) on the generated ISP peer.
   *
   * <p>This configuration is not required if the local IP (or update source) is configured in the
   * BGP peering itself.
   */
  @JsonProperty(PROP_OVERRIDE_LOCAL_ADDRESS)
  public @Nullable Ip getOverrideLocalAddress() {
    return _overrideLocalAddress;
  }

  /**
   * The peer IP (aka, the ISP's IP) on the device on which this peering is configured. Required in
   * order to find the configuration of the peering with the ISP.
   */
  @JsonProperty(PROP_PEER_ADDRESS)
  public @Nonnull Ip getPeerAddress() {
    return _peerAddress;
  }

  /**
   * The name of the VRF in which the peering is configured.
   *
   * <p>This configuration is not required, and will use the default VRF if not populated.
   */
  @JsonProperty(PROP_VRF)
  public @Nullable String getVrf() {
    return _vrf;
  }

  /**
   * Information about where in the snapshot the ISP is physically connected. May describe a
   * location on the device indicated by {@link #getHostname()} or on a different device.
   */
  @JsonProperty(PROP_ISP_ATTACHMENT)
  public @Nullable IspAttachment getIspAttachment() {
    return _ispAttachment;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hostname", _hostname)
        .add("overrideLocalAddress", _overrideLocalAddress)
        .add("peerAddress", _peerAddress)
        .add("vrf", _vrf)
        .add("ispAttachment", _ispAttachment)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BgpPeerInfo)) {
      return false;
    }
    BgpPeerInfo that = (BgpPeerInfo) o;
    return _hostname.equals(that._hostname)
        && Objects.equals(_overrideLocalAddress, that._overrideLocalAddress)
        && _peerAddress.equals(that._peerAddress)
        && Objects.equals(_vrf, that._vrf)
        && Objects.equals(_ispAttachment, that._ispAttachment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _overrideLocalAddress, _peerAddress, _vrf, _ispAttachment);
  }
}
