package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedMap;
import java.util.SortedSet;
import org.batfish.common.Warnings;
import org.batfish.datamodel.IpWildcard;

public final class AddressAddressBookEntry extends AddressBookEntry {

  private final IpWildcard _ipWildcard;

  public AddressAddressBookEntry(String name, IpWildcard ipWildcard) {
    super(name);
    _ipWildcard = ipWildcard;
  }

  @Override
  public SortedMap<String, AddressSetEntry> getEntries() {
    return ImmutableSortedMap.of();
  }

  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }

  @Override
  public SortedSet<IpWildcard> getIpWildcards(Warnings w) {
    return ImmutableSortedSet.of(_ipWildcard);
  }
}
