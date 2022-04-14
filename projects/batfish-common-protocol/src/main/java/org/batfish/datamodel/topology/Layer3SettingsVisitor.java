package org.batfish.datamodel.topology;

/** A visitor of {@link Layer3Settings} that returns a generic value of type {@code T}. */
public interface Layer3SettingsVisitor<T> {

  default T visit(Layer3Settings layer3Settings) {
    return layer3Settings.accept(this);
  }

  T visitLayer3NonVlanAwareBridgeSettings(
      Layer3NonVlanAwareBridgeSettings layer3NonVlanAwareBridgeSettings);

  T visitLayer3NonBridgedSettings(Layer3NonBridgedSettings layer3NonBridgedSettings);

  T visitLayer3TunnelSettings(Layer3TunnelSettings layer3TunnelSettings);

  T visitLayer3VlanAwareBridgeSettings(Layer3VlanAwareBridgeSettings layer3VlanAwareBridgeSettings);
}
