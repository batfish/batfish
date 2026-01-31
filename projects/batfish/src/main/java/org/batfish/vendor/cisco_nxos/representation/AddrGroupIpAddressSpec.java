package org.batfish.vendor.cisco_nxos.representation;

import javax.annotation.Nonnull;

/** An {@link IpAddressSpec} delegating to an object-group. */
public final class AddrGroupIpAddressSpec implements IpAddressSpec {

  private final @Nonnull String _name;

  public AddrGroupIpAddressSpec(String name) {
    _name = name;
  }

  @Override
  public <T> T accept(IpAddressSpecVisitor<T> visitor) {
    return visitor.visitAddrGroupIpAddressSpec(this);
  }

  public @Nonnull String getName() {
    return _name;
  }
}
