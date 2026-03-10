package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * Boolean expression that tests whether an IPv4 prefix extracted from an {@link Environment} using
 * a given {@link PrefixExpr} matches a given {@link PrefixSetExpr}.
 */
@ParametersAreNonnullByDefault
public final class MatchPrefixSet extends BooleanExpr {

  private static final String PROP_PREFIX = "prefix";
  private static final String PROP_PREFIX_SET = "prefixSet";

  private final @Nonnull PrefixExpr _prefix;
  private final @Nonnull PrefixSetExpr _prefixSet;

  @JsonCreator
  private static MatchPrefixSet jsonCreator(
      @JsonProperty(PROP_PREFIX) @Nullable PrefixExpr prefix,
      @JsonProperty(PROP_PREFIX_SET) @Nullable PrefixSetExpr prefixSet,
      @JsonProperty(PROP_COMMENT) @Nullable String comment) {
    checkArgument(prefix != null, "%s must be provided", PROP_PREFIX);
    checkArgument(prefixSet != null, "%s must be provided", PROP_PREFIX_SET);
    MatchPrefixSet ret = new MatchPrefixSet(prefix, prefixSet);
    ret.setComment(comment);
    return ret;
  }

  public MatchPrefixSet(PrefixExpr prefix, PrefixSetExpr prefixSet) {
    _prefix = prefix;
    _prefixSet = prefixSet;
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchPrefixSet(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    Prefix prefix = _prefix.evaluate(environment);
    return new Result(_prefixSet.matches(prefix, environment));
  }

  @JsonProperty(PROP_PREFIX)
  public @Nonnull PrefixExpr getPrefix() {
    return _prefix;
  }

  @JsonProperty(PROP_PREFIX_SET)
  public @Nonnull PrefixSetExpr getPrefixSet() {
    return _prefixSet;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof MatchPrefixSet)) {
      return false;
    }
    MatchPrefixSet other = (MatchPrefixSet) obj;
    return Objects.equals(_prefix, other._prefix) && Objects.equals(_prefixSet, other._prefixSet);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_prefix, _prefixSet);
  }

  @Override
  public String toString() {
    return toStringHelper().add(PROP_PREFIX, _prefix).add(PROP_PREFIX_SET, _prefixSet).toString();
  }
}
