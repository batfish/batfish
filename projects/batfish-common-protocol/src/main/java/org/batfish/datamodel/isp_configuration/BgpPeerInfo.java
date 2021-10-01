package org.batfish.datamodel.isp_configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import static com.google.common.base.Preconditions.checkArgument;
import org.batfish.datamodel.Ip;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * BGP peer information used to create sessions to modeled ISPs. This specification identifies a BGP
 * peer in the snapshot via 1) the hostname, and 2) peer address, and 3) vrf (optional, needed to
 * resolve ambiguity with peer addresses).
 *
 * <p>The specification also allows for optionally describing new connectivity needed to establish
 * the session, via {@link IspAttachment}. The BGP peering is not modeled if this new connectivity
 * conflicts with other information (e.g., L1 links conflict with existing L1).
 */
@ParametersAreNonnullByDefault
public class BgpPeerInfo {
  private static final String PROP_HOSTNAME = "hostname";
  private static final String PROP_PEER_ADDRESS = "peerAddress";
  private static final String PROP_VRF = "vrf";
  private static final String PROP_ISP_ATTACHMENT = "ispAttachment";

  @Nonnull private final String _hostname;
  @Nonnull private final Ip _peerAddress;
  @Nullable private final String _vrf;
  @Nullable private final IspAttachment _ispAttachment;

  public BgpPeerInfo(
      String hostname,
      Ip peerAddress,
      @Nullable String vrf,
      @Nullable IspAttachment ispAttachment) {
    _hostname = hostname;
    _peerAddress = peerAddress;
    _vrf = vrf;
    _ispAttachment = ispAttachment;
  }

  @JsonCreator
  private static BgpPeerInfo jsonCreator(
      @JsonProperty(PROP_HOSTNAME) @Nullable String hostname,
      @JsonProperty(PROP_PEER_ADDRESS) @Nullable Ip peerAddress,
      @JsonProperty(PROP_VRF) @Nullable String vrf,
      @JsonProperty(PROP_ISP_ATTACHMENT) @Nullable IspAttachment bgpPeerConnectivity) {
    checkArgument(hostname != null, "Missing %s", PROP_HOSTNAME);
    checkArgument(peerAddress != null, "Missing %s", PROP_PEER_ADDRESS);
    return new BgpPeerInfo(hostname, peerAddress, vrf, bgpPeerConnectivity);
  }

  public String getHostname() {
    return _hostname;
  }

  public @Nonnull Ip getPeerAddress() {
    return _peerAddress;
  }

  public @Nullable String getVrf() {
    return _vrf;
  }

  public @Nullable IspAttachment getIspAttachment() {
    return _ispAttachment;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hostname", _hostname)
        .add("peerAddress", _peerAddress)
        .add("vrf", _vrf)
        .add("ispAttachment", _ispAttachment)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BgpPeerInfo)) {
      return false;
    }
    BgpPeerInfo that = (BgpPeerInfo) o;
    return _hostname.equals(that._hostname)
        && _peerAddress.equals(that._peerAddress)
        && Objects.equals(_vrf, that._vrf)
        && Objects.equals(_ispAttachment, that._ispAttachment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _peerAddress, _vrf, _ispAttachment);
  }
}
