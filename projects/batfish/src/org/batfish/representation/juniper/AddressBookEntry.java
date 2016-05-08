package org.batfish.representation.juniper;

import java.util.Set;

import org.batfish.common.datamodel.Prefix;
import org.batfish.common.util.ComparableStructure;
import org.batfish.main.Warnings;

public abstract class AddressBookEntry extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public AddressBookEntry(String name) {
      super(name);
   }

   public abstract Set<Prefix> getPrefixes(Warnings w);

}
