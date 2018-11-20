package org.batfish.representation.juniper;

import java.io.Serializable;

/** A {@link NatRule} that nats using the specified pool */
public final class NatRuleThenPool implements NatRuleThen, Serializable {

  private static final long serialVersionUID = 1L;

  private final String _poolName;

  public NatRuleThenPool(String poolName) {
    _poolName = poolName;
  }

  public String getPoolName() {
    return _poolName;
  }
}
