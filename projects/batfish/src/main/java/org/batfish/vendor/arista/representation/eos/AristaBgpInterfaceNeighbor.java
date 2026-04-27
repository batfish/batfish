package org.batfish.vendor.arista.representation.eos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Vrf;

/** Config for BGP unnumbered neighbors (created using "neighbor interface ... peer-group") */
public class AristaBgpInterfaceNeighbor extends AristaBgpConcreteNeighbor {
  private final @Nonnull String _interfaceName;
  private @Nullable String _peerFilter;

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
  public @Nonnull String getPeerString() {
    return _interfaceName;
  }

  @Override
  public @Nonnull String getTextDesc(Vrf v) {
    return String.format("BGP neighbor interface %s in vrf %s", _interfaceName, v.getName());
  }

  @Override
  public @Nullable AristaBgpNeighborAddressFamily getAfSettings(AristaBgpVrfAddressFamily af) {
    return af.getInterfaceNeighbor(_interfaceName);
  }

  @Override
  protected @Nonnull AristaBgpNeighborAddressFamily getOrCreateAfSettings(
      AristaBgpVrfAddressFamily af) {
    return af.getOrCreateInterfaceNeighbor(_interfaceName);
  }
}
