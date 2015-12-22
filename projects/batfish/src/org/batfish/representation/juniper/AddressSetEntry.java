package org.batfish.representation.juniper;

import java.util.Set;

import org.batfish.main.Warnings;
import org.batfish.representation.Prefix;
import org.batfish.util.NamedStructure;

public final class AddressSetEntry extends NamedStructure {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   protected final AddressBook _book;

   public AddressSetEntry(String name, AddressBook book) {
      super(name);
      _book = book;
   }

   public Set<Prefix> getPrefixes(Warnings w) {
      return _book.getPrefixes(_name, w);
   }

}
