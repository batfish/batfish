package org.batfish.vendor.sonic.representation;

import static org.batfish.vendor.sonic.representation.L3Interface.createL3Interfaces;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents loopback interfaces, found under LOOPBACK:
 * https://github.com/Azure/SONiC/wiki/Configuration#loopback-interface
 */
@ParametersAreNonnullByDefault
public class LoopbackDb implements ConfigDbObject {

  private @Nonnull final Map<String, L3Interface> _interfaces;

  public LoopbackDb(Map<String, L3Interface> interfaces) {
    _interfaces = ImmutableMap.copyOf(interfaces);
  }

  @JsonCreator
  private static LoopbackDb create(Map<String, Object> interfaces) {
    return new LoopbackDb(createL3Interfaces(interfaces));
  }

  @Nonnull
  public Map<String, L3Interface> getInterfaces() {
    return _interfaces;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LoopbackDb)) {
      return false;
    }
    LoopbackDb that = (LoopbackDb) o;
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
