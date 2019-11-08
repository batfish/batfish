package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.expr.LongExpr;

/**
 * An expression representing a route-target extended community via an expression for its global
 * administrator 32 bits and its local administrator 16 bits
 */
public class RouteTargetExtendedCommunityExpr extends CommunityExpr {

  public RouteTargetExtendedCommunityExpr(LongExpr gaExpr, IntExpr laExpr) {
    _gaExpr = gaExpr;
    _laExpr = laExpr;
  }

  @Override
  public <T, U> T accept(CommunityExprVisitor<T, U> visitor, U arg) {
    return visitor.visitRouteTargetExtendedCommunityExpr(this, arg);
  }

  @JsonProperty(PROP_GA_EXPR)
  public @Nonnull LongExpr getGaExpr() {
    return _gaExpr;
  }

  @JsonProperty(PROP_LA_EXPR)
  public IntExpr getLaExpr() {
    return _laExpr;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RouteTargetExtendedCommunityExpr)) {
      return false;
    }
    RouteTargetExtendedCommunityExpr rhs = (RouteTargetExtendedCommunityExpr) obj;
    return _gaExpr.equals(rhs._gaExpr) && _laExpr.equals(rhs._laExpr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_gaExpr, _laExpr);
  }

  private static final String PROP_GA_EXPR = "gaExpr";
  private static final String PROP_LA_EXPR = "laExpr";

  @JsonCreator
  private static @Nonnull RouteTargetExtendedCommunityExpr create(
      @JsonProperty(PROP_GA_EXPR) @Nullable LongExpr gaExpr,
      @JsonProperty(PROP_LA_EXPR) @Nullable IntExpr laExpr) {
    checkArgument(gaExpr != null, "Missing %s", PROP_GA_EXPR);
    checkArgument(laExpr != null, "Missing %s", PROP_LA_EXPR);
    return new RouteTargetExtendedCommunityExpr(gaExpr, laExpr);
  }

  private final @Nonnull LongExpr _gaExpr;
  private final @Nonnull IntExpr _laExpr;
}
