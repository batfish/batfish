package org.batfish.representation.frr;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.LongSpace;

/** Parent for all BGP neighbors. */
public abstract class BgpNeighbor implements Serializable {
  public static class RemoteAs implements Serializable {
    final @Nullable Long _asn;
    final @Nonnull RemoteAsType _type;

    public static RemoteAs explicit(long asn) {
      return new RemoteAs(asn, RemoteAsType.EXPLICIT);
    }

    public static RemoteAs external() {
      return new RemoteAs(null, RemoteAsType.EXTERNAL);
    }

    public static RemoteAs internal() {
      return new RemoteAs(null, RemoteAsType.INTERNAL);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (!(o instanceof RemoteAs)) {
        return false;
      }
      RemoteAs remoteAs = (RemoteAs) o;
      return Objects.equals(_asn, remoteAs._asn) && _type == remoteAs._type;
    }

    @Override
    public int hashCode() {
      return Objects.hash(_asn, _type);
    }

    /**
     * Returns the remote AS values for this remote AS configuration with the given local ASN, or
     * {@link LongSpace#EMPTY} if {@code localAs} is required to compute the AS values but is {@code
     * null}.
     */
    public @Nonnull LongSpace getRemoteAs(@Nullable Long localAs) {
      if (_type == RemoteAsType.EXPLICIT) {
        assert _asn != null;
        return LongSpace.of(_asn);
      } else if (localAs == null) {
        // For either INTERNAL or EXTERNAL, we need to know the local AS to implement this.
        return LongSpace.EMPTY;
      } else if (_type == RemoteAsType.EXTERNAL) {
        // Everything but the local ASN.
        return BgpPeerConfig.ALL_AS_NUMBERS.difference(LongSpace.of(localAs));
      } else {
        assert _type == RemoteAsType.INTERNAL;
        return LongSpace.of(localAs);
      }
    }

    /**
     * Returns {@code true} if this remote AS is known to be different from the given {@code
     * localAs}, and {@code false} otherwise.
     *
     * <p>Returns {@code false} when the result is unknown, aka, when {@code localAs} is {@code
     * null} and this is an explicitly numbered peer.
     */
    public boolean isKnownEbgp(@Nullable Long localAs) {
      return _type == RemoteAsType.EXTERNAL
          || _type == RemoteAsType.EXPLICIT && localAs != null && !localAs.equals(_asn);
    }

    /**
     * Returns {@code true} if this remote AS is known to be the same as the given {@code localAs},
     * and {@code false} otherwise.
     *
     * <p>Returns {@code false} when the result is unknown, aka, when {@code localAs} is {@code
     * null} and this is an explicitly numbered peer.
     */
    public boolean isKnownIbgp(@Nullable Long localAs) {
      return _type == RemoteAsType.INTERNAL
          || _type == RemoteAsType.EXPLICIT && localAs != null && localAs.equals(_asn);
    }

    private RemoteAs(@Nullable Long asn, RemoteAsType type) {
      _asn = asn;
      _type = type;
    }
  }

  private final @Nonnull String _name;
  private @Nullable String _description;
  private @Nullable String _peerGroup;

  // Inheritable properties
  private @Nullable RemoteAs _remoteAs;
  private @Nullable Boolean _ebgpMultihop;
  private @Nullable BgpNeighborSource _bgpNeighborSource;
  private @Nullable Long _localAs;

  // Whether this configuration has inherited from its parent.
  private boolean _inherited = false;

  public BgpNeighbor(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  public @Nullable String getPeerGroup() {
    return _peerGroup;
  }

  public void setPeerGroup(@Nullable String peerGroup) {
    _peerGroup = peerGroup;
  }

  public @Nullable Long getLocalAs() {
    return _localAs;
  }

  public @Nullable RemoteAs getRemoteAs() {
    return _remoteAs;
  }

  public @Nullable Boolean getEbgpMultihop() {
    return _ebgpMultihop;
  }

  public void setEbgpMultihop(@Nullable Boolean ebgpMultihop) {
    _ebgpMultihop = ebgpMultihop;
  }

  public void setLocalAs(@Nullable Long localAs) {
    _localAs = localAs;
  }

  public void setRemoteAs(@Nullable RemoteAs remoteAs) {
    _remoteAs = remoteAs;
  }

  protected void inheritFrom(@Nonnull BgpVrf vrf) {
    if (_inherited) {
      return;
    }
    _inherited = true;

    @Nullable BgpNeighbor other = _peerGroup == null ? null : vrf.getNeighbors().get(_peerGroup);
    if (other == null) {
      return;
    }

    // Do not inherit description.
    // Do not inherit name.
    // Do not inherit peer group.

    if (_bgpNeighborSource == null) {
      _bgpNeighborSource = other.getBgpNeighborSource();
    }

    if (_ebgpMultihop == null) {
      _ebgpMultihop = other.getEbgpMultihop();
    }

    if (_remoteAs == null) {
      _remoteAs = other.getRemoteAs();
    }

    // IPv4 inheritance
    {
      BgpIpv4UnicastAddressFamily ipv4uaf = vrf.getIpv4Unicast();
      if (ipv4uaf != null && ipv4uaf.getNeighbors().containsKey(other._name)) {
        BgpNeighborIpv4UnicastAddressFamily otherIpv4u = ipv4uaf.getNeighbors().get(other._name);
        BgpNeighborIpv4UnicastAddressFamily ipv4u = ipv4uaf.getNeighbors().get(_name);
        if (ipv4u == null) {
          ipv4u = new BgpNeighborIpv4UnicastAddressFamily();
          ipv4uaf.getNeighbors().put(_name, ipv4u);
        }
        ipv4u.inheritFrom(otherIpv4u);
      }
    }

    // L2VPN inheritance
    {
      BgpL2vpnEvpnAddressFamily l2evpnaf = vrf.getL2VpnEvpn();
      if (l2evpnaf != null && l2evpnaf.getNeighbors().containsKey(other._name)) {
        BgpNeighborL2vpnEvpnAddressFamily otherL2evpn = l2evpnaf.getNeighbors().get(other._name);
        BgpNeighborL2vpnEvpnAddressFamily l2evpn = l2evpnaf.getNeighbors().get(_name);
        if (l2evpn == null) {
          l2evpn = new BgpNeighborL2vpnEvpnAddressFamily();
          l2evpnaf.getNeighbors().put(_name, l2evpn);
        }
        l2evpn.inheritFrom(otherL2evpn);
      }
    }

    if (_localAs == null) {
      _localAs = other.getLocalAs();
    }
  }

  public @Nullable BgpNeighborSource getBgpNeighborSource() {
    return _bgpNeighborSource;
  }

  public void setBgpNeighborSource(@Nullable BgpNeighborSource bgpNeighborSource) {
    _bgpNeighborSource = bgpNeighborSource;
  }
}
