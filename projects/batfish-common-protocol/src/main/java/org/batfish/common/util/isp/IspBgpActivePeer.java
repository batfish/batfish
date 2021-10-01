package org.batfish.common.util.isp;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.Ip;

/** Captures settings used to create an active BGP peer on the ISP */
final class IspBgpActivePeer extends IspBgpPeer {
  @Nonnull private final Ip _peerAddress;
  @Nonnull private final Ip _localIp;

  public IspBgpActivePeer(
      Ip peerAddress, Ip localIp, Long remoteAsn, Long localAsn, boolean ebgpMultihop) {
    super(remoteAsn, localAsn, ebgpMultihop);
    _peerAddress = peerAddress;
    _localIp = localIp;
  }

  /** Extracts parameters from {@code snapshotBgpPeer} to create IspBgpPeer */
  static IspBgpActivePeer create(BgpActivePeerConfig snapshotBgpPeer) {
    assert snapshotBgpPeer.getLocalIp() != null;
    assert snapshotBgpPeer.getPeerAddress() != null;
    return new IspBgpActivePeer(
        snapshotBgpPeer.getLocalIp(),
        snapshotBgpPeer.getPeerAddress(),
        firstNonNull(snapshotBgpPeer.getConfederationAsn(), snapshotBgpPeer.getLocalAs()),
        snapshotBgpPeer.getRemoteAsns().least(),
        snapshotBgpPeer.getEbgpMultihop());
  }

  public @Nonnull Ip getPeerAddress() {
    return _peerAddress;
  }

  public @Nonnull Ip getLocalIp() {
    return _localIp;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof IspBgpActivePeer)) {
      return false;
    }
    IspBgpActivePeer that = (IspBgpActivePeer) o;
    return _peerAddress.equals(that._peerAddress)
        && _localIp.equals(that._localIp)
        && _localAsn.equals(that._localAsn)
        && _remoteAsn.equals(that._remoteAsn)
        && (_ebgpMultiHop == that._ebgpMultiHop);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_peerAddress, _localIp, _localAsn, _remoteAsn, _ebgpMultiHop);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("peerAddress", _peerAddress)
        .add("localIp", _localIp)
        .add("basePeer", super.toString())
        .toString();
  }
}
