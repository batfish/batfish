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

  public Nat(Type type) {
    _type = type;
    _pools = new TreeMap<>();
    _ruleSets = new TreeMap<>();
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
}
