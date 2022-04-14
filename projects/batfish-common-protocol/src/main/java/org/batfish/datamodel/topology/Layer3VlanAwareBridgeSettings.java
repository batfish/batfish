package org.batfish.datamodel.topology;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.L3ToVlanAwareBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.VlanAwareBridgeDomainToL3;

/** Configuration for briding a layer-3 (IRB/Vlan) interface to a vlan-aware bridge. */
public final class Layer3VlanAwareBridgeSettings implements Layer3Settings {

  public static @Nonnull Layer3VlanAwareBridgeSettings of(
      String vlanAwareBridge,
      VlanAwareBridgeDomainToL3 fromBridgeDomain,
      L3ToVlanAwareBridgeDomain toBridgeDomain) {
    return new Layer3VlanAwareBridgeSettings(vlanAwareBridge, fromBridgeDomain, toBridgeDomain);
  }

  @Override
  public <T> T accept(Layer3SettingsVisitor<T> visitor) {
    return visitor.visitLayer3VlanAwareBridgeSettings(this);
  }

  @Override
  public <T, U> T accept(Layer3SettingsArgVisitor<T, U> visitor, U arg) {
    return visitor.visitLayer3VlanAwareBridgeSettings(this, arg);
  }

  public @Nonnull String getVlanAwareBridge() {
    return _vlanAwareBridge;
  }

  public @Nonnull VlanAwareBridgeDomainToL3 getFromBridgeDomain() {
    return _fromBridgeDomain;
  }

  public @Nonnull L3ToVlanAwareBridgeDomain getToBridgeDomain() {
    return _toBridgeDomain;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Layer3VlanAwareBridgeSettings)) {
      return false;
    }
    Layer3VlanAwareBridgeSettings that = (Layer3VlanAwareBridgeSettings) o;
    return _vlanAwareBridge.equals(that._vlanAwareBridge)
        && _fromBridgeDomain.equals(that._fromBridgeDomain)
        && _toBridgeDomain.equals(that._toBridgeDomain);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_vlanAwareBridge, _fromBridgeDomain, _toBridgeDomain);
  }

  private Layer3VlanAwareBridgeSettings(
      String vlanAwareBridge,
      VlanAwareBridgeDomainToL3 fromBridgeDomain,
      L3ToVlanAwareBridgeDomain toBridgeDomain) {
    _vlanAwareBridge = vlanAwareBridge;
    _fromBridgeDomain = fromBridgeDomain;
    _toBridgeDomain = toBridgeDomain;
  }

  private final @Nonnull String _vlanAwareBridge;
  private final @Nonnull VlanAwareBridgeDomainToL3 _fromBridgeDomain;
  private final @Nonnull L3ToVlanAwareBridgeDomain _toBridgeDomain;
}
