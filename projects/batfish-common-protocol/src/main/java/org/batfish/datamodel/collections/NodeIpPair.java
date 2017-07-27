package org.batfish.datamodel.collections;

import org.batfish.common.Pair;
import org.batfish.datamodel.Ip;

public class NodeIpPair extends Pair<String, Ip> {

  /** */
  private static final long serialVersionUID = 1L;

  public NodeIpPair(String t1, Ip t2) {
    super(t1, t2);
  }

  public Ip getIp() {
    return _second;
  }

  public String getNode() {
    return _first;
  }
}
