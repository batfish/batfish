package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.visitors.PrefixSpaceEvaluator;

/**
 * A {@link BooleanExpr} that evaluates to true iff there is a route in the RIB represented by a
 * provided RIB expression whose prefix is in the {@link PrefixSpace} represented by a provided
 * {@link PrefixSpaceExpr}.
 */
@ParametersAreNonnullByDefault
public final class RibIntersectsPrefixSpace extends BooleanExpr {

  public RibIntersectsPrefixSpace(RibExpr ribExpr, PrefixSpaceExpr prefixSpaceExpr) {
    _ribExpr = ribExpr;
    _prefixSpaceExpr = prefixSpaceExpr;
  }

  @JsonCreator
  private static RibIntersectsPrefixSpace create(
      @Nullable @JsonProperty(PROP_RIB_EXPR) RibExpr ribExpr,
      @Nullable @JsonProperty(PROP_PREFIX_SPACE_EXPR) PrefixSpaceExpr prefixSpaceExpr) {
    checkArgument(ribExpr != null, "Missing %s", PROP_RIB_EXPR);
    checkArgument(prefixSpaceExpr != null, "Missing %s", PROP_PREFIX_SPACE_EXPR);
    return new RibIntersectsPrefixSpace(ribExpr, prefixSpaceExpr);
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitRibIntersectsPrefixSpace(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    PrefixSpace prefixSpace = _prefixSpaceExpr.accept(PrefixSpaceEvaluator.instance(), environment);
    BiFunction<RibExpr, PrefixSpace, Boolean> evaluator =
        environment.getRibIntersectsPrefixSpaceEvaluator();
    checkState(
        evaluator != null, "Cannot check RIB routes; RIB state is not available at this time.");

    return new Result(
        environment.getRibIntersectsPrefixSpaceEvaluator().apply(_ribExpr, prefixSpace));
  }

  @JsonProperty(PROP_RIB_EXPR)
  @Nonnull
  public RibExpr getRibExpr() {
    return _ribExpr;
  }

  @JsonProperty(PROP_PREFIX_SPACE_EXPR)
  @Nonnull
  public PrefixSpaceExpr getPrefixSpaceExpr() {
    return _prefixSpaceExpr;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof RibIntersectsPrefixSpace)) {
      return false;
    }
    RibIntersectsPrefixSpace other = (RibIntersectsPrefixSpace) obj;
    return _ribExpr.equals(other._ribExpr) && _prefixSpaceExpr.equals(other._prefixSpaceExpr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ribExpr, _prefixSpaceExpr);
  }

  private static final String PROP_RIB_EXPR = "ribExpr";
  private static final String PROP_PREFIX_SPACE_EXPR = "prefixSpaceExpr";

  @Nonnull private final RibExpr _ribExpr;
  @Nonnull private final PrefixSpaceExpr _prefixSpaceExpr;
}
