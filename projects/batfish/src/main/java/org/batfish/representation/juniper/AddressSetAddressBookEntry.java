package org.batfish.representation.juniper;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.Warnings;
import org.batfish.datamodel.IpWildcard;

public final class AddressSetAddressBookEntry extends AddressBookEntry {

  private final SortedMap<String, AddressSetEntry> _entries;

  public AddressSetAddressBookEntry(String name) {
    super(name);
    _entries = new TreeMap<>();
  }

  @Override
  public SortedMap<String, AddressSetEntry> getEntries() {
    return _entries;
  }

  @Override
  public SortedSet<IpWildcard> getIpWildcards(Warnings w) {
    SortedSet<IpWildcard> prefixes = new TreeSet<>();
    for (AddressSetEntry entry : _entries.values()) {
      SortedSet<IpWildcard> subPrefixes = entry.getIpWildcards(w);
      prefixes.addAll(subPrefixes);
    }
    return prefixes;
  }
}
