package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.TraceElement;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for security policy match destination-address */
public final class FwFromDestinationAddressBookEntry extends FwFrom {

  private final String _addressBookEntryName;

  private final AddressBook _globalAddressBook;

  // if zone is null, consult the global address book; o/w, the zone's address book
  @Nullable final Zone _zone;

  public FwFromDestinationAddressBookEntry(
      Zone zone, AddressBook globalAddressBook, String addressBookEntryName) {
    _zone = zone;
    _globalAddressBook = globalAddressBook;
    _addressBookEntryName = addressBookEntryName;
  }

  @Override
  public void applyTo(
      HeaderSpace.Builder headerSpaceBuilder,
      JuniperConfiguration jc,
      Warnings w,
      Configuration c) {
    AddressBook addressBook = _zone == null ? _globalAddressBook : _zone.getAddressBook();
    String addressBookName = addressBook.getAddressBookName(_addressBookEntryName);
    if (addressBookName == null) {
      w.redFlag(
          String.format("Missing destination address-book entry '%s'", _addressBookEntryName));
      // Leave existing constraint, otherwise match nothing
      if (headerSpaceBuilder.getDstIps() == null) {
        headerSpaceBuilder.setDstIps(EmptyIpSpace.INSTANCE);
      }
      return;
    }
    String ipSpaceName = addressBookName + "~" + _addressBookEntryName;
    IpSpaceReference ipSpaceReference = new IpSpaceReference(ipSpaceName);
    if (headerSpaceBuilder.getDstIps() != null) {
      headerSpaceBuilder.setDstIps(
          AclIpSpace.union(
              ImmutableList.<IpSpace>builder()
                  .add(ipSpaceReference)
                  .add(headerSpaceBuilder.getDstIps())
                  .build()));
    } else {
      headerSpaceBuilder.setDstIps(AclIpSpace.union(ipSpaceReference));
    }
  }

  @Override
  Field getField() {
    return Field.DESTINATION;
  }

  @Override
  TraceElement getTraceElement() {
    return TraceElement.of(String.format("Matched destination-address %s", _addressBookEntryName));
  }

  @Override
  HeaderSpace toHeaderspace(JuniperConfiguration jc, Configuration c, Warnings w) {
    AddressBook addressBook = _zone == null ? _globalAddressBook : _zone.getAddressBook();
    String addressBookName = addressBook.getAddressBookName(_addressBookEntryName);
    IpSpace referencedIpSpace;
    if (addressBookName == null) {
      w.redFlag(
          String.format("Missing destination address-book entry '%s'", _addressBookEntryName));
      // match nothing
      referencedIpSpace = EmptyIpSpace.INSTANCE;
    } else {
      String ipSpaceName = addressBookName + "~" + _addressBookEntryName;
      referencedIpSpace = new IpSpaceReference(ipSpaceName);
    }
    return HeaderSpace.builder().setDstIps(referencedIpSpace).build();
  }
}
