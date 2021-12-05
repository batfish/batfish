package org.batfish.vendor.sonic.representation;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/**
 * Represents a "data plane L3" interface, defined under INTERFACE, LOOPBACK, PORTCHANNEL_INTERFACE,
 * or VLAN objects
 */
public class L3Interface implements Serializable {
  // TODO: Does configDb accommodate multiple addresses?
  private @Nullable final ConcreteInterfaceAddress _address;

  public L3Interface(@Nullable ConcreteInterfaceAddress address) {
    _address = address;
  }

  @Nullable
  public ConcreteInterfaceAddress getAddress() {
    return _address;
  }

  static Map<String, L3Interface> createL3Interfaces(Map<String, Object> interfaces) {
    Map<String, L3Interface> interfaceMap = new HashMap<>();
    for (String key : interfaces.keySet()) {
      String[] parts = key.split("\\|");
      interfaceMap.computeIfAbsent(parts[0], i -> new L3Interface(null));
      if (parts.length == 2) {
        try {
          ConcreteInterfaceAddress v4Address = ConcreteInterfaceAddress.parse(parts[1]);
          interfaceMap.put(parts[0], new L3Interface(v4Address));
        } catch (IllegalArgumentException e) {
          // ignore -- could be v6 address
        }
      }
    }
    return ImmutableMap.copyOf(interfaceMap);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof L3Interface)) {
      return false;
    }
    L3Interface that = (L3Interface) o;
    return Objects.equals(_address, that._address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_address);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("v4Address", _address).toString();
  }
}
