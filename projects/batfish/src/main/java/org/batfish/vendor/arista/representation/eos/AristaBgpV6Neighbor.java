package org.batfish.vendor.arista.representation.eos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip6;

/** IPv6 BGP neighbor. Not yet converted to the vendor-independent model. */
public final class AristaBgpV6Neighbor extends AristaBgpConcreteNeighbor {
  private final @Nonnull Ip6 _ip;

  public AristaBgpV6Neighbor(@Nonnull Ip6 ip) {
    _ip = ip;
  }

  public @Nonnull Ip6 getIp() {
    return _ip;
  }

  @Override
  public @Nonnull String getPeerString() {
    return _ip.toString();
  }

  // TODO: wire up IPv6 address-family settings. For now, return null/a throwaway AF so the
  // shared inherit() logic is a no-op for V6 neighbors.
  @Override
  public @Nullable AristaBgpNeighborAddressFamily getAfSettings(AristaBgpVrfAddressFamily af) {
    return null;
  }

  @Override
  protected @Nonnull AristaBgpNeighborAddressFamily getOrCreateAfSettings(
      AristaBgpVrfAddressFamily af) {
    return new AristaBgpNeighborAddressFamily();
  }
}
