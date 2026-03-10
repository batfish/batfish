package org.batfish.representation.juniper;

import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for security policy match destination-address */
public final class FwFromDestinationAddressBookEntry implements FwFrom {

  private final String _addressBookEntryName;

  private final AddressBook _globalAddressBook;

  // if zone is null, consult the global address book; o/w, the zone's address book
  final @Nullable Zone _zone;

  public FwFromDestinationAddressBookEntry(
      Zone zone, AddressBook globalAddressBook, String addressBookEntryName) {
    _zone = zone;
    _globalAddressBook = globalAddressBook;
    _addressBookEntryName = addressBookEntryName;
  }

  @Override
  public Field getField() {
    return Field.DESTINATION;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return matchDst(toIpSpace(w), getTraceElement());
  }

  @VisibleForTesting
  IpSpace toIpSpace(Warnings w) {
    AddressBook addressBook = _zone == null ? _globalAddressBook : _zone.getAddressBook();
    String addressBookName = addressBook.getAddressBookName(_addressBookEntryName);
    IpSpace referencedIpSpace;
    if (addressBookName == null) {
      w.redFlagf("Missing destination address-book entry '%s'", _addressBookEntryName);
      // match nothing
      referencedIpSpace = EmptyIpSpace.INSTANCE;
    } else {
      String ipSpaceName = addressBookName + "~" + _addressBookEntryName;
      referencedIpSpace = new IpSpaceReference(ipSpaceName);
    }
    return referencedIpSpace;
  }

  private TraceElement getTraceElement() {
    return TraceElement.of(String.format("Matched destination-address %s", _addressBookEntryName));
  }
}
