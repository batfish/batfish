package org.batfish.representation.cisco;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.SubRange;

/** A {@link PortSpec} consisting of a literal port ranges. */
@ParametersAreNonnullByDefault
public final class LiteralPortSpec implements PortSpec {
  public static LiteralPortSpec ALL_PORTS = new LiteralPortSpec(IntegerSpace.PORTS.getSubRanges());

  private final List<SubRange> _ports;

  public LiteralPortSpec(Iterable<SubRange> ports) {
    _ports = ImmutableList.copyOf(ports);
  }

  @Override
  public <T> T accept(PortSpecVisitor<T> visitor) {
    return visitor.visitLiteralPortSpec(this);
  }

  public @Nonnull List<SubRange> getPorts() {
    return _ports;
  }
}
