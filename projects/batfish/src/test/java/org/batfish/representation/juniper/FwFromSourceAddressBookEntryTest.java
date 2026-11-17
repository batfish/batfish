package org.batfish.representation.juniper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.batfish.common.Warnings;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.junit.jupiter.api.Test;

/** Test for {@link FwFromSourceAddressBookEntry } */
class FwFromSourceAddressBookEntryTest {
  @Test
  void testToHeaderspace() {
    String addressBookEntryName = "addressBookEntry";

    AddressBook globalAddressBook = new AddressBook("addressBook", null);
    globalAddressBook
        .getEntries()
        .put(
            addressBookEntryName,
            new AddressAddressBookEntry(addressBookEntryName, IpWildcard.parse("1.1.1.0/24")));

    Warnings w = new Warnings();

    FwFromSourceAddressBookEntry from =
        new FwFromSourceAddressBookEntry(null, globalAddressBook, addressBookEntryName);

    assertEquals(from.toIpSpace(w), new IpSpaceReference("addressBook~addressBookEntry"));
  }

  @Test
  void testToHeaderspace_noEntry() {
    String addressBookEntryName = "addressBookEntry";
    AddressBook globalAddressBook = new AddressBook("addressBook", null);
    Warnings w = new Warnings();

    FwFromSourceAddressBookEntry from =
        new FwFromSourceAddressBookEntry(null, globalAddressBook, addressBookEntryName);

    assertEquals(from.toIpSpace(w), EmptyIpSpace.INSTANCE);
  }
}
