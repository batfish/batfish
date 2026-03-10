package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.common.Warnings;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.junit.Test;

/** Test for {@link FwFromDestinationAddressBookEntry} */
public class FwFromDestinationAddressBookEntryTest {

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

    FwFromDestinationAddressBookEntry from =
        new FwFromDestinationAddressBookEntry(null, globalAddressBook, addressBookEntryName);

    assertThat(from.toIpSpace(w), equalTo(new IpSpaceReference("addressBook~addressBookEntry")));
  }

  @Test
  public void testToHeaderspace_noEntry() {
    String addressBookEntryName = "addressBookEntry";
    AddressBook globalAddressBook = new AddressBook("addressBook", null);
    Warnings w = new Warnings();

    FwFromDestinationAddressBookEntry from =
        new FwFromDestinationAddressBookEntry(null, globalAddressBook, addressBookEntryName);

    assertThat(from.toIpSpace(w), equalTo(EmptyIpSpace.INSTANCE));
  }
}
