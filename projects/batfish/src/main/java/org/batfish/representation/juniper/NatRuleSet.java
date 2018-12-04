package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a Juniper nat rule set */
@ParametersAreNonnullByDefault
public final class NatRuleSet implements Serializable {

  private static final long serialVersionUID = 1L;

  private final NatPacketLocation _fromLocation;

  private final List<NatRule> _rules;

  private final NatPacketLocation _toLocation;

  public NatRuleSet() {
    _fromLocation = new NatPacketLocation();
    _rules = new ArrayList<>();
    _toLocation = new NatPacketLocation();
  }

  @Nonnull
  public NatPacketLocation getFromLocation() {
    return _fromLocation;
  }

  @Nonnull
  public List<NatRule> getRules() {
    return _rules;
  }

  @Nonnull
  public NatPacketLocation getToLocation() {
    return _toLocation;
  }
}
