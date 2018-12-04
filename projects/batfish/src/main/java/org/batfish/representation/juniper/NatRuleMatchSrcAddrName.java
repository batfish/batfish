package org.batfish.representation.juniper;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A {@link NatRule} that matches on source address name */
@ParametersAreNonnullByDefault
public final class NatRuleMatchSrcAddrName implements NatRuleMatch {

  private static final long serialVersionUID = 1L;

  private final String _name;

  public NatRuleMatchSrcAddrName(String name) {
    _name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NatRuleMatchSrcAddrName)) {
      return false;
    }
    NatRuleMatchSrcAddrName that = (NatRuleMatchSrcAddrName) o;
    return Objects.equals(_name, that._name);
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name);
  }
}
