package org.batfish.datamodel.topology;

/** A visitor of {@link Layer2BridgeSettings} that returns a generic value of type {@code T}. */
public interface Layer2BridgeSettingsVisitor<T> {
  default T visit(Layer2BridgeSettings layer2BridgeSettings) {
    return layer2BridgeSettings.accept(this);
  }

  T visitLayer2VlanAwareBridgeSettings(Layer2VlanAwareBridgeSettings layer2VlanAwareBridgeSettings);

  T visitLayer2NonVlanAwareBridgeSettings(
      Layer2NonVlanAwareBridgeSettings layer2NonVlanAwareBridgeSettings);
}
