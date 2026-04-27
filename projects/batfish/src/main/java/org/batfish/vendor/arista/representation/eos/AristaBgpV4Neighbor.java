package org.batfish.vendor.arista.representation.eos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** IPv4 BGP neighbor */
public final class AristaBgpV4Neighbor extends AristaBgpConcreteNeighbor {
  private final @Nonnull Ip _ip;

  public AristaBgpV4Neighbor(@Nonnull Ip ip) {
    _ip = ip;
  }

  public @Nonnull Ip getIp() {
    return _ip;
  }

  @Override
  public @Nonnull String getPeerString() {
    return _ip.toString();
  }

  @Override
  public @Nullable AristaBgpNeighborAddressFamily getAfSettings(AristaBgpVrfAddressFamily af) {
    return af.getNeighbor(_ip);
  }

  @Override
  protected @Nonnull AristaBgpNeighborAddressFamily getOrCreateAfSettings(
      AristaBgpVrfAddressFamily af) {
    return af.getOrCreateNeighbor(_ip);
  }
}
