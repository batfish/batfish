package org.batfish.common.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class ReferenceCountedStructure implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private transient Map<Object, String> _referers;

   public Map<Object, String> getReferers() {
      initReferers();
      return _referers;
   }

   private void initReferers() {
      if (_referers == null) {
         _referers = new HashMap<Object, String>();
      }
   }

   public boolean isUnused() {
      initReferers();
      return _referers.isEmpty();
   }

}
