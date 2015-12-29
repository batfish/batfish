package org.batfish.representation.juniper;

import java.util.Set;

import org.batfish.main.Warnings;
import org.batfish.representation.Prefix;
import org.batfish.util.NamedStructure;

public abstract class AddressBookEntry extends NamedStructure {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public AddressBookEntry(String name) {
      super(name);
   }

   public abstract Set<Prefix> getPrefixes(Warnings w);

}
