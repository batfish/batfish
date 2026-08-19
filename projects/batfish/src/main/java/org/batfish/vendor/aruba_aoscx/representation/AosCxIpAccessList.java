package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;

/** Vendor-specific representation of an Aruba AOS-CX IPv4 access list. */
public final class AosCxIpAccessList implements Serializable {

  public AosCxIpAccessList(String name) {
    _name = name;
    _entries = new TreeMap<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull SortedMap<Long, AosCxIpAccessListEntry> getEntries() {
    return _entries;
  }

  public long getNextSequence() {
    return _entries.isEmpty() ? 10L : _entries.lastKey() + 10L;
  }

  public void addEntry(AosCxIpAccessListEntry entry) {
    _entries.put(entry.getSequence(), entry);
  }

  private final @Nonnull String _name;
  private final @Nonnull SortedMap<Long, AosCxIpAccessListEntry> _entries;
}
