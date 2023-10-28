package org.batfish.datamodel.packet_policy;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.transformation.Transformation;

/** Apply packet transformation */
public class ApplyTransformation implements Statement {

  private static final String PROP_TRANSFORMATION = "transformation";

  private final @Nonnull Transformation _transformation;

  public ApplyTransformation(Transformation transformation) {
    _transformation = transformation;
  }

  @JsonProperty(PROP_TRANSFORMATION)
  public @Nonnull Transformation getTransformation() {
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

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(ApplyTransformation.class).toString();
  }

  @JsonCreator
  private static ApplyTransformation create(
      @JsonProperty(PROP_TRANSFORMATION) @Nullable Transformation transformation) {
    checkArgument(transformation != null, "Missing %s", PROP_TRANSFORMATION);
    return new ApplyTransformation(transformation);
  }
}
