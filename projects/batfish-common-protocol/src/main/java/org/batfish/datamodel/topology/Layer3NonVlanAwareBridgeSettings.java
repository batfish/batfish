package org.batfish.datamodel.topology;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.L3ToNonVlanAwareBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.NonVlanAwareBridgeDomainToL3;

/** Configuration for briding a layer-3 (IRB) interface to a non-vlan-aware bridge. */
public final class Layer3NonVlanAwareBridgeSettings implements Layer3Settings {

  public static @Nonnull Layer3NonVlanAwareBridgeSettings of(
      String nonVlanAwareBridge,
      NonVlanAwareBridgeDomainToL3 fromBridgeDomain,
      L3ToNonVlanAwareBridgeDomain toBridgeDomain) {
    return new Layer3NonVlanAwareBridgeSettings(
        nonVlanAwareBridge, fromBridgeDomain, toBridgeDomain);
  }

  @Override
  public <T> T accept(Layer3SettingsVisitor<T> visitor) {
    return visitor.visitLayer3NonVlanAwareBridgeSettings(this);
  }

  @Override
  public <T, U> T accept(Layer3SettingsArgVisitor<T, U> visitor, U arg) {
    return visitor.visitLayer3NonVlanAwareBridgeSettings(this, arg);
  }

  public @Nonnull String getNonVlanAwareBridge() {
    return _nonVlanAwareBridge;
  }

  public @Nonnull NonVlanAwareBridgeDomainToL3 getFromBridgeDomain() {
    return _fromBridgeDomain;
  }

  public @Nonnull L3ToNonVlanAwareBridgeDomain getToBridgeDomain() {
    return _toBridgeDomain;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Layer3NonVlanAwareBridgeSettings)) {
      return false;
    }
    Layer3NonVlanAwareBridgeSettings that = (Layer3NonVlanAwareBridgeSettings) o;
    return _nonVlanAwareBridge.equals(that._nonVlanAwareBridge)
        && _fromBridgeDomain.equals(that._fromBridgeDomain)
        && _toBridgeDomain.equals(that._toBridgeDomain);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nonVlanAwareBridge, _fromBridgeDomain, _toBridgeDomain);
  }

  private Layer3NonVlanAwareBridgeSettings(
      String nonVlanAwareBridge,
      NonVlanAwareBridgeDomainToL3 fromBridgeDomain,
      L3ToNonVlanAwareBridgeDomain toBridgeDomain) {
    _nonVlanAwareBridge = nonVlanAwareBridge;
    _fromBridgeDomain = fromBridgeDomain;
    _toBridgeDomain = toBridgeDomain;
  }

  private final @Nonnull String _nonVlanAwareBridge;
  private final @Nonnull NonVlanAwareBridgeDomainToL3 _fromBridgeDomain;
  private final @Nonnull L3ToNonVlanAwareBridgeDomain _toBridgeDomain;
}
