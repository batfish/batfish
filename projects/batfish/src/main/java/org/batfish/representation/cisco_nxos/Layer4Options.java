package org.batfish.representation.cisco_nxos;

import java.io.Serializable;

public interface Layer4Options extends Serializable {

  <T> T accept(Layer4OptionsVisitor<T> visitor);
}
