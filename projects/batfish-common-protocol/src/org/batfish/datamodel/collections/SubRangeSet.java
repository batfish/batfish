package org.batfish.datamodel.collections;

import java.util.TreeSet;

import org.batfish.datamodel.SubRange;

public class SubRangeSet extends TreeSet<SubRange> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public SubRangeSet() {

   }

   public SubRangeSet(SubRange sRange) {
      add(sRange);
   }
}
