package org.batfish.collections;

import java.util.HashSet;

public class InterfaceSet extends HashSet<NodeInterfacePair> {

   private static final long serialVersionUID = 1L;

   public void removeNodes(NodeSet nodes) {
      InterfaceSet toRemove = new InterfaceSet();
      for (NodeInterfacePair iface : this) {
         String node = iface.getHostname();
         if (nodes.contains(node)) {
            toRemove.add(iface);
         }
      }
      removeAll(toRemove);
   }

}
