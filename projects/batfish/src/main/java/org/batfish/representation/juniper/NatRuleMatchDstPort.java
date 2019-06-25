package org.batfish.representation.juniper;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;

/** A {@link NatRule} that matches on destination port */
@ParametersAreNonnullByDefault
public final class NatRuleMatchDstPort implements NatRuleMatch {

  private final int _from;

  private final int _to;

  public NatRuleMatchDstPort(int from, int to) {
    _from = from;
    _to = to;
  }

  @Override
  public <T> T accept(NatRuleMatchVisitor<T> visitor) {
    return visitor.visitNatRuleMatchDstPort(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NatRuleMatchDstPort)) {
      return false;
    }
    NatRuleMatchDstPort that = (NatRuleMatchDstPort) o;
    return _from == that._from && _to == that._to;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_from, _to);
  }

  public int getFrom() {
    return _from;
  }

  public int getTo() {
    return _to;
  }
}
