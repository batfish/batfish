package org.batfish.vendor.arista.representation.eos;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Vrf;

/**
 * Base class for concrete (non-peer-group) Arista BGP neighbors, keyed by IP, prefix, or interface.
 *
 * <p>Subclasses supply the per-address-family settings lookup keyed by their specific identifier;
 * this class provides the common peer-group/address-family inheritance and activity checks.
 */
public abstract class AristaBgpConcreteNeighbor extends AristaBgpNeighbor
    implements AristaBgpHasPeerGroup {
  private @Nullable String _peerGroup;

  @Override
  public @Nullable String getPeerGroup() {
    return _peerGroup;
  }

  @Override
  public void setPeerGroup(@Nullable String peerGroup) {
    _peerGroup = peerGroup;
  }

  /** String representation of this peer, used to name generated policies and in warnings. */
  public abstract @Nonnull String getPeerString();

  /** Human-readable description for warnings, e.g., {@code "BGP neighbor 1.2.3.4 in vrf V"}. */
  public @Nonnull String getTextDesc(Vrf v) {
    return String.format("BGP neighbor %s in vrf %s", getPeerString(), v.getName());
  }

  /** Returns this peer's per-AF settings, or {@code null} if none are configured. */
  public abstract @Nullable AristaBgpNeighborAddressFamily getAfSettings(
      AristaBgpVrfAddressFamily af);

  /** Returns or creates this peer's per-AF settings. */
  protected abstract @Nonnull AristaBgpNeighborAddressFamily getOrCreateAfSettings(
      AristaBgpVrfAddressFamily af);

  @Override
  public void inherit(AristaBgpProcess bgpGlobal, AristaBgpVrf bgpVrf, Warnings w) {
    Optional<String> pgName = Optional.ofNullable(_peerGroup);
    Optional<AristaBgpPeerGroupNeighbor> pgn = pgName.map(bgpGlobal::getPeerGroup);
    // Inherit the overall (non-address-family) specific settings.
    pgn.ifPresent(this::inheritFrom);

    // Inherit for each address family separately.
    AristaBgpVrfIpv4UnicastAddressFamily v4 = bgpVrf.getV4UnicastAf();
    if (v4 != null) {
      AristaBgpNeighborAddressFamily neighbor = getOrCreateAfSettings(v4);
      neighbor.inheritFrom(getGenericAddressFamily());
      pgName.map(v4::getPeerGroup).ifPresent(neighbor::inheritFrom);
      pgn.map(AristaBgpPeerGroupNeighbor::getGenericAddressFamily).ifPresent(neighbor::inheritFrom);
      // If not yet set, activate based on the vrf setting for v4 unicast.
      if (neighbor.getActivate() == null) {
        neighbor.setActivate(bgpVrf.getDefaultIpv4Unicast());
      }
    }

    AristaBgpVrfEvpnAddressFamily evpn = bgpVrf.getEvpnAf();
    if (evpn != null) {
      AristaBgpNeighborAddressFamily neighbor = getOrCreateAfSettings(evpn);
      neighbor.inheritFrom(getGenericAddressFamily());
      pgName.map(evpn::getPeerGroup).ifPresent(neighbor::inheritFrom);
      pgn.map(AristaBgpPeerGroupNeighbor::getGenericAddressFamily).ifPresent(neighbor::inheritFrom);
    }
  }

  /**
   * Checks that this neighbor is not shutdown and at least one supported address family is
   * activated. Emits a red-flag warning if no supported AF is active.
   */
  public boolean isActive(Vrf vrf, AristaBgpVrf bgpVrf, Warnings w) {
    if (firstNonNull(getShutdown(), Boolean.FALSE)) {
      return false;
    }
    boolean v4 =
        Optional.ofNullable(bgpVrf.getV4UnicastAf())
            .map(this::getAfSettings)
            .map(AristaBgpNeighborAddressFamily::getActivate)
            .orElse(Boolean.FALSE);
    boolean evpn =
        Optional.ofNullable(bgpVrf.getEvpnAf())
            .map(this::getAfSettings)
            .map(AristaBgpNeighborAddressFamily::getActivate)
            .orElse(Boolean.FALSE);
    if (!v4 && !evpn) {
      w.redFlag("No supported address-family configured for " + getTextDesc(vrf));
      return false;
    }
    return true;
  }
}
