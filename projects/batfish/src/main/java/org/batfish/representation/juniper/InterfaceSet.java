package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/** Represents a set of interfaces specified by `firewall interface-set FOO [interfaces]`. */
public class InterfaceSet implements Serializable {
  private final Set<String> _interfaces;

  public InterfaceSet() {
    _interfaces = new HashSet<>();
  }

  public void addInterface(String iface) {
    _interfaces.add(iface);
  }

  /**
   * Get the interfaces specified in this interface set. These can be literal interface names or
   * interface wildcards.
   */
  public Set<String> getInterfaces() {
    return ImmutableSet.copyOf(_interfaces);
  }
}
