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
      return _first;
   }

   public String getInterface() {
      return _second;
   }

   @Override
   public String toString() {
      return _first + ":" + _second;
   }

}
