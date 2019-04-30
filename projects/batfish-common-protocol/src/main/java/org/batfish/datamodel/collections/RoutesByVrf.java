package org.batfish.datamodel.collections;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeMap;
import org.batfish.datamodel.Route;

public final class RoutesByVrf extends TreeMap<String, SortedSet<Route>> implements Serializable {

  private static final long serialVersionUID = 1L;

  private boolean _unrecognized;

  public boolean getUnrecognized() {
    return _unrecognized;
  }

  public void setUnrecognized(boolean unrecognized) {
    _unrecognized = unrecognized;
  }
}
