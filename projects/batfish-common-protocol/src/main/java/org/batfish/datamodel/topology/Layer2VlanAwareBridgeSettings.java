package org.batfish.datamodel.topology;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.L2ToVlanAwareBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.VlanAwareBridgeDomainToL2;

/** Configuration for bridging a layer-2 interface to a vlan-aware bridge domain. */
public final class Layer2VlanAwareBridgeSettings implements Layer2BridgeSettings {

  public static @Nonnull Layer2VlanAwareBridgeSettings of(
      String vlanAwareBridgeDomain,
      VlanAwareBridgeDomainToL2 fromBridgeDomain,
      L2ToVlanAwareBridgeDomain toBridgeDomain) {
    return new Layer2VlanAwareBridgeSettings(
        vlanAwareBridgeDomain, fromBridgeDomain, toBridgeDomain);
  }

  @Override
  public <T> T accept(Layer2BridgeSettingsVisitor<T> visitor) {
    return visitor.visitLayer2VlanAwareBridgeSettings(this);
  }

  public @Nonnull String getVlanAwareBridgeDomain() {
    return _vlanAwareBridgeDomain;
  }

  public @Nonnull VlanAwareBridgeDomainToL2 getFromBridgeDomain() {
    return _fromBridgeDomain;
  }

  public @Nonnull L2ToVlanAwareBridgeDomain getToBridgeDomain() {
    return _toBridgeDomain;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Layer2VlanAwareBridgeSettings)) {
      return false;
    }
    Layer2VlanAwareBridgeSettings that = (Layer2VlanAwareBridgeSettings) o;
    return _vlanAwareBridgeDomain.equals(that._vlanAwareBridgeDomain)
        && _fromBridgeDomain.equals(that._fromBridgeDomain)
        && _toBridgeDomain.equals(that._toBridgeDomain);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_vlanAwareBridgeDomain, _fromBridgeDomain, _toBridgeDomain);
  }

  private Layer2VlanAwareBridgeSettings(
      String vlanAwareBridgeDomain,
      VlanAwareBridgeDomainToL2 fromBridgeDomain,
      L2ToVlanAwareBridgeDomain toBridgeDomain) {
    _vlanAwareBridgeDomain = vlanAwareBridgeDomain;
    _fromBridgeDomain = fromBridgeDomain;
    _toBridgeDomain = toBridgeDomain;
  }

  private final @Nonnull String _vlanAwareBridgeDomain;
  private final @Nonnull VlanAwareBridgeDomainToL2 _fromBridgeDomain;
  private final @Nonnull L2ToVlanAwareBridgeDomain _toBridgeDomain;
}
