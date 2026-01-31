package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import java.util.NavigableMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;

/**
 * NX-OS structure that implements routing policy via a series of ordered entries with optional
 * branching.
 */
public final class RouteMap implements Serializable {

  private final @Nonnull NavigableMap<Integer, RouteMapEntry> _entries;
  private final @Nonnull String _name;
  private boolean _pbrStatistics;

  public RouteMap(String name) {
    _name = name;
    _entries = new TreeMap<>();
  }

  public @Nonnull NavigableMap<Integer, RouteMapEntry> getEntries() {
    return _entries;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public boolean getPbrStatistics() {
    return _pbrStatistics;
  }

  public void setPbrStatistics(boolean pbrStatistics) {
    _pbrStatistics = pbrStatistics;
  }
}
