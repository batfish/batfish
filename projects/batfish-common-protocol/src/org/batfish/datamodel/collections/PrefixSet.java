package org.batfish.datamodel.collections;

import java.util.TreeSet;

import org.batfish.datamodel.Prefix;

public class PrefixSet extends TreeSet<Prefix> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public PrefixSet() {

   }

   public PrefixSet(Prefix prefix) {
      add(prefix);
   }
}
