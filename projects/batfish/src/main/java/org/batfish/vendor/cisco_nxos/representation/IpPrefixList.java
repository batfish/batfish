package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** An access-list for IPv4 prefixes */
public final class IpPrefixList implements Serializable {

  private @Nullable String _description;
  private final @Nonnull SortedMap<Long, IpPrefixListLine> _lines;
  private final @Nonnull String _name;

  public IpPrefixList(String name) {
    _name = name;
    _lines = new TreeMap<>();
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public @Nonnull SortedMap<Long, IpPrefixListLine> getLines() {
    return _lines;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public void setDescription(String description) {
    _description = description;
  }
}
