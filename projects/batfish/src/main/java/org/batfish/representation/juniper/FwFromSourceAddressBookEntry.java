package org.batfish.representation.juniper;

import java.util.Set;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.common.Warnings;

public final class FwFromSourceAddressBookEntry extends FwFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _addressBookEntryName;

   private final AddressBook _localAddressBook;

   public FwFromSourceAddressBookEntry(AddressBook localAddressBook,
         String addressBookEntryName) {
      _localAddressBook = localAddressBook;
      _addressBookEntryName = addressBookEntryName;
   }

   @Override
   public void applyTo(IpAccessListLine line, JuniperConfiguration jc,
         Warnings w, Configuration c) {
      Set<Prefix> prefixes = _localAddressBook
            .getPrefixes(_addressBookEntryName, w);
      for (Prefix prefix : prefixes) {
         IpWildcard wildcard = new IpWildcard(prefix);
         line.getSrcIps().add(wildcard);
      }
   }

}
