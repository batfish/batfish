package org.batfish.representation.juniper;

import java.util.Set;
import java.util.TreeSet;

import org.batfish.representation.Prefix;
import org.batfish.util.ReferenceCountedStructure;

public class PrefixList extends ReferenceCountedStructure {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _name;

   private Set<Prefix> _prefixes;

   public PrefixList(String name) {
      _name = name;
      _prefixes = new TreeSet<Prefix>();
   }

   public String getName() {
      return _name;
   }

   public Set<Prefix> getPrefixes() {
      return _prefixes;
   }

}
