package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;

/** Vendor-specific representation of an Aruba AOS-CX IPv4 prefix list. */
public final class AosCxPrefixList implements Serializable {

  public AosCxPrefixList(String name) {
    _name = name;
    _entries = new TreeMap<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull SortedMap<Long, AosCxPrefixListEntry> getEntries() {
    return _entries;
  }

  public long getNextSequence() {
    return _entries.isEmpty() ? 10L : _entries.lastKey() + 10L;
  }

  public void addEntry(AosCxPrefixListEntry entry) {
    _entries.put(entry.getSequence(), entry);
  }

  private final @Nonnull String _name;
  private final @Nonnull SortedMap<Long, AosCxPrefixListEntry> _entries;
}
