package org.batfish.representation.f5_bigip;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;

/** Configuration for a BGP neighbor representing an actual peer. */
@ParametersAreNonnullByDefault
public final class BgpNeighbor extends AbstractBgpNeighbor {

  private @Nullable Ip _address;
  private @Nullable Ip6 _address6;
  private @Nullable Integer _ebgpMultihop;
  private @Nullable String _peerGroup;

  public BgpNeighbor(String name) {
    super(name);
    Ip.tryParse(name).ifPresent(this::setAddress);
    Ip6.tryParse(name).ifPresent(this::setAddress6);
  }

  private void applyIpv4AddressFamily(BgpNeighborIpv4AddressFamily parent) {
    BgpNeighborIpv4AddressFamily af = getIpv4AddressFamily();
    if (af.getRouteMapIn() == null) {
      af.setRouteMapIn(parent.getRouteMapIn());
    }
    if (af.getRouteMapOut() == null) {
      af.setRouteMapOut(parent.getRouteMapOut());
    }
  }

  private void applyIpv6AddressFamily(BgpNeighborIpv6AddressFamily parent) {
    BgpNeighborIpv6AddressFamily af = getIpv6AddressFamily();
    if (af.getRouteMapIn() == null) {
      af.setRouteMapIn(parent.getRouteMapIn());
    }
    if (af.getRouteMapOut() == null) {
      af.setRouteMapOut(parent.getRouteMapOut());
    }
  }

  public void applyPeerGroup(BgpPeerGroup peerGroup) {
    if (_address != null) {
      applyIpv4AddressFamily(peerGroup.getIpv4AddressFamily());
    } else if (_address6 != null) {
      applyIpv6AddressFamily(peerGroup.getIpv6AddressFamily());
    }
    if (getNextHopSelf() == null) {
      setNextHopSelf(peerGroup.getNextHopSelf());
    }
    if (getRemoteAs() == null) {
      setRemoteAs(peerGroup.getRemoteAs());
    }
    if (getUpdateSource() == null) {
      setUpdateSource(peerGroup.getUpdateSource());
    }
  }

  public @Nullable Ip getAddress() {
    return _address;
  }

  public @Nullable Ip6 getAddress6() {
    return _address6;
  }

  public @Nullable Integer getEbgpMultihop() {
    return _ebgpMultihop;
  }

  public @Nullable String getPeerGroup() {
    return _peerGroup;
  }

  public void setAddress(@Nullable Ip address) {
    _address = address;
  }

  public void setAddress6(@Nullable Ip6 address6) {
    _address6 = address6;
  }

  public void setEbgpMultihop(@Nullable Integer ebgpMultihop) {
    _ebgpMultihop = ebgpMultihop;
  }

  public void setPeerGroup(@Nullable String peerGroup) {
    _peerGroup = peerGroup;
  }
}
