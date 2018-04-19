package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;

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
    Set<Prefix> prefixes = _localAddressBook.getPrefixes(_addressBookEntryName, w);
    List<IpSpace> wildcards =
        prefixes.stream().map(Prefix::toIpSpace).collect(ImmutableList.toImmutableList());
    headerSpaceBuilder.setDstIps(
        AclIpSpace.union(
            ImmutableList.<IpSpace>builder()
                .add(headerSpaceBuilder.getDstIps())
                .addAll(wildcards)
                .build()));
  }
}
