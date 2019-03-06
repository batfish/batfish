package org.batfish.representation.f5_bigip;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.transformation.TransformationStep;

/**
 * Intermediate transformation structure used during conversion to build up vendor-independent
 * interface transformation
 */
@ParametersAreNonnullByDefault
final class SimpleTransformation {

  private final @Nonnull AclLineMatchExpr _guard;
  private final @Nonnull TransformationStep _step;

  SimpleTransformation(AclLineMatchExpr guard, TransformationStep step) {
    _guard = guard;
    _step = step;
  }

  public @Nonnull AclLineMatchExpr getGuard() {
    return _guard;
  }

  public @Nonnull TransformationStep getStep() {
    return _step;
  }
}
