package org.batfish.representation.juniper;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;

/** A {@link NatRule} that matches on destination address */
@ParametersAreNonnullByDefault
public final class NatRuleMatchDstAddr implements NatRuleMatch {

  private static final long serialVersionUID = 1L;

  private final Prefix _prefix;

  public NatRuleMatchDstAddr(Prefix prefix) {
    _prefix = prefix;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NatRuleMatchDstAddr)) {
      return false;
    }
    NatRuleMatchDstAddr that = (NatRuleMatchDstAddr) o;
    return Objects.equals(_prefix, that._prefix);
  }

  @Nonnull
  public Prefix getPrefix() {
    return _prefix;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_prefix);
  }
}
