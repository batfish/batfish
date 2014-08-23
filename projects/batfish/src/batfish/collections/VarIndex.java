package batfish.collections;

import java.util.HashMap;

public class VarIndex extends HashMap<Integer, String> {

   private static final long serialVersionUID = 1L;

   @Override
   public String get(Object i) {
      String ret = super.get(i);
      if (ret == null) {
         throw new Error("Key not found: " + i.toString());
      }
      else {
         return ret;
      }
   }
   
}
