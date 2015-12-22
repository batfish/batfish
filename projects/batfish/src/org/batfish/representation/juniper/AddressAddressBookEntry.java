package org.batfish.representation.juniper;

import java.util.Collections;
import java.util.Set;

import org.batfish.main.Warnings;
import org.batfish.representation.Prefix;

public final class AddressAddressBookEntry extends AddressBookEntry {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Prefix _prefix;

   public AddressAddressBookEntry(String name, Prefix prefix) {
      super(name);
      _prefix = prefix;
   }

   public Prefix getPrefix() {
      return _prefix;
   }

   @Override
   public Set<Prefix> getPrefixes(Warnings w) {
      return Collections.singleton(_prefix);
   }

}
