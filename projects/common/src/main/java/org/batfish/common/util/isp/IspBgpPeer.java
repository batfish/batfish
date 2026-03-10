package org.batfish.common.util.isp;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;

/** Captures common settings used to create a BGP peer on the ISP node */
abstract class IspBgpPeer {
  protected final @Nonnull Long _remoteAsn;
  protected final @Nonnull Long _localAsn;
  protected final boolean _ebgpMultiHop;

  public IspBgpPeer(Long remoteAsn, Long localAsn, boolean ebgpMultiHop) {
    _remoteAsn = remoteAsn;
    _localAsn = localAsn;
    _ebgpMultiHop = ebgpMultiHop;
  }

  public @Nonnull Long getRemoteAsn() {
    return _remoteAsn;
  }

  public @Nonnull Long getLocalAsn() {
    return _localAsn;
  }

  public boolean getEbgpMultiHop() {
    return _ebgpMultiHop;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("remoteAsn", _remoteAsn)
        .add("localAsn", _localAsn)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IspBgpPeer)) {
      return false;
    }
    IspBgpPeer that = (IspBgpPeer) o;
    return _localAsn.equals(that._localAsn)
        && _remoteAsn.equals(that._remoteAsn)
        && (_ebgpMultiHop == that._ebgpMultiHop);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_localAsn, _remoteAsn, _ebgpMultiHop);
  }
}
