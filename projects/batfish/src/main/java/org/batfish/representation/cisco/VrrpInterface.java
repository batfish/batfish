package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

public class VrrpInterface implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private final int _definitionLine;

  private final String _name;

  private final SortedMap<Integer, VrrpGroup> _vrrpGroups;

  public VrrpInterface(String name, int definitionLine) {
    _name = name;
    _definitionLine = definitionLine;
    _vrrpGroups = new TreeMap<>();
  }

  public int getDefinitionLine() {
    return _definitionLine;
  }

  public SortedMap<Integer, VrrpGroup> getVrrpGroups() {
    return _vrrpGroups;
  }
}
