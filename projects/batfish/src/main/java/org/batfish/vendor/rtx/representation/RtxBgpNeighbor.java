package org.batfish.vendor.rtx.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

/** A BGP peer declared by {@code bgp neighbor <id> <remote-as> <remote-address>}. */
public final class RtxBgpNeighbor implements Serializable {

  private final int _id;
  private final long _remoteAs;
  private final @Nonnull Ip _remoteAddress;

  public RtxBgpNeighbor(int id, long remoteAs, Ip remoteAddress) {
    _id = id;
    _remoteAs = remoteAs;
    _remoteAddress = remoteAddress;
  }

  /** The neighbor's identifying number, which other {@code bgp neighbor} commands refer to. */
  public int getId() {
    return _id;
  }

  public long getRemoteAs() {
    return _remoteAs;
  }

  public @Nonnull Ip getRemoteAddress() {
    return _remoteAddress;
  }
}
