package org.batfish.datamodel;

import java.util.ArrayList;

public class AsPath extends ArrayList<AsSet> {

   private static final long serialVersionUID = 1L;

   public AsPath() {
      super();
   }

   public AsPath(int size) {
      for (int i = 0; i < size; i++) {
         add(new AsSet());
      }
   }

   public boolean containsAs(int as) {
      for (AsSet asSet : this) {
         if (asSet.contains(as)) {
            return true;
         }
      }
      return false;
   }

}
