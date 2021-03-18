package org.batfish.datamodel.visitors;

import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.expr.MainRibRoutes;

/**
 * Visitor that evaluates a {@link org.batfish.datamodel.routing_policy.expr.RoutesExpr}, yielding a
 * {@link Collection<AbstractRoute>}.
 */
@ParametersAreNonnullByDefault
public final class RoutesExprEvaluator
    implements RoutesExprVisitor<Collection<AbstractRoute>, Environment> {

  @Nonnull
  public static RoutesExprEvaluator instance() {
    return INSTANCE;
  }

  @Nonnull
  @Override
  public Collection<AbstractRoute> visitMainRibRoutes(
      MainRibRoutes mainRibRoutes, Environment environment) {
    Collection<AbstractRoute> routes = environment.getMainRibRoutes();
    checkState(routes != null, "Cannot evaluate main RIB routes outside of data plane generation");
    return routes;
  }

  private static final RoutesExprEvaluator INSTANCE = new RoutesExprEvaluator();

  private RoutesExprEvaluator() {}
}
