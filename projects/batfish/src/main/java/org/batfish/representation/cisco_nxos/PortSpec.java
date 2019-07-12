package org.batfish.representation.cisco_nxos;

import java.io.Serializable;

/** A specifier for a space of TCP/UDP ports. */
public interface PortSpec extends Serializable {

  <T> T accept(PortSpecVisitor<T> visitor);
}
