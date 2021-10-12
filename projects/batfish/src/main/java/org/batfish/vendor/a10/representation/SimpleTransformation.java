package org.batfish.vendor.a10.representation;

import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.transformation.TransformationStep;

/**
 * Intermediate transformation structure used during conversion to build up vendor-independent
 * transformations.
 */
final class SimpleTransformation {

  @Nonnull
  public AclLineMatchExpr getGuard() {
    return _guard;
  }

  @Nonnull
  public TransformationStep getStep() {
    return _step;
  }

  SimpleTransformation(AclLineMatchExpr guard, TransformationStep step) {
    _guard = guard;
    _step = step;
  }

  @Nonnull private final AclLineMatchExpr _guard;
  @Nonnull private final TransformationStep _step;
}
