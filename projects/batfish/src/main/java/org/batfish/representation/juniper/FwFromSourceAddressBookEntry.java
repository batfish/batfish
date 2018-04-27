package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;

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
  public void applyTo(
      HeaderSpace.Builder headerSpaceBuilder,
      JuniperConfiguration jc,
      Warnings w,
      Configuration c) {
    Set<IpWildcard> wildcards = _localAddressBook.getIpWildcards(_addressBookEntryName, w);
    List<IpSpace> ipSpaces =
        wildcards.stream().map(IpWildcard::toIpSpace).collect(ImmutableList.toImmutableList());
    ImmutableList.Builder<IpSpace> ipSpacesBuilder =
        ImmutableList.<IpSpace>builder().addAll(ipSpaces);
    if (headerSpaceBuilder.getSrcIps() != null) {
      ipSpacesBuilder.add(headerSpaceBuilder.getSrcIps());
    }
    headerSpaceBuilder.setSrcIps(AclIpSpace.union(ipSpacesBuilder.build()));
  }
}
