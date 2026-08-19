package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;

/** Vendor-specific representation of an Aruba AOS-CX route map. */
public final class AosCxRouteMap implements Serializable {

  public AosCxRouteMap(String name) {
    _name = name;
    _entries = new TreeMap<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull SortedMap<Long, AosCxRouteMapEntry> getEntries() {
    return _entries;
  }

  public AosCxRouteMapEntry getOrCreateEntry(long sequence, org.batfish.datamodel.LineAction action) {
    return _entries.computeIfAbsent(sequence, seq -> new AosCxRouteMapEntry(seq, action));
  }

  private final @Nonnull String _name;
  private final @Nonnull SortedMap<Long, AosCxRouteMapEntry> _entries;
}
