package org.batfish.representation.aws;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;

/**
 * Intermediate transformation structure used during conversion to build up vendor-independent
 * interface transformation
 */
@ParametersAreNonnullByDefault
final class LoadBalancerTransformation {

  private final @Nonnull AclLineMatchExpr _guard;
  private final @Nonnull TransformationStep _step;

  LoadBalancerTransformation(AclLineMatchExpr guard, TransformationStep step) {
    _guard = guard;
    _step = step;
  }

  @Nonnull
  public AclLineMatchExpr getGuard() {
    return _guard;
  }

  @Nonnull
  public TransformationStep getStep() {
    return _step;
  }

  @Nonnull
  public Transformation toTransformation(@Nullable Transformation elseTransformation) {
    return new Transformation(_guard, ImmutableList.of(_step), null, elseTransformation);
  }
}
