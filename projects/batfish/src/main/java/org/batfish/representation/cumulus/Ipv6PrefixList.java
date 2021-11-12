package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A prefix-list for IPv6 */
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

  public void addLine(Ipv6PrefixListLine line) {
    _lines.put(line.getLine(), line);
  }

  public @Nonnull String getName() {
    return _name;
  }

  public void setDescription(String description) {
    _description = description;
  }
}
