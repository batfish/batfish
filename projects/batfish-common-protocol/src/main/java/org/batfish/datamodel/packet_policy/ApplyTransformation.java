package org.batfish.datamodel.packet_policy;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.transformation.Transformation;

/** Apply packet transformation */
public class ApplyTransformation implements Statement {

  private static final String PROP_TRANSFORMATION = "transformation";

  @Nonnull private final Transformation _transformation;

  public ApplyTransformation(Transformation transformation) {
    _transformation = transformation;
  }

  @Nonnull
  @JsonProperty(PROP_TRANSFORMATION)
  public Transformation getTransformation() {
    return _transformation;
  }

  @Override
  public <T> T accept(StatementVisitor<T> visitor) {
    return visitor.visitApplyTransformation(this);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ApplyTransformation)) {
      return false;
    }
    ApplyTransformation that = (ApplyTransformation) o;
    return _transformation.equals(that._transformation);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_transformation);
  }

  @JsonCreator
  private static ApplyTransformation create(
      @Nullable @JsonProperty(PROP_TRANSFORMATION) Transformation transformation) {
    checkArgument(transformation != null, "Missing %s", PROP_TRANSFORMATION);
    return new ApplyTransformation(transformation);
  }
}
