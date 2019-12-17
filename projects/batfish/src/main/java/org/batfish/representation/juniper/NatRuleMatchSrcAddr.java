package org.batfish.representation.juniper;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;

/** A {@link NatRule} that matches on source address */
@ParametersAreNonnullByDefault
public final class NatRuleMatchSrcAddr implements NatRuleMatch {

  private final Prefix _prefix;

  public NatRuleMatchSrcAddr(Prefix prefix) {
    _prefix = prefix;
  }

  @Override
  public <T> T accept(NatRuleMatchVisitor<T> visitor) {
    return visitor.visitNatRuleMatchSrcAddr(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NatRuleMatchSrcAddr)) {
      return false;
    }
    NatRuleMatchSrcAddr that = (NatRuleMatchSrcAddr) o;
    return Objects.equals(_prefix, that._prefix);
  }

  @Nonnull
  public Prefix getPrefix() {
    return _prefix;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_prefix);
  }
}
