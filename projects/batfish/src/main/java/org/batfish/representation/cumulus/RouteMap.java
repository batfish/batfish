package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;

/** Named structure that implements routing policy via a series of ordered entries */
public class RouteMap implements Serializable {

  private final @Nonnull SortedMap<Integer, RouteMapEntry> _entries;
  private final @Nonnull String _name;

  public RouteMap(String name) {
    _name = name;
    _entries = new TreeMap<>();
  }

  public @Nonnull SortedMap<Integer, RouteMapEntry> getEntries() {
    return _entries;
  }

  public @Nonnull String getName() {
    return _name;
  }
}
