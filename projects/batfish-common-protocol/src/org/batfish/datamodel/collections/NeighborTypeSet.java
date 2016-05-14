package org.batfish.datamodel.collections;

import java.util.TreeSet;

import org.batfish.datamodel.NeighborType;

public class NeighborTypeSet extends TreeSet<NeighborType> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public NeighborTypeSet() {

   }

   public NeighborTypeSet(NeighborType nType) {
      add(nType);
   }
}
