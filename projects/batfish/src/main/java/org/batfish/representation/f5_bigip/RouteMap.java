package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Name structure that implements routing policy via a series of ordered entries */
@ParametersAreNonnullByDefault
public final class RouteMap implements Serializable {

  private SortedMap<Long, RouteMapEntry> _entries;

  private final @Nonnull String _name;

  public RouteMap(String name) {
    _entries = new TreeMap<>();
    _name = name;
  }

  public SortedMap<Long, RouteMapEntry> getEntries() {
    return _entries;
  }

  public @Nonnull String getName() {
    return _name;
  }
}
