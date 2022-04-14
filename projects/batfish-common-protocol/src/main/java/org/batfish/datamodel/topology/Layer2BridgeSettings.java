package org.batfish.datamodel.topology;

import java.io.Serializable;

/** Vlan-aware or non-vlan-aware bridging configuration. */
public interface Layer2BridgeSettings extends Serializable {

  <T> T accept(Layer2BridgeSettingsVisitor<T> visitor);
}
