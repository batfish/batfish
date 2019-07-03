package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.SortedSet;
import org.batfish.common.Warnings;
import org.batfish.datamodel.IpWildcard;

public final class AddressSetEntry implements Serializable {

  private final AddressBook _book;

  private final String _name;

  public AddressSetEntry(String name, AddressBook book) {
    _name = name;
    _book = book;
  }

  public SortedSet<IpWildcard> getIpWildcards(Warnings w) {
    return _book.getIpWildcards(_name, w);
  }

  public String getName() {
    return _name;
  }
}
