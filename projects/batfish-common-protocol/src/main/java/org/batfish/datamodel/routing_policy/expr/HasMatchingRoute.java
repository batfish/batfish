package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.visitors.RoutesExprEvaluator;

/**
 * A {@link BooleanExpr} that evaluates to true iff there is a route among routes represented by a
 * provided routes expression matching a provided condition.
 */
@ParametersAreNonnullByDefault
public final class HasMatchingRoute extends BooleanExpr {

  public HasMatchingRoute(RoutesExpr routesExpr, BooleanExpr matchExpr) {
    _routesExpr = routesExpr;
    _matchExpr = matchExpr;
  }

  @JsonCreator
  private static HasMatchingRoute create(
      @Nullable @JsonProperty(PROP_ROUTES_EXPR) RoutesExpr routesExpr,
      @Nullable @JsonProperty(PROP_MATCH_EXPR) BooleanExpr routeMatchExpr) {
    checkArgument(routesExpr != null, "Missing %s", PROP_ROUTES_EXPR);
    checkArgument(routeMatchExpr != null, "Missing %s", PROP_MATCH_EXPR);
    return new HasMatchingRoute(routesExpr, routeMatchExpr);
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitHasMatchingRoute(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    Collection<AbstractRoute> routeCandidates =
        _routesExpr.accept(RoutesExprEvaluator.instance(), environment);
    boolean matches = false;
    for (AbstractRoute routeCandidate : routeCandidates) {
      if (environment.withAlternateRoute(
          routeCandidate, () -> _matchExpr.evaluate(environment).getBooleanValue())) {
        matches = true;
        break;
      }
    }
    return new Result(matches);
  }

  @JsonProperty(PROP_ROUTES_EXPR)
  @Nonnull
  public RoutesExpr getRoutesExpr() {
    return _routesExpr;
  }

  @JsonProperty(PROP_MATCH_EXPR)
  @Nonnull
  public BooleanExpr getRouteMatchExpr() {
    return _matchExpr;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof HasMatchingRoute)) {
      return false;
    }
    HasMatchingRoute other = (HasMatchingRoute) obj;
    return _routesExpr.equals(other._routesExpr) && _matchExpr.equals(other._matchExpr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_routesExpr, _matchExpr);
  }

  private static final String PROP_ROUTES_EXPR = "routesExpr";
  private static final String PROP_MATCH_EXPR = "matchExpr";

  @Nonnull private final RoutesExpr _routesExpr;
  @Nonnull private final BooleanExpr _matchExpr;
}
