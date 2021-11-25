package org.batfish.vendor.sonic.representation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.ConcreteInterfaceAddress;

@ParametersAreNonnullByDefault
public class InterfaceDb implements ConfigDbObject {

  public static class Interface implements Serializable {
    private @Nullable final ConcreteInterfaceAddress _v4Address;

    public Interface(@Nullable ConcreteInterfaceAddress v4address) {
      _v4Address = v4address;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Interface)) {
        return false;
      }
      Interface that = (Interface) o;
      return Objects.equals(_v4Address, that._v4Address);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_v4Address);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).add("v4Address", _v4Address).toString();
    }
  }

  private @Nonnull final Map<String, Interface> _interfaces;

  public InterfaceDb(Map<String, Interface> interfaces) {
    _interfaces = ImmutableMap.copyOf(interfaces);
  }

  @JsonCreator
  private static InterfaceDb create(Map<String, Object> interfaces) {
    Map<String, Interface> interfaceMap = new HashMap<>();
    for (String key : interfaces.keySet()) {
      String[] parts = key.split("\\|");
      interfaceMap.computeIfAbsent(parts[0], i -> new Interface(null));
      if (parts.length == 2) {
        try {
          ConcreteInterfaceAddress v4Address = ConcreteInterfaceAddress.parse(parts[1]);
          interfaceMap.put(parts[0], new Interface(v4Address));
        } catch (IllegalArgumentException e) {
          // ignore -- could be v6 address
        }
      }
    }
    return new InterfaceDb(interfaceMap);
  }

  @Nonnull
  public Map<String, Interface> getInterfaces() {
    return _interfaces;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InterfaceDb)) {
      return false;
    }
    InterfaceDb that = (InterfaceDb) o;
    return Objects.equals(_interfaces, that._interfaces);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_interfaces);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("interfaces", _interfaces).toString();
  }
}
