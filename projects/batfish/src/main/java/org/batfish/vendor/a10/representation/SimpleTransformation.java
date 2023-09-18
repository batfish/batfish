package org.batfish.vendor.a10.representation;

import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.transformation.TransformationStep;

/**
 * Intermediate transformation structure used during conversion to build up vendor-independent
 * transformations.
 */
final class SimpleTransformation {

  public @Nonnull AclLineMatchExpr getGuard() {
    return _guard;
  }

  public @Nonnull TransformationStep getStep() {
    return _step;
  }

  SimpleTransformation(AclLineMatchExpr guard, TransformationStep step) {
    _guard = guard;
    _step = step;
  }

  private final @Nonnull AclLineMatchExpr _guard;
  private final @Nonnull TransformationStep _step;
}
