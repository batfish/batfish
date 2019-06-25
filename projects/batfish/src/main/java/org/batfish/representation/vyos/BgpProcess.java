package org.batfish.representation.vyos;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import org.batfish.datamodel.Ip;

public class BgpProcess implements Serializable {

  private final Map<Ip, BgpNeighbor> _neighbors;

  private final int _localAs;

  public BgpProcess(int localAs) {
    _localAs = localAs;
    _neighbors = new TreeMap<>();
  }

  public int getLocalAs() {
    return _localAs;
  }

  public Map<Ip, BgpNeighbor> getNeighbors() {
    return _neighbors;
  }
}
