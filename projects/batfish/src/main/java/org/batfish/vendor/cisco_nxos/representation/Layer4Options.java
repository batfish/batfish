package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;

public interface Layer4Options extends Serializable {

  <T> T accept(Layer4OptionsVisitor<T> visitor);
}
