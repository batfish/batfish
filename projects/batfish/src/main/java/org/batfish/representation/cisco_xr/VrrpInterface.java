package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

public class VrrpInterface implements Serializable {

  private final SortedMap<Integer, VrrpGroup> _vrrpGroups;

  public VrrpInterface() {
    _vrrpGroups = new TreeMap<>();
  }

  public SortedMap<Integer, VrrpGroup> getVrrpGroups() {
    return _vrrpGroups;
  }
}
