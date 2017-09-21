package org.batfish.representation.cisco;

import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.util.ComparableStructure;

public class VrrpInterface extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private final int _definitionLine;

  private final SortedMap<Integer, VrrpGroup> _vrrpGroups;

  public VrrpInterface(String name, int definitionLine) {
    super(name);
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
