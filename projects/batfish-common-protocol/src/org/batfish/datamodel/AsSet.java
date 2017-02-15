package org.batfish.datamodel;

import java.util.LinkedHashSet;

public class AsSet extends LinkedHashSet<Integer> {

   private static final long serialVersionUID = 1L;

   public AsSet() {
      super();
   }

   public AsSet(AsSet set) {
      super(set);
   }

}
