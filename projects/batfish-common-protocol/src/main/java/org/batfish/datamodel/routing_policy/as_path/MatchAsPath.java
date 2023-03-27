package org.batfish.datamodel.routing_policy.as_path;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprVisitor;

/**
 * A {@link BooleanExpr} that evaluates to {code true} iff a route's as-path is matched by a given
 * {@link AsPathMatchExpr}.
 */
public final class MatchAsPath extends BooleanExpr {

  public static @Nonnull MatchAsPath of(AsPathExpr asPathExpr, AsPathMatchExpr asPathMatchExpr) {
    return new MatchAsPath(asPathExpr, asPathMatchExpr);
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchAsPath(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    Optional<AsPathContext> maybeCtx = AsPathContext.fromEnvironment(environment);
    if (!maybeCtx.isPresent()) {
      return new Result(false);
    }
    AsPathContext ctx = maybeCtx.get();
    AsPath asPath = _asPathExpr.accept(AsPathExprEvaluator.instance(), ctx);
    return new Result(_asPathMatchExpr.accept(ctx.getAsPathMatchExprEvaluator(), asPath));
  }

  @JsonProperty(PROP_AS_PATH_EXPR)
  public @Nonnull AsPathExpr getAsPathExpr() {
    return _asPathExpr;
  }

  @JsonProperty(PROP_AS_PATH_MATCH_EXPR)
  public @Nonnull AsPathMatchExpr getAsPathMatchExpr() {
    return _asPathMatchExpr;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof MatchAsPath)) {
      return false;
    }
    MatchAsPath that = (MatchAsPath) obj;
    return _asPathExpr.equals(that._asPathExpr) && _asPathMatchExpr.equals(that._asPathMatchExpr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_asPathExpr, _asPathMatchExpr);
  }

  @Override
  public String toString() {
    return toStringHelper()
        .add(PROP_AS_PATH_EXPR, _asPathExpr)
        .add(PROP_AS_PATH_MATCH_EXPR, _asPathMatchExpr)
        .toString();
  }

  private static final String PROP_AS_PATH_EXPR = "asPathExpr";
  private static final String PROP_AS_PATH_MATCH_EXPR = "asPathMatchExpr";

  @JsonCreator
  private static @Nonnull MatchAsPath create(
      @JsonProperty(PROP_AS_PATH_EXPR) @Nullable AsPathExpr asPathExpr,
      @JsonProperty(PROP_AS_PATH_MATCH_EXPR) @Nullable AsPathMatchExpr asPathMatchExpr) {
    checkArgument(asPathExpr != null, "Missing %s", PROP_AS_PATH_EXPR);
    checkArgument(asPathMatchExpr != null, "Missing %s", PROP_AS_PATH_MATCH_EXPR);
    return of(asPathExpr, asPathMatchExpr);
  }

  private final @Nonnull AsPathExpr _asPathExpr;
  private final @Nonnull AsPathMatchExpr _asPathMatchExpr;

  private MatchAsPath(AsPathExpr asPathExpr, AsPathMatchExpr asPathMatchExpr) {
    _asPathExpr = asPathExpr;
    _asPathMatchExpr = asPathMatchExpr;
  }
}
