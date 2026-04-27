package org.batfish.vendor.arista.representation.eos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

/** Config for dynamic BGP neighbors (created using "bgp listen range") */
public class AristaBgpV4DynamicNeighbor extends AristaBgpConcreteNeighbor {
  private final @Nonnull Prefix _range;
  private @Nullable String _peerFilter;

  public AristaBgpV4DynamicNeighbor(@Nonnull Prefix range) {
    _range = range;
  }

  public @Nonnull Prefix getRange() {
    return _range;
  }

  public @Nullable String getPeerFilter() {
    return _peerFilter;
  }

  public AristaBgpV4DynamicNeighbor setPeerFilter(@Nullable String peerFilter) {
    _peerFilter = peerFilter;
    return this;
  }

  @Override
  public @Nonnull String getPeerString() {
    return _range.toString();
  }

  @Override
  public @Nullable AristaBgpNeighborAddressFamily getAfSettings(AristaBgpVrfAddressFamily af) {
    return af.getNeighbor(_range);
  }

  @Override
  protected @Nonnull AristaBgpNeighborAddressFamily getOrCreateAfSettings(
      AristaBgpVrfAddressFamily af) {
    return af.getOrCreateNeighbor(_range);
  }
}
