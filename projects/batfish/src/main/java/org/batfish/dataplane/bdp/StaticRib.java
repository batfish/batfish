package org.batfish.dataplane.bdp;

import org.batfish.datamodel.StaticRoute;

public class StaticRib extends AbstractRib<StaticRoute> {

  /** */
  private static final long serialVersionUID = 1L;

  public StaticRib(VirtualRouter owner) {
    super(owner);
  }

  @Override
  public int comparePreference(StaticRoute lhs, StaticRoute rhs) {
    /** TODO: see if this needs to be changed */
    return 0;
  }
}
