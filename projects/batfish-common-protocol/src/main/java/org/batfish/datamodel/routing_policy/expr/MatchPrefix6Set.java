package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

@ParametersAreNonnullByDefault
public final class MatchPrefix6Set extends BooleanExpr {
  private static final String PROP_PREFIX = "prefix";
  private static final String PROP_PREFIX_SET = "prefixSet";
  /** */
  private static final long serialVersionUID = 1L;

  @Nonnull private Prefix6Expr _prefix;

  @Nonnull private Prefix6SetExpr _prefixSet;

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
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof MatchPrefix6Set)) {
      return false;
    }
    MatchPrefix6Set other = (MatchPrefix6Set) obj;
    return _prefix.equals(other._prefix) && _prefixSet.equals(other._prefixSet);
  }

  @Override
  public Result evaluate(Environment environment) {
    Prefix6 prefix = _prefix.evaluate(environment);
    boolean match = prefix != null && _prefixSet.matches(prefix, environment);
    Result result = new Result();
    result.setBooleanValue(match);
    return result;
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _prefix.hashCode();
    result = prime * result + _prefixSet.hashCode();
    return result;
  }
}
