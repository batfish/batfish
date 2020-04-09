package org.batfish.representation.juniper;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.juniper.FwTerm.Field;

/** Test for security policy match source-address */
public final class FwFromSourceAddressBookEntry implements FwFrom {

  private final String _addressBookEntryName;

  private final AddressBook _globalAddressBook;

  // if zone is null, consult the global address book; o/w, the zone's address book
  @Nullable private final Zone _zone;

  public FwFromSourceAddressBookEntry(
      Zone zone, AddressBook globalAddressBook, String addressBookEntryName) {
    _zone = zone;
    _globalAddressBook = globalAddressBook;
    _addressBookEntryName = addressBookEntryName;
  }

  @Override
  public Field getField() {
    return Field.SOURCE;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return new MatchHeaderSpace(toHeaderspace(w), getTraceElement());
  }

  @VisibleForTesting
  HeaderSpace toHeaderspace(Warnings w) {
    AddressBook addressBook = _zone == null ? _globalAddressBook : _zone.getAddressBook();
    String addressBookName = addressBook.getAddressBookName(_addressBookEntryName);
    IpSpace referencedIpSpace;
    if (addressBookName == null) {
      w.redFlag(String.format("Missing source address-book entry '%s'", _addressBookEntryName));
      // match nothing
      referencedIpSpace = EmptyIpSpace.INSTANCE;
    } else {
      String ipSpaceName = addressBookName + "~" + _addressBookEntryName;
      referencedIpSpace = new IpSpaceReference(ipSpaceName);
    }
    return HeaderSpace.builder().setSrcIps(referencedIpSpace).build();
  }

  private TraceElement getTraceElement() {
    return TraceElement.of(String.format("Matched source-address %s", _addressBookEntryName));
  }
}
