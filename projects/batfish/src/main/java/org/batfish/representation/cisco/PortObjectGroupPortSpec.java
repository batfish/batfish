package org.batfish.representation.cisco;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents an {@code ip port-group} */
@ParametersAreNonnullByDefault
public final class PortObjectGroupPortSpec implements PortSpec {

  private @Nonnull final String _name;

  public PortObjectGroupPortSpec(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public <T> T accept(PortSpecVisitor<T> visitor) {
    return visitor.visitPortGroupPortSpec(this);
  }
}
