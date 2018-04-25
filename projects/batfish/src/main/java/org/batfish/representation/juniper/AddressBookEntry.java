package org.batfish.representation.juniper;

import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.IpWildcard;

public abstract class AddressBookEntry extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  public AddressBookEntry(String name) {
    super(name);
  }

  public abstract Set<AddressSetEntry> getEntries();

  public abstract Set<IpWildcard> getIpWildcards(Warnings w);
}
