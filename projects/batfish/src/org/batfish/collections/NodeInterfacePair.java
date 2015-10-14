package org.batfish.collections;

import org.batfish.common.Pair;

public class NodeInterfacePair extends Pair<String, String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public NodeInterfacePair(String node, String iface) {
      super(node, iface);
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
