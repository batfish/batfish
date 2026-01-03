package org.batfish.datamodel.transformation;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Composite {@link TransformationStep} that applies exactly one of the provided steps */
@ParametersAreNonnullByDefault
public class ApplyAny implements TransformationStep, Serializable {
  private static final String PROP_STEPS = "steps";

  @JsonCreator
  private static final @Nonnull ApplyAny create(
      @JsonProperty(PROP_STEPS) @Nullable Iterable<TransformationStep> steps) {
    return new ApplyAny(firstNonNull(steps, ImmutableList.of()));
  }

  private final @Nonnull List<TransformationStep> _steps;

  public ApplyAny(Iterable<TransformationStep> steps) {
    _steps = ImmutableList.copyOf(steps);
  }

  public ApplyAny(TransformationStep... steps) {
    _steps = ImmutableList.copyOf(steps);
  }

  @Override
  public @Nullable <T> T accept(TransformationStepVisitor<T> visitor) {
    return visitor.visitApplyAny(this);
  }

  public @Nonnull List<TransformationStep> getSteps() {
    return _steps;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ApplyAny)) {
      return false;
    }
    return _steps.equals(((ApplyAny) obj)._steps);
  }

  @Override
  public int hashCode() {
    return _steps.hashCode();
  }
}
