package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/** Represents a Juniper nat rule set */
public final class NatRuleSet implements Serializable {

  private static final long serialVersionUID = 1L;

  private final NatPacketLocation _fromLocation;

  private final Map<String, NatRule> _rules;

  private final NatPacketLocation _toLocation;

  public NatRuleSet() {
    _fromLocation = new NatPacketLocation();
    _rules = new TreeMap<>();
    _toLocation = new NatPacketLocation();
  }

  public NatPacketLocation getFromLocation() {
    return _fromLocation;
  }

  public Map<String, NatRule> getRules() {
    return _rules;
  }

  public NatPacketLocation getToLocation() {
    return _toLocation;
  }
}
