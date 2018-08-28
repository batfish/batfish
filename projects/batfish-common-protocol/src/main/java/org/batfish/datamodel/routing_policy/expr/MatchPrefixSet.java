package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

@ParametersAreNonnullByDefault
public final class MatchPrefixSet extends BooleanExpr {

  private static final long serialVersionUID = 1L;

  private static final String PROP_PREFIX = "prefix";
  private static final String PROP_PREFIX_SET = "prefixSet";

  @Nonnull private PrefixExpr _prefix;

  @Nonnull private PrefixSetExpr _prefixSet;

  @JsonCreator
  private static MatchPrefixSet jsonCreator(
      @Nullable @JsonProperty(PROP_PREFIX) PrefixExpr prefix,
      @Nullable @JsonProperty(PROP_PREFIX_SET) PrefixSetExpr prefixSet,
      @Nullable @JsonProperty(PROP_COMMENT) String comment) {
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
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof MatchPrefixSet)) {
      return false;
    }
    MatchPrefixSet other = (MatchPrefixSet) obj;
    return _prefix.equals(other._prefix) && _prefixSet.equals(other._prefixSet);
  }

  @Override
  public Result evaluate(Environment environment) {
    Prefix prefix = _prefix.evaluate(environment);
    boolean match = _prefixSet.matches(prefix, environment);
    Result result = new Result();
    result.setBooleanValue(match);
    return result;
  }

  @JsonProperty(PROP_PREFIX)
  @Nonnull
  public PrefixExpr getPrefix() {
    return _prefix;
  }

  @JsonProperty(PROP_PREFIX_SET)
  @Nonnull
  public PrefixSetExpr getPrefixSet() {
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

  @Override
  public String toString() {
    return toStringHelper().add(PROP_PREFIX, _prefix).add(PROP_PREFIX_SET, _prefixSet).toString();
  }
}
