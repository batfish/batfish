package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Comparator;
import java.util.SortedSet;
import org.batfish.common.Warnings;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.IpWildcard;

public final class AddressSetEntry extends ComparableStructure<String> {

  static final class NameComparator implements Comparator<AddressSetEntry>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(AddressSetEntry o1, AddressSetEntry o2) {
      return o1._key.compareTo(o2._key);
    }
  }

  public static final Comparator<AddressSetEntry> NAME_COMPARATOR = new NameComparator();

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
