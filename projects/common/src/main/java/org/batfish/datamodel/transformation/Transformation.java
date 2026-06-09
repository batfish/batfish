package org.batfish.datamodel.transformation;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.acl.AclLineMatchExpr;

/**
 * A representation of a composite packet transformation. When the guard matches the input flow,
 * apply the {@link TransformationStep TransformationSteps} and then apply the {@code andThen}
 * {@link Transformation}. When the guard does not match the flow, apply the {@code orElse} {@link
 * Transformation}. Stop upon reaching a {@code null} transformation.
 */
@ParametersAreNonnullByDefault
public final class Transformation implements Serializable {

  private static final String PROP_GUARD = "guard";
  private static final String PROP_TRANSFORMATION_STEPS = "transformationSteps";
  private static final String PROP_AND_THEN = "andThen";
  private static final String PROP_OR_ELSE = "orElse";

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
    _guard = guard;
    _transformationSteps = ImmutableList.copyOf(transformationSteps);
    _andThen = andThen;
    _orElse = orElse;
  }

  @JsonCreator
  private static Transformation jsonCreator(
      @JsonProperty(PROP_GUARD) AclLineMatchExpr guard,
      @JsonProperty(PROP_TRANSFORMATION_STEPS) List<TransformationStep> transformationSteps,
      @JsonProperty(PROP_AND_THEN) Transformation andThen,
      @JsonProperty(PROP_OR_ELSE) Transformation orElse) {
    checkNotNull(guard, PROP_GUARD + " cannot be null");
    return new Transformation(
        guard,
        // jackson serializes empty lists as null
        firstNonNull(transformationSteps, ImmutableList.of()),
        andThen,
        orElse);
  }

  /** A predicate specifying which flows should be transformed. */
  @JsonProperty(PROP_GUARD)
  public @Nonnull AclLineMatchExpr getGuard() {
    return _guard;
  }

  /** A list of transformation steps to apply if the guard matches the flow. */
  @JsonProperty(PROP_TRANSFORMATION_STEPS)
  public @Nonnull List<TransformationStep> getTransformationSteps() {
    return _transformationSteps;
  }

  /** The next transformation to apply (if any) when this one matches and transforms. */
  @JsonProperty(PROP_AND_THEN)
  public @Nullable Transformation getAndThen() {
    return _andThen;
  }

  /** The next transformation to apply (if any) when this one does not match. */
  @JsonProperty(PROP_OR_ELSE)
  public @Nullable Transformation getOrElse() {
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

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("guard", _guard)
        .add("transformationSteps", _transformationSteps)
        .add("andThen", _andThen)
        .add("orElse", _orElse)
        .toString();
  }

  @Serial
  private Object writeReplace() throws ObjectStreamException {
    return new SerializedForm(this);
  }

  /**
   * Serializable representation of a {@link Transformation} that avoids deep recursion.
   *
   * <p>A device's NAT rule-set is converted into a chain of {@link Transformation}s linked via
   * {@code orElse} (one link per rule). Configs with thousands of NAT rules produce a chain
   * thousands of links deep. Default Java serialization recurses once per link, so serializing or
   * deserializing such a chain overflows the stack. This proxy flattens the {@code orElse} spine
   * into a list, writing and reading it iteratively so chains of any depth survive a round trip.
   */
  private static final class SerializedForm implements Serializable {
    private final @Nonnull List<AclLineMatchExpr> _guards;
    private final @Nonnull List<List<TransformationStep>> _steps;
    private final @Nonnull List<Transformation> _andThens;

    SerializedForm(Transformation transformation) {
      _guards = new ArrayList<>();
      _steps = new ArrayList<>();
      _andThens = new ArrayList<>();
      // Walk the orElse spine iteratively; andThen sub-trees are not part of the spine and are
      // serialized normally (they are not deep in practice).
      for (Transformation t = transformation; t != null; t = t._orElse) {
        _guards.add(t._guard);
        _steps.add(t._transformationSteps);
        _andThens.add(t._andThen);
      }
    }

    @Serial
    private Object readResolve() throws ObjectStreamException {
      // Rebuild the orElse spine from the tail forward so each node's orElse is already built.
      Transformation orElse = null;
      for (int i = _guards.size() - 1; i >= 0; i--) {
        orElse = new Transformation(_guards.get(i), _steps.get(i), _andThens.get(i), orElse);
      }
      return orElse;
    }
  }
}
