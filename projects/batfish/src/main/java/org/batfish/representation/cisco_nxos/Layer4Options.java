package org.batfish.representation.cisco_nxos;

public interface Layer4Options {

  <T> T accept(Layer4OptionsVisitor<T> visitor);
}
