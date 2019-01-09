package org.batfish.datamodel.transformation;

import static org.batfish.datamodel.flow.TransformationStep.TransformationType.DEST_NAT;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;

/**
 * A {@link TransformationStep} that does nothing to the packet. It exists so we can record the noop
 * in the trace.
 */
@ParametersAreNonnullByDefault
public final class Noop implements TransformationStep {
  private final TransformationType _type;

  public static final Noop NOOP_DEST_NAT = new Noop(DEST_NAT);
  public static final Noop NOOP_SOURCE_NAT = new Noop(SOURCE_NAT);

  public Noop(TransformationType type) {
    _type = type;
  }

  public static Noop noop(IpField field) {
    switch (field) {
      case DESTINATION:
        return NOOP_DEST_NAT;
      case SOURCE:
        return NOOP_SOURCE_NAT;
      default:
        throw new BatfishException("Unknown IpField: " + field);
    }
  }

  @Override
  public <T> T accept(TransformationStepVisitor<T> visitor) {
    return visitor.visitNoop(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Noop)) {
      return false;
    }
    Noop noop = (Noop) o;
    return _type == noop._type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type);
  }

  @Override
  public TransformationType getType() {
    return _type;
  }
}
