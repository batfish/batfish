package org.batfish.collections;

public class NodeInterfacePair extends Pair<String, String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public NodeInterfacePair(String t1, String t2) {
      super(t1, t2);
   }

   public String getHostname() {
      return _t1;
   }

   public String getInterface() {
      return _t2;
   }

   @Override
   public String toString() {
      return _t1 + ":" + _t2;
   }

}
