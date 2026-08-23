package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;

/** Vendor-specific representation of an Aruba AOS-CX IPv6 prefix list. */
public final class AosCxIpv6PrefixList implements Serializable {

  public AosCxIpv6PrefixList(String name) {
    _name = name;
    _entries = new TreeMap<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull SortedMap<Long, AosCxIpv6PrefixListEntry>
      getEntries() {
    return _entries;
  }

  public long getNextSequence() {
    return _entries.isEmpty()
        ? 10L
        : _entries.lastKey() + 10L;
  }

  public void addEntry(
      AosCxIpv6PrefixListEntry entry) {
    _entries.put(
        entry.getSequence(),
        entry);
  }

  private final @Nonnull String _name;
  private final @Nonnull
      SortedMap<Long, AosCxIpv6PrefixListEntry> _entries;
}
