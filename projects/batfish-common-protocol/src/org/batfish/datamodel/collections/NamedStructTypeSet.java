package org.batfish.datamodel.collections;

import java.util.TreeSet;

import org.batfish.datamodel.NamedStructType;

public class NamedStructTypeSet extends TreeSet<NamedStructType> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public NamedStructTypeSet() {

   }

   public NamedStructTypeSet(NamedStructType nType) {
      add(nType);
   }
}
