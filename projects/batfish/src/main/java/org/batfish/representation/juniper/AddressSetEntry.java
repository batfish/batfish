package org.batfish.representation.juniper;

import java.util.SortedSet;
import org.batfish.common.Warnings;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.IpWildcard;

public final class AddressSetEntry extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  protected final AddressBook _book;

  public AddressSetEntry(String name, AddressBook book) {
    super(name);
    _book = book;
  }

  public SortedSet<IpWildcard> getIpWildcards(Warnings w) {
    return _book.getIpWildcards(_key, w);
  }
}
