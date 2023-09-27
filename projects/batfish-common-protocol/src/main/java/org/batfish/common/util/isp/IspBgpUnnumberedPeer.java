package org.batfish.common.util.isp;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;

/** Captures settings used to create an unnumbered BGP peer on the ISP */
final class IspBgpUnnumberedPeer extends IspBgpPeer {
  private final @Nonnull String _localIfaceName;

  public IspBgpUnnumberedPeer(
      String localIfaceName, Long remoteAsn, Long localAsn, boolean ebgpMultihop) {
    super(remoteAsn, localAsn, ebgpMultihop);
    _localIfaceName = localIfaceName;
  }

  /** Extracts parameters from {@code snapshotBgpPeer} to create IspBgpPeer */
  static IspBgpUnnumberedPeer create(
      BgpUnnumberedPeerConfig snapshotBgpPeer, String snapshotIfacename) {
    return new IspBgpUnnumberedPeer(
        snapshotIfacename,
        firstNonNull(snapshotBgpPeer.getConfederationAsn(), snapshotBgpPeer.getLocalAs()),
        snapshotBgpPeer.getRemoteAsns().least(),
        snapshotBgpPeer.getEbgpMultihop());
  }

  public @Nonnull String getLocalIfacename() {
    return _localIfaceName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IspBgpUnnumberedPeer)) {
      return false;
    }
    IspBgpUnnumberedPeer that = (IspBgpUnnumberedPeer) o;
    return _localIfaceName.equals(that._localIfaceName) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_localIfaceName, super.hashCode());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("localIfaceName", _localIfaceName)
        .add("basePeer", super.toString())
        .toString();
  }
}
