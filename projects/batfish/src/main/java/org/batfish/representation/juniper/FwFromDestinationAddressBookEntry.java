package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;

public final class FwFromDestinationAddressBookEntry extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private final String _addressBookEntryName;

  private final AddressBook _localAddressBook;

  public FwFromDestinationAddressBookEntry(
      AddressBook localAddressBook, String addressBookEntryName) {
    _localAddressBook = localAddressBook;
    _addressBookEntryName = addressBookEntryName;
  }

  @Override
  public void applyTo(
      HeaderSpace.Builder headerSpaceBuilder,
      JuniperConfiguration jc,
      Warnings w,
      Configuration c) {
    // Address book name may be the local address book name or the global name
    String addressBookName = _localAddressBook.getAddressBookName(_addressBookEntryName);
    String ipSpaceName = addressBookName + "~" + _addressBookEntryName;
    IpSpaceReference ipSpaceReference = new IpSpaceReference(ipSpaceName);
    if (headerSpaceBuilder.getDstIps() != null) {
      ImmutableList.Builder<IpSpace> ipSpacesBuilder =
          ImmutableList.<IpSpace>builder().add(ipSpaceReference);
      ipSpacesBuilder.add(headerSpaceBuilder.getDstIps());
      headerSpaceBuilder.setDstIps(AclIpSpace.union(ipSpacesBuilder.build()));
    } else {
      headerSpaceBuilder.setDstIps(AclIpSpace.union(ipSpaceReference));
    }
  }
}
