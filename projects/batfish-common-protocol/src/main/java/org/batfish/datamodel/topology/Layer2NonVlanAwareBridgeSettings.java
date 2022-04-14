package org.batfish.datamodel.topology;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.L2ToNonVlanAwareBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.NonVlanAwareBridgeDomainToL2;
import org.batfish.common.topology.bridge_domain.edge.NonVlanAwareBridgeDomainToL2.Function;

/** Configuration for bridging a layer-2 interface to a non-vlan-aware bridge domain. */
public final class Layer2NonVlanAwareBridgeSettings implements Layer2BridgeSettings {

  public static @Nonnull Layer2NonVlanAwareBridgeSettings of(
      String nonVlanAwareBridgeDomain,
      NonVlanAwareBridgeDomainToL2.Function fromBridgeDomain,
      L2ToNonVlanAwareBridgeDomain.Function toBridgeDomain) {
    return new Layer2NonVlanAwareBridgeSettings(
        nonVlanAwareBridgeDomain, fromBridgeDomain, toBridgeDomain);
  }

  @Override
  public <T> T accept(Layer2BridgeSettingsVisitor<T> visitor) {
    return visitor.visitLayer2NonVlanAwareBridgeSettings(this);
  }

  public @Nonnull String getNonVlanAwareBridgeDomain() {
    return _nonVlanAwareBridgeDomain;
  }

  public @Nonnull Function getFromBridgeDomain() {
    return _fromBridgeDomain;
  }

  public @Nonnull L2ToNonVlanAwareBridgeDomain.Function getToBridgeDomain() {
    return _toBridgeDomain;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Layer2NonVlanAwareBridgeSettings)) {
      return false;
    }
    Layer2NonVlanAwareBridgeSettings that = (Layer2NonVlanAwareBridgeSettings) o;
    return _nonVlanAwareBridgeDomain.equals(that._nonVlanAwareBridgeDomain)
        && _fromBridgeDomain.equals(that._fromBridgeDomain)
        && _toBridgeDomain.equals(that._toBridgeDomain);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nonVlanAwareBridgeDomain, _fromBridgeDomain, _toBridgeDomain);
  }

  private Layer2NonVlanAwareBridgeSettings(
      String nonVlanAwareBridgeDomain,
      NonVlanAwareBridgeDomainToL2.Function fromBridgeDomain,
      L2ToNonVlanAwareBridgeDomain.Function toBridgeDomain) {
    _nonVlanAwareBridgeDomain = nonVlanAwareBridgeDomain;
    _fromBridgeDomain = fromBridgeDomain;
    _toBridgeDomain = toBridgeDomain;
  }

  private final @Nonnull String _nonVlanAwareBridgeDomain;
  private final @Nonnull NonVlanAwareBridgeDomainToL2.Function _fromBridgeDomain;
  private final @Nonnull L2ToNonVlanAwareBridgeDomain.Function _toBridgeDomain;
}
