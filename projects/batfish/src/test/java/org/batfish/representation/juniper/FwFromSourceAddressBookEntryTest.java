package org.batfish.representation.juniper;

import static org.junit.Assert.assertEquals;

import org.batfish.common.Warnings;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.junit.Test;

/** Test for {@link FwFromSourceAddressBookEntry } */
public class FwFromSourceAddressBookEntryTest {
  @Test
  public void testToHeaderspace() {
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

    assertEquals(
        from.toHeaderspace(w),
        HeaderSpace.builder()
            .setSrcIps(new IpSpaceReference("addressBook~addressBookEntry"))
            .build());
  }

  @Test
  public void testToHeaderspace_noEntry() {
    String addressBookEntryName = "addressBookEntry";
    AddressBook globalAddressBook = new AddressBook("addressBook", null);
    Warnings w = new Warnings();

    FwFromSourceAddressBookEntry from =
        new FwFromSourceAddressBookEntry(null, globalAddressBook, addressBookEntryName);

    assertEquals(
        from.toHeaderspace(w), HeaderSpace.builder().setSrcIps(EmptyIpSpace.INSTANCE).build());
  }
}
