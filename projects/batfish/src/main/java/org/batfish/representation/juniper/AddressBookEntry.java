package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Set;
import java.util.SortedSet;
import org.batfish.common.Warnings;
import org.batfish.datamodel.IpWildcard;

public abstract class AddressBookEntry implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private final String _name;

  public AddressBookEntry(String name) {
    _name = name;
  }

  public abstract Set<AddressSetEntry> getEntries();

  public abstract SortedSet<IpWildcard> getIpWildcards(Warnings w);

  public final String getName() {
    return _name;
  }
}
