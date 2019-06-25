package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class NodeDevice implements Serializable {

  private final Map<String, Interface> _interfaces;

  public NodeDevice() {
    _interfaces = new TreeMap<>();
  }

  public Map<String, Interface> getInterfaces() {
    return _interfaces;
  }
}
