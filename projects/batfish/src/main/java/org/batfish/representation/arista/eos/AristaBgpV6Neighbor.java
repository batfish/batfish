package org.batfish.representation.arista.eos;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Ip6;

/** IPv4 BGP neighbor */
public final class AristaBgpV6Neighbor extends AristaBgpNeighbor implements AristaBgpHasPeerGroup {
  @Nonnull private final Ip6 _ip;
  @Nullable private String _peerGroup;

  public AristaBgpV6Neighbor(@Nonnull Ip6 ip) {
    _ip = ip;
  }

  @Nonnull
  public Ip6 getIp() {
    return _ip;
  }

  @Override
  @Nullable
  public String getPeerGroup() {
    return _peerGroup;
  }

  @Override
  public void setPeerGroup(@Nullable String peerGroup) {
    _peerGroup = peerGroup;
  }

  @Override
  public void inherit(AristaBgpProcess bgpGlobal, AristaBgpVrf bgpVrf, Warnings w) {
    Optional<String> pgName = Optional.ofNullable(_peerGroup);
    Optional<AristaBgpPeerGroupNeighbor> pgn = pgName.map(bgpGlobal::getPeerGroup);
    // Inherit the overall (non-address-family) specific settings.
    pgn.ifPresent(this::inheritFrom);

    /////////////////////////////////////////////////////
    //// Inherit for each address family separately. ////
    /////////////////////////////////////////////////////

    // TODO: flesh out
  }
}
