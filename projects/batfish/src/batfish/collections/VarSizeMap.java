package batfish.collections;

import java.util.TreeMap;

public class VarSizeMap extends TreeMap<String, Integer> {

   private static final long serialVersionUID = 1L;

   @Override
   public Integer get(Object i) {
      Integer ret = super.get(i);
      if (ret == null) {
         throw new Error("Key not found: " + i.toString());
      }
      else {
         return ret;
      }
   }
   
}
