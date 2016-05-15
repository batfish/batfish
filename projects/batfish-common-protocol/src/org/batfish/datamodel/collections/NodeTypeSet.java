package org.batfish.datamodel.collections;

import java.util.TreeSet;

import org.batfish.datamodel.NodeType;

public class NodeTypeSet extends TreeSet<NodeType> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public NodeTypeSet() {

   }

   public NodeTypeSet(NodeType nType) {
      add(nType);
   }
}
