package org.batfish.datamodel;

import java.util.ArrayList;
import java.util.Iterator;

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

   public String getAsPathString() {
      StringBuilder sb = new StringBuilder();
      for (AsSet asSet : this) {
         if (asSet.size() == 1) {
            int elem = asSet.iterator().next();
            sb.append(elem);
         }
         else {
            sb.append("{");
            Iterator<Integer> i = asSet.iterator();
            sb.append(i.next());
            while (i.hasNext()) {
               sb.append(",");
               sb.append(i.next());
            }
            sb.append("}");
         }
         sb.append(" ");
      }
      String result = sb.toString().trim();
      return result;
   }

}
