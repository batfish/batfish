package org.batfish.datamodel.transformation;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;

/** Composite {@link TransformationStep} that applies all provided steps */
@ParametersAreNonnullByDefault
public class ApplyAll implements TransformationStep, Serializable {

  private static final String PROP_STEPS = "steps";
  private static final String PROP_TYPE = "type";
  private static final long serialVersionUID = 1L;

  @JsonCreator
  private static final @Nonnull ApplyAll create(
      @JsonProperty(PROP_TYPE) @Nullable TransformationType type,
      @JsonProperty(PROP_STEPS) @Nullable Iterable<TransformationStep> steps) {
    checkArgument(type != null, "Missing: %s", PROP_TYPE);
    if (steps != null) {
      steps.forEach(
          step ->
              checkArgument(
                  step.getType() == type,
                  "Expected only steps with type '%s' but got step: %s",
                  type,
                  step));
    }
    return new ApplyAll(type, firstNonNull(steps, ImmutableList.of()));
  }

  private final @Nonnull List<TransformationStep> _steps;

  private final @Nonnull TransformationType _type;

  public ApplyAll(TransformationType type, Iterable<TransformationStep> steps) {
    _type = type;
    _steps = ImmutableList.copyOf(steps);
  }

  public ApplyAll(TransformationType type, TransformationStep... steps) {
    _type = type;
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
  public @Nonnull TransformationType getType() {
    return _type;
  }
}
