package org.batfish.vendor.cisco_nxos.representation;

import javax.annotation.Nonnull;

/** A {@link PortSpec} delegating to an object-group. */
public final class PortGroupPortSpec implements PortSpec {

  private final @Nonnull String _name;

  public PortGroupPortSpec(String name) {
    _name = name;
  }

  @Override
  public <T> T accept(PortSpecVisitor<T> visitor) {
    return visitor.visitPortGroupPortSpec(this);
  }

  public @Nonnull String getName() {
    return _name;
  }
}
