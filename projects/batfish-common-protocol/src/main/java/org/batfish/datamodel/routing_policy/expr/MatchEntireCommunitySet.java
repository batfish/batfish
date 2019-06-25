package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public final class MatchEntireCommunitySet extends BooleanExpr {

  private static final String PROP_EXPR = "expr";

  private final CommunitySetExpr _expr;

  @JsonCreator
  private static MatchEntireCommunitySet create(@JsonProperty(PROP_EXPR) CommunitySetExpr expr) {
    checkArgument(expr != null, "%s must be provided", PROP_EXPR);
    return new MatchEntireCommunitySet(expr);
  }

  public MatchEntireCommunitySet(CommunitySetExpr expr) {
    _expr = expr;
  }

  @Override
  public Result evaluate(Environment environment) {
    throw new BatfishException("No implementation for MatchEntireCommunitySet.evaluate()");
  }

  @JsonProperty(PROP_EXPR)
  public CommunitySetExpr getExpr() {
    return _expr;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MatchEntireCommunitySet)) {
      return false;
    }
    MatchEntireCommunitySet other = (MatchEntireCommunitySet) obj;
    return Objects.equals(_expr, other._expr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_expr);
  }
}
