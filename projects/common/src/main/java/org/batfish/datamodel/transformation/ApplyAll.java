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

/** Composite {@link TransformationStep} that applies all provided steps */
@ParametersAreNonnullByDefault
public class ApplyAll implements TransformationStep, Serializable {
  private static final String PROP_STEPS = "steps";

  @JsonCreator
  private static final @Nonnull ApplyAll create(
      @JsonProperty(PROP_STEPS) @Nullable Iterable<TransformationStep> steps) {
    return new ApplyAll(firstNonNull(steps, ImmutableList.of()));
  }

  private final @Nonnull List<TransformationStep> _steps;

  public ApplyAll(Iterable<TransformationStep> steps) {
    _steps = ImmutableList.copyOf(steps);
  }

  public ApplyAll(TransformationStep... steps) {
    _steps = ImmutableList.copyOf(steps);
  }

  @Override
  public @Nullable <T> T accept(TransformationStepVisitor<T> visitor) {
    return visitor.visitApplyAll(this);
  }

  public @Nonnull List<TransformationStep> getSteps() {
    return _steps;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ApplyAll)) {
      return false;
    }
    return _steps.equals(((ApplyAll) obj)._steps);
  }

  @Override
  public int hashCode() {
    return _steps.hashCode();
  }
}
