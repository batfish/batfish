package org.batfish.datamodel.visitors;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;

/**
 * Visitor that evaluates a {@link org.batfish.datamodel.routing_policy.expr.PrefixSpaceExpr} under
 * a provided {@link Environment}, returning a {@link PrefixSpace}.
 */
@ParametersAreNonnullByDefault
public final class PrefixSpaceEvaluator
    implements PrefixSpaceExprVisitor<PrefixSpace, Environment> {

  public static @Nonnull PrefixSpaceEvaluator instance() {
    return INSTANCE;
  }

  @Override
  public PrefixSpace visitExplicitPrefixSet(ExplicitPrefixSet explicitPrefixSet, Environment arg) {
    return explicitPrefixSet.getPrefixSpace();
  }

  private static final PrefixSpaceEvaluator INSTANCE = new PrefixSpaceEvaluator();
}
