package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * Boolean expression that tests whether an IPv6 prefix extracted from an {@link Environment} using
 * a given {@link Prefix6Expr} matches a given {@link Prefix6SetExpr}.
 */
@ParametersAreNonnullByDefault
public final class MatchPrefix6Set extends BooleanExpr {
  private static final String PROP_PREFIX = "prefix";
  private static final String PROP_PREFIX_SET = "prefixSet";

  @Nonnull private final Prefix6Expr _prefix;
  @Nonnull private final Prefix6SetExpr _prefixSet;

  @JsonCreator
  private static MatchPrefix6Set jsonCreator(
      @Nullable @JsonProperty(PROP_PREFIX) Prefix6Expr prefix,
      @Nullable @JsonProperty(PROP_PREFIX_SET) Prefix6SetExpr prefixSet) {
    checkArgument(prefix != null, "%s must be provided", PROP_PREFIX);
    checkArgument(prefixSet != null, "%s must be provided", PROP_PREFIX_SET);
    return new MatchPrefix6Set(prefix, prefixSet);
  }

  public MatchPrefix6Set(Prefix6Expr prefix, Prefix6SetExpr prefixSet) {
    _prefix = prefix;
    _prefixSet = prefixSet;
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchPrefix6Set(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    Prefix6 prefix = _prefix.evaluate(environment);
    return new Result(prefix != null && _prefixSet.matches(prefix, environment));
  }

  @JsonProperty(PROP_PREFIX)
  @Nonnull
  public Prefix6Expr getPrefix() {
    return _prefix;
  }

  @JsonProperty(PROP_PREFIX_SET)
  @Nonnull
  public Prefix6SetExpr getPrefixSet() {
    return _prefixSet;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof MatchPrefix6Set)) {
      return false;
    }
    MatchPrefix6Set other = (MatchPrefix6Set) obj;
    return Objects.equals(_prefix, other._prefix) && Objects.equals(_prefixSet, other._prefixSet);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_prefix, _prefixSet);
  }
}
