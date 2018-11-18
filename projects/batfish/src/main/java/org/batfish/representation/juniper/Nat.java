package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class Nat implements Serializable {

  public enum Type {
    DESTINATION,
    SOURCE,
    STATIC
  }

  /** */
  private static final long serialVersionUID = 1L;

  private final Map<String, NatPool> _pools;

  private final Map<String, NatRuleSet> _ruleSets;

  private final Type _type;

  public Nat(Type type) {
    _type = type;
    _pools = new TreeMap<>();
    _ruleSets = new TreeMap<>();
  }

  public Map<String, NatPool> getPools() {
    return _pools;
  }

  public Map<String, NatRuleSet> getRuleSets() {
    return _ruleSets;
  }

  public Type getType() {
    return _type;
  }
}
