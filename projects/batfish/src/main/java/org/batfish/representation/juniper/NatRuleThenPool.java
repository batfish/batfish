package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nonnull;

public class NatRuleThenPool implements NatRuleThen, Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private @Nonnull final String _poolName;

  public NatRuleThenPool(String poolName) {
    _poolName = poolName;
  }

  public String getPoolName() {
    return _poolName;
  }
}
