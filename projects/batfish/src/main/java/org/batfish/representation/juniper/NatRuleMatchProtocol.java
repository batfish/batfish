package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpProtocol;

/** A {@link NatRule} that matches on IP Protocol. */
@ParametersAreNonnullByDefault
public final class NatRuleMatchProtocol implements NatRuleMatch {

  private final @Nonnull IpProtocol _protocol;

  public NatRuleMatchProtocol(IpProtocol protocol) {
    _protocol = protocol;
  }

  @Override
  public <T> T accept(NatRuleMatchVisitor<T> visitor) {
    return visitor.visitNatRuleMatchProtocol(this);
  }

  public @Nonnull IpProtocol getProtocol() {
    return _protocol;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof NatRuleMatchProtocol)) {
      return false;
    }
    NatRuleMatchProtocol that = (NatRuleMatchProtocol) o;
    return _protocol == that._protocol;
  }

  @Override
  public int hashCode() {
    return _protocol.ordinal();
  }
}
