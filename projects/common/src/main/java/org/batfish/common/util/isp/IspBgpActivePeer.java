package org.batfish.common.util.isp;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.Ip;

/** Captures settings used to create an active BGP peer on the ISP */
final class IspBgpActivePeer extends IspBgpPeer {
  private final @Nonnull Ip _peerAddress;
  private final @Nonnull Ip _localIp;

  public IspBgpActivePeer(
      Ip peerAddress, Ip localIp, Long remoteAsn, Long localAsn, boolean ebgpMultihop) {
    super(remoteAsn, localAsn, ebgpMultihop);
    _peerAddress = peerAddress;
    _localIp = localIp;
  }

  /** Extracts parameters from {@code snapshotBgpPeer} to create IspBgpPeer */
  static IspBgpActivePeer create(BgpActivePeerConfig snapshotBgpPeer) {
    checkArgument(
        Objects.nonNull(snapshotBgpPeer.getLocalIp()),
        "Local IP must not be null when creating the ISP BGP peer");
    return create(snapshotBgpPeer, snapshotBgpPeer.getLocalIp());
  }

  /** Extracts parameters from {@code snapshotBgpPeer} to create IspBgpPeer */
  static IspBgpActivePeer create(BgpActivePeerConfig snapshotBgpPeer, Ip snapshotLocalIp) {
    assert snapshotLocalIp != null;
    assert snapshotBgpPeer.getPeerAddress() != null;
    assert snapshotBgpPeer.getLocalAs() != null;
    return new IspBgpActivePeer(
        snapshotLocalIp,
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
    if (this == o) {
      return true;
    }
    if (!(o instanceof IspBgpActivePeer)) {
      return false;
    }
    IspBgpActivePeer that = (IspBgpActivePeer) o;
    return _peerAddress.equals(that._peerAddress)
        && _localIp.equals(that._localIp)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_peerAddress, _localIp, super.hashCode());
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
