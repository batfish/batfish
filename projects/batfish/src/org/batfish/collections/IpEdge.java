package org.batfish.collections;

import org.batfish.common.Pair;
import org.batfish.representation.Ip;

public class IpEdge extends Pair<NodeIpPair, NodeIpPair> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public IpEdge(NodeIpPair p1, NodeIpPair p2) {
      super(p1, p2);
   }

   public Ip getIp1() {
      return _first.getIp();
   }

   public Ip getIp2() {
      return _second.getIp();
   }

   public String getNode1() {
      return _first.getNode();
   }

   public String getNode2() {
      return _second.getNode();
   }

}
