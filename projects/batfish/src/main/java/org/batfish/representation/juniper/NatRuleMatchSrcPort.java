package org.batfish.representation.juniper;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;

/** A {@link NatRule} that matches on source port */
@ParametersAreNonnullByDefault
public final class NatRuleMatchSrcPort implements NatRuleMatch {

  private final int _from;

  private final int _to;

  public NatRuleMatchSrcPort(int from, int to) {
    _from = from;
    _to = to;
  }

  @Override
  public <T> T accept(NatRuleMatchVisitor<T> visitor) {
    return visitor.visitNatRuleMatchSrcPort(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NatRuleMatchSrcPort)) {
      return false;
    }
    NatRuleMatchSrcPort that = (NatRuleMatchSrcPort) o;
    return _from == that._from && _to == that._to;
  }

  public int getFrom() {
    return _from;
  }

  public int getTo() {
    return _to;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_from, _to);
  }
}
