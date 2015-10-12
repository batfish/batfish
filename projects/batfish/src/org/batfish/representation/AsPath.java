package org.batfish.representation;

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

}
