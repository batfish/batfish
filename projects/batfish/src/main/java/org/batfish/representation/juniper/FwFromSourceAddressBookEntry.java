package org.batfish.representation.juniper;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

public final class FwFromSourceAddressBookEntry extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private final String _addressBookEntryName;

  private final AddressBook _localAddressBook;

  public FwFromSourceAddressBookEntry(AddressBook localAddressBook, String addressBookEntryName) {
    _localAddressBook = localAddressBook;
    _addressBookEntryName = addressBookEntryName;
  }

  @Override
  public void applyTo(IpAccessListLine line, JuniperConfiguration jc, Warnings w, Configuration c) {
    Set<Prefix> prefixes = _localAddressBook.getPrefixes(_addressBookEntryName, w);
    List<IpWildcard> wildcards =
        prefixes.stream().map(IpWildcard::new).collect(Collectors.toList());
    line.setSrcIps(Iterables.concat(line.getSrcIps(), wildcards));
  }
}
