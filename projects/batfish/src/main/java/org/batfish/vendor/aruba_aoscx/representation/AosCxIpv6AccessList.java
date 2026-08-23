package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;

/** Vendor-specific Aruba AOS-CX IPv6 access list. */
public final class AosCxIpv6AccessList implements Serializable {

  public AosCxIpv6AccessList(String name) {
    _name = name;
    _entries = new TreeMap<>();
  }

  public void addEntry(
      AosCxIpv6AccessListEntry entry) {
    _entries.put(
        entry.getSequence(),
        entry);
  }

  public @Nonnull SortedMap<
          Long, AosCxIpv6AccessListEntry>
      getEntries() {
    return _entries;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public long getNextSequence() {
    return _entries.isEmpty()
        ? 10L
        : _entries.lastKey() + 10L;
  }

  private final @Nonnull SortedMap<
          Long, AosCxIpv6AccessListEntry>
      _entries;
  private final @Nonnull String _name;
}
