package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Set;
import java.util.SortedSet;
import org.batfish.common.Warnings;
import org.batfish.datamodel.IpWildcard;

public final class AddressAddressBookEntry extends AddressBookEntry {

  /** */
  private static final long serialVersionUID = 1L;

  private final IpWildcard _ipWildcard;

  public AddressAddressBookEntry(String name, IpWildcard ipWildcard) {
    super(name);
    _ipWildcard = ipWildcard;
  }

  @Override
  public Set<AddressSetEntry> getEntries() {
    return ImmutableSet.of();
  }

  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }

  @Override
  public SortedSet<IpWildcard> getIpWildcards(Warnings w) {
    return ImmutableSortedSet.of(_ipWildcard);
  }
}
