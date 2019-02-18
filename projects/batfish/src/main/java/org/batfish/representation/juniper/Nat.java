package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a Juniper Nat */
@ParametersAreNonnullByDefault
public final class Nat implements Serializable {

  public enum Type {
    DESTINATION,
    SOURCE,
    STATIC
  }

  private static final long serialVersionUID = 1L;

  private final Map<String, NatPool> _pools;

  private final Map<String, NatRuleSet> _ruleSets;

  private final Type _type;

  private static final int DEFAULT_FROM_PORT = 1024;

  private static final int DEFAULT_TO_PORT = 63487;

  private int _defaultFromPort;

  private int _defaultToPort;

  public Nat(Type type) {
    _type = type;
    _pools = new TreeMap<>();
    _ruleSets = new TreeMap<>();
    _defaultFromPort = DEFAULT_FROM_PORT;
    _defaultToPort = DEFAULT_TO_PORT;
  }

  @Nonnull
  public Map<String, NatPool> getPools() {
    return _pools;
  }

  @Nonnull
  public Map<String, NatRuleSet> getRuleSets() {
    return _ruleSets;
  }

  @Nonnull
  public Type getType() {
    return _type;
  }

  public int getDefaultFromPort() {
    return _defaultFromPort;
  }

  public int getDefaultToPort() {
    return _defaultToPort;
  }

  public void setDefaultFromPort(int fromPort) {
    _defaultFromPort = fromPort;
  }

  public void setDefaultToPort(int toPort) {
    _defaultToPort = toPort;
  }

  public void populateDefaultPortRange() {
    if (_type != Type.SOURCE) {
      // only populate port range for source nat
      return;
    }
    _pools.entrySet().stream()
        .forEach(
            entry -> {
              NatPool pool = entry.getValue();
              if (pool.getPortAddressTranslation() == null) {
                pool.setPortAddressTranslation(new PatPool(_defaultFromPort, _defaultToPort));
              }
            });
  }
}
