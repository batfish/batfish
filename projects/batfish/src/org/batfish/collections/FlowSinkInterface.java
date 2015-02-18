package org.batfish.collections;

public class FlowSinkInterface extends Pair<String, String> {

   private static final long serialVersionUID = 1L;

   public FlowSinkInterface(String node, String iface) {
      super(node, iface);
   }

   public String getInterface() {
      return _t2;
   }

   public String getNode() {
      return _t1;
   }

}
