package org.batfish.datamodel.topology;

/**
 * A visitor of {@link Layer3Settings} that takes a generic argument of type {@code U} and returns a
 * generic value of type {@code T}.
 */
public interface Layer3SettingsArgVisitor<T, U> {

  default T visit(Layer3Settings layer3Settings, U arg) {
    return layer3Settings.accept(this, arg);
  }

  T visitLayer3NonBridgedSettings(Layer3NonBridgedSettings layer3NonBridgedSettings, U arg);

  T visitLayer3NonVlanAwareBridgeSettings(
      Layer3NonVlanAwareBridgeSettings layer3NonVlanAwareBridgeSettings, U arg);

  T visitLayer3VlanAwareBridgeSettings(
      Layer3VlanAwareBridgeSettings layer3VlanAwareBridgeSettings, U arg);

  T visitLayer3TunnelSettings(Layer3TunnelSettings layer3TunnelSettings, U arg);
}
