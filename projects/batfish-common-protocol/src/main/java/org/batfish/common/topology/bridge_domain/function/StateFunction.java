package org.batfish.common.topology.bridge_domain.function;

import java.io.Serializable;

/** A function or filter on state. */
public interface StateFunction extends Serializable {
  <T, U> T accept(StateFunctionVisitor<T, U> visitor, U arg);
}
