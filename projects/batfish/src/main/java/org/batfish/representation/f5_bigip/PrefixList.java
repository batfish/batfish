package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Named prefix-list serving as an access-control list for either IPv4 or IPv6 prefixes */
@ParametersAreNonnullByDefault
public final class PrefixList implements Serializable {

  private final @Nonnull SortedMap<Long, PrefixListEntry> _entries;

  private final @Nonnull String _name;

  public PrefixList(String name) {
    _name = name;
    _entries = new TreeMap<>();
  }

  public @Nonnull SortedMap<Long, PrefixListEntry> getEntries() {
    return _entries;
  }

  public @Nonnull String getName() {
    return _name;
  }
}
