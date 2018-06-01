package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;

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
    // Address book name may be the local address book name or the global name
    String addressBookName = _localAddressBook.getAddressBookName(_addressBookEntryName);
    String ipSpaceName = addressBookName + "~" + _addressBookEntryName;
    IpSpaceReference ipSpaceReference = new IpSpaceReference(ipSpaceName);
    if (headerSpaceBuilder.getSrcIps() != null) {
      headerSpaceBuilder.setSrcIps(
          AclIpSpace.union(
              ImmutableList.<IpSpace>builder()
                  .add(ipSpaceReference)
                  .add(headerSpaceBuilder.getSrcIps())
                  .build()));
    } else {
      headerSpaceBuilder.setSrcIps(AclIpSpace.union(ipSpaceReference));
    }
  }
}
