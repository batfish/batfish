package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.routing_policy.expr.LongExpr;

/**
 * An expression representing a route-target extended community via expressions for its global
 * administrator and its local administrator.
 *
 * <p>Per RFC 4360, exactly one of the two may be 4 bytes wide; the type is derived from the values
 * by {@link org.batfish.datamodel.bgp.community.ExtendedCommunity#target}.
 */
public class RouteTargetExtendedCommunityExpr extends CommunityExpr {

  public RouteTargetExtendedCommunityExpr(LongExpr gaExpr, LongExpr laExpr) {
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
  public @Nonnull LongExpr getLaExpr() {
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
      @JsonProperty(PROP_LA_EXPR) @Nullable LongExpr laExpr) {
    checkArgument(gaExpr != null, "Missing %s", PROP_GA_EXPR);
    checkArgument(laExpr != null, "Missing %s", PROP_LA_EXPR);
    return new RouteTargetExtendedCommunityExpr(gaExpr, laExpr);
  }

  private final @Nonnull LongExpr _gaExpr;
  private final @Nonnull LongExpr _laExpr;
}
