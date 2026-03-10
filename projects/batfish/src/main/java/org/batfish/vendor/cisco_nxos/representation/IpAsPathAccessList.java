package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;

/** An access-list that matches a route's as-path attribute against a list of as-path regexes. */
public final class IpAsPathAccessList implements Serializable {

  private final @Nonnull SortedMap<Long, IpAsPathAccessListLine> _lines;
  private final @Nonnull String _name;

  public IpAsPathAccessList(String name) {
    _name = name;
    _lines = new TreeMap<>();
  }

  public @Nonnull SortedMap<Long, IpAsPathAccessListLine> getLines() {
    return _lines;
  }

  public @Nonnull String getName() {
    return _name;
  }
}
