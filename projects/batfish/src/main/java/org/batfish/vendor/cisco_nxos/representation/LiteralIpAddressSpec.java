package org.batfish.vendor.cisco_nxos.representation;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IpSpace;

/** An {@link IpAddressSpec} consisting of a literal {@link IpSpace}. */
public final class LiteralIpAddressSpec implements IpAddressSpec {

  private final @Nonnull IpSpace _ipSpace;

  public LiteralIpAddressSpec(IpSpace ipSpace) {
    _ipSpace = ipSpace;
  }

  @Override
  public <T> T accept(IpAddressSpecVisitor<T> visitor) {
    return visitor.visitLiteralIpAddressSpec(this);
  }

  public @Nonnull IpSpace getIpSpace() {
    return _ipSpace;
  }
}
