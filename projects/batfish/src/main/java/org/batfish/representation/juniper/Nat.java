package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;

/** Represents a Juniper Nat */
@ParametersAreNonnullByDefault
public final class Nat implements Serializable {

  public enum Type {
    DESTINATION,
    SOURCE,
    STATIC;

    public TransformationType toTransformationType() {
      return switch (this) {
        case SOURCE -> TransformationType.SOURCE_NAT;
        case DESTINATION -> TransformationType.DEST_NAT;
        case STATIC -> TransformationType.STATIC_NAT;
      };
    }
  }

  private final Map<String, NatPool> _pools;

  private final Map<String, NatRuleSet> _ruleSets;

  private final Type _type;

  public static final int DEFAULT_FROM_PORT = 1024;

  public static final int DEFAULT_TO_PORT = 63487;

  private int _defaultFromPort;

  private int _defaultToPort;

  public Nat(Type type) {
    _type = type;
    _pools = new TreeMap<>();
    _ruleSets = new TreeMap<>();
    _defaultFromPort = DEFAULT_FROM_PORT;
    _defaultToPort = DEFAULT_TO_PORT;
  }

  public @Nonnull Map<String, NatPool> getPools() {
    return _pools;
  }

  public @Nonnull Map<String, NatRuleSet> getRuleSets() {
    return _ruleSets;
  }

  public @Nonnull Type getType() {
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
}
