package org.batfish.datamodel.transformation;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.acl.AclLineMatchExpr;

/** A representation of a composite packet transformation. */
@ParametersAreNonnullByDefault
public final class Transformation implements Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  private static final String ERROR_NO_STEPS =
      "Cannot create Transformation with zero transformationSteps. Consider using Noop.";

  public static final class Builder {
    private @Nonnull AclLineMatchExpr _guard;
    private @Nonnull List<TransformationStep> _transformationSteps;
    private @Nullable Transformation _andThen;
    private @Nullable Transformation _orElse;

    Builder(AclLineMatchExpr guard) {
      _guard = guard;
      _transformationSteps = ImmutableList.of();
    }

    public Builder apply(TransformationStep... transformationSteps) {
      checkArgument(transformationSteps.length > 0, ERROR_NO_STEPS);
      _transformationSteps = ImmutableList.copyOf(transformationSteps);
      return this;
    }

    public Builder apply(Iterable<TransformationStep> transformationSteps) {
      _transformationSteps = ImmutableList.copyOf(transformationSteps);
      return this;
    }

    public Builder apply(TransformationStep transformationStep) {
      _transformationSteps = ImmutableList.of(transformationStep);
      return this;
    }

    public Builder setAndThen(@Nullable Transformation andThen) {
      _andThen = andThen;
      return this;
    }

    public Builder setOrElse(@Nullable Transformation orElse) {
      _orElse = orElse;
      return this;
    }

    public Transformation build() {
      return new Transformation(_guard, _transformationSteps, _andThen, _orElse);
    }
  }

  public static Builder always() {
    return new Builder(TRUE);
  }

  public static Builder when(AclLineMatchExpr guard) {
    return new Builder(guard);
  }

  private final @Nonnull AclLineMatchExpr _guard;
  private final @Nonnull List<TransformationStep> _transformationSteps;
  private final @Nullable Transformation _andThen;
  private final @Nullable Transformation _orElse;

  public Transformation(
      @Nonnull AclLineMatchExpr guard,
      @Nonnull List<TransformationStep> transformationSteps,
      @Nullable Transformation andThen,
      @Nullable Transformation orElse) {
    checkArgument(!transformationSteps.isEmpty(), ERROR_NO_STEPS);
    _guard = guard;
    _transformationSteps = ImmutableList.copyOf(transformationSteps);
    _andThen = andThen;
    _orElse = orElse;
  }

  /** A predicate specifying which flows should be transformed. */
  @Nonnull
  public AclLineMatchExpr getGuard() {
    return _guard;
  }

  /** A list of transformation steps to apply if the guard matches the flow. */
  @Nonnull
  public List<TransformationStep> getTransformationSteps() {
    return _transformationSteps;
  }

  /** The next transformation to apply (if any) when this one matches and transforms. */
  @Nullable
  public Transformation getAndThen() {
    return _andThen;
  }

  /** The next transformation to apply (if any) when this one does not match. */
  @Nullable
  public Transformation getOrElse() {
    return _orElse;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Transformation)) {
      return false;
    }
    Transformation that = (Transformation) o;
    return Objects.equals(_guard, that._guard)
        && Objects.equals(_transformationSteps, that._transformationSteps)
        && Objects.equals(_andThen, that._andThen)
        && Objects.equals(_orElse, that._orElse);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_guard, _transformationSteps, _andThen, _orElse);
  }
}
