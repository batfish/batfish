package org.batfish.common.topology.bridge_domain.function;

/** A function or filter on state. */
public interface StateFunction {
  <T, U> T accept(StateFunctionVisitor<T, U> visitor, U arg);
}
