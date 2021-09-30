package org.batfish.common.util.isp;

import com.google.common.base.MoreObjects;
import javax.annotation.Nonnull;

/** Captures common settings used to create a BGP peer on the ISP node */
abstract class IspBgpPeer {
  @Nonnull protected final Long _remoteAsn;
  @Nonnull protected final Long _localAsn;

  public IspBgpPeer(Long remoteAsn, Long localAsn) {
    _remoteAsn = remoteAsn;
    _localAsn = localAsn;
  }

  public @Nonnull Long getRemoteAsn() {
    return _remoteAsn;
  }

  public @Nonnull Long getLocalAsn() {
    return _localAsn;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("remoteAsn", _remoteAsn)
        .add("localAsn", _localAsn)
        .toString();
  }
}
