package org.batfish.vendor.cisco_nxos.representation;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IntegerSpace;

/** A {@link PortSpec} consisting of a literal {@link IntegerSpace}. */
public final class LiteralPortSpec implements PortSpec {

  private final @Nonnull IntegerSpace _ports;

  public LiteralPortSpec(IntegerSpace ports) {
    _ports = ports;
  }

  @Override
  public <T> T accept(PortSpecVisitor<T> visitor) {
    return visitor.visitLiteralPortSpec(this);
  }

  public @Nonnull IntegerSpace getPorts() {
    return _ports;
  }
}
