package org.batfish.representation.juniper;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.Warnings;
import org.batfish.datamodel.IpWildcard;

public final class AddressSetAddressBookEntry extends AddressBookEntry {

  /** */
  private static final long serialVersionUID = 1L;

  private final SortedSet<AddressSetEntry> _entries;

  public AddressSetAddressBookEntry(String name) {
    super(name);
    _entries = new TreeSet<>(AddressSetEntry.NAME_COMPARATOR);
  }

  @Override
  public Set<AddressSetEntry> getEntries() {
    return _entries;
  }

  @Override
  public SortedSet<IpWildcard> getIpWildcards(Warnings w) {
    SortedSet<IpWildcard> prefixes = new TreeSet<>();
    for (AddressSetEntry entry : _entries) {
      SortedSet<IpWildcard> subPrefixes = entry.getIpWildcards(w);
      prefixes.addAll(subPrefixes);
    }
    return prefixes;
  }
}
