package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a Juniper nat rule set */
@ParametersAreNonnullByDefault
public final class NatRuleSet implements Serializable, Comparable<NatRuleSet> {

  private static final long serialVersionUID = 1L;

  private final NatPacketLocation _fromLocation;

  private final List<NatRule> _rules;

  private final NatPacketLocation _toLocation;

  private final String _name;

  public NatRuleSet(String name) {
    _name = name;
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

  @Nonnull
  public String getName() {
    return _name;
  }

  /* Sort NatRuleSet according to the following order:
   * 1. Source interface/destination interface
   * 2. Source zone/destination interface
   * 3. Source routing instance/destination interface
   * 4. Source interface/destination zone
   * 5. Source zone/destination zone
   * 6. Source routing instance/destination zone
   * 7. Source interface/destination routing instance
   * 8. Source zone/destination routing instance
   * 9. Source routing instance/destination routing instance
   */
  private static final Comparator<NatRuleSet> COMPARATOR =
      Comparator.comparing(NatRuleSet::getToLocation).thenComparing(NatRuleSet::getFromLocation);

  @Override
  public int compareTo(NatRuleSet o) {
    return COMPARATOR.compare(this, o);
  }
}
