package org.batfish.representation.juniper;

import java.util.Map;
import java.util.TreeMap;
import org.batfish.common.util.ComparableStructure;

public class NodeDevice extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private final Map<String, Interface> _interfaces;

  public NodeDevice(String name) {
    super(name);
    _interfaces = new TreeMap<>();
  }

  public Map<String, Interface> getInterfaces() {
    return _interfaces;
  }
}
