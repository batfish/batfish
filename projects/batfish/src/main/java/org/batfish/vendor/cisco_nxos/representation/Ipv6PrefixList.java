package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** An access-list for IPv6 prefixes */
public final class Ipv6PrefixList implements Serializable {

  private @Nullable String _description;
  private final @Nonnull SortedMap<Long, Ipv6PrefixListLine> _lines;
  private final @Nonnull String _name;

  public Ipv6PrefixList(String name) {
    _name = name;
    _lines = new TreeMap<>();
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public @Nonnull SortedMap<Long, Ipv6PrefixListLine> getLines() {
    return _lines;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public void setDescription(String description) {
    _description = description;
  }
}
