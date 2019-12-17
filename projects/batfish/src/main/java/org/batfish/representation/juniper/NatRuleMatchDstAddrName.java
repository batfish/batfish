package org.batfish.representation.juniper;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A {@link NatRule} that matches on destination address name */
@ParametersAreNonnullByDefault
public final class NatRuleMatchDstAddrName implements NatRuleMatch {

  private final String _name;

  public NatRuleMatchDstAddrName(String name) {
    _name = name;
  }

  @Override
  public <T> T accept(NatRuleMatchVisitor<T> visitor) {
    return visitor.visitNatRuleMatchDstAddrName(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NatRuleMatchDstAddrName)) {
      return false;
    }
    NatRuleMatchDstAddrName that = (NatRuleMatchDstAddrName) o;
    return Objects.equals(_name, that._name);
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_name);
  }
}
