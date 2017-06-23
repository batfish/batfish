package org.batfish.datamodel.collections;

import org.batfish.common.Pair;
import org.batfish.datamodel.Ip;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IpEdge extends Pair<NodeIpPair, NodeIpPair> {

   private static final String IP1_VAR = "ip1";

   private static final String IP2_VAR = "ip2";

   private static final String NODE1_VAR = "node1";

   private static final String NODE2_VAR = "node2";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public IpEdge(NodeIpPair p1, NodeIpPair p2) {
      super(p1, p2);
   }

   @JsonCreator
   public IpEdge(@JsonProperty(NODE1_VAR) String node1,
         @JsonProperty(IP1_VAR) Ip ip1, @JsonProperty(NODE2_VAR) String node2,
         @JsonProperty(IP2_VAR) Ip ip2) {
      super(new NodeIpPair(node1, ip1), new NodeIpPair(node2, ip2));
   }

   @JsonProperty(IP1_VAR)
   public Ip getIp1() {
      return _first.getIp();
   }

   @JsonProperty(IP2_VAR)
   public Ip getIp2() {
      return _second.getIp();
   }

   @JsonProperty(NODE1_VAR)
   public String getNode1() {
      return _first.getNode();
   }

   @JsonProperty(NODE2_VAR)
   public String getNode2() {
      return _second.getNode();
   }

   @Override
   public String toString() {
      return "<" + getNode1() + ":" + getIp1() + ", " + getNode2() + ":"
            + getIp2() + ">";
   }
}
