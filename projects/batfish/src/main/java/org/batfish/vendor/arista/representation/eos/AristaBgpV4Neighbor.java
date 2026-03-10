package org.batfish.vendor.arista.representation.eos;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Ip;

/** IPv4 BGP neighbor */
public final class AristaBgpV4Neighbor extends AristaBgpNeighbor implements AristaBgpHasPeerGroup {
  private final @Nonnull Ip _ip;
  private @Nullable String _peerGroup;

  public AristaBgpV4Neighbor(@Nonnull Ip ip) {
    _ip = ip;
  }

  public @Nonnull Ip getIp() {
    return _ip;
  }

  @Override
  public @Nullable String getPeerGroup() {
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

    // V4
    AristaBgpVrfIpv4UnicastAddressFamily v4 = bgpVrf.getV4UnicastAf();
    if (v4 != null) {
      AristaBgpNeighborAddressFamily neighbor = v4.getOrCreateNeighbor(_ip);
      neighbor.inheritFrom(getGenericAddressFamily());
      pgName.map(v4::getPeerGroup).ifPresent(neighbor::inheritFrom);
      pgn.map(AristaBgpPeerGroupNeighbor::getGenericAddressFamily).ifPresent(neighbor::inheritFrom);
      // If not yet set, activate based on the vrf setting for v4 unicast.
      if (neighbor.getActivate() == null) {
        neighbor.setActivate(bgpVrf.getDefaultIpv4Unicast());
      }
    }

    // EVPN
    AristaBgpVrfEvpnAddressFamily evpn = bgpVrf.getEvpnAf();
    if (evpn != null) {
      AristaBgpNeighborAddressFamily neighbor = evpn.getOrCreateNeighbor(_ip);
      neighbor.inheritFrom(getGenericAddressFamily());
      pgName.map(evpn::getPeerGroup).ifPresent(neighbor::inheritFrom);
      pgn.map(AristaBgpPeerGroupNeighbor::getGenericAddressFamily).ifPresent(neighbor::inheritFrom);
    }
  }
}
