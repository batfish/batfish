package org.batfish.vendor.arista.representation.eos;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;

/** Config for BGP unnumbered neighbors (created using "neighbor interface ... peer-group") */
public class AristaBgpInterfaceNeighbor extends AristaBgpNeighbor implements AristaBgpHasPeerGroup {
  private final @Nonnull String _interfaceName;
  private @Nullable String _peerFilter;
  private @Nullable String _peerGroup;

  public AristaBgpInterfaceNeighbor(@Nonnull String interfaceName) {
    _interfaceName = interfaceName;
  }

  public @Nonnull String getInterfaceName() {
    return _interfaceName;
  }

  public @Nullable String getPeerFilter() {
    return _peerFilter;
  }

  public AristaBgpInterfaceNeighbor setPeerFilter(@Nullable String peerFilter) {
    _peerFilter = peerFilter;
    return this;
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
      AristaBgpNeighborAddressFamily neighbor = v4.getOrCreateInterfaceNeighbor(_interfaceName);
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
      AristaBgpNeighborAddressFamily neighbor = evpn.getOrCreateInterfaceNeighbor(_interfaceName);
      neighbor.inheritFrom(getGenericAddressFamily());
      pgName.map(evpn::getPeerGroup).ifPresent(neighbor::inheritFrom);
      pgn.map(AristaBgpPeerGroupNeighbor::getGenericAddressFamily).ifPresent(neighbor::inheritFrom);
    }
  }
}
