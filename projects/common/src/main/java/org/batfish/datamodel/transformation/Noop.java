package org.batfish.datamodel.transformation;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.DEST_NAT;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;

/**
 * A {@link TransformationStep} that does nothing to the packet. It exists so we can record the noop
 * in the trace.
 */
@ParametersAreNonnullByDefault
public final class Noop implements TransformationStep, Serializable {

  public static final Noop NOOP_DEST_NAT = new Noop(DEST_NAT);
  public static final Noop NOOP_SOURCE_NAT = new Noop(SOURCE_NAT);
  private static final String PROP_TRANSFORMATION_TYPE = "transformationType";

  private final TransformationType _type;

  public Noop(TransformationType type) {
    _type = type;
  }

  @JsonCreator
  private static Noop jsonCreator(@JsonProperty(PROP_TRANSFORMATION_TYPE) TransformationType type) {
    checkNotNull(type, PROP_TRANSFORMATION_TYPE + " cannot be null");
    return new Noop(type);
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
    return Objects.hashCode(_type);
  }

  @JsonProperty(PROP_TRANSFORMATION_TYPE)
  public TransformationType getType() {
    return _type;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add(PROP_TRANSFORMATION_TYPE, _type).toString();
  }
}
