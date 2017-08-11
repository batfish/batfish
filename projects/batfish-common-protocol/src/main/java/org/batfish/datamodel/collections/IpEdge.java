package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.Pair;
import org.batfish.datamodel.Ip;

public class IpEdge extends Pair<NodeIpPair, NodeIpPair> {

  private static final String PROP_IP1 = "ip1";

  private static final String PROP_IP2 = "ip2";

  private static final String PROP_NODE1 = "node1";

  private static final String PROP_NODE2 = "node2";

  /** */
  private static final long serialVersionUID = 1L;

  public IpEdge(NodeIpPair p1, NodeIpPair p2) {
    super(p1, p2);
  }

  @JsonCreator
  public IpEdge(
      @JsonProperty(PROP_NODE1) String node1,
      @JsonProperty(PROP_IP1) Ip ip1,
      @JsonProperty(PROP_NODE2) String node2,
      @JsonProperty(PROP_IP2) Ip ip2) {
    super(new NodeIpPair(node1, ip1), new NodeIpPair(node2, ip2));
  }

  @JsonProperty(PROP_IP1)
  public Ip getIp1() {
    return _first.getIp();
  }

  @JsonProperty(PROP_IP2)
  public Ip getIp2() {
    return _second.getIp();
  }

  @JsonProperty(PROP_NODE1)
  public String getNode1() {
    return _first.getNode();
  }

  @JsonProperty(PROP_NODE2)
  public String getNode2() {
    return _second.getNode();
  }

  @Override
  public String toString() {
    return "<" + getNode1() + ":" + getIp1() + ", " + getNode2() + ":" + getIp2() + ">";
  }
}
